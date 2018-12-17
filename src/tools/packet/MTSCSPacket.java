package tools.packet;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.List;

import client.IItem;
import client.MapleClient;
import client.MapleCharacter;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import tools.HexTool;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MTSCSPacket {

	public static MaplePacket warpCS(MapleClient c) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPEN.getValue());
		final MapleCharacter chr = c.getPlayer();
		mplew.writeLong(-1);
		mplew.writeShort(0);
                mplew.writeZeroBytes(5); //134
		PacketHelper.addCharStats(mplew, chr);
		mplew.write(chr.getBuddylist().getCapacity());
		if (chr.getBlessOfFairyOrigin() != null) {
			mplew.write(1);
			mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
		} else {
			mplew.write(0);
		}
                mplew.writeShort(0); //134
                mplew.write(HexTool.getByteArrayFromHexString("00 64 B2 15 D5 61 C8 01")); //134
		PacketHelper.addInventoryInfo(mplew, chr);
		PacketHelper.addSkillInfo(mplew, chr);
		PacketHelper.addCoolDownInfo(mplew, chr);
		PacketHelper.addQuestInfo(mplew, chr);
		PacketHelper.addRingInfo(mplew, chr);
		PacketHelper.addRocksInfo(mplew, chr);
		PacketHelper.addInventoryInfo(mplew, chr);
                PacketHelper.addSkillInfo(mplew, chr);
                PacketHelper.addCoolDownInfo(mplew, chr); // 00 00
                PacketHelper.addQuestInfo(mplew, chr); // 00 00 00 00
                PacketHelper.addRingInfo(mplew, chr); // 00 00 00 00 00 00 00 00
                PacketHelper.addRocksInfo(mplew, chr);
                chr.QuestInfoPacket(mplew); // 00 00  00 00 00 00
                mplew.writeInt(0);
        
		return mplew.getPacket();
	}

	public static MaplePacket useCharm(byte charmsleft, byte daysleft) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(6);
		mplew.write(1);
		mplew.write(charmsleft);
		mplew.write(daysleft);

		return mplew.getPacket();
	}

	public static MaplePacket itemExpired(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(2);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static MaplePacket ViciousHammer(boolean start, int hammered) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
		if (start) {
			mplew.write(49);
			mplew.writeInt(0);
			mplew.writeInt(hammered);
		} else {
			mplew.write(53);
			mplew.writeInt(0);
		}

		return mplew.getPacket();
	}

	public static MaplePacket changePetName(MapleCharacter chr, String newname, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());

		mplew.writeInt(chr.getId());
		mplew.write(0);
		mplew.writeMapleAsciiString(newname);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static MaplePacket showNotes(ResultSet notes, int count) throws SQLException {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
		mplew.write(3);
		mplew.write(count);
		for (int i = 0; i < count; i++) {
			mplew.writeInt(notes.getInt("id"));
			mplew.writeMapleAsciiString(notes.getString("from"));
			mplew.writeMapleAsciiString(notes.getString("message"));
			mplew.writeLong(PacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
			mplew.write(0);
			notes.next();
		}

		return mplew.getPacket();
	}

	public static MaplePacket useChalkboard(final int charid, final String msg) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CHALKBOARD.getValue());

		mplew.writeInt(charid);
		if (msg == null) {
			mplew.write(0);
		} else {
			mplew.write(1);
			mplew.writeMapleAsciiString(msg);
		}

		return mplew.getPacket();
	}

	public static MaplePacket getTrockRefresh(MapleCharacter chr, boolean vip, boolean delete) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
		mplew.write(delete ? 2 : 3);
		mplew.write(vip ? 1 : 0);
		int[] map = chr.getRocks();
		for (int i = 0; i < 10; i++) {
			mplew.writeInt(map[i]);
		}
		return mplew.getPacket();
	}

	public static MaplePacket sendWishList(MapleCharacter chr, boolean update) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(update ? 0x58 : 0x52);
		int[] list = chr.getWishlist();
		for (int i = 0; i < 10; i++) {
			mplew.writeInt(list[i] != -1 ? list[i] : 0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket showNXMapleTokens(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
		mplew.writeInt(chr.getCSPoints(1)); // A-cash
		mplew.writeInt(chr.getCSPoints(2)); // MPoint

		return mplew.getPacket();
	}

	public static MaplePacket showBoughtCSItem(int itemid , int accountId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x5A);
		mplew.writeLong(137235);//HexTool.getByteArrayFromHexString("13 18 02 00 00 00 00 00")); // uniq id
		mplew.writeLong(accountId);
		mplew.writeInt(itemid);
		mplew.write(HexTool.getByteArrayFromHexString("03 D1 CC 01")); // probably SN
		mplew.writeShort(1); // quantity
		mplew.writeZeroBytes(14);
		PacketHelper.addExpirationTime(mplew, itemid);
		mplew.writeLong(0);

		return mplew.getPacket();
	}

	public static MaplePacket showBoughtCSQuestItem(short position, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x92); // MapleSEA v1.01
		mplew.write(HexTool.getByteArrayFromHexString("01 00 00 00 01 00")); // probably ID and something else
		mplew.writeShort(position);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static MaplePacket transferFromCSToInv(IItem item, int position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x6D);
		mplew.write(position);//in csinventory
		PacketHelper.addItemInfo(mplew, item, true, false, true);

		return mplew.getPacket();
	}

	public static MaplePacket transferFromInvToCS(IItem item, int accountId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x4E);
		//addCashItemInformation(mplew, item, accountId);

		return mplew.getPacket();
	}

	public static MaplePacket enableUse0() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.write(0x0B);
		mplew.write(1);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static MaplePacket enableUse1() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.writeShort(0x4F);
		mplew.write(0);
		mplew.writeShort(0x04);
		mplew.writeInt(0x06);
		mplew.writeShort(1);

		return mplew.getPacket();
	}

	public static MaplePacket enableUse2() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.write(0x176);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static MaplePacket enableUse3() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x51);
		mplew.writeShort(0);

		return mplew.getPacket();
	}

	public static MaplePacket enableUse4() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
		mplew.write(0x53);
		mplew.writeZeroBytes(40);

		return mplew.getPacket();
	}
}