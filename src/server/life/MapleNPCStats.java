package server.life;

import java.util.Map;
import java.util.HashMap;

public class MapleNPCStats {

    private String name;
    private Map<Byte, Integer> equips;
    private int face, hair;
    private byte skin;
    private int FH, RX0, RX1, CY;

    public MapleNPCStats(String name, boolean playerNpc) {
	this.name = name;
	
	if (playerNpc) {
	    equips = new HashMap<Byte, Integer>();
	}
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    // Player NPC shiet!

    public Map<Byte, Integer> getEquips() {
        return equips;
    }

    public int getFH() {
        return FH;
    }

    public int getRX0() {
        return RX0;
    }

    public int getRX1() {
        return RX1;
    }

    public int getCY() {
        return CY;
    }

    public byte getSkin() {
        return skin;
    }

    public int getFace() {
        return face;
    }

    public int getHair() {
        return hair;
    }
    
    public void setEquips(Map<Byte, Integer> equips) {
        this.equips = equips;
    }

    public void setFH(int FH) {
        this.FH = FH;
    }

    public void setRX0(int RX0) {
        this.RX0 = RX0;
    }

    public void setRX1(int RX1) {
        this.RX1 = RX1;
    }

    public void setCY(int CY) {
        this.CY = CY;
    }

    public void setSkin(byte skin) {
        this.skin = skin;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }
}
