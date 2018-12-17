/*
	Maple Administrator
*/

var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (status == 0) {
	cm.sendSimple("Whoa..! Are you here for job advance? \n\r #L0##bI would like to be one of Aran now!#k#l  \n\r #L1##bI would like to be one of Evan now!#k#l  \n\r #L2##bI would like to be one of Dual Blade now!#k#l");
    } else if (status == 1) {
	if (selection == 0) {
	    var job = cm.getJob();
	    var level = cm.getPlayerStat("LVL");
	    if (job == 0 || job == 1000 || job == 2000) {
		if (level >= 10) {
		    cm.changeJob(2100);
		    cm.resetStats(35, 4, 4, 4);
		    cm.gainItem(1442000, 1);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 10 and above for first job advancement.");
		    cm.safeDispose();
		}
	    } else if (job == 2100) {
		if (level >= 30) {
		    cm.changeJob(2110);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 30 and above for second job advancement.");
		    cm.safeDispose();
		}
	    } else if (job == 2110) {
		if (level >= 70) {
		    cm.changeJob(2111);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 70 and above for third job advancement.");
		    cm.safeDispose();
		}
	    } else if (job == 2111) {
		if (level >= 120) {
		    cm.changeJob(2112);
		    cm.teachSkill(21110003, 30, 0); // 3rd job
		    cm.teachSkill(21121000, 10, 10); // MW
		    cm.teachSkill(21120001, 30, 30); // Aggression
		    cm.teachSkill(21120002, 30, 30); // Overswing
		    cm.teachSkill(21121003, 30, 30); // Freezing posture
		    cm.teachSkill(21120004, 30, 30); // High class def
		    cm.teachSkill(21120005, 30, 30); // PA Finale
		    cm.teachSkill(21120006, 30, 30); // Tempest
		    cm.teachSkill(21120007, 30, 30); // EZ sield
		    cm.teachSkill(21121008, 1, 5); // Will
		} else {
		    cm.teachSkill(21110003, 0, 0);
		    cm.sendOk("You must be level 120 and above for fourth job advancement.")
		    cm.safeDispose();
		}
	    } else {
		cm.sendOk("You arn't a beginner, only loyal beginners are allowed to be part of the the legendary warrior!");
		cm.safeDispose();
	    }
	} else if (selection == 1) {
	    var job = cm.getJob();
	    var level = cm.getPlayerStat("LVL");
	    if (job == 0 || job == 1000 || job == 2000) {
		if (level >= 10) {
		    cm.changeJob(2200);
		    cm.resetStats(4, 4, 20, 4);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 10 and above for first job advancement.");
		    cm.safeDispose();
		}
	    } else {
		cm.sendOk("You arn't a beginner, only loyal beginners are allowed to be part of the the legendary warrior!");
		cm.safeDispose();
	    }
	} else if (selection == 2) {
	    var job = cm.getJob();
	    var level = cm.getPlayerStat("LVL");
	    if (job == 0 || job == 1000 || job == 2000) {
		if (level >= 10) {
		    cm.changeJob(400);
		    cm.resetStats(4, 25, 4, 4);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 10 and above for first job advancement.");
		    cm.safeDispose();
		}
	    } else if (job == 400) {
		if (level >= 20) {
		    cm.changeJob(430);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 20 and above for second job advancement.");
		    cm.safeDispose();
		}
	    } else if (job == 430) {
		if (level >= 30) {
		    cm.changeJob(431);
		    cm.teachSkill(4311003, 0, 20);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 30 and above for third job advancement.");
		    cm.safeDispose();
		}
	    } else if (job == 431) {
		if (level >= 55) {
		    cm.changeJob(432);
		    cm.teachSkill(4321000, 0, 20);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 55 and above for fourth job advancement.");
		    cm.safeDispose();
		}
	    } else if (job == 432) {
		if (level >= 70) {
		    cm.changeJob(433);
		    cm.teachSkill(4331002, 0, 30);
		    cm.teachSkill(4331005, 0, 20);
		    cm.dispose();
		} else {
		    cm.sendOk("You must be level 70 and above for fifth job advancement.");
		    cm.safeDispose();
		}
	    } else if (job == 433) {
		if (level >= 120) {
		    cm.changeJob(434);
		    cm.teachSkill(4341000, 10, 10); // MW
		    cm.teachSkill(4340001, 30, 30); // Aggression
		    cm.teachSkill(4341002, 30, 30); // Overswing
		    cm.teachSkill(4341003, 30, 30); // Freezing posture
		    cm.teachSkill(4341004, 30, 30); // High class def
		    cm.teachSkill(4341005, 30, 30); // PA Finale
		    //cm.teachSkill(4341006, 30, 30); // Tempest
		    cm.teachSkill(4341007, 30, 30); // EZ sield
		    cm.teachSkill(4341008, 1, 5); // Will
		} else {
		    cm.sendOk("You must be level 120 and above for sixth job advancement.");
		    cm.safeDispose();
		}
	    } else {
		cm.sendOk("You arn't a beginner, only loyal beginners are allowed to be part of the the legendary warrior!");
		cm.safeDispose();
	    }
	} else {
	    cm.dispose();
	}
    }
}