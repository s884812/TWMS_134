function enter(pi) {
    if ( pi.getPlayer().getCarnivalParty().getTeam() == 0 ) {
	pi.warp( 980000201, "red_revive" );
    } else {
	pi.warp( 980000201, "blue_revive" );
    }
}