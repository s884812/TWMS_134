package handling.channel.handler;

import java.util.List;
import java.awt.Point;

import client.ISkill;
import client.GameConstants;
import client.MapleCharacter;
import client.SkillFactory;
import server.MapleStatEffect;
import server.AutobanManager;
import tools.AttackPair;

public class AttackInfo {

    public int skill, charge, lastAttackTickCount;
    public List<AttackPair> allDamage;
    public Point position;
    public byte hits, targets, tbyte, display, animation, speed, csstar, AOE, slot;

    public final MapleStatEffect getAttackEffect(final MapleCharacter chr, int skillLevel, final ISkill skill_) {
	if (GameConstants.isMulungSkill(skill)) {
	    skillLevel = 1;
	} else if (skillLevel == 0) {
	    return null;
	}
	if (GameConstants.isLinkedAranSkill(skill)) {
	    final ISkill skillLink = SkillFactory.getSkill(skill);
	    if (display > 80) {
		if (!skillLink.getAction()) {
		    //AutobanManager.getInstance().autoban(chr.getClient(), "No delay hack, SkillID : " + skill);
		    return null;
		}
	    }
	    return skillLink.getEffect(chr, skillLevel);
	}
	if (display > 80) {
	    if (!skill_.getAction()) {
		//AutobanManager.getInstance().autoban(chr.getClient(), "No delay hack, SkillID : " + skill);
		return null;
	    }
	}
	return skill_.getEffect(chr, skillLevel);
    }
}
