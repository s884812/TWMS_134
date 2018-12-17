package server.maps;

import client.MapleClient;
import tools.MaplePacketCreator;
import handling.MaplePacket;

public class MapleMapEffect {

    private String msg;
    private int itemId;
    private boolean active = true;

    public MapleMapEffect(String msg, int itemId) {
	this.msg = msg;
	this.itemId = itemId;
    }

    public void setActive(boolean active) {
	this.active = active;
    }

    public MaplePacket makeDestroyData() {
	return MaplePacketCreator.removeMapEffect();
    }

    public MaplePacket makeStartData() {
	return MaplePacketCreator.startMapEffect(msg, itemId, active);
    }

    public void sendStartData(MapleClient c) {
	c.getSession().write(MaplePacketCreator.startMapEffect(msg, itemId, active));
    }
}
