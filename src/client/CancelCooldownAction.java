
package client;

import java.lang.ref.WeakReference;
import tools.MaplePacketCreator;

public class CancelCooldownAction implements Runnable {

    private int skillId;
    private WeakReference<MapleCharacter> target;

    public CancelCooldownAction(MapleCharacter target, int skillId) {
	this.target = new WeakReference<MapleCharacter>(target);
	this.skillId = skillId;
    }

    @Override
    public void run() {
	final MapleCharacter realTarget = target.get();
	if (realTarget != null) {
	    realTarget.removeCooldown(skillId);
	    realTarget.getClient().getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
	}
    }
}
