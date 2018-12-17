package client;

public interface IEquip extends IItem {
	public static enum ScrollResult {
		SUCCESS, FAIL, CURSE
	}
	byte getUpgradeSlots();
	byte getLevel();
	public byte getViciousHammer();
	public byte getItemLevel();
	public byte getPotential();
	public byte getPStars();
	public short getStr();
	public short getDex();
	public short getInt();
	public short getLuk();
	public short getHp();
	public short getMp();
	public short getWatk();
	public short getMatk();
	public short getWdef();
	public short getMdef();
	public short getAcc();
	public short getAvoid();
	public short getHands();
	public short getSpeed();
	public short getJump();
	public short getItemEXP();
	public short getPotential_1();
	public short getPotential_2();
	public short getPotential_3();
	public int getRingId();
	public void setPotential(byte paramByte);
	public void setPStars(byte paramByte);
	public void setPotential_1(short paramShort);
	public void setPotential_2(short paramShort);
	public void setPotential_3(short paramShort);
}