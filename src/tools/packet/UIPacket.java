package tools.packet;

import handling.MaplePacket;
import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class UIPacket {

	public static final MaplePacket EarnTitleMsg(final String msg) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		//"You have acquired the Pig's Weakness skill."
		mplew.writeShort(SendPacketOpcode.EARN_TITLE_MSG.getValue());
		mplew.writeMapleAsciiString(msg);

		return mplew.getPacket();
	}

	public static MaplePacket getStatusMsg(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		// Temporary transformed as a dragon, even with the skill ......
		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(7);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static MaplePacket getSPMsg(byte sp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(4);
		mplew.writeShort(0);
		mplew.write(sp);

		return mplew.getPacket();
	}

	public static MaplePacket getGPMsg(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		// Temporary transformed as a dragon, even with the skill ......
		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(7);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static final MaplePacket MapNameDisplay(final int mapid) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(3);
		mplew.writeMapleAsciiString("maplemap/enter/" + mapid);

		return mplew.getPacket();
	}

	public static final MaplePacket Aran_Start() {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(4);
		mplew.writeMapleAsciiString("Aran/balloon");

		return mplew.getPacket();
	}

	public static final MaplePacket AranTutInstructionalBalloon(final String data) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(25); // 0x19
		mplew.writeMapleAsciiString(data);
		mplew.writeInt(1);

		return mplew.getPacket();
	}

	public static final MaplePacket EvanTutInstructionalBalloon(final String data) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(3);
		mplew.writeMapleAsciiString(data);

		return mplew.getPacket();
	}

	public static final MaplePacket EvanDragonEyes() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeInt(87548);// FC 55 01 00
		mplew.write(0);

		return mplew.getPacket();
	}

	public static final MaplePacket ShowWZEffect(final String data) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(0x18);
		mplew.writeMapleAsciiString(data);
                mplew.writeInt(1);

		return mplew.getPacket();
	}

	public static MaplePacket summonHelper(boolean summon) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SUMMON_HINT.getValue());
		mplew.write(summon ? 0 : 1);
                mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static MaplePacket summonMessage(int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
		mplew.write(1);
		mplew.writeInt(type);
		mplew.writeInt(7000); // probably the delay

		return mplew.getPacket();
	}

	public static MaplePacket summonMessage(String message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
		mplew.write(0);
		mplew.writeMapleAsciiString(message);
		mplew.writeInt(200);
		mplew.writeInt(4000);

		return mplew.getPacket();
	}

	public static MaplePacket IntroLock(boolean enable) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UI_LOCK.getValue());
		mplew.write(enable ? 1 : 0);

		return mplew.getPacket();
	}

	public static MaplePacket IntroDisableUI(boolean enable) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UI_DISABLE.getValue());
		mplew.write(enable ? 1 : 0);

		return mplew.getPacket();
	}

	public static MaplePacket fishingUpdate(byte type, int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.FISHING_BOARD_UPDATE.getValue());
		mplew.write(type);
		mplew.writeInt(id);

		return mplew.getPacket();
	}

	public static MaplePacket fishingCaught(int chrid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.FISHING_CAUGHT.getValue());
		mplew.writeInt(chrid);

		return mplew.getPacket();
	}

	public static MaplePacket fairyPendantMessage(int hour, int percent) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.FAIRY_PEND_MSG.getValue());
		mplew.writeInt(21); // 15 00 00 00
		mplew.writeInt(hour);
		mplew.writeInt(percent); // 0A 00 00 00

		return mplew.getPacket();
	}
}