/*
	Machine Apparatus - Origin of Clocktower(220080001)
*/

var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (status == 0) {
	cm.sendYesNo("Hey, do you want to fight pap again? Just press yes and then teleport back in via @npc -> boss warper and there will be a new hole in the wall!");
    } else if (status == 1) {
	cm.resetReactors();
	cm.warp(220080000);
	cm.dispose();
    }
}
