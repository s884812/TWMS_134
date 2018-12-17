var status = -1;

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		cm.dispose();
		status--;
	}
	if (status == 0) {
		cm.sendSimple("#b#L1#Potion Store#l\r\n#L2#Spinel - World Tour Guide#l\r\n#L3#Job Advance#l\r\n#L4#Redeem Vote Points#l\r\n#L5#Joyce - Skill Manager#l\r\n#L7#Drop my Cash item#l\r\n#L8#Maple Leaf Manager#l\r\n#L10#Trade #r1#b vote point for 5, 000 @cash#l");
	} else if (status == 1) {
		if (selection == 0){ // #L0#Equipment Store#l
			cm.dispose();
			//cm.sendSimple("#L1#Bowman Weapon#l\r\n#L2#Bowman Armor#l\r\n\r\n#L3#Magician Weapon#l\r\n#L4#Magician Armor#l\r\n\r\n#L5#Pirate Weapon#l\r\n#L6#Pirate Armor#l\r\n\r\n#L7#Theif Weapon#l\r\n#L8#Theif Armor#l\r\n\r\n#L9#Warrior Weapon#l\r\n#L10#Warrior Armor#l");
		} else if (selection == 1){ // Potion Store
			cm.dispose();
			cm.openShop(2);
		} else if (selection == 2){ // Spinel - World Tour Guide
			cm.dispose();
			cm.openNpc(9000020);
		} else if (selection == 3) { // Job Advance
			cm.dispose();
			cm.openNpc(9105010); // Vavaan (9330027); // TMS
		} else if (selection == 4) { // Redeem Vote Points
			cm.dispose();
			cm.openNpc(9330024);
		} else if (selection == 5) { // Joyce - Skill Manager
			cm.dispose();
			cm.openNpc(9270035);
		} else if (selection == 6) { // @cash Store
			cm.dispose();
			//cm.openNpc(9310017);
		} else if (selection == 7) { // Drop my @cash item
			cm.dispose();
			cm.openNpc(9010017);
		} else if (selection == 8) { // Maple Leaf Manager
			cm.dispose();
			cm.openNpc(9000017);
		} else if (selection == 9) { // Alliance Bank Malaysia Berhad
			cm.dispose();
			//cm.openNpc(9250122);
		} else if (selection == 10) {
			if(cm.getVPoints() >= 1){
				cm.gainVPoints(-1);
				cm.gainNX(5000);
				cm.sendOk("Enjoy your items and remember to vote for us and get more items!");
				cm.dispose();
			} else {
				cm.sendOk("Not enough vote points!");
				cm.dispose();
			}
		}
	} else if (status == 2){
		if (selection == 0) {
			status = -1;
			cm.dispose();
		} else if (selection == 1) { // Bowman Weapon
			cm.dispose();
			//cm.openShop(9);
		} else if (selection == 2) { // Bowman Armor
			cm.dispose();
			//cm.openShop(10);
		} else if (selection == 3) { // Magician Weapon
			cm.dispose();
			//cm.openShop(3);
		} else if (selection == 4) { // Magician Armor
			cm.dispose();
			//cm.openShop(4);
		} else if (selection == 5) { // Pirate Weapon
			cm.dispose();
			//cm.openShop(11);
		} else if (selection == 6) { // Pirate Weapon
			cm.dispose();
			//cm.openShop(12);
		} else if (selection == 7) { // Theif Weapon
			cm.dispose();
			//cm.openShop(5);
		} else if (selection == 8) { // Theif Armor
			cm.dispose();
			//cm.openShop(6);
		} else if (selection == 9) { // Warrior Armor
			cm.dispose();
			//cm.openShop(7);
		} else if (selection == 10) { // Warrior Armor
			cm.dispose();
			//cm.openShop(8);
		} else if (selection == 11) { // Warrior Armor
			cm.dispose();
			//cm.openNpc(9010000);
		}
			/*else if (selection == 2) { //coco
			cm.dispose();
			cm.openNpc(9000017);
		} else if (selection == 3) { //reset
			cm.dispose();
			cm.openNpc(1052014);
		} else if (selection == 4) { //cygus skill
			cm.dispose();
			cm.openNpc(1052015);
		} else if (selection == 5) { //aran
			cm.dispose();
			cm.openNpc(9010000);
		} else if (selection == 6) { //shop
			cm.dispose();
			cm.openShop(1);
		} else if (selection == 22) { //boss warper
			cm.dispose();
			cm.openNpc(9310016);
		} else if (selection == 8) { //4th job
			cm.dispose();
			cm.openNpc(9310015);
		} else if (selection == 9) { //cash item
			cm.dispose();
			cm.openNpc(9310017);
		} else if (selection == 10) { //donate
			cm.dispose();
			cm.openNpc(9310018);
		} else if (selection == 11) { //fmwarp
			cm.dispose();
			cm.warp(910000000, 0);
		} else if (selection == 12) { //TIMELESS/REVERSE SHOP
			cm.dispose();
			cm.openNpc(9330032);
		} else if (selection == 13) { //medal seller
			cm.dispose();
			cm.openNpc(9330028);
		} else if (selection == 14) { //trophy
			cm.dispose();
			cm.openNpc(9330031);
		} else if (selection == 15) { // Weapon guy
			cm.dispose();
			cm.openShop(42);
		} else if (selection == 16) { //Vote points
			cm.dispose();
			cm.openNpc(9330024);
		}*/
	}
}