function enter(pi) {
    if (pi.haveItem(4001433, 5)) {
		pi.gainItem(4001433, -5);
		pi.playPortalSE();
		pi.warp(950101000, 6);
		return true;
    } else {
		pi.playerMessage("You must have 5 Sunbursts to enter. You can obtain Sunbursts from the monsters inside the Goblin Cave.");
		return false;
    }
}