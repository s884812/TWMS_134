package client;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.io.Serializable;

import server.MapleItemInformationProvider;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PlayerStats implements Serializable {

	private static final long serialVersionUID = -679541993413738569L;
	private transient WeakReference<MapleCharacter> chr;
	private transient float shouldHealHP, shouldHealMP;
	public int str, dex, luk, int_, hp, maxhp, mp, maxmp;
	private transient short passive_sharpeye_percent, passive_sharpeye_rate;
	private transient int localmaxhp, localmaxmp, localstr, localdex, localluk, localint_;
	private transient int magic, watk, hands, accuracy;
	private transient float speedMod, jumpMod, localmaxbasedamage;
	public transient int element_amp_percent;
	public transient int def, element_ice, element_fire, element_light, element_psn;

	public PlayerStats(final MapleCharacter chr) {
		this.chr = new WeakReference<MapleCharacter>(chr);
	}

	public final void init() {
		relocHeal();
		recalcLocalStats();
	}

	public final int getStr() {
		return str;
	}

	public final int getDex() {
		return dex;
	}

	public final int getLuk() {
		return luk;
	}

	public final int getInt() {
		return int_;
	}

	public final void setStr(final int str) {
		this.str = str;
		recalcLocalStats();
	}

	public final void setDex(final int dex) {
		this.dex = dex;
		recalcLocalStats();
	}

	public final void setLuk(final int luk) {
		this.luk = luk;
		recalcLocalStats();
	}

	public final void setInt(final int int_) {
		this.int_ = int_;
		recalcLocalStats();
	}

	public final boolean setHp(final int newhp) {
		return setHp(newhp, false);
	}

	public final boolean setHp(int newhp, boolean silent) {
		final int oldHp = hp;
		int thp = newhp;
		if (thp < 0) {
			thp = 0;
		}
		if (thp > localmaxhp) {
			thp = localmaxhp;
		}
		this.hp = thp;
		final MapleCharacter chra = chr.get();
		if (chra != null) {
			if (!silent) {
				chra.updatePartyMemberHP();
			}
			if (oldHp > hp && !chra.isAlive()) {
				chra.playerDead();
			}
		}
		return hp != oldHp;
	}

	public final boolean setMp(final int newmp) {
		final int oldMp = mp;
		int tmp = newmp;
		if (tmp < 0) {
			tmp = 0;
		}
		if (tmp > localmaxmp) {
			tmp = localmaxmp;
		}
		this.mp = tmp;
		return mp != oldMp;
	}

	public final void setMaxHp(final int hp) {
		this.maxhp = hp;
		recalcLocalStats();
	}

	public final void setMaxMp(final int mp) {
		this.maxmp = mp;
		recalcLocalStats();
	}

	public final int getHp() {
		return hp;
	}

	public final int getMaxHp() {
		return maxhp;
	}

	public final int getMp() {
		return mp;
	}

	public final int getMaxMp() {
		return maxmp;
	}

	public final int getTotalDex() {
		return localdex;
	}

	public final int getTotalInt() {
		return localint_;
	}

	public final int getTotalStr() {
		return localstr;
	}

	public final int getTotalLuk() {
		return localluk;
	}

	public final int getTotalMagic() {
		return magic;
	}

	public final double getSpeedMod() {
		return speedMod;
	}

	public final double getJumpMod() {
		return jumpMod;
	}

	public final int getTotalWatk() {
		return watk;
	}

	public final int getCurrentMaxHp() {
		return localmaxhp;
	}

	public final int getCurrentMaxMp() {
		return localmaxmp;
	}

	public final int getHands() {
		return hands;
	}

	public final float getCurrentMaxBaseDamage() {
		return localmaxbasedamage;
	}

	public void recalcLocalStats() {
	final MapleCharacter chra = chr.get();
	if (chra == null) {
		return;
	}
	int oldmaxhp = localmaxhp;
	localmaxhp = getMaxHp();
	localmaxmp = getMaxMp();
	localdex = getDex();
	localint_ = getInt();
	localstr = getStr();
	localluk = getLuk();
	int speed = 100;
	int jump = 100;
	magic = localint_;
	watk = 0;
	for (IItem item : chra.getInventory(MapleInventoryType.EQUIPPED)) {
		final IEquip equip = (IEquip) item;

		if (equip.getPosition() == -11) {
		if (GameConstants.isMagicWeapon(equip.getItemId())) {
			final Map<String, Integer> eqstat = MapleItemInformationProvider.getInstance().getEquipStats(equip.getItemId());

			element_fire = eqstat.get("incRMAF");
			element_ice = eqstat.get("incRMAI");
			element_light = eqstat.get("incRMAL");
			element_psn = eqstat.get("incRMAS");
			def = eqstat.get("elemDefault");
		} else {
			element_fire = 100;
			element_ice = 100;
			element_light = 100;
			element_psn = 100;
			def = 100;
		}
		}
		accuracy += equip.getAcc();
		localmaxhp += equip.getHp();
		localmaxmp += equip.getMp();
		localdex += equip.getDex();
		localint_ += equip.getInt();
		localstr += equip.getStr();
		localluk += equip.getLuk();
		magic += equip.getMatk() + equip.getInt();
		watk += equip.getWatk();
		speed += equip.getSpeed();
		jump += equip.getJump();
	}
	Integer buff = chra.getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
	if (buff != null) {
		final double d = buff.doubleValue() / 100;
		localstr += d * localstr;
		localdex += d * localdex;
		localluk += d * localluk;

		final int before = localint_;
		localint_ += d * localint_;
		magic += localint_ - before;
	}
	buff = chra.getBuffedValue(MapleBuffStat.ECHO_OF_HERO);
	if (buff != null) {
		final double d = buff.doubleValue() / 100;
		watk += watk / 100 * d;
		magic += magic / 100 * d;
	}
	buff = chra.getBuffedValue(MapleBuffStat.ARAN_COMBO);
	if (buff != null) {
		watk += buff / 10;
	}
	buff = chra.getBuffedValue(MapleBuffStat.MAXHP);
	if (buff != null) {
		localmaxhp += (buff.doubleValue() / 100) * localmaxhp;
	}
	buff = chra.getBuffedValue(MapleBuffStat.MAXMP);
	if (buff != null) {
		localmaxmp += (buff.doubleValue() / 100) * localmaxmp;
	}
	element_amp_percent = 100;

	switch (chra.getJob()) {
		case 322: { // Crossbowman
		final ISkill expert = SkillFactory.getSkill(3220004);
		final int boostLevel = chra.getSkillLevel(expert);
		if (boostLevel > 0) {
			watk += expert.getEffect(boostLevel).getX();
		}
		break;
		}
		case 312: { // Bowmaster
		final ISkill expert = SkillFactory.getSkill(3120005);
		final int boostLevel = chra.getSkillLevel(expert);
		if (boostLevel > 0) {
			watk += expert.getEffect(boostLevel).getX();
		}
		break;
		}
		case 211:
		case 212: { // IL
		final ISkill amp = SkillFactory.getSkill(2110001);
		final int level = chra.getSkillLevel(amp);
		if (level > 0) {
			element_amp_percent = amp.getEffect(level).getY();
		}
		break;
		}
		case 221:
		case 222: { // IL
		final ISkill amp = SkillFactory.getSkill(2210001);
		final int level = chra.getSkillLevel(amp);
		if (level > 0) {
			element_amp_percent = amp.getEffect(level).getY();
		}
		break;
		}
		case 1211:
		case 1212: { // flame
		final ISkill amp = SkillFactory.getSkill(12110001);
		final int level = chra.getSkillLevel(amp);
		if (level > 0) {
			element_amp_percent = amp.getEffect(level).getY();
		}
		break;
		}
		case 2215:
		case 2216:
		case 2217:
		case 2218: {
		final ISkill amp = SkillFactory.getSkill(22150000);
		final int level = chra.getSkillLevel(amp);
		if (level > 0) {
			element_amp_percent = amp.getEffect(level).getY();
		}
		break;
		}
		case 2112: { // Aran
		final ISkill expert = SkillFactory.getSkill(21120001);
		final int boostLevel = chra.getSkillLevel(expert);
		if (boostLevel > 0) {
			watk += expert.getEffect(boostLevel).getX();
		}
		break;
		}
	}
	final ISkill blessoffairy = SkillFactory.getSkill(GameConstants.getBOF_ForJob(chra.getJob()));
	final int boflevel = chra.getSkillLevel(blessoffairy);
	if (boflevel > 0) {
		watk += blessoffairy.getEffect(boflevel).getX();
		magic += blessoffairy.getEffect(boflevel).getY();
	}
	buff = chra.getBuffedValue(MapleBuffStat.ACC);
	if (buff != null) {
		accuracy += buff.intValue();
	}
	buff = chra.getBuffedValue(MapleBuffStat.WATK);
	if (buff != null) {
		watk += buff.intValue();
	}
	buff = chra.getBuffedValue(MapleBuffStat.MATK);
	if (buff != null) {
		magic += buff.intValue();
	}
	buff = chra.getBuffedValue(MapleBuffStat.SPEED);
	if (buff != null) {
		speed += buff.intValue();
	}
	buff = chra.getBuffedValue(MapleBuffStat.JUMP);
	if (buff != null) {
		jump += buff.intValue();
	}
	buff = chra.getBuffedValue(MapleBuffStat.DASH_SPEED);
	if (buff != null) {
		speed += buff.intValue();
	}
	buff = chra.getBuffedValue(MapleBuffStat.DASH_JUMP);
	if (buff != null) {
		jump += buff.intValue();
	}
	if (speed > 140) {
		speed = 140;
	}
	if (jump > 123) {
		jump = 123;
	}
	speedMod = speed / 100.0f;
	jumpMod = jump / 100.0f;
	Integer mount = chra.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
	if (mount != null) {
		jumpMod = 1.23f;
		switch (mount.intValue()) {
		case 1:
			speedMod = 1.5f;
			break;
		case 2:
			speedMod = 1.7f;
			break;
		case 3:
			speedMod = 1.8f;
			break;
		default:
			System.err.println("Unhandeled monster riding level, Speedmod = " + speedMod + "");
		}
	}
	hands = this.localdex + this.localint_ + this.localluk;

	magic = Math.min(magic, MapleCharacter.magicCap);
	localmaxhp = Math.min(30000, localmaxhp);
	localmaxmp = Math.min(30000, localmaxmp);

	CalcPassive_SharpEye(chra);

	localmaxbasedamage = calculateMaxBaseDamage(watk);
	if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
		chra.updatePartyMemberHP();
	}
	}

	private final void CalcPassive_SharpEye(final MapleCharacter player) {
	switch (player.getJob()) { // Apply passive Critical bonus
		case 410:
		case 411:
		case 412: { // Assasin/ Hermit / NL
		final ISkill critSkill = SkillFactory.getSkill(4100001);
		final int critlevel = player.getSkillLevel(critSkill);
		if (critlevel > 0) {
			this.passive_sharpeye_percent = (short) (critSkill.getEffect(critlevel).getDamage() - 100);
			this.passive_sharpeye_rate = critSkill.getEffect(critlevel).getProb();
			return;
		}
		break;
		}
		case 1410:
		case 1411:
		case 1412: { // Night Walker
		final ISkill critSkill = SkillFactory.getSkill(14100001);
		final int critlevel = player.getSkillLevel(critSkill);
		if (critlevel > 0) {
			this.passive_sharpeye_percent = (short) (critSkill.getEffect(critlevel).getDamage() - 100);
			this.passive_sharpeye_rate = critSkill.getEffect(critlevel).getProb();
			return;
		}
		break;
		}
		case 511:
		case 512: { // Buccaner, Viper
		final ISkill critSkill = SkillFactory.getSkill(5110000);
		final int critlevel = player.getSkillLevel(critSkill);
		if (critlevel > 0) {
			this.passive_sharpeye_percent = (short) (critSkill.getEffect(critlevel).getDamage() - 100);
			this.passive_sharpeye_rate = critSkill.getEffect(critlevel).getProb();
			return;
		}
		break;
		}
		case 1511:
		case 1512: {
		final ISkill critSkill = SkillFactory.getSkill(15110000);
		final int critlevel = player.getSkillLevel(critSkill);
		if (critlevel > 0) {
			this.passive_sharpeye_percent = (short) (critSkill.getEffect(critlevel).getDamage() - 100);
			this.passive_sharpeye_rate = critSkill.getEffect(critlevel).getProb();
			return;
		}
		break;
		}
		case 2111:
		case 2112: { // Aran, TODO : only applies when there's > 10 combo
		final ISkill critSkill = SkillFactory.getSkill(21110000);
		final int critlevel = player.getSkillLevel(critSkill);
		if (critlevel > 0) {
			this.passive_sharpeye_percent = (short) (critSkill.getEffect(critlevel).getDamage());
			this.passive_sharpeye_rate = critSkill.getEffect(critlevel).getProb();
			return;
		}
		break;
		}
		case 300:
		case 310:
		case 311:
		case 312:
		case 320:
		case 321:
		case 322: { // Bowman
		final ISkill critSkill = SkillFactory.getSkill(3000001);
		final int critlevel = player.getSkillLevel(critSkill);
		if (critlevel > 0) {
			this.passive_sharpeye_percent = (short) (critSkill.getEffect(critlevel).getDamage() - 100);
			this.passive_sharpeye_rate = critSkill.getEffect(critlevel).getProb();
			return;
		}
		break;
		}
		case 1300:
		case 1310:
		case 1311:
		case 1312: { // Bowman
		final ISkill critSkill = SkillFactory.getSkill(13000000);
		final int critlevel = player.getSkillLevel(critSkill);
		if (critlevel > 0) {
			this.passive_sharpeye_percent = (short) (critSkill.getEffect(critlevel).getDamage() - 100);
			this.passive_sharpeye_rate = critSkill.getEffect(critlevel).getProb();
			return;
		}
		break;
		}
		case 2214:
		case 2215:
		case 2216:
		case 2217:
		case 2218: { //Evan
		final ISkill critSkill = SkillFactory.getSkill(22140000);
		final int critlevel = player.getSkillLevel(critSkill);
		if (critlevel > 0) {
			this.passive_sharpeye_percent = (short) (critSkill.getEffect(critlevel).getDamage() - 100);
			this.passive_sharpeye_rate = critSkill.getEffect(critlevel).getProb();
			return;
		}
		break;
		}
	}
	this.passive_sharpeye_percent = 0;
	this.passive_sharpeye_rate = 0;
	}

	public final short passive_sharpeye_percent() {
	return passive_sharpeye_percent;
	}

	public final short passive_sharpeye_rate() {
	return passive_sharpeye_rate;
	}

	public final float calculateMaxBaseDamage(final int watk) {
	final MapleCharacter chra = chr.get();
	if (chra == null) {
		return 0;
	}
	float maxbasedamage;
	if (watk == 0) {
		maxbasedamage = 1;
	} else {
		final IItem weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);

		if (weapon_item != null) {
		final int job = chra.getJob();
		final MapleWeaponType weapon = GameConstants.getWeaponType(weapon_item.getItemId());
		int mainstat, secondarystat;

		switch (weapon) {
			case BOW:
			case CROSSBOW:
			mainstat = localdex;
			secondarystat = localstr;
			break;
			case CLAW:
			case DAGGER:
			if ((job >= 400 && job <= 422) || (job >= 1400 && job <= 1412)) {
				mainstat = localluk;
				secondarystat = localdex + localstr;
			} else { // Non Thieves
				mainstat = localstr;
				secondarystat = localdex;
			}
			break;
			case KNUCKLE:
			mainstat = localstr;
			secondarystat = localdex;
			break;
			case GUN:
			mainstat = localdex;
			secondarystat = localstr;
			break;
			case NOT_A_WEAPON:
			if ((job >= 500 && job <= 522) || (job >= 1500 && job <= 1512)) {
				mainstat = localstr;
				secondarystat = localdex;
			} else {
				mainstat = 0;
				secondarystat = 0;
			}
			break;
			default:
			mainstat = localstr;
			secondarystat = localdex;
			break;
		}
		maxbasedamage = ((weapon.getMaxDamageMultiplier() * mainstat) + secondarystat) * watk / 100;
		} else {
		maxbasedamage = 0;
		}
	}
	return maxbasedamage;
	}

	public final float getHealHP() {
	return shouldHealHP;
	}

	public final float getHealMP() {
	return shouldHealMP;
	}

	public final void relocHeal() {
	final MapleCharacter chra = chr.get();
	if (chra == null) {
		return;
	}
	final int playerjob = chra.getJob();

	shouldHealHP = 10; // Reset
	shouldHealMP = 3;

	if (GameConstants.isJobFamily(200, playerjob)) { // Improving MP recovery
		shouldHealMP += ((float) ((float) chra.getSkillLevel(SkillFactory.getSkill(2000000)) / 10) * chra.getLevel());

	} else if (GameConstants.isJobFamily(111, playerjob)) {
		final ISkill effect = SkillFactory.getSkill(1110000); // Improving MP Recovery
		final int lvl = chra.getSkillLevel(effect);
		if (lvl > 0) {
		shouldHealMP += effect.getEffect(lvl).getMp();
		}

	} else if (GameConstants.isJobFamily(121, playerjob)) {
		final ISkill effect = SkillFactory.getSkill(1210000); // Improving MP Recovery
		final int lvl = chra.getSkillLevel(effect);
		if (lvl > 0) {
		shouldHealMP += effect.getEffect(lvl).getMp();
		}

	} else if (GameConstants.isJobFamily(1111, playerjob)) {
		final ISkill effect = SkillFactory.getSkill(11110000); // Improving MP Recovery
		final int lvl = chra.getSkillLevel(effect);
		if (lvl > 0) {
		shouldHealMP += effect.getEffect(lvl).getMp();
		}

	} else if (GameConstants.isJobFamily(410, playerjob)) {
		final ISkill effect = SkillFactory.getSkill(4100002); // Endure
		final int lvl = chra.getSkillLevel(effect);
		if (lvl > 0) {
		shouldHealHP += effect.getEffect(lvl).getHp();
		shouldHealMP += effect.getEffect(lvl).getMp();
		}

	} else if (GameConstants.isJobFamily(420, playerjob)) {
		final ISkill effect = SkillFactory.getSkill(4200001); // Endure
		final int lvl = chra.getSkillLevel(effect);
		if (lvl > 0) {
		shouldHealHP += effect.getEffect(lvl).getHp();
		shouldHealMP += effect.getEffect(lvl).getMp();
		}
	}
	if (chra.isGM()) {
		shouldHealHP += 1000;
		shouldHealMP += 1000;
	}
	if (chra.getChair() != 0) { // Is sitting on a chair.
		shouldHealHP += 99; // Until the values of Chair heal has been fixed,
		shouldHealMP += 99; // MP is different here, if chair data MP = 0, heal + 1.5
	} else { // Because Heal isn't multipled when there's a chair :)
		final float recvRate = chra.getMap().getRecoveryRate();
		shouldHealHP *= recvRate;
		shouldHealMP *= recvRate;
	}
	shouldHealHP *= 2; // To avoid any problem with bathrobe / Sauna >.<
	shouldHealMP *= 2; // 1.5
	}

	public final void connectData(final MaplePacketLittleEndianWriter mplew) {
		mplew.writeShort(str); // str
		mplew.writeShort(dex); // dex
		mplew.writeShort(int_); // int
		mplew.writeShort(luk); // luk
		mplew.writeInt(hp); // hp
		mplew.writeInt(maxhp); // maxhp
		mplew.writeInt(mp); // mp
		mplew.writeInt(maxmp); // maxmp
	}
}