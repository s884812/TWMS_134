package server.maps;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import handling.world.MaplePartyCharacter;
import server.MaplePortal;
import tools.MaplePacketCreator;

public class MapleDoor extends AbstractMapleMapObject {

    private MapleCharacter owner;
    private MapleMap town;
    private MaplePortal townPortal;
    private MapleMap target;
    private Point targetPosition;

    public MapleDoor(final MapleCharacter owner, final Point targetPosition) {
	super();
	this.owner = owner;
	this.target = owner.getMap();
	this.targetPosition = targetPosition;
	setPosition(this.targetPosition);
	this.town = this.target.getReturnMap();
	this.townPortal = getFreePortal();
    }

    public MapleDoor(final MapleDoor origDoor) {
	super();
	this.owner = origDoor.owner;
	this.town = origDoor.town;
	this.townPortal = origDoor.townPortal;
	this.target = origDoor.target;
	this.targetPosition = origDoor.targetPosition;
	this.townPortal = origDoor.townPortal;
	setPosition(townPortal.getPosition());
    }

    private final MaplePortal getFreePortal() {
	final List<MaplePortal> freePortals = new ArrayList<MaplePortal>();

	for (final MaplePortal port : town.getPortals()) {
	    if (port.getType() == 6) {
		freePortals.add(port);
	    }
	}
	Collections.sort(freePortals, new Comparator<MaplePortal>() {

	    @Override
	    public final int compare(final MaplePortal o1, final MaplePortal o2) {
		if (o1.getId() < o2.getId()) {
		    return -1;
		} else if (o1.getId() == o2.getId()) {
		    return 0;
		} else {
		    return 1;
		}
	    }
	});
	for (final MapleMapObject obj : town.getAllDoor()) {
	    final MapleDoor door = (MapleDoor) obj;
	    if (door.getOwner().getParty() != null
		    && owner.getParty().containsMembers(new MaplePartyCharacter(door.getOwner()))) {
		freePortals.remove(door.getTownPortal());
	    }
	}
	return freePortals.iterator().next();
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {
	if (target.getId() == client.getPlayer().getMapId() || owner == client.getPlayer() && owner.getParty() == null) {
	    client.getSession().write(MaplePacketCreator.spawnDoor(owner.getId(), town.getId() == client.getPlayer().getMapId() ? townPortal.getPosition() : targetPosition, true));
	    if (owner.getParty() != null && (owner == client.getPlayer() || owner.getParty().containsMembers(new MaplePartyCharacter(client.getPlayer())))) {
		client.getSession().write(MaplePacketCreator.partyPortal(town.getId(), target.getId(), targetPosition));
	    }
	    client.getSession().write(MaplePacketCreator.spawnPortal(town.getId(), target.getId(), targetPosition));
	}
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
	if (target.getId() == client.getPlayer().getMapId() || owner == client.getPlayer() || owner.getParty() != null && owner.getParty().containsMembers(new MaplePartyCharacter(client.getPlayer()))) {
	    if (owner.getParty() != null && (owner == client.getPlayer() || owner.getParty().containsMembers(new MaplePartyCharacter(client.getPlayer())))) {
		client.getSession().write(MaplePacketCreator.partyPortal(999999999, 999999999, new Point(-1, -1)));
	    }
	    client.getSession().write(MaplePacketCreator.removeDoor(owner.getId(), false));
	    client.getSession().write(MaplePacketCreator.removeDoor(owner.getId(), true));
	}
    }

    public final void warp(final MapleCharacter chr, final boolean toTown) {
	if (chr == owner || owner.getParty() != null && owner.getParty().containsMembers(new MaplePartyCharacter(chr))) {
	    if (!toTown) {
		chr.changeMap(target, targetPosition);
	    } else {
		chr.changeMap(town, townPortal);
	    }
	} else {
	    chr.getClient().getSession().write(MaplePacketCreator.enableActions());
	}
    }

    public final MapleCharacter getOwner() {
	return owner;
    }

    public final MapleMap getTown() {
	return town;
    }

    public final MaplePortal getTownPortal() {
	return townPortal;
    }

    public final MapleMap getTarget() {
	return target;
    }

    public final Point getTargetPosition() {
	return targetPosition;
    }

    @Override
    public final MapleMapObjectType getType() {
	return MapleMapObjectType.DOOR;
    }
}
