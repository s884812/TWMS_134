
package client;

import java.io.Serializable;

public enum MapleDisease implements Serializable {

    NULL(0x0),
    SLOW(0x1),
    MORPH(0x2), // turns into an orange mushroom
    SEDUCE(0x80),
    ZOMBIFY(0x4000, true), // 00 00 00 00 00 00 00 01
    REVERSE_DIRECTION(0x80000),
    WERID_FLAME(0x08000000),
    CURSE(0x800000000000L),
    STUN(0x2000000000000L),
    POISON(0x4000000000000L),
    SEAL(0x8000000000000L),
    DARKNESS(0x10000000000000L),
    WEAKEN(0x4000000000000000L),
    ;
    // 0x100 is disable skill except buff
    private static final long serialVersionUID = 0L;
    private long i;
    private boolean first;

    private MapleDisease(long i) {
	this.i = i;
	first = false;
    }

    private MapleDisease(long i, boolean first) {
	this.i = i;
	this.first = first;
    }

    public boolean isFirst() {
	return first;
    }

    public long getValue() {
	return i;
    }
}
