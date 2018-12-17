var status = -1;

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		if(type == 1 && mode == 0) {
			qm.sendOk("Ah, okay. I understand. Heroes are very busy. *Sniff...* If you ever get any free time, though...");
			qm.dispose();
			return;
		}else{
			qm.dispose();
		return;
		}
	}
    if (status == 0) 
		qm.sendNext("I just heard someone say, 'The hero has returned!' Did I hear wrong, or is it...is it really true? So this...this person standing before me is the hero?!")		
    else if (status == 1) {
		qm.sendNextPrev("#i4001172#");
    } else if (status == 2) {
		qm.sendNextPrev("Oh this is unreal... I can't believe I am staring right at a hero of Maple World! It's an honor! Here, let me shake your hand...and if you could give me a hug... Hehe, could you please sign this, too?");
    } else if (status == 3) {
		qm.sendAcceptDecline("Ah, I'm so sorry. I was so happy to have finally met you that I guess I got a little carried away. Whew, deep breaths. Deep breaths. Okay, I feel better now. But um...can I ask you a favor? Please?");
    } else if (status == 4) {
		qm.forceStartQuest();
		qm.sendOk("My brother #bPuir #kis just down the street, and he's been dying to meet you! I know you're busy, but could you please stop by and say hello to Puir? Please...");
		qm.dispose();
	}
}

function end(mode, type, selection) {
	status++;
	if (mode != 1) {
		if(type == 1 && mode == 0) {
			qm.sendNext("*sniff sniff* Isn't this sword good enough for you, just for now? I'd be so honored...");
			qm.dispose();
		}else{
			qm.dispose();
			return;
		}  
	}		
	if (status == 0) 
		qm.sendNext("Wait, so you are... no way... are you the hero that #p1201000# had been preaching to us all this time? #p1201000#! Don't just nod like that! Tell me! Is this the hero you've been waiting for?");
	else if (status == 1) {   	
		qm.sendNextPrev("#i4001171#");
	} else if (status == 2) { 
		qm.sendNextPrev("I'm sorry. I'm just overcome with emotions, that's all. Sniff... but really, this is monumental... my gosh, I'm tearing up again... #p1201000#, you must be really happy, too.");
	} else if (status == 3) { 
		qm.sendNextPrev("I just noticed that you're not carrying any weapons. I heard that heroes carry their own set of weapons, and... oh, you must have lost it during the battle with the Black Wizard.");    
	} else if (status == 4) {  
		qm.sendYesNo("I know this is really nothing compared to your weapon, but #bjust carry this around for now#k. It's a gift for you. I don't like seeing a hero like you walking around without a weapon.\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#i1302000# #t1302000#1\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 35 exp");	
	} else if (status == 5) {
	if(qm.canHold(1302000)){
		qm.gainItem(1302000, 1);
		qm.gainExp(35);
		qm.forceCompleteQuest();
		qm.sendNext("#b(Even my skill was nothing like a hero's... and this sword feels really awkward as well. Did I really use one in the past? How do I even carry one around?)#k", 3);
	}else
		qm.dropMessage(1,"Your inventory is full");   
	} else if (status == 6) {
		qm.playerSummonMessage(16); 
		qm.dispose();
	}
}