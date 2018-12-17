package tools.packet;

import handling.MaplePacket;
import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MonsterBookPacket {

    public static MaplePacket addCard(boolean full, int cardid, int level) {
	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.MONSTERBOOK_ADD.getValue());

        if (!full) {
            mplew.write(1);
            mplew.writeInt(cardid);
            mplew.writeInt(level);
        } else {
            mplew.write(0);
        }

	return mplew.getPacket();
    }

    public static MaplePacket showGainCard(final int itemid) {
	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
	mplew.write(0);
	mplew.write(2);
	mplew.writeInt(itemid);

	return mplew.getPacket();
    }

    public static MaplePacket showForeginCardEffect(int id) {
	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
	mplew.writeInt(id);
	mplew.write(0x0D);

	return mplew.getPacket();
    }

    public static MaplePacket changeCover(int cardid) {
	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

	mplew.writeShort(SendPacketOpcode.MONSTERBOOK_CHANGE_COVER.getValue());
	mplew.writeInt(cardid);

	return mplew.getPacket();
    }
}