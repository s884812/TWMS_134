/*
	So Gong
	Mu Lung Training Center
*/

var status = -1;
var sel;
var mapid;

function start() {
	mapid = cm.getMapId();
	if (mapid == 925020001) {
		cm.sendSimple("My master is the most powerful man in Mu Lung. Are you telling me you're trying to challenge our great master?  Don't say I didn't warn you.\r\n#b#L0#I want to tackle him myself.#l\r\n#L1#I want to challenge him as a team.#l\r\n\r\n#L2#I want a belt.#l\r\n#L3#I want to reset my training points.#l\r\n#L4#I want to receive a medal.#l\r\n#L5#What's a Mu Lung Training Tower?#l");
	} else if (isRestingSpot(mapid)) {
		cm.sendSimple("I'm amazed to know that you've safely reached up to this level. I can guarantee you, however, that it won't get any easier. What do you think? Do you want to keep going?#b \n\r #L0# Yes, I'll keep going.#l \n\r #L1# I want out#l \n\r #L2# I want to save my progress on record.#l");
	} else {
		cm.sendYesNo("What? You're ready to quit already? You just need to move on to the next level. Are you sure you want to quit?");
	}
}

function action(mode, type, selection) {
	if (mapid == 925020001) {
		if (mode == 1) {
			status++;
		} else {
			cm.dispose();
		}
		if (status == 0) {
			sel = selection;
			if (sel == 5) {
				cm.sendNext("My master is the most powerful individual in Mu Lung, and he is responsible for erecting this amazing Mu Lung Training Tower. Mu Lung Training Tower is a colossal training facility that consists of 38 floors. Each floor represents additional levels of difficulty. Of course, with your skills, reaching the top floor will be impossible...");
				cm.dispose();
			} else if (sel == 4) {
				cm.sendNext("In order to earn #b#t1142034##k, you'll have to defeat #b97#k more. You can't stop now! Keep working! Oh, and like I said before, the master only counts the monster that he personally summoned in Mu Lung Training Tower. Oh, and one more thing. You're not just wiping out the monsters and immediately leave the stage. #rAfter defeating the monsters, if you don't move on to the next level, then it will not count as a victory.#k..");
				cm.dispose();
			} else if (sel == 3) {
				cm.sendYesNo("You know if you reset your training points, then it'll return to 0, right? I can honestly say that it's not necessarily a bad thing. Once you reset your training points and start over again, then you'll be able to receive the belts once more. Do you want to reset your training points?");
			} else if (sel == 2) {
				cm.sendSimple("Your total training points so far are #b"+cm.getDojoPoints()+"#k. Our master loves talented individuals, so if you rack up enough training points, you'll be able to receive a belt based on your training points.\r\n\r\n#L0##i1132000:# #t1132000##l\r\n#L1##i1132001:# #t1132001##l\r\n#L2##i1132002:# #t1132002##l\r\n#L3##i1132003:# #t1132003##l\r\n#L4##i1132004:# #t1132004##l");
			} else if (sel == 1) {
				if (cm.getParty() != null) {
					if (cm.isLeader()) {
						//if(cm.getParty() > 1) {
							if (cm.allMembersHere()) {
								cm.start_DojoAgent(true, true);
							} else {
								cm.sendOk("Seem that some of your party member are not here. You can't enter until your party member is ready");
							}
						//} else {
							//cm.sendOk("You want to challenge this party mission by yourself?");
						//}
					} else {
						cm.sendOk("Hey, you're not even a leader of your party. What are you doing trying to sneak in? Tell your party leader to talk to me if you want to enter the premise.");
					}
				} else {
					cm.sendOk("Hey, you're not even in a party. What are you doing trying to sneak in? Form a party and ask party leader to talk to me if you want to enter the premise.");
				}
				cm.dispose();
			} else if (sel == 0) {
				var record = cm.getQuestRecord(150000);
				var data = record.getCustomData();
				if (data != null) {
					cm.warp(get_restinFieldID(parseInt(data)), 0);
					record.setCustomData(null);
				} else {
					//cm.start_DojoAgent(true, false);
				}
				cm.sendOk("Mu Lung Training Center is currently in debug mode.");
				cm.dispose();
				//cm.sendYesNo("The last time you took the challenge yourself, you were able to reach Floor #18. I can take you straight to that floor, if you want. Are you interested?");
			}
		} else if (status == 1) {
			if (sel == 3) {
				cm.setDojoRecord(true);
				cm.sendOk("I have resetted your training points to 0.");
				cm.dispose();
			} else if (sel == 2) {
				var record = cm.getDojoRecord();
				var required = 0;
				switch (record) {
					case 0:
					required = 200;
					break;
					case 1:
					required = 1800;
					break;
					case 2:
					required = 4000;
					break;
					case 3:
					required = 9200;
					break;
					case 4:
					required = 17000;
					break;
				}
				if (record == selection && cm.getDojoPoints() >= required) {
					var item = 1132000 + record;
					if (cm.canHold(item)) {
						cm.gainItem(item, 1);
						cm.setDojoRecord(false);
					} else {
						cm.sendOk("Please check if you have any available slot in your inventory.");
					}
				} else {
					cm.sendOk("You either already have it or insufficient training points. Do try getting the weaker belts first.");
				}
				cm.dispose();
			}
		}
	} else if (isRestingSpot(mapid)) {
		if (mode == 1) {
			status++;
		} else if (status == 0 && mode == 0) {
			cm.dispose();
			return;
		}
		if (status == 0) {
			sel = selection;
			if (sel == 0) {
				cm.dojoAgent_NextMap(true, true);
				cm.dispose();
			} else if (sel == 1) {
				cm.askAcceptDecline("Do you want to quit? You really want to leave here?");
			} else if (sel == 2) {
				var stage = get_stageId(cm.getMapId());
				cm.getQuestRecord(150000).setCustomData(stage);
				cm.sendOk("I have just recorded your progress. The next time you get here, I'll sent you directly to this level.");
				cm.dispose();
			}
		} else if (status == 1) {
			if (sel == 1) {
				cm.warp(925020002);
				cm.playerMessage("Your indecisiveness is just ridiculous.");
			}
			cm.dispose();
		}
	} else {
		if (mode == 1) {
			cm.warp(925020002, 0);
			cm.playerMessage("Your indecisiveness is just ridiculous.");
		}
		cm.dispose();
	}
}

function get_restinFieldID(id) {
	switch (id) {
		case 1:
		return 925020600;
		case 2:
		return 925021200;
		case 3:
		return 925021800;
		case 4:
		return 925022400;
		case 5:
		return 925023000;
		case 6:
		return 925023600;
	}
	return 925020002;
}

function get_stageId(mapid) {
	if (mapid >= 925020600 && mapid <= 925020609) {
		return 1;
	} else if (mapid >= 925021200 && mapid <= 925021209) {
		return 2;
	} else if (mapid >= 925021800 && mapid <= 925021809) {
		return 3;
	} else if (mapid >= 925022400 && mapid <= 925022409) {
		return 4;
	} else if (mapid >= 925023000 && mapid <= 925023009) {
		return 5;
	} else if (mapid >= 925023600 && mapid <= 925023609) {
		return 6;
	}
	return 0;
}

function isRestingSpot(id) {
	// Resting rooms :
	// 925020600 ~ 925020609
	// 925021200 ~ 925021209
	// 925021800 ~ 925021809
	// 925022400 ~ 925022409
	// 925023000 ~ 925023009
	// 925023600 ~ 925023609
	var shortid = id / 100;
	switch (shortid) {
		case 9250206:
		case 9250212:
		case 9250218:
		case 9250224:
		case 9250230:
		case 9250236:
		return true;
	}
	return false;
}