package server.shops;

import client.IItem;

public class MaplePlayerShopItem {
    public IItem item;
    public short bundles;
    public int price;

    public MaplePlayerShopItem(IItem item, short bundles, int price) {
	this.item = item;
	this.bundles = bundles;
	this.price = price;
    }
}
