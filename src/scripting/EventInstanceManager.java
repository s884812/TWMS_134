package scripting;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.script.ScriptException;

import client.MapleCharacter;
import client.MapleQuestStatus;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import server.MapleCarnivalParty;
import server.TimerManager;
import server.MapleSquad;
import server.quest.MapleQuest;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.MaplePacketCreator;

public class EventInstanceManager {

    private List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
    private List<MapleMonster> mobs = new LinkedList<MapleMonster>();
    private Map<MapleCharacter, Integer> killCount = new HashMap<MapleCharacter, Integer>();
    private EventManager em;
    private MapleMapFactory mapFactory;
    private String name;
    private Properties props = new Properties();
    private long timeStarted = 0;
    private long eventTime = 0;
    private List<Integer> mapIds = new LinkedList<Integer>();
    private ScheduledFuture<?> eventTimer;
    private final Lock mutex = new ReentrantLock();
	private int world;

    public EventInstanceManager(EventManager em, String name, MapleMapFactory factory, int world) {
	this.em = em;
	this.name = name;
	mapFactory = factory;
	this.world = world;
    }

    public void registerPlayer(MapleCharacter chr) {
	try {
	    mutex.lock();
	    try {
		chars.add(chr);
	    } finally {
		mutex.unlock();
	    }
	    chr.setEventInstance(this);
	    em.getIv().invokeFunction("playerEntry", this, chr);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public void changedMap(final MapleCharacter chr, final int mapid) {
	try {
	    em.getIv().invokeFunction("changedMap", this, chr, mapid);
	} catch (NullPointerException npe) {
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public void timeOut(final long delay, final EventInstanceManager eim) {
	eventTimer = TimerManager.getInstance().schedule(new Runnable() {

	    public void run() {
		try {
		    em.getIv().invokeFunction("scheduledTimeout", eim);
		} catch (NullPointerException npe) {
		} catch (ScriptException ex) {
		    ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
		    ex.printStackTrace();
		}
	    }
	}, delay);
    }

    public void stopEventTimer() {
	eventTime = 0;
	timeStarted = 0;
	if (eventTimer != null) {
	    eventTimer.cancel(false);
	}
    }

    public void restartEventTimer(long time) {
	timeStarted = System.currentTimeMillis();
	eventTime = time;
	if (eventTimer != null) {
	    eventTimer.cancel(false);
	}
	eventTimer = null;
	final int timesend = (int) time / 1000;

	mutex.lock();
	try {
	    for (final MapleCharacter chr : chars) {
		chr.getClient().getSession().write(MaplePacketCreator.getClock(timesend));
	    }
	} finally {
	    mutex.unlock();
	}
	timeOut(time, this);
    }

    public void startEventTimer(long time) {
	timeStarted = System.currentTimeMillis();
	eventTime = time;
	final int timesend = (int) time / 1000;

	mutex.lock();
	try {
	    for (final MapleCharacter chr : chars) {
		chr.getClient().getSession().write(MaplePacketCreator.getClock(timesend));
	    }
	} finally {
	    mutex.unlock();
	}
	timeOut(time, this);
    }

    public boolean isTimerStarted() {
	return eventTime > 0 && timeStarted > 0;
    }

    public long getTimeLeft() {
	return eventTime - (System.currentTimeMillis() - timeStarted);
    }

    public void registerParty(MapleParty party, MapleMap map) {
	for (MaplePartyCharacter pc : party.getMembers()) {
	    MapleCharacter c = map.getCharacterById_InMap(pc.getId());
	    registerPlayer(c);
	}
    }

    public void unregisterPlayer(final MapleCharacter chr) {
	mutex.lock();
	try {
	    chars.remove(chr);
	} finally {
	    mutex.unlock();
	}
	chr.setEventInstance(null);
    }

    public final boolean disposeIfPlayerBelow(final byte size, final int towarp) {
	MapleMap map = null;
	if (towarp != 0) {
	    map = this.getMapFactory().getMap(towarp);
	}
	mutex.lock();
	try {
	    if (chars.size() <= size) {

		MapleCharacter chr;
		for (int i = 0; i < chars.size(); i++) {
		    chr = chars.get(i);
		    unregisterPlayer(chr);

		    if (towarp != 0) {
			chr.changeMap(map, map.getPortal(0));
		    }
		}
		dispose();
		return true;
	    }
	} finally {
	    mutex.unlock();
	}
	return false;
    }

    public final void saveBossQuest(final int points) {
	mutex.lock();
	try {
	    for (MapleCharacter chr : chars) {
		final MapleQuestStatus record = chr.getQuestNAdd(MapleQuest.getInstance(150001));

		if (record.getCustomData() != null) {
		    record.setCustomData(String.valueOf(points + Integer.parseInt(record.getCustomData())));
		} else {
		    record.setCustomData(String.valueOf(points)); // First time
		}
	    }
	} finally {
	    mutex.unlock();
	}
    }

    public List<MapleCharacter> getPlayers() {
	return Collections.unmodifiableList(chars);
    }

    public final int getPlayerCount() {
	return chars.size();
    }

    public void registerMonster(MapleMonster mob) {
	mobs.add(mob);
	mob.setEventInstance(this);
    }

    public void unregisterMonster(MapleMonster mob) {
	mobs.remove(mob);
	mob.setEventInstance(null);
	if (mobs.size() == 0) {
	    try {
		em.getIv().invokeFunction("allMonstersDead", this);
	    } catch (ScriptException ex) {
		ex.printStackTrace();
	    } catch (NoSuchMethodException ex) {
		ex.printStackTrace();
	    }
	}
    }

    public void playerKilled(MapleCharacter chr) {
	try {
	    em.getIv().invokeFunction("playerDead", this, chr);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public boolean revivePlayer(MapleCharacter chr) {
	try {
	    Object b = em.getIv().invokeFunction("playerRevive", this, chr);
	    if (b instanceof Boolean) {
		return (Boolean) b;
	    }
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
	return true;
    }

    public void playerDisconnected(final MapleCharacter chr) {
	try {
	    byte ret = ((Double) em.getIv().invokeFunction("playerDisconnected", this, chr)).byteValue();

	    if (ret == 0) {
		unregisterPlayer(chr);
		if (getPlayerCount() <= 0) {
		    dispose();
		}
	    } else {
		mutex.lock();
		try {
		    if (ret > 0) {
			unregisterPlayer(chr);
			if (getPlayerCount() < ret) {
			    for (MapleCharacter player : chars) {
				removePlayer(player);
			    }
			    dispose();
			}
		    } else {
			unregisterPlayer(chr);
			ret *= -1;

			if (isLeader(chr)) {
			    for (MapleCharacter player : chars) {
				removePlayer(player);
			    }
			    dispose();
			} else {
			    if (getPlayerCount() < ret) {
				for (MapleCharacter player : chars) {
				    removePlayer(player);
				}
				dispose();
			    }
			}
		    }
		} finally {
		    mutex.unlock();
		}
	    }
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    /**
     *
     * @param chr
     * @param mob
     */
    public void monsterKilled(final MapleCharacter chr, final MapleMonster mob) {
	try {
	    Integer kc = killCount.get(chr);
	    int inc = ((Double) em.getIv().invokeFunction("monsterValue", this, mob.getId())).intValue();
	    if (kc == null) {
		kc = inc;
	    } else {
		kc += inc;
	    }
	    killCount.put(chr, kc);
	    if (chr.getCarnivalParty() != null) {
		em.getIv().invokeFunction("monsterKilled", this, chr, mob.getStats().getCP());
	    }
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public int getKillCount(MapleCharacter chr) {
	Integer kc = killCount.get(chr);
	if (kc == null) {
	    return 0;
	} else {
	    return kc;
	}
    }

    public void dispose() {
	mutex.lock();
	try {
	    chars.clear();
	    chars = null;
	} finally {
	    mutex.unlock();
	}
	mobs.clear();
	mobs = null;
	killCount.clear();
	killCount = null;
	timeStarted = 0;
	eventTime = 0;
	props.clear();
	props = null;
	for (final Integer i : mapIds) {
	    mapFactory.removeInstanceMap(i);
	}
	mapIds = null;
	mapFactory = null;
	em.disposeInstance(name);
	em = null;
    }

    public final void broadcastPlayerMsg(final int type, final String msg) {
	mutex.lock();
	try {
	    for (final MapleCharacter chr : chars) {
		chr.getClient().getSession().write(MaplePacketCreator.serverNotice(type, msg));
	    }
	} finally {
	    mutex.unlock();
	}
    }

    public final MapleMap createInstanceMap(final int mapid) {
	int assignedid = em.getChannelServer().getEventSM(world).getNewInstanceMapId();
	mapIds.add(assignedid);
	return mapFactory.CreateInstanceMap(mapid, true, true, true, assignedid);
    }

    public final MapleMap createInstanceMapS(final int mapid) {
	final int assignedid = em.getChannelServer().getEventSM(world).getNewInstanceMapId();
	mapIds.add(assignedid);
	return mapFactory.CreateInstanceMap(mapid, false, false, false, assignedid);
    }

    public final MapleMapFactory getMapFactory() {
	return mapFactory;
    }

    public final MapleMap getMapInstance(int args) {
	final MapleMap map = mapFactory.getInstanceMap(mapIds.get(args));

	// in case reactors need shuffling and we are actually loading the map
	if (!mapFactory.isInstanceMapLoaded(mapIds.get(args))) {
	    if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true")) {
		map.shuffleReactors();
	    }
	}
	return map;
    }

    public final void schedule(final String methodName, final long delay) {
	TimerManager.getInstance().schedule(new Runnable() {

	    public void run() {
		try {
		    em.getIv().invokeFunction(methodName, EventInstanceManager.this);
		} catch (NullPointerException npe) {
		} catch (ScriptException ex) {
		    ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
		    ex.printStackTrace();
		}
	    }
	}, delay);
    }

    public final String getName() {
	return name;
    }

    public final void setProperty(final String key, final String value) {
	props.setProperty(key, value);
    }

    public final Object setProperty(final String key, final String value, final boolean prev) {
	return props.setProperty(key, value);
    }

    public final String getProperty(final String key) {
	return props.getProperty(key);
    }

    public final void leftParty(final MapleCharacter chr) {
	try {
	    em.getIv().invokeFunction("leftParty", this, chr);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public final void disbandParty() {
	try {
	    em.getIv().invokeFunction("disbandParty", this);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    //Separate function to warp players to a "finish" map, if applicable
    public final void finishPQ() {
	try {
	    em.getIv().invokeFunction("clearPQ", this);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public final void removePlayer(final MapleCharacter chr) {
	try {
	    em.getIv().invokeFunction("playerExit", this, chr);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public final void registerCarnivalParty(final MapleCharacter leader, final MapleMap map, final byte team) {
	leader.clearCarnivalRequests();
	List<MapleCharacter> characters = new LinkedList<MapleCharacter>();
	final MapleParty party = leader.getParty();

	if (party == null) {
	    return;
	}
	for (MaplePartyCharacter pc : party.getMembers()) {
	    final MapleCharacter c = map.getCharacterById_InMap(pc.getId());
	    characters.add(c);
	    registerPlayer(c);
	    c.resetCP();
	}
	final MapleCarnivalParty carnivalParty = new MapleCarnivalParty(leader, characters, team);
	try {
	    em.getIv().invokeFunction("registerCarnivalParty", this, carnivalParty);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public void onMapLoad(final MapleCharacter chr) {
	try {
	    em.getIv().invokeFunction("onMapLoad", this, chr);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    // Ignore, we don't want to update this for all events.
	}
    }

    public boolean isLeader(final MapleCharacter chr) {
	return (chr.getParty().getLeader().getId() == chr.getId());
    }

    public void registerSquad(MapleSquad squad, MapleMap map) {
	final int mapid = map.getId();

	for (MapleCharacter player : squad.getMembers()) {
	    if (player != null && player.getMapId() == mapid) {
		registerPlayer(player);
	    }
	}
    }
}
