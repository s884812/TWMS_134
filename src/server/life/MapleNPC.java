package server.life;

import client.MapleClient;
import server.MapleShopFactory;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

public class MapleNPC extends AbstractLoadedMapleLife {

    private final MapleNPCStats stats;
    private boolean custom = false;

    public MapleNPC(final int id, final MapleNPCStats stats) {
	super(id);
	this.stats = stats;
    }

    public final boolean hasShop() {
	return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public final void sendShop(final MapleClient c) {
	MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {

	if (getId() >= 9901000 && getId() <= 9901551) {
	    if (!stats.getName().equals("")) {
		client.getSession().write(MaplePacketCreator.spawnPlayerNPC(stats, getId()));
		client.getSession().write(MaplePacketCreator.spawnNPCRequestController(this, false));
	    }
	} else {
	    client.getSession().write(MaplePacketCreator.spawnNPC(this, true));
	    client.getSession().write(MaplePacketCreator.spawnNPCRequestController(this, true));
	}
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
	client.getSession().write(MaplePacketCreator.removeNPC(getObjectId()));
    }

    @Override
    public final MapleMapObjectType getType() {
	return MapleMapObjectType.NPC;
    }

    public final String getName() {
	return stats.getName();
    }

    public final boolean isCustom() {
	return custom;
    }

    public final void setCustom(final boolean custom) {
	this.custom = custom;
    }
}
