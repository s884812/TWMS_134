
package client;

import java.util.concurrent.ScheduledFuture;

public class MapleCoolDownValueHolder {

    public int skillId;
    public long startTime;
    public long length;
    public ScheduledFuture<?> timer;

    public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
	super();
	this.skillId = skillId;
	this.startTime = startTime;
	this.length = length;
	this.timer = timer;
    }
}
