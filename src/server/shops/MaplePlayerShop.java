package server.shops;

import java.util.ArrayList;
import java.util.List;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import server.MapleInventoryManipulator;
import server.maps.MapleMapObjectType;
import tools.packet.PlayerShopPacket;

public class MaplePlayerShop extends AbstractPlayerStore {

    private boolean open;
    private MapleCharacter owner;
    private int boughtnumber = 0;
    private List<String> bannedList = new ArrayList<String>();

    public MaplePlayerShop(MapleCharacter owner, int itemId, String desc) {
	super(owner, itemId, desc);
	this.owner = owner;
	open = false;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
	MaplePlayerShopItem pItem = items.get(item);
	if (pItem.bundles > 0) {
/*	    synchronized (items) {
		IItem newItem = pItem.item.copy();
		newItem.setQuantity(quantity);
		if (c.getPlayer().getMeso() >= pItem.price * quantity) {
		    if (MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
			pItem.totalquantity -= pItem.bundles - quantity;
			c.getPlayer().gainMeso(-pItem.price * quantity, false);

			if (pItem == 0) {
			    boughtnumber++;
			    if (boughtnumber == items.size()) {
				removeAllVisitors(10, 1);
				owner.getClient().getSession().write(PlayerShopPacket.shopErrorMessage(10, 1));
				closeShop(false, true);
			    }
			}
		    } else {
			c.getPlayer().dropMessage(1, "Your inventory is full.");
		    }
		} else {
		    c.getPlayer().dropMessage(1, "You do not have enough mesos.");
		}
	    }*/
	    owner.getClient().getSession().write(PlayerShopPacket.shopItemUpdate(this));
	}
    }

    @Override
    public byte getShopType() {
	return IMaplePlayerShop.PLAYER_SHOP;
    }

    @Override
    public void closeShop(boolean saveItems, boolean remove) {
	owner.getMap().broadcastMessage(PlayerShopPacket.removeCharBox(owner));
	owner.getMap().removeMapObject(this);

	if (saveItems) {
	    saveItems();
	}
	owner.setPlayerShop(null);
    }

    public void banPlayer(String name) {
	if (!bannedList.contains(name)) {
	    bannedList.add(name);
	}
	for (int i = 0; i < 3; i++) {
	    MapleCharacter chr = getVisitor(i);
	    if (chr.getName().equals(name)) {
		chr.getClient().getSession().write(PlayerShopPacket.shopErrorMessage(5, 1));
		chr.setPlayerShop(null);
		removeVisitor(chr);
	    }
	}
    }

    @Override
    public void setOpen(boolean open) {
	this.open = open;
    }

    @Override
    public boolean isOpen() {
	return open;
    }

    public boolean isBanned(String name) {
	if (bannedList.contains(name)) {
	    return true;
	}
	return false;
    }

    public MapleCharacter getMCOwner() {
	return owner;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
    }

    @Override
    public void sendSpawnData(MapleClient client) {
    }

    @Override
    public MapleMapObjectType getType() {
	return MapleMapObjectType.SHOP;
    }
}
