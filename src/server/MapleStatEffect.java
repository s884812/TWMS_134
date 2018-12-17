package server;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import client.IItem;
import client.ISkill;
import client.GameConstants;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleDisease;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleStat;
import client.SkillFactory;
import client.PlayerStats;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.MapleMonster;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import handling.world.PlayerCoolDownValueHolder;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleStatEffect implements Serializable {

	private static final long serialVersionUID = 9179541993413738569L;
	private byte mastery, mhpR, mmpR, mobCount, attackCount, bulletCount;
	private short hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, mpCon, hpCon, damage, prop;
	private double hpR, mpR;
	private int duration, sourceid, moveTo, x, y, z, itemCon, itemConNo, bulletConsume, moneyCon, cooldown, morphId = 0, expinc;
	private boolean overTime, skill;
	private List<Pair<MapleBuffStat, Integer>> statups;
	private Map<MonsterStatus, Integer> monsterStatus;
	private Point lt, rb;
	//private List<Pair<Integer, Integer>> randomMorph;
	private List<MapleDisease> cureDebuffs;

	public static final MapleStatEffect loadSkillEffectFromData(final MapleData source, final int skillid, final boolean overtime) {
		return loadFromData(source, skillid, true, overtime);
	}

	public static final MapleStatEffect loadItemEffectFromData(final MapleData source, final int itemid) {
		return loadFromData(source, itemid, false, false);
	}

	private static final void addBuffStatPairToListIfNotZero(final List<Pair<MapleBuffStat, Integer>> list, final MapleBuffStat buffstat, final Integer val) {
		if (val.intValue() != 0) {
			list.add(new Pair<MapleBuffStat, Integer>(buffstat, val));
		}
	}

	private static MapleStatEffect loadFromData(final MapleData source, final int sourceid, final boolean skill, final boolean overTime) {
		final MapleStatEffect ret = new MapleStatEffect();
		final MapleData oldSchool = source.getChildByPath("level");
		if (oldSchool != null) {
			ret.duration = MapleDataTool.getIntConvert("time", source, -1);
			ret.hp = (short) MapleDataTool.getInt("hp", source, 0);
			ret.hpR = MapleDataTool.getInt("hpR", source, 0) / 100.0;
			ret.mp = (short) MapleDataTool.getInt("mp", source, 0);
			ret.mpR = MapleDataTool.getInt("mpR", source, 0) / 100.0;
			ret.mhpR = (byte) MapleDataTool.getInt("mhpR", source, 0);
			ret.mmpR = (byte) MapleDataTool.getInt("mmpR", source, 0);
			ret.mpCon = (short) MapleDataTool.getInt("mpCon", source, 0);
			ret.hpCon = (short) MapleDataTool.getInt("hpCon", source, 0);
			ret.prop = (short) MapleDataTool.getInt("prop", source, 100);
			ret.cooldown = MapleDataTool.getInt("cooltime", source, 0);
			ret.expinc = MapleDataTool.getInt("expinc", source, 0);
			ret.morphId = MapleDataTool.getInt("morph", source, 0);
			ret.mobCount = (byte) MapleDataTool.getInt("mobCount", source, 1);

			if (skill) {
				switch (sourceid) {
					case 1100002:
					case 1100003:
					case 1200002:
					case 1200003:
					case 1300002:
					case 1300003:
					case 3100001:
					case 3200001:
					case 11101002:
					case 13101002:
						ret.mobCount = 6;
						break;
				}
			}

			/*	final MapleData randMorph = source.getChildByPath("morphRandom");
			if (randMorph != null) {
			for (MapleData data : randMorph.getChildren()) {
			ret.randomMorph.add(new Pair(
			MapleDataTool.getInt("morph", data, 0),
			MapleDataTool.getIntConvert("prop", data, 0)));
			}
			}*/

			ret.sourceid = sourceid;
			ret.skill = skill;

			if (!ret.skill && ret.duration > -1) {
				ret.overTime = true;
			} else {
				ret.duration *= 1000; // items have their times stored in ms, of course
				ret.overTime = overTime;
			}
			final ArrayList<Pair<MapleBuffStat, Integer>> statups = new ArrayList<Pair<MapleBuffStat, Integer>>();

			ret.mastery = (byte) MapleDataTool.getInt("mastery", source, 0);
			ret.watk = (short) MapleDataTool.getInt("pad", source, 0);
			ret.wdef = (short) MapleDataTool.getInt("pdd", source, 0);
			ret.matk = (short) MapleDataTool.getInt("mad", source, 0);
			ret.mdef = (short) MapleDataTool.getInt("mdd", source, 0);
			ret.acc = (short) MapleDataTool.getIntConvert("acc", source, 0);
			ret.avoid = (short) MapleDataTool.getInt("eva", source, 0);
			ret.speed = (short) MapleDataTool.getInt("speed", source, 0);
			ret.jump = (short) MapleDataTool.getInt("jump", source, 0);

			List<MapleDisease> cure = new ArrayList<MapleDisease>(5);
			if (MapleDataTool.getInt("poison", source, 0) > 0) {
				cure.add(MapleDisease.POISON);
			}
			if (MapleDataTool.getInt("seal", source, 0) > 0) {
				cure.add(MapleDisease.SEAL);
			}
			if (MapleDataTool.getInt("darkness", source, 0) > 0) {
				cure.add(MapleDisease.DARKNESS);
			}
			if (MapleDataTool.getInt("weakness", source, 0) > 0) {
				cure.add(MapleDisease.WEAKEN);
			}
			if (MapleDataTool.getInt("curse", source, 0) > 0) {
				cure.add(MapleDisease.CURSE);
			}
			ret.cureDebuffs = cure;

			if (ret.overTime && ret.getSummonMovementType() == null) {
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAXHP, (int) ret.mhpR);
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAXMP, (int) ret.mmpR);
				//addBuffStatPairToListIfNotZero(statups, MapleBuffStat.EXPRATE, Integer.valueOf(2)); // EXP
			}

			final MapleData ltd = source.getChildByPath("lt");
			if (ltd != null) {
				ret.lt = (Point) ltd.getData();
				ret.rb = (Point) source.getChildByPath("rb").getData();
			}

			ret.x = MapleDataTool.getInt("x", source, 0);
			ret.y = MapleDataTool.getInt("y", source, 0);
			ret.z = MapleDataTool.getInt("z", source, 0);
			ret.damage = (short) MapleDataTool.getIntConvert("damage", source, 100);
			ret.attackCount = (byte) MapleDataTool.getIntConvert("attackCount", source, 1);
			ret.bulletCount = (byte) MapleDataTool.getIntConvert("bulletCount", source, 1);
			ret.bulletConsume = MapleDataTool.getIntConvert("bulletConsume", source, 0);
			ret.moneyCon = MapleDataTool.getIntConvert("moneyCon", source, 0);
			ret.itemCon = MapleDataTool.getInt("itemCon", source, 0);
			ret.itemConNo = MapleDataTool.getInt("itemConNo", source, 0);
			ret.moveTo = MapleDataTool.getInt("moveTo", source, -1);

			Map<MonsterStatus, Integer> monsterStatus = new ArrayMap<MonsterStatus, Integer>();

			if (skill) { // hack because we can't get from the datafile...
				switch (sourceid) {
					case 2001002: // magic guard
					case 12001001:
					case 22111001:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_GUARD, ret.x));
						break;
					case 2301003: // invincible
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INVINCIBLE, ret.x));
						break;
					case 9001004: // hide
						ret.duration = 60 * 120 * 1000;
						ret.overTime = true;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, ret.x));
						break;
					case 13101006: // Wind Walk
					case 4001003: // darksight
					case 14001003: // cygnus ds
					case 4330001:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, ret.x));
						break;
					case 4211003: // pickpocket
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PICKPOCKET, ret.x));
						break;
					case 4211005: // mesoguard
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOGUARD, ret.x));
						break;
					case 4111001: // mesoup
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOUP, ret.x));
						break;
					case 4111002: // shadowpartner
					case 14111000: // cygnus
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, ret.x));
						break;
					case 11101002: // All Final attack
					case 13101002:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINALATTACK, 1));
						break;
					case 3101004: // soul arrow
					case 3201004:
					case 2311002: // mystic door - hacked buff icon
					case 13101003:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, ret.x));
						break;
					case 1211006: // wk charges
					case 1211003:
					case 1211004:
					case 1211005:
					case 1211008:
					case 1211007:
					case 1221003:
					case 1221004:
					case 11111007:
					case 15101006:
					case 15111006: // Spark
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPARK, ret.x));
						break;
					case 21111005:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WK_CHARGE, ret.x));
						break;
					case 12101005:
					case 22121001: // Elemental Reset
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ELEMENT_RESET, ret.x));
						break;
					case 5110001: // Energy Charge
					case 15100004:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, 1));
						break;
					case 1101005: // booster
					case 1101004:
					case 1201005:
					case 1201004:
					case 1301005:
					case 1301004:
					case 3101002:
					case 3201002:
					case 4101003:
					case 4201002:
					case 2111005: // spell booster, do these work the same?
					case 2211005:
					case 5101006:
					case 5201003:
					case 11101001:
					case 12101004:
					case 13101001:
					case 14101002:
					case 15101002:
					case 21001003: // Aran - Pole Arm Booster
					case 22141002: // Magic Booster
					case 4301002:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BOOSTER, ret.x));
						break;
					//case 5121009:
					//case 15111005:
					//    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPEED_INFUSION, ret.x));
					//    break;
					case 4321000: //tornado spin uses same buffstats
						ret.duration = 1000;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_SPEED, 100 + ret.x));
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_JUMP, ret.y)); //always 0 but its there
						break;
					case 5001005: // Dash
					case 15001003:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_SPEED, ret.x));
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_JUMP, ret.y));
						break;
					case 1101007: // pguard
					case 1201007:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.POWERGUARD, ret.x));
						break;
					case 1301007: // hyper body
					case 9001008:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAXHP, ret.x));
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAXMP, ret.y));
						break;
					case 1001: // recovery
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.RECOVERY, ret.x));
						break;
					case 1111002: // combo
					case 11111001: // combo
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
						break;
					case 5211006: // Homing Beacon
					case 5220011: // Bullseye
					case 22151002: //killer wings

						ret.duration = 60 * 120000;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOMING_BEACON, ret.x));
						break;
					case 1011: // Berserk fury
					case 10001011:
					case 20001011:
					case 20011011:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BERSERK_FURY, 1));
						break;
					case 1010:
					case 10001010:// Invincible Barrier
					case 20001010:
					case 20011010:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DIVINE_BODY, 1));
						break;
					case 1311006: //dragon roar
						ret.hpR = -ret.x / 100.0;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGON_ROAR, ret.y));
						break;
					case 1311008: // dragon blood
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGONBLOOD, ret.x));
						break;
					case 4341007:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.THORNS, ret.x << 8 | ret.y));
						break;
					case 4341002:
						ret.duration = 60 * 1000;
						ret.overTime = true;
						ret.hpR = -ret.x / 100.0;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINAL_CUT, ret.y));
						break;
					case 4331002:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_IMAGE, ret.x));
						break;
					case 4331003:
						ret.duration = 60 * 1000;
						ret.overTime = true;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.OWL_SPIRIT, ret.y));
						break;
					case 1121000: // maple warrior, all classes
					case 1221000:
					case 1321000:
					case 2121000:
					case 2221000:
					case 2321000:
					case 3121000:
					case 3221000:
					case 4121000:
					case 4221000:
					case 5121000:
					case 5221000:
					case 21121000: // Aran - Maple Warrior
					case 22171000:
					case 4341000:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAPLE_WARRIOR, ret.x));
						break;
					case 3121002: // sharp eyes bow master
					case 3221002: // sharp eyes marksmen
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHARP_EYES, ret.x << 8 | ret.y));
						break;
					case 21101003: // Body Pressure
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BODY_PRESSURE, ret.x));
						break;
					case 21000000: // Aran Combo
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARAN_COMBO, 100));
						break;
					case 21100005: // Combo Drain
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO_DRAIN, ret.x));
						break;
					case 21111001: // Smart Knockback
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SMART_KNOCKBACK, ret.x));
						break;
					case 4001002: // disorder
					case 14001002: // cygnus disorder
						monsterStatus.put(MonsterStatus.WATK, ret.x);
						monsterStatus.put(MonsterStatus.WDEF, ret.y);
						break;
					case 5221009: // Mind Control
						monsterStatus.put(MonsterStatus.HYPNOTIZE, 1);
						break;
					case 1201006: // threaten
						monsterStatus.put(MonsterStatus.WATK, ret.x);
						monsterStatus.put(MonsterStatus.WDEF, ret.y);
						break;
					case 1211002: // charged blow
					case 1111008: // shout
					case 4211002: // assaulter
					case 3101005: // arrow bomb
					case 1111005: // coma: sword
					case 1111006: // coma: axe
					case 4221007: // boomerang step
					case 5101002: // Backspin Blow
					case 5101003: // Double Uppercut
					case 5121004: // Demolition
					case 5121005: // Snatch
					case 5121007: // Barrage
					case 5201004: // pirate blank shot
					case 4121008: // Ninja Storm
					case 22151001:
					case 4201004: //steal, new
						monsterStatus.put(MonsterStatus.STUN, 1);
						break;
					case 4321002:
						monsterStatus.put(MonsterStatus.DARKNESS, 1);
						break;
					case 4221003:
					case 4121003:
						monsterStatus.put(MonsterStatus.SHOWDOWN, ret.x);
						monsterStatus.put(MonsterStatus.MDEF, ret.x);
						monsterStatus.put(MonsterStatus.WDEF, ret.x);
						break;
					case 2201004: // cold beam
					case 2211002: // ice strike
					case 3211003: // blizzard
					case 2211006: // il elemental compo
					case 2221007: // Blizzard
					case 5211005: // Ice Splitter
					case 2121006: // Paralyze
					case 21120006: // Tempest
					case 22121000:
						monsterStatus.put(MonsterStatus.FREEZE, 1);
						ret.duration *= 2; // freezing skills are a little strange
						break;
					case 2101003: // fp slow
					case 2201003: // il slow
					case 12101001:
					case 22141003: // Slow
						monsterStatus.put(MonsterStatus.SPEED, ret.x);
						break;
					case 2101005: // poison breath
					case 2111006: // fp elemental compo
					case 2121003: // ice demon
					case 2221003: // fire demon
					case 3111003: //inferno, new
					case 22161002: //phantom imprint
						monsterStatus.put(MonsterStatus.POISON, 1);
						break;
					case 4121004: // Ninja ambush
					case 4221004:
						monsterStatus.put(MonsterStatus.NINJA_AMBUSH, (int) ret.damage);
						break;
					case 2311005:
						monsterStatus.put(MonsterStatus.DOOM, 1);
						break;
					/*case 4341006:
					statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_TARGET, 1));
					break;*/
					case 3111002: // puppet ranger
					case 3211002: // puppet sniper
					case 13111004: // puppet cygnus
					case 5211001: // Pirate octopus summon
					case 5220002: // wrath of the octopi
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PUPPET, 1));
						break;
					case 3211005: // golden eagle
					case 3111005: // golden hawk
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
						monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
						break;
					case 3221005: // frostprey
					case 2121005: // elquines
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
						monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
						break;
					case 2311006: // summon dragon
					case 3121006: // phoenix
					case 2221005: // ifrit
					case 2321003: // bahamut
					case 1321007: // Beholder
					case 5211002: // Pirate bird summon
					case 11001004:
					case 12001004:
					case 12111004: // Itrit
					case 13001004:
					case 14001005:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
						break;
					case 2311003: // hs
					case 9001002: // GM hs
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SYMBOL, ret.x));
						break;
					case 2211004: // il seal
					case 2111004: // fp seal
					case 12111002: // cygnus seal
						monsterStatus.put(MonsterStatus.SEAL, 1);
						break;
					case 4111003: // shadow web
					case 14111001:
						monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
						break;
					case 4121006: // spirit claw
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPIRIT_CLAW, 0));
						break;
					case 2121004:
					case 2221004:
					case 2321004: // Infinity
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INFINITY, ret.x));
						break;
					case 1121002:
					case 1221002:
					case 1321002: // Stance
					case 21121003: // Aran - Freezing Posture
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.STANCE, (int) ret.prop));
						break;
					case 1005: // Echo of Hero
					case 10001005: // Cygnus Echo
					case 20001005: // Aran
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ECHO_OF_HERO, ret.x));
						break;
					case 1026: // Soaring
					case 10001026: // Soaring
					case 20001026: // Soaring
					case 20011026: // Soaring
						ret.duration = 60 * 120 * 1000; //because it seems to dispel asap.

						ret.overTime = true;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOARING, 1));
						break;
					case 2121002: // mana reflection
					case 2221002:
					case 2321002:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MANA_REFLECTION, 1));
						break;
					case 2321005: // holy shield
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SHIELD, ret.x));
						break;
					case 3121007: // Hamstring
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HAMSTRING, ret.x));
						monsterStatus.put(MonsterStatus.SPEED, ret.x);
						break;
					case 3221006: // Blind
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLIND, ret.x));
						monsterStatus.put(MonsterStatus.ACC, ret.x);
						break;
					default:
						break;
				}
			}
			if (ret.morphId > 0 || ret.isPirateMorph()) {
				statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, ret.getMorph()));
			}
			if (ret.isMonsterRiding()) {
				statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 1));
			}
			ret.monsterStatus = monsterStatus;
			statups.trimToSize();
			ret.statups = statups;
		} else {
			ret.duration = MapleDataTool.getIntConvert("common/time", source, -1);
			ret.hp = (short) MapleDataTool.getInt("common/hp", source, 0);
			ret.hpR = MapleDataTool.getInt("common/hpR", source, 0) / 100.0;
			ret.mp = (short) MapleDataTool.getInt("common/mp", source, 0);
			ret.mpR = MapleDataTool.getInt("common/mpR", source, 0) / 100.0;
			ret.mhpR = (byte) MapleDataTool.getInt("common/mhpR", source, 0);
			ret.mmpR = (byte) MapleDataTool.getInt("common/mmpR", source, 0);
			ret.mpCon = (short) MapleDataTool.getInt("common/mpCon", source, 0);
			ret.hpCon = (short) MapleDataTool.getInt("common/hpCon", source, 0);
			ret.prop = (short) MapleDataTool.getInt("common/prop", source, 100);
			ret.cooldown = MapleDataTool.getInt("common/cooltime", source, 0);
			ret.expinc = MapleDataTool.getInt("common/expinc", source, 0);
			ret.morphId = MapleDataTool.getInt("common/morph", source, 0);
			ret.mobCount = (byte) MapleDataTool.getInt("common/mobCount", source, 1);

			if (skill) {
				switch (sourceid) {
					case 1100002:
					case 1100003:
					case 1200002:
					case 1200003:
					case 1300002:
					case 1300003:
					case 3100001:
					case 3200001:
					case 11101002:
					case 13101002:
						ret.mobCount = 6;
						break;
				}
			}

			/*	final MapleData randMorph = source.getChildByPath("morphRandom");
			if (randMorph != null) {
			for (MapleData data : randMorph.getChildren()) {
			ret.randomMorph.add(new Pair(
			MapleDataTool.getInt("common/morph", data, 0),
			MapleDataTool.getIntConvert("common/prop", data, 0)));
			}
			}*/

			ret.sourceid = sourceid;
			ret.skill = skill;

			if (!ret.skill && ret.duration > -1) {
				ret.overTime = true;
			} else {
				ret.duration *= 1000; // items have their times stored in ms, of course
				ret.overTime = overTime;
			}
			final ArrayList<Pair<MapleBuffStat, Integer>> statups = new ArrayList<Pair<MapleBuffStat, Integer>>();

			ret.mastery = (byte) MapleDataTool.getInt("common/mastery", source, 0);
			ret.watk = (short) MapleDataTool.getInt("common/pad", source, 0);
			ret.wdef = (short) MapleDataTool.getInt("common/pdd", source, 0);
			ret.matk = (short) MapleDataTool.getInt("common/mad", source, 0);
			ret.mdef = (short) MapleDataTool.getInt("common/mdd", source, 0);
			ret.acc = (short) MapleDataTool.getIntConvert("common/acc", source, 0);
			ret.avoid = (short) MapleDataTool.getInt("common/eva", source, 0);
			ret.speed = (short) MapleDataTool.getInt("common/speed", source, 0);
			ret.jump = (short) MapleDataTool.getInt("common/jump", source, 0);

			List<MapleDisease> cure = new ArrayList<MapleDisease>(5);
			if (MapleDataTool.getInt("common/poison", source, 0) > 0) {
				cure.add(MapleDisease.POISON);
			}
			if (MapleDataTool.getInt("common/seal", source, 0) > 0) {
				cure.add(MapleDisease.SEAL);
			}
			if (MapleDataTool.getInt("common/darkness", source, 0) > 0) {
				cure.add(MapleDisease.DARKNESS);
			}
			if (MapleDataTool.getInt("common/weakness", source, 0) > 0) {
				cure.add(MapleDisease.WEAKEN);
			}
			if (MapleDataTool.getInt("common/curse", source, 0) > 0) {
				cure.add(MapleDisease.CURSE);
			}
			ret.cureDebuffs = cure;

			if (ret.overTime && ret.getSummonMovementType() == null) {
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAXHP, (int) ret.mhpR);
				addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAXMP, (int) ret.mmpR);
				//addBuffStatPairToListIfNotZero(statups, MapleBuffStat.EXPRATE, Integer.valueOf(2)); // EXP
			}

			final MapleData ltd = source.getChildByPath("lt");
			if (ltd != null) {
				ret.lt = (Point) ltd.getData();
				ret.rb = (Point) source.getChildByPath("rb").getData();
			}

			ret.x = MapleDataTool.getInt("common/x", source, 0);
			ret.y = MapleDataTool.getInt("common/y", source, 0);
			ret.z = MapleDataTool.getInt("common/z", source, 0);
			ret.damage = (short) MapleDataTool.getIntConvert("common/damage", source, 100);
			ret.attackCount = (byte) MapleDataTool.getIntConvert("common/attackCount", source, 1);
			ret.bulletCount = (byte) MapleDataTool.getIntConvert("common/bulletCount", source, 1);
			ret.bulletConsume = MapleDataTool.getIntConvert("common/bulletConsume", source, 0);
			ret.moneyCon = MapleDataTool.getIntConvert("common/moneyCon", source, 0);
			ret.itemCon = MapleDataTool.getInt("common/itemCon", source, 0);
			ret.itemConNo = MapleDataTool.getInt("common/itemConNo", source, 0);
			ret.moveTo = MapleDataTool.getInt("common/moveTo", source, -1);

			Map<MonsterStatus, Integer> monsterStatus = new ArrayMap<MonsterStatus, Integer>();

			if (skill) { // hack because we can't get from the datafile...
				switch (sourceid) {
					case 2001002: // magic guard
					case 12001001:
					case 22111001:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_GUARD, ret.x));
						break;
					case 2301003: // invincible
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INVINCIBLE, ret.x));
						break;
					case 9001004: // hide
						ret.duration = 60 * 120 * 1000;
						ret.overTime = true;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, ret.x));
						break;
					case 13101006: // Wind Walk
					case 4001003: // darksight
					case 14001003: // cygnus ds
					case 4330001:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, ret.x));
						break;
					case 4211003: // pickpocket
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PICKPOCKET, ret.x));
						break;
					case 4211005: // mesoguard
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOGUARD, ret.x));
						break;
					case 4111001: // mesoup
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOUP, ret.x));
						break;
					case 4111002: // shadowpartner
					case 14111000: // cygnus
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, ret.x));
						break;
					case 11101002: // All Final attack
					case 13101002:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINALATTACK, 1));
						break;
					case 3101004: // soul arrow
					case 3201004:
					case 2311002: // mystic door - hacked buff icon
					case 13101003:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, ret.x));
						break;
					case 1211006: // wk charges
					case 1211003:
					case 1211004:
					case 1211005:
					case 1211008:
					case 1211007:
					case 1221003:
					case 1221004:
					case 11111007:
					case 15101006:
					case 15111006: // Spark
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPARK, ret.x));
						break;
					case 21111005:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WK_CHARGE, ret.x));
						break;
					case 12101005:
					case 22121001: // Elemental Reset
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ELEMENT_RESET, ret.x));
						break;
					case 5110001: // Energy Charge
					case 15100004:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, 1));
						break;
					case 1101005: // booster
					case 1101004:
					case 1201005:
					case 1201004:
					case 1301005:
					case 1301004:
					case 3101002:
					case 3201002:
					case 4101003:
					case 4201002:
					case 2111005: // spell booster, do these work the same?
					case 2211005:
					case 5101006:
					case 5201003:
					case 11101001:
					case 12101004:
					case 13101001:
					case 14101002:
					case 15101002:
					case 21001003: // Aran - Pole Arm Booster
					case 22141002: // Magic Booster
					case 4301002:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BOOSTER, ret.x));
						break;
					//case 5121009:
					//case 15111005:
					//    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPEED_INFUSION, ret.x));
					//    break;
					case 4321000: //tornado spin uses same buffstats
						ret.duration = 1000;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_SPEED, 100 + ret.x));
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_JUMP, ret.y)); //always 0 but its there
						break;
					case 5001005: // Dash
					case 15001003:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_SPEED, ret.x));
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_JUMP, ret.y));
						break;
					case 1101007: // pguard
					case 1201007:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.POWERGUARD, ret.x));
						break;
					case 1301007: // hyper body
					case 9001008:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAXHP, ret.x));
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAXMP, ret.y));
						break;
					case 1001: // recovery
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.RECOVERY, ret.x));
						break;
					case 1111002: // combo
					case 11111001: // combo
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
						break;
					case 5211006: // Homing Beacon
					case 5220011: // Bullseye
					case 22151002: //killer wings

						ret.duration = 60 * 120000;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOMING_BEACON, ret.x));
						break;
					case 1011: // Berserk fury
					case 10001011:
					case 20001011:
					case 20011011:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BERSERK_FURY, 1));
						break;
					case 1010:
					case 10001010:// Invincible Barrier
					case 20001010:
					case 20011010:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DIVINE_BODY, 1));
						break;
					case 1311006: //dragon roar
						ret.hpR = -ret.x / 100.0;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGON_ROAR, ret.y));
						break;
					case 1311008: // dragon blood
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGONBLOOD, ret.x));
						break;
					case 4341007:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.THORNS, ret.x << 8 | ret.y));
						break;
					case 4341002:
						ret.duration = 60 * 1000;
						ret.overTime = true;
						ret.hpR = -ret.x / 100.0;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINAL_CUT, ret.y));
						break;
					case 4331002:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_IMAGE, ret.x));
						break;
					case 4331003:
						ret.duration = 60 * 1000;
						ret.overTime = true;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.OWL_SPIRIT, ret.y));
						break;
					case 1121000: // maple warrior, all classes
					case 1221000:
					case 1321000:
					case 2121000:
					case 2221000:
					case 2321000:
					case 3121000:
					case 3221000:
					case 4121000:
					case 4221000:
					case 5121000:
					case 5221000:
					case 21121000: // Aran - Maple Warrior
					case 22171000:
					case 4341000:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAPLE_WARRIOR, ret.x));
						break;
					case 3121002: // sharp eyes bow master
					case 3221002: // sharp eyes marksmen
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHARP_EYES, ret.x << 8 | ret.y));
						break;
					case 21101003: // Body Pressure
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BODY_PRESSURE, ret.x));
						break;
					case 21000000: // Aran Combo
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARAN_COMBO, 100));
						break;
					case 21100005: // Combo Drain
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO_DRAIN, ret.x));
						break;
					case 21111001: // Smart Knockback
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SMART_KNOCKBACK, ret.x));
						break;
					case 4001002: // disorder
					case 14001002: // cygnus disorder
						monsterStatus.put(MonsterStatus.WATK, ret.x);
						monsterStatus.put(MonsterStatus.WDEF, ret.y);
						break;
					case 5221009: // Mind Control
						monsterStatus.put(MonsterStatus.HYPNOTIZE, 1);
						break;
					case 1201006: // threaten
						monsterStatus.put(MonsterStatus.WATK, ret.x);
						monsterStatus.put(MonsterStatus.WDEF, ret.y);
						break;
					case 1211002: // charged blow
					case 1111008: // shout
					case 4211002: // assaulter
					case 3101005: // arrow bomb
					case 1111005: // coma: sword
					case 1111006: // coma: axe
					case 4221007: // boomerang step
					case 5101002: // Backspin Blow
					case 5101003: // Double Uppercut
					case 5121004: // Demolition
					case 5121005: // Snatch
					case 5121007: // Barrage
					case 5201004: // pirate blank shot
					case 4121008: // Ninja Storm
					case 22151001:
					case 4201004: //steal, new
						monsterStatus.put(MonsterStatus.STUN, 1);
						break;
					case 4321002:
						monsterStatus.put(MonsterStatus.DARKNESS, 1);
						break;
					case 4221003:
					case 4121003:
						monsterStatus.put(MonsterStatus.SHOWDOWN, ret.x);
						monsterStatus.put(MonsterStatus.MDEF, ret.x);
						monsterStatus.put(MonsterStatus.WDEF, ret.x);
						break;
					case 2201004: // cold beam
					case 2211002: // ice strike
					case 3211003: // blizzard
					case 2211006: // il elemental compo
					case 2221007: // Blizzard
					case 5211005: // Ice Splitter
					case 2121006: // Paralyze
					case 21120006: // Tempest
					case 22121000:
						monsterStatus.put(MonsterStatus.FREEZE, 1);
						ret.duration *= 2; // freezing skills are a little strange
						break;
					case 2101003: // fp slow
					case 2201003: // il slow
					case 12101001:
					case 22141003: // Slow
						monsterStatus.put(MonsterStatus.SPEED, ret.x);
						break;
					case 2101005: // poison breath
					case 2111006: // fp elemental compo
					case 2121003: // ice demon
					case 2221003: // fire demon
					case 3111003: //inferno, new
					case 22161002: //phantom imprint
						monsterStatus.put(MonsterStatus.POISON, 1);
						break;
					case 4121004: // Ninja ambush
					case 4221004:
						monsterStatus.put(MonsterStatus.NINJA_AMBUSH, (int) ret.damage);
						break;
					case 2311005:
						monsterStatus.put(MonsterStatus.DOOM, 1);
						break;
					/*case 4341006:
					statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_TARGET, 1));
					break;*/
					case 3111002: // puppet ranger
					case 3211002: // puppet sniper
					case 13111004: // puppet cygnus
					case 5211001: // Pirate octopus summon
					case 5220002: // wrath of the octopi
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PUPPET, 1));
						break;
					case 3211005: // golden eagle
					case 3111005: // golden hawk
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
						monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
						break;
					case 3221005: // frostprey
					case 2121005: // elquines
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
						monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
						break;
					case 2311006: // summon dragon
					case 3121006: // phoenix
					case 2221005: // ifrit
					case 2321003: // bahamut
					case 1321007: // Beholder
					case 5211002: // Pirate bird summon
					case 11001004:
					case 12001004:
					case 12111004: // Itrit
					case 13001004:
					case 14001005:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
						break;
					case 2311003: // hs
					case 9001002: // GM hs
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SYMBOL, ret.x));
						break;
					case 2211004: // il seal
					case 2111004: // fp seal
					case 12111002: // cygnus seal
						monsterStatus.put(MonsterStatus.SEAL, 1);
						break;
					case 4111003: // shadow web
					case 14111001:
						monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
						break;
					case 4121006: // spirit claw
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPIRIT_CLAW, 0));
						break;
					case 2121004:
					case 2221004:
					case 2321004: // Infinity
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INFINITY, ret.x));
						break;
					case 1121002:
					case 1221002:
					case 1321002: // Stance
					case 21121003: // Aran - Freezing Posture
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.STANCE, (int) ret.prop));
						break;
					case 1005: // Echo of Hero
					case 10001005: // Cygnus Echo
					case 20001005: // Aran
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ECHO_OF_HERO, ret.x));
						break;
					case 1026: // Soaring
					case 10001026: // Soaring
					case 20001026: // Soaring
					case 20011026: // Soaring
						ret.duration = 60 * 120 * 1000; //because it seems to dispel asap.

						ret.overTime = true;
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOARING, 1));
						break;
					case 2121002: // mana reflection
					case 2221002:
					case 2321002:
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MANA_REFLECTION, 1));
						break;
					case 2321005: // holy shield
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SHIELD, ret.x));
						break;
					case 3121007: // Hamstring
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HAMSTRING, ret.x));
						monsterStatus.put(MonsterStatus.SPEED, ret.x);
						break;
					case 3221006: // Blind
						statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLIND, ret.x));
						monsterStatus.put(MonsterStatus.ACC, ret.x);
						break;
					default:
						break;
				}
			}
			if (ret.morphId > 0 || ret.isPirateMorph()) {
				statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, ret.getMorph()));
			}
			if (ret.isMonsterRiding()) {
				statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 1));
			}
			ret.monsterStatus = monsterStatus;
			statups.trimToSize();
			ret.statups = statups;
		}

		return ret;
	}

	/**
	 * @param applyto
	 * @param obj
	 * @param attack damage done by the skill
	 */
	public final void applyPassive(final MapleCharacter applyto, final MapleMapObject obj) {
		if (makeChanceResult()) {
			switch (sourceid) { // MP eater
				case 2100000:
				case 2200000:
				case 2300000:
					if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
						return;
					}
					final MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
					if (!mob.getStats().isBoss()) {
						final int absorbMp = Math.min((int) (mob.getMobMaxMp() * (getX() / 100.0)), mob.getMp());
						if (absorbMp > 0) {
							mob.setMp(mob.getMp() - absorbMp);
							applyto.getStat().setMp(applyto.getStat().getMp() + absorbMp);
							applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 1));
							applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1), false);
						}
					}
					break;
			}
		}
	}

	public final boolean applyTo(MapleCharacter chr) {
		return applyTo(chr, chr, true, null);
	}

	public final boolean applyTo(MapleCharacter chr, Point pos) {
		return applyTo(chr, chr, true, pos);
	}

	private final boolean applyTo(final MapleCharacter applyfrom, final MapleCharacter applyto, final boolean primary, final Point pos) {
		/*	if (sourceid == 4341006 && applyfrom.getBuffedValue(MapleBuffStat.MIRROR_IMAGE) == null) {
		return false;
		} */
		int hpchange = calcHPChange(applyfrom, primary);
		int mpchange = calcMPChange(applyfrom, primary);

		final PlayerStats stat = applyto.getStat();

		if (primary) {
			if (itemConNo != 0) {
				MapleInventoryManipulator.removeById(applyto.getClient(), GameConstants.getInventoryType(itemCon), itemCon, itemConNo, false, true);
			}
		} else if (!primary && isResurrection()) {
			hpchange = stat.getMaxHp();
			applyto.setStance(0); //TODO fix death bug, player doesnt spawn on other screen
		}
		if (isDispel() && makeChanceResult()) {
			applyto.dispelDebuffs();
		} else if (isHeroWill()) {
			applyto.dispelDebuff(MapleDisease.SEDUCE);
		} else if (cureDebuffs.size() > 0) {
			for (final MapleDisease debuff : cureDebuffs) {
				applyfrom.dispelDebuff(debuff);
			}
		} else if (isMPRecovery()) {
			final int toDecreaseHP = ((stat.getMaxHp() / 100) * 10);
			if (stat.getHp() > toDecreaseHP) {
				hpchange += -toDecreaseHP; // -10% of max HP
			} else {
				hpchange = stat.getHp() == 1 ? 0 : stat.getHp() - 1;
			}
			mpchange += ((toDecreaseHP / 100) * getY());
		}
		final List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
		if (hpchange != 0) {
			if (hpchange < 0 && (-hpchange) > stat.getHp() && !applyto.hasDisease(MapleDisease.ZOMBIFY)) {
				return false;
			}
			stat.setHp(stat.getHp() + hpchange);
		}
		if (mpchange != 0) {
			if (mpchange < 0 && (-mpchange) > stat.getMp()) {
				return false;
			}
			stat.setMp(stat.getMp() + mpchange);

			hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(stat.getMp())));
		}
		hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(stat.getHp())));

		applyto.getClient().getSession().write(MaplePacketCreator.updatePlayerStats(hpmpupdate, true, applyto.getJob()));

		if (expinc != 0) {
			applyto.gainExp(expinc, true, true, false);
			applyto.getClient().getSession().write(MaplePacketCreator.showSpecialEffect(19));
		} else if (GameConstants.isMonsterCard(sourceid)) {
			applyto.getMonsterBook().addCard(applyto.getClient(), sourceid);
		} else if (isSpiritClaw()) {
			MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
			IItem item;
			for (int i = 0; i < use.getSlotLimit(); i++) { // impose order...
				item = use.getItem((byte) i);
				if (item != null) {
					if (GameConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200) {
						MapleInventoryManipulator.removeById(applyto.getClient(), MapleInventoryType.USE, item.getItemId(), 200, false, true);
						break;
					}
				}
			}
		}
		if (overTime) {
			applyBuffEffect(applyfrom, applyto, primary);
		}
		if (primary) {
			if (overTime || isHeal()) {
				applyBuff(applyfrom);
			}
			if (isMonsterBuff()) {
				applyMonsterBuff(applyfrom);
			}
		}
		final SummonMovementType summonMovementType = getSummonMovementType();
		if (summonMovementType != null && pos != null) {
			final MapleSummon tosummon = new MapleSummon(applyfrom, sourceid, pos, summonMovementType);
			if (!tosummon.isPuppet()) {
				applyfrom.getCheatTracker().resetSummonAttack();
			}
			applyfrom.getMap().spawnSummon(tosummon);
			applyfrom.getSummons().put(sourceid, tosummon);
			tosummon.addHP((short) x);
			if (isBeholder()) {
				tosummon.addHP((short) 1);
			}
			/*if (sourceid == 4341006) {
			applyfrom.cancelEffectFromBuffStat(MapleBuffStat.MIRROR_IMAGE);
			}*/
		} else if (isMagicDoor()) { // Magic Door
			MapleDoor door = new MapleDoor(applyto, new Point(applyto.getPosition())); // Current Map door
			applyto.getMap().spawnDoor(door);
			applyto.addDoor(door);

			MapleDoor townDoor = new MapleDoor(door); // Town door
			applyto.addDoor(townDoor);
			door.getTown().spawnDoor(townDoor);

			if (applyto.getParty() != null) { // update town doors
				applyto.silentPartyUpdate();
			}
			applyto.disableDoor();

		} else if (isMist()) {
			final Rectangle bounds = calculateBoundingBox(pos != null ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
			final MapleMist mist = new MapleMist(bounds, applyfrom, this);
			applyfrom.getMap().spawnMist(mist, getDuration(), isMistPoison(), false);

		} else if (isTimeLeap()) { // Time Leap
			for (PlayerCoolDownValueHolder i : applyto.getAllCooldowns()) {
				if (i.skillId != 5121010) {
					applyto.removeCooldown(i.skillId);
					applyto.getClient().getSession().write(MaplePacketCreator.skillCooldown(i.skillId, 0));
				}
			}
		}
		return true;
	}

	public final boolean applyReturnScroll(final MapleCharacter applyto) {
		if (moveTo != -1) {
			if (applyto.getMap().getReturnMapId() != applyto.getMapId()) {
				MapleMap target;
				if (moveTo == 999999999) {
					target = applyto.getMap().getReturnMap();
				} else {
					target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory(applyto.getWorld()).getMap(moveTo);
					if (target.getId() / 10000000 != 60 && applyto.getMapId() / 10000000 != 61) {
						if (target.getId() / 10000000 != 21 && applyto.getMapId() / 10000000 != 20) {
							if (target.getId() / 10000000 != applyto.getMapId() / 10000000) {
								return false;
							}
						}
					}
				}
				applyto.changeMap(target, target.getPortal(0));
				return true;
			}
		}
		return false;
	}

	private final void applyBuff(final MapleCharacter applyfrom) {
		if (isPartyBuff() && (applyfrom.getParty() != null || isGmBuff())) {
			final Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
			final List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));

			for (final MapleMapObject affectedmo : affecteds) {
				final MapleCharacter affected = (MapleCharacter) affectedmo;

				if (affected != applyfrom && (isGmBuff() || applyfrom.getParty().equals(affected.getParty()))) {
					if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
						applyTo(applyfrom, affected, false, null);
						affected.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 2));
						affected.getMap().broadcastMessage(affected, MaplePacketCreator.showBuffeffect(affected.getId(), sourceid, 2), false);
					}
					if (isTimeLeap()) {
						for (PlayerCoolDownValueHolder i : affected.getAllCooldowns()) {
							if (i.skillId != 5121010) {
								affected.removeCooldown(i.skillId);
								affected.getClient().getSession().write(MaplePacketCreator.skillCooldown(i.skillId, 0));
							}
						}
					}
				}
			}
		}
	}

	private final void applyMonsterBuff(final MapleCharacter applyfrom) {
		final Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
		final List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
		int i = 0;

		for (final MapleMapObject mo : affected) {
			if (makeChanceResult()) {
				((MapleMonster) mo).applyStatus(applyfrom, new MonsterStatusEffect(getMonsterStati(), SkillFactory.getSkill(sourceid), null, false), isPoison(), getDuration(), false);
			}
			i++;
			if (i >= mobCount) {
				break;
			}
		}
	}

	private final Rectangle calculateBoundingBox(final Point posFrom, final boolean facingLeft) {
		Point mylt;
		Point myrb;
		if (facingLeft) {
			mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
			myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
		} else {
			myrb = new Point(lt.x * -1 + posFrom.x, rb.y + posFrom.y);
			mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
		}
		return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
	}

	public final void silentApplyBuff(final MapleCharacter chr, final long starttime) {
		final int localDuration = alchemistModifyVal(chr, duration, false);
		chr.registerEffect(this, starttime, TimerManager.getInstance().schedule(new CancelEffectAction(chr, this, starttime),
				((starttime + localDuration) - System.currentTimeMillis())));

		final SummonMovementType summonMovementType = getSummonMovementType();
		if (summonMovementType != null) {
			final MapleSummon tosummon = new MapleSummon(chr, sourceid, chr.getPosition(), summonMovementType);
			if (!tosummon.isPuppet()) {
				chr.getCheatTracker().resetSummonAttack();
				chr.getMap().spawnSummon(tosummon);
				chr.getSummons().put(sourceid, tosummon);
				tosummon.addHP((short) x);
			}
		}
	}

	public final void applyComboBuff(final MapleCharacter applyto, short combo) {
		final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARAN_COMBO, (int) combo));
		applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, 99999, stat, this)); // Hackish timing, todo find out

		final long starttime = System.currentTimeMillis();
//	final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
//	final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + 99999) - System.currentTimeMillis()));
		applyto.registerEffect(this, starttime, null);
	}

	public final void applyEnergyBuff(final MapleCharacter applyto, final boolean infinity) {
//	final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, (int) applyto.getEnergyCharge()));
		applyto.getClient().getSession().write(MaplePacketCreator.giveEnergyChargeTest(0));

		final long starttime = System.currentTimeMillis();
		if (infinity) {
			applyto.registerEffect(this, starttime, null);
		} else {
			final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
			final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + duration) - System.currentTimeMillis()));
			applyto.registerEffect(this, starttime, schedule);
		}
	}

	private final void applyBuffEffect(final MapleCharacter applyfrom, final MapleCharacter applyto, final boolean primary) {
		if (!isMonsterRiding_()) {
			applyto.cancelEffect(this, true, -1);
		}
		int localDuration = duration;

		if (primary) {
			localDuration = alchemistModifyVal(applyfrom, localDuration, false);
			applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1), false);
		}
		boolean normal = true;

		switch (sourceid) {
			case 5001005: // Dash
			case 4321000: //tornado spin
			case 15001003: {
				applyto.getClient().getSession().write(MaplePacketCreator.givePirate(statups, localDuration / 1000, sourceid));
				applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignPirate(statups, localDuration / 1000, applyto.getId(), sourceid), false);
				normal = false;
				break;
			}
			case 5211006: // Homing Beacon
			case 22151002: //killer wings
			case 5220011: {// Bullseye
				if (applyto.getLinkMid() > 0) {
					applyto.getClient().getSession().write(MaplePacketCreator.cancelHoming());
					applyto.getClient().getSession().write(MaplePacketCreator.giveHoming(sourceid, applyto.getLinkMid()));
				} else {
					return;
				}
				normal = false;
				break;
			}
			case 1004:
			case 10001004:
			case 5221006:
			case 20001004: {
				final int mountid = parseMountInfo(applyto, sourceid);
				if (mountid != 0) {
					final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 0));
					applyto.getClient().getSession().write(MaplePacketCreator.giveMount(mountid, sourceid, stat));
					applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showMonsterRiding(applyto.getId(), stat, mountid, sourceid), false);
					normal = false;
				}
				break;
			}
			case 15100004:
			case 5110001: { // Energy Charge
//		final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, applyto.getEnergyCharge()));
//		applyto.getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(stat, (skill ? sourceid : -sourceid), localDuration));
				applyto.getClient().getSession().write(MaplePacketCreator.giveEnergyChargeTest(0));
				normal = false;
				break;
			}
			case 5121009: // Speed Infusion
			case 15111005:
				applyto.getClient().getSession().write(MaplePacketCreator.giveInfusion(statups, sourceid, localDuration));
				applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignInfusion(applyto.getId(), x, localDuration), false);
				normal = false;
				break;
			case 13101006:
			case 4330001:
			case 4001003:
			case 14001003: { // Dark Sight
				final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
				applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
				break;
			}
			case 4341002: { // Final Cut
				final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINAL_CUT, y));
				applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, stat, this));
				normal = false;
				break;
			}
			case 4331003: { // Owl Spirit
				final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.OWL_SPIRIT, y));
				applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, stat, this));
				normal = false;
				break;
			}
			case 4331002: { // Mirror Image
				final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_IMAGE, 0));
				applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
				break;
			}
			case 1111002:
			case 11111001: { // Combo
				final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
				applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
				break;
			}
			case 3101004:
			case 3201004:
			case 13101003: { // Soul Arrow
				final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, 0));
				applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
				break;
			}
			case 4111002:
			case 14111000: { // Shadow Partne
				final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, 0));
				applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
				break;
			}
			case 1121010: // Enrage
				applyto.handleOrbconsume();
				break;
			default:
				if (isMorph() || isPirateMorph()) {
					final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(getMorph(applyto))));
					applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
				} else if (isMonsterRiding()) {
					final int mountid = parseMountInfo(applyto, sourceid);
					if (mountid != 0) {
						final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 0));
						applyto.getClient().getSession().write(MaplePacketCreator.cancelBuff(null));
						applyto.getClient().getSession().write(MaplePacketCreator.giveMount(mountid, sourceid, stat));
						applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showMonsterRiding(applyto.getId(), stat, mountid, sourceid), false);
					} else {
						return;
					}
					normal = false;
				} else if (isSoaring()) {
					final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOARING, 1));
					applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
					applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, stat, this));
					normal = false;
				}
				break;
		}
		// Broadcast effect to self
		if (normal && statups.size() > 0) {
			applyto.getClient().getSession().write(MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, statups, this));
		}
		final long starttime = System.currentTimeMillis();
		final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
		final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + localDuration) - System.currentTimeMillis()));
		applyto.registerEffect(this, starttime, schedule);
	}

	public static final int parseMountInfo(final MapleCharacter player, final int skillid) {
		switch (skillid) {
			case 1004: // Monster riding
			case 10001004:
			case 20001004:
			case 20011004:
				if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -22) != null) {
					return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -22).getItemId();
				}
				return 0;
			case 5221006: // Battleship
				return 1932000;
		}
		return 0;
	}

	private final int calcHPChange(final MapleCharacter applyfrom, final boolean primary) {
		int hpchange = 0;
		if (hp != 0) {
			if (!skill) {
				if (primary) {
					hpchange += alchemistModifyVal(applyfrom, hp, true);
				} else {
					hpchange += hp;
				}
				if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
					hpchange /= 2;
				}
			} else { // assumption: this is heal
				hpchange += makeHealHP(hp / 100.0, applyfrom.getStat().getTotalMagic(), 3, 5);
				if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
					hpchange = -hpchange;
				}
			}
		}
		if (hpR != 0) {
			hpchange += (int) (applyfrom.getStat().getCurrentMaxHp() * hpR);
		}
		// actually receivers probably never get any hp when it's not heal but whatever
		if (primary) {
			if (hpCon != 0) {
				hpchange -= hpCon;
			}
		}
		switch (this.sourceid) {
			case 4211001: // Chakra
//		final PlayerStats stat = applyfrom.getStat();
//		int v42 = getY() + 100;
//		int v38 = Randomizer.rand(100, 200) % 0x64 + 100;
//		hpchange = (int) ((v38 * stat.getLuk() * 0.033 + stat.getDex()) * v42 * 0.002);
				hpchange += makeHealHP(getY() / 100.0, applyfrom.getStat().getTotalLuk(), 2.3, 3.5);
				break;
		}
		return hpchange;
	}

	private static final int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
		return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
	}

	private static final int getElementalAmp(final int job) {
		switch (job) {
			case 211:
			case 212:
				return 2110001;
			case 221:
			case 222:
				return 2210001;
			case 1211:
			case 1212:
				return 12110001;
			case 2215:
			case 2216:
			case 2217:
			case 2218:
				return 22150000;
		}
		return -1;
	}

	private final int calcMPChange(final MapleCharacter applyfrom, final boolean primary) {
		int mpchange = 0;
		if (mp != 0) {
			if (primary) {
				mpchange += alchemistModifyVal(applyfrom, mp, true);
			} else {
				mpchange += mp;
			}
		}
		if (mpR != 0) {
			mpchange += (int) (applyfrom.getStat().getCurrentMaxMp() * mpR);
		}
		if (primary) {
			if (mpCon != 0) {
				double mod = 1.0;

				final int ElemSkillId = getElementalAmp(applyfrom.getJob());
				if (ElemSkillId != -1) {
					final ISkill amp = SkillFactory.getSkill(ElemSkillId);
					final int ampLevel = applyfrom.getSkillLevel(amp);
					if (ampLevel > 0) {
						MapleStatEffect ampStat = amp.getEffect(ampLevel);
						mod = ampStat.getX() / 100.0;
					}
				}
				if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
					mpchange = 0;
				} else {
					mpchange -= mpCon * mod;
				}
			}
		}
		return mpchange;
	}

	private final int alchemistModifyVal(final MapleCharacter chr, final int val, final boolean withX) {
		if (!skill) {
			final MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
			if (alchemistEffect != null) {
				return (int) (val * ((withX ? alchemistEffect.getX() : alchemistEffect.getY()) / 100.0));
			}
		}
		return val;
	}

	private final MapleStatEffect getAlchemistEffect(final MapleCharacter chr) {
		ISkill al;
		switch (chr.getJob()) {
			case 411:
			case 412:
				al = SkillFactory.getSkill(4110000);
				if (chr.getSkillLevel(al) == 0) {
					return null;
				}
				return al.getEffect(chr.getSkillLevel(al));
			case 1411:
			case 1412:
				al = SkillFactory.getSkill(14110003);
				if (chr.getSkillLevel(al) == 0) {
					return null;
				}
				return al.getEffect(chr.getSkillLevel(al));
		}
		return null;
	}

	public final void setSourceId(final int newid) {
		sourceid = newid;
	}

	private final boolean isGmBuff() {
		switch (sourceid) {
			case 1005: // echo of hero acts like a gm buff
			case 10001005: // cygnus Echo
			case 20001005: // Echo
			case 20011005:
			case 9001000: // GM dispel
			case 9001001: // GM haste
			case 9001002: // GM Holy Symbol
			case 9001003: // GM Bless
			case 9001005: // GM resurrection
			case 9001008: // GM Hyper body
				return true;
			default:
				return false;
		}
	}

	private final boolean isMonsterBuff() {
		switch (sourceid) {
			case 1201006: // threaten
			case 2101003: // fp slow
			case 2201003: // il slow
			case 12101001: // cygnus slow
			case 2211004: // il seal
			case 2111004: // fp seal
			case 12111002: // cygnus seal
			case 2311005: // doom
			case 4111003: // shadow web
			case 14111001: // cygnus web
			case 4121004: // Ninja ambush
			case 4221004: // Ninja ambush
			case 22151001:
			case 22141003:
			case 22121000:
			case 22161002:
			case 4321002:
				return skill;
		}
		return false;
	}

	public final boolean isMonsterRiding_() {
		return skill && (sourceid == 1004 || sourceid == 10001004 || sourceid == 20001004 || sourceid == 20011004);
	}

	public final boolean isMonsterRiding() {
		return skill && (isMonsterRiding_());
	}

	private final boolean isPartyBuff() {
		if (lt == null || rb == null) {
			return false;
		}
		switch (sourceid) {
			case 1211003:
			case 1211004:
			case 1211005:
			case 1211006:
			case 1211007:
			case 1211008:
			case 1221003:
			case 1221004:
			case 11111007:
			case 12101005:
				return false;
		}
		return true;
	}

	public final boolean isHeal() {
		return sourceid == 2301002 || sourceid == 9101000;
	}

	public final boolean isResurrection() {
		return sourceid == 9001005 || sourceid == 2321006;
	}

	public final boolean isTimeLeap() {
		return sourceid == 5121010;
	}

	public final short getHp() {
		return hp;
	}

	public final short getMp() {
		return mp;
	}

	public final byte getMastery() {
		return mastery;
	}

	public final short getWatk() {
		return watk;
	}

	public final short getMatk() {
		return matk;
	}

	public final short getWdef() {
		return wdef;
	}

	public final short getMdef() {
		return mdef;
	}

	public final short getAcc() {
		return acc;
	}

	public final short getAvoid() {
		return avoid;
	}

	public final short getHands() {
		return hands;
	}

	public final short getSpeed() {
		return speed;
	}

	public final short getJump() {
		return jump;
	}

	public final int getDuration() {
		return duration;
	}

	public final boolean isOverTime() {
		return overTime;
	}

	public final List<Pair<MapleBuffStat, Integer>> getStatups() {
		return statups;
	}

	public final boolean sameSource(final MapleStatEffect effect) {
		return this.sourceid == effect.sourceid && this.skill == effect.skill;
	}

	public final int getX() {
		return x;
	}

	public final int getY() {
		return y;
	}

	public final int getZ() {
		return z;
	}

	public final short getDamage() {
		return damage;
	}

	public final byte getAttackCount() {
		return attackCount;
	}

	public final byte getBulletCount() {
		return bulletCount;
	}

	public final int getBulletConsume() {
		return bulletConsume;
	}

	public final byte getMobCount() {
		return mobCount;
	}

	public final int getMoneyCon() {
		return moneyCon;
	}

	public final int getCooldown() {
		return cooldown;
	}

	public final Map<MonsterStatus, Integer> getMonsterStati() {
		return monsterStatus;
	}

	public final boolean isHide() {
		return skill && sourceid == 9001004;
	}

	public final boolean isDragonBlood() {
		return skill && sourceid == 1311008;
	}

	public final boolean isBerserk() {
		return skill && sourceid == 1320006;
	}

	public final boolean isBeholder() {
		return skill && sourceid == 1321007;
	}

	public final boolean isMPRecovery() {
		return skill && sourceid == 5101005;
	}

	public final boolean isMagicDoor() {
		return skill && sourceid == 2311002;
	}

	public final boolean isMesoGuard() {
		return skill && sourceid == 4211005;
	}

	public final boolean isCharge() {
		switch (sourceid) {
			case 1211003:
			case 1211008:
			case 11111007:
			case 12101005:
			case 15101006:
			case 21111005:
				return skill;
		}
		return false;
	}

	public final boolean isMistPoison() {
		switch (sourceid) {
			case 2111003:
			case 12111005: // Flame gear
			case 14111006: // Poison bomb
				return true;
		}
		return false;
	}

	public final boolean isPoison() {
		switch (sourceid) {
			case 2111003:
			case 2101005:
			case 2111006:
			case 2121003:
			case 2221003:
			case 12111005: // Flame gear
			case 3111003: //inferno, new
			case 22161002: //phantom imprint
				return skill;
		}
		return false;
	}

	private final boolean isMist() {
		return skill && (sourceid == 2111003 || sourceid == 4221006 || sourceid == 12111005 || sourceid == 14111006); // poison mist, smokescreen and flame gear
	}

	private final boolean isSpiritClaw() {
		return skill && sourceid == 4121006;
	}

	private final boolean isDispel() {
		return skill && (sourceid == 2311001 || sourceid == 9001000);
	}

	private final boolean isHeroWill() {
		switch (sourceid) {
			case 1121011:
			case 1221012:
			case 1321010:
			case 2121008:
			case 2221008:
			case 2321009:
			case 3121009:
			case 3221008:
			case 4121009:
			case 4221008:
			case 5121008:
			case 5221010:
			case 21121008:
			case 22171004:
			case 4341008:
				return skill;
		}
		return false;
	}

	public final boolean isAranCombo() {
		return sourceid == 21000000;
	}

	public final boolean isPirateMorph() {
		switch (sourceid) {
			case 15111002:
			case 5111005:
			case 5121003:
				return skill;
		}
		return false;
	}

	public final boolean isMorph() {
		return morphId > 0;
	}

	public final int getMorph() {
		return morphId;
	}

	public final int getMorph(final MapleCharacter chr) {
		switch (morphId) {
			case 1000:
			case 1100:
				return morphId + chr.getGender();
			case 1003:
				return morphId + (chr.getGender() * 100);
		}
		return morphId;
	}

	public final SummonMovementType getSummonMovementType() {
		if (!skill) {
			return null;
		}
		switch (sourceid) {
			case 3211002: // puppet sniper
			case 3111002: // puppet ranger
			case 13111004: // puppet cygnus
			case 5211001: // octopus - pirate
			case 5220002: // advanced octopus - pirate
			case 4341006:
				return SummonMovementType.STATIONARY;
			case 3211005: // golden eagle
			case 3111005: // golden hawk
			case 2311006: // summon dragon
			case 3221005: // frostprey
			case 3121006: // phoenix
			case 5211002: // bird - pirate
				return SummonMovementType.CIRCLE_FOLLOW;
			case 1321007: // beholder
			case 2121005: // elquines
			case 2221005: // ifrit
			case 2321003: // bahamut
			case 12111004: // Ifrit
			case 11001004: // soul
			case 12001004: // flame
			case 13001004: // storm
			case 14001005: // darkness
			case 15001004:
				return SummonMovementType.FOLLOW;
		}
		return null;
	}

	public final boolean isSoaring() {

		switch (sourceid) {

			case 1026: // Soaring

			case 10001026: // Soaring

			case 20001026: // Soaring

			case 20011026: // Soaring

				return skill;

		}

		return false;

	}

	public final boolean isSkill() {
		return skill;
	}

	public final int getSourceId() {
		return sourceid;
	}

	/**
	 *
	 * @return true if the effect should happen based on it's probablity, false otherwise
	 */
	public final boolean makeChanceResult() {
		return prop == 100 || Randomizer.nextInt(99) < prop;
	}

	public final short getProb() {
		return prop;
	}

	public static class CancelEffectAction implements Runnable {

		private final MapleStatEffect effect;
		private final WeakReference<MapleCharacter> target;
		private final long startTime;

		public CancelEffectAction(final MapleCharacter target, final MapleStatEffect effect, final long startTime) {
			this.effect = effect;
			this.target = new WeakReference<MapleCharacter>(target);
			this.startTime = startTime;
		}

		@Override
		public void run() {
			final MapleCharacter realTarget = target.get();
			if (realTarget != null) {
				realTarget.cancelEffect(effect, false, startTime);
			}
		}
	}
}