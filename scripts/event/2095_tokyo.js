function init() {
}

function setup(eim, leaderid) {
    var eim = em.newInstance("2095_tokyo" + leaderid);

    var map = eim.createInstanceMap(802000311);
    map.killAllMonsters(false);

    eim.startEventTimer(1200000); // 20 min
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(0);
    player.changeMap(map, map.getPortal(0));
}

function playerRevive(eim, player) {
    return false;
}

function scheduledTimeout(eim) {
    eim.disposeIfPlayerBelow(100, 802000312);
}

function changedMap(eim, player, mapid) {
    if (mapid != 802000311 && (mapid == 802000313 || mapid == 802000310)) {
	eim.unregisterPlayer(player);

	eim.disposeIfPlayerBelow(100, mapid);
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

    eim.disposeIfPlayerBelow(0, 0);
}

function end(eim) {
    eim.disposeIfPlayerBelow(100, 0);
}

function clearPQ(eim) {
    end(eim);
}

function allMonstersDead(eim) {}
function leftParty (eim, player) {}
function disbandParty (eim) {}
function playerDead(eim, player) {}
function cancelSchedule() {}