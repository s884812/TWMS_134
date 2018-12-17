/*
	NPC : TMS
	MAP : 741000000
*/
// Removed / Implemented into auto job scripts
/*var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		cm.dispose();
		status--;
	}
	if (status == 0) {
		if (cm.getJob() == 0 && cm.getPlayerStat("LVL") == 8) {
			cm.sendSimple("You seem to be eligible to be a Magician, would you like to be a Magician?\r\n#L0#Yes#l\r\n#L1#No#l");
		} else if (cm.getJob() == 0 && cm.getPlayerStat("LVL") == 10) {
			cm.sendSimple("Please select a job that you wish to be.\r\n#L2#Warrior#l\r\n#L3#Archer#l\r\n#L4#Theif#l\r\n#L5#Pirate#l");
		} else {
			cm.sendOk("You must be level 8/10 for first job and level 30 to be second job! Sorry but you does not meet the requirement!");
			cm.dispose();
		}
	} else if (status == 1) {
		if (selection == 0){ // Be Magician
			cm.changeJob(200);
		} else if (selection == 1){ // Not to be Magician
			cm.sendOk("Well, its your choice then. Good luck~");
		}
	}
}*/