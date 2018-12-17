package handling.world.remote;

import java.io.Serializable;

public class WorldLocation implements Serializable {

    private static final long serialVersionUID = 2226165329466413678L;
    public int map;
    public byte channel;

    public WorldLocation(final int map, final byte channel) {
	this.map = map;
	this.channel = channel;
    }
}
