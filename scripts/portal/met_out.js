function enter(pi) {
    pi.playPortalSE();
    pi.warp(pi.getSavedLocation("MIRROR_OF_DIMENSION"));
    pi.clearSavedLocation("MIRROR_OF_DIMENSION");
}