/*
	Lakelis
	Kerning City
	Party Quest NPC
*/
function start() {
	if (cm.getParty() == null) {
		cm.sendOk("How about you and your party members collectively beating a quest? Here you'll find obstacles and problems where you won't be able to beat it without great teamwork.  If you want to try it, please tell the #bleader of your party#k to talk to me.");
	} else if (!cm.isLeader()) {
		cm.sendOk("If you want to try the quest, please tell the #bleader of your party#k to talk to me.");
	} else {
		var party = cm.getParty().getMembers();
		var mapId = cm.getMapId();
		var next = true;
		var levelValid = 0;
		var inMap = 0;
		var it = party.iterator();
		while (it.hasNext()) {
			var cPlayer = it.next();
			if ((cPlayer.getLevel() >= 21 && cPlayer.getLevel() <= 200) ) {
				levelValid += 1;
			} else {
				next = false;
			}
			if (cPlayer.getMapid() == mapId) {
				inMap += 1;
			}
		}
		if (party.size() < 1 || party.size() > 6 || inMap < 1) {
			next = false;
		}
		if (next) {
			var em = cm.getEventManager("KerningPQ");
			if (em == null) {
				cm.sendOk("This PQ is not currently available.");
			} else {
			var prop = em.getProperty("started");
			//var prop = false;
			if (prop == null || prop.equals("false")) {
				em.startInstance(cm.getParty(),cm.getMap());
			} else {
				cm.sendOk("Someone is already attempting on the quest.");
				cm.dispose();
			}
				cm.removeAll(4001008);
				cm.removeAll(4001007);
			}
		} else {
			cm.sendOk("Your party is not a party of four. Please make sure all your members are present and qualified to participate in this quest. I see #b" + levelValid.toString() + "#k members are in the right level range, and #b" + inMap.toString() + "#k are in Kerning. If this seems wrong, #blog out and log back in,#k or reform the party.");
		}
	}
	cm.dispose();
}

function action(mode, type, selection) {
	//
}