function enter(pi) {
	if (pi.getPlayerCount(950101010) == 0) {
		var map = pi.getMap(950101010);
		map.killAllMonsters(false);
		if (pi.getParty() != null) {
			if (pi.isLeader()) {
				if (pi.allMembersHere()) {
					pi.playerMessage("Entering the Sanctuary of Evil");
					pi.warpParty(950101010, 3);
				} else {
					pi.playerMessage("Sorry but seem that some of your party member isn't here.");
				}
			} else {
				pi.playerMessage("Sorry but only leader of this party can enter.");
			}
		} else {
			pi.playerMessage("Entering the Sanctuary of Evil");
			pi.warp(950101010, 3);
		}
	} else {
		pi.playerMessage("Someone is already attempting to defeat the boss. Better come back later.");
	}
}