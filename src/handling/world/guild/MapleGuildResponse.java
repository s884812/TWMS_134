package handling.world.guild;

import tools.MaplePacketCreator;
import handling.MaplePacket;

public enum MapleGuildResponse {

    NOT_IN_CHANNEL(0x2a),
    ALREADY_IN_GUILD(0x28),
    NOT_IN_GUILD(0x2d);
    private int value;

    private MapleGuildResponse(int val) {
	value = val;
    }

    public int getValue() {
	return value;
    }

    public MaplePacket getPacket() {
	return MaplePacketCreator.genericGuildMessage((byte) value);
    }
}
