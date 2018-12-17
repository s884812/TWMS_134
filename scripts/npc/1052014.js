//rest npc
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
    }
    else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("Goodbye");
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        }
        else {
            status--;
        }

        if (status == 0) {
		            if ((cm.getReborns() == 3) && (cm.getWorld() == 6)) {
			cm.sendNext("You have reached the maximum number of resets, congradulations!");
			cm.dispose();
            	} else if( (cm.getJob() <= 999 && cm.getPlayerStat("LVL") == 200)|| ( cm.getJob() >= 1000 && cm.getJob() < 2000  && cm.getPlayerStat("LVL") == 120 ) || (cm.getJob() >= 2000 && cm.getPlayerStat("LVL") == 200 )) {
				if (cm.getWorld() ==6 ){
			cm.sendNext("Congradulations on reaching max level for the first time! \r\n you are probebly wondering what happens now. \r\n Basically you keep your job and ap and skill, but you go back to level 1 and can keep getting AP, so you get stronger and stronger! \r\n It costs 2500 Maple Leafs to reset. Press next to reset. ");
		       } else if (cm.getWorld() == 5) {
			cm.sendNext("Congradulations on reaching max level for the first time! \r\n you are probebly wondering what happens now. \r\n Basically you keep your job and ap and skill, but you go back to level 1 and can keep getting AP, so you get stronger and stronger! \r\n . Press next to reset. ");
			   }
			   
			   } else if ((cm.getJob() < 1000 && cm.getPlayerStat("LVL") != 200) || (cm.getJob() >= 1000 && cm.getPlayerStat("LVL") != 120)){
			cm.sendNext("Come back when you are a level 200 adventurer/aran or a level 120 cygnus .");
                        cm.dispose();

        }

        }  else if (status == 1) {
		if ( cm.getWorld() == 6){
         if (cm.haveItem(4001126,2500)) {
                        cm.gainItem(4001126, -2500);
                        cm.doReborn();
                        cm.sendNext("The reset was succesful, Change channel for the effects to take place!");
                    cm.dispose();
                } else {
                    cm.sendNext("You do not have 2500 MapleLeafs");
                     cm.dispose();
                 }
				 }
	      else if(cm.getWorld() == 5){
		                          cm.doReborn();
                        cm.sendNext("The reset was succesful, Change channel for the effects to take place!");
                    cm.dispose();
		  }
				 
        
  }



    }
    
}

        

    
