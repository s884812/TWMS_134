package server.maps;

import java.awt.Point;
import java.awt.Rectangle;

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import handling.MaplePacket;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MobSkill;
import tools.MaplePacketCreator;

public class MapleMist extends AbstractMapleMapObject {

    private Rectangle mistPosition;
    private MapleCharacter owner = null; // Assume this is removed, else weakref will be required
    private MapleMonster mob = null;
    private MapleStatEffect source;
    private MobSkill skill;
    private boolean isMobMist, isPoisonMist;
    private int skillDelay, skilllevel;

    public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
	this.mistPosition = mistPosition;
	this.mob = mob;
	this.skill = skill;
	skilllevel = skill.getSkillId();

	isMobMist = true;
	isPoisonMist = true;
	skillDelay = 0;
    }

    public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
	this.mistPosition = mistPosition;
	this.owner = owner;
	this.source = source;
	this.skilllevel = owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId()));

	switch (source.getSourceId()) {
	    case 4221006: // Smoke Screen
		isMobMist = false;
		isPoisonMist = false;
		skillDelay = 8;
		break;
	    case 2111003: // FP mist
	    case 12111005: // Flame wizard, [Flame Gear]
		isMobMist = false;
		isPoisonMist = true;
		skillDelay = 8;
		break;
	}
    }

    @Override
    public MapleMapObjectType getType() {
	return MapleMapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
	return mistPosition.getLocation();
    }

    public ISkill getSourceSkill() {
	return SkillFactory.getSkill(source.getSourceId());
    }

    public boolean isMobMist() {
	return isMobMist;
    }

    public boolean isPoisonMist() {
	return isPoisonMist;
    }

    public int getSkillDelay() {
	return skillDelay;
    }

    public int getSkillLevel() {
	return skilllevel;
    }

    public MapleMonster getMobOwner() {
	return mob;
    }

    public MapleCharacter getOwner() {
	return owner;
    }

    public MobSkill getMobSkill() {
	return this.skill;
    }

    public Rectangle getBox() {
	return mistPosition;
    }

    @Override
    public void setPosition(Point position) {
    }

    public MaplePacket fakeSpawnData(int level) {
	if (owner != null) {
	    return MaplePacketCreator.spawnMist(this);
	}
	return MaplePacketCreator.spawnMist(this);
    }

    @Override
    public void sendSpawnData(final MapleClient c) {
	c.getSession().write(MaplePacketCreator.spawnMist(this));
    }

    @Override
    public void sendDestroyData(final MapleClient c) {
	c.getSession().write(MaplePacketCreator.removeMist(getObjectId()));
    }

    public boolean makeChanceResult() {
	return source.makeChanceResult();
    }
}