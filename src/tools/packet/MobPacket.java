package tools.packet;

import java.awt.Point;
import java.util.Collection;
import java.util.Map;
import java.util.List;

import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import server.life.MapleMonster;
import server.movement.LifeMovementFragment;
import tools.HexTool;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MobPacket {

	public static MaplePacket damageMonster(final int oid, final int damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.writeInt(damage);

		return mplew.getPacket();
	}

	public static MaplePacket damageFriendlyMob(final MapleMonster mob, final int damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(mob.getObjectId());
		mplew.write(1);
		mplew.writeInt(damage);
		mplew.writeInt(mob.getHp());
		mplew.writeInt(mob.getMobMaxHp());

		return mplew.getPacket();
	}

	public static MaplePacket killMonster(final int oid, final int animation) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(animation); // 0 = dissapear, 1 = fade out, 2+ = special

		return mplew.getPacket();
	}

	public static MaplePacket healMonster(final int oid, final int heal) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(1);
		mplew.writeInt(-heal);

		return mplew.getPacket();
	}

	public static MaplePacket showMonsterHP(int oid, int remhppercentage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
		mplew.writeInt(oid);
		mplew.write(remhppercentage);

		return mplew.getPacket();
	}

	public static MaplePacket showBossHP(final MapleMonster mob) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(5);
		mplew.writeInt(mob.getId());
		mplew.writeInt(mob.getHp());
		mplew.writeInt(mob.getMobMaxHp());
		mplew.write(mob.getStats().getTagColor());
		mplew.write(mob.getStats().getTagBgColor());

		return mplew.getPacket();
	}

	public static MaplePacket moveMonster(boolean useskill, int skill, int skill1, int skill2, int skill3, int skill4, int oid, Point startPos, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.writeShort(0);
		mplew.write(useskill ? 1 : 0);
		mplew.write(skill);
		mplew.write(skill1);
		mplew.write(skill2);
		mplew.write(skill3);
		mplew.write(skill4);
		mplew.writeZeroBytes(8);
		mplew.writePos(startPos);
		mplew.writeInt(4275593);
		serializeMovementList(mplew, moves);

		return mplew.getPacket();
	}

	private static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
		lew.write(moves.size());
		for (LifeMovementFragment move : moves) {
			move.serialize(lew);
		}
	}

	public static MaplePacket spawnMonster(MapleMonster life, int spawnType, int effect, int link) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
		mplew.writeInt(life.getObjectId());
		mplew.write(1); // 1 = Control normal, 5 = Control none
		mplew.writeInt(life.getId());
		mplew.writeZeroBytes(20);
                mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 E0 00 00 00"));
                mplew.write(HexTool.getByteArrayFromHexString("00 00 00 88 00 00 00 00"));
                mplew.write(HexTool.getByteArrayFromHexString("00 00 22 76 00 00 00 00")); //?
                mplew.write(HexTool.getByteArrayFromHexString("00 00 22 76 00 00 00 00")); //?
                mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getCy());
		mplew.write(life.getStance());
		mplew.writeShort(life.getFh()); // FH?
		mplew.writeShort(life.getFh()); // FH
		if (effect != 0 || link != 0) {
			mplew.write(effect != 0 ? effect : -3);
			mplew.writeInt(link);
		} else {
			if (spawnType == 0) {
				mplew.write(effect);
				mplew.write(0);
				mplew.writeShort(0);
			}
			mplew.write(spawnType); // newSpawn ? -1 : -2
		}
		mplew.write(life.getCarnivalTeam());
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static MaplePacket controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(aggro ? 2 : 1);
		mplew.writeInt(life.getObjectId());
		mplew.write(1); // 1 = Control normal, 5 = Control none
		mplew.writeInt(life.getId());
		mplew.writeZeroBytes(20);
                mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 E0 00 00 00"));
                mplew.write(HexTool.getByteArrayFromHexString("00 00 00 88 00 00 00 00"));
                mplew.write(HexTool.getByteArrayFromHexString("00 00 22 76 00 00 00 00")); //?
                mplew.write(HexTool.getByteArrayFromHexString("00 00 22 76 00 00 00 00")); //?
                mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getCy());
		mplew.write(life.getStance());
		mplew.writeShort(life.getFh()); // FH?
		mplew.writeShort(life.getFh()); // FH
		mplew.write(life.isFake() ? 0xfc : newSpawn ? -1 : -2);
		mplew.write(life.getCarnivalTeam());
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static MaplePacket stopControllingMonster(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(0);
		mplew.writeInt(oid);

		return mplew.getPacket();
	}

	public static MaplePacket makeMonsterInvisible(MapleMonster life) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(0);
		mplew.writeInt(life.getObjectId());

		return mplew.getPacket();
	}

	public static MaplePacket makeMonsterReal(MapleMonster life) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
		mplew.writeInt(life.getObjectId());
		mplew.write(1); // 1 = Control normal, 5 = Control none
		mplew.writeInt(life.getId());
		mplew.writeZeroBytes(15); // Added on v.82 MSEA
		mplew.write(0x88);
		mplew.writeZeroBytes(6);
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getPosition().y);
		mplew.write(life.getStance());
		mplew.writeShort(0); // FH
		mplew.writeShort(life.getFh()); // Origin FH
		mplew.writeShort(-1);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
		mplew.writeInt(objectid);
		mplew.writeShort(moveid);
		mplew.write(useSkills ? 1 : 0);
		mplew.writeShort(currentMp);
		mplew.write(skillId);
		mplew.write(skillLevel);
                mplew.writeInt(0);

		return mplew.getPacket();
	}

	private static long getSpecialLongMask(Collection<MonsterStatus> statups) {
		long mask = 0;
		for (MonsterStatus statup : statups) {
			if (statup.isFirst()) {
				mask |= statup.getValue();
			}
		}
		return mask;
	}

	private static long getLongMask(Collection<MonsterStatus> statups) {
		long mask = 0;
		for (MonsterStatus statup : statups) {
			if (!statup.isFirst()) {
				mask |= statup.getValue();
			}
		}
		return mask;
	}

	private static void writeIntMask(MaplePacketLittleEndianWriter mplew, Map<MonsterStatus, Integer> stats) {
		mplew.writeZeroBytes(9); // v104
		mplew.writeLong(getSpecialLongMask(stats.keySet()));
		mplew.writeLong(getLongMask(stats.keySet()));
	}

	public static MaplePacket applyMonsterStatus(final int oid, final MonsterStatusEffect mse) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		writeIntMask(mplew, mse.getStati());
		for (Map.Entry<MonsterStatus, Integer> stat : mse.getStati().entrySet()) {
			mplew.writeShort(stat.getValue());
			if (mse.isMonsterSkill()) {
				mplew.writeShort(mse.getMobSkill().getSkillId());
				mplew.writeShort(mse.getMobSkill().getSkillLevel());
			} else {
				mplew.writeInt(mse.getSkill().getId());
			}
			mplew.writeShort(0); // might actually be the buffTime but it's not displayed anywhere
		}
		mplew.writeShort(0); // delay in ms
		mplew.write(mse.getStati().size()); // size
		mplew.write(1);

		return mplew.getPacket();
	}

	public static MaplePacket applyMonsterStatus(final int oid, final MonsterStatusEffect mse, final List<Integer> reflection) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		writeIntMask(mplew, mse.getStati());
		for (Map.Entry<MonsterStatus, Integer> stat : mse.getStati().entrySet()) {
			mplew.writeShort(stat.getValue());
			if (mse.isMonsterSkill()) {
				mplew.writeShort(mse.getMobSkill().getSkillId());
				mplew.writeShort(mse.getMobSkill().getSkillLevel());
			} else {
				mplew.writeInt(mse.getSkill().getId());
			}
			mplew.writeShort(0); // might actually be the buffTime but it's not displayed anywhere
		}
		for (Integer ref : reflection) {
			mplew.writeInt(ref);
		}
		mplew.writeInt(0);
		mplew.writeShort(0); // delay in ms
		int size = mse.getStati().size(); // size
		if (reflection.size() > 0) {
			size /= 2; // This gives 2 buffs per reflection but it's really one buff
		}
		mplew.write(size); // size
		mplew.write(1);

		return mplew.getPacket();
	}

	public static MaplePacket cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		writeIntMask(mplew, stats);
		mplew.write(1);
		mplew.write(2);

		return mplew.getPacket();
	}
}