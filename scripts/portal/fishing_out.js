function enter(pi) {
    pi.playPortalSE();
    pi.warp(pi.getSavedLocation("FISHING"));
    pi.clearSavedLocation("FISHING");
}