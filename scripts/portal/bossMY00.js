function enter(pi) {
    if (!pi.haveItem(4032246)) {
	pi.playerMessage(5, "You do not have the Spirit of Fantasy Theme park.");
    } else {
	if (pi.getPlayerCount(551030200) <= 0) { // Fant. Map
	    var FantMap = pi.getMap(551030200);

	    FantMap.resetReactors();
	    FantMap.killAllMonsters(false);

	    pi.playPortalSE();
	    pi.warp(551030200, "sp");
	} else {
	    if (pi.getMonsterCount(551030200) <= 0) {
		pi.playPortalSE();
		pi.warp(551030200, "sp");
	    } else {
		pi.playerMessage(5, "The battle against the boss has already begun, so you may not enter this place.");
	    }
	}
    }
}