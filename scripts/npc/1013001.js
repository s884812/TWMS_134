/*
	Evan newbie dragon
*/

var status;

function start() {
	status = -1;
	action(1,0,0);
}

function action(mode, type, selection){
	if (mode == 0 && status == 0) {
		cm.dispose();
		return;
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNextS("You, who are destined to be a Dragon Master... You have finally arrived.", 1);
		} else if (status == 1) {
			cm.sendNextPrevS("Go and fulfill your duties as the Dragon Master...", 1);
		} else if (status == 2) {
			cm.warp(900090101, 0);
			cm.dispose();
		}
	}
}