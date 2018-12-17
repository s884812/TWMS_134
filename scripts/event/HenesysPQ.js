importPackage(client);
importPackage(handling);
importPackage(tools);

var exitMap;
var minPlayers = 0;
var pqTime = 600;

function init() {
    instanceId = 1;
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    // exit = 910010300
    var map = em.getChannelServer().getMapFactory(em.getWorld()).getMap(910010000);
    map.shuffleReactors();

    return eim;
}

function playerEntry(eim, player) {
    var map = em.getChannelServer().getMapFactory(em.getWorld()).getMap(910010000);
    player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
    if (player.isAlive()) { //trigger on manual revive
	if (eim.isLeader(player)) { //it checks for party leader
	    //boot whole party and end
	    var party = eim.getPlayers();
	    for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	    }
	    eim.dispose();
	}
	else { //boot dead player
	    // if its only 2 ppl left its uncompletable:
	    var party = eim.getPlayers();
	    if (party.size() < minPlayers) {
		for (var i = 0; i < party.size(); i++) {
		    playerExit(eim,party.get(i));
		}
		eim.dispose();
	    }
	    else
		playerExit(eim, player);
	}
    }
}

function playerDisconnected(eim, player) {
    return -2;
}

function leftParty(eim, player) {			
    // If only 2 players are left, uncompletable:
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
	for (var i = 0; i < party.size(); i++) {
	    playerExit(eim,party.get(i));
	}
	eim.dispose();
    }
    else
	playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
	playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    //like gms if you complete only XP as reward no items/bonus:
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
	playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function cancelSchedule() {
}

function timeOut(eim) {
    if (eim.getPlayerCount() > 0) {
	var pIter = eim.getPlayers().iterator();
	while (pIter.hasNext()) {
	    playerExit(eim, pIter.next());
	}
    }
    eim.dispose();

}