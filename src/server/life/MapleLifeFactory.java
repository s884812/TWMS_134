package server.life;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import database.DatabaseConnection;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.WzXML.MapleDataType;
import tools.Pair;
import tools.StringUtil;

public class MapleLifeFactory {

	private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Mob.wz"));
	private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
	private static final MapleDataProvider etcDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
	private static final MapleData mobStringData = stringDataWZ.getData("Mob.img");
	private static final MapleData npcStringData = stringDataWZ.getData("Npc.img");
	private static final MapleData npclocData = etcDataWZ.getData("NpcLocation.img");
	private static Map<Integer, MapleMonsterStats> monsterStats = new HashMap<Integer, MapleMonsterStats>();
	private static Map<Integer, Integer> NPCLoc = new HashMap<Integer, Integer>();

	public static AbstractLoadedMapleLife getLife(int id, String type) {
	if (type.equalsIgnoreCase("n")) {
		return getNPC(id);
	} else if (type.equalsIgnoreCase("m")) {
		return getMonster(id);
	} else {
		System.err.println("Unknown Life type: " + type + "");
		return null;
	}
	}

	public static int getNPCLocation(int npcid) {
	if (NPCLoc.containsKey(npcid)) {
		return NPCLoc.get(npcid);
	}
	final int map = MapleDataTool.getIntConvert(Integer.toString(npcid) + "/0", npclocData);
	NPCLoc.put(npcid, map);
	return map;
	}

	public static MapleMonster getMonster(int mid) {
	MapleMonsterStats stats = monsterStats.get(Integer.valueOf(mid));

	if (stats == null) {
		MapleData monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
		if (monsterData == null) {
		return null;
		}
		MapleData monsterInfoData = monsterData.getChildByPath("info");
		stats = new MapleMonsterStats();

		stats.setHp(MapleDataTool.getIntConvert("maxHP", monsterInfoData));
		stats.setMp(MapleDataTool.getIntConvert("maxMP", monsterInfoData, 0));
		stats.setExp(MapleDataTool.getIntConvert("exp", monsterInfoData, 0));
		stats.setLevel((short) MapleDataTool.getIntConvert("level", monsterInfoData));
		stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", monsterInfoData, 0));
		stats.setrareItemDropLevel((byte) MapleDataTool.getIntConvert("rareItemDropLevel", monsterInfoData, 0));
		stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));
		stats.setOnlyNormalAttack(MapleDataTool.getIntConvert("onlyNormalAttack", monsterInfoData, 0) > 0);
		stats.setBoss(MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0 || mid == 8810018 || mid == 9410066);
		stats.setExplosiveReward(MapleDataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0);
		stats.setFfaLoot(MapleDataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0);
		stats.setUndead(MapleDataTool.getIntConvert("undead", monsterInfoData, 0) > 0);
		stats.setName(MapleDataTool.getString(mid + "/name", mobStringData, "MISSINGNO"));
		stats.setBuffToGive(MapleDataTool.getIntConvert("buff", monsterInfoData, -1));
		stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) > 0);
		stats.setCP((byte) MapleDataTool.getIntConvert("getCP", monsterInfoData, 0));
		stats.setPhysicalDefense((short) MapleDataTool.getIntConvert("PDDamage", monsterInfoData, 0));
		stats.setMagicDefense((short) MapleDataTool.getIntConvert("MDDamage", monsterInfoData, 0));
		stats.setEva((short) MapleDataTool.getIntConvert("eva", monsterInfoData, 0));

		final MapleData selfd = monsterInfoData.getChildByPath("selfDestruction");
		if (selfd != null) {
		stats.setSelfDHP(MapleDataTool.getIntConvert("hp", selfd, 0));
		stats.setSelfD((byte) MapleDataTool.getIntConvert("action", selfd, -1));
		} else {
		stats.setSelfD((byte) -1);
		}

		final MapleData firstAttackData = monsterInfoData.getChildByPath("firstAttack");
		if (firstAttackData != null) {
		if (firstAttackData.getType() == MapleDataType.FLOAT) {
			stats.setFirstAttack(Math.round(MapleDataTool.getFloat(firstAttackData)) > 0);
		} else {
			stats.setFirstAttack(MapleDataTool.getInt(firstAttackData) > 0);
		}
		}
		if (stats.isBoss() || isDmgSponge(mid)) {
		if (monsterInfoData.getChildByPath("hpTagColor") == null || monsterInfoData.getChildByPath("hpTagBgcolor") == null) {
			stats.setTagColor(0);
			stats.setTagBgColor(0);
		} else {
			stats.setTagColor(MapleDataTool.getIntConvert("hpTagColor", monsterInfoData));
			stats.setTagBgColor(MapleDataTool.getIntConvert("hpTagBgcolor", monsterInfoData));
		}
		}

		final MapleData banishData = monsterInfoData.getChildByPath("ban");
		if (banishData != null) {
		stats.setBanishInfo(new BanishInfo(
			MapleDataTool.getString("banMsg", banishData),
			MapleDataTool.getInt("banMap/0/field", banishData, -1),
			MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
		}

		final MapleData reviveInfo = monsterInfoData.getChildByPath("revive");
		if (reviveInfo != null) {
		List<Integer> revives = new LinkedList<Integer>();
		for (MapleData bdata : reviveInfo) {
			revives.add(MapleDataTool.getInt(bdata));
		}
		stats.setRevives(revives);
		}

		final MapleData monsterSkillData = monsterInfoData.getChildByPath("skill");
		if (monsterSkillData != null) {
		int i = 0;
		List<Pair<Integer, Integer>> skills = new ArrayList<Pair<Integer, Integer>>();
		while (monsterSkillData.getChildByPath(Integer.toString(i)) != null) {
			skills.add(new Pair<Integer, Integer>(Integer.valueOf(MapleDataTool.getInt(i + "/skill", monsterSkillData, 0)), Integer.valueOf(MapleDataTool.getInt(i + "/level", monsterSkillData, 0))));
			i++;
		}
		stats.setSkills(skills);
		}

		decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));

		// Other data which isn;t in the mob, but might in the linked data

		final int link = MapleDataTool.getIntConvert("link", monsterInfoData, 0);
		if (link != 0) { // Store another copy, for faster processing.
		monsterData = data.getData(StringUtil.getLeftPaddedStr(link + ".img", '0', 11));
		monsterInfoData = monsterData.getChildByPath("info");
		}

		for (MapleData idata : monsterData) {
		if (idata.getName().equals("fly")) {
			stats.setFly(true);
			stats.setMobile(true);
			break;
		} else if (idata.getName().equals("move")) {
			stats.setMobile(true);
		}
		}
		
		byte hpdisplaytype = -1;
		if (stats.getTagColor() > 0) {
		hpdisplaytype = 0;
		} else if (stats.isFriendly()) {
		hpdisplaytype = 1;
		} else if (mid >= 9300184 && mid <= 9300215) { // Mulung TC mobs
		hpdisplaytype = 2;
		} else if (!stats.isBoss() || mid == 9410066) { // Not boss and dong dong chiang
		hpdisplaytype = 3;
		}
		stats.setHPDisplayType(hpdisplaytype);

		monsterStats.put(Integer.valueOf(mid), stats);
	}
	return new MapleMonster(mid, stats);
	}

	public static final void decodeElementalString(MapleMonsterStats stats, String elemAttr) {
	for (int i = 0; i < elemAttr.length(); i += 2) {
		stats.setEffectiveness(
			Element.getFromChar(elemAttr.charAt(i)),
			ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
	}
	}

	private static final boolean isDmgSponge(final int mid) {
	switch (mid) {
		case 8810018:
		case 8820010:
		case 8820011:
		case 8820012:
		case 8820013:
		case 8820014:
		return true;
	}
	return false;
	}

	public static MapleNPC getNPC(final int nid) {
	if (nid >= 9901000 && nid <= 9901551) {
		final MapleNPCStats stats = new MapleNPCStats("", true);
		final MapleNPC npc = new MapleNPC(nid, stats);

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
		Connection con = DatabaseConnection.getConnection();
		ps = con.prepareStatement("SELECT * FROM playernpcs WHERE npcid = ?");
		ps.setInt(1, nid);

		rs = ps.executeQuery();
		if (rs.next()) {
			stats.setCY(rs.getInt("cy"));
			stats.setName(rs.getString("name"));
			stats.setHair(rs.getInt("hair"));
			stats.setFace(rs.getInt("face"));
			stats.setSkin(rs.getByte("skin"));
			stats.setFH(rs.getInt("Foothold"));
			stats.setRX0(rs.getInt("rx0"));
			stats.setRX1(rs.getInt("rx1"));
			npc.setPosition(new Point(rs.getInt("x"), stats.getCY()));
			ps.close();
			rs.close();

			ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE npcid = ?");
			ps.setInt(1, rs.getInt("id"));
			rs = ps.executeQuery();

			Map<Byte, Integer> equips = new HashMap<Byte, Integer>();

			while (rs.next()) {
			equips.put(rs.getByte("equippos"), rs.getInt("equipid"));
			}
			stats.setEquips(equips);
			rs.close();
			ps.close();
		}
		} catch (SQLException ex) {
		} finally {
		try {
			if (ps != null) {
			ps.close();
			}
			if (rs != null) {
			rs.close();
			}
		} catch (SQLException ignore) {
		}
		}
		return npc;
	} else {
		final String name = MapleDataTool.getString(nid + "/name", npcStringData, "MISSINGNO");
		return new MapleNPC(nid, new MapleNPCStats(name, false));
	}
	}
}
