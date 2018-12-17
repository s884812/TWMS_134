package handling.channel.handler;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class PotentialItem {

	private static PotentialItem instance = new PotentialItem();
	public byte incSTR;
	public byte incDEX;
	public byte incINT;
	public byte incLUK;
	public byte incACC;
	public byte incEVA;
	public byte incSpeed;
	public byte incJump;
	public byte incPAD;
	public byte incMAD;
	public byte incPDD;
	public byte incMDD;
	public byte prop;
	public byte time;
	public byte incSTRr;
	public byte incDEXr;
	public byte incINTr;
	public byte incLUKr;
	public byte incMHPr;
	public byte incMMPr;
	public byte incACCr;
	public byte incEVAr;
	public byte incPADr;
	public byte incMADr;
	public byte incPDDr;
	public byte incMDDr;
	public byte incCr;
	public byte incDAMr;
	public byte RecoveryHP;
	public byte RecoveryMP;
	public byte HP;
	public byte MP;
	public byte level;
	public byte ignoreTargetDEF;
	public byte ignoreDAM;
	public byte DAMreflect;
	public byte mpconReduce;
	public byte mpRestore;
	public byte incMesoProp;
	public byte incRewardProp;
	public byte incAllskill;
	public byte ignoreDAMr;
	public byte RecoveryUP;
	public boolean boss;
	public short incMHP;
	public short incMMP;
	public short attackType;
	public short potentialID;
	public short skillID;
	public int optionType;
	public int reqLevel;
	public String face;
	private PotentialItem item;
	private List<PotentialItem> items;
	protected final MapleDataProvider itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Item.wz"));
	protected final MapleData potsData = this.itemData.getData("ItemOption.img");
	protected final Map<Integer, List<PotentialItem>> potentialCache = new HashMap();

	public static PotentialItem getInstance() {
		return instance;
	}

	public final Map<Integer, List<PotentialItem>> getAllPotentialInfo() {
		return this.potentialCache;
	}

	protected PotentialItem() {
		for (MapleData dat : this.potsData) {
			this.items = new LinkedList();
			for (MapleData level : dat.getChildByPath("level")) {
				this.item = new PotentialItem();
				this.item.optionType = MapleDataTool.getIntConvert("info/optionType", dat, 0);
				this.item.reqLevel = MapleDataTool.getIntConvert("info/reqLevel", dat, 0);
				this.item.face = MapleDataTool.getString("face", level, "");
				this.item.boss = (MapleDataTool.getIntConvert("boss", level, 0) > 0);
				this.item.potentialID = Short.parseShort(dat.getName());
				this.item.attackType = (short) MapleDataTool.getIntConvert("attackType", level, 0);
				this.item.incMHP = (short) MapleDataTool.getIntConvert("incMHP", level, 0);
				this.item.incMMP = (short) MapleDataTool.getIntConvert("incMMP", level, 0);
				this.item.incSTR = (byte) MapleDataTool.getIntConvert("incSTR", level, 0);
				this.item.incDEX = (byte) MapleDataTool.getIntConvert("incDEX", level, 0);
				this.item.incINT = (byte) MapleDataTool.getIntConvert("incINT", level, 0);
				this.item.incLUK = (byte) MapleDataTool.getIntConvert("incLUK", level, 0);
				this.item.incACC = (byte) MapleDataTool.getIntConvert("incACC", level, 0);
				this.item.incEVA = (byte) MapleDataTool.getIntConvert("incEVA", level, 0);
				this.item.incSpeed = (byte) MapleDataTool.getIntConvert("incSpeed", level, 0);
				this.item.incJump = (byte) MapleDataTool.getIntConvert("incJump", level, 0);
				this.item.incPAD = (byte) MapleDataTool.getIntConvert("incPAD", level, 0);
				this.item.incMAD = (byte) MapleDataTool.getIntConvert("incMAD", level, 0);
				this.item.incPDD = (byte) MapleDataTool.getIntConvert("incPDD", level, 0);
				this.item.incMDD = (byte) MapleDataTool.getIntConvert("incMDD", level, 0);
				this.item.prop = (byte) MapleDataTool.getIntConvert("prop", level, 0);
				this.item.time = (byte) MapleDataTool.getIntConvert("time", level, 0);
				this.item.incSTRr = (byte) MapleDataTool.getIntConvert("incSTRr", level, 0);
				this.item.incDEXr = (byte) MapleDataTool.getIntConvert("incDEXr", level, 0);
				this.item.incINTr = (byte) MapleDataTool.getIntConvert("incINTr", level, 0);
				this.item.incLUKr = (byte) MapleDataTool.getIntConvert("incLUKr", level, 0);
				this.item.incMHPr = (byte) MapleDataTool.getIntConvert("incMHPr", level, 0);
				this.item.incMMPr = (byte) MapleDataTool.getIntConvert("incMMPr", level, 0);
				this.item.incACCr = (byte) MapleDataTool.getIntConvert("incACCr", level, 0);
				this.item.incEVAr = (byte) MapleDataTool.getIntConvert("incEVAr", level, 0);
				this.item.incPADr = (byte) MapleDataTool.getIntConvert("incPADr", level, 0);
				this.item.incMADr = (byte) MapleDataTool.getIntConvert("incMADr", level, 0);
				this.item.incPDDr = (byte) MapleDataTool.getIntConvert("incPDDr", level, 0);
				this.item.incMDDr = (byte) MapleDataTool.getIntConvert("incMDDr", level, 0);
				this.item.incCr = (byte) MapleDataTool.getIntConvert("incCr", level, 0);
				this.item.incDAMr = (byte) MapleDataTool.getIntConvert("incDAMr", level, 0);
				this.item.RecoveryHP = (byte) MapleDataTool.getIntConvert("RecoveryHP", level, 0);
				this.item.RecoveryMP = (byte) MapleDataTool.getIntConvert("RecoveryMP", level, 0);
				this.item.HP = (byte) MapleDataTool.getIntConvert("HP", level, 0);
				this.item.MP = (byte) MapleDataTool.getIntConvert("MP", level, 0);
				this.item.level = (byte) MapleDataTool.getIntConvert("level", level, 0);
				this.item.ignoreTargetDEF = (byte) MapleDataTool.getIntConvert("ignoreTargetDEF", level, 0);
				this.item.ignoreDAM = (byte) MapleDataTool.getIntConvert("ignoreDAM", level, 0);
				this.item.DAMreflect = (byte) MapleDataTool.getIntConvert("DAMreflect", level, 0);
				this.item.mpconReduce = (byte) MapleDataTool.getIntConvert("mpconReduce", level, 0);
				this.item.mpRestore = (byte) MapleDataTool.getIntConvert("mpRestore", level, 0);
				this.item.incMesoProp = (byte) MapleDataTool.getIntConvert("incMesoProp", level, 0);
				this.item.incRewardProp = (byte) MapleDataTool.getIntConvert("incRewardProp", level, 0);
				this.item.incAllskill = (byte) MapleDataTool.getIntConvert("incAllskill", level, 0);
				this.item.ignoreDAMr = (byte) MapleDataTool.getIntConvert("ignoreDAMr", level, 0);
				this.item.RecoveryUP = (byte) MapleDataTool.getIntConvert("RecoveryUP", level, 0);
				switch (this.item.potentialID) {
					case 31001:
					case 31002:
					case 31003:
					case 31004:
						this.item.skillID = (short) (this.item.potentialID - 23001);
						break;
					default:
						this.item.skillID = 0;
				}

				this.items.add(this.item);
			}
			this.potentialCache.put(Integer.valueOf(Integer.parseInt(dat.getName())), this.items);
		}
	}
}