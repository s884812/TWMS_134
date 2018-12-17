package server.life;

import server.maps.MapleMap;

public abstract class Spawns {
    public abstract MapleMonster getMonster();
    public abstract byte getCarnivalTeam();
    public abstract boolean shouldSpawn();
    public abstract MapleMonster spawnMonster(MapleMap map);
}