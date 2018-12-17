package client;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataFileEntry;
import provider.MapleDataProviderFactory;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataTool;
import tools.StringUtil;

public class SkillFactory {

	private static final Map<Integer, ISkill> skills = new HashMap<Integer, ISkill>();
	private static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap<Integer, SummonSkillEntry>();
	private final static MapleData stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Skill.img");

	public static final ISkill getSkill(final int id) {
		if (skills.size() != 0) {
			return skills.get(Integer.valueOf(id));
		}
		System.out.println(":: Loading SkillFactory ::");
		final MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Skill.wz"));
		final MapleDataDirectoryEntry root = datasource.getRoot();
		int skillid;
		MapleData summon_data;
		SummonSkillEntry sse;
		for (MapleDataFileEntry topDir : root.getFiles()) { // Loop thru jobs
			if (topDir.getName().length() <= 8) {
				for (MapleData data : datasource.getData(topDir.getName())) { // Loop thru each jobs
					if (data.getName().equals("skill")) {
						for (MapleData data2 : data) { // Loop thru each jobs
							if (data2 != null) {
								skillid = Integer.parseInt(data2.getName());
								skills.put(skillid, Skill.loadFromData(skillid, data2));
								summon_data = data2.getChildByPath("summon/attack1/info");
								if (summon_data != null) {
									sse = new SummonSkillEntry();
									sse.attackAfter = (short) MapleDataTool.getInt("attackAfter", summon_data, 999999);
									sse.type = (byte) MapleDataTool.getInt("type", summon_data, 0);
									sse.mobCount = (byte) MapleDataTool.getInt("mobCount", summon_data, 1);
									SummonSkillInformation.put(skillid, sse);
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static final String getSkillName(final int id) {
		String strId = Integer.toString(id);
		strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
		MapleData skillroot = stringData.getChildByPath(strId);
		if (skillroot != null) {
			return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
		}
		return null;
	}

	public static final SummonSkillEntry getSummonData(final int skillid) {
		return SummonSkillInformation.get(skillid);
	}
}