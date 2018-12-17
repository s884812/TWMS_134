function enter(pi) {
    pi.playPortalSE();
    pi.warp(pi.getSavedLocation("AMORIA"));
    pi.clearSavedLocation("AMORIA");
}