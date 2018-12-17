/* 
 * @Author Lerk
 * 
 * Tiger Statue (990000900)
 * 
 * Guild Quest - end of boss
 */

importPackage(java.lang);


function action(mode, type, selection) {
    var eim = cm.getEventInstance();
    if (eim != null) {
	if (eim.getProperty("leader").equals(cm.getName())) {
	    if (cm.haveItem(4001024)) {
		cm.removeAll(4001024);
		var prev = eim.setProperty("bossclear","true",true);
		if (prev == null) {
		    var start = parseInt(eim.getProperty("entryTimestamp"));
		    var diff = System.currentTimeMillis() - start;
		    var points = 1000 - Math.floor(diff / (100 * 60));
		    cm.getGuild().gainGP(points);
		}
		eim.finishPQ();
	    } else {
		cm.sendOk("This is your final challenge. Defeat the evil lurking within the Rubian and return it to me. That is all.");
		cm.safeDispose();
	    }
	}
    } else {
	cm.warp(990001100);
	cm.dispose();
    }
}
