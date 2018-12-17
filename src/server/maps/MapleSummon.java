package server.maps;

import java.awt.Point;

import client.MapleCharacter;
import client.MapleClient;
import client.GameConstants;
import client.SkillFactory;
import client.anticheat.CheatingOffense;
import tools.MaplePacketCreator;

public class MapleSummon extends AbstractAnimatedMapleMapObject {

    private final int ownerid;
    private final int skillLevel;
    private final int skill;
    private short hp;
    private SummonMovementType movementType;
    
    // Since player can have more than 1 summon [Pirate] 
    // Let's put it here instead of cheat tracker
    private int lastSummonTickCount;
    private byte Summon_tickResetCount;
    private long Server_ClientSummonTickDiff;

    public MapleSummon(final MapleCharacter owner, final int skill, final Point pos, final SummonMovementType movementType) {
	super();
	this.ownerid = owner.getId();
	this.skill = skill;
	this.skillLevel = owner.getSkillLevel(SkillFactory.getSkill(skill));
	if (skillLevel == 0) {
	    return;
	}
	this.movementType = movementType;
	setPosition(pos);

	if (!isPuppet()) { // Safe up 12 bytes of data, since puppet doesn't attack.
	    lastSummonTickCount = 0;
	    Summon_tickResetCount = 0;
	    Server_ClientSummonTickDiff = 0;
	}
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {
	client.getSession().write(MaplePacketCreator.spawnSummon(this, skillLevel, false));
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
	client.getSession().write(MaplePacketCreator.removeSummon(this, false));
    }

    public final int getOwnerId() {
	return ownerid;
    }

    public final int getSkill() {
	return skill;
    }

    public final short getHP() {
	return hp;
    }

    public final void addHP(final short delta) {
	this.hp += delta;
    }

    public final SummonMovementType getMovementType() {
	return movementType;
    }

    public final boolean isPuppet() {
	switch (skill) {
	    case 3111002:
	    case 3211002:
	    case 13111004:
	//case 4341006:
		return true;
	}
	return false;
    }

    public final boolean isSummon() {
	switch (skill) {
	    case 12111004:
	    case 2311006:
	    case 2321003:
	    case 2121005:
	    case 2221005:
	    case 5211001: // Pirate octopus summon
	    case 5211002:
	    case 5220002: // wrath of the octopi
	    case 13111004:
	    case 11001004:
	    case 12001004:
	    case 13001004:
	    case 14001005:
	case 15001004:
		return true;
	}
	return false;
    }

     public final int getSkillLevel() {
	return skillLevel;
    }

    @Override
    public final MapleMapObjectType getType() {
	return MapleMapObjectType.SUMMON;
    }

    public final void CheckSummonAttackFrequency(final MapleCharacter chr, final int tickcount) {
	final int tickdifference = (tickcount - lastSummonTickCount);
	if (tickdifference < GameConstants.getSummonAttackDelay(skill)) {
	    chr.getCheatTracker().registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
	}
	final long STime_TC = System.currentTimeMillis() - tickcount;
	final long S_C_Difference = Server_ClientSummonTickDiff - STime_TC;
	if (S_C_Difference > 200) {
	    chr.getCheatTracker().registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
	}
	Summon_tickResetCount++;
	if (Summon_tickResetCount > 4) {
	    Summon_tickResetCount = 0;
	    Server_ClientSummonTickDiff = STime_TC;
	}
	lastSummonTickCount = tickcount;
    }
}
