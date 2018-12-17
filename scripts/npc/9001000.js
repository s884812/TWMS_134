/* 
 * Cokebear operator : Event starter
 */

var points;

function start() {
    var record = cm.getQuestRecord(150001);
    points = record.getCustomData() == null ? "0" : record.getCustomData();

    cm.sendSimple("Would you like to have a taste of a relentless boss battle? If so you must definitely try this! Which of these difficulty levels do you want to take on?.... \n\r #b[TIP : The points will be saved on every defeat of bosses!]#k \n\r #b#L3#Current points#l#k \n\r\n\r\n #b#L0# #v03994115##l #L1# #v03994116##l #L2# #v03994117##l   \n\r\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \n\r #b#L4##i2022179:#Trade 10,000 points (Onyx Apple)#l#k \n\r #b#L5##i2340000:#Trade 100,000 points (White Scroll)#l#k \n\r #b#L6##i2049100:#Trade 40,000 points (Chaos Scroll)#l#k \n\r #b#L7##i3010041:#Trade 100,000 points (Skull Throne)#l#k \n\r #b#L8##i2070018:#Trade 200,000 points (Balanced Fury)#l#k");
}

function action(mode, type, selection) {
    if (mode == 1) {
	switch (selection) {
	    case 0:
		if (cm.getParty() != null) {
		    if (cm.isLeader()) {
			var q = cm.getEventManager("BossQuestEASY");
			if (q == null) {
			    cm.sendOk("Unknown error occured");
			} else {
			    q.startInstance(cm.getParty(), cm.getMap());
			}
		    } else {
			cm.sendOk("You are not the leader of the party, please ask your leader to talk to me.");
		    }
		} else {
		    cm.sendOk("Please form a party first.");
		}
		break;
	    case 1:
		if (cm.getParty() != null) {
		    if (cm.isLeader()) {
			var q = cm.getEventManager("BossQuestMed");
			if (q == null) {
			    cm.sendOk("Unknown error occured");
			} else {
			    q.startInstance(cm.getParty(), cm.getMap());
			}
		    } else {
			cm.sendOk("You are not the leader of the party, please ask your leader to talk to me.");
		    }
		} else {
		    cm.sendOk("Please form a party first.");
		}
		break;
	    case 2:
		if (cm.getParty() != null) {
		    if (cm.isLeader()) {
			var q = cm.getEventManager("BossQuestHARD");
			if (q == null) {
			    cm.sendOk("Unknown error occured");
			} else {
			    q.startInstance(cm.getParty(), cm.getMap());
			}
		    } else {
			cm.sendOk("You are not the leader of the party, please ask your leader to talk to me.");
		    }
		} else {
		    cm.sendOk("Please form a party first.");
		}
		break;
	    case 3:
		cm.sendOk("#bCurrent Points : " + points);
		break;
	    case 4: // Onyx Apple
		var record = cm.getQuestRecord(150001);
		var intPoints = parseInt(points);

		if (intPoints >= 10000) {
		    if (cm.canHold(2022179)) {
			intPoints -= 10000;
			record.setCustomData(""+intPoints+"");
			cm.gainItem(2022179, 1);
			cm.sendOk("Enjoy your rewards :P");
		    } else {
			cm.sendOk("Please check if you have sufficient inventory slot for it.")
		    }
		} else {
		    cm.sendOk("Please check if you have sufficient points for it, #bCurrent Points : " + points);
		}
		break;
	    case 5: // White Scroll
		var record = cm.getQuestRecord(150001);
		var intPoints = parseInt(points);

		if (intPoints >= 100000) {
		    if (cm.canHold(2340000)) {
			intPoints -= 100000;
			record.setCustomData(""+intPoints+"");
			cm.gainItem(2340000, 1);
			cm.sendOk("Enjoy your rewards :P");
		    } else {
			cm.sendOk("Please check if you have sufficient inventory slot for it.")
		    }
		} else {
		    cm.sendOk("Please check if you have sufficient points for it, #bCurrent Points : " + points);
		}
		break;
	    case 6: // Chaos Scroll
		var record = cm.getQuestRecord(150001);
		var intPoints = parseInt(points);

		if (intPoints >= 40000) {
		    if (cm.canHold(2049100)) {
			intPoints -= 40000;
			record.setCustomData(""+intPoints+"");
			cm.gainItem(2049100, 1);
			cm.sendOk("Enjoy your rewards :P");
		    } else {
			cm.sendOk("Please check if you have sufficient inventory slot for it.")
		    }
		} else {
		    cm.sendOk("Please check if you have sufficient points for it, #bCurrent Points : " + points);
		}
		break;
	    case 7: // Skull Throne
		var record = cm.getQuestRecord(150001);
		var intPoints = parseInt(points);

		if (intPoints >= 100000) {
		    if (cm.canHold(3010041)) {
			intPoints -= 100000;
			record.setCustomData(""+intPoints+"");
			cm.gainItem(3010041, 1);
			cm.sendOk("Enjoy your rewards :P");
		    } else {
			cm.sendOk("Please check if you have sufficient inventory slot for it.")
		    }
		} else {
		    cm.sendOk("Please check if you have sufficient points for it, #bCurrent Points : " + points);
		}
		break;
	    case 8: // Balanced Fury
		var record = cm.getQuestRecord(150001);
		var intPoints = parseInt(points);

		if (intPoints >= 200000) {
		    if (cm.canHold(2070018)) {
			intPoints -= 200000;
			record.setCustomData(""+intPoints+"");
			cm.gainItem(2070018, 1);
			cm.sendOk("Enjoy your rewards :P");
		    } else {
			cm.sendOk("Please check if you have sufficient inventory slot for it.")
		    }
		} else {
		    cm.sendOk("Please check if you have sufficient points for it, #bCurrent Points : " + points);
		}
		break;
	}
    }
    cm.dispose();
}