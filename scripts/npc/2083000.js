/*
	Encrypted Slate of the Squad - Leafre Cave of life
*/

var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
	    cm.sendOk("If you want to move, talk to me again.");
	    cm.safeDispose();
	    return;
	}
	status--;
    }

    if (status == 0) {
	if (cm.getParty() != null) {
	    if (cm.isLeader()) {
		if (cm.haveItem(4001086)) {
		    cm.sendYesNo("The letters on the slate glitter and the backdoor opens. Do you want to go to the secret path?");
		} else {
		    cm.sendOk("You can't read the words on the slate. You have no idea where to use it.");
		    cm.safeDispose();
		}
	    }
	} else {
	    cm.sendOk("Please proceed through the Party Leader.");
	    cm.safeDispose();
	}
    } else if (status == 1) {
	cm.warpParty(240050400);
	cm.dispose();
    }
}