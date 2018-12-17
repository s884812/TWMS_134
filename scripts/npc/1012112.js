/* Tory , Henesys PQ
*/

var typeTo;
var map;

function start() {
    map = cm.getMapId();
    switch (map) {
	case 100000200: {
	    if (cm.getParty() == null) {
		cm.sendNext("Hi there! I'm Tory. This place is covered with mysterious aura of the full moon, and no one person can enter here by him/herself.");
		typeTo = 0;
	    } else {
		cm.sendNext("Hi there! I'm Tory. Inside here is a beautiful hill where the primrose blooms. There's a tiger that lives in the hill, Growlie, and he seems to be looking for something to eat.");
		typeTo = 1;
	    }
	    break;
	}
	default:
	    cm.dispose();
	    break;
    }
}

function action(mode, type, selection) {
    if (mode == -1) {
	cm.dispose();
    } else {
	if (mode == 1) {
	    typeTo++;
	}
	switch (typeTo) {
	    case 1:
		cm.sendPrev("If you'd like to enter here, the leader of your party will have to talk to me. Talk to your party leader about this.");
		cm.dispose();
		break;
	    case 2:
		cm.sendSimple("Would you like to head over to the hill of primrose and join forces with your party members to help Growlie out? \n\r #b#L0# Yes, I will go.#l");
		break;
	    case 3: {
		var party = cm.getParty().getMembers();
		var next = true;
		var levelValid = 0;
		var inMap = 0;
/*		if (party.size() < 3 || party.size() > 6) {
		    next = false;
		} else {
		    for (var i = 0; i < party.size() && next; i++) {
			if (party.get(i).getLevel() >= 10) {
			    levelValid += 1;
			}
			if (party.get(i).getMapid() == map) {
			    inMap += 1;
			}
		    }
		    if (levelValid < 3 || inMap < 3) {
			next = false;
		    }
		}*/
		if (next) {
		    var em = cm.getEventManager("HenesysPQ");
		    if (em == null) {
			cm.sendOk("#rError#k: HenesysPQ is unavailable at the moment. Please try again later.");
		    } else {
			em.startInstance(cm.getParty(), cm.getMap());
		    }
		} else {
		    cm.sendNext("I'm sorry, but the party you're a member of does NOT consist of at least 3 members. Please adjust your party to make sure that your party consists of at least 3 members that are all at Level 10 or higher. Let me know when you're done.");
		}
		cm.dispose();
		break;
	    }
	}
    }

/*    if (mode == -1) {
	cm.dispose();
    } else {
	if (mode == 0 && status == 0) {
	    cm.dispose();
	    return;
	}
	if (mode == 1)
	    status++;
	else
	    status--;
	
	if (cm.getMapId() == 100000200) {
	    if (status == 0) {
		cm.sendNext("This is the #rPrimrose Hill#k. When there is a full moon the #rMoon Bunny#k comes to make #bRice Cakes#k. #rGrowlie#k wants #bRice Cakes#k so you had better go help him or he will eat you.");
	    } else if (status == 1) {
		cm.sendSimple("Would you like to go help Growlie?#b\r\n#L0#Yes, I will go.#l#k");
	    } else if (status == 2) {
		if (cm.getParty() == null) {
		    cm.sendOk("You are not in a party.");
		    cm.dispose();
		    return;
		}
		if (!cm.isLeader()) {
		    cm.sendOk("You are not the party leader.");
		    cm.dispose();
		} else {
		    var party = cm.getParty().getMembers();
		    var mapId = cm.getMapId();
		    var next = true;
		    var levelValid = 0;
		    var inMap = 0;
		    if (party.size() < minPlayers || party.size() > maxPlayers)
			next = false;
		    else {
			for (var i = 0; i < party.size() && next; i++) {
			    if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
				levelValid += 1;
			    if (party.get(i).getMapid() == mapId)
				inMap += 1;
			}
			if (levelValid < minPlayers || inMap < minPlayers)
			    next = false;
		    }
		    if (next) {
			var em = cm.getEventManager("HenesysPQ");
			if (em == null) {
			    cm.sendOk("#rError#k: HenesysPQ is unavailable at the moment. Please try again later.");
			    cm.dispose();
			} else {
			    em.startInstance(cm.getParty(), cm.getMap());
			    var party = cm.getEventInstance().getPlayers();
			}
			cm.dispose();
		    } else {
			cm.sendOk("Your party is not a party of three to six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
			cm.dispose();
		    }
		}
	    }
	} else if (cm.getMapId() == 910010400) {
	    if (status == 0){
		cm.warp(100000200);
		cm.playerMessage("You have been warped to #rHenesys Park#k.");
		cm.dispose();
	    }
	} else if (cm.getMapId() == 910010100) {
	    if (status==0) {
		cm.sendYesNo("Would you like go to #rHenesys Park#k?");
	    } else if (status == 1) {
		cm.warp(100000200, 0);
		cm.dispose();
	    }
	}
    }*/
}