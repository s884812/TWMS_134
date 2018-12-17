package scripting;

import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import javax.script.Invocable;
import javax.script.ScriptException;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import server.TimerManager;
import server.MapleSquad;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapFactory;
import tools.MaplePacketCreator;

public class EventManager {

    private Invocable iv;
    private ChannelServer cserv;
    private WeakHashMap<String, EventInstanceManager> instances = new WeakHashMap<String, EventInstanceManager>();
    private Properties props = new Properties();
    private String name;
	private int world;

    public EventManager(ChannelServer cserv, Invocable iv, String name, int world) {
	this.iv = iv;
	this.cserv = cserv;
	this.name = name;
	this.world = world;
    }

    public void cancel() {
	try {
	    iv.invokeFunction("cancelSchedule", (Object) null);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public void schedule(final String methodName, long delay) {
	TimerManager.getInstance().schedule(new Runnable() {

	    public void run() {
		try {
		    iv.invokeFunction(methodName, (Object) null);
		} catch (ScriptException ex) {
		    ex.printStackTrace();
		    System.out.println("method Name : " + methodName + "");
		} catch (NoSuchMethodException ex) {
		    ex.printStackTrace();
		    System.out.println("method Name : " + methodName + "");
		}
	    }
	}, delay);
    }

    public void schedule(final String methodName, long delay, final EventInstanceManager eim) {
	TimerManager.getInstance().schedule(new Runnable() {

	    public void run() {
		try {
		    iv.invokeFunction(methodName, eim);
		} catch (ScriptException ex) {
		    ex.printStackTrace();
		    System.out.println("method Name : " + methodName + "");
		} catch (NoSuchMethodException ex) {
		    ex.printStackTrace();
		    System.out.println("method Name : " + methodName + "");
		}
	    }
	}, delay);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
	return TimerManager.getInstance().scheduleAtTimestamp(new Runnable() {

	    public void run() {
		try {
		    iv.invokeFunction(methodName, (Object) null);
		} catch (ScriptException ex) {
		    ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
		    ex.printStackTrace();
		}
	    }
	}, timestamp);
    }

    public ChannelServer getChannelServer() {
	return cserv;
    }

    public EventInstanceManager getInstance(String name) {
	return instances.get(name);
    }

    public Collection<EventInstanceManager> getInstances() {
	return Collections.unmodifiableCollection(instances.values());
    }

    public EventInstanceManager newInstance(String name) {
	EventInstanceManager ret = new EventInstanceManager(this, name, cserv.getMapFactory(world), world);
	instances.put(name, ret);
	return ret;
    }

    public void disposeInstance(String name) {
	instances.remove(name);
    }

    public Invocable getIv() {
	return iv;
    }

    public void setProperty(String key, String value) {
	props.setProperty(key, value);
    }

    public String getProperty(String key) {
	return props.getProperty(key);
    }

    public String getName() {
	return name;
    }

    public void startInstance() {
	try {
	    iv.invokeFunction("setup", (Object) null);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public void startInstance(MapleCharacter character) {
	try {
	    EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
	    eim.registerPlayer(character);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    //PQ method: starts a PQ
    public void startInstance(MapleParty party, MapleMap map) {
	try {
	    EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
	    eim.registerParty(party, map);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    //non-PQ method for starting instance
    public void startInstance(EventInstanceManager eim, String leader) {
	try {
	    iv.invokeFunction("setup", eim);
	    eim.setProperty("leader", leader);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public void startInstance(MapleSquad squad, MapleMap map) {
	try {
	    EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", squad.getLeader().getId()));
	    eim.registerSquad(squad, map);
	} catch (ScriptException ex) {
	    ex.printStackTrace();
	} catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	}
    }

    public void warpAllPlayer(int from, int to) {
	final MapleMap tomap = cserv.getMapFactory(world).getMap(to);
	for (MapleMapObject mmo : cserv.getMapFactory(world).getMap(from).getAllPlayer()) {
	    ((MapleCharacter) mmo).changeMap(tomap, tomap.getPortal(0));
	}
    }

    public MapleMapFactory getMapFactory(int world) {
	return cserv.getMapFactory(world);
    }

    public OverrideMonsterStats newMonsterStats() {
	return new OverrideMonsterStats();
    }

    public MapleMonster getMonster(final int id) {
	return MapleLifeFactory.getMonster(id);
    }

    public void broadcastShip(final int mapid, final int effect) {
	cserv.getMapFactory(world).getMap(mapid).broadcastMessage(MaplePacketCreator.boatPacket(effect));
    }

    public void broadcastServerMsg(final int type, final String msg, final boolean weather) {
	if (!weather) {
	    cserv.broadcastPacket(MaplePacketCreator.serverNotice(type, msg));
	} else {
	    for (Entry<Integer, MapleMap> map : cserv.getMapFactory(world).getMaps().entrySet()) {
		final MapleMap load = map.getValue();
		if (load.getCharactersSize() > 0) {
		    load.startMapEffect(msg, type);
		}
	    }
	}
    }
}
