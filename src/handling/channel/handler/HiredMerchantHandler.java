package handling.channel.handler;

import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.Equip;
import client.IItem;
import client.Item;
import client.MapleInventoryType;
import client.MapleClient;
import client.MapleCharacter;
import client.GameConstants;
import database.DatabaseConnection;
import server.MapleInventoryManipulator;
import server.MerchItemPackage;
import tools.packet.PlayerShopPacket;
import tools.data.input.SeekableLittleEndianAccessor;

public class HiredMerchantHandler {

	public static final void UseHiredMerchant(final SeekableLittleEndianAccessor slea, final MapleClient c) {
//	slea.readInt(); // TimeStamp

	if (c.getPlayer().getMap().allowPersonalShop()) {
		final byte state = checkExistance(c.getAccID());

		switch (state) {
		case 1:
			c.getPlayer().dropMessage(1, "Please claim your items from Fredrick first.");
			break;
		case 0:
			boolean merch = true;
			try {
			merch = c.getChannelServer().getWorldInterface().hasMerchant(c.getAccID());
			} catch (RemoteException re) {
			c.getChannelServer().reconnectWorld();
			}
			if (!merch) {
//		    c.getPlayer().dropMessage(1, "The Hired Merchant is temporary disabled until it's fixed.");
			c.getSession().write(PlayerShopPacket.sendTitleBox());
			} else {
			c.getPlayer().dropMessage(1, "Please close the existing store and try again.");
			}
			break;
		default:
			c.getPlayer().dropMessage(1, "An unknown error occured.");
			break;
		}
	} else {
		c.getSession().close();
	}
	}

	private static final byte checkExistance(final int accid) {
	Connection con = DatabaseConnection.getConnection();
	try {
		PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where accountid = ?");
		ps.setInt(1, accid);

		if (ps.executeQuery().next()) {
		ps.close();
		return 1;
		}
		ps.close();
		return 0;
	} catch (SQLException se) {
		return -1;
	}
	}

	public static final void MerchantItemStore(final SeekableLittleEndianAccessor slea, final MapleClient c) {
	final byte operation = slea.readByte();

	switch (operation) {
		case 20: {
		final String AS13Digit = slea.readMapleAsciiString();

		final int conv = c.getPlayer().getConversation();

		if (conv == 3) { // Hired Merch
			final MerchItemPackage pack = loadItemFrom_Database(c.getPlayer().getId());

			if (pack == null) {
			c.getPlayer().dropMessage(1, "You do not have any item(s) with Fredrick.");
			c.getPlayer().setConversation(0);
			} else {
			c.getSession().write(PlayerShopPacket.merchItemStore_ItemData(pack));
			}
		}
		break;
		}
		case 25: { // Request take out iteme
		if (c.getPlayer().getConversation() != 3) {
			return;
		}
		c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x24));
		break;
		}
		case 26: { // Take out item
		if (c.getPlayer().getConversation() != 3) {
			return;
		}
		final MerchItemPackage pack = loadItemFrom_Database(c.getPlayer().getId());

		if (!check(c.getPlayer(), pack)) {
			c.getSession().write(PlayerShopPacket.merchItem_Message((byte) 0x21));
			return;
		}
		if (deletePackage(c.getPlayer().getId())) {
			c.getPlayer().gainMeso(pack.getMesos(), false);
			for (IItem item : pack.getItems()) {
			MapleInventoryManipulator.addFromDrop(c, item, false);
			}
			c.getSession().write(PlayerShopPacket.merchItem_Message((byte) 0x1d));
		} else {
			c.getPlayer().dropMessage(1, "An unknown error occured.");
		}
		break;
		}
		case 27: { // Exit
		c.getPlayer().setConversation(0);
		break;
		}
	}
	}

	private static final boolean check(final MapleCharacter chr, final MerchItemPackage pack) {
	if (chr.getMeso() + pack.getMesos() < 0) {
		return false;
	}
	byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
	for (IItem item : pack.getItems()) {
		final MapleInventoryType invtype = GameConstants.getInventoryType(item.getItemId());
		if (invtype == MapleInventoryType.EQUIP) {
		eq++;
		} else if (invtype == MapleInventoryType.USE) {
		use++;
		} else if (invtype == MapleInventoryType.SETUP) {
		setup++;
		} else if (invtype == MapleInventoryType.ETC) {
		etc++;
		} else if (invtype == MapleInventoryType.CASH) {
		cash++;
		}
	}
	if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() <= eq
		|| chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() <= use
		|| chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() <= setup
		|| chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() <= etc
		|| chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() <= cash) {
		return false;
	}
	return true;
	}

	private static final boolean deletePackage(final int charid) {
	final Connection con = DatabaseConnection.getConnection();

	try {
		PreparedStatement ps = con.prepareStatement("DELETE from hiredmerch where characterid = ?");
		ps.setInt(1, charid);
		ps.execute();
		ps.close();
		return true;
	} catch (SQLException e) {
		return false;
	}
	}

	private static final MerchItemPackage loadItemFrom_Database(final int charid) {
	final Connection con = DatabaseConnection.getConnection();

	try {
		PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where characterid = ?");
		ps.setInt(1, charid);

		ResultSet rs = ps.executeQuery();

		if (!rs.next()) {
		ps.close();
		rs.close();
		return null;
		}
		final int packageid = rs.getInt("PackageId");

		final MerchItemPackage pack = new MerchItemPackage();
		pack.setPackageid(packageid);
		pack.setMesos(rs.getInt("Mesos"));
		pack.setSentTime(rs.getLong("time"));

		ps.close();
		rs.close();

		List<IItem> items = new ArrayList<IItem>();

		PreparedStatement ps2 = con.prepareStatement("SELECT * from hiredmerchitems where PackageId = ?");
		ps2.setInt(1, packageid);
		ResultSet rs2 = ps2.executeQuery();

		while (rs2.next()) {
		final int itemid = rs2.getInt("itemid");
		final MapleInventoryType type = GameConstants.getInventoryType(itemid);

		if (type == MapleInventoryType.EQUIP) {
			final Equip equip = new Equip(rs2.getInt("itemid"), (byte) 0, -1, rs2.getByte("flag"));
			equip.setOwner(rs2.getString("owner"));
			equip.setQuantity(rs2.getShort("quantity"));
			equip.setAcc(rs2.getShort("acc"));
			equip.setAvoid(rs2.getShort("avoid"));
			equip.setDex(rs2.getShort("dex"));
			equip.setHands(rs2.getShort("hands"));
			equip.setHp(rs2.getShort("hp"));
			equip.setInt(rs2.getShort("int"));
			equip.setJump(rs2.getShort("jump"));
			equip.setLuk(rs2.getShort("luk"));
			equip.setMatk(rs2.getShort("matk"));
			equip.setMdef(rs2.getShort("mdef"));
			equip.setMp(rs2.getShort("mp"));
			equip.setSpeed(rs2.getShort("speed"));
			equip.setStr(rs2.getShort("str"));
			equip.setWatk(rs2.getShort("watk"));
			equip.setWdef(rs2.getShort("wdef"));
			equip.setItemLevel(rs2.getByte("itemLevel"));
			equip.setItemEXP(rs2.getShort("itemEXP"));
			equip.setViciousHammer(rs2.getByte("ViciousHammer"));
			equip.setUpgradeSlots(rs2.getByte("upgradeslots"));
			equip.setLevel(rs2.getByte("level"));
			equip.setFlag(rs2.getByte("flag"));
			equip.setExpiration(rs2.getLong("expiredate"));
			equip.setGMLog(rs2.getString("GM_Log"));

			items.add(equip);
		} else {
			final IItem item = new Item(rs2.getInt("itemid"), (byte) 0, rs2.getShort("quantity"), rs2.getByte("flag"));
			item.setOwner(rs2.getString("owner"));
			item.setFlag(rs2.getByte("flag"));
			item.setExpiration(rs2.getLong("expiredate"));
			item.setGMLog(rs2.getString("GM_Log"));

			items.add(item);
		}
		}
		ps.close();
		rs.close();

		pack.setItems(items);

		return pack;
	} catch (SQLException e) {
		e.printStackTrace();
		return null;
	}
	}
}