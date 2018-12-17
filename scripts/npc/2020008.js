/** 
	Tylus: Warrior 3rd job advancement
	El Nath: Chief's Residence (211000001)

	Custom Quest 100100, 100102
*/

var status = 0;
var job;

function start() {
    status = -1;
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
	if (!(cm.getJob() == 110 || cm.getJob() == 120 || cm.getJob() == 130)) {
	    if (cm.getQuestStatus(6192) == 1) {
		if (cm.getParty() != null) {
		    var ddz = cm.getEventManager("ProtectTylus");
		    if (ddz == null) {
			cm.sendOk("Unknown error occured");
		    } else {
			var prop = ddz.getProperty("TylusSummoned");
			if (prop == null || prop.equals("0")) {
			    ddz.startInstance(cm.getParty(), cm.getMap());
			} else {
			    cm.sendOk("Someone else is already trying to protect Tylus, please try again in a bit.");
			}
		    }
		} else {
		    cm.sendOk("Please form a party in order to protect Tylus!");
		}
	    } else {
		cm.sendOk("May #rOdin#k be with you!");
	    }
	    cm.dispose();
	    return;
	}
	cm.completeQuest(100100);
	cm.completeQuest(100102);
	if (cm.getQuestStatus(100102) == 2) {
	    cm.sendNext("You have proven to be worthy of the strength I will now bestow upon you.");
	} else if (cm.getQuestStatus(100102) == 1) {
	    cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
	    cm.dispose();
	} else if (cm.getQuestStatus(100100) == 2) {
	    cm.sendNext("I was right, your strength is truly excellent.");
	} else if (cm.getQuestStatus(100100) == 1) {
	    cm.sendOk("Well, well. Now go and see #bDances with Balrog#k. He will show you the way.");
	    cm.dispose();
	} else if ((cm.getJob() == 110 || cm.getJob() == 120 || cm.getJob() == 130) && cm.getPlayerStat("LVL") >= 70 && cm.getPlayerStat("RSP") <= (cm.getPlayerStat("LVL") - 70) * 3) {
	    cm.sendNext("You are indeed a strong one.");
	} else {
	    cm.sendOk("Please make sure that you have used all your 2nd job skill point before proceeding.");
	    cm.dispose();
	}
    } else if (status == 1) {
	if (cm.getQuestStatus(100102) == 2) {
	    if (cm.getJob() == 110) { // FIGHTER
		cm.changeJob(111); // CRUSADER
		cm.gainAp(5);
		cm.sendOk("You are now a #bCrusader#k. May #rOdin#k be with you!");
		cm.dispose();
	    } else if (cm.getJob() == 120) { // PAGE
		cm.changeJob(121); // WHITEKNIHT
		cm.gainAp(5);
		cm.sendOk("You are now a #bWhite Knight#k. May #rOdin#k be with you!");
		cm.dispose();
	    } else if (cm.getJob() == 130) { // SPEARMAN
		cm.changeJob(131); // DRAGONKNIGHT
		cm.gainAp(5);
		cm.sendOk("You are now a #bDragon Knight#k. May #rOdin#k be with you!");
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
	    cm.sendOk("Well, well. Now go and see #bDances with Balrog#k. He will show you the way.");
	    cm.dispose();
	}
    }
}