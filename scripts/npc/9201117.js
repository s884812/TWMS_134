/*
	NPC : Toh Relicseeker
	Function : 
*/

var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 0) {
		cm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if (cm.getJob() == 112 && cm.getPlayerStat("LVL") >= 120) {
				if (cm.haveItem(4031035)) {
					cm.gainItem(4031035, -1);
					cm.teachSkill(1120003, 30, 30);
					cm.teachSkill(1120004, 30, 30);
					cm.teachSkill(1120005, 30, 30);
					cm.teachSkill(1121000, 30, 30);
					cm.teachSkill(1121001, 30, 30);
					cm.teachSkill(1121002, 30, 30);
					cm.teachSkill(1121008, 30, 30);
					cm.teachSkill(1121010, 30, 30);
					cm.teachSkill(1121011, 5, 5);
				} else {
					cm.sendOk("Sorry but you must be #blevel 120#k or above and have #i4031035:#");
				}
			} else if (cm.getJob() == 122 && cm.getPlayerStat("LVL") >= 120) {
				if (cm.haveItem(4031035)) {
					cm.gainItem(4031035, -1);
					cm.teachSkill(1220005, 30, 30);
					cm.teachSkill(1220006, 30, 30);
					cm.teachSkill(1220010, 10, 10);
					cm.teachSkill(1221000, 30, 30);
					cm.teachSkill(1221001, 30, 30);
					cm.teachSkill(1221002, 30, 30);
					cm.teachSkill(1221011, 30, 30);
					cm.teachSkill(1221012, 5, 5);
				} else {
					cm.sendOk("Sorry but you must be #blevel 120#k or above and have #i4031035:#");
				}
			} else if (cm.getJob() == 232 && cm.getPlayerStat("LVL") >= 120) {
				if (cm.haveItem(4031035)) {
					cm.gainItem(4031035, -1);
					cm.teachSkill(2321000, 30, 30);
					cm.teachSkill(2321001, 30, 30);
					cm.teachSkill(2321002, 30, 30);
					cm.teachSkill(2321003, 30, 30);
					cm.teachSkill(2321004, 30, 30);
					cm.teachSkill(2321005, 30, 30);
					cm.teachSkill(2321006, 10, 10);
					cm.teachSkill(2321007, 30, 30);
					cm.teachSkill(2321008, 30, 30);
					cm.teachSkill(2321009, 5, 5);
				} else {
					cm.sendOk("Sorry but you must be #blevel 120#k or above and have #i4031035:#");
				}
			}
			cm.dispose();
		}
	}
}