package scripting;

import client.MapleClient;
import server.MaplePortal;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private final MaplePortal portal;

    public PortalPlayerInteraction(final MapleClient c, final MaplePortal portal) {
	super(c);
	this.portal = portal;
    }

    public final MaplePortal getPortal() {
	return portal;
    }

    public final void inFreeMarket() {
	if (getMapId() != 910000000) {
	    if (getPlayer().getLevel() > 10) {
		saveLocation("FREE_MARKET");
		playPortalSE();
		warp(910000000, "st00");
	    } else {
		playerMessage(5, "You must be over level 10 to enter here.");
	    }
	}
    }
}
