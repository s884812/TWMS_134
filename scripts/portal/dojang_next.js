function enter(pi) {
	if (!pi.haveMonster(9300216)) {
		pi.playerMessage("There are still some monsters remaining.");
	} else {
		pi.dojoAgent_NextMap(true, false);
	}
}
/*
function enter(pi) {
    if (!pi.haveMonster(9300216)) {
		pi.playerMessage("There are still some monsters remaining.");
    } else {
		if (pi.isLeader()) {
			if (pi.allMembersHere()) {
				pi.warpParty(925020100);
			} else {
				pi.playerMessage("Sorry, but all party members must be here in order to process.");
			}
		} else {
			pi.playerMessage("Sorry, but only leader of the party can access the portal.");
    }
}//pi.dojoAgent_NextMap(true, false);
*/