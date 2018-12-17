package handling.login;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import client.Equip;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

import client.IItem;

public class LoginInformationProvider {

	private final static LoginInformationProvider instance = new LoginInformationProvider();
	protected final Map<Integer, Map<String, Integer>> equipStatsCache = new HashMap<Integer, Map<String, Integer>>();
	protected final Map<Integer, Equip> equipCache = new HashMap<Integer, Equip>();
	protected final List<String> ForbiddenName = new ArrayList<String>();

	public static LoginInformationProvider getInstance() {
	return instance;
	}

	protected LoginInformationProvider() {
	System.out.println(":: Loading LoginInformationProvider ::");

	final int[] LoadEquipment = {
		1040002, 1040006, 1040010, // top
		1060006, 1060002, 1060138,// Bottom
		1041002, 1041006, 1041010, 1041011, 1042167, 1042180, // Top
		1061002, 1061008, 1062115, 1061160, // Bottom
		1302000, 1322005, 1312004, 1442079, 1302132, // Weapon
		1072001, 1072005, 1072037, 1072038, 1072383, 1072418// Shoes
	};
	final String WZpath = System.getProperty("wzpath");
	final MapleDataProvider equipData = MapleDataProviderFactory.getDataProvider(new File(WZpath + "/Character.wz"));
	for (int i = 0; i < LoadEquipment.length; i++) {
		loadEquipStats(LoadEquipment[i], equipData);
	}

	final MapleData nameData = MapleDataProviderFactory.getDataProvider(new File(WZpath + "/Etc.wz")).getData("ForbiddenName.img");
	for (final MapleData data : nameData.getChildren()) {
		ForbiddenName.add(MapleDataTool.getString(data));
	}
	}

	private final void loadEquipStats(final int itemId, final MapleDataProvider equipData) {
	final MapleData item = getItemData(itemId, equipData);
	if (item == null) {
		return;
	}
	final MapleData info = item.getChildByPath("info");
	if (info == null) {
		return;
	}
		final Map<String, Integer> ret = new LinkedHashMap<String, Integer>();

	for (final MapleData data : info.getChildren()) {
		if (data.getName().startsWith("inc")) {
		ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
		}
	}
	ret.put("tuc", MapleDataTool.getInt("tuc", info, 0));
	ret.put("reqLevel", MapleDataTool.getInt("reqLevel", info, 0));
	ret.put("reqJob", MapleDataTool.getInt("reqJob", info, 0));
	ret.put("reqSTR", MapleDataTool.getInt("reqSTR", info, 0));
	ret.put("reqDEX", MapleDataTool.getInt("reqDEX", info, 0));
	ret.put("reqINT", MapleDataTool.getInt("reqINT", info, 0));
	ret.put("reqLUK", MapleDataTool.getInt("reqLUK", info, 0));
	ret.put("cash", MapleDataTool.getInt("cash", info, 0));
	ret.put("cursed", MapleDataTool.getInt("cursed", info, 0));
	ret.put("success", MapleDataTool.getInt("success", info, 0));
	equipStatsCache.put(itemId, ret);
	}

	private final MapleData getItemData(final int itemId, final MapleDataProvider equipData) {
	MapleData ret = null;
	String idStr = "0" + String.valueOf(itemId);
	MapleDataDirectoryEntry root = equipData.getRoot();
	for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
		for (MapleDataFileEntry iFile : topDir.getFiles()) {
		if (iFile.getName().equals(idStr + ".img")) {
			return equipData.getData(topDir.getName() + "/" + iFile.getName());
		}
		}
	}
	return ret;
	}

	public final IItem getEquipById(final int equipId) {
	final Equip nEquip = new Equip(equipId, (byte) 0, -1, (byte) 0);
	nEquip.setQuantity((short) 1);
	final Map<String, Integer> stats = equipStatsCache.get(equipId);
	if (stats != null) {
		for (Entry<String, Integer> stat : stats.entrySet()) {
		final String key = stat.getKey();
		
		if (key.equals("STR")) {
			nEquip.setStr(stat.getValue().shortValue());
		} else if (key.equals("DEX")) {
			nEquip.setDex(stat.getValue().shortValue());
		} else if (key.equals("INT")) {
			nEquip.setInt(stat.getValue().shortValue());
		} else if (key.equals("LUK")) {
			nEquip.setLuk(stat.getValue().shortValue());
		} else if (key.equals("PAD")) {
			nEquip.setWatk(stat.getValue().shortValue());
		} else if (key.equals("PDD")) {
			nEquip.setWdef(stat.getValue().shortValue());
		} else if (key.equals("MAD")) {
			nEquip.setMatk(stat.getValue().shortValue());
		} else if (key.equals("MDD")) {
			nEquip.setMdef(stat.getValue().shortValue());
		} else if (key.equals("ACC")) {
			nEquip.setAcc(stat.getValue().shortValue());
		} else if (key.equals("EVA")) {
			nEquip.setAvoid(stat.getValue().shortValue());
		} else if (key.equals("Speed")) {
			nEquip.setSpeed(stat.getValue().shortValue());
		} else if (key.equals("Jump")) {
			nEquip.setJump(stat.getValue().shortValue());
		} else if (key.equals("MHP")) {
			nEquip.setHp(stat.getValue().shortValue());
		} else if (key.equals("MMP")) {
			nEquip.setMp(stat.getValue().shortValue());
		} else if (key.equals("tuc")) {
			nEquip.setUpgradeSlots(stat.getValue().byteValue());
		} else if (key.equals("afterImage")) {
		}
		}
	}
	equipCache.put(equipId, nEquip);
	return nEquip.copy();
	}

	public final boolean isForbiddenName(final String in) {
	for (final String name : ForbiddenName) {
		if (in.contains(name)) {
		return true;
		}
	}
	return false;
	}
}