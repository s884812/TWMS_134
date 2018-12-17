/* Arec
	Thief 3rd job advancement
	El Nath: Chief's Residence (211000001)

	Custom Quest 100100, 100102
*/

var status = -1;
var job;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 1) {
	    cm.sendOk("Make up your mind and visit me again.");
	    cm.safeDispose();
	    return;
	}
	status--;
    }

    if (status == 0) {
	if (!(cm.getJob() == 410 || cm.getJob() == 420)) {
	    cm.sendOk("May #rOdin#k be with you!");
	    cm.safeDispose();
	    return;
	}
	cm.completeQuest(100100);
	cm.completeQuest(100102);
	if (cm.getQuestStatus(100102) == 2) {
	    cm.sendNext("Indeed, you have proven to be worthy of the strength I will now bestow upon you.");
	} else if (cm.getQuestStatus(100102) == 2) {
	    cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
	    cm.safeDispose();
	} else if (cm.getQuestStatus(100100) == 2) {
	    cm.sendNext("#rBy Odin's raven!#k I was right, your strength is truly excellent.");
	} else if (cm.getQuestStatus(100100) == 1) {
	    cm.sendOk("Well, well. Now go and see #bthe Dark Lord#k. He will show you the way.");
	    cm.safeDispose();
	} else if ((cm.getJob() == 410 || cm.getJob() == 420) && cm.getPlayerStat("LVL") >= 70 && cm.getPlayerStat("RSP") <= (cm.getPlayerStat("LVL") - 70) * 3) {
	    cm.sendNext("You are indeed a strong one.");
	} else {
	    cm.sendOk("Your time has yet to come...");
	    cm.safeDispose();
	}
    } else if (status == 1) {
	if (cm.getQuestStatus(100102) == 2) {
	    if (cm.getJob() == 410) { // ASSASIN
		cm.changeJob(411); // HERMIT
		cm.gainAp(5);
		cm.sendOk("You are now a #bHermit#k. May #rOdin#k be with you!");
		cm.safeDispose();
	    } else if (cm.getJob() == 420) { // BANDIT
		cm.changeJob(421); // CDIT
		cm.gainAp(5);
		cm.sendOk("You are now a #bChief Bandit#k. May #rOdin#k be with you!");
		cm.safeDispose();
	    }
	} else if (cm.getQuestStatus(100100) == 2) {
	    cm.askAcceptDecline("Is your mind ready to undertake the final test?");
	} else {
	    cm.askAcceptDecline("But I can make you even stronger. Although you will have to prove not only your strength but your knowledge. Are you ready for the challenge?");
	}
    } else if (status == 2) {
	if (cm.getQuestStatus(100100) == 2) {
	    cm.startQuest(100102);
	    cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
	    cm.safeDispose();
	} else {
	    cm.startQuest(100100);
	    cm.sendOk("Well, well. Now go and see #bthe Dark Lord#k. He will show you the way.");
	    cm.safeDispose();
	}
    }
}
