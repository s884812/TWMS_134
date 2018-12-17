package server;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import client.IItem;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class CashItemFactory {
	private static CashItemFactory instance = new CashItemFactory();
	private Map<Integer, CashItemInfo> itemStats = new HashMap<Integer, CashItemInfo>();
	private MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));

	public static CashItemFactory getInstance() {
		return instance;
	}

	protected CashItemFactory() {
		System.out.println(":: Loading CashItemFactory ::");
		for (MapleData field : data.getData("Commodity.img").getChildren()) {
			boolean onSale = MapleDataTool.getIntConvert("OnSale", field, 0) > 0;
			if (onSale) {
				final CashItemInfo stats = new CashItemInfo(
					MapleDataTool.getIntConvert("ItemId", field),
					MapleDataTool.getIntConvert("Count", field, 1),
					MapleDataTool.getIntConvert("Price", field, 0),
					MapleDataTool.getIntConvert("Period", field, 0)
				);
				itemStats.put(MapleDataTool.getIntConvert("SN", field, 0), stats);
			}
		}
	}

	public CashItemInfo getItem(int sn) {
		CashItemInfo stats = itemStats.get(sn);
		if (stats == null) {
			return null;
		}
		return stats;
	}

	public List<Integer> getPackageItems(int itemId) {
		List<Integer> packageItems = new ArrayList<Integer>();
		for (MapleData b : data.getData("CashPackage.img").getChildren()) {
			if (itemId == Integer.parseInt(b.getName())) {
				for (MapleData c : b.getChildren()) {
					for (MapleData d : c.getChildren()) {
						packageItems.add(getItem(MapleDataTool.getIntConvert("" + Integer.parseInt(d.getName()), c)).getId());
					}
				}
				break;
			}
		}
		return packageItems;
	}

	public void addToInventory(IItem item) {
		//inventory.add(item);
	}

	public void removeFromInventory(IItem item) {
		//inventory.remove(item);
	}
}