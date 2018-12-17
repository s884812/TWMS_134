package server.shops;

import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.ItemFlag;
import client.GameConstants;
import client.MapleCharacter;
import client.MapleClient;
import handling.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.TimerManager;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import tools.packet.PlayerShopPacket;

public class HiredMerchant extends AbstractPlayerStore {

    public ScheduledFuture<?> schedule;
    private MapleMap map;
    private int channel, storeid;
    private long start;

    public HiredMerchant(MapleCharacter owner, int itemId, String desc) {
	super(owner, itemId, desc);
	start = System.currentTimeMillis();
	this.map = owner.getMap();
	this.channel = owner.getClient().getChannel();
	this.schedule = TimerManager.getInstance().schedule(new Runnable() {

	    @Override
	    public void run() {
		closeShop(true, true);
	    }
	}, 1000 * 60 * 60 * 24);
    }

    public byte getShopType() {
	return IMaplePlayerShop.HIRED_MERCHANT;
    }

    public final void setStoreid(final int storeid) {
	this.storeid = storeid;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
	final MaplePlayerShopItem pItem = items.get(item);
	final IItem shopItem = pItem.item;
	final IItem newItem = shopItem.copy();
	final short perbundle = newItem.getQuantity();

	newItem.setQuantity((short) (quantity * perbundle));

	byte flag = newItem.getFlag();

	if (ItemFlag.KARMA_EQ.check(flag)) {
	    newItem.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
	} else if (ItemFlag.KARMA_USE.check(flag)) {
	    newItem.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
	}

	if (MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
	    pItem.bundles -= quantity; // Number remaining in the store

	    final int gainmeso = getMeso() + (pItem.price * quantity);
	    setMeso(gainmeso - GameConstants.EntrustedStoreTax(gainmeso));
	    c.getPlayer().gainMeso(-pItem.price * quantity, false);
	} else {
	    c.getPlayer().dropMessage(1, "Your inventory is full.");
	}
    }

    @Override
    public void closeShop(boolean saveItems, boolean remove) {
	if (schedule != null) {
	    schedule.cancel(false);
	}
	if (saveItems) {
	    saveItems();
	}
	if (remove) {
	    ChannelServer.getInstance(channel).removeMerchant(this);
	    map.broadcastMessage(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
	}
	map.removeMapObject(this);

	map = null;
	schedule = null;
    }

    public int getTimeLeft() {
	return (int) ((System.currentTimeMillis() - start) / 1000);
    }

    public MapleMap getMap() {
	return map;
    }

    public final int getStoreId() {
	return storeid;
    }

    @Override
    public MapleMapObjectType getType() {
	return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
	client.getSession().write(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
	client.getSession().write(PlayerShopPacket.spawnHiredMerchant(this));
    }
}
