function start() {
	cm.askMapSelection("#1# Mu Lung Training Center #2# Monster Carnival 1 #3# Monster Carnival 2 #4# Ghost Ship in the Foggy Ocean #5# Nett's Pyramid #6# Kerning Subway #7# ????? #8# Golden Temple #9# Crimson Wood Keep");
}

function action(mode, type, selection) {
	if (mode == 1) {
		switch (selection) {
			case 1:
				cm.saveLocation("MIRROR_OF_DIMENSION");
				cm.warp(925020000, 4); // Portal exit
			break;
			case 2:
			if (cm.getPlayerStat("LVL") >= 30 && cm.getPlayerStat("LVL") <= 50) {
				cm.saveLocation("MIRROR_OF_DIMENSION");
				cm.warp(980000000, 4);
			} else {
				cm.sendOk("Sorry but you must be between level 30 to 50 proceed.");
			}
			break;
			case 3:
			if (cm.getPlayerStat("LVL") >= 30 && cm.getPlayerStat("LVL") <= 50) {
				cm.saveLocation("MIRROR_OF_DIMENSION");
				cm.warp(980030000, 4);
			} else {
				cm.sendOk("Sorry but you must be between level 30 to 50 to proceed.");
			}
			break;
			case 4:
			if (cm.getPlayerStat("LVL") >= 60 && cm.getPlayerStat("LVL") <= 80) {
				cm.saveLocation("MIRROR_OF_DIMENSION");
				cm.warp(923020000, 0); // no portal
			} else {
				cm.sendOk("Sorry but you must be between level 60 to 80 to proceed.");
			}
			break;
			case 5:
			if (cm.getPlayerStat("LVL") >= 40) {
				cm.saveLocation("MIRROR_OF_DIMENSION");
				cm.warp(926010000, 4); // Nett's Pyramid
				break;
			} else {
				cm.sendOk("Sorry but you must be level 40 or higher to proceed.");
			}
			break;
			case 6:
			if (cm.getPlayerStat("LVL") >= 25 && cm.getPlayerStat("LVL") <= 50) {
				cm.saveLocation("MIRROR_OF_DIMENSION");
				cm.warp(910320000, 2);
				break;
			} else {
				cm.sendOk("Sorry but you must be between level 25 to 30 to proceed.");
			}
			break;
			case 7:
				//cm.saveLocation("MIRROR_OF_DIMENSION");
				//cm.warp(950100000, 9);
				cm.dispose();
			break;
			case 8:
				cm.saveLocation("MIRROR_OF_DIMENSION");
				cm.warp(950100000, 9);
			break;
			case 9:
				//cm.saveLocation("MIRROR_OF_DIMENSION");
				//cm.warp(950100000, 9);
				cm.dispose();
			break;
		}
	}
	cm.dispose();
}