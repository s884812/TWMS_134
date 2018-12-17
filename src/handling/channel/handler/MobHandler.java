package handling.channel.handler;

import java.awt.Point;
import java.util.List;

import client.MapleClient;
import client.MapleCharacter;
import server.Randomizer;
import server.maps.MapleMap;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.movement.LifeMovementFragment;
import tools.Pair;
import tools.packet.MobPacket;
import tools.data.input.SeekableLittleEndianAccessor;

public class MobHandler {

	public static final void MoveMonster(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final MapleMonster monster = chr.getMap().getMonsterByOid(slea.readInt());

		if (monster == null) { // movin something which is not a monster
			return;
		}
		final short moveid = slea.readShort();
		final boolean useSkill = slea.readByte() > 0;
		final byte skill = slea.readByte();
		final int skill1 = slea.readByte() & 0xFF; // unsigned?
		final int skill2 = slea.readByte();
		final int skill3 = slea.readByte();
		final int skill4 = slea.readByte();
		int realskill = 0;
		int level = 0;

		if (useSkill) {
			final byte size = monster.getNoSkills();
			boolean used = false;

			if (size > 0) {
				final Pair<Integer, Integer> skillToUse = monster.getSkills().get((byte) Randomizer.nextInt(size));
				realskill = skillToUse.getLeft();
				level = skillToUse.getRight();
				// Skill ID and Level
				final MobSkill mobSkill = MobSkillFactory.getMobSkill(realskill, level);

				if (!mobSkill.checkCurrentBuff(chr, monster)) {
					final long now = System.currentTimeMillis();
					final long ls = monster.getLastSkillUsed(realskill);

					if (ls == 0 || ((now - ls) > mobSkill.getCoolTime())) {
						monster.setLastSkillUsed(realskill, now, mobSkill.getCoolTime());

						final int reqHp = (int) (((float) monster.getHp() / monster.getMobMaxHp()) * 100); // In case this monster have 2.1b and above HP
						if (reqHp <= mobSkill.getHP()) {
							used = true;
							mobSkill.applyEffect(chr, monster, true);
						}
					}
				}
			}
			if (!used) {
				realskill = 0;
				level = 0;
			}
		}
		slea.skip(33);
		final Point startPos = monster.getPosition();
		final List<LifeMovementFragment> res = MovementParse.parseMovement(slea);

		c.getSession().write(MobPacket.moveMonsterResponse(monster.getObjectId(), moveid, monster.getMp(), monster.isControllerHasAggro(), realskill, level));

		if (res != null) {
			/*if (slea.available() != 9 && slea.available() != 17) { //9.. 0 -> endPos? -> endPos again? -> 0 -> 0
				System.out.println(":: slea.available() != 9 && slea.available() != 17 | movement parsing error ::");
				c.getSession().close();
				return;
			}*/
			final MapleMap map = c.getPlayer().getMap();
			MovementParse.updatePosition(res, monster, -1);
			map.moveMonster(monster, monster.getPosition());
			map.broadcastMessage(chr, MobPacket.moveMonster(useSkill, skill, skill1, skill2, skill3, skill4, monster.getObjectId(), startPos, res), monster.getPosition());
			chr.getCheatTracker().checkMoveMonster(monster.getPosition());
		}
	}

	public static final void FriendlyDamage(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
		final MapleMap map = chr.getMap();
		final MapleMonster mobfrom = map.getMonsterByOid(slea.readInt());
		slea.skip(4); // Player ID
		final MapleMonster mobto = map.getMonsterByOid(slea.readInt());

		if (mobfrom != null && mobto != null && mobto.getStats().isFriendly()) {
			final int damage = (mobto.getStats().getLevel() * Randomizer.nextInt(99)) / 2; // Temp for now until I figure out something more effective
			mobto.damage(chr, damage, true);
		}
	}

	public static final void MonsterBomb(final int oid, final MapleCharacter chr) {
		final MapleMonster monster = chr.getMap().getMonsterByOid(oid);

		if (monster == null || !chr.isAlive() || chr.isHidden()) {
			return;
		}
		final byte selfd = monster.getStats().getSelfD();
		if (selfd != -1) {
			chr.getMap().killMonster(monster, chr, false, false, selfd);
		}
	}

	public static final void AutoAggro(final int monsteroid, final MapleCharacter chr) {
		final MapleMonster monster = chr.getMap().getMonsterByOid(monsteroid);

		if (monster != null && chr.getPosition().distance(monster.getPosition()) < 200000) {
			if (monster.getController() != null) {
				if (chr.getMap().getCharacterById_InMap(monster.getController().getId()) == null) {
					monster.switchController(chr, true);
				} else {
					monster.switchController(monster.getController(), true);
				}
			} else {
				monster.switchController(chr, true);
			}
		}
	}

	public static final void HypnotizeDmg(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
		final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
		slea.skip(4); // Player ID
		final int to = slea.readInt(); // mobto
		slea.skip(1); // Same as player damage, -1 = bump, integer = skill ID
		final int damage = slea.readInt();
		//slea.skip(1); // Facing direction
		//slea.skip(4); // Some type of pos, damage display, I think

		final MapleMonster mob_to = chr.getMap().getMonsterByOid(to);

		if (mob_from != null && mob_to != null) {
			if (damage > 30000) {
				return;
			}
			mob_to.damage(chr, damage, true);
			// TODO : Get the real broadcast damage packet
			chr.getMap().broadcastMessage(chr, MobPacket.damageMonster(to, damage), false);
		}
	}
}