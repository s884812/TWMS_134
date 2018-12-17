package tools.packet;

import handling.MaplePacket;
import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class FamilyPacket {
	public static MaplePacket getFamilyData() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.FAMILY.getValue());
		mplew.writeInt(7); // Number of events
		mplew.write(0);
		mplew.writeInt(300); // REP needed
		mplew.writeInt(1); // Number of times allowed per day
		mplew.writeMapleAsciiString("立刻移動至家族成員");
		mplew.writeMapleAsciiString("[對象] 自己\n[效果] 移動至想要的家族成員所在地點");
		mplew.write(1);
		mplew.writeInt(500); // REP needed
		mplew.writeInt(1); // Number of times allowed per day
		mplew.writeMapleAsciiString("立刻召喚家族成員");
		mplew.writeMapleAsciiString("[對象] 家族成員1名\n[效果] 可以將想召喚的家族成員召喚到自己所在的地圖。");
		mplew.write(2);
		mplew.writeInt(700); // REP needed
		mplew.writeInt(1); // Number of times allowed per day
		mplew.writeMapleAsciiString("我的掉寶率1.2倍(15分鐘)");
		mplew.writeMapleAsciiString("[對象] 自己\n[持續時間] 15分鐘\n[效果] 獵捕怪物的掉寶率提升至 #c 1.2倍#。\n※與掉寶值活動重疊時，其效果將無效。");
		mplew.write(3);
		mplew.writeInt(900); // REP needed
		mplew.writeInt(1); // Number of times allowed per day
		mplew.writeMapleAsciiString("我的經驗值1.2倍(15分鐘)");
		mplew.writeMapleAsciiString("[對象] 自己\n[持續時間] 15分鐘\n[效果]獵捕怪物時，將自己取得的經驗值提升至 #c 1.2倍#。\n※與經驗值活動重疊時，其效果將無效。");
		mplew.write(2);
		mplew.writeInt(1500); // REP needed
		mplew.writeInt(1); // Number of times allowed per day
		mplew.writeMapleAsciiString("我的掉寶率1.2倍(30分鐘)");
		mplew.writeMapleAsciiString("[對象] 自己\n[持續時間] 30分鐘\n[效果] 獵捕怪物的掉寶率提升至 #c 1.2倍#。\n※與掉寶值活動重疊時，其效果將無效。");
		mplew.write(3);
		mplew.writeInt(2000); // REP needed
		mplew.writeInt(1); // Number of times allowed per day
		mplew.writeMapleAsciiString("我的經驗值1.2倍(30分鐘)");
		mplew.writeMapleAsciiString("[對象] 自己\n[持續時間] 30分鐘\n[效果]獵捕怪物時，將自己取得的經驗值提升至 #c 1.2倍#。\n※與經驗值活動重疊時，其效果將無效。");
		mplew.write(4);
		mplew.writeInt(3000); // REP needed
		mplew.writeInt(1); // Number of times allowed per day
		mplew.writeMapleAsciiString("家族成員的團結(30分鐘)");
		mplew.writeMapleAsciiString("[發動條件] 家系圖內6位以上的家族成員登入\n[持續時間] 30分鐘\n[效果] 掉寶率和經驗值提升 #c1.5倍# ※經驗值與掉寶率 活動重疊時，其效果將無效。");
		
		return mplew.getPacket();
	}
}