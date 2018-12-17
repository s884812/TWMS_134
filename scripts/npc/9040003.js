/* 
 * Sharen III's Soul, Sharenian: Sharen III's Grave (990000700)
 * Guild Quest - end of stage 4
 */

var status = 0;

function action(mode, type, selection) {
    if (mode == 1)
	status++;
    else
	cm.dispose();

    if (status == 0) {
	if (cm.getEventInstance().getProperty("leader").equals(cm.getName())) {
	    if (cm.getEventInstance().getProperty("stage4clear") != null && cm.getEventInstance().getProperty("stage4clear").equals("true")) {
		cm.sendOk("After what I thought would be an immortal sleep, I have finally found someone that will save Sharenian. I can truly rest in peace now.");
		cm.safeDispose();
	    } else {
		var prev = cm.getEventInstance().setProperty("stage4clear","true",true);
		if (prev == null) {
		    cm.sendNext("After what I thought would be an immortal sleep, I have finally found someone that will save Sharenian. This old man will now pave the way for you to finish the quest." + mode);
		} else { //if not null, was set before, and Gp already gained
		    cm.sendOk("After what I thought would be an immortal sleep, I have finally found someone that will save Sharenian. I can truly rest in peace now.");
		    cm.safeDispose();
		}
	    }
	} else {
	    if (cm.getEventInstance().getProperty("stage4clear") != null && cm.getEventInstance().getProperty("stage4clear").equals("true"))
		cm.sendOk("After what I thought would be an immortal sleep, I have finally found someone that will save Sharenian. I can truly rest in peace now.");
	    else
		cm.sendOk("I need the leader of your party to speak with me, nobody else.");
	    cm.safeDispose();
	}
    } else if (status == 1) {
	cm.getGuild().gainGP(30);
	cm.getMap().getReactorByName("ghostgate").hitReactor(cm.getC());
	cm.showEffect(true, "quest/party/clear");
	cm.playSound(true, "Party1/Clear");
	cm.dispose();
    }
}