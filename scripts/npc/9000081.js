/*
	Ms. Tang
	Golden Temple
	Exchange 10 Golden Ticket pieces into 1 Golden Ticket
*/
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
		status++;
    } else {
		status--;
    }
    if (status == 0) {
		if (cm.haveItem(4032605, 10)) {
			cm.sendYesNo("You seem to be have 10 #i4032605#, do you want to exchange that for 1 #i4001431#?");
		} else {
			cm.sendOk("Sorry but you must have 10 #i4032605# in order to exchange 1 #i4001431#");
			cm.dispose();
		}
    } else if (status == 1) {
		cm.gainItem(4032605, -10);
		cm.gainItem(4001431, 1);
		cm.sendOk("Use this #i4001431# wisely thanks you!");
		cm.dispose();
    } else if (status == -1) {
		cm.sendOk("Well, that's your choice... See you later");
		cm.dispose();
    }
}