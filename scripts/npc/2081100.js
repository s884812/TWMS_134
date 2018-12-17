/*  NPC : Harmonia
	Warrior 4th job advancement
	Forest of the priest (240010501)
*/

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 0) {
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;

    if (status == 0) {
	if (!(cm.getJob() == 111 || cm.getJob() == 121 || cm.getJob() == 131)) {
	    cm.sendOk("Why do you want to see me? There is nothing you want to ask me.");
	    cm.dispose();
	    return;
	} else if (cm.getPlayerStat("LVL") < 120) {
	    cm.sendOk("You're still weak to go to warrior extreme road. If you get stronger, come back to me.");
	    cm.dispose();
	    return;
	} else {
	    if (cm.getQuestStatus(6904) == 2) {
		if (cm.getJob() == 111)
		    cm.sendSimple("You're qualified to be a true warrior. \r\nDo you want job advancement?\r\n#b#L0# I want to advance to Hero.#l\r\n#b#L1#  Let me think for a while.#l");
		else if (cm.getJob() == 121)
		    cm.sendSimple("You're qualified to be a true warrior. \r\nDo you want job advancement?\r\n#b#L0# I want to advance to Paladin.#l\r\n#b#L1#  Let me think for a while.#l");
		else
		    cm.sendSimple("You're qualified to be a true warrior. \r\nDo you want job advancement?\r\n#b#L0# I want to advance to Dark Knight.#l\r\n#b#L1#  Let me think for a while.#l");
	    } else {
		cm.sendOk("You're not ready to make 4th job advancement. When you're ready, talk to me.");
		cm.dispose();
		return;
	    }
	}
    } else if (status == 1) {
	if (selection == 1) {
	    cm.sendOk("You don't have to hesitate to be the best Warrior..Whenever you make your decision, talk to me. If you're ready, I'll let you make the 4th job advancement.");
	    cm.dispose();
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

		if (cm.getJob() == 111) {
		    cm.changeJob(112);
		    cm.teachSkill(1121001, 0, 10); // Monster Magnet
		    cm.teachSkill(1120004, 0, 10); // Achillies
		    cm.teachSkill(1121008, 0, 10); // Brandish
		    cm.sendNext("You have become the best of warriors, my #bHero#k.You will gain the #bRush#k Skill which makes you attack mutiple enemies and give you indomitable will along with #bStance#k and #bAchilles#k");
		} else if (cm.getJob() == 121) {
		    cm.changeJob(122);
		    cm.teachSkill(1221001, 0, 10); // Monster Magnet
		    cm.teachSkill(1220005, 0, 10); // Achillies
		    cm.teachSkill(1221009, 0, 10); // Blast
		    cm.sendNext("You have become the best of warriors, my #bPaladint#k.You will gain the #bRush#k Skill which makes you attack mutiple enemies and give you indomitable will along with #bStance#k and #bAchilles#k");
		} else {
		    cm.changeJob(132);
		    cm.teachSkill(1321001, 0, 10);
		    cm.teachSkill(1320005, 0, 10);
		    cm.teachSkill(1321007, 0, 10);
		    cm.sendNext("You have become the best of warriors, my #bDark Knight#k.You will gain the #bRush#k Skill which makes you attack mutiple enemies and give you indomitable with along with #bStance#k and #bAchilles#k.");
		}
	    } else {
		cm.sendOk("You can't proceed as you don't have an empty slot in your inventory. Please clear your inventory and try again.");
		cm.dispose();
		return;
	    }
	}
    } else if (status == 2) {
	if (cm.getJob() == 112) {
	    cm.sendNext("This is not all about Hero. Hero is a well-balanced warrior who has excellent attack and defense power. It can learn various attack skills as well as combo attack if he trains himself.");
	} else if (cm.getJob() == 122) {
	    cm.sendNextPrev("This is not all about Paladin. Paladin is good at element-based attack and defense. It can use a new element-based and may break the limit of charge blow if you train yourself.");
	} else {
	    cm.sendNextPrev("This is not all about Dark Knight. Dark Knight can use the power of darkness. It can attack with power of darkness which is unbelievably strong and may summon the figure of darkness.");
	}
    } else if (status == 3) {
	cm.sendNextPrev("Don't forget that it all depends on how much you train.");
	cm.dispose();
    }
}