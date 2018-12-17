/*
* Guild Quest 
*/

function init() {
    em.setProperty("started", "false");
    em.setProperty("3minutesTimer", "false");
    em.setProperty("guildid", "-1");
}

function monsterValue(eim, mobId) {
    return -1;
}

function setup() {
    em.setProperty("guildid", "-1");
    em.setProperty("started", "true");
    em.setProperty("3minutesTimer", "true");

    var eim = em.newInstance("GuildQuest");

    //shuffle reactors in two maps for stage 3
    var mapfact = em.getMapFactory(em.getWorld());
    mapfact.getMap(990000501).shuffleReactors();
    mapfact.getMap(990000502).shuffleReactors();

    //force no-respawn on certain map reactors
    mapfact.getMap(990000611).getReactorByName("").setDelay(-1);
    mapfact.getMap(990000620).getReactorByName("").setDelay(-1);
    mapfact.getMap(990000631).getReactorByName("").setDelay(-1);
    mapfact.getMap(990000641).getReactorByName("").setDelay(-1);
   
//    mapfact.getMap(990000641).getPortal(5).setScriptName("guildwaitingenter");

    var map = mapfact.getMap(990000000);
    eim.startEventTimer(180000); // 3 minutes
}

function scheduledTimeout(eim) {
    if (em.getProperty("3minutesTimer").equals("true")) {
	em.setProperty("3minutesTimer", "false");

	var party = eim.getPlayers();
	if (party.size() < 6) {
	    end(eim, "You need at least 6 people to begin the Guild Quest.");
	} else {
	    var iter = party.iterator();
	    while (iter.hasNext()) {
		iter.next().dropMessage(5, "The Guild Quest has begun.");
	    }
	    restartEventTimer(3600000);
	}
    } else {
	end(eim, "The time has run out, guild PQ will end.");
    }
}

function playerEntry(eim, player) {
    var map = em.getMapFactory(em.getWorld()).getMap(990000000);
    player.changeMap(map, map.getPortal(0));
}

function playerRevive(eim, player) {
    return false;
}

function playerDead(eim, player) {
}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
    if (player.getName().equals(eim.getProperty("leader"))) { //check for party leader
	//boot all players and end
	var map = em.getMapFactory(em.getWorld()).getMap(990001100);
	
	var iter = party.iterator();
	while (iter.hasNext()) {
	    var pl = iter.next();
	    pl.dropMessage(6, "The leader of the Guild Quest has disconnected, resulting in the remaining players being warped out.");
	    if (pl.equals(player)) {
		eim.unregisterPlayer(player);
	    } else {
		eim.unregisterPlayer(pl);
		pl.changeMap(map, map.getPortal(0));
	    }
	}
	em.setProperty("started", "false");
	eim.dispose();
    } else {
	eim.unregisterPlayer(player);
	if (party.size() < 6) {
	    end(eim, "There are no longer enough players to continue the Guild Quest, meaning the remaining players shall be warped out.");
	}
    }
}

function leftParty(eim, player) { //ignore for GQ
}

function disbandParty(eim) { //ignore for GQ
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    var map = eim.getMapFactory(em.getWorld()).getMap(990001100);
    player.changeMap(map, map.getPortal(0));

    var party = eim.getPlayers();
    if (party.size() < 6) { //five after player booted
	end(eim, "There are no longer enough players to continue the Guild Quest, meaning the remaining players shall be warped out.");
    }
}

function end(eim, msg) {
    var iter = eim.getPlayers().iterator();
    var map = eim.getMapFactory(em.getWorld()).getMap(990001100);

    while (iter.hasNext()) {
	var player = iter.next();
	player.dropMessage(6, msg);
	eim.unregisterPlayer(player);
	player.changeMap(map, map.getPortal(0));
    }
    em.setProperty("started", "false");
    eim.dispose();
}

function clearPQ(eim) {
    var iter = eim.getPlayers().iterator();
    var bonusMap = eim.getMapFactory(em.getWorld()).getMap(990001000);

    bonusMap.resetReactors();

    while (iter.hasNext()) { // Time is automatically processed
	iter.next().changeMap(bonusMap, bonusMap.getPortal(0));
    }
    em.setProperty("started", "false");
    eim.dispose();
}

function finish(eim) {
    var iter = eim.getPlayers().iterator();
    var map = eim.getMapFactory(em.getWorld()).getMap(990001100);

    while (iter.hasNext()) {
	var player = iter.next();
	eim.unregisterPlayer(player);
	player.changeMap(map, map.getPortal(0));
    }
    em.setProperty("started", "false");
    eim.dispose();
}

function allMonstersDead(eim) {
//do nothing; GQ has nothing to do with monster killing
}

function cancelSchedule() {
}

function timeOut() {
}