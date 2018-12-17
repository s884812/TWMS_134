function init() {
}

function setup(eim, leaderid) {
    var eim = em.newInstance("Dunas2" + leaderid);

    eim.setProperty("dunas_summoned", "false");

    var map = eim.createInstanceMap(802000711);
    map.killAllMonsters(false);

    var mob = em.getMonster(9400293); // Dunas Unit
    eim.registerMonster(mob);
    map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(-829, 43));

    eim.startEventTimer(14400000); // 4 hrs
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
    eim.disposeIfPlayerBelow(100, 802000712);
}

function changedMap(eim, player, mapid) {
    if (mapid != 802000711) {
	eim.unregisterPlayer(player);

	eim.disposeIfPlayerBelow(0, 0);
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

function allMonstersDead(eim) {
    if (eim.getProperty("dunas_summoned").equals("false")) {
	eim.setProperty("dunas_summoned", "true");
	var mob = em.getMonster(9400294);
	eim.registerMonster(mob);
	eim.getMapInstance(0).spawnMonsterOnGroundBelow(mob, new java.awt.Point(-1787, 335));
    } else {
	eim.getMapInstance(0).spawnNpc(9120026, new java.awt.Point(-405, 335));
    }
}

function leftParty (eim, player) {}
function disbandParty (eim) {}
function playerDead(eim, player) {}
function cancelSchedule() {}