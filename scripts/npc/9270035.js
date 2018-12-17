var status = -1;

function start() {
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		if (status >= 2 || status == 0) {
			cm.dispose();
			return;
		}
		status--;
	}
	if (status == 0) {
		cm.sendSimple("Hello #r#h ##k! I am #rJoyce - Skill Manager#k.\r\n#b#L0#Secret Scroll [4th Job] for 10 million mesos#l\r\n#L1#I would like to learn Maker Skill#l\r\n#L2#I would like to learn Monster Rider Skill#l\r\n#L3#Trade #v2290153# for Slash Storm Mastery#l#k");
	} else if (selection == 0) {
		if (cm.haveItem(4031348)) {
			cm.sendOk("You already have one, I don't think you'll need it anyway.");
		} else if (cm.getPlayerStat("LVL") >= 120 && cm.getPlayerStat("LVL") <= 130 && cm.getMeso() >= 10000000) {
			if (!cm.canHold(4031348)) {
				cm.sendOk("Please check if you have sufficient space.");
			} else {
				cm.gainMeso(-10000000);
				cm.gainItem(4031348, 1);
			}
		} else {
			cm.sendOk("Hey, I dont think you have enough mesos or the required level range of 120 ~ 130.");
		}
		cm.dispose();
	} else if (selection == 1) {
		if (cm.getPlayerStat("LVL") < 45) {
			cm.sendOk("Please be at least level 45 and find me again to acquire the skill.");
		} else if (cm.getJob() >= 2001) { // Evan
			cm.teachSkill(20011007, 3, 3);
		} else if (cm.getJob() >= 2000) { // Aran
			cm.teachSkill(20001007, 3, 3);
		} else if (cm.getJob() >= 1000) { // KOC
			cm.teachSkill(10001007, 3, 3);
		} else { // Adventurer
			cm.teachSkill(1007, 3, 3);
		}
		cm.sendOk("I've taught you Maker skill, please make good use of it.");
		cm.dispose();
	} else if (selection == 2) {
		if (cm.getPlayerStat("LVL") < 70) {
			cm.sendOk("Please be at least level 70 and find me again to acquire the skill.");
		} else if (cm.getJob() >= 2001) { // Evan
			cm.teachSkill(20011004, 1, 1);
		} else if (cm.getJob() >= 2000) { // Aran
			cm.teachSkill(20001004, 1, 1);
		} else if (cm.getJob() >= 1000) { // KOC
			cm.teachSkill(10001004, 1, 1);
		} else { // Adventurer
			cm.teachSkill(1004, 1, 1);
		}
		cm.sendOk("I've taught you Monster Rider skill, please make good use of it.");
		cm.dispose();
	} else if (selection == 3) {
		if (cm.haveItem(2290153)) { // Slash Storm mastery 20
			cm.gainItem(2290153, -1);
			cm.teachSkill(4311003, 5, 20);
			cm.dispose();
		} else {
			cm.sendOk("I'm sorry but seem that you don't have #v2290153# to trade with.");
			cm.dispose();
		}
	}
}