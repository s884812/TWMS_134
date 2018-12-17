function init() {
    em.setProperty("started", "false");
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    em.setProperty("started", "true");

    var eim = em.newInstance("KerningPQ");
    var map = eim.getMapFactory(em.getWorld()).getMap(103000800);

    map.getPortal("next00").setScriptName("kpq1");
    map.shuffleReactors();
    eim.startEventTimer(map, 1800000);

    var laststg = eim.getMapFactory(em.getWorld()).getMap(103000804);
    laststg.killAllMonsters(false);
    laststg.respawn(true);

    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapFactory(em.getWorld()).getMap(103000800);
    player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function changedMap(eim, player, mapid) {
    switch (mapid) {
	case 103000800: // 1st Stage
	case 103000801: // 2nd Stage
	case 103000802: // 3rd Stage
	case 103000803: // 4th Stage
	case 103000804: // 5th Stage
	    return; // Everything is fine
    //	case 103000805: // Bonus stage
    //	    eim.restartEventTimer(1800000); // 30 minutes
    //	    break;
    }
    eim.unregisterPlayer(player);

    if (eim.disposeIfPlayerBelow(0, 0)) {
	em.setProperty("started", "false");
    }
}

function playerRevive(eim, player) {
}

function playerDisconnected(eim, player) {
    return -2;
}

function leftParty(eim, player) {			
    // If only 2 players are left, uncompletable
    if (eim.disposeIfPlayerBelow(3, 103000890)) {
	em.setProperty("started", "false");
    } else {
	playerExit(eim, player);
    }
}

function disbandParty(eim) {
    // Boot whole party and end
    eim.disposeIfPlayerBelow(100, 103000890);

    em.setProperty("started", "false");
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);

    var exit = eim.getMapFactory(em.getWorld()).getMap(103000890);
    player.changeMap(exit, exit.getPortal(0));
}

function clearPQ(eim) {
    // KPQ does nothing special with winners
    eim.disposeIfPlayerBelow(100, 103000890);

    em.setProperty("started", "false");
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}