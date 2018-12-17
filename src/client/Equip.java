package client;

import java.io.Serializable;

public class Equip extends Item implements IEquip, Serializable {

	private byte upgradeSlots, level, vicioushammer, itemLevel, potential, pstars;
	private short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, itemEXP, potential_1, potential_2, potential_3;
	private int ringid, job;

	public Equip(int id, short position, byte flag) {
		super(id, position, (short) 1, flag);
		this.ringid = -1;
	}

	public Equip(int id, short position, int ringid, byte flag) {
		super(id, position, (short) 1, flag);
		this.ringid = ringid;
	}

	@Override
	public IItem copy() {
		Equip ret = new Equip(getItemId(), getPosition(), ringid, getFlag());
		ret.str = str;
		ret.dex = dex;
		ret._int = _int;
		ret.luk = luk;
		ret.hp = hp;
		ret.mp = mp;
		ret.matk = matk;
		ret.mdef = mdef;
		ret.watk = watk;
		ret.wdef = wdef;
		ret.acc = acc;
		ret.avoid = avoid;
		ret.hands = hands;
		ret.speed = speed;
		ret.jump = jump;
		ret.upgradeSlots = upgradeSlots;
		ret.level = level;
		ret.itemEXP = itemEXP;
		ret.itemLevel = itemLevel;
		ret.vicioushammer = vicioushammer;
		ret.potential = potential;
		ret.pstars = pstars;
		ret.potential_1 = potential_1;
		ret.potential_2 = potential_2;
		ret.potential_3 = potential_3;
		ret.setOwner(getOwner());
		ret.setQuantity(getQuantity());
		ret.setExpiration(getExpiration());
		return ret;
	}

	@Override
	public byte getType() {
		return 1;
	}

	@Override
	public byte getUpgradeSlots() {
		return upgradeSlots;
	}

	@Override
	public int getRingId() {
		return ringid;
	}

	@Override
	public short getStr() {
		return str;
	}

	@Override
	public short getDex() {
		return dex;
	}

	@Override
	public short getInt() {
		return _int;
	}

	@Override
	public short getLuk() {
		return luk;
	}

	@Override
	public short getHp() {
		return hp;
	}

	@Override
	public short getMp() {
		return mp;
	}

	@Override
	public short getWatk() {
		return watk;
	}

	@Override
	public short getMatk() {
		return matk;
	}

	@Override
	public short getWdef() {
		return wdef;
	}

	@Override
	public short getMdef() {
		return mdef;
	}

	@Override
	public short getAcc() {
		return acc;
	}

	@Override
	public short getAvoid() {
		return avoid;
	}

	@Override
	public short getHands() {
		return hands;
	}

	@Override
	public short getSpeed() {
		return speed;
	}

	@Override
	public short getJump() {
		return jump;
	}

	public int getJob() {
		return job;
	}

	public void setStr(short str) {
		if (str < 0) {
			str = 0;
		}
		this.str = str;
	}

	public void setDex(short dex) {
		if (dex < 0) {
			dex = 0;
		}
		this.dex = dex;
	}

	public void setInt(short _int) {
		if (_int < 0) {
			_int = 0;
		}
		this._int = _int;
	}

	public void setLuk(short luk) {
		if (luk < 0) {
			luk = 0;
		}
		this.luk = luk;
	}

	public void setHp(short hp) {
		if (hp < 0) {
			hp = 0;
		}
		this.hp = hp;
	}

	public void setMp(short mp) {
		if (mp < 0) {
			mp = 0;
		}
		this.mp = mp;
	}

	public void setWatk(short watk) {
		if (watk < 0) {
			watk = 0;
		}
		this.watk = watk;
	}

	public void setMatk(short matk) {
		if (matk < 0) {
			matk = 0;
		}
		this.matk = matk;
	}

	public void setWdef(short wdef) {
		if (wdef < 0) {
			wdef = 0;
		} else if (wdef > 255) {
			wdef = 255;
		}
		this.wdef = wdef;
	}

	public void setMdef(short mdef) {
		if (mdef < 0) {
			mdef = 0;
		} else if (mdef > 255) {
			mdef = 255;
		}
		this.mdef = mdef;
	}

	public void setAcc(short acc) {
		if (acc < 0) {
			acc = 0;
		}
		this.acc = acc;
	}

	public void setAvoid(short avoid) {
		if (avoid < 0) {
			avoid = 0;
		}
		this.avoid = avoid;
	}

	public void setHands(short hands) {
		if (hands < 0) {
			hands = 0;
		}
		this.hands = hands;
	}

	public void setSpeed(short speed) {
		if (speed < 0) {
			speed = 0;
		}
		this.speed = speed;
	}

	public void setJump(short jump) {
		if (jump < 0) {
			jump = 0;
		}
		this.jump = jump;
	}

	public void setUpgradeSlots(byte upgradeSlots) {
		this.upgradeSlots = upgradeSlots;
	}

	@Override
	public byte getLevel() {
		return level;
	}

	public void setLevel(byte level) {
		this.level = level;
	}

	@Override
	public byte getViciousHammer() {
		return vicioushammer;
	}

	public void setViciousHammer(byte ham) {
		vicioushammer = ham;
	}

	@Override
	public byte getItemLevel() {
		return itemLevel;
	}

	public void setItemLevel(byte itemLevel) {
		if (itemLevel < 0) {
			itemLevel = 0;
		}
		this.itemLevel = itemLevel;
	}

	@Override
	public byte getPotential() {
		return potential;
	}

	public void setPotential(byte potential) {
		this.potential = potential;
	}

	@Override
	public byte getPStars() {
		return pstars;
	}

	public void setPStars(byte pstars) {
		this.pstars = pstars;
	}

	@Override
	public short getItemEXP() {
		return itemEXP;
	}

	public void setItemEXP(short itemEXP) {
		if (itemEXP < 0) {
			itemEXP = 0;
		}
		this.itemEXP = itemEXP;
	}

	@Override
	public short getPotential_1() {
		return potential_1;
	}

	public void setPotential_1(short potential_1) {
		this.potential_1 = potential_1;
	}

	@Override
	public short getPotential_2() {
		return potential_2;
	}

	public void setPotential_2(short potential_2) {
		this.potential_2 = potential_2;
	}

	@Override
	public short getPotential_3() {
		return potential_3;
	}

	public void setPotential_3(short potential_3) {
		this.potential_3 = potential_3;
	}

	@Override
	public void setQuantity(short quantity) {
		if (quantity < 0 || quantity > 1) {
			throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
		}
		super.setQuantity(quantity);
	}

	public void setJob(int job) {
		this.job = job;
	}

	public void setRingId(int ringId) {
		this.ringid = ringId;
	}
}