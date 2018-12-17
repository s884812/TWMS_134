function init() {
}

function setup(eim, leaderid) {
    var eim = em.newInstance("nmm" + leaderid);

    eim.setProperty("summoned", "0");

    var map = eim.createInstanceMap(802000111);
    map.killAllMonsters(false);
    map.removeNpc(9120026);

    var mob = em.getMonster(9400279);
    eim.registerMonster(mob);
    map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(334, 45));

    eim.startEventTimer(14400000); // 4 hrs
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(0);
    player.changeMap(map, map.getPortal(0));

    if (eim.getProperty("summoned").equals("0")) {
	player.dropMessage(5, "You will be removed from this map unless monsters are defeated within 20 minutes.");
    }
}

function playerRevive(eim, player) {
    return false;
}

function scheduledTimeout(eim) {
    eim.disposeIfPlayerBelow(100, 802000112);
}

function changedMap(eim, player, mapid) {
    if (mapid != 802000111) {
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
    eim.disposeIfPlayerBelow(100, 802000112);
}

function clearPQ(eim) {
    end(eim);
}

function allMonstersDead(eim) {
    var prop = eim.getProperty("summoned");

    if (prop.equals("0")) {
	eim.broadcastPlayerMsg(5, "Free me from Kamuna says Dunas!");

	eim.setProperty("summoned", "1");
	var map = eim.getMapInstance(0);
	var mob = em.getMonster(9400266);
	eim.registerMonster(mob);
	map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(334, 45));
    } else {
	var map = eim.getMapInstance(0);
	map.spawnNpc(9120026, new java.awt.Point(-472, -32));
    }
}

function leftParty (eim, player) {}
function disbandParty (eim) {}
function playerDead(eim, player) {}
function cancelSchedule() {}