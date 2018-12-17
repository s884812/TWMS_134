/*
	Red Sign - 101st Floor Eos Tower (221024500)
*/

var status = -1;
var minLevel = 35; // 35
var maxLevel = 200; // 65

var minPartySize = 1;
var maxPartySize = 6;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }

    if (status == 0) {
	if (cm.getParty() == null) { // No Party
	    cm.sendOk("How about you and your party members collectively beating a quest? Here you'll find obstacles and problems where you won't be able to beat it unless with great teamwork. If you want to try it, please tell the #bleader of your party#k to talk to me.\r\n\r\n#rRequirements: " + minPartySize + " Party Members, all between level " + minLevel + " and level " + maxLevel + ".");
	    cm.safeDispose();
	} else if (!cm.isLeader()) { // Not Party Leader
	    cm.sendOk("If you want to try the quest, please tell the #bleader of your party#k to talk to me.");
	    cm.safeDispose();
	} else {
	    // Check if all party members are within PQ levels
	    var party = cm.getParty().getMembers();
	    var mapId = cm.getMapId();
	    var next = true;
	    var levelValid = 0;
	    var inMap = 0;
	    var it = party.iterator();

	    while (it.hasNext()) {
		var cPlayer = it.next();
		if ((cPlayer.getLevel() >= minLevel) && (cPlayer.getLevel() <= maxLevel)) {
		    levelValid += 1;
		} else {
		    next = false;
		}
		if (cPlayer.getMapid() == mapId) {
		    inMap += 1;
		}
	    }
	    if (party.size() < minPartySize || party.size() > maxPartySize || inMap < minPartySize) {
		next = false;
	    }
	    if (next) {
		var em = cm.getEventManager("LudiPQ");
		if (em == null) {
		    cm.sendOk("The Ludibrium PQ has encountered an error. Please report this on the forums, with a screenshot.");
		    cm.safeDispose();
		} else {
		    var prop = em.getProperty("started");
		    if (prop.equals("false") || prop == null) {
			em.startInstance(cm.getParty(), cm.getMap());
			cm.removeAll(4001022);
			cm.removeAll(4001023);
			cm.dispose();
		    } else {
			cm.sendNext("Another party has already entered the #rParty Quest#k in this channel. Please try another channel, or wait for the current party to finish.");
			cm.safeDispose();
		    }
		}
	    } else {
		cm.sendNext("Your party is invalid. Please adhere to the following requirements:\r\n\r\n#rRequirements: " + minPartySize + " Party Members, all between level " + minLevel + " and level " + maxLevel + ".");
		cm.safeDispose();
	    }
	}
    }
}