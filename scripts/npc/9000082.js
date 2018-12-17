/*
	Pong
	Guard
	Ravana NPC
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
		cm.sendOk("status == 0");
		cm.dispose();
	} else if (status == 1) {
		cm.sendOk("status == 1");
		cm.dispose();
	}
}