package handling.channel.handler;

import java.util.ArrayList;
import java.util.List;

import client.GameConstants;
import client.ISkill;
import client.MapleClient;
import client.MapleCharacter;
import client.MapleStat;
import client.PlayerStats;
import client.SkillFactory;
import server.AutobanManager;
import server.Randomizer;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class StatsHandling {

	public static final void DistributeAP(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
		//c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
		slea.skip(4);

		final PlayerStats stat = chr.getStat();

		if (chr.getRemainingAp() > 0) {
			switch (slea.readInt()) {
				case 64: // Str
					if (stat.getStr() >= c.getPlayer().getMaxStats()) {
						return;
					}
					stat.setStr(stat.getStr() + 1);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, stat.getStr()));
					break;
				case 128: // Dex
					if (stat.getDex() >= c.getPlayer().getMaxStats()) {
						return;
					}
					stat.setDex(stat.getDex() + 1);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, stat.getDex()));
					break;
				case 256: // Int
					if (stat.getInt() >= c.getPlayer().getMaxStats()) {
						return;
					}
					stat.setInt(stat.getInt() + 1);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, stat.getInt()));
					break;
				case 512: // Luk
					if (stat.getLuk() >= c.getPlayer().getMaxStats()) {
						return;
					}
					stat.setLuk(stat.getLuk() + 1);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, stat.getLuk()));
					break;
				case 2048: // HP
					int MaxHP = stat.getMaxHp();
					if (chr.getHpApUsed() >= 10000 || MaxHP >= 30000) {
						return;
					}
					ISkill improvingMaxHP = null;
					int improvingMaxHPLevel = 0;
					if (chr.getJob() == 0) { // Beginner
						MaxHP += Randomizer.rand(8, 12);
					} else if (chr.getJob() >= 100 && chr.getJob() <= 132) { // Warrior
						improvingMaxHP = SkillFactory.getSkill(1000001);
						improvingMaxHPLevel = chr.getSkillLevel(improvingMaxHP);
						MaxHP += Randomizer.rand(20, 24);
						if (improvingMaxHPLevel >= 1) {
							MaxHP += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
						}
					} else if (chr.getJob() >= 200 && chr.getJob() <= 232) { // Magician
						MaxHP += Randomizer.rand(6, 10);
					} else if (chr.getJob() >= 300 && chr.getJob() <= 322) { // Bowman
						MaxHP += Randomizer.rand(16, 20);
					} else if (chr.getJob() >= 400 && chr.getJob() <= 422) { // Thief
						MaxHP += Randomizer.rand(20, 24);
					} else if (chr.getJob() >= 500 && chr.getJob() <= 522) { // Pirate
						improvingMaxHP = SkillFactory.getSkill(5100000);
						improvingMaxHPLevel = chr.getSkillLevel(improvingMaxHP);
						MaxHP += Randomizer.rand(16, 20);
						if (improvingMaxHPLevel >= 1) {
							MaxHP += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
						}
					} else if (chr.getJob() >= 1100 && chr.getJob() <= 1111) { // Soul Master
						improvingMaxHP = SkillFactory.getSkill(11000000);
						improvingMaxHPLevel = chr.getSkillLevel(improvingMaxHP);
						MaxHP += Randomizer.rand(36, 42);
						if (improvingMaxHPLevel >= 1) {
							MaxHP += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
						}
					} else if (chr.getJob() >= 1200 && chr.getJob() <= 1211) { // Flame Wizard
						MaxHP += Randomizer.rand(15, 21);
					} else if ((chr.getJob() >= 1300 && chr.getJob() <= 1311) || (chr.getJob() >= 1400 && chr.getJob() <= 1411)) { // Wind Breaker and Night Walker
						MaxHP += Randomizer.rand(30, 36);
					} else { // GameMaster
						MaxHP += Randomizer.rand(50, 100);
					}
					MaxHP = Math.min(30000, MaxHP);
					chr.setHpApUsed(chr.getHpApUsed() + 1);
					stat.setMaxHp(MaxHP);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, MaxHP));
					break;
				case 8192: // MP
					int MaxMP = stat.getMaxMp();
					if (chr.getMpApUsed() >= 10000 && stat.getMaxMp() >= 30000) {
						return;
					}
					if (chr.getJob() == 0) { // Beginner
						MaxMP += Randomizer.rand(6, 8);
					} else if (chr.getJob() >= 100 && chr.getJob() <= 132) { // Warrior
						MaxMP += Randomizer.rand(2, 4);
					} else if (chr.getJob() >= 200 && chr.getJob() <= 232) { // Magician
						ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
						int improvingMaxMPLevel = chr.getSkillLevel(improvingMaxMP);
						if (improvingMaxMPLevel >= 1) {
							MaxMP += Randomizer.rand(18, 20) + improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
						} else {
							MaxMP += Randomizer.rand(18, 20);
						}
					} else if (chr.getJob() >= 300 && chr.getJob() <= 322) { // Bowman
						MaxMP += Randomizer.rand(10, 12);
					} else if (chr.getJob() >= 400 && chr.getJob() <= 422) { // Thief
						MaxMP += Randomizer.rand(10, 12);
					} else if (chr.getJob() >= 500 && chr.getJob() <= 522) { // Pirate
						MaxMP += Randomizer.rand(10, 12);
					} else if (chr.getJob() >= 1100 && chr.getJob() <= 1111) { // Soul Master
						MaxMP += Randomizer.rand(6, 9);
					} else if (chr.getJob() >= 1200 && chr.getJob() <= 1211) { // Flame Wizard
						ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
						int improvingMaxMPLevel = chr.getSkillLevel(improvingMaxMP);
						MaxMP += Randomizer.rand(33, 36);
						if (improvingMaxMPLevel >= 1) {
							MaxMP += improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
						}
					} else if ((chr.getJob() >= 1300 && chr.getJob() <= 1311) || (chr.getJob() >= 1400 && chr.getJob() <= 1411)) { // Wind Breaker and Night Walker
						MaxMP += Randomizer.rand(21, 24);
					} else { // GameMaster
						MaxMP += Randomizer.rand(50, 100);
					}
					MaxMP = Math.min(30000, MaxMP);
					chr.setMpApUsed(chr.getMpApUsed() + 1);
					stat.setMaxMp(MaxMP);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, MaxMP));
					break;
				default:
					c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
					return;
			}
			chr.setRemainingAp(chr.getRemainingAp() - 1);
			statupdate.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, chr.getRemainingAp()));
			c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
		}
	}

	public static final void DistributeSP(final int skillid, final MapleClient c, final MapleCharacter chr) {
		boolean isBeginnerSkill = false;
		int remainingSp = 0;

		switch (skillid) {
			case 1000:
			case 1001:
			case 1002: {
				final int snailsLevel = chr.getSkillLevel(SkillFactory.getSkill(1000));
				final int recoveryLevel = chr.getSkillLevel(SkillFactory.getSkill(1001));
				final int nimbleFeetLevel = chr.getSkillLevel(SkillFactory.getSkill(1002));
				remainingSp = Math.min((chr.getLevel() - 1), 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
				isBeginnerSkill = true;
				break;
			}
			case 10001000:
			case 10001001:
			case 10001002: {
				final int snailsLevel = chr.getSkillLevel(SkillFactory.getSkill(10001000));
				final int recoveryLevel = chr.getSkillLevel(SkillFactory.getSkill(10001001));
				final int nimbleFeetLevel = chr.getSkillLevel(SkillFactory.getSkill(10001002));
				remainingSp = Math.min((chr.getLevel() - 1), 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
				isBeginnerSkill = true;
				break;
			}
			case 20001000:
			case 20001001:
			case 20001002: {
				final int snailsLevel = chr.getSkillLevel(SkillFactory.getSkill(20001000));
				final int recoveryLevel = chr.getSkillLevel(SkillFactory.getSkill(20001001));
				final int nimbleFeetLevel = chr.getSkillLevel(SkillFactory.getSkill(20001002));
				remainingSp = Math.min((chr.getLevel() - 1), 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
				isBeginnerSkill = true;
				break;
			}
			case 20011000:
			case 20011001:
			case 20011002: {
				final int snailsLevel = chr.getSkillLevel(SkillFactory.getSkill(20011000));
				final int recoveryLevel = chr.getSkillLevel(SkillFactory.getSkill(20011001));
				final int nimbleFeetLevel = chr.getSkillLevel(SkillFactory.getSkill(20011002));
				remainingSp = Math.min((chr.getLevel() - 1), 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
				isBeginnerSkill = true;
				break;
			}
			case 30001000:
			case 30001001:
			case 30001002: {
				final int snailsLevel = chr.getSkillLevel(SkillFactory.getSkill(30001000));
				final int recoveryLevel = chr.getSkillLevel(SkillFactory.getSkill(30001002));
				final int nimbleFeetLevel = chr.getSkillLevel(SkillFactory.getSkill(30001001));
				remainingSp = Math.min((chr.getLevel() - 1), 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
				isBeginnerSkill = true;
				break;
			}
			default: {
				remainingSp = chr.getRemainingSp(GameConstants.getSkillBookForSkill(skillid));
				break;
			}
		}
		final ISkill skill = SkillFactory.getSkill(skillid);

		if (skill.hasRequiredSkill()) {
			if (chr.getSkillLevel(SkillFactory.getSkill(skill.getRequiredSkillId())) < skill.getRequiredSkillLevel()) {
				AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill without the required skill (" + skillid + ")");
				return;
			}
		}
		final int maxlevel = skill.isFourthJob() ? chr.getMasterLevel(skill) : skill.getMaxLevel();
		final int curLevel = chr.getSkillLevel(skill);

		if (skill.isInvisible() && chr.getSkillLevel(skill) == 0) {
			if ((skill.isFourthJob() && chr.getMasterLevel(skill) == 0) || !skill.isFourthJob() && maxlevel < 10) {
				AutobanManager.getInstance().addPoints(c, 1000, 0, "Illegal distribution of SP to invisible skills (" + skillid + ")");
				return;
			}
		}

		if ((remainingSp > 0 && curLevel + 1 <= maxlevel) && skill.canBeLearnedBy(chr.getJob())) {
			if (!isBeginnerSkill) {
				final int skillbook = GameConstants.getSkillBookForSkill(skillid);
				chr.setRemainingSp(chr.getRemainingSp(skillbook) - 1, skillbook);
			}
			chr.updateSingleStat(MapleStat.AVAILABLESP, chr.getRemainingSp());
			chr.changeSkillLevel(skill, (byte) (curLevel + 1), chr.getMasterLevel(skill));
		} else if (!skill.canBeLearnedBy(chr.getJob())) {
			AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill for a different job (" + skillid + ")");
		}
	}

	public static final void AutoAssignAP(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		slea.skip(8);
		final int PrimaryStat = slea.readInt();
		final int amount = slea.readInt();
		final int SecondaryStat = slea.readInt();
		final int amount2 = slea.readInt();
		final PlayerStats playerst = chr.getStat();
		List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
		//c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
		if (chr.getRemainingAp() == amount + amount2) {
			switch (PrimaryStat) {
				case 64: // Str
					if (playerst.getStr() + amount > 999) {
						return;
					}
					playerst.setStr(playerst.getStr() + amount);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, playerst.getStr()));
					break;
				case 128: // Dex
					if (playerst.getDex() + amount > 999) {
						return;
					}
					playerst.setDex(playerst.getDex() + amount);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, playerst.getDex()));
					break;
				case 256: // Int
					if (playerst.getInt() + amount > 999) {
						return;
					}
					playerst.setInt(playerst.getInt() + amount);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, playerst.getInt()));
					break;
				case 512: // Luk
					if (playerst.getLuk() + amount > 999) {
						return;
					}
					playerst.setLuk(playerst.getLuk() + amount);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, playerst.getLuk()));
					break;
				default:
					c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
					return;
			}
			switch (SecondaryStat) {
				case 64: // Str
					if (playerst.getStr() + amount2 > 999) {
						return;
					}
					playerst.setStr(playerst.getStr() + amount2);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, playerst.getStr()));
					break;
				case 128: // Dex
					if (playerst.getDex() + amount2 > 999) {
						return;
					}
					playerst.setDex(playerst.getDex() + amount2);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, playerst.getDex()));
					break;
				case 256: // Int
					if (playerst.getInt() + amount2 > 999) {
						return;
					}
					playerst.setInt(playerst.getInt() + amount2);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, playerst.getInt()));
					break;
				case 512: // Luk
					if (playerst.getLuk() + amount2 > 999) {
						return;
					}
					playerst.setLuk(playerst.getLuk() + amount2);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, playerst.getLuk()));
					break;
				default:
					c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
					return;
			}
			chr.setRemainingAp(chr.getRemainingAp() - (amount + amount2));
			statupdate.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, chr.getRemainingAp()));
			c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
		}
	}
}