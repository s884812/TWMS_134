/*
	NPC Name: 		Adobis
	Map(s): 		El Nath : Entrance to Zakum Altar
	Description: 		Zakum battle starter
*/
var status = 0;

function action(mode, type, selection) {
    switch (status) {
	case 0:
	    var em = cm.getEventManager("ZakumBattle");

	    if (em == null) {
		cm.sendOk("The event isn't started, please contact a GM.");
		cm.safeDispose();
		return;
	    }

	    var squadAvailability = cm.getSquadAvailability("ZAK");
	    if (squadAvailability == -1) {
		status = 1;
		cm.sendYesNo("Are you interested in becoming the leader of the expedition Squad?");

	    } else if (squadAvailability == 1) {
		// -1 = Cancelled, 0 = not, 1 = true
		var type = cm.isSquadLeader("ZAK");
		if (type == -1) {
		    cm.sendOk("The squad has ended, please re-register.");
		    cm.safeDispose();
		} else if (type == 0) {
		    var memberType = cm.isSquadMember("ZAK");
		    if (memberType == 2) {
			cm.sendOk("You been banned from the squad.");
			cm.safeDispose();
		    } else if (memberType == 1) {
			status = 5;
			cm.sendSimple("What do you want to do? \r\n#b#L0#Check out members#l \r\n#b#L1#Join the squad#l \r\n#b#L2#Withdraw from squad#l");
		    } else if (memberType == -1) {
			cm.sendOk("The squad has ended, please re-register.");
			cm.safeDispose();
		    } else {
			status = 5;
			cm.sendSimple("What do you want to do? \r\n#b#L0#Check out members#l \r\n#b#L1#Join the squad#l \r\n#b#L2#Withdraw from squad#l");
		    }
		} else { // Is leader
		    status = 10;
		    cm.sendSimple("What do you want to do? \r\n#b#L0#Check out members#l \r\n#b#L1#Remove member#l \r\n#b#L2#Edit restricted list#l \r\n#r#L3#Enter map#l");
		// TODO viewing!
		}
	    } else {
		cm.sendOk("The battle against the boss has already begun.");
		cm.safeDispose();
	    }
	    break;
	case 1:
	    if (mode == 1) {
		cm.registerSquad("ZAK", 5, " has been named the Leader of the squad. If you would you like to join please register for the Expedition Squad within the time period.");
		cm.sendOk("You have been named the Leader of the Squad. For the next 5 minutes, you can add the members of the Expedition Squad.");
	    } else {
		cm.sendOk("Talk to me if you want to become the leader of the Expedition squad.")
	    }
	    cm.safeDispose();
	    break;
	case 5:
	    if (selection == 0) {
		if (!cm.getSquadList("ZAK", 0)) {
		    cm.sendOk("Due to an unknown error, the request for squad has been denied.");
		    cm.safeDispose();
		} else {
		    cm.dispose();
		}
	    } else if (selection == 1) { // join
		var ba = cm.addMember("ZAK", true);
		if (ba == 2) {
		    cm.sendOk("The squad is currently full, please try again later.");
		    cm.safeDispose();
		} else if (ba == 1) {
		    cm.sendOk("You have joined the squad successfully");
		    cm.safeDispose();
		} else {
		    cm.sendOk("You are already part of the squad.");
		    cm.safeDispose();
		}
	    } else {// withdraw
		var baa = cm.addMember("ZAK", false);
		if (baa == 1) {
		    cm.sendOk("You have withdrawed from the squad successfully");
		    cm.safeDispose();
		} else {
		    cm.sendOk("You are not part of the squad.");
		    cm.safeDispose();
		}
	    }
	    break;
	case 10:
	    if (selection == 0) {
		if (!cm.getSquadList("ZAK", 0)) {
		    cm.sendOk("Due to an unknown error, the request for squad has been denied.");
		}
		cm.safeDispose();
	    } else if (selection == 1) {
		status = 11;
		if (!cm.getSquadList("ZAK", 1)) {
		    cm.sendOk("Due to an unknown error, the request for squad has been denied.");
		}
		cm.safeDispose();
	    } else if (selection == 2) {
		status = 12;
		if (!cm.getSquadList("ZAK", 2)) {
		    cm.sendOk("Due to an unknown error, the request for squad has been denied.");
		}
		cm.safeDispose();
	    } else if (selection == 3) { // get insode
		if (cm.getSquad("ZAK") != null) {
		    var dd = cm.getEventManager("ZakumBattle");
		    dd.startInstance(cm.getSquad("ZAK"), cm.getMap());
		    cm.dispose();
		} else {
		    cm.sendOk("Due to an unknown error, the request for squad has been denied.");
		    cm.safeDispose();
		}
	    }
	    break;
	case 11:
	    cm.banMember("ZAK", selection);
	    cm.dispose();
	    break;
	case 12:
	    if (selection != -1) {
		cm.acceptMember("ZAK", selection);
	    }
	    cm.dispose();
	    break;
    }
}