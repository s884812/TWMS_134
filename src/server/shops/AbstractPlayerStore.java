package server.shops;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.ref.WeakReference;

import client.IItem;
import client.Equip;
import client.MapleCharacter;
import database.DatabaseConnection;
import handling.MaplePacket;
import server.maps.AbstractMapleMapObject;
import tools.Pair;
import tools.packet.PlayerShopPacket;

public abstract class AbstractPlayerStore extends AbstractMapleMapObject implements IMaplePlayerShop {

    private boolean open;
    private String ownerName, des;
    private int ownerId, owneraccount, itemId;
    private AtomicInteger meso = new AtomicInteger(0);
    protected WeakReference<MapleCharacter> chr1 = new WeakReference<MapleCharacter>(null);
    protected WeakReference<MapleCharacter> chr2 = new WeakReference<MapleCharacter>(null);
    protected WeakReference<MapleCharacter> chr3 = new WeakReference<MapleCharacter>(null);
    protected List<MaplePlayerShopItem> items = new LinkedList<MaplePlayerShopItem>();

    public AbstractPlayerStore(MapleCharacter owner, int itemId, String desc) {
	this.setPosition(owner.getPosition());
	this.ownerName = owner.getName();
	this.ownerId = owner.getId();
	this.owneraccount = owner.getAccountID();
	this.itemId = itemId;
	this.des = desc;
	this.open = false;
    }

    @Override
    public void broadcastToVisitors(MaplePacket packet) {
	broadcastToVisitors(packet, true);
    }

    public void broadcastToVisitors(MaplePacket packet, boolean owner) {
	MapleCharacter chr = chr1.get();
	if (chr != null) {
	    chr.getClient().getSession().write(packet);
	}
	chr = chr2.get();
	if (chr != null) {
	    chr.getClient().getSession().write(packet);
	}
	chr = chr3.get();
	if (chr != null) {
	    chr.getClient().getSession().write(packet);
	}
	if (getShopType() == 2 && owner) {
	    ((MaplePlayerShop) this).getMCOwner().getClient().getSession().write(packet);
	}
    }

    @Override
    public int getMeso() {
	return meso.get();
    }

    @Override
    public void setMeso(int meso) {
	this.meso.set(meso);
    }

    public void removeVisitors() {
	MapleCharacter chr = chr1.get();
	if (chr != null) {
	    removeVisitor(chr);
	}
	chr = chr2.get();
	if (chr != null) {
	    removeVisitor(chr);
	}
	chr = chr3.get();
	if (chr != null) {
	    removeVisitor(chr);
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

    public boolean saveItems() {
	Connection con = DatabaseConnection.getConnection();
	try {
	    PreparedStatement ps = con.prepareStatement("INSERT INTO hiredmerch (characterid, accountid, Mesos, time) VALUES (?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
	    ps.setInt(1, ownerId);
	    ps.setInt(2, owneraccount);
	    ps.setInt(3, meso.get());
	    ps.setLong(4, System.currentTimeMillis());

	    ps.executeUpdate();

	    ResultSet rs = ps.getGeneratedKeys();
	    rs.next();
	    final int packageid = rs.getInt(1);
	    rs.close();
	    ps.close();

	    PreparedStatement ps2;

	    for (MaplePlayerShopItem pItems : items) {
		if (pItems.bundles <= 0) {
		    continue;
		}
		final IItem item = pItems.item;

		if (item.getType() == 1) { // equips
		    ps2 = con.prepareStatement("INSERT INTO hiredmerchitems (PackageId, itemid, quantity, upgradeslots, level, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, owner, GM_Log, flag, expiredate, ViciousHammer, itemLevel, itemEXP) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		    final Equip eq = (Equip) item;
		    ps2.setInt(1, packageid);
		    ps2.setInt(2, eq.getItemId());
		    ps2.setInt(3, 1); // Quantity
		    ps2.setInt(4, eq.getUpgradeSlots());
		    ps2.setInt(5, eq.getLevel());
		    ps2.setInt(6, eq.getStr());
		    ps2.setInt(7, eq.getDex());
		    ps2.setInt(8, eq.getInt());
		    ps2.setInt(9, eq.getLuk());
		    ps2.setInt(10, eq.getHp());
		    ps2.setInt(11, eq.getMp());
		    ps2.setInt(12, eq.getWatk());
		    ps2.setInt(13, eq.getMatk());
		    ps2.setInt(14, eq.getWdef());
		    ps2.setInt(15, eq.getMdef());
		    ps2.setInt(16, eq.getAcc());
		    ps2.setInt(17, eq.getAvoid());
		    ps2.setInt(18, eq.getHands());
		    ps2.setInt(19, eq.getSpeed());
		    ps2.setInt(20, eq.getJump());
		    ps2.setString(21, eq.getOwner());
		    ps2.setString(22, eq.getGMLog());
		    ps2.setInt(23, eq.getFlag());
		    ps2.setLong(24, eq.getExpiration());
		    ps2.setInt(25, eq.getViciousHammer());
		    ps2.setInt(26, eq.getItemLevel());
		    ps2.setInt(27, eq.getItemEXP());
		} else {
		    ps2 = con.prepareStatement("INSERT INTO hiredmerchitems (PackageId, itemid, quantity, owner, GM_Log, flag, expiredate) VALUES (?, ?, ?, ?, ?, ?, ?)");
		    ps2.setInt(1, packageid);
		    ps2.setInt(2, item.getItemId());
		    ps2.setInt(3, pItems.bundles * item.getQuantity());
		    ps2.setString(4, item.getOwner());
		    ps2.setString(5, item.getGMLog());
		    ps2.setInt(6, item.getFlag());
		    ps2.setLong(7, item.getExpiration());
		}
		ps2.execute();
		ps2.close();
	    }
	    return true;
	} catch (SQLException se) {
	    se.printStackTrace();
	}
	return false;
    }

    public MapleCharacter getVisitor(int num) {
	switch (num) {
	    case 1:
		return chr1.get();
	    case 2:
		return chr2.get();
	    case 3:
		return chr3.get();
	}
	return null;
    }

    @Override
    public void addVisitor(MapleCharacter visitor) {
	int i = getFreeSlot();
	if (i > -1) {
	    broadcastToVisitors(PlayerShopPacket.shopVisitorAdd(visitor, i));

	    switch (i) {
		case 1:
		    chr1 = new WeakReference(visitor);
		    break;
		case 2:
		    chr2 = new WeakReference(visitor);
		    break;
		case 3:
		    chr3 = new WeakReference(visitor);
		    break;
	    }
	    if (i == 3) {
		if (getShopType() == 1) {
		    ((HiredMerchant) this).getMap().broadcastMessage(PlayerShopPacket.updateHiredMerchant((HiredMerchant) this));
		} else {
		    ((MaplePlayerShop) this).getMCOwner().getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(((MaplePlayerShop) this).getMCOwner()));
		}
	    }
	}
    }

    @Override
    public void removeVisitor(MapleCharacter visitor) {
	final byte slot = getVisitorSlot(visitor);
	boolean shouldUpdate = getFreeSlot() == -1;
	if (slot != -1) {
	    broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(slot));

	    switch (slot) {
		case 1:
		    chr1 = new WeakReference(null);
		    break;
		case 2:
		    chr2 = new WeakReference(null);
		    break;
		case 3:
		    chr3 = new WeakReference(null);
		    break;
	    }
	    if (shouldUpdate) {
		if (getShopType() == 1) {
		    ((HiredMerchant) this).getMap().broadcastMessage(PlayerShopPacket.updateHiredMerchant((HiredMerchant) this));
		} else {
		    ((MaplePlayerShop) this).getMCOwner().getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(((MaplePlayerShop) this).getMCOwner()));
		}
	    }
	}
    }

    @Override
    public byte getVisitorSlot(MapleCharacter visitor) {
	MapleCharacter chr = chr1.get();
	if (chr == visitor) {
	    return 1;
	}
	chr = chr2.get();
	if (chr == visitor) {
	    return 2;
	}
	chr = chr3.get();
	if (chr == visitor) {
	    return 3;
	}
        if(visitor.getId() == ownerId){
            return 0;
        }
	return -1;
    }

    @Override
    public void removeAllVisitors(int error, int type) {
	for (int i = 1; i <= 3; i++) {
	    MapleCharacter visitor = getVisitor(i);
	    if (visitor != null) {
		if (type != -1) {
		    visitor.getClient().getSession().write(PlayerShopPacket.shopErrorMessage(error, type));
		}
		visitor.setPlayerShop(null);

		switch (i) {
		    case 1:
			chr1 = new WeakReference(null);
			break;
		    case 2:
			chr2 = new WeakReference(null);
			break;
		    case 3:
			chr3 = new WeakReference(null);
			break;
		}
	    }
	}
    }

    @Override
    public String getOwnerName() {
	return ownerName;
    }

    @Override
    public int getOwnerId() {
	return ownerId;
    }

    @Override
    public int getOwnerAccId() {
	return owneraccount;
    }

    @Override
    public String getDescription() {
	if (des == null) {
	    return "";
	}
	return des;
    }

    @Override
    public List<Pair<Byte, MapleCharacter>> getVisitors() {
	List<Pair<Byte, MapleCharacter>> chrs = new LinkedList<Pair<Byte, MapleCharacter>>();
	MapleCharacter chr = chr1.get();
	if (chr != null) {
	    chrs.add(new Pair<Byte, MapleCharacter>((byte) 1, chr));
	}
	chr = chr2.get();
	if (chr != null) {
	    chrs.add(new Pair<Byte, MapleCharacter>((byte) 2, chr));
	}
	chr = chr3.get();
	if (chr != null) {
	    chrs.add(new Pair<Byte, MapleCharacter>((byte) 3, chr));
	}
	return chrs;
    }

    @Override
    public List<MaplePlayerShopItem> getItems() {
	return items;
    }

    @Override
    public void addItem(MaplePlayerShopItem item) {
	items.add(item);
    }

    @Override
    public boolean removeItem(int item) {
	return false;
    }

    @Override
    public void removeFromSlot(int slot) {
	items.remove(slot);
    }

    @Override
    public byte getFreeSlot() {
	MapleCharacter chr = chr1.get();
	if (chr == null) {
	    return 1;
	}
	chr = chr2.get();
	if (chr == null) {
	    return 2;
	}
	chr = chr3.get();
	if (chr == null) {
	    return 3;
	}
	return -1;
    }

    @Override
    public int getItemId() {
	return itemId;
    }

    @Override
    public boolean isOwner(MapleCharacter chr) {
	return chr.getId() == ownerId && chr.getName().equals(ownerName);
    }
}
