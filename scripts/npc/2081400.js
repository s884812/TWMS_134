/*  NPC : Hellin
	Thief 4th job advancement
	Forest of the priest (240010501)
 */

var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }

    if (status == 0) {
	if (!(cm.getJob() == 411 || cm.getJob() == 421)) {
	    cm.sendOk("Why do you want to see me? There is nothing you want to ask me.");
	    cm.safeDispose();
	    return;
	} else if (cm.getPlayerStat("LVL") < 120) {
	    cm.sendOk("You're still weak to go to thief extreme road. If you get stronger, come back to me.");
	    cm.safeDispose();
	    return;
	} else {
	    if (cm.getQuestStatus(6934) == 2) {
		if (cm.getJob() == 411)
		    cm.sendSimple("You're qualified to be a true thief. \r\nDo you want job advancement?\r\n#b#L0# I want to advance to Night Lord.#l\r\n#b#L1#  Let me think for a while.#l");
		else
		    cm.sendSimple("You're qualified to be a true thief. \r\nDo you want job advancement?\r\n#b#L0# I want to advance to Shadower.#l\r\n#b#L1#  Let me think for a while.#l");
	    } else {
		cm.sendOk("You're not ready to make 4th job advancement. When you're ready, talk to me.");
		cm.safeDispose();
		return;
	    }
	}
    } else if (status == 1) {
	if (selection == 1) {
	    cm.sendOk("You don't have to hesitate.... Whenever you decide, talk to me. If you're ready, I'll let you make the 4th job advancement.");
	    cm.safeDispose();
	    return;
	}
	if (cm.getPlayerStat("RSP") > (cm.getPlayerStat("LVL") - 120) * 3) {
	    cm.sendOk("Hmm...You have too many #bSP#k. You can't make the 4th job advancement with too many SP left.");
	    cm.dispose();
	    return;
	} else {
	    if (cm.canHold(2280003)) {
		cm.gainAp(5);
		cm.gainItem(2280003, 1);

		if (cm.getJob() == 411) {
		    cm.changeJob(412);
		    cm.teachSkill(4120002,0,10);
		    cm.teachSkill(4121006,0,10);
		    cm.teachSkill(4120005,0,10);
		    cm.sendNext("You became the best thief #bNight Lord#k. Night Lord is good at using #bFake#k to avoid enemy's attack and #bNinja Ambush#k to call hidden colleagues. It attacks the blind side of enemy. ");
		} else if (cm.getJob() == 421) {
		    cm.changeJob(422);
		    cm.teachSkill(4220002,0,10);
		    cm.teachSkill(4221007,0,10);
		    cm.teachSkill(4220005,0,10);
		    cm.sendNext("You became the best thief #bShadower#k. Night Lord is good at using #bFake#k to avoid enemy's attack and #bNinja Ambush#k to call hidden colleagues. It attacks the blind side of enemy. ");
		}
	    } else {
		cm.sendOk("You can't proceed as you don't have an empty slot in your inventory. Please clear your inventory and try again.");
		cm.safeDispose();
		return;
	    }
	}
    } else if (status == 2) {
	if (cm.getJob() == 412) {
	    cm.sendNext("This is not all about Night Lord. Night Lord is good at fast war. It can throw many stars at one time and may beat off plenty of enemies at once.");
	} else {
	    cm.sendNext("This is not all about Shadower. Shadower is good at sudden attack. It can attack enemies before they notice and even beat them locked in the darkness.");
	}
    } else if (status == 3) {
	cm.sendNextPrev("Don't forget that it all depends on how much you train.");
    } else if (status == 4) {
	cm.dispose();
    }
}