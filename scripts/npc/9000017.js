var status = -1;

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		cm.dispose();
		status--;
	}
	if (status == 0) {
		cm.sendSimple("So, what can i do for you?\r\n#L0#Trade for Warrior Item#l\r\n#L1#Trade for Magician Item#l\r\n#L2#Trade for Archer Item#l\r\n#L3#Trade for Thief Item#l\r\n#L4#Trade for Pirate Item#l\r\n#L5#Trade for Armours#l");
	} else if (status == 1) {
		if (selection == 0) { // Warrior
			cm.sendSimple("Here a list of current item i have in storage, see if anything you wanted?\r\n#L0##i" + 1302020 + "##l\r\n#L10##i" + 1302064 + "##l\r\n#L9##i" + 1432012 + "##l\r\n#L11##i" + 1432040 + "##l\r\n#L30##i" + 1302030 + "##l\r\n#L31##i" + 1302064 + "##l\r\n#L32##i" + 1402039 + "##l\r\n#L33##i" + 1402085 + "##l\r\n#L34##i" + 1312032 + "##l\r\n#L35##i" + 1312056 + "##l\r\n#L36##i" + 1412011 + "##l\r\n#L37##i" + 1412027 + "##l\r\n#L38##i" + 1412055 + "##l\r\n#L39##i" + 1322054 + "##l\r\n#L40##i" + 1322084 + "##l\r\n#L41##i" + 1422014 + "##l\r\n#L42##i" + 1422029 + "##l\r\n#L43##i" + 1422057 + "##l");
		} else if (selection == 1) { // Magician
			cm.sendSimple("Here a list of current item i have in storage, see if anything you wanted?\r\n#L2##i" + 1382009 + "##l\r\n#L23##i" + 1382012 + "##l\r\n#L24##i" + 1382039 + "##l\r\n#L25##i" + 1372034 + "##l");
		} else if (selection == 2) { // Archer
			cm.sendSimple("Here a list of current item i have in storage, see if anything you wanted?\r\n#L3##i" + 1452016 + "##l\r\n#L18##i" + 1452022 + "##l\r\n#L19##i" + 1452045 + "##l\r\n#L4##i" + 1462014 + "##l\r\n#L20##i" + 1462019 + "##l\r\n#L21##i" + 1462040 + "##l");
		} else if (selection == 3) { // Theif
			cm.sendSimple("Here a list of current item i have in storage, see if anything you wanted?\r\n#L1##i" + 1332025 + "##l\r\n#L14##i" + 1332056 + "##l\r\n#L5##i" + 1472030 + "##l\r\n#L13##i" + 1472032 + "##l\r\n#L17##i" + 1472055 + "##l\r\n#L12##i" + 1342025 + "##l\r\n#L15##i" + 1342026 + "##l\r\n#L16##i" + 1342027 + "##l");
		} else if (selection == 4) { // Pirates
			cm.sendSimple("Here a list of current item i have in storage, see if anything you wanted?\r\n#L6##i" + 1492020 + "##l\r\n#L28##i" + 1492021 + "##l\r\n#L29##i" + 1492022 + "##l\r\n#L7##i" + 1482020 + "##l\r\n#L26##i" + 1482021 + "##l\r\n#L27##i" + 1482022 + "##l");
		} else if (selection == 5) { // Armor
			cm.sendSimple("Here a list of current item i have in storage, see if anything you wanted?\r\n#L8##i" + 1092030 + "##l");
		}
	} else if (status == 2) {
		if (selection == 0) {
			newWepId = 1302020; // Maple Sword
			leaves = 100;
			cost = 50000;
		} else if (selection == 1) {
			newWepId = 1332025; // Maple Wagner
			leaves = 100;
			cost = 50000;
		} else if (selection == 2) {
			newWepId = 1382009; // Maple Staff
			leaves = 100;
			cost = 50000;
		} else if (selection == 3) {
			newWepId = 1452016; // Maple Bow
			leaves = 100;
			cost = 50000;
		} else if (selection == 4) {
			newWepId = 1462014; // Maple Crow
			leaves = 100;
			cost = 50000;
		} else if (selection == 5) {
			newWepId = 1472030; // Maple Claw
			leaves = 100;
			cost = 50000;
		} else if (selection == 6) {
			newWepId = 1492020; // Maple Gun
			leaves = 100;
			cost = 50000;
		} else if (selection == 7) {
			newWepId = 1482020; // Maple Knucle
			leaves = 100;
			cost = 50000;
		} else if (selection == 8) {
			newWepId = 1092030; // Maple Shield
			leaves = 100;
			cost = 50000;
		} else if (selection == 9) {
			newWepId = 1432012; // Maple Impaler
			leaves = 200;
			cost = 100000;
		} else if (selection == 10) {
			newWepId = 1302064; // Maple Glory Sword
			leaves = 500;
			cost = 200000;
		} else if (selection == 11) {
			newWepId = 1432040; // Maple Berit Spear
			leaves = 500;
			cost = 200000;
		} else if (selection == 12) {
			newWepId = 1342025; // Maple Katara
			leaves = 100;
			cost = 50000;
		} else if (selection == 13) {
			newWepId = 1472032; // Maple Kandayo
			leaves = 200;
			cost = 100000;
		} else if (selection == 14) {
			newWepId = 1332056; // Maple Asura Dagger
			leaves = 500;
			cost = 200000;
		} else if (selection == 15) {
			newWepId = 1342026; // Maple Duke Katara
			leaves = 200;
			cost = 100000;
		} else if (selection == 16) {
			newWepId = 1342027; // Maple Cleat Katara
			leaves = 500;
			cost = 200000;
		} else if (selection == 17) {
			newWepId = 1472055; // Maple Scanda
			leaves = 500;
			cost = 200000;
		} else if (selection == 18) {
			newWepId = 1452022; // Maple Soul Searcher
			leaves = 200;
			cost = 100000;
		} else if (selection == 19) {
			newWepId = 1452045; // Maple Gandiva Bow
			leaves = 500;
			cost = 200000;
		} else if (selection == 20) {
			newWepId = 1462019; // Maple Crossbow
			leaves = 200;
			cost = 100000;
		} else if (selection == 21) {
			newWepId = 1462040; // Maple Nishada
			leaves = 500;
			cost = 200000;
		} else if (selection == 22) {
			newWepId = 1462019; // Maple Crossbow
			leaves = 100;
			cost = 50000;
		} else if (selection == 23) {
			newWepId = 1382012; // Maple Lama Staff
			leaves = 200;
			cost = 100000;
		} else if (selection == 24) {
			newWepId = 1382039; // Maple Widam Staff
			leaves = 500;
			cost = 200000;
		} else if (selection == 25) {
			newWepId = 1372034; // Maple Shiny Wand
			leaves = 500;
			cost = 200000;
		} else if (selection == 26) {
			newWepId = 1482021; // Maple Storm Finger
			leaves = 200;
			cost = 100000;
		} else if (selection == 27) {
			newWepId = 1482022; // Maple Golden Claw
			leaves = 500;
			cost = 200000;
		}else if (selection == 28) {
			newWepId = 1492021; // Maple Storm Pistol
			leaves = 200;
			cost = 100000;
		} else if (selection == 29) {
			newWepId = 1492022; // Maple Cannon Shooter
			leaves = 500;
			cost = 200000;
		} else if (selection == 30) {
			newWepId = 1302030; // Maple Soul Singer
			leaves = 200;
			cost = 100000;
		} else if (selection == 31) {
			newWepId = 1302064; // Maple Glory Sword
			leaves = 500;
			cost = 200000;
		} else if (selection == 32) {
			newWepId = 1402039; // Maple Soul Lohen
			leaves = 500;
			cost = 200000;
		} else if (selection == 33) {
			newWepId = 1402085; // Maple Pyrope Rohen
			leaves = 800;
			cost = 300000;
		} else if (selection == 34) {
			newWepId = 1312032; // Maple Steel Axe
			leaves = 500;
			cost = 200000;
		} else if (selection == 35) {
			newWepId = 1312056; // Maple Pyrope Axe
			leaves = 800;
			cost = 300000;
		} else if (selection == 36) {
			newWepId = 1412011; // Maple Dragon Axe
			leaves = 200;
			cost = 100000;
		} else if (selection == 37) {
			newWepId = 1412027; // Maple Demon Axe
			leaves = 500;
			cost = 200000;
		} else if (selection == 38) {
			newWepId = 1412055; // Maple Pyrope Battle Axe
			leaves = 800;
			cost = 300000;
		} else if (selection == 39) {
			newWepId = 1322054; // Maple Havoc Hammer
			leaves = 500;
			cost = 200000;
		} else if (selection == 40) {
			newWepId = 1322084; // Maple Pyrope Hammer
			leaves = 800;
			cost = 300000;
		} else if (selection == 41) {
			newWepId = 1422014; // Maple Doom Singer
			leaves = 200;
			cost = 100000;
		} else if (selection == 42) {
			newWepId = 1422029; // Maple Belzet
			leaves = 500;
			cost = 100000;
		} else if (selection == 43) {
			newWepId = 1422057; // Maple Pyrope Maul
			leaves = 800;
			cost = 300000;
		}
		cm.sendYesNo("Are you sure you want to make a #i" + newWepId + "#?\r\nThe following items and materials will be required.\r\n\r\n\#i4001126# x" + leaves + "#k\r\n\r\n#fUI/UIWindow.img/QuestIcon/7/0#  " + cost);
	} else if (status == 3) {
		if ((cm.getMeso() < cost) || !cm.haveItem(4001126,leaves)) {
			cm.sendOk("Sorry, but you don't seem to have all the items. Please get them all, and try again.");
		} else {
			if (cm.canHold(newWepId)) {
				cm.gainItem(4001126, -leaves);
				cm.gainMeso(-cost);
				cm.gainItem(newWepId, 1, true, 2617200000); // exactly 30 days | System and game time diff is 7 hour exactly
				cm.sendOk("There, all done! That was quick, wasn't it? If you need any more items, I'll be waiting here.");
			} else {
				cm.sendOk("It appears that you are currently in full inventory, please check.");
			}
		}
		cm.dispose();
	}
}