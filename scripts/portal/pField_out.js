function enter(pi) {
    pi.playPortalSE();
    pi.warp(pi.getSavedLocation("RICHIE"));
    pi.clearSavedLocation("RICHIE");
}