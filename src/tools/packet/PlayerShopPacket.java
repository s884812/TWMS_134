package tools.packet;

import client.MapleCharacter;
import client.IItem;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import server.MerchItemPackage;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import server.shops.MaplePlayerShop;
import server.shops.MaplePlayerShopItem;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PlayerShopPacket {

    private static final void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, final IMaplePlayerShop shop) {
	mplew.write(4);
	mplew.writeInt(((MaplePlayerShop) shop).getObjectId());
	mplew.writeMapleAsciiString(shop.getDescription());
	mplew.write(0);
	mplew.write(shop.getItemId() % 10);
	mplew.write(1);
	mplew.write(shop.getFreeSlot() > -1 ? 4 : 1);
	mplew.write(0);
    }

    public static final MaplePacket addCharBox(final MapleCharacter c, final int type) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
	mplew.writeInt(c.getId());
	addAnnounceBox(mplew, c.getPlayerShop());

	return mplew.getPacket();
    }

    public static final MaplePacket removeCharBox(final MapleCharacter c) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
	mplew.writeInt(c.getId());
	mplew.write(0);

	return mplew.getPacket();
    }

    public static final MaplePacket sendTitleBox() {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.SEND_TITLE_BOX.getValue());
	mplew.write(7);

	return mplew.getPacket();
    }

    public static final MaplePacket sendPlayerShopBox(final MapleCharacter c) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
	mplew.writeInt(c.getId());
	addAnnounceBox(mplew, c.getPlayerShop());

	return mplew.getPacket();
    }

    public static final MaplePacket getHiredMerch(final MapleCharacter chr, final HiredMerchant merch, final boolean firstTime) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(5);
		mplew.write(5);
		mplew.write(4);
		mplew.writeShort(merch.getVisitorSlot(chr));
		mplew.writeInt(merch.getItemId());
		mplew.writeMapleAsciiString("Hired Merchant");
		for (final Pair<Byte, MapleCharacter> storechr : merch.getVisitors()) {
			mplew.write(storechr.left);
			PacketHelper.addCharLook(mplew, storechr.right, false);
			mplew.writeMapleAsciiString(storechr.right.getName());
		mplew.writeShort(storechr.right.getJob());
		}
		mplew.write(-1);
		mplew.writeShort(0);
		mplew.writeMapleAsciiString(merch.getOwnerName());
		if (merch.isOwner(chr)) {
			mplew.writeInt(merch.getTimeLeft());
			mplew.write(firstTime ? 1 : 0);
			mplew.writeInt(0);
			mplew.write(0);
		}
		mplew.writeMapleAsciiString(merch.getDescription());
		mplew.write(10);
		mplew.writeInt(merch.getMeso()); // meso
		mplew.write(merch.getItems().size());
		for (final MaplePlayerShopItem item : merch.getItems()) {
			mplew.writeShort(item.bundles);
			mplew.writeShort(item.item.getQuantity());
			mplew.writeInt(item.price);
			PacketHelper.addItemInfo(mplew, item.item, true, true);
		}
		return mplew.getPacket();
    }

    public static final MaplePacket getPlayerStore(final MapleCharacter chr, final boolean firstTime) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
	IMaplePlayerShop ips = chr.getPlayerShop();

	switch (ips.getShopType()) {
	    case 2:
		mplew.write(5);
		mplew.write(4);
		mplew.write(4);
		break;
	    case 3:
		mplew.write(5);
		mplew.write(2);
		mplew.write(2);
		break;
	    case 4:
		mplew.write(5);
		mplew.write(1);
		mplew.write(2);
		break;
	}
mplew.writeShort(ips.getVisitorSlot(chr));

	PacketHelper.addCharLook(mplew, ((MaplePlayerShop) ips).getMCOwner(), false);
	mplew.writeMapleAsciiString(ips.getOwnerName());

	for (final Pair<Byte, MapleCharacter> storechr : ips.getVisitors()) {
	    mplew.write(storechr.left);
	    PacketHelper.addCharLook(mplew, storechr.right, false);
	    mplew.writeMapleAsciiString(storechr.right.getName());
	}
	mplew.write(0xFF);
	mplew.writeMapleAsciiString(ips.getDescription());
	mplew.write(10);
	mplew.write(ips.getItems().size());

	for (final MaplePlayerShopItem item : ips.getItems()) {
	    mplew.writeShort(item.bundles);
	    mplew.writeShort(item.item.getQuantity());
	    mplew.writeInt(item.price);
	    PacketHelper.addItemInfo(mplew, item.item, true, true);
	}
	return mplew.getPacket();
    }

    public static final MaplePacket shopChat(final String message, final int slot) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
	mplew.write(6);
	mplew.write(8);
	mplew.write(slot);
	mplew.writeMapleAsciiString(message);

	return mplew.getPacket();
    }

    public static final MaplePacket shopErrorMessage(final int error, final int type) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
	mplew.write(0x0A);
	mplew.write(type);
	mplew.write(error);

	return mplew.getPacket();
    }

    public static final MaplePacket spawnHiredMerchant(final HiredMerchant hm) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.SPAWN_HIRED_MERCHANT.getValue());
	mplew.writeInt(hm.getOwnerId());
	mplew.writeInt(hm.getItemId());
	mplew.writePos(hm.getPosition());
	mplew.writeShort(0);
	mplew.writeMapleAsciiString(hm.getOwnerName());
	mplew.write(5);
	mplew.writeInt(hm.getObjectId());
	mplew.writeMapleAsciiString(hm.getDescription());
	mplew.write(hm.getItemId() % 10);
	mplew.write(1);
	mplew.write(4);

	return mplew.getPacket();
    }

    public static final MaplePacket destroyHiredMerchant(final int id) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.DESTROY_HIRED_MERCHANT.getValue());
	mplew.writeInt(id);

	return mplew.getPacket();
    }

    public static final MaplePacket shopItemUpdate(final IMaplePlayerShop shop) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
	mplew.write(0x17);
	if (shop.getShopType() == 1) {
	    mplew.writeInt(0);
	}
	mplew.write(shop.getItems().size());

	for (final MaplePlayerShopItem item : shop.getItems()) {
	    mplew.writeShort(item.bundles);
	    mplew.writeShort(item.item.getQuantity());
	    mplew.writeInt(item.price);
	    PacketHelper.addItemInfo(mplew, item.item, true, true);
	}
	return mplew.getPacket();
    }

    public static final MaplePacket shopVisitorAdd(final MapleCharacter chr, final int slot) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
	mplew.write(4);
	mplew.write(slot);
	PacketHelper.addCharLook(mplew, chr, false);
	mplew.writeMapleAsciiString(chr.getName());
	mplew.writeShort(chr.getJob());

	return mplew.getPacket();
    }

    public static final MaplePacket shopVisitorLeave(final byte slot) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
	mplew.write(0x0A);
	mplew.write(slot);

	return mplew.getPacket();
    }

    public static final MaplePacket Merchant_Buy_Error(final byte message) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	// 2 = You have not enough meso
	mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
	mplew.write(0x16);
	mplew.write(message);

	return mplew.getPacket();
    }

    public static final MaplePacket updateHiredMerchant(final HiredMerchant shop) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.UPDATE_HIRED_MERCHANT.getValue());
	mplew.writeInt(shop.getOwnerId());
	mplew.write(0x05);
	mplew.writeInt(shop.getObjectId());
	mplew.writeMapleAsciiString(shop.getDescription());
	mplew.write(shop.getItemId() % 10);
	mplew.write(shop.getFreeSlot() > -1 ? 3 : 2);
	mplew.write(0x04);

	return mplew.getPacket();
    }

    public static final MaplePacket merchItem_Message(final byte op) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.MERCH_ITEM_MSG.getValue());
	mplew.write(op);

	return mplew.getPacket();
    }

    public static final MaplePacket merchItemStore(final byte op) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	// [28 01] [22 01] - Invalid Asiasoft Passport
	// [28 01] [22 00] - Open Asiasoft pin typing
	mplew.writeShort(SendPacketOpcode.MERCH_ITEM_STORE.getValue());
	mplew.write(op);

	switch (op) {
	    case 0x24:
		mplew.writeZeroBytes(8);
		break;
	    default:
		mplew.write(0);
		break;
	}

	return mplew.getPacket();
    }

    public static final MaplePacket merchItemStore_ItemData(final MerchItemPackage pack) {
	final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.MERCH_ITEM_STORE.getValue());
	mplew.write(0x23);
	mplew.writeInt(9030000); // Fredrick
	mplew.writeInt(32272); // pack.getPackageid()
	mplew.writeZeroBytes(5);
	mplew.writeInt(pack.getMesos());
	mplew.write(0);
	mplew.write(pack.getItems().size());

	for (final IItem item : pack.getItems()) {
	    PacketHelper.addItemInfo(mplew, item, true, true);
	}
	mplew.writeZeroBytes(3);

	return mplew.getPacket();
    }
}
