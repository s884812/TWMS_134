package client;

import java.util.ArrayList;
import java.util.List;

import provider.MapleData;
import provider.MapleDataTool;
import server.MapleStatEffect;
import server.life.Element;

public class Skill implements ISkill {

	public static final int[] skills = new int[]{4311003, 4321000, 4331002, 4331005, 4341004, 4341007};
	private int id;
	private final List<MapleStatEffect> effects = new ArrayList<MapleStatEffect>();
	private Element element;
	private byte level;
	private int animationTime, requiredSkill, masterLevel;
	private boolean action;
	private boolean invisible;
	private boolean chargeskill;
	public static final int[] evanskills1 = new int[]{22171000, 22171002, 22171003, 22171004};
	public static final int[] evanskills2 = new int[]{22181000, 22181001, 22181002, 22181003};

	private Skill(final int id) {
		super();
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	public static final Skill loadFromData(final int id, final MapleData data) {
		Skill ret = new Skill(id);

		final MapleData oldSchool = data.getChildByPath("level");
		if (oldSchool != null) {
			boolean isBuff = false;
			final int skillType = MapleDataTool.getInt("skillType", data, -1);
			final String elem = MapleDataTool.getString("elemAttr", data, null);
			if (elem != null) {
				ret.element = Element.getFromChar(elem.charAt(0));
			} else {
				ret.element = Element.NEUTRAL;
			}
			ret.invisible = MapleDataTool.getInt("invisible", data, 0) > 0;
			//ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);

			// unfortunatly this is only set for a few skills so we have to do some more to figure out if it's a buff
			final MapleData effect = data.getChildByPath("effect");
			if (skillType != -1) {
				if (skillType == 2) {
					isBuff = true;
				}
			} else {
				final MapleData action_ = data.getChildByPath("action");
				final MapleData hit = data.getChildByPath("hit");
				final MapleData ball = data.getChildByPath("ball");

				boolean action = false;
				if (action_ == null) {
					if (data.getChildByPath("prepare/action") != null) {
						action = true;
					} else {
						switch (id) {
							case 5201001:
							case 5221009:
								action = true;
								break;
						}
					}
				} else {
					action = true;
				}
				ret.action = action;
				isBuff = effect != null && hit == null && ball == null;
				isBuff |= action_ != null && MapleDataTool.getString("0", action_, "").equals("alert2");
				switch (id) {
					case 2301002: // heal is alert2 but not overtime...
					case 2111003: // poison mist
					case 12111005: // Flame Gear
					case 2111002: // explosion
					case 4211001: // chakra
					case 2121001: // Big bang
					case 2221001: // Big bang
					case 2321001: // Big bang
						isBuff = false;
						break;
					case 1004: // monster riding
					case 10001004:
					case 20001004:
					case 20011004:
					case 9101004: // hide is a buff -.- atleast for us o.o"
					case 1111002: // combo
					case 15111006: // Spark
					case 4211003: // pickpocket
					case 4111001: // mesoup
					case 15111002: // Super Transformation
					case 5111005: // Transformation
					case 5121003: // Super Transformation
					case 13111005: // Alabtross
					case 21000000: // Aran Combo
					case 21101003: // Body Pressure
					case 5211001: // Pirate octopus summon
					case 5211002:
					case 5220002: // wrath of the octopi
					case 5211006: //homing beacon
					case 5220011: //bullseye

					case 22121001: //element reset
					case 22131001: //magic shield -- NOT CODED
					case 22141002: //magic booster
					case 22151002: //killer wing
					case 22151003: //magic resist -- NOT CODED
					case 22171000: //maple warrior
					case 22171004: //hero will
					case 22181000: //onyx blessing
					case 22181003: //soul stone -- NOT CODED
					//case 22121000:
					//case 22141003:
					//case 22151001:
					//case 22161002:
					//tornado spin too ?
					//case 4341006: //final cut
					case 4331003: //owl spirit
					case 4321000: //tornado spin
						isBuff = true;
						break;
				}
			}
			ret.chargeskill = data.getChildByPath("keydown") != null;

			for (final MapleData level : data.getChildByPath("level")) {
				ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff));
			}
			final MapleData reqDataRoot = data.getChildByPath("req");
			if (reqDataRoot != null) {
				for (final MapleData reqData : reqDataRoot.getChildren()) {
					ret.requiredSkill = Integer.parseInt(reqData.getName());
					ret.level = (byte) MapleDataTool.getInt(reqData, 1);
				}
			}
			ret.animationTime = 0;
			if (effect != null) {
				for (final MapleData effectEntry : effect) {
					ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
				}
			}
		} else {
			boolean isBuff = false;
			final int skillType = MapleDataTool.getInt("common/skillType", data, -1);
			final String elem = MapleDataTool.getString("common/elemAttr", data, null);
			if (elem != null) {
				ret.element = Element.getFromChar(elem.charAt(0));
			} else {
				ret.element = Element.NEUTRAL;
			}
			ret.invisible = MapleDataTool.getInt("common/invisible", data, 0) > 0;
			//ret.masterLevel = MapleDataTool.getInt("common/masterLevel", data, 0);

			// unfortunatly this is only set for a few skills so we have to do some more to figure out if it's a buff
			final MapleData effect = data.getChildByPath("common/effect");
			if (skillType != -1) {
				if (skillType == 2) {
					isBuff = true;
				}
			} else {
				final MapleData action_ = data.getChildByPath("common/action");
				final MapleData hit = data.getChildByPath("common/hit");
				final MapleData ball = data.getChildByPath("common/ball");

				boolean action = false;
				if (action_ == null) {
					if (data.getChildByPath("common/prepare/action") != null) {
						action = true;
					} else {
						switch (id) {
							case 5201001:
							case 5221009:
								action = true;
								break;
						}
					}
				} else {
					action = true;
				}
				ret.action = action;
				isBuff = effect != null && hit == null && ball == null;
				isBuff |= action_ != null && MapleDataTool.getString("common/0", action_, "").equals("alert2");
				switch (id) {
					case 2301002: // heal is alert2 but not overtime...
					case 2111003: // poison mist
					case 12111005: // Flame Gear
					case 2111002: // explosion
					case 4211001: // chakra
					case 2121001: // Big bang
					case 2221001: // Big bang
					case 2321001: // Big bang
						isBuff = false;
						break;
					case 1004: // monster riding
					case 10001004:
					case 20001004:
					case 20011004:
					case 9101004: // hide is a buff -.- atleast for us o.o"
					case 1111002: // combo
					case 15111006: // Spark
					case 4211003: // pickpocket
					case 4111001: // mesoup
					case 15111002: // Super Transformation
					case 5111005: // Transformation
					case 5121003: // Super Transformation
					case 13111005: // Alabtross
					case 21000000: // Aran Combo
					case 21101003: // Body Pressure
					case 5211001: // Pirate octopus summon
					case 5211002:
					case 5220002: // wrath of the octopi
					case 5211006: //homing beacon
					case 5220011: //bullseye

					case 22121001: //element reset
					case 22131001: //magic shield -- NOT CODED
					case 22141002: //magic booster
					case 22151002: //killer wing
					case 22151003: //magic resist -- NOT CODED
					case 22171000: //maple warrior
					case 22171004: //hero will
					case 22181000: //onyx blessing
					case 22181003: //soul stone -- NOT CODED
					//case 22121000:
					//case 22141003:
					//case 22151001:
					//case 22161002:
					//tornado spin too ?
					//case 4341006: //final cut
					case 4331003: //owl spirit
					case 4321000: //tornado spin
						isBuff = true;
						break;
				}
			}
			ret.chargeskill = data.getChildByPath("common/keydown") != null;

			for (final MapleData level : data.getChildByPath("common/maxLevel")) {
				ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff));
			}
			final MapleData reqDataRoot = data.getChildByPath("common/req");
			if (reqDataRoot != null) {
				for (final MapleData reqData : reqDataRoot.getChildren()) {
					ret.requiredSkill = Integer.parseInt(reqData.getName());
					ret.level = (byte) MapleDataTool.getInt(reqData, 1);
				}
			}
			ret.animationTime = 0;
			if (effect != null) {
				for (final MapleData effectEntry : effect) {
					ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
				}
			}
		}
		return ret;
	}

	@Override
	public MapleStatEffect getEffect(final int level) {
		return effects.get(level - 1);
	}

	@Override
	public MapleStatEffect getEffect(final MapleCharacter chr, final int level) {
		return effects.get(level - 1);
	}

	@Override
	public boolean getAction() {
		return action;
	}

	@Override
	public boolean isChargeSkill() {
		return chargeskill;
	}

	@Override
	public boolean isInvisible() {
		return invisible;
	}

	@Override
	public boolean hasRequiredSkill() {
		return level > 0;
	}

	@Override
	public int getRequiredSkillLevel() {
		return level;
	}

	@Override
	public int getRequiredSkillId() {
		return requiredSkill;
	}

	@Override
	public byte getMaxLevel() {
		return (byte) effects.size();
	}

	@Override
	public boolean canBeLearnedBy(int job) {
		int jid = job;
		int skillForJob = id / 10000;
		if (skillForJob == 2001 && GameConstants.isEvan(job)) {
			return true; //special exception for evan -.-
		}
		if (job < 1000) {
			if (jid / 100 != skillForJob / 100 && skillForJob / 100 != 0) { // wrong job
				return false;
			}
		} else {
			if (jid / 1000 != skillForJob / 1000 && skillForJob / 1000 != 0) { // wrong job
				return false;
			}
		}
		if (GameConstants.isAdventurer(skillForJob) && !GameConstants.isAdventurer(job)) {
			return false;
		} else if (GameConstants.isKOC(skillForJob) && !GameConstants.isKOC(job)) {
			return false;
		} else if (GameConstants.isAran(skillForJob) && !GameConstants.isAran(job)) {
			return false;
		} else if (GameConstants.isEvan(skillForJob) && !GameConstants.isEvan(job)) {
			return false;
		}
		if ((skillForJob / 10) % 10 > (jid / 10) % 10) { // wrong 2nd job
			return false;
		}
		if (skillForJob % 10 > jid % 10) { // wrong 3rd/4th job
			return false;
		}
		return true;
	}

	@Override
	public boolean isFourthJob() {
		if (id / 10000 >= 2212 && id / 10000 < 3000) { //evan skill
			return ((id / 10000) % 10) >= 7;
		}
		if (id / 10000 >= 430 && id / 10000 <= 434) { //db skill
			return ((id / 10000) % 10) == 4 || isMasterSkill(id);
		}
		return ((id / 10000) % 10) == 2;
	}

	@Override
	public Element getElement() {
		return element;
	}

	@Override
	public int getAnimationTime() {
		return animationTime;
	}

	@Override
	public int getMasterLevel() {
		return masterLevel;
	}

	public static final boolean isMasterSkill(final int skill) {
		for (int i : skills) {
			if (i == skill) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBeginnerSkill() {
		String idString = String.valueOf(id);
		if (idString.length() == 4 || idString.length() == 1) {
			return true;
		}
		return false;
	}
}