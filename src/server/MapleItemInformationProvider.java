package server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import client.Equip;
import client.IItem;
import client.ItemFlag;
import client.GameConstants;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import handling.channel.handler.PotentialItem;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

public class MapleItemInformationProvider {

	private final static MapleItemInformationProvider instance = new MapleItemInformationProvider();
	protected final MapleDataProvider itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
	protected final MapleDataProvider equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
	protected final MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
	protected final MapleData cashStringData = stringData.getData("Cash.img");
	protected final MapleData consumeStringData = stringData.getData("Consume.img");
	protected final MapleData eqpStringData = stringData.getData("Eqp.img");
	protected final MapleData etcStringData = stringData.getData("Etc.img");
	protected final MapleData insStringData = stringData.getData("Ins.img");
	protected final MapleData petStringData = stringData.getData("Pet.img");
	protected final Map<Integer, Short> slotMaxCache = new HashMap<Integer, Short>();
	protected final Map<Integer, MapleStatEffect> itemEffects = new HashMap<Integer, MapleStatEffect>();
	protected final Map<Integer, Map<String, Integer>> equipStatsCache = new HashMap<Integer, Map<String, Integer>>();
	protected final Map<Integer, Map<String, Byte>> itemMakeStatsCache = new HashMap<Integer, Map<String, Byte>>();
	protected final Map<Integer, List<StructEquipLevel>> equipLevelCache = new HashMap<Integer, List<StructEquipLevel>>();
	protected final Map<Integer, Short> itemMakeLevel = new HashMap<Integer, Short>();
	protected final Map<Integer, Equip> equipCache = new HashMap<Integer, Equip>();
	protected final Map<Integer, Double> priceCache = new HashMap<Integer, Double>();
	protected final Map<Integer, Integer> wholePriceCache = new HashMap<Integer, Integer>();
	protected final Map<Integer, Integer> projectileWatkCache = new HashMap<Integer, Integer>();
	protected final Map<Integer, Integer> monsterBookID = new HashMap<Integer, Integer>();
	protected final Map<Integer, String> nameCache = new HashMap<Integer, String>();
	protected final Map<Integer, String> descCache = new HashMap<Integer, String>();
	protected final Map<Integer, String> msgCache = new HashMap<Integer, String>();
	protected final Map<Integer, Map<String, Integer>> SkillStatsCache = new HashMap<Integer, Map<String, Integer>>();
	protected final Map<Integer, Byte> consumeOnPickupCache = new HashMap<Integer, Byte>();
	protected final Map<Integer, Boolean> dropRestrictionCache = new HashMap<Integer, Boolean>();
	protected final Map<Integer, Boolean> pickupRestrictionCache = new HashMap<Integer, Boolean>();
	protected final Map<Integer, Integer> stateChangeCache = new HashMap<Integer, Integer>(40);
	protected final Map<Integer, Integer> karmaEnabledCache = new HashMap<Integer, Integer>();
	protected final Map<Integer, Boolean> isQuestItemCache = new HashMap<Integer, Boolean>();
	protected final Map<Integer, List<Pair<Integer, Integer>>> summonMobCache = new HashMap<Integer, List<Pair<Integer, Integer>>>();
	protected final List<Pair<Integer, String>> itemNameCache = new ArrayList<Pair<Integer, String>>();
	protected final Map<Integer, Pair<Integer, List<StructRewardItem>>> RewardItem = new HashMap<Integer, Pair<Integer, List<StructRewardItem>>>();
	private PotentialItem item;
	private List<PotentialItem> items;
	protected final MapleData potsData = this.itemData.getData("ItemOption.img"); // potential data
	protected final Map<Integer, List<PotentialItem>> potentialCache = new HashMap();

	protected MapleItemInformationProvider() {
		System.out.println(":: Loading MapleItemInformationProvider ::");
	}

	public static final MapleItemInformationProvider getInstance() {
		return instance;
	}

	public final List<Pair<Integer, String>> getAllItems() {
		if (itemNameCache.size() != 0) {
			return itemNameCache;
		}
		final List<Pair<Integer, String>> itemPairs = new ArrayList<Pair<Integer, String>>();
		MapleData itemsData;

		itemsData = stringData.getData("Cash.img");
		for (final MapleData itemFolder : itemsData.getChildren()) {
			itemPairs.add(new Pair<Integer, String>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
		}

		itemsData = stringData.getData("Consume.img");
		for (final MapleData itemFolder : itemsData.getChildren()) {
			itemPairs.add(new Pair<Integer, String>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
		}

		itemsData = stringData.getData("Eqp.img").getChildByPath("Eqp");
		for (final MapleData eqpType : itemsData.getChildren()) {
			for (final MapleData itemFolder : eqpType.getChildren()) {
				itemPairs.add(new Pair<Integer, String>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
			}
		}

		itemsData = stringData.getData("Etc.img").getChildByPath("Etc");
		for (final MapleData itemFolder : itemsData.getChildren()) {
			itemPairs.add(new Pair<Integer, String>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
		}

		itemsData = stringData.getData("Ins.img");
		for (final MapleData itemFolder : itemsData.getChildren()) {
			itemPairs.add(new Pair<Integer, String>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
		}

		itemsData = stringData.getData("Pet.img");
		for (final MapleData itemFolder : itemsData.getChildren()) {
			itemPairs.add(new Pair<Integer, String>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
		}
		return itemPairs;
	}

	public final Map<Integer, List<PotentialItem>> getAllPotentialInfo() {
		return this.potentialCache;
	}

	protected final MapleData getStringData(final int itemId) {
		String cat = null;
		MapleData data;

		if (itemId >= 5010000) {
			data = cashStringData;
		} else if (itemId >= 2000000 && itemId < 3000000) {
			data = consumeStringData;
		} else if (itemId >= 1142000 && itemId < 1143000 || itemId >= 1010000 && itemId < 1040000 || itemId >= 1122000 && itemId < 1123000) {
			data = eqpStringData;
			cat = "Accessory";
		} else if (itemId >= 1000000 && itemId < 1010000) {
			data = eqpStringData;
			cat = "Cap";
		} else if (itemId >= 1102000 && itemId < 1103000) {
			data = eqpStringData;
			cat = "Cape";
		} else if (itemId >= 1040000 && itemId < 1050000) {
			data = eqpStringData;
			cat = "Coat";
		} else if (itemId >= 20000 && itemId < 22000) {
			data = eqpStringData;
			cat = "Face";
		} else if (itemId >= 1080000 && itemId < 1090000) {
			data = eqpStringData;
			cat = "Glove";
		} else if (itemId >= 30000 && itemId < 32000) {
			data = eqpStringData;
			cat = "Hair";
		} else if (itemId >= 1050000 && itemId < 1060000) {
			data = eqpStringData;
			cat = "Longcoat";
		} else if (itemId >= 1060000 && itemId < 1070000) {
			data = eqpStringData;
			cat = "Pants";
		} else if (itemId >= 1802000 && itemId < 1810000) {
			data = eqpStringData;
			cat = "PetEquip";
		} else if (itemId >= 1112000 && itemId < 1120000) {
			data = eqpStringData;
			cat = "Ring";
		} else if (itemId >= 1092000 && itemId < 1100000) {
			data = eqpStringData;
			cat = "Shield";
		} else if (itemId >= 1070000 && itemId < 1080000) {
			data = eqpStringData;
			cat = "Shoes";
		} else if (itemId >= 1900000 && itemId < 2000000) {
			data = eqpStringData;
			cat = "Taming";
		} else if (itemId >= 1300000 && itemId < 1800000) {
			data = eqpStringData;
			cat = "Weapon";
		} else if (itemId >= 4000000 && itemId < 5000000) {
			data = etcStringData;
		} else if (itemId >= 3000000 && itemId < 4000000) {
			data = insStringData;
		} else if (itemId >= 5000000 && itemId < 5010000) {
			data = petStringData;
		} else {
			return null;
		}
		if (cat == null) {
			return data.getChildByPath(String.valueOf(itemId));
		} else {
			return data.getChildByPath("Eqp/" + cat + "/" + itemId);
		}
	}

	protected final MapleData getItemData(final int itemId) {
		MapleData ret = null;
		final String idStr = "0" + String.valueOf(itemId);
		MapleDataDirectoryEntry root = itemData.getRoot();
		for (final MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
			// we should have .img files here beginning with the first 4 IID
			for (final MapleDataFileEntry iFile : topDir.getFiles()) {
				if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
					ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
					if (ret == null) {
						return null;
					}
					ret = ret.getChildByPath(idStr);
					return ret;
				} else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
					return itemData.getData(topDir.getName() + "/" + iFile.getName());
				}
			}
		}
		root = equipData.getRoot();
		for (final MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
			for (final MapleDataFileEntry iFile : topDir.getFiles()) {
				if (iFile.getName().equals(idStr + ".img")) {
					return equipData.getData(topDir.getName() + "/" + iFile.getName());
				}
			}
		}
		return ret;
	}

	/** returns the maximum of items in one slot */
	public final short getSlotMax(final MapleClient c, final int itemId) {
		if (slotMaxCache.containsKey(itemId)) {
			return slotMaxCache.get(itemId);
		}
		short ret = 0;
		final MapleData item = getItemData(itemId);
		if (item != null) {
			final MapleData smEntry = item.getChildByPath("info/slotMax");
			if (smEntry == null) {
				if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
					ret = 1;
				} else {
					ret = 100;
				}
			} else {
				ret = (short) MapleDataTool.getInt(smEntry);
			}
		}
		slotMaxCache.put(itemId, ret);
		return ret;
	}

	public final int getWholePrice(final int itemId) {
		if (wholePriceCache.containsKey(itemId)) {
			return wholePriceCache.get(itemId);
		}
		final MapleData item = getItemData(itemId);
		if (item == null) {
			return -1;
		}
		int pEntry = 0;
		final MapleData pData = item.getChildByPath("info/price");
		if (pData == null) {
			return -1;
		}
		pEntry = MapleDataTool.getInt(pData);

		wholePriceCache.put(itemId, pEntry);
		return pEntry;
	}

	public final double getPrice(final int itemId) {
		if (priceCache.containsKey(itemId)) {
			return priceCache.get(itemId);
		}
		final MapleData item = getItemData(itemId);
		if (item == null) {
			return -1;
		}
		double pEntry = 0.0;
		MapleData pData = item.getChildByPath("info/unitPrice");
		if (pData != null) {
			try {
				pEntry = MapleDataTool.getDouble(pData);
			} catch (Exception e) {
				pEntry = (double) MapleDataTool.getInt(pData);
			}
		} else {
			pData = item.getChildByPath("info/price");
			if (pData == null) {
				return -1;
			}
			pEntry = (double) MapleDataTool.getInt(pData);
		}
		if (itemId == 2070019 || itemId == 2330007) {
			pEntry = 1.0;
		}
		priceCache.put(itemId, pEntry);
		return pEntry;
	}

	public final List<StructEquipLevel> getEquipLevelStat(final int itemid, final byte level) {
		if (equipLevelCache.containsKey(itemid)) {
			return equipLevelCache.get(itemid);
		}
		final MapleData item = getItemData(itemid);
		if (item == null) {
			return null;
		}
		final MapleData info = item.getChildByPath("info/level");
		if (info == null) {
			return null;
		}
		final List<StructEquipLevel> el = new ArrayList<StructEquipLevel>();
		StructEquipLevel sel;

		for (final MapleData data : info.getChildByPath("info")) {
			sel = new StructEquipLevel();
			sel.incSTRMax = (byte) MapleDataTool.getInt("incSTRMax", data, 0);
			sel.incSTRMin = (byte) MapleDataTool.getInt("incSTRMin", data, 0);

			sel.incDEXMax = (byte) MapleDataTool.getInt("incDEXMax", data, 0);
			sel.incDEXMin = (byte) MapleDataTool.getInt("incDEXMin", data, 0);

			sel.incLUKMax = (byte) MapleDataTool.getInt("incLUKMax", data, 0);
			sel.incLUKMin = (byte) MapleDataTool.getInt("incLUKMin", data, 0);

			sel.incINTMax = (byte) MapleDataTool.getInt("incINTMax", data, 0);
			sel.incINTMin = (byte) MapleDataTool.getInt("incINTMin", data, 0);

			sel.incPADMax = (byte) MapleDataTool.getInt("incPADMax", data, 0);
			sel.incPADMin = (byte) MapleDataTool.getInt("incPADMin", data, 0);

			sel.incMADMax = (byte) MapleDataTool.getInt("incMADMax", data, 0);
			sel.incMADMin = (byte) MapleDataTool.getInt("incMADMin", data, 0);

			el.add(sel);
		}
		equipLevelCache.put(itemid, el);
		return el;
	}

	public final Map<String, Byte> getItemMakeStats(final int itemId) {
		if (itemMakeStatsCache.containsKey(itemId)) {
			return itemMakeStatsCache.get(itemId);
		}
		if (itemId / 10000 != 425) {
			return null;
		}
		final Map<String, Byte> ret = new LinkedHashMap<String, Byte>();
		final MapleData item = getItemData(itemId);
		if (item == null) {
			return null;
		}
		final MapleData info = item.getChildByPath("info");
		if (info == null) {
			return null;
		}
		ret.put("incPAD", (byte) MapleDataTool.getInt("incPAD", info, 0)); // WATK
		ret.put("incMAD", (byte) MapleDataTool.getInt("incMAD", info, 0)); // MATK
		ret.put("incACC", (byte) MapleDataTool.getInt("incACC", info, 0)); // ACC
		ret.put("incEVA", (byte) MapleDataTool.getInt("incEVA", info, 0)); // AVOID
		ret.put("incSpeed", (byte) MapleDataTool.getInt("incSpeed", info, 0)); // SPEED
		ret.put("incJump", (byte) MapleDataTool.getInt("incJump", info, 0)); // JUMP
		ret.put("incMaxHP", (byte) MapleDataTool.getInt("incMaxHP", info, 0)); // HP
		ret.put("incMaxMP", (byte) MapleDataTool.getInt("incMaxMP", info, 0)); // MP
		ret.put("incSTR", (byte) MapleDataTool.getInt("incSTR", info, 0)); // STR
		ret.put("incINT", (byte) MapleDataTool.getInt("incINT", info, 0)); // INT
		ret.put("incLUK", (byte) MapleDataTool.getInt("incLUK", info, 0)); // LUK
		ret.put("incDEX", (byte) MapleDataTool.getInt("incDEX", info, 0)); // DEX
//	ret.put("incReqLevel", MapleDataTool.getInt("incReqLevel", info, 0)); // IDK!
		ret.put("randOption", (byte) MapleDataTool.getInt("randOption", info, 0)); // Black Crystal Wa/MA
		ret.put("randStat", (byte) MapleDataTool.getInt("randStat", info, 0)); // Dark Crystal - Str/Dex/int/Luk

		itemMakeStatsCache.put(itemId, ret);
		return ret;
	}

	public final Map<String, Integer> getEquipStats(final int itemId) {
		if (equipStatsCache.containsKey(itemId)) {
			return equipStatsCache.get(itemId);
		}
		final Map<String, Integer> ret = new LinkedHashMap<String, Integer>();
		final MapleData item = getItemData(itemId);
		if (item == null) {
			return null;
		}
		final MapleData info = item.getChildByPath("info");
		if (info == null) {
			return null;
		}
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
		ret.put("reqPOP", MapleDataTool.getInt("reqPOP", info, 0));
		ret.put("cash", MapleDataTool.getInt("cash", info, 0));
		ret.put("canLevel", info.getChildByPath("level") == null ? 0 : 1);
		ret.put("cursed", MapleDataTool.getInt("cursed", info, 0));
		ret.put("success", MapleDataTool.getInt("success", info, 0));
		ret.put("equipTradeBlock", MapleDataTool.getInt("equipTradeBlock", info, 0));

		if (GameConstants.isMagicWeapon(itemId)) {
			ret.put("elemDefault", MapleDataTool.getInt("elemDefault", info, 100));
			ret.put("incRMAS", MapleDataTool.getInt("incRMAS", info, 100)); // Poison
			ret.put("incRMAF", MapleDataTool.getInt("incRMAF", info, 100)); // Fire
			ret.put("incRMAL", MapleDataTool.getInt("incRMAL", info, 100)); // Lightning
			ret.put("incRMAI", MapleDataTool.getInt("incRMAI", info, 100)); // Ice
		}

		equipStatsCache.put(itemId, ret);
		return ret;
	}

	public final boolean canEquip(final Map<String, Integer> stats, final int itemid, final int level, final int job, final int fame, final int str, final int dex, final int luk, final int int_) {
		if (level >= stats.get("reqLevel") && str >= stats.get("reqSTR") && dex >= stats.get("reqDEX") && luk >= stats.get("reqLUK") && int_ >= stats.get("reqINT")) {
			final int fameReq = stats.get("reqPOP");
			if (fameReq != 0 && fame < fameReq) {
				return false;
			}
			return true;
		}
		return false;
	}

	public final int getReqLevel(final int itemId) {
		return getEquipStats(itemId).get("reqLevel");
	}

	public final List<Integer> getScrollReqs(final int itemId) {
		final List<Integer> ret = new ArrayList<Integer>();
		final MapleData data = getItemData(itemId).getChildByPath("req");

		if (data == null) {
			return ret;
		}
		for (final MapleData req : data.getChildren()) {
			ret.add(MapleDataTool.getInt(req));
		}
		return ret;
	}

	public final IItem scrollEquipWithId(final IItem equip, final int scrollId, final boolean ws) {
		if (equip.getType() == 1) { // See IItem.java
			final Equip nEquip = (Equip) equip;
			final Map<String, Integer> stats = getEquipStats(scrollId);
			final Map<String, Integer> eqstats = getEquipStats(equip.getItemId());

			if (Randomizer.nextInt(100) <= stats.get("success")) {
				switch (scrollId) {
					case 2049000:
					case 2049001:
					case 2049002:
					case 2049003: {
						if (nEquip.getLevel() + nEquip.getUpgradeSlots() < eqstats.get("tuc")) {
							nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() + 1));
						}
						break;
					}
					case 2040727: // Spikes on shoe, prevents slip
					{
						byte flag = nEquip.getFlag();
						flag |= ItemFlag.SPIKES.getValue();
						nEquip.setFlag(flag);
						break;
					}
					case 2041058: // Cape for Cold protection
					{
						byte flag = nEquip.getFlag();
						flag |= ItemFlag.COLD.getValue();
						nEquip.setFlag(flag);
						break;
					}
					case 2049100: // Chaos Scroll
					case 2049101: // Liar's Wood Liquid
					case 2049102: // Maple Syrup
					case 2049104: // Angent Equipmenet scroll
					case 2049103: { // Beach Sandals Scroll
						final int increase = Randomizer.nextBoolean() ? 1 : -1;

						if (nEquip.getStr() > 0) {
							nEquip.setStr((short) (nEquip.getStr() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getDex() > 0) {
							nEquip.setDex((short) (nEquip.getDex() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getInt() > 0) {
							nEquip.setInt((short) (nEquip.getInt() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getLuk() > 0) {
							nEquip.setLuk((short) (nEquip.getLuk() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getWatk() > 0) {
							nEquip.setWatk((short) (nEquip.getWatk() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getWdef() > 0) {
							nEquip.setWdef((short) (nEquip.getWdef() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getMatk() > 0) {
							nEquip.setMatk((short) (nEquip.getMatk() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getMdef() > 0) {
							nEquip.setMdef((short) (nEquip.getMdef() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getAcc() > 0) {
							nEquip.setAcc((short) (nEquip.getAcc() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getAvoid() > 0) {
							nEquip.setAvoid((short) (nEquip.getAvoid() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getSpeed() > 0) {
							nEquip.setSpeed((short) (nEquip.getSpeed() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getJump() > 0) {
							nEquip.setJump((short) (nEquip.getJump() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getHp() > 0) {
							nEquip.setHp((short) (nEquip.getHp() + Randomizer.nextInt(5) * increase));
						}
						if (nEquip.getMp() > 0) {
							nEquip.setMp((short) (nEquip.getMp() + Randomizer.nextInt(5) * increase));
						}
						break;
					}
					default: {
						for (Entry<String, Integer> stat : stats.entrySet()) {
							final String key = stat.getKey();

							if (key.equals("STR")) {
								nEquip.setStr((short) (nEquip.getStr() + stat.getValue().intValue()));
							} else if (key.equals("DEX")) {
								nEquip.setDex((short) (nEquip.getDex() + stat.getValue().intValue()));
							} else if (key.equals("INT")) {
								nEquip.setInt((short) (nEquip.getInt() + stat.getValue().intValue()));
							} else if (key.equals("LUK")) {
								nEquip.setLuk((short) (nEquip.getLuk() + stat.getValue().intValue()));
							} else if (key.equals("PAD")) {
								nEquip.setWatk((short) (nEquip.getWatk() + stat.getValue().intValue()));
							} else if (key.equals("PDD")) {
								nEquip.setWdef((short) (nEquip.getWdef() + stat.getValue().intValue()));
							} else if (key.equals("MAD")) {
								nEquip.setMatk((short) (nEquip.getMatk() + stat.getValue().intValue()));
							} else if (key.equals("MDD")) {
								nEquip.setMdef((short) (nEquip.getMdef() + stat.getValue().intValue()));
							} else if (key.equals("ACC")) {
								nEquip.setAcc((short) (nEquip.getAcc() + stat.getValue().intValue()));
							} else if (key.equals("EVA")) {
								nEquip.setAvoid((short) (nEquip.getAvoid() + stat.getValue().intValue()));
							} else if (key.equals("Speed")) {
								nEquip.setSpeed((short) (nEquip.getSpeed() + stat.getValue().intValue()));
							} else if (key.equals("Jump")) {
								nEquip.setJump((short) (nEquip.getJump() + stat.getValue().intValue()));
							} else if (key.equals("MHP")) {
								nEquip.setHp((short) (nEquip.getHp() + stat.getValue().intValue()));
							} else if (key.equals("MMP")) {
								nEquip.setMp((short) (nEquip.getMp() + stat.getValue().intValue()));
//			    } else if (stat.getKey().equals("afterImage")) {
							}
						}
						break;
					}
				}
				if (!GameConstants.isCleanSlate(scrollId) && !GameConstants.isSpecialScroll(scrollId)) {
					if (nEquip.getItemId() != MapleCharacter.unlimitedSlotItem) { // unlimited slot item check
						nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
					}
					nEquip.setLevel((byte) (nEquip.getLevel() + 1));
				}
			} else {
				if (!ws && !GameConstants.isCleanSlate(scrollId) && !GameConstants.isSpecialScroll(scrollId)) {
					if (nEquip.getItemId() != MapleCharacter.unlimitedSlotItem) { // unlimited slot item check
						nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
					}
				}
				if (nEquip.getItemId() != MapleCharacter.unlimitedSlotItem) { // unlimited slot item check
					if (Randomizer.nextInt(99) < stats.get("cursed")) {
						return null;
					}
				}
			}
		}
		return equip;
	}

	public final IItem scrollHiddenPotential(IItem equip, int scrollId) {
		if ((equip instanceof Equip)) {
			Equip nEquip = (Equip) equip;
			int rate = scrollId == 2049400 ? 90 : 70;

			if (Randomizer.nextInt(100) <= rate) {
				switch (scrollId) {
					case 2049400:
					case 2049401:
						nEquip.setPotential((byte) 3);
				}
			} else {
				return null;
			}
		}
		return equip;
	}

	public IItem scrollEnhancement(IItem equip, int scrollId) {
		if ((equip instanceof Equip)) {
			Equip nEquip = (Equip) equip;
			int rate = 0;
			switch (nEquip.getPStars()) {
				case 0:
					rate = scrollId == 2049300 ? 100 : 80;
					break;
				case 1:
					rate = scrollId == 2049300 ? 90 : 70;
					break;
				case 2:
					rate = scrollId == 2049300 ? 80 : 60;
					break;
				case 3:
					rate = scrollId == 2049300 ? 70 : 50;
					break;
				case 4:
					rate = scrollId == 2049300 ? 60 : 40;
					break;
				case 5:
					rate = scrollId == 2049300 ? 50 : 30;
					break;
				case 6:
					rate = scrollId == 2049300 ? 40 : 20;
					break;
				case 7:
					rate = scrollId == 2049300 ? 30 : 10;
					break;
				case 8:
					rate = scrollId == 2049300 ? 20 : 10;
					break;
				default:
					rate = scrollId == 2049300 ? 10 : 10;
			}

			if (Randomizer.nextInt(100) <= rate) {
				nEquip.setPStars((byte) (nEquip.getPStars() + 1));
				if (nEquip.getStr() > 0) {
					nEquip.setStr((short) (nEquip.getStr() + Randomizer.nextInt(5)));
				}
				if (nEquip.getDex() > 0) {
					nEquip.setDex((short) (nEquip.getDex() + Randomizer.nextInt(5)));
				}
				if (nEquip.getInt() > 0) {
					nEquip.setInt((short) (nEquip.getInt() + Randomizer.nextInt(5)));
				}
				if (nEquip.getLuk() > 0) {
					nEquip.setLuk((short) (nEquip.getLuk() + Randomizer.nextInt(5)));
				}
				if (nEquip.getWatk() > 0) {
					nEquip.setWatk((short) (nEquip.getWatk() + Randomizer.nextInt(5)));
				}
				if (nEquip.getWdef() > 0) {
					nEquip.setWdef((short) (nEquip.getWdef() + Randomizer.nextInt(5)));
				}
				if (nEquip.getMatk() > 0) {
					nEquip.setMatk((short) (nEquip.getMatk() + Randomizer.nextInt(5)));
				}
				if (nEquip.getMdef() > 0) {
					nEquip.setMdef((short) (nEquip.getMdef() + Randomizer.nextInt(5)));
				}
				if (nEquip.getAcc() > 0) {
					nEquip.setAcc((short) (nEquip.getAcc() + Randomizer.nextInt(5)));
				}
				if (nEquip.getAvoid() > 0) {
					nEquip.setAvoid((short) (nEquip.getAvoid() + Randomizer.nextInt(5)));
				}
				if (nEquip.getSpeed() > 0) {
					nEquip.setSpeed((short) (nEquip.getSpeed() + Randomizer.nextInt(5)));
				}
				if (nEquip.getJump() > 0) {
					nEquip.setJump((short) (nEquip.getJump() + Randomizer.nextInt(5)));
				}
				if (nEquip.getHp() > 0) {
					nEquip.setHp((short) (nEquip.getHp() + Randomizer.nextInt(5)));
				}
				if (nEquip.getMp() > 0) {
					nEquip.setMp((short) (nEquip.getMp() + Randomizer.nextInt(5)));
				}
			} else {
				return null;
			}
		}
		return equip;
	}

	public final IItem getEquipById(final int equipId) {
		return getEquipById(equipId, -1);
	}

	public final IItem getEquipById(final int equipId, final int ringId) {
		final Equip nEquip = new Equip(equipId, (byte) 0, ringId, (byte) 0);
		nEquip.setQuantity((short) 1);
		final Map<String, Integer> stats = getEquipStats(equipId);
		if (stats != null) {
			for (Entry<String, Integer> stat : stats.entrySet()) {
				final String key = stat.getKey();

				if (key.equals("STR")) {
					nEquip.setStr((short) stat.getValue().intValue());
				} else if (key.equals("DEX")) {
					nEquip.setDex((short) stat.getValue().intValue());
				} else if (key.equals("INT")) {
					nEquip.setInt((short) stat.getValue().intValue());
				} else if (key.equals("LUK")) {
					nEquip.setLuk((short) stat.getValue().intValue());
				} else if (key.equals("PAD")) {
					nEquip.setWatk((short) stat.getValue().intValue());
				} else if (key.equals("PDD")) {
					nEquip.setWdef((short) stat.getValue().intValue());
				} else if (key.equals("MAD")) {
					nEquip.setMatk((short) stat.getValue().intValue());
				} else if (key.equals("MDD")) {
					nEquip.setMdef((short) stat.getValue().intValue());
				} else if (key.equals("ACC")) {
					nEquip.setAcc((short) stat.getValue().intValue());
				} else if (key.equals("EVA")) {
					nEquip.setAvoid((short) stat.getValue().intValue());
				} else if (key.equals("Speed")) {
					nEquip.setSpeed((short) stat.getValue().intValue());
				} else if (key.equals("Jump")) {
					nEquip.setJump((short) stat.getValue().intValue());
				} else if (key.equals("MHP")) {
					nEquip.setHp((short) stat.getValue().intValue());
				} else if (key.equals("MMP")) {
					nEquip.setMp((short) stat.getValue().intValue());
				} else if (key.equals("tuc")) {
					nEquip.setUpgradeSlots((byte) stat.getValue().intValue());
				} else if (key.equals("Craft")) {
					nEquip.setHands((short) stat.getValue().intValue());
//                } else if (key.equals("afterImage")) {
				}
			}
		}
		equipCache.put(equipId, nEquip);
		return nEquip.copy();
	}

	private final short getRandStat(final short defaultValue, final int maxRange) {
		if (defaultValue == 0) {
			return 0;
		}
		// vary no more than ceil of 10% of stat
		final int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1), maxRange);

		return (short) ((defaultValue - lMaxRange) + Math.floor(Math.random() * (lMaxRange * 2 + 1)));
	}

	public final Equip randomizeStats(final Equip equip) {
		equip.setStr(getRandStat(equip.getStr(), 5));
		equip.setDex(getRandStat(equip.getDex(), 5));
		equip.setInt(getRandStat(equip.getInt(), 5));
		equip.setLuk(getRandStat(equip.getLuk(), 5));
		equip.setMatk(getRandStat(equip.getMatk(), 5));
		equip.setWatk(getRandStat(equip.getWatk(), 5));
		equip.setAcc(getRandStat(equip.getAcc(), 5));
		equip.setAvoid(getRandStat(equip.getAvoid(), 5));
		equip.setJump(getRandStat(equip.getJump(), 5));
		equip.setHands(getRandStat(equip.getHands(), 5));
		equip.setSpeed(getRandStat(equip.getSpeed(), 5));
		equip.setWdef(getRandStat(equip.getWdef(), 10));
		equip.setMdef(getRandStat(equip.getMdef(), 10));
		equip.setHp(getRandStat(equip.getHp(), 10));
		equip.setMp(getRandStat(equip.getMp(), 10));
		if ((byte) (int) (Math.random() * 100.0D) < 10) {
			equip.setPotential((byte) 3);
		} else {
			equip.setPotential((byte) 0); // Incase somehow din't input 0! JUST IN CASE!
		}
		return equip;
	}

	public final MapleStatEffect getItemEffect(final int itemId) {
		MapleStatEffect ret = itemEffects.get(Integer.valueOf(itemId));
		if (ret == null) {
			final MapleData item = getItemData(itemId);
			if (item == null) {
				return null;
			}
			ret = MapleStatEffect.loadItemEffectFromData(item.getChildByPath("spec"), itemId);
			itemEffects.put(Integer.valueOf(itemId), ret);
		}
		return ret;
	}

	public final List<Pair<Integer, Integer>> getSummonMobs(final int itemId) {
		if (summonMobCache.containsKey(Integer.valueOf(itemId))) {
			return summonMobCache.get(itemId);
		}
		if (!GameConstants.isSummonSack(itemId)) {
			return null;
		}
		final MapleData data = getItemData(itemId).getChildByPath("mob");
		if (data == null) {
			return null;
		}
		final List<Pair<Integer, Integer>> mobPairs = new ArrayList<Pair<Integer, Integer>>();

		for (final MapleData child : data.getChildren()) {
			mobPairs.add(new Pair(
					MapleDataTool.getIntConvert("id", child),
					MapleDataTool.getIntConvert("prob", child)));
		}
		summonMobCache.put(itemId, mobPairs);
		return mobPairs;
	}

	public final int getCardMobId(final int id) {
		if (id == 0) {
			return 0;
		}
		if (monsterBookID.containsKey(id)) {
			return monsterBookID.get(id);
		}
		final MapleData data = getItemData(id);
		final int monsterid = MapleDataTool.getIntConvert("info/mob", data, 0);

		if (monsterid == 0) { // Hack.
			return 0;
		}
		monsterBookID.put(id, monsterid);
		return monsterBookID.get(id);
	}

	public final int getWatkForProjectile(final int itemId) {
		Integer atk = projectileWatkCache.get(itemId);
		if (atk != null) {
			return atk.intValue();
		}
		final MapleData data = getItemData(itemId);
		atk = Integer.valueOf(MapleDataTool.getInt("info/incPAD", data, 0));
		projectileWatkCache.put(itemId, atk);
		return atk.intValue();
	}

	public final boolean canScroll(final int scrollid, final int itemid) {
		return (scrollid / 100) % 100 == (itemid / 10000) % 100;
	}

	public final String getName(final int itemId) {
		if (nameCache.containsKey(itemId)) {
			return nameCache.get(itemId);
		}
		final MapleData strings = getStringData(itemId);
		if (strings == null) {
			return null;
		}
		final String ret = MapleDataTool.getString("name", strings, null);
		nameCache.put(itemId, ret);
		return ret;
	}

	public final String getDesc(final int itemId) {
		if (descCache.containsKey(itemId)) {
			return descCache.get(itemId);
		}
		final MapleData strings = getStringData(itemId);
		if (strings == null) {
			return null;
		}
		final String ret = MapleDataTool.getString("desc", strings, null);
		descCache.put(itemId, ret);
		return ret;
	}

	public final String getMsg(final int itemId) {
		if (msgCache.containsKey(itemId)) {
			return msgCache.get(itemId);
		}
		final MapleData strings = getStringData(itemId);
		if (strings == null) {
			return null;
		}
		final String ret = MapleDataTool.getString("msg", strings, null);
		msgCache.put(itemId, ret);
		return ret;
	}

	public final short getItemMakeLevel(final int itemId) {
		if (itemMakeLevel.containsKey(itemId)) {
			return itemMakeLevel.get(itemId);
		}
		if (itemId / 10000 != 400) {
			return 0;
		}
		final short lvl = (short) MapleDataTool.getIntConvert("info/lv", getItemData(itemId), 0);
		itemMakeLevel.put(itemId, lvl);
		return lvl;
	}

	public final byte isConsumeOnPickup(final int itemId) {
		// 0 = not, 1 = consume on pickup, 2 = consume + party
		if (consumeOnPickupCache.containsKey(itemId)) {
			return consumeOnPickupCache.get(itemId);
		}
		final MapleData data = getItemData(itemId);
		byte consume = (byte) MapleDataTool.getIntConvert("spec/consumeOnPickup", data, 0);
		if (consume == 0) {
			consume = (byte) MapleDataTool.getIntConvert("specEx/consumeOnPickup", data, 0);
		}
		if (consume == 1) {
			if (MapleDataTool.getIntConvert("spec/party", getItemData(itemId), 0) > 0) {
				consume = 2;
			}
		}
		consumeOnPickupCache.put(itemId, consume);
		return consume;
	}

	public final boolean isDropRestricted(final int itemId) {
		if (dropRestrictionCache.containsKey(itemId)) {
			return dropRestrictionCache.get(itemId);
		}
		final MapleData data = getItemData(itemId);

		boolean trade = false;
		if (MapleDataTool.getIntConvert("info/tradeBlock", data, 0) == 1
				|| MapleDataTool.getIntConvert("info/quest", data, 0) == 1) {
			trade = true;
		}
		dropRestrictionCache.put(itemId, trade);
		return trade;
	}

	public final boolean isPickupRestricted(final int itemId) {
		if (pickupRestrictionCache.containsKey(itemId)) {
			return pickupRestrictionCache.get(itemId);
		}
		final boolean bRestricted = MapleDataTool.getIntConvert("info/only", getItemData(itemId), 0) == 1;

		pickupRestrictionCache.put(itemId, bRestricted);
		return bRestricted;
	}

	public final int getStateChangeItem(final int itemId) {
		if (stateChangeCache.containsKey(itemId)) {
			return stateChangeCache.get(itemId);
		}
		final int triggerItem = MapleDataTool.getIntConvert("info/stateChangeItem", getItemData(itemId), 0);
		stateChangeCache.put(itemId, triggerItem);
		return triggerItem;
	}

	public final boolean isKarmaEnabled(final int itemId, final int karmaID) {
		if (karmaEnabledCache.containsKey(itemId)) {
			return karmaEnabledCache.get(itemId) == (karmaID % 10 + 1);
		}
		final int bRestricted = MapleDataTool.getIntConvert("info/tradeAvailable", getItemData(itemId), 0);

		karmaEnabledCache.put(itemId, bRestricted);
		return bRestricted == (karmaID % 10 + 1);
	}

	public final Pair<Integer, List<StructRewardItem>> getRewardItem(final int itemid) {
		if (RewardItem.containsKey(itemid)) {
			return RewardItem.get(itemid);
		}
		final MapleData data = getItemData(itemid);
		if (data == null) {
			return null;
		}
		final MapleData rewards = data.getChildByPath("reward");
		if (rewards == null) {
			return null;
		}
		int totalprob = 0; // As there are some rewards with prob above 2000, we can't assume it's always 100
		List<StructRewardItem> all = new ArrayList();

		for (final MapleData reward : rewards) {
			StructRewardItem struct = new StructRewardItem();

			struct.itemid = MapleDataTool.getInt("item", reward, 0);
			struct.prob = (byte) MapleDataTool.getInt("prob", reward, 0);
			struct.quantity = (short) MapleDataTool.getInt("count", reward, 0);
			struct.effect = MapleDataTool.getString("Effect", reward, "");
			struct.worldmsg = MapleDataTool.getString("worldMsg", reward, null);
			struct.period = MapleDataTool.getInt("period", reward, -1);

			totalprob += struct.prob;

			all.add(struct);
		}
		Pair<Integer, List<StructRewardItem>> toreturn = new Pair(totalprob, all);
		RewardItem.put(itemid, toreturn);
		return toreturn;
	}

	public final Map<String, Integer> getSkillStats(final int itemId) {
		if (SkillStatsCache.containsKey(itemId)) {
			return SkillStatsCache.get(itemId);
		}
		if (!(itemId / 10000 == 228 || itemId / 10000 == 229)) { // Skillbook and mastery book
			return null;
		}
		final MapleData item = getItemData(itemId);
		if (item == null) {
			return null;
		}
		final MapleData info = item.getChildByPath("info");
		if (info == null) {
			return null;
		}
		final Map<String, Integer> ret = new LinkedHashMap<String, Integer>();
		for (final MapleData data : info.getChildren()) {
			if (data.getName().startsWith("inc")) {
				ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
			}
		}
		ret.put("masterLevel", MapleDataTool.getInt("masterLevel", info, 0));
		ret.put("reqSkillLevel", MapleDataTool.getInt("reqSkillLevel", info, 0));
		ret.put("success", MapleDataTool.getInt("success", info, 0));

		final MapleData skill = info.getChildByPath("skill");

		for (int i = 0; i < skill.getChildren().size(); i++) { // List of allowed skillIds
			ret.put("skillid" + i, MapleDataTool.getInt(Integer.toString(i), skill, 0));
		}
		SkillStatsCache.put(itemId, ret);
		return ret;
	}

	public final List<Integer> petsCanConsume(final int itemId) {
		final List<Integer> ret = new ArrayList<Integer>();
		final MapleData data = getItemData(itemId);
		int curPetId = 0;
		int size = data.getChildren().size();
		for (int i = 0; i < size; i++) {
			curPetId = MapleDataTool.getInt("spec/" + Integer.toString(i), data, 0);
			if (curPetId == 0) {
				break;
			}
			ret.add(Integer.valueOf(curPetId));
		}
		return ret;
	}

	public final boolean isQuestItem(final int itemId) {
		if (isQuestItemCache.containsKey(itemId)) {
			return isQuestItemCache.get(itemId);
		}
		final boolean questItem = MapleDataTool.getIntConvert("info/quest", getItemData(itemId), 0) == 1;
		isQuestItemCache.put(itemId, questItem);
		return questItem;
	}

	public final boolean isCash(final int itemid) {
		if (getEquipStats(itemid) == null) {
			return GameConstants.getInventoryType(itemid) == MapleInventoryType.CASH;
		}
		return GameConstants.getInventoryType(itemid) == MapleInventoryType.CASH || getEquipStats(itemid).get("cash") > 0;
	}

	public Equip hardcoreItem(Equip equip, short stat) {
		equip.setStr(stat);
		equip.setDex(stat);
		equip.setInt(stat);
		equip.setLuk(stat);
		equip.setMatk(stat);
		equip.setWatk(stat);
		equip.setAcc(stat);
		equip.setAvoid(stat);
		equip.setJump(stat);
		equip.setSpeed(stat);
		equip.setWdef(stat);
		equip.setMdef(stat);
		equip.setHp(stat);
		equip.setMp(stat);
		return equip;
	}
}