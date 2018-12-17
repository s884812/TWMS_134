package server.life;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;

import server.maps.MapleMap;
import tools.MaplePacketCreator;

public class SpawnPoint extends Spawns {

    private MapleMonster monster;
    private Point pos;
    private long nextPossibleSpawn;
    private int mobTime;
    private AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private boolean immobile;
    private String msg;
    private byte carnivalTeam;

    public SpawnPoint(final MapleMonster monster, final Point pos, final int mobTime, final byte carnivalTeam, final String msg) {
	this.monster = monster;
	this.pos = pos;
	this.mobTime = mobTime * 1000;
	this.carnivalTeam = carnivalTeam;
	this.msg = msg;
	this.immobile = !monster.getStats().getMobile();
	this.nextPossibleSpawn = System.currentTimeMillis();
    }

    @Override
    public final MapleMonster getMonster() {
	return monster;
    }

    @Override
    public final byte getCarnivalTeam() {
	return carnivalTeam;
    }

    @Override
    public final boolean shouldSpawn() {
	if (mobTime < 0) {
	    return false;
	}
	// regular spawnpoints should spawn a maximum of 3 monsters; immobile spawnpoints or spawnpoints with mobtime a
	// maximum of 1
	if (((mobTime != 0 || immobile) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 1) {
	    return false;
	}
	return nextPossibleSpawn <= System.currentTimeMillis();
    }

    @Override
    public final MapleMonster spawnMonster(final MapleMap map) {
	final MapleMonster mob = new MapleMonster(monster);
	mob.setPosition(pos);
        mob.setCarnivalTeam(carnivalTeam);
	spawnedMonsters.incrementAndGet();
	mob.addListener(new MonsterListener() {

	    @Override
	    public void monsterKilled() {
		nextPossibleSpawn = System.currentTimeMillis();

		if (mobTime > 0) {
		    nextPossibleSpawn += mobTime;
		}
		spawnedMonsters.decrementAndGet();
	    }
	});
	map.spawnMonster(mob, -1);

	if (msg != null) {
	    map.broadcastMessage(MaplePacketCreator.serverNotice(6, msg));
	}
	return mob;
    }
}
