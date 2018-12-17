var status = -1;

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		cm.dispose();
		status--;
	}
	/*if (status == 0) {
		if (cm.getPlayerStat("GM") == 0) {
			cm.gainNX(-350000);
			cm.dispose();
		} else {
			cm.sendSimple("Welcome to @cash store.\r\nYou currently have #b"+ cm.getNX() +"#k @cash\r\n#L0#Selection 1#l\r\n#L1#Selection 2#l");
		}
	} else if (status == 1) {
		if (selection == 1){
			cm.sendSimple("#L1#Bowman Weapon#l\r\n#L2#Bowman Armor#l\r\n\r\n#L3#Magician Weapon#l");
		} else if (selection == 2){
			cm.dispose();
		}
	} else if (status == 2){
		if (selection == 0) {
			//
		} else if (selection == 1) { // Bowman Weapon
			if (cm.getNX() >= 350000) {
				cm.gainNX(-350000);
				cm.gainItem(2070019, 800);
				cm.sendNext("You have recieved Magic Throwing Stars");
				cm.dispose();
			} else {
				cm.sendNext("You dont have 350, 000 @cash");
				cm.dispose();
			}
		}
	}*/
}