package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Calendar;
import java.rmi.RemoteException;

import client.Equip;
import client.IItem;
import client.Item;
import client.GameConstants;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.OdinSEA;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import handling.world.PartyOperation;
import handling.world.MaplePartyCharacter;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.Randomizer;
import server.MapleInventoryManipulator;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MapleLifeFactory;
import server.life.Spawns;
import server.life.SpawnPoint;
import server.life.SpawnPointAreaBoss;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.life.MapleMonsterInformationProvider;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.packet.TestPacket;
import tools.packet.PetPacket;
import tools.packet.MobPacket;

public class MapleMap {

	private final Map<Integer, MapleMapObject> mapobjects = new HashMap<Integer, MapleMapObject>();
	private final Collection<Spawns> monsterSpawn = new LinkedList<Spawns>();
	private final AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
	private final List<MapleCharacter> characters = new ArrayList<MapleCharacter>();
	private final Map<Integer, MaplePortal> portals = new HashMap<Integer, MaplePortal>();
	private final List<Rectangle> areas = new ArrayList<Rectangle>();
	private MapleFootholdTree footholds = null;
	private float monsterRate, recoveryRate;
	private MapleMapEffect mapEffect;
	private byte channel;
	private short decHP = 0, createMobInterval = 9000;
	private int protectItem = 0, mapid, returnMapId, timeLimit, fieldLimit, maxRegularSpawn = 0;
	private int runningOid = 9, forcedReturnMap = 999999999;
	private boolean town, clock, personalShop, everlast = false, dropsDisabled = false;
	private String mapName, streetName, onUserEnter, onFirstUserEnter;
	private final Lock mutex = new ReentrantLock();
	private final int world;

	public MapleMap(final int mapid, final int channel, final int returnMapId, final float monsterRate, final int world) {
	this.mapid = mapid;
	this.channel = (byte) channel;
	this.returnMapId = returnMapId;
	this.monsterRate = monsterRate;
	this.world = world;
	}

	public int getWorld() {
		return world;
	}

	public final void toggleDrops() {
	this.dropsDisabled = !dropsDisabled;
	}

	public final int getId() {
	return mapid;
	}

	public final MapleMap getReturnMap() {
	return ChannelServer.getInstance(channel).getMapFactory(world).getMap(returnMapId);
	}

	public final int getReturnMapId() {
	return returnMapId;
	}

	public final int getForcedReturnId() {
	return forcedReturnMap;
	}

	public final MapleMap getForcedReturnMap() {
	return ChannelServer.getInstance(channel).getMapFactory(world).getMap(forcedReturnMap);
	}

	public final void setForcedReturnMap(final int map) {
	this.forcedReturnMap = map;
	}

	public final float getRecoveryRate() {
	return recoveryRate;
	}

	public final void setRecoveryRate(final float recoveryRate) {
	this.recoveryRate = recoveryRate;
	}

	public final int getFieldLimit() {
	return fieldLimit;
	}

	public final void setFieldLimit(final int fieldLimit) {
	this.fieldLimit = fieldLimit;
	}

	public final void setCreateMobInterval(final short createMobInterval) {
	this.createMobInterval = createMobInterval;
	}

	public final void setTimeLimit(final int timeLimit) {
	this.timeLimit = timeLimit;
	}

	public final void setMapName(final String mapName) {
	this.mapName = mapName;
	}

	public final String getMapName() {
	return mapName;
	}

	public final String getStreetName() {
	return streetName;
	}

	public final void setFirstUserEnter(final String onFirstUserEnter) {
	this.onFirstUserEnter = onFirstUserEnter;
	}

	public final void setUserEnter(final String onUserEnter) {
	this.onUserEnter = onUserEnter;
	}

	public final boolean hasClock() {
	return clock;
	}

	public final void setClock(final boolean hasClock) {
	this.clock = hasClock;
	}

	public final boolean isTown() {
	return town;
	}

	public final void setTown(final boolean town) {
	this.town = town;
	}

	public final boolean allowPersonalShop() {
	return personalShop;
	}

	public final void setPersonalShop(final boolean personalShop) {
	this.personalShop = personalShop;
	}

	public final void setStreetName(final String streetName) {
	this.streetName = streetName;
	}

	public final void setEverlast(final boolean everlast) {
	this.everlast = everlast;
	}

	public final boolean getEverlast() {
	return everlast;
	}

	public final int getHPDec() {
	return decHP;
	}

	public final void setHPDec(final int delta) {
	decHP = (short) delta;
	}

	public final int getHPDecProtect() {
	return protectItem;
	}

	public final void setHPDecProtect(final int delta) {
	this.protectItem = delta;
	}

	public final int getCurrentPartyId() {
	mutex.lock();
	try {
		final Iterator<MapleCharacter> ltr = characters.iterator();
		MapleCharacter chr;
		while (ltr.hasNext()) {
		chr = ltr.next();
		if (chr.getPartyId() != -1) {
			return chr.getPartyId();
		}
		}
	} finally {
		mutex.unlock();
	}
	return -1;
	}

	public final void addMapObject(final MapleMapObject mapobject) {
	mutex.lock();

	try {
		runningOid++;
		mapobject.setObjectId(runningOid);
		mapobjects.put(runningOid, mapobject);
	} finally {
		mutex.unlock();
	}
	}

	private final void spawnAndAddRangedMapObject(final MapleMapObject mapobject, final DelayedPacketCreation packetbakery, final SpawnCondition condition) {
	mutex.lock();

	try {
		runningOid++;
		mapobject.setObjectId(runningOid);
		mapobjects.put(runningOid, mapobject);

		final Iterator<MapleCharacter> ltr = characters.iterator();
		MapleCharacter chr;
		while (ltr.hasNext()) {
		chr = ltr.next();
		if (condition == null || condition.canSpawn(chr)) {
			if (chr.getPosition().distanceSq(mapobject.getPosition()) <= GameConstants.maxViewRangeSq()) {
			packetbakery.sendPackets(chr.getClient());
			chr.addVisibleMapObject(mapobject);
			}
		}
		}
	} finally {
		mutex.unlock();
	}
	}

	public final void removeMapObject(final int num) {
	mutex.lock();
	try {
		mapobjects.remove(Integer.valueOf(num));
	} finally {
		mutex.unlock();
	}
	}

	public final void removeMapObject(final MapleMapObject obj) {
	mutex.lock();
	try {
		mapobjects.remove(Integer.valueOf(obj.getObjectId()));
	} finally {
		mutex.unlock();
	}
	}

	public final Point calcPointBelow(final Point initial) {
	final MapleFoothold fh = footholds.findBelow(initial);
	if (fh == null) {
		return null;
	}
	int dropY = fh.getY1();
	if (!fh.isWall() && fh.getY1() != fh.getY2()) {
		final double s1 = Math.abs(fh.getY2() - fh.getY1());
		final double s2 = Math.abs(fh.getX2() - fh.getX1());
		if (fh.getY2() < fh.getY1()) {
		dropY = fh.getY1() - (int) (Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
		} else {
		dropY = fh.getY1() + (int) (Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
		}
	}
	return new Point(initial.x, dropY);
	}

	private final Point calcDropPos(final Point initial, final Point fallback) {
	final Point ret = calcPointBelow(new Point(initial.x, initial.y - 50));
	if (ret == null) {
		return fallback;
	}
	return ret;
	}

	private final void dropFromMonster(final MapleCharacter chr, final MapleMonster mob) {
		if (dropsDisabled || mob.dropsDisabled()) {
			return;
		}
		final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		final byte droptype = (byte) (mob.getStats().isExplosiveReward() ? 3 : mob.getStats().isFfaLoot() ? 2 : chr.getParty() != null ? 1 : 0);
		final int mobpos = mob.getPosition().x, chServerrate = ChannelServer.getInstance(channel).getDropRate();
		IItem idrop;
		byte d = 1;
		Point pos = new Point(0, mob.getPosition().y);
		final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
		final List<MonsterDropEntry> dropEntry = new ArrayList<MonsterDropEntry>(mi.retrieveDrop(mob.getId()));
		Collections.shuffle(dropEntry);
		for (final MonsterDropEntry de : dropEntry) {
			if (Randomizer.nextInt(999999) < de.chance * chServerrate) {
				if (droptype == 3) {
					pos.x = (int) (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
				} else {
					pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
				}
				if (de.itemId == 0) { // meso
					int mesos = Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum;
					if (mesos > 0) {
						if (chr.getBuffedValue(MapleBuffStat.MESOUP) != null) {
							mesos = (int) (mesos * chr.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
						}
						spawnMobMesoDrop(mesos * chr.getClient().getChannelServer().getMesoRate(), calcDropPos(pos, mob.getPosition()), mob, chr, false, droptype);
					}
				} else {
					if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
						idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
					} else {
						idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1), (byte) 0);
					}
					spawnMobDrop(idrop, calcDropPos(pos, mob.getPosition()), mob, chr, droptype, de.questid);
				}
				d++;
			}
		}
		final List<MonsterGlobalDropEntry> globalEntry = mi.getGlobalDrop();
		// Global Drops
		for (final MonsterGlobalDropEntry de : globalEntry) {
			if (Randomizer.nextInt(999999) < de.chance) {
				if (droptype == 3) {
					pos.x = (int) (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
				} else {
					pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
				}
				if (de.itemId == 0) { // Random Cash xD
					int cashGain;
					cashGain = (int) (Math.random() * 100);
					if (cashGain < 20) {
						cashGain = 20;
						chr.modifyCSPoints(1, cashGain, true);
					} else {
						chr.modifyCSPoints(1, cashGain, true);
					}
				} else {
					if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
						idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
					} else {
						idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1), (byte) 0);
					}
					spawnMobDrop(idrop, calcDropPos(pos, mob.getPosition()), mob, chr, droptype, de.questid);
					d++;
				}
			}
		}
	}

	private final void killMonster(final MapleMonster monster) { // For mobs with removeAfter
		spawnedMonstersOnMap.decrementAndGet();
		monster.setHp(0);
		monster.spawnRevives(this);
		broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 1));
		removeMapObject(monster);
	}

	public final void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean second, final byte animation) {
		if (monster.getId() == 8810018 && !second) {
			MapTimer.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					killMonster(monster, chr, true, true, (byte) 1);
					killAllMonsters(true);
				}
			}, 3000);
			return;
		}
		spawnedMonstersOnMap.decrementAndGet();
		removeMapObject(monster);
		MapleCharacter dropOwner = monster.killBy(chr);
		broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animation));
		if (monster.getBuffToGive() > -1) {
			final int buffid = monster.getBuffToGive();
			final MapleStatEffect buff = MapleItemInformationProvider.getInstance().getItemEffect(buffid);
			for (final MapleMapObject mmo : getAllPlayer()) {
				final MapleCharacter c = (MapleCharacter) mmo;
				if (c.isAlive()) {
					buff.applyTo(c);
					switch (monster.getId()) {
					case 8810018:
					case 8820001:
						c.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(buffid, 11)); // HT nine spirit
						broadcastMessage(c, MaplePacketCreator.showBuffeffect(c.getId(), buffid, 11), false); // HT nine spirit
						break;
					}
				}
			}
		}
		final int mobid = monster.getId();
		if (mobid == 8810018) {
			try {
				ChannelServer.getInstance(channel).getWorldInterface().broadcastMessage(MaplePacketCreator.serverNotice(6, "To the crew that have finally conquered Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!").getBytes());
			} catch (RemoteException e) {
				ChannelServer.getInstance(channel).reconnectWorld();
			}
			FileoutputUtil.log(FileoutputUtil.Horntail_Log, MapDebug_Log());
		} else if (mobid == 8820001) {
			try {
				ChannelServer.getInstance(channel).getWorldInterface().broadcastMessage(MaplePacketCreator.serverNotice(6, "Expedition who defeated Pink Bean with invicible passion! You are the true timeless hero!").getBytes());
			} catch (RemoteException e) {
				ChannelServer.getInstance(channel).reconnectWorld();
			}
			FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, MapDebug_Log());
		} else if (mobid >= 8800003 && mobid <= 8800010) {
			boolean makeZakReal = true;
			final Collection<MapleMapObject> objects = getAllMonster();
			for (final MapleMapObject object : objects) {
				final MapleMonster mons = ((MapleMonster) object);
				if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
					makeZakReal = false;
					break;
				}
			}
			if (makeZakReal) {
				for (final MapleMapObject object : objects) {
					final MapleMonster mons = ((MapleMonster) object);
					if (mons.getId() == 8800000) {
						final Point pos = mons.getPosition();
						this.killAllMonsters(true);
						spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), pos);
						break;
					}
				}
			}
		}
		if (withDrops) {
			if (dropOwner == null) {
				dropOwner = chr;
			}
			dropFromMonster(dropOwner, monster);
		}
	}

	public final void killAllMonsters(final boolean animate) {
	for (final MapleMapObject monstermo : getAllMonster()) {
		final MapleMonster monster = (MapleMonster) monstermo;
		spawnedMonstersOnMap.decrementAndGet();
		monster.setHp(0);
		broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
		removeMapObject(monster);
	}
	}

	public final void killMonster(final int monsId) {
	for (final MapleMapObject mmo : getAllMonster()) {
		if (((MapleMonster) mmo).getId() == monsId) {
		spawnedMonstersOnMap.decrementAndGet();
		removeMapObject(mmo);
		broadcastMessage(MobPacket.killMonster(mmo.getObjectId(), 1));
		break;
		}
	}
	}

	private final String MapDebug_Log() {
	final StringBuilder sb = new StringBuilder("Defeat time : ");
	sb.append(FileoutputUtil.CurrentReadable_Time());

	sb.append(" | Mapid : ").append(this.mapid);

	final List<MapleMapObject> players = getAllPlayer();
	sb.append(" Users [").append(players.size()).append("] | ");
	final Iterator<MapleMapObject> itr = players.iterator();
	while (itr.hasNext()) {
		sb.append(((MapleCharacter) itr.next()).getName()).append(", ");
	}
	return sb.toString();
	}

	public final void destroyReactor(final int oid) {
	final MapleReactor reactor = getReactorByOid(oid);
	broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
	reactor.setAlive(false);
	removeMapObject(reactor);
	reactor.setTimerActive(false);

	if (reactor.getDelay() > 0) {
		MapTimer.getInstance().schedule(new Runnable() {

		@Override
		public final void run() {
			respawnReactor(reactor);
		}
		}, reactor.getDelay());
	}
	}

	/*
	 * command to reset all item-reactors in a map to state 0 for GM/NPC use - not tested (broken reactors get removed
	 * from mapobjects when destroyed) Should create instances for multiple copies of non-respawning reactors...
	 */
	public final void resetReactors() {
	for (final MapleMapObject o : getAllReactor()) {
		((MapleReactor) o).setState((byte) 0);
		((MapleReactor) o).setTimerActive(false);
		broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 0));
	}
	}

	public final void setReactorState() {
	for (final MapleMapObject o : getAllReactor()) {
		((MapleReactor) o).setState((byte) 1);
		((MapleReactor) o).setTimerActive(false);
		broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 1));
	}
	}

	/*
	 * command to shuffle the positions of all reactors in a map for PQ purposes (such as ZPQ/LMPQ)
	 */
	public final void shuffleReactors() {
	List<Point> points = new ArrayList<Point>();

	for (final MapleMapObject o : getAllReactor()) {
		points.add(((MapleReactor) o).getPosition());
	}
	Collections.shuffle(points);
	for (final MapleMapObject o : getAllReactor()) {
		((MapleReactor) o).setPosition(points.remove(points.size() - 1));
	}
	}

	/**
	 * Automagically finds a new controller for the given monster from the chars on the map...
	 *
	 * @param monster
	 */
	public final void updateMonsterController(final MapleMonster monster) {
	if (!monster.isAlive()) {
		return;
	}
	if (monster.getController() != null) {
		if (monster.getController().getMap() != this) {
		monster.getController().stopControllingMonster(monster);
		} else { // Everything is fine :)
		return;
		}
	}
	int mincontrolled = -1;
	MapleCharacter newController = null;

	mutex.lock();

	try {
		final Iterator<MapleCharacter> ltr = characters.iterator();
		MapleCharacter chr;
		while (ltr.hasNext()) {
		chr = ltr.next();
		if (!chr.isHidden() && (chr.getControlledMonsters().size() < mincontrolled || mincontrolled == -1)) {
			mincontrolled = chr.getControlledMonsters().size();
			newController = chr;
		}
		}
	} finally {
		mutex.unlock();
	}
	if (newController != null) {
		if (monster.isFirstAttack()) {
		newController.controlMonster(monster, true);
		monster.setControllerHasAggro(true);
		monster.setControllerKnowsAboutAggro(true);
		} else {
		newController.controlMonster(monster, false);
		}
	}
	}

	/*    public Collection<MapleMapObject> getMapObjects() {
	return Collections.unmodifiableCollection(mapobjects.values());
	}*/
	public final MapleMapObject getMapObject(final int oid) {
	return mapobjects.get(oid);
	}

	public final int containsNPC(final int npcid) {
	for (MapleMapObject obj : getAllNPC()) {
		if (((MapleNPC) obj).getId() == npcid) {
		return obj.getObjectId();
		}
	}
	return -1;
	}

	/**
	 * returns a monster with the given oid, if no such monster exists returns null
	 *
	 * @param oid
	 * @return
	 */
	public final MapleMonster getMonsterByOid(final int oid) {
	final MapleMapObject mmo = getMapObject(oid);
	if (mmo == null) {
		return null;
	}
	if (mmo.getType() == MapleMapObjectType.MONSTER) {
		return (MapleMonster) mmo;
	}
	return null;
	}

	public final MapleNPC getNPCByOid(final int oid) {
	final MapleMapObject mmo = getMapObject(oid);
	if (mmo == null) {
		return null;
	}
	if (mmo.getType() == MapleMapObjectType.NPC) {
		return (MapleNPC) mmo;
	}
	return null;
	}

	public final MapleReactor getReactorByOid(final int oid) {
	final MapleMapObject mmo = getMapObject(oid);
	if (mmo == null) {
		return null;
	}
	if (mmo.getType() == MapleMapObjectType.REACTOR) {
		return (MapleReactor) mmo;
	}
	return null;
	}

	public final MapleReactor getReactorByName(final String name) {
	for (final MapleMapObject obj : getAllReactor()) {
		if (((MapleReactor) obj).getName().equals(name)) {
		return (MapleReactor) obj;
		}
	}
	return null;
	}

	public final void spawnNpc(final int id, final Point pos) {
	final MapleNPC npc = MapleLifeFactory.getNPC(id);
	npc.setPosition(pos);
	npc.setCy(pos.y);
	npc.setRx0(pos.x + 50);
	npc.setRx1(pos.x - 50);
	npc.setFh(getFootholds().findBelow(pos).getId());
	npc.setCustom(true);
	addMapObject(npc);
	broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
	}

	public final void removeNpc(final int id) {
	final List<MapleMapObject> npcs = getAllNPC();
	for (final MapleMapObject npcmo : npcs) {
		final MapleNPC npc = (MapleNPC) npcmo;
		if (npc.isCustom() && npc.getId() == id) {
		broadcastMessage(MaplePacketCreator.removeNPC(npc.getObjectId()));
		removeMapObject(npc.getObjectId());
		}
	}
	}

	public final void spawnMonster_sSack(final MapleMonster mob, final Point pos, final int spawnType) {
	final Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
	mob.setPosition(spos);
	spawnMonster(mob, spawnType);
	}

	public final void spawnMonsterOnGroundBelow(final MapleMonster mob, final Point pos) {
	final Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
	mob.setPosition(spos);
	spawnMonster(mob, -1);
	}

	public final void spawnZakum(final Point pos) {
	final MapleMonster mainb = MapleLifeFactory.getMonster(8800000);
	final Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
	mainb.setPosition(spos);
	mainb.setFake(true);

	// Might be possible to use the map object for reference in future.
	spawnFakeMonster(mainb);

	final int[] zakpart = {8800003, 8800004, 8800005, 8800006, 8800007,
		8800008, 8800009, 8800010};

	for (final int i : zakpart) {
		final MapleMonster part = MapleLifeFactory.getMonster(i);
		part.setPosition(spos);

		spawnMonster(part, -1);
	}
	}

	public final void spawnFakeMonsterOnGroundBelow(final MapleMonster mob, final Point pos) {
	Point spos = new Point(pos.x, pos.y - 1);
	spos = calcPointBelow(spos);
	spos.y -= 1;
	mob.setPosition(spos);
	spawnFakeMonster(mob);
	}

	private final void checkRemoveAfter(final MapleMonster monster) {
	final int ra = monster.getStats().getRemoveAfter();

	if (ra > 0) {
		MapTimer.getInstance().schedule(new Runnable() {

		@Override
		public final void run() {
			if (monster != null) {
			killMonster(monster);
			}
		}
		}, ra * 1000);
	}
	}

	public final void spawnRevives(final MapleMonster monster, final int oid) {
	monster.setMap(this);
	checkRemoveAfter(monster);

	spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

		@Override
		public final void sendPackets(MapleClient c) {
		c.getSession().write(MobPacket.spawnMonster(monster, -1, 0, oid)); // TODO effect
		}
	}, null);
	updateMonsterController(monster);

	spawnedMonstersOnMap.incrementAndGet();
	}

	public final void spawnMonster(final MapleMonster monster, final int spawnType) {
	monster.setMap(this);
	checkRemoveAfter(monster);

	spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

		public final void sendPackets(MapleClient c) {
		c.getSession().write(MobPacket.spawnMonster(monster, spawnType, 0, 0));
		}
	}, null);
	updateMonsterController(monster);

	spawnedMonstersOnMap.incrementAndGet();
	}

	public final void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
	try {
		monster.setMap(this);
		monster.setPosition(pos);

		spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

		@Override
		public final void sendPackets(MapleClient c) {
			c.getSession().write(MobPacket.spawnMonster(monster, -1, effect, 0));
		}
		}, null);
		updateMonsterController(monster);

		spawnedMonstersOnMap.incrementAndGet();
	} catch (Exception e) {
	}
	}

	public final void spawnFakeMonster(final MapleMonster monster) {
	monster.setMap(this);
	monster.setFake(true);

	spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

		@Override
		public final void sendPackets(MapleClient c) {
		c.getSession().write(MobPacket.spawnMonster(monster, -1, 0xfc, 0));
//		c.getSession().write(MobPacket.spawnFakeMonster(monster, 0));
		}
	}, null);
	updateMonsterController(monster);

	spawnedMonstersOnMap.incrementAndGet();
	}

	public final void spawnReactor(final MapleReactor reactor) {
	reactor.setMap(this);

	spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {

		@Override
		public final void sendPackets(MapleClient c) {
		c.getSession().write(MaplePacketCreator.spawnReactor(reactor));
		}
	}, null);
	}

	private final void respawnReactor(final MapleReactor reactor) {
	reactor.setState((byte) 0);
	reactor.setAlive(true);
	spawnReactor(reactor);
	}

	public final void spawnDoor(final MapleDoor door) {
	spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

		public final void sendPackets(MapleClient c) {
		c.getSession().write(MaplePacketCreator.spawnDoor(door.getOwner().getId(), door.getTargetPosition(), false));
		if (door.getOwner().getParty() != null && (door.getOwner() == c.getPlayer() || door.getOwner().getParty().containsMembers(new MaplePartyCharacter(c.getPlayer())))) {
			c.getSession().write(MaplePacketCreator.partyPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
		}
		c.getSession().write(MaplePacketCreator.spawnPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
		c.getSession().write(MaplePacketCreator.enableActions());
		}
	}, new SpawnCondition() {

		public final boolean canSpawn(final MapleCharacter chr) {
		return chr.getMapId() == door.getTarget().getId() || chr == door.getOwner() && chr.getParty() == null;
		}
	});
	}

	public final void spawnDragon(final MapleDragon summon) {
	spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

		@Override
		public void sendPackets(MapleClient c) {
		c.getSession().write(MaplePacketCreator.spawnDragon(summon));
		}
	}, null);
	}

	public final void spawnSummon(final MapleSummon summon) {
	spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

		@Override
		public void sendPackets(MapleClient c) {
		c.getSession().write(MaplePacketCreator.spawnSummon(summon, summon.getSkillLevel(), true));
		}
	}, null);
	}

	public final void spawnMist(final MapleMist mist, final int duration, boolean poison, boolean fake) {
	spawnAndAddRangedMapObject(mist, new DelayedPacketCreation() {

		@Override
		public void sendPackets(MapleClient c) {
		c.getSession().write(MaplePacketCreator.spawnMist(mist));
		}
	}, null);

	final MapTimer tMan = MapTimer.getInstance();
	final ScheduledFuture<?> poisonSchedule;

	if (poison) {
		poisonSchedule = tMan.register(new Runnable() {

		@Override
		public void run() {
			for (final MapleMapObject mo : getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
			if (mist.makeChanceResult()) {
				((MapleMonster) mo).applyStatus(mist.getOwner(), new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), mist.getSourceSkill(), null, false), true, duration, false);
			}
			}
		}
		}, 2000, 2500);
	} else {
		poisonSchedule = null;
	}
	tMan.schedule(new Runnable() {

		@Override
		public void run() {
		broadcastMessage(MaplePacketCreator.removeMist(mist.getObjectId()));
		removeMapObject(mist);
		if (poisonSchedule != null) {
			poisonSchedule.cancel(false);
		}
		}
	}, duration);
	}

	public final void disappearingItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, final Point pos) {
		final Point droppos = calcDropPos(pos, pos);
		final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 1, false);
		broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 3), drop.getPosition());
	}

	public final void spawnMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
		final Point droppos = calcDropPos(position, position);
		final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);
		spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
			@Override
			public void sendPackets(MapleClient c) {
				c.getSession().write(MaplePacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), droppos, (byte) 1));
			}
		}, null);
		if (!everlast) {
			MapTimer.getInstance().schedule(new ExpireMapItemJob(mdrop), 180000);
		}
	}

	public final void spawnMobMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
		final MapleMapItem mdrop = new MapleMapItem(meso, position, dropper, owner, droptype, playerDrop);
		spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
			@Override
			public void sendPackets(MapleClient c) {
				c.getSession().write(MaplePacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), position, (byte) 1));
			}
		}, null);
		MapTimer.getInstance().schedule(new ExpireMapItemJob(mdrop), 180000);
	}

	private final void spawnMobDrop(final IItem idrop, final Point dropPos, final MapleMonster mob, final MapleCharacter chr, final byte droptype, final short questid) {
		final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, droptype, false, questid);
		spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
			@Override
			public void sendPackets(MapleClient c) {
				if (questid <= 0 || c.getPlayer().getQuestStatus(questid) == 1) {
					c.getSession().write(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte) 1));
				}
			}
		}, null);
		MapTimer.getInstance().schedule(new ExpireMapItemJob(mdrop), 180000);
		activateItemReactors(mdrop, chr.getClient());
	}

	public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean playerDrop) {
		final Point droppos = calcDropPos(pos, pos);
		final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 0, playerDrop);
		spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {
			@Override
			public void sendPackets(MapleClient c) {
				c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 1));
			}
		}, null);
		broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 0));
		if (!everlast) {
			MapTimer.getInstance().schedule(new ExpireMapItemJob(drop), 180000);
			activateItemReactors(drop, owner.getClient());
		}
	}

	private final void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
	final IItem item = drop.getItem();

	for (final MapleMapObject o : getAllReactor()) {
		final MapleReactor react = (MapleReactor) o;

		if (react.getReactorType() == 100) {
		if (react.getReactItem().getLeft() == item.getItemId() && react.getReactItem().getRight() == item.getQuantity()) {

			if (react.getArea().contains(drop.getPosition())) {
			if (!react.isTimerActive()) {
				MapTimer.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000);
				react.setTimerActive(true);
				break;
			}
			}
		}
		}
	}
	}

	public final void AriantPQStart() {
	int i = 1;

	mutex.lock();

	try {
		final Iterator<MapleCharacter> ltr = characters.iterator();
		MapleCharacter chars;
		while (ltr.hasNext()) {
		chars = ltr.next();
		broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chars.getName(), 0, false));
		broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars.getName(), 0, false).toString()));
		if (this.getCharactersSize() > i) {
			broadcastMessage(MaplePacketCreator.updateAriantPQRanking(null, 0, true));
			broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars.getName(), 0, true).toString()));
		}
		i++;
		}
	} finally {
		mutex.unlock();
	}
	}

	public final void returnEverLastItem(final MapleCharacter chr) {
	for (final MapleMapObject o : getAllItems()) {
		final MapleMapItem item = ((MapleMapItem) o);
		if (item.getOwner() == chr.getId()) {
		item.setPickedUp(true);
		broadcastMessage(MaplePacketCreator.removeItemFromMap(item.getObjectId(), 2, chr.getId()), item.getPosition());
		if (item.getMeso() > 0) {
			chr.gainMeso(item.getMeso(), false);
		} else {
			MapleInventoryManipulator.addFromDrop(chr.getClient(), item.getItem(), false);
		}
		removeMapObject(item);
		}
	}
	}

	public final void startMapEffect(final String msg, final int itemId) {
	if (mapEffect != null) {
		return;
	}
	mapEffect = new MapleMapEffect(msg, itemId);
	broadcastMessage(mapEffect.makeStartData());
	MapTimer.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		broadcastMessage(mapEffect.makeDestroyData());
		mapEffect = null;
		}
	}, 30000);
	}

	public final void addPlayer(final MapleCharacter chr) {
        final ChannelServer cserv = ChannelServer.getInstance(chr.getClient().getChannel());
        
	mutex.lock();
	try {
		characters.add(chr);
		mapobjects.put(chr.getObjectId(), chr);
	} finally {
		mutex.unlock();
	}
	if (!chr.isHidden()) {
		broadcastMessage(MaplePacketCreator.spawnPlayerMapobject(chr)); //Spawn Player
	}
        
	sendObjectPlacement(chr); //Spawn NPC And Monster
        
        cserv.addPlayer(chr); //ServerMessage
        //chr.getClient().getSession().write(MaplePacketCreator.spawnPlayerMapobject(chr));
        
	if (!onFirstUserEnter.equals("")) {
		if (getCharactersSize() == 1) {
		MapScriptMethods.startScript_FirstUser(chr.getClient(), onFirstUserEnter);
		}
	}
	if (!onUserEnter.equals("")) {
		MapScriptMethods.startScript_User(chr.getClient(), onUserEnter);
	}
	for (final MaplePet pet : chr.getPets()) {
		if (pet.getSummoned()) {
		broadcastMessage(chr, PetPacket.showPet(chr, pet, false, false), false);
		}
	}
	switch (mapid) {
		case 809000101:
		case 809000201:
		chr.getClient().getSession().write(MaplePacketCreator.showEquipEffect());
		break;
	}
	if (getHPDec() > 0) {
		chr.startHurtHp();
	}
        if (chr.getParty() != null) {
		chr.silentPartyUpdate();
		chr.getClient().getSession().write(MaplePacketCreator.updateParty(chr.getClient().getChannel(), chr.getParty(), PartyOperation.SILENT_UPDATE, null));
		chr.updatePartyMemberHP();
		chr.receivePartyMemberHP();
	} 
	final MapleStatEffect stat = chr.getStatForBuff(MapleBuffStat.SUMMON);
	if (stat != null) {
		final MapleSummon summon = chr.getSummons().get(stat.getSourceId());
		summon.setPosition(chr.getPosition());
		chr.addVisibleMapObject(summon);
		this.spawnSummon(summon);
	}
	if (mapEffect != null) {
		mapEffect.sendStartData(chr.getClient());
	}
	if (timeLimit > 0 && getForcedReturnMap() != null) {
		chr.startMapTimeLimitTask(timeLimit, getForcedReturnMap());
	}
	if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
		if (FieldLimitType.Mount.check(fieldLimit)) {
		chr.cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
		}
	}
	if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) {
		chr.getClient().getSession().write(MaplePacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000)));
	}
	if (hasClock()) {
		final Calendar cal = Calendar.getInstance();
		chr.getClient().getSession().write((MaplePacketCreator.getClockTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
	}
	if (chr.getCarnivalParty() != null && chr.getEventInstance() != null) {
		chr.getEventInstance().onMapLoad(chr);
	}
	if (GameConstants.isEvan(chr.getJob()) && chr.getJob() >= 2200 && chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null) {
		if (chr.getDragon() == null) {
		chr.makeDragon();
		}
		spawnDragon(chr.getDragon());
		updateMapObjectVisibility(chr, chr.getDragon());
	}
	}

	public final void removePlayer(final MapleCharacter chr) {
	//log.warn("[dc] [level2] Player {} leaves map {}", new Object[] { chr.getName(), mapid });

	if (everlast) {
		returnEverLastItem(chr);
	}
	mutex.lock();
	try {
		characters.remove(chr);
	} finally {
		mutex.unlock();
	}
	removeMapObject(Integer.valueOf(chr.getObjectId()));
	broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));

	for (final MapleMonster monster : chr.getControlledMonsters()) {
		monster.setController(null);
		monster.setControllerHasAggro(false);
		monster.setControllerKnowsAboutAggro(false);
		updateMonsterController(monster);
	}
	chr.leaveMap();
	chr.cancelMapTimeLimitTask();

	for (final MapleSummon summon : chr.getSummons().values()) {
		if (summon.isPuppet()) {
		chr.cancelBuffStats(MapleBuffStat.PUPPET);
		chr.cancelBuffStats(MapleBuffStat.MIRROR_TARGET);
		} else {
		removeMapObject(summon);
		}
	}
	if (chr.getDragon() != null) {
		removeMapObject(chr.getDragon());
	}
	}

	public final void broadcastMessage(final MaplePacket packet) {
	broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
	}

	public final void broadcastMessage(final MapleCharacter source, final MaplePacket packet, final boolean repeatToSource) {
		broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
	}

	/*	public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource, boolean ranged) {
	broadcastMessage(repeatToSource ? null : source, packet, ranged ? MapleCharacter.MAX_VIEW_RANGE_SQ : Double.POSITIVE_INFINITY, source.getPosition());
	}*/
	public final void broadcastMessage(final MaplePacket packet, final Point rangedFrom) {
	broadcastMessage(null, packet, GameConstants.maxViewRangeSq(), rangedFrom);
	}

	public final void broadcastMessage(final MapleCharacter source, final MaplePacket packet, final Point rangedFrom) {
	broadcastMessage(source, packet, GameConstants.maxViewRangeSq(), rangedFrom);
	}

	private final void broadcastMessage(final MapleCharacter source, final MaplePacket packet, final double rangeSq, final Point rangedFrom) {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> ltr = characters.iterator();
			MapleCharacter chr;
			while (ltr.hasNext()) {
			chr = ltr.next();
			if (chr != source) {
				if (rangeSq < Double.POSITIVE_INFINITY) {
				if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
					chr.getClient().getSession().write(packet);
				}
				} else {
				chr.getClient().getSession().write(packet);
				}
			}
			}
		} finally {
			mutex.unlock();
		}
	}

	private final void sendObjectPlacement(final MapleCharacter c) {
		if (c == null) {
			return;
		}
		for (final MapleMapObject o : getAllMonster()) {
			updateMonsterController((MapleMonster) o);
		}
		for (final MapleMapObject o : getMapObjectsInRange(c.getPosition(), GameConstants.maxViewRangeSq(), GameConstants.rangedMapobjectTypes)) {
			if (o.getType() == MapleMapObjectType.REACTOR) {
				if (!((MapleReactor) o).isAlive()) {
					continue;
				}
			}
			o.sendSpawnData(c.getClient());
			c.addVisibleMapObject(o);
		}
	}

	public final List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq) {
	final List<MapleMapObject> ret = new LinkedList<MapleMapObject>();

	mutex.lock();
	try {
		final Iterator<MapleMapObject> ltr = mapobjects.values().iterator();
		MapleMapObject obj;
		while (ltr.hasNext()) {
		obj = ltr.next();
		if (from.distanceSq(obj.getPosition()) <= rangeSq) {
			ret.add(obj);
		}
		}
	} finally {
		mutex.unlock();
	}
	return ret;
	}

	public final List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq, final List<MapleMapObjectType> MapObject_types) {
	final List<MapleMapObject> ret = new LinkedList<MapleMapObject>();

	mutex.lock();
	try {
		final Iterator<MapleMapObject> ltr = mapobjects.values().iterator();
		MapleMapObject obj;
		while (ltr.hasNext()) {
		obj = ltr.next();
		if (MapObject_types.contains(obj.getType())) {
			if (from.distanceSq(obj.getPosition()) <= rangeSq) {
			ret.add(obj);
			}
		}
		}
	} finally {
		mutex.unlock();
	}
	return ret;
	}

	public final List<MapleMapObject> getMapObjectsInRect(final Rectangle box, final List<MapleMapObjectType> MapObject_types) {
	final List<MapleMapObject> ret = new LinkedList<MapleMapObject>();

	mutex.lock();
	try {
		final Iterator<MapleMapObject> ltr = mapobjects.values().iterator();
		MapleMapObject obj;
		while (ltr.hasNext()) {
		obj = ltr.next();
		if (MapObject_types.contains(obj.getType())) {
			if (box.contains(obj.getPosition())) {
			ret.add(obj);
			}
		}
		}
	} finally {
		mutex.unlock();
	}
	return ret;
	}

	public final List<MapleCharacter> getPlayersInRect(final Rectangle box, final List<MapleCharacter> CharacterList) {
	final List<MapleCharacter> character = new LinkedList<MapleCharacter>();

	mutex.lock();
	try {
		final Iterator<MapleCharacter> ltr = characters.iterator();
		MapleCharacter a;
		while (ltr.hasNext()) {
		a = ltr.next();
		if (CharacterList.contains(a.getClient().getPlayer())) {
			if (box.contains(a.getPosition())) {
			character.add(a);
			}
		}
		}
	} finally {
		mutex.unlock();
	}
	return character;
	}

	public final List<MapleMapObject> getAllItems() {
	return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
	}

	public final List<MapleMapObject> getAllNPC() {
	return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
	}

	public final List<MapleMapObject> getAllReactor() {
	return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
	}

	public final List<MapleMapObject> getAllPlayer() {
	return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
	}

	public final List<MapleMapObject> getAllMonster() {
	return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
	}

	public final List<MapleMapObject> getAllDoor() {
	return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.DOOR));
	}

	public final void addPortal(final MaplePortal myPortal) {
	portals.put(myPortal.getId(), myPortal);
	}

	public final MaplePortal getPortal(final String portalname) {
	for (final MaplePortal port : portals.values()) {
		if (port.getName().equals(portalname)) {
		return port;
		}
	}
	return null;
	}

	public final MaplePortal getPortal(final int portalid) {
	return portals.get(portalid);
	}

	public final void addMapleArea(final Rectangle rec) {
	areas.add(rec);
	}

	public final List<Rectangle> getAreas() {
	return new ArrayList<Rectangle>(areas);
	}

	public final Rectangle getArea(final int index) {
	return areas.get(index);
	}

	public final void setFootholds(final MapleFootholdTree footholds) {
	this.footholds = footholds;
	}

	public final MapleFootholdTree getFootholds() {
	return footholds;
	}

	public final void loadMonsterRate(final boolean first) {
	final int spawnSize = monsterSpawn.size();
	/*	if (spawnSize >= 25 || monsterRate > 1.5) {
	maxRegularSpawn = Math.round(spawnSize / monsterRate);
	} else {
	maxRegularSpawn = Math.round(spawnSize * monsterRate);
	}*/
	maxRegularSpawn = Math.round(spawnSize * monsterRate);
	if (maxRegularSpawn < 2) {
		maxRegularSpawn = 2;
	} else if (maxRegularSpawn > spawnSize) {
		maxRegularSpawn = spawnSize - (spawnSize / 15);
	}
	Collection<Spawns> newSpawn = new LinkedList<Spawns>();
	Collection<Spawns> newBossSpawn = new LinkedList<Spawns>();
	for (final Spawns s : monsterSpawn) {
		if (s.getCarnivalTeam() >= 2) {
		continue; // Remove carnival spawned mobs
		}
		if (s.getMonster().getStats().isBoss()) {
		newBossSpawn.add(s);
		} else {
		newSpawn.add(s);
		}
	}
	monsterSpawn.clear();
	monsterSpawn.addAll(newBossSpawn);
	monsterSpawn.addAll(newSpawn);

	if (first && spawnSize > 0) {
		MapTimer.getInstance().register(new Runnable() {

		@Override
		public void run() {
			respawn(false);
		}
		}, createMobInterval);
	}
	}

	public final void addMonsterSpawn(final MapleMonster monster, final int mobTime, final byte carnivalTeam, final String msg) {
		final Point newpos = calcPointBelow(monster.getPosition());
		newpos.y -= 1;
		monsterSpawn.add(new SpawnPoint(monster, newpos, mobTime, carnivalTeam, msg));
	}

	public final void addAreaMonsterSpawn(final MapleMonster monster, Point pos1, Point pos2, Point pos3, final int mobTime, final String msg) {
	pos1 = calcPointBelow(pos1);
	pos2 = calcPointBelow(pos2);
	pos3 = calcPointBelow(pos3);
	pos1.y -= 1;
	pos2.y -= 1;
	pos3.y -= 1;

	monsterSpawn.add(new SpawnPointAreaBoss(monster, pos1, pos2, pos3, mobTime, msg));
	}

	public final Collection<MapleCharacter> getCharacters() {
	final List<MapleCharacter> chars = new ArrayList<MapleCharacter>();

	mutex.lock();
	try {
		final Iterator<MapleCharacter> ltr = characters.iterator();
		while (ltr.hasNext()) {
		chars.add(ltr.next());
		}
	} finally {
		mutex.unlock();
	}
	return chars;
	}

	public final MapleCharacter getCharacterById_InMap(final int id) {
	mutex.lock();
	try {
		final Iterator<MapleCharacter> ltr = characters.iterator();
		MapleCharacter c;
		while (ltr.hasNext()) {
		c = ltr.next();
		if (c.getId() == id) {
			return c;
		}
		}
	} finally {
		mutex.unlock();
	}
	return null;
	}

	private final void updateMapObjectVisibility(final MapleCharacter chr, final MapleMapObject mo) {
	if (!chr.isMapObjectVisible(mo)) { // monster entered view range
		if (mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq(chr.getPosition()) <= GameConstants.maxViewRangeSq()) {
		chr.addVisibleMapObject(mo);
		mo.sendSpawnData(chr.getClient());
		}
	} else { // monster left view range
		if (mo.getType() != MapleMapObjectType.SUMMON && mo.getPosition().distanceSq(chr.getPosition()) > GameConstants.maxViewRangeSq()) {
		chr.removeVisibleMapObject(mo);
		mo.sendDestroyData(chr.getClient());
		}
	}
	}

	public void moveMonster(MapleMonster monster, Point reportedPos) {
	monster.setPosition(reportedPos);

	mutex.lock();
	try {
		final Iterator<MapleCharacter> ltr = characters.iterator();
		while (ltr.hasNext()) {
		updateMapObjectVisibility(ltr.next(), monster);
		}
	} finally {
		mutex.unlock();
	}
	}

	public void movePlayer(final MapleCharacter player, final Point newPosition) {
	player.setPosition(newPosition);

	final Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
	final MapleMapObject[] visibleObjectsNow = visibleObjects.toArray(new MapleMapObject[visibleObjects.size()]);

	for (MapleMapObject mo : visibleObjectsNow) {
		if (getMapObject(mo.getObjectId()) == mo) {
		updateMapObjectVisibility(player, mo);
		} else {
		player.removeVisibleMapObject(mo);
		}
	}
	for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), GameConstants.maxViewRangeSq())) {
		if (!player.isMapObjectVisible(mo)) {
		mo.sendSpawnData(player.getClient());
		player.addVisibleMapObject(mo);
		}
	}
	}

	public MaplePortal findClosestSpawnpoint(Point from) {
	MaplePortal closest = null;
	double distance, shortestDistance = Double.POSITIVE_INFINITY;
	for (MaplePortal portal : portals.values()) {
		distance = portal.getPosition().distanceSq(from);
		if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
		closest = portal;
		shortestDistance = distance;
		}
	}
	return closest;
	}

	public String spawnDebug() {
	StringBuilder sb = new StringBuilder("Mapobjects in map : ");
	sb.append(this.getMapObjectSize());
	sb.append(" spawnedMonstersOnMap: ");
	sb.append(spawnedMonstersOnMap);
	sb.append(" spawnpoints: ");
	sb.append(monsterSpawn.size());
	sb.append(" maxRegularSpawn: ");
	sb.append(maxRegularSpawn);
	sb.append(" actual monsters: ");
	sb.append(getAllMonster().size());

	return sb.toString();
	}

	public final int getMapObjectSize() {
		return mapobjects.size();
	}

	public final int getCharactersSize() {
		return characters.size();
	}

	public Collection<MaplePortal> getPortals() {
		return Collections.unmodifiableCollection(portals.values());
	}

	public int getSpawnedMonstersOnMap() {
		return spawnedMonstersOnMap.get();
	}

	private class ExpireMapItemJob implements Runnable {
	private MapleMapItem mapitem;
	public ExpireMapItemJob(MapleMapItem mapitem) {
		this.mapitem = mapitem;
	}
	@Override
		public void run() {
			if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
				if (mapitem.isPickedUp()) {
					return;
				}
				mapitem.setPickedUp(true);
				broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0));
				removeMapObject(mapitem);
			}
		}
	}

	private class ActivateItemReactor implements Runnable {
	private MapleMapItem mapitem;
	private MapleReactor reactor;
	private MapleClient c;
	public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
		this.mapitem = mapitem;
		this.reactor = reactor;
		this.c = c;
	}
		@Override
		public void run() {
			if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
				if (mapitem.isPickedUp()) {
					reactor.setTimerActive(false);
					return;
				}
				mapitem.setPickedUp(true);
				broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0));
				removeMapObject(mapitem);
				reactor.hitReactor(c);
				reactor.setTimerActive(false);
				if (reactor.getDelay() > 0) {
					MapTimer.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							reactor.setState((byte) 0);
							broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
						}
					}, reactor.getDelay());
				}
			}
		}
	}

	public void respawn(final boolean force) {
		if (force) {
			final int numShouldSpawn = monsterSpawn.size() - spawnedMonstersOnMap.get();
			if (numShouldSpawn > 0) {
				int spawned = 0;
				for (Spawns spawnPoint : monsterSpawn) {
					spawnPoint.spawnMonster(this);
					spawned++;
					if (spawned >= numShouldSpawn) {
					break;
					}
				}
			}
		} else {
			if (getCharactersSize() <= 0) {
				return;
			}
			final int numShouldSpawn = maxRegularSpawn - spawnedMonstersOnMap.get();
			if (numShouldSpawn > 0) {
				int spawned = 0;
				final List<Spawns> randomSpawn = new ArrayList<Spawns>(monsterSpawn);
				Collections.shuffle(randomSpawn);
				for (Spawns spawnPoint : randomSpawn) {
					if (spawnPoint.shouldSpawn()) {
						spawnPoint.spawnMonster(this);
						spawned++;
					}
					if (spawned >= numShouldSpawn) {
						break;
					}
				}
			}
		}
	}

	private static interface DelayedPacketCreation {
		void sendPackets(MapleClient c);
	}

	private static interface SpawnCondition {
		boolean canSpawn(MapleCharacter chr);
	}
}