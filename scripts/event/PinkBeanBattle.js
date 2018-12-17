function init() {
    em.setProperty("started", "false");
}

function setup(eim, leaderid) {
    var eim = em.newInstance("Pinkbean");

    em.setProperty("started", "true");

    eim.startEventTimer(14400000); // 1 hr
    return eim;
}

function playerEntry(eim, player) {
    var map = em.getMapFactory(em.getWorld()).getMap(270050100);
    player.changeMap(map, map.getPortal(0));
}

function playerRevive(eim, player) {
    return false;
}

function scheduledTimeout(eim) {
    eim.disposeIfPlayerBelow(100, 270050300);

    em.setProperty("started", "false");
}

function changedMap(eim, player, mapid) {
    if (mapid != 270050100) {
	eim.unregisterPlayer(player);

	if (eim.disposeIfPlayerBelow(0, 0)) {
	    em.setProperty("started", "false");
	}
    }
}

function playerDisconnected(eim, player) {
    return 0;
}

function monsterValue(eim, mobId) {
    return 1;
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);

    if (eim.disposeIfPlayerBelow(0, 0)) {
	em.setProperty("started", "false");
    }
}

function end(eim) {
    if (eim.disposeIfPlayerBelow(100, 270050300)) {
	em.setProperty("started", "false");
    }
}

function clearPQ(eim) {
    end(eim);
}

function allMonstersDead(eim) {
}

function leftParty (eim, player) {}
function disbandParty (eim) {}
function playerDead(eim, player) {}
function cancelSchedule() {}