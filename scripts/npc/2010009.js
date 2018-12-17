var status = 0;
var sel = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0) {
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;

    if (status == 0) {
	cm.sendOk("lolz");
	cm.dispose();
//	cm.sendSimple("What would you like to do?\r\n#b#L0#Create an Alliance#l\r\n#L1#I want to invite a guild to my alliance#l");
    } else if (status == 1) {
	if (selection == 0) {
	    if (cm.getPlayerStat("AID") > 0) {
		cm.sendOk("You may not create a new Alliance while you are in one.");
		cm.dispose();
	    } else {
		cm.sendYesNo("Creating an Alliance costs #b100000000 mesos#k, are you sure you want to continue?");
	    }
	    sel = 0;
	} else if (selection == 1) {
	    if (cm.getPlayerStat("AID") <= 0) {
		cm.sendOk("You can't invite someone if you aren't in an alliance..");
		cm.dispose();
	    } else
		cm.sendYesNo("The max ammount of alliances is 5, are you sure you want to add someone?");
	    sel = 1;
	}
    } else if (status == 2) {
	if (sel == 0)
	    cm.sendGetText("And what would be the name of your alliance?");
	else
	    cm.sendGetText("What is the name of the leader of that guild?");
    } else if (status == 3) {
	var name = cm.getText();
	if (sel == 0) {
	    if (!cm.hasAlliance()) {
		cm.createAlliance(name);
	    } else {
		cm.sendOk("You are already in an Alliance!")
	    }
	} else {
	    cm.sendAllianceInvite(name);
	}
	cm.dispose();
    }
}