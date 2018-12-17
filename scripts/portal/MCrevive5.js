function enter(pi) {
    if ( pi.getPlayer().getCarnivalParty().getTeam() == 0 ) {
	pi.warp( 980000501, "red_revive" );
    } else {
	pi.warp( 980000501, "blue_revive" );
    }
}