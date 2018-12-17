package server;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 * Note for this class : MapleCharacter reference must be removed immediately after cpq or upon dc.
 * @author Rob
 */
public class MapleCarnivalParty {

    private List<MapleCharacter> members = new LinkedList<MapleCharacter>();
    private MapleCharacter leader;
    private byte team;
    private short availableCP = 0, totalCP = 0;
    private boolean winner = false;

    public MapleCarnivalParty(final MapleCharacter owner, final List<MapleCharacter> members1, final byte team1) {
        leader = owner;
        members = members1;
        team = team1;

        for (final MapleCharacter chr : members) {
            chr.setCarnivalParty(this);
        }
    }

    public final MapleCharacter getLeader() {
        return leader;
    }

    public void addCP(MapleCharacter player, int ammount) {
        totalCP += ammount;
        availableCP += ammount;
        player.addCP(ammount);
    }

    public int getTotalCP() {
        return totalCP;
    }

    public int getAvailableCP() {
        return availableCP;
    }

    public void useCP(MapleCharacter player, int ammount) {
        availableCP -= ammount;
        player.useCP(ammount);
    }

    public List<MapleCharacter> getMembers() {
        return members;
    }

    public int getTeam() {
        return team;
    }

    public void warp(final MapleMap map, final String portalname) {
        for (MapleCharacter chr : members) {
            chr.changeMap(map, map.getPortal(portalname));
        }
    }

    public void warp(final MapleMap map, final int portalid) {
        for (MapleCharacter chr : members) {
            chr.changeMap(map, map.getPortal(portalid));
        }
    }

    public boolean allInMap(MapleMap map) {
        boolean status = true;
        for (MapleCharacter chr : members) {
            if (chr.getMap() != map) {
                status = false;
            }
        }
        return status;
    }

    public void removeMember(MapleCharacter chr) {
        members.remove(chr);
        chr.setCarnivalParty(null);
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean status) {
        winner = status;
    }

    public void displayMatchResult() {
        final String effect = winner ? "quest/carnival/win" : "quest/carnival/lose";

        for (final MapleCharacter chr : members) {
            chr.getClient().getSession().write(MaplePacketCreator.showEffect(effect));
        }

    }
}
