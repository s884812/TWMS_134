package handling.world;

import java.io.Serializable;

public class PlayerCoolDownValueHolder implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public int skillId;
    public long startTime;
    public long length;

    public PlayerCoolDownValueHolder(final int skillId, final long startTime, final long length) {
	this.skillId = skillId;
	this.startTime = startTime;
	this.length = length;
    }
}
