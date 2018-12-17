/*
	Dao
	Golden Temple
	Monkey Temple Guide
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
		cm.sendSimple("Are you here because you heard about the Monkey Temple inside the Golden Temple?\r\n#b#L0#I want to enter the Monkey Temple.#l\r\n#b#L1#Please tell me more about the Monkey Temple.#l#k");
	} else if (status == 1) {
		if (selection == 0){
			cm.sendSimple("Which temple do you wish to enter? Do you know that you have to enter the temple on your own?\r\n#b#L0#Monkey Temple 1 (Lv.15 Wild Monkey)#l\r\n#b#L1#Monkey Temple 2 (Lv.21 Mama Monkey)#l\r\n#b#L2#Monkey Temple 3 (Lv.27 White Baby Monkey)#l\r\n#b#L3#Monkey Temple 4 (Lv.34 White Mama Monkey)#l#k");
		} else if (selection == 1){
			cm.sendOk("This is a forest where the monkeys outside of the Golden Temple live.\r\n\r\n1.Benefits of the Monkey Temple\r\n#b - Yields more EXP than other monsters of the same level\r\n - Drops various scrolls#k\r\n\r\n2. How to obtain the Golden Ticket (required to enter)\r\n - Mr. Yoo's quest (can be completed once per day)\r\n - Freely enter once per hour if you possess a Premium Golden Ticket");
			cm.dispose();
		}
	} else if (status == 2){
		if (selection == 0) {
			if (cm.haveItem(4001431)) { // Golden Ticket
				cm.gainItem(4001431, -1);
				cm.warp(950100100, 0);
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry but you can't enter the Monkey Temple without a ticket. Let me explain the Monkey Temple to you again so you can understand how to obtain a ticket.");
				cm.dispose();
			}
		} else if (selection == 1) {
			if (cm.haveItem(4001431)) { // Golden Ticket
				cm.gainItem(4001431, -1);
				cm.warp(950100200, 0);
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry but you can't enter the Monkey Temple without a ticket. Let me explain the Monkey Temple to you again so you can understand how to obtain a ticket.");
				cm.dispose();
			}
		} else if (selection == 2) {
			if (cm.haveItem(4001431)) { // Golden Ticket
				cm.gainItem(4001431, -1);
				cm.warp(950100300, 0);
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry but you can't enter the Monkey Temple without a ticket. Let me explain the Monkey Temple to you again so you can understand how to obtain a ticket.");
				cm.dispose();
			}
		} else if (selection == 3) {
			if (cm.haveItem(4001431)) { // Golden Ticket
				cm.gainItem(4001431, -1);
				cm.warp(950100400, 0);
				cm.dispose();
			} else {
				cm.sendOk("I'm sorry but you can't enter the Monkey Temple without a ticket. Let me explain the Monkey Temple to you again so you can understand how to obtain a ticket.");
				cm.dispose();
			}
		}
	}
}