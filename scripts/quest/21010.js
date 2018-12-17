importPackage(Packages.client);

var status = -1;

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		if(type == 1 && mode == 0) {
			qm.sendNext("Oh, no need to decline my offer. It's no big deal. It's just a potion. Well, let me know if you change your mind.");
			qm.dispose();
		}else{
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendNext("Hmmm? What's human doing here? Wait, hey there #p1201000#. What brought you here? Oh and... do you know this person, #p1201000#? What? A hero?");
	} else if (status == 1) {
		qm.sendNextPrev("#i4001170#");
	} else if (status == 2) {
		qm.sendNextPrev("Wait, so I am looking at the very person that your race has been waiting for hundreds of years? Wow!! I could tell the hero looked a bit different from the rest...");
	} else if (status == 3) { 
		qm.askAcceptDecline("But because of that curse of the Black Wizard that got you trapped in ice for hundreds of years, you do look quite weak. #bHere's a potion for recovery. Please take it#k.");		
	} else if (status == 4) {
		if (qm.getPlayerStat("HP") >= 50) {
			//
		} 
		if (!qm.haveItem(2000022))
		qm.gainItem(2000022, 1);
		qm.forceStartQuest();
		qm.sendNext("Just drink it up first, then we'll continue our talk!", 9);
	} else if (status == 5) {
		qm.sendNextPrev("#b(Wait, how do I drink this? I don't remember...)#k", 3);
	} else if (status == 6) {
		qm.playerSummonMessage(14);
		qm.dispose();
	}
}

function end(mode, type, selection) {
	status++;
	if (mode != 1) {
		if(type == 1 && mode == 0)
			qm.dispose();
		else{
			qm.dispose();
			return;
		}
	}
	if (status == 0)
	if (qm.getPlayerStat("HP") < 50) {
		qm.sendNext("Don't feel like you need to save this potion for later use. Just drink it! It's not much, but it'll be enough to restore some of your HP.");
		qm.dispose();
	} else
		qm.sendNext("I've been searching through blocks of ice inside the cave in hopes of finding our hero, but... I didn't think I'd actually see one in front of me right now! The prophecy is correct! #p1201000#, you were right! Now that the hero has been resurrected, we won't have to worry about the Black Wizard anymore, right?");
	else if (status == 1)
		qm.sendNextPrev("Wait, I have been holding onto you for too long. I'm sorry, but I bet you other penguins will react the same way as I did. I know you're busy and all, but on your way to town, #bplease go strike up a conversation with other penguins#k. Everyone will be shocked if the hero is the one initiating a conversation with them!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#i2000022# #t2000022# 5\r\n#i2000023# #t2000023# 5\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 16 exp");
	else if (status == 2) {
	if(qm.canHold(2000022) && qm.canHold(2000023)){
			qm.gainExp(16);
			qm.gainItem(2000022, 3);
			qm.gainItem(2000023, 3);
		qm.forceCompleteQuest();
		qm.sendNextPrev("Wow, you managed to level up! That means you may have acquired skill points too. In the world of Maple, every level up means 3 skill points. Press #bK#k to open the skill window and find out.", 9);
	}else
		qm.dropMessage(1,"Your inventory is full");        
	} else if (status == 3) {
		qm.sendNextPrev("#b(These penguins are so nice to me in every way possible, yet I don't remember them one bit. I better check the skill window first... but how do I do that?)#k", 3);
	} else if (status == 4) {
		qm.playerSummonMessage(15);
		qm.dispose();
	}
}