



var exitMap;
var waitingMap;
var reviveMap;
var fieldMap;
var winnerMap;
var loserMap;

var instanceId;

var blueParty = null; // team 1
var redParty = null; // team 0

var forfeit = false;
var hasStarted = false;
var eim;
var map;
function init() {
    instanceId = 1;
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    var mapf = em.getMapFactory(em.getWorld());

    exitMap = mapf.getMap(980000000); // <exit>
    waitingMap = mapf.getMap(980000300);
    reviveMap = mapf.getMap(980000302);
    fieldMap = mapf.getMap(980000301);
    winnerMap = mapf.getMap(980000303);
    loserMap = mapf.getMap(980000304);
    blueParty = null;
    redParty = null;
    var instanceName = "cpq3";
    eim = em.newInstance(instanceName);
    instanceId++;
    var portal = reviveMap.getPortal("pt00");
    portal.setScriptName("MCrevive3");
    //no time limit yet unless it becomes necessary
    //em.schedule("timeOut", 30 * 60000);
    em.setProperty("started", "false");
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(waitingMap, 0);
}

function registerCarnivalParty(eim, carnivalParty) {
    if (redParty == null) {
        redParty = carnivalParty;
        // display message about recieving invites for next 3 minutes;
        em.schedule("end", 3 * 60 * 1000); // 3 minutes
    } else {
        em.setProperty("started", "true");
        blueParty = carnivalParty;
        em.schedule("start", 10000);
    }
}

function playerDead(eim, player) {
    player.getCarnivalParty().useCP(10);
    for (i = 0; i< eim.getPlayerCount(); i++) {
        var chr = eim.getPlayers().get(i);
        chr.playerDiedCPQ(player.getName(), 10, player.getCarnivalParty().getTeam());
    }
}

function leftParty(eim, player) {
    if (em.getProperty("started") == "true") {
        warpOut();
    } else {
        for (i = 0; i< eim.getPlayerCount(); i++) {
            var player = eim.getPlayers().get(i);
            eim.unregisterPlayer(player);
            player.changeMap(exitMap, 0);
            player.getCarnivalParty().removeMember(player);
        }
        eim.dispose();
    }
}

function disbandParty(eim) {
    if (em.getProperty("started") == "true") {
        warpOut();
    } else {
        for (i = 0; i< eim.getPlayerCount(); i++) {
            var player = eim.getPlayers().get(i);
            eim.unregisterPlayer(player);
            player.changeMap(exitMap, 0);
            player.getCarnivalParty().removeMember(player);
        }
        eim.dispose();
    }
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.getCarnivalParty().removeMember(player);
    player.changeMap(exitMap, 0);
    if (eim.playerCount() < 1) {
        eim.dispose();
    }
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getCarnivalParty().removeMember(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function start() {
    eim.startEventTimer(10 * 60 * 1000);
    blueParty.warp(fieldMap, "blue00");
    redParty.warp(fieldMap, "red00");
}

function monsterKilled(eim, chr, cp) {
    chr.getCarnivalParty().addCP(chr, cp);
    chr.CPUpdate(false, chr.getAvailableCP(), chr.getTotalCP(), 0);
    for (i = 0; i < eim.getPlayerCount(); i++) {
        var player = eim.getPlayers().get(i);
        player.CPUpdate(true, chr.getCarnivalParty().getAvailableCP(), chr.getCarnivalParty().getTotalCP(), chr.getCarnivalParty().getTeam());
    }
}

function monsterValue(eim, mobId) {
    return 0;
}


function end() {
    if (em.getProperty("started") != "true") {
        for (i = 0; i< eim.getPlayerCount(); i++) {
            var player = eim.getPlayers().get(i);
            player.changeMap(exitMap, 0);
            player.getCarnivalParty().removeMember(player);
            eim.unregisterPlayer(player);
        }
        eim.dispose();
    }
}

function warpOut() {
    if (blueParty.isWinner()) {
        blueParty.warp(winnerMap, 0);
        redParty.warp(loserMap, 0);
    } else {
        redParty.warp(winnerMap, 0);
        blueParty.warp(loserMap, 0);
    }
    for (i = 0; i< eim.getPlayerCount(); i++) {
        var player = eim.getPlayers().get(i);
        eim.unregisterPlayer(player);
    }
    eim.dispose();
}

function scheduledTimeout(eim) {
    eim.stopEventTimer();
    if (blueParty.getTotalCP() > redParty.getTotalCP()) {
        blueParty.setWinner(true);
    } else if (redParty.getTotalCP() > blueParty.getTotalCP()) {
        redParty.setWinner(true);
    }
    blueParty.displayMatchResult();
    redParty.displayMatchResult();
    em.schedule("warpOut", 10000);
}

function playerRevive(eim, player) {
    player.changeMap(reviveMap, 0);
}

function playerDisconnected(eim, player) {
    player.setMap(exitMap);
    eim.unregisterPlayer(player);
    player.getCarnivalParty().removeMember(player);
    if (em.getProperty("started") != "true") {
        for (i = 0; i< eim.getPlayerCount(); i++) {
            var chr = eim.getPlayers().get(i);
            eim.unregisterPlayer(chr);
            chr.changeMap(exitMap, 0);
            chr.getCarnivalParty().removeMember(chr);
        }
        eim.dispose();
    }
    if (eim.playerCount() < 1) {
        eim.dispose();
    }
}

function onMapLoad(eim, chr) {
    if (chr.getCarnivalParty().getTeam() == 0) {
        chr.startMonsterCarnival(blueParty.getAvailableCP(), blueParty.getTotalCP());
    } else {
        chr.startMonsterCarnival(redParty.getAvailableCP(), redParty.getTotalCP());
    }
}

function cancelSchedule() {
}

function clearPQ(eim) {
}

function allMonstersDead(eim) {
}

function changedMap(eim, chr, mapid) {
}
