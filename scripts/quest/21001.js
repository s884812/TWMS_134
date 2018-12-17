var status = -1;

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		if(type == 1 && mode == 0) {
			qm.sendNext("*Sob* Aran has declined my request!");
			qm.dispose();
			return;
		}else{
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.askAcceptDecline("(Shivering) I...was...so...scared...in...here. Please take me to Athena Pierce!");
	} else if (status == 1) {
		qm.forceStartQuest();
		qm.gainItem(4001271, 1);
		qm.warp(914000300);
		qm.dispose();
	}
}

function end(mode, type, selection) {
	status++;
	if (mode != 1) {
		if(type == 1 && mode == 0) {
			qm.sendNext("What about the child? Please give me the child!");
			qm.dispose();
			return;
		} else{
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendYesNo("Ahhh, you're safe! What about the kid? Where's the kid?");
	else if (status == 1) {
		qm.gainItem(4001271, -1);
		qm.forceCompleteQuest();
		qm.sendNextS("Oh phew... thank goodness...", 9);
	} else if (status == 2)
		qm.sendNextPrevS("Get on board right now! We do not have much time!", 3);
	else if (status == 3)
		qm.sendNextPrevS("Yes, yes. We don't have much time for that. I can feel the force of Black Wizard creeping up ever so close, and I have a feeling the Wizard has located the ark! If we don't leave now, then we will be attacked!", 9);
	else if (status == 4)
		qm.sendNextPrevS("Leave now!", 3);
	else if (status == 5)
		qm.sendNextPrevS("Aran! Get on board right now! I understand that you want to join them in the battle, but... it's too late! Let your friends take care of the Black Wizard, and you should get on right now and escape to Victoria Island!", 9);
	else if (status == 6)
		qm.sendNextPrevS("No, I can't do that!", 3);
	else if (status == 7) {
		qm.sendNextPrevS("Athena Pierce, you take care of these people and head over to Victoria Island. I promise you, I will not die. I will meet you there at the island soon. I better help my friends out and battle the Black Wizard once and for all!", 3);
	} else if (status == 8) {
		qm.warp(140090000);
		qm.dispose();
	}
}