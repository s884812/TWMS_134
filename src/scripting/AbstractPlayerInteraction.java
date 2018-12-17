package scripting;

import java.rmi.RemoteException;
import java.awt.Point;
import java.util.List;

import client.Equip;
import client.IItem;
import client.SkillFactory;
import client.GameConstants;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.MapleInventoryType;
import client.MaplePet;
import client.MapleQuestStatus;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.guild.MapleGuild;
import server.Randomizer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import server.maps.MapleMapObject;
import server.maps.SavedLocationType;
import server.maps.Event_DojoAgent;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.packet.PetPacket;
import tools.packet.UIPacket;

public class AbstractPlayerInteraction {

	private MapleClient c;
        private MapleStat stat;

	public AbstractPlayerInteraction(final MapleClient c) {
		this.c = c;
	}

	public final MapleClient getClient() {
		return c;
	}

	public final MapleCharacter getPlayer() {
		return c.getPlayer();
	}
        
        public final MapleStat getMapleStat() {
                return stat;
        }
        
	public final EventManager getEventManager(final String event) {
		return c.getChannelServer().getEventSM(c.getPlayer().getWorld()).getEventManager(event);
	}

	public final EventInstanceManager getEventInstance() {
		return c.getPlayer().getEventInstance();
	}

	public final void warp(final int map) {
		final MapleMap mapz = getWarpMap(map);
		c.getPlayer().changeMap(mapz, mapz.getPortal(Randomizer.nextInt(mapz.getPortals().size())));
	}

	public final void warp(final int map, final int portal) {
		final MapleMap mapz = getWarpMap(map);
		c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
	}

	public final void warp(final int map, final String portal) {
		final MapleMap mapz = getWarpMap(map);
		c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
	}

	public final void warpMap(final int mapid, final int portal) {
		final MapleMap map = getMap(mapid);
		for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
			chr.changeMap(map, map.getPortal(portal));
		}
	}

	public final void playPortalSE() {
		c.getSession().write(MaplePacketCreator.showOwnBuffEffect(0, 7));
	}

	private final MapleMap getWarpMap(final int map) {
		return ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(map);
	}

	public final MapleMap getMap() {
		return c.getPlayer().getMap();
	}

	public final MapleMap getMap(final int map) {
		return getWarpMap(map);
	}

	public final void spawnMob(final int id, final int x, final int y) {
		spawnMob(id, 1, new Point(x, y));
	}

	private final void spawnMob(final int id, final int qty, final Point pos) {
		for (int i = 0; i < qty; i++) {
			c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
		}
	}

	public final void killMob(int ids) {
		c.getPlayer().getMap().killMonster(ids);
	}

	public final void killAllMob() {
		c.getPlayer().getMap().killAllMonsters(true);
	}

	public final void addHP(final int delta) {
		c.getPlayer().addHP(delta);
	}

	public final int getPlayerStat(final String type) {
		if (type.equals("LVL")) {
			return c.getPlayer().getLevel();
		} else if (type.equals("STR")) {
			return c.getPlayer().getStat().getStr();
		} else if (type.equals("DEX")) {
			return c.getPlayer().getStat().getDex();
		} else if (type.equals("INT")) {
			return c.getPlayer().getStat().getInt();
		} else if (type.equals("LUK")) {
			return c.getPlayer().getStat().getLuk();
		} else if (type.equals("HP")) {
			return c.getPlayer().getStat().getHp();
		} else if (type.equals("MP")) {
			return c.getPlayer().getStat().getMp();
		} else if (type.equals("MAXHP")) {
			return c.getPlayer().getStat().getMaxHp();
		} else if (type.equals("MAXMP")) {
			return c.getPlayer().getStat().getMaxMp();
		} else if (type.equals("RAP")) {
			return c.getPlayer().getRemainingAp();
		} else if (type.equals("RSP")) {
			return c.getPlayer().getRemainingSp();
		} else if (type.equals("GID")) {
			return c.getPlayer().getGuildId();
		} else if (type.equals("AID")) {
			return c.getPlayer().getGuild().getAllianceId();
		} else if (type.equals("GRANK")) {
			return c.getPlayer().getGuildRank();
		} else if (type.equals("GM")) {
			return c.getPlayer().isGM() ? 1 : 0;
		} else if (type.equals("GENDER")) {
			return c.getPlayer().getGender();
		} else if (type.equals("FACE")) {
			return c.getPlayer().getFace();
		} else if (type.equals("HAIR")) {
			return c.getPlayer().getHair();
		}
		return -1;
	}

	public final String getName() {
		return c.getPlayer().getName();
	}

	public final boolean haveItem(final int itemid) {
		return haveItem(itemid, 1);
	}

	public final boolean haveItem(final int itemid, final int quantity) {
		return haveItem(itemid, quantity, false, true);
	}

	public final boolean haveItem(final int itemid, final int quantity, final boolean checkEquipped, final boolean greaterOrEquals) {
		return c.getPlayer().haveItem(itemid, quantity, checkEquipped, greaterOrEquals);
	}

	public final boolean canHold(final int itemid) {
		return c.getPlayer().getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
	}

	public final MapleQuestStatus getQuestRecord(final int id) {
		return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(id));
	}

	public final byte getQuestStatus(final int id) {
		return c.getPlayer().getQuestStatus(id);
	}

	public final void forceStartQuest(final int id, final String data) {
		MapleQuest.getInstance(id).forceStart(c.getPlayer(), 0, data);
	}

	public final void forceCompleteQuest(final int id) {
		MapleQuest.getInstance(id).forceComplete(getPlayer(), 0);
                c.getSession().write(MaplePacketCreator.showSpecialEffect(12)); // Quest completion
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showSpecialEffect(c.getPlayer().getId(), 12), false);
	}

	public void spawnNpc(final int npcId) {
		c.getPlayer().getMap().spawnNpc(npcId, c.getPlayer().getPosition());
	}

	public final void spawnNpc(final int npcId, final int x, final int y) {
		c.getPlayer().getMap().spawnNpc(npcId, new Point(x, y));
	}

	public final void spawnNpc(final int npcId, final Point pos) {
		c.getPlayer().getMap().spawnNpc(npcId, pos);
	}

	public final void removeNpc(final int mapid, final int npcId) {
		c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(mapid).removeNpc(npcId);
	}

	public final void forceStartReactor(final int mapid, final int id) {
		MapleMap map = c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(mapid);
		MapleReactor react;
		for (final MapleMapObject remo : map.getAllReactor()) {
			react = (MapleReactor) remo;
			if (react.getReactorId() == id) {
			react.forceStartReactor(c);
			break;
			}
		}
	}

	public final void destroyReactor(final int mapid, final int id) {
		MapleMap map = c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(mapid);
		MapleReactor react;
		for (final MapleMapObject remo : map.getAllReactor()) {
			react = (MapleReactor) remo;
			if (react.getReactorId() == id) {
			react.hitReactor(c);
			break;
			}
		}
	}

	public final void hitReactor(final int mapid, final int id) {
		MapleMap map = c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(mapid);
		MapleReactor react;
		for (final MapleMapObject remo : map.getAllReactor()) {
			react = (MapleReactor) remo;
			if (react.getReactorId() == id) {
			react.hitReactor(c);
			break;
			}
		}
	}

	public final int getJob() {
		return c.getPlayer().getJob();
	}

	public int getJobId() {
		return  getPlayer().getJob();
	}

	public final void gainNX(final int amount) {
		c.getPlayer().modifyCSPoints(1, amount, true);
	}
        
        public final void getShowItemGain(final int id, final short quantity) {
		c.getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
	}

	public final void gainItem(final int id, final short quantity) {
		gainItem(id, quantity, false, 0);
	}

	public final void gainItem(final int id, final short quantity, final boolean randomStats) {
		gainItem(id, quantity, randomStats, 0);
	}

	public final void gainItem(final int id, final short quantity, final long period) {
		gainItem(id, quantity, false, period);
	}

	public final void gainItem(final int id, final short quantity, final boolean randomStats, final long period) {
            if (quantity >= 0) {
			final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			final MapleInventoryType type = GameConstants.getInventoryType(id);
			if (!MapleInventoryManipulator.checkSpace(c, id, quantity, "")) {
				return;
			}
			if (type.equals(MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
				final IItem item = randomStats ? ii.randomizeStats((Equip) ii.getEquipById(id)) : ii.getEquipById(id);
				if (period > 0) {
					item.setExpiration(System.currentTimeMillis() + period);
				}
				MapleInventoryManipulator.addbyItem(c, item);
			} else {
				MapleInventoryManipulator.addById(c, id, quantity, "", null, period);
			}
		} else {
			MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(id), id, -quantity, true, false);
		}
		
	}

	public final void changeMusic(final String songName) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
	}

	public final void playerMessage(final String message) {
		playerMessage(5, message); // default playerMessage and mapMessage to use type 5
	}

	public final void mapMessage(final String message) {
		mapMessage(5, message);
	}

	public final void guildMessage(final String message) {
		guildMessage(5, message);
	}

	public final void playerMessage(final int type, final String message) {
		c.getSession().write(MaplePacketCreator.serverNotice(type, message));
	}

	public final void mapMessage(final int type, final String message) {
		c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
	}

	public final void guildMessage(final int type, final String message) {
		if (getGuild() != null) {
			getGuild().guildMessage(MaplePacketCreator.serverNotice(type, message));
		}
	}

	public final MapleGuild getGuild() {
		try {
			return c.getChannelServer().getWorldInterface().getGuild(getPlayer().getGuildId(), null);
		} catch (final RemoteException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public final MapleParty getParty() {
		return c.getPlayer().getParty();
	}

	public final int getCurrentPartyId(int mapid) {
		return getMap(mapid).getCurrentPartyId();
	}

	public final boolean isLeader() {
		return getParty().getLeader().equals(new MaplePartyCharacter(c.getPlayer()));
	}

	public final boolean isAllPartyMembersAllowedJob(final int job) {
		boolean allow = true;
		for (final MapleCharacter mem : c.getChannelServer().getPartyMembers(c.getPlayer().getParty())) {
			if (mem.getJob() / 100 != job) {
			allow = false;
			break;
			}
		}
		return allow;
	}

	public final boolean allMembersHere() {
		boolean allHere = true;
		for (final MapleCharacter partymem : c.getChannelServer().getPartyMembers(c.getPlayer().getParty())) { // TODO, store info in MaplePartyCharacter instead
			if (partymem.getMapId() != c.getPlayer().getMapId()) {
			allHere = false;
			break;
			}
		}
		return allHere;
	}

	public final void warpParty(final int mapId) {
		final int cMap = c.getPlayer().getMapId();
		final MapleMap target = getMap(mapId);
		for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
			final MapleCharacter curChar = getClient().getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
			if (curChar != null && curChar.getMapId() == cMap) {
				curChar.changeMap(target, target.getPortal(0));
			}
		}
	}

	public final void givePartyItems(final int id, final short quantity, final List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			if (quantity >= 0) {
				MapleInventoryManipulator.addById(chr.getClient(), id, quantity);
			} else {
				MapleInventoryManipulator.removeById(chr.getClient(), GameConstants.getInventoryType(id), id, -quantity, true, false);
			}
			chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
		}
	}

	public final void givePartyExp(final int amount, final List<MapleCharacter> party) {
		for (final MapleCharacter chr : party) {
			chr.gainExp(amount * c.getChannelServer().getExpRate(), true, true, true);
		}
	}

	public final void removeFromParty(final int id, final List<MapleCharacter> party) {
		for (final MapleCharacter chr : party) {
			final int possesed = chr.getInventory(GameConstants.getInventoryType(id)).countById(id);
			if (possesed > 0) {
				MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(id), id, possesed, true, false);
				chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, (short) -possesed, true));
			}
		}
	}

	public final void useItem(final int id) {
		MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(c.getPlayer());
		c.getSession().write(UIPacket.getStatusMsg(id));
	}

	public final void cancelItem(final int id) {
		c.getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(id), false, -1);
	}

	public final int getMorphState() {
		return c.getPlayer().getMorphState();
	}

	public final void removeAll(final int id) {
		final int possessed = c.getPlayer().getInventory(GameConstants.getInventoryType(id)).countById(id);
		if (possessed > 0) {
			MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(id), id, possessed, true, false);
			c.getSession().write(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
		}
	}

	public final void gainCloseness(final int closeness, final int index) {
		if (getPlayer().getPet(index) != null) {
			getPlayer().getPet(index).setCloseness(getPlayer().getPet(index).getCloseness() + closeness);
			getClient().getSession().write(PetPacket.updatePet(getPlayer().getPet(index), true));
		}
	}

	public final void gainClosenessAll(final int closeness) {
		for (final MaplePet pet : getPlayer().getPets()) {
			if (pet != null) {
				pet.setCloseness(pet.getCloseness() + closeness);
				getClient().getSession().write(PetPacket.updatePet(pet, true));
			}
		}
	}

	public final void resetMap(final int mapid) {
		final MapleMap map = getMap(mapid);
		map.resetReactors();
		map.killAllMonsters(false);
		for (final MapleMapObject i : map.getAllItems()) {
			map.removeMapObject(i);
		}
	}

	public final void openNpc(final int id) {
		NPCScriptManager.getInstance().start(getClient(), id);
	}

	public final int getMapId() {
		return c.getPlayer().getMap().getId();
	}

	public final boolean haveMonster(final int mobid) {
		for (MapleMapObject obj : c.getPlayer().getMap().getAllMonster()) {
			final MapleMonster mob = (MapleMonster) obj;
			if (mob.getId() == mobid) {
			return true;
			}
		}
		return false;
	}

	public final int getChannelNumber() {
		return c.getChannel();
	}

	public final int getMonsterCount(final int mapid) {
		return c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(mapid).getAllMonster().size();
	}

	public final void teachSkill(final int id, final byte level, final byte masterlevel) {
		getPlayer().changeSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
	}

	public final int getPlayerCount(final int mapid) {
		return c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(mapid).getCharactersSize();
	}

	public final void dojo_getUp() {
		c.getSession().write(MaplePacketCreator.Mulung_DojoUp());
		c.getSession().write(MaplePacketCreator.Mulung_DojoUp2());
		c.getSession().write(MaplePacketCreator.instantMapWarp((byte) 6));
	}

	public final boolean dojoAgent_NextMap(final boolean dojo, final boolean fromresting) {
		if (dojo) {
			return Event_DojoAgent.warpNextMap(c.getPlayer(), fromresting);
		}
		return Event_DojoAgent.warpNextMap_Agent(c.getPlayer(), fromresting);
	}

	public final int dojo_getPts() {
		return c.getPlayer().getDojo();
	}

	public final int getSavedLocation(final String loc) {
		final Integer ret = c.getPlayer().getSavedLocation(SavedLocationType.fromString(loc));
		if (ret == null || ret == -1) {
			return 102000000;
		}
		return ret;
	}

	public final void saveLocation(final String loc) {
		c.getPlayer().saveLocation(SavedLocationType.fromString(loc));
	}

	public final void clearSavedLocation(final String loc) {
		c.getPlayer().clearSavedLocation(SavedLocationType.fromString(loc));
	}

	public final void summonMsg(final String msg) {
		c.getSession().write(UIPacket.summonMessage(msg));
	}

	public final void summonMsg(final int type) {
		c.getSession().write(UIPacket.summonMessage(type));
	}

	public final void showInstruction(final String msg, final int width, final int height) {
		c.getSession().write(MaplePacketCreator.sendHint(msg, width, height));
	}

	public final void playerSummonHint(final boolean summon) {
		c.getSession().write(UIPacket.summonHelper(summon));
	}

	public final void playerSummonMessage(final int type) {
		c.getSession().write(UIPacket.summonMessage(type));
	}

	public final void playerSummonMessage(final String message) {
		c.getSession().write(UIPacket.summonMessage(message));
	}

	public final String getInfoQuest(final int id) {
		return c.getPlayer().getInfoQuest(id);
	}

	public final void updateInfoQuest(final int id, final String data) {
		c.getPlayer().updateInfoQuest(id, data);
	}

	public final void Aran_Start() {
		c.getSession().write(UIPacket.Aran_Start());
	}

	public final void AranTutInstructionalBubble(final String data) {
		c.getSession().write(UIPacket.AranTutInstructionalBalloon(data));
	}

	public final void EvanTutInstructionalBubble(final String data) {
		c.getSession().write(UIPacket.EvanTutInstructionalBalloon(data));
	}

	public final void EvanDragonEyes() {
		c.getSession().write(UIPacket.EvanDragonEyes());
	}

	public final void ShowWZEffect(final String data) {
		c.getSession().write(UIPacket.ShowWZEffect(data));
	}

	public final void EarnTitleMsg(final String data) {
		c.getSession().write(UIPacket.EarnTitleMsg(data));
	}

	public final void MovieClipIntroUI(final boolean enabled) {
		c.getSession().write(UIPacket.IntroDisableUI(enabled));
		c.getSession().write(UIPacket.IntroLock(enabled));
	}
}