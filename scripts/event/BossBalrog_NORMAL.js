function init() {
    // After loading, ChannelServer
    em.setProperty("state", "0");
}

function setup(eim, leaderid) {
    em.setProperty("state", "1");
    // Setup the instance when invoked, EG : start PQ
    var eim = em.newInstance("BossBalrog_NORMAL" + leaderid);

    eim.startEventTimer(1800000);
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapFactory(em.getWorld()).getMap(105100300);
    player.changeMap(map, map.getPortal(0));
}

function changedMap(eim, player, mapid) {
    if (mapid != 105100300) {
	eim.unregisterPlayer(player);

	if (eim.disposeIfPlayerBelow(0, 0)) {
	    em.setProperty("state", "0");
	}
    }
}

function scheduledTimeout(eim) {
    eim.disposeIfPlayerBelow(100, 105100100);
    em.setProperty("state", "0");
}

function allMonstersDead(eim) {
    // When invoking unregisterMonster(MapleMonster mob) OR killed
    // Happens only when size = 0
}

function playerDead(eim, player) {
    // Happens when player dies
}

function playerRevive(eim, player) {
    // Happens when player's revived.
    // @Param : returns true/false
}

function playerDisconnected(eim, player) {
    return 0;
    // return 0 - Deregister player normally Dispose instance if there are zero player left
    // return x that is > 0 - Deregister player normally + Dispose instance if there x player or below
    // return x that is < 0 - Deregister player normally + Dispose instance if there x player or below, if it's leader = boot all
}

function monsterValue(eim, mobid) {
    // Invoked when a monster that's registered has been killed
    // return x amount for this player - "Saved Points"
}

function leftParty(eim, player) {
    // Happens when a player left the party
}

function disbandParty(eim, player) {
    // Happens when the party is disbanded by the leader.
}

function clearPQ(eim) {
    // Happens when the function EventInstanceManager.finishPQ() is invoked by NPC/Reactor script
}

function removePlayer(eim, player) {
    // Happens when the funtion NPCConversationalManager.removePlayerFromInstance() is invoked
}

function registerCarnivalParty(eim, carnivalparty) {
    // Happens when carnival PQ is started. - Unused for now.
}

function onMapLoad(eim, player) {
    // Happens when player change map - Unused for now.
}

function cancelSchedule() {
}