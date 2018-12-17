/*
	Mr. Yoo
	Golden Temple
	Send you back wherever you came from
*/
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
		status++;
    } else {
		status--;
    }
    if (status == 0) {
		cm.sendYesNo("Do you wish to leave the Golden Temple and return to your original town?");
	} else if (status == 1) {
		cm.sendNext("Okay. Visit the Golden Temple again soon!");
    } else if (status == 2) {
		returnmap = cm.getSavedLocation("MIRROR_OF_DIMENSION");
		cm.clearSavedLocation("MIRROR_OF_DIMENSION");
		cm.warp(returnmap);
		cm.dispose();
	} else if (status == -1) {
		cm.sendNext("Okay, then continue your tour. Have fun!");
		cm.dispose();
	}
}