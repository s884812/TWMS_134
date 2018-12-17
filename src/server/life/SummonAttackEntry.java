package server.life;

public class SummonAttackEntry {

    private MapleMonster mob;
    private int damage;

    public SummonAttackEntry(MapleMonster mob, int damage) {
	super();
	this.mob = mob;
	this.damage = damage;
    }

    public MapleMonster getMonster() {
	return mob;
    }

    public int getDamage() {
	return damage;
    }
}
