/* Robeira
	Magician 3rd job advancement
	El Nath: Chief's Residence (211000001)

	Custom Quest 100100, 100102
*/

var status = -1;
var job;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 1) {
	cm.sendOk("Make up your mind and visit me again.");
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;

    if (status == 0) {
	if (!(cm.getJob() != 210 || cm.getJob() != 220 || cm.getJob() != 230)) { // CLERIC
	    cm.sendOk("May #rOdin#k be with you!");
	    cm.dispose();
	    return;
	}
	cm.completeQuest(100100);
	cm.completeQuest(100102);
	if (cm.getQuestStatus(100102) == 2) {
	    cm.sendNext("Indeed, you have proven to be worthy of the strength I will now bestow upon you.");
	} else if (cm.getQuestStatus(100102) == 1) {
	    cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
	    cm.dispose();
	} else if (cm.getQuestStatus(100100) == 1) {
	    cm.sendNext("I was right, your strength is truly excellent.");
	} else if (cm.getQuestStatus(100100) == 1) {
	    cm.sendOk("Well, well. Now go and see #bGrendel the Really Old#k. He will show you the way.");
	    cm.dispose();
	} else if ((cm.getJob() == 210 || cm.getJob() == 220 || cm.getJob() == 230) && cm.getPlayerStat("LVL") >= 70 && cm.getPlayerStat("RSP") <= (cm.getPlayerStat("LVL") - 70) * 3) {
	    cm.sendNext("You are indeed a strong one.");
	} else {
	    cm.sendOk("Your time has yet to come...");
	    cm.dispose();
	}
    } else if (status == 1) {
	if (cm.getQuestStatus(100102) == 2) {
	    if (cm.getJob() == 210) { // FP
		cm.changeJob(211); // FP MAGE
		cm.gainAp(5);
		cm.sendOk("You are now a #bFire/Poison Mage#k. May #rOdin#k be with you!");
		cm.dispose();
	    } else if (cm.getJob() == 220) { // IL
		cm.changeJob(221); // IL MAGE
		cm.gainAp(5);
		cm.sendOk("You are now an #bIce/Lightning Mage#k. May #rOdin#k be with you!");
		cm.dispose();
	    } else if (cm.getJob() == 230) { // CLERIC
		cm.changeJob(231); // PRIEST
		cm.gainAp(5);
		cm.sendOk("You are now a #bPriest#k. May #rOdin#k be with you!");
		cm.dispose();
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
	    cm.dispose();
	} else {
	    cm.startQuest(100100);
	    cm.sendOk("Well, well. Now go and see #bGrendel the Really Old#k. He will show you the way.");
	    cm.dispose();
	}
    }
}