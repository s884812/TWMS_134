package server.maps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;
import tools.StringUtil;

public class MapleReactorFactory {

    private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Reactor.wz"));
    private static Map<Integer, MapleReactorStats> reactorStats = new HashMap<Integer, MapleReactorStats>();

    public static final MapleReactorStats getReactor(int rid) {
	MapleReactorStats stats = reactorStats.get(Integer.valueOf(rid));
	if (stats == null) {
	    int infoId = rid;
	    MapleData reactorData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(infoId) + ".img", '0', 11));
	    MapleData link = reactorData.getChildByPath("info/link");
	    if (link != null) {
		infoId = MapleDataTool.getIntConvert("info/link", reactorData);
		stats = reactorStats.get(Integer.valueOf(infoId));
	    }
	    if (stats == null) {
		reactorData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(infoId) + ".img", '0', 11));
		MapleData reactorInfoData = reactorData.getChildByPath("0/event/0");
		stats = new MapleReactorStats();

		if (reactorInfoData != null) {
		    boolean areaSet = false;
		    int i = 0;
		    while (reactorInfoData != null) {
			Pair<Integer, Integer> reactItem = null;
			int type = MapleDataTool.getIntConvert("type", reactorInfoData);
			if (type == 100) { //reactor waits for item
			    reactItem = new Pair<Integer, Integer>(MapleDataTool.getIntConvert("0", reactorInfoData), MapleDataTool.getIntConvert("1", reactorInfoData));
			    if (!areaSet) { //only set area of effect for item-triggered reactors once
				stats.setTL(MapleDataTool.getPoint("lt", reactorInfoData));
				stats.setBR(MapleDataTool.getPoint("rb", reactorInfoData));
				areaSet = true;
			    }
			}
			stats.addState((byte) i, type, reactItem, (byte) MapleDataTool.getIntConvert("state", reactorInfoData));
			i++;
			reactorInfoData = reactorData.getChildByPath(i + "/event/0");
		    }
		} else { //sit there and look pretty; likely a reactor such as Zakum/Papulatus doors that shows if player can enter
		    stats.addState((byte) 0, 999, null, (byte) 0);
		}
		reactorStats.put(Integer.valueOf(infoId), stats);
		if (rid != infoId) {
		    reactorStats.put(Integer.valueOf(rid), stats);
		}
	    } else { // stats exist at infoId but not rid; add to map
		reactorStats.put(Integer.valueOf(rid), stats);
	    }
	}
	return stats;
    }
}
