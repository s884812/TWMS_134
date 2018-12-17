/*
	Chan
	Golden Temple
	Goblin Cave Guard
*/

var status = -1;

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		status--;
		cm.dispose();
	}
	if (status == 0) {
		cm.sendSimple("What do you want? Please step aside.\r\n#b#L0#I want to enter the Goblin Cave.#l\r\n#b#L1#Please tell me more about the Goblin Cave.#l#k");
	} else if (status == 1) {
		if (selection == 0){
			cm.sendSimple("You need a Golden Ticket to enter. You can only enter when you're alone, too. Where do you want to go?\r\n#b#L0#Goblin Cave 1 (Lv.43 Blue Goblin)#l\r\n#b#L1#Goblin Cave 2 (Lv.54 Red Goblin)#l\r\n#b#L2#Goblin Cave 3 (Lv.66 Strong Stone Goblin)#l#k");
		} else if (selection == 1){
			cm.sendOk("This is the entrance to a cave under the Golden Temple filled with ferocious goblins. Only the strong should enter.\r\n\r\n1.Benefits of the Goblin Cave\r\n#b - Yields more EXP than other monsters of the same level\r\n - Drops various scrolls\r\n - Sunbursts required to enter the Ravana Dungeon #k\r\n\r\n2. How to obtain a Golden Ticket (required to enter)\r\n - Mr. Yoo's quest (can be completed once per day)\r\n - Freely enter every hour if you possess a Preminum Golden Ticket");
			cm.dispose();
		}
	} else if (status == 2){
		if (selection == 0) {
			if (cm.haveItem(4001431)) { // Golden Ticket
				cm.gainItem(4001431, -1);
				cm.warp(950100500, 0);
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry but without a ticket, I cannot let you in.");
				cm.dispose();
			}
		} else if (selection == 1) {
			if (cm.haveItem(4001431)) { // Golden Ticket
				cm.gainItem(4001431, -1);
				cm.warp(950100600, 0);
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry but without a ticket, I cannot let you in.");
				cm.dispose();
			}
		} else if (selection == 2) {
			if (cm.haveItem(4001431)) { // Golden Ticket
				cm.gainItem(4001431, -1);
				cm.warp(950100700, 0);
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry but without a ticket, I cannot let you in.");
				cm.dispose();
			}
		}
	}
}