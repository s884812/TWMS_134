// @Author ElternalFire
// FirstPQ - Level 1-8 BossPQ
// Made by : ElternalFire

importPackage(net.sf.odinms.world);
importPackage(net.sf.odinms.client);

var exitMap;
var instanceId;
var monster;
monster = new Array(
	100100, // Snail,
	100101, // Blue Snail
	1110100, // Green Mushroom
	1110101, // Dark Stump
	1120100, // Octopus
	1130100, // Axe Stump
	1210100, // Pig
	1210103, // Bubbling
	2130100, // Dark Axe Stump
	2220100, // Blue Mushroom
	2230101, // Zombie Mushroom
	2230102, // Wild Boar
	2230100, // Evil Eye
	2230110, // Wooden Mask
	2230111, // Rocky Mask
	2300100, // Stringe
	3000000, // Sentitel
	3000005, // Brown Teddy
	3110101, // Pink Teddy
	3110102, // Ratz
	3230200 // Star Pixie
); 


function init() {
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup(partyid) {
	exitMap = em.getChannelServer().getMapFactory().getMap(200000000);
	var instanceName = "FirstPQ" + partyid;

	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	var map = mf.getMap(240060200, false, true, false);
	map.toggleDrops();

	eim.setProperty("points", 0);
	eim.setProperty("monster_number", 0);

	eim.schedule("beginQuest", 5000);
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(240060200);
	player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { 
	player.setHp(player.getMaxHp());
	playerExit(eim, player);
	return false;
}

function playerDisconnected(eim, player) {
	removePlayer(eim, player);
}

function leftParty(eim, player) {			
	playerExit(eim, player);
}

function disbandParty(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
}

function playerExit(eim, player) {
	var party = eim.getPlayers();
	var dispose = false;
	if (party.size() == 1) {
		dispose = true;
	}
	eim.saveBossQuestPoints(parseInt(eim.getProperty("points")), player);
	player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.serverNotice(6, "[FirstPQ] Your current points have been awarded, spend them as you wish. Better luck next time!"));
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
	if (dispose) {
		eim.dispose();
	}
}

function removePlayer(eim, player) {
	var party = eim.getPlayers();
	var dispose = false;
	if (party.size() == 1) {
		dispose = true;
	}
	eim.saveBossQuestPoints(parseInt(eim.getProperty("points")), player);
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
	if (dispose) {
		eim.dispose();
	}
}

function clearPQ(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
}

function allMonstersDead(eim) {
	var monster_number = parseInt(eim.getProperty("monster_number"));
	var points = parseInt(eim.getProperty("points"));
	
	var monster_end = java.lang.System.currentTimeMillis();
	var monster_time = Math.round((monster_end - parseInt(eim.getProperty("monster_start"))) / 1000);

	if (1200 - monster_time <= 0) points += monster_number * 20000;
	else points += (monster_number * 20000) + ((1200 - monster_time) * (monster_number + 1));
	
	monster_number++;

	if (monster_number > 20) {
		points += 7500000;
	}
	
	eim.setProperty("points", points);
	eim.setProperty("monster_number", monster_number);
	
	var map = eim.getMapInstance(240060200);

	if (monster_number > 20) {
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			party.get(i).finishAchievement(2);
		}
		map.broadcastMessage(net.sf.odinms.tools.MaplePacketCreator.serverNotice(6, "[FirstPQ] Congratulations! Your team has defeated all the bosses with " + points + " points!"));
		map.broadcastMessage(net.sf.odinms.tools.MaplePacketCreator.serverNotice(6, "[FirstPQ] The points have been awarded, spend them as you wish."));
		disbandParty();
	}
	else {
		map.broadcastMessage(net.sf.odinms.tools.MaplePacketCreator.serverNotice(6, "[FirstPQ] Your team now has " + points + " points! The next boss will spawn in 10 seconds."));
		map.broadcastMessage(net.sf.odinms.tools.MaplePacketCreator.getClock(10));
		eim.schedule("monsterSpawn", 10000);
	}
}

function monsterSpawn(eim) {
	var mob = net.sf.odinms.server.life.MapleLifeFactory.getMonster(monster[parseInt(eim.getProperty("monster_number"))]);
	var overrideStats = new net.sf.odinms.server.life.MapleMonsterStats();

	if (parseInt(eim.getProperty("monster_number")) > 17) overrideStats.setHp(mob.getHp() * 1.5);
	else overrideStats.setHp(mob.getHp() * 2);

	overrideStats.setExp(mob.getExp());
	overrideStats.setMp(mob.getMaxMp());
	mob.setOverrideStats(overrideStats);

	if (parseInt(eim.getProperty("monster_number")) > 17) mob.setHp(Math.floor(mob.getHp() * 1.5));
	else mob.setHp(mob.getHp() * 2);

	eim.registerMonster(mob);

	var map = eim.getMapInstance(240060200);
	map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(100, 100));
	eim.setProperty("monster_start", java.lang.System.currentTimeMillis());
}

function beginQuest(eim) {
	var map = eim.getMapInstance(240060200);
	map.broadcastMessage(net.sf.odinms.tools.MaplePacketCreator.serverNotice(6, "[FirstPQ] The creatures of the darkness are coming in 30 seconds. Prepare for the worst!"));
	eim.schedule("monsterSpawn", 30000);
	map.broadcastMessage(net.sf.odinms.tools.MaplePacketCreator.getClock(30));
}

function cancelSchedule() {
}