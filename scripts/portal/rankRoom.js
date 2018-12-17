function enter(pi) {
    pi.playPortalSE();
    
    switch (pi.getMapId()) {
	case 130000000:
	    pi.warp(130000100, 5);
	    break;
	case 130000200:
	    pi.warp(130000100, 4);
	    break;
    }
}