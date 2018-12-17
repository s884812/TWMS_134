/*
	Maple Administrator
	Take Care of Beginner Explorer
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
	} else if (status == 1) {
		cm.warp(104000000, 0);
		cm.dispose();
	} else if (status == -1) {
		cm.sendOk("Enjoy your trip");
		cm.dispose();
	}
}