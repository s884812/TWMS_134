function enter(pi) {
    if ( pi.getPlayer().getCarnivalParty().getTeam() == 0 ) {
	pi.warp( 980000401, "red_revive" );
    } else {
	pi.warp( 980000401, "blue_revive" );
    }
}