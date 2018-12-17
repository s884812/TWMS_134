
package client;

import java.util.concurrent.ScheduledFuture;

import server.MapleStatEffect;

public class MapleBuffStatValueHolder {

    public MapleStatEffect effect;
    public long startTime;
    public int value;
    public ScheduledFuture<?> schedule;

    public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, ScheduledFuture<?> schedule, int value) {
	super();
	this.effect = effect;
	this.startTime = startTime;
	this.schedule = schedule;
	this.value = value;
    }
}
