/** Author: nejevoli
	NPC Name: 		NimaKin
	Map(s): 		Victoria Road : Ellinia (180000000)
	Description: 		Maxes out your stats and able to modify your equipment stats
*/
importPackage(java.lang);

var status = 0;
var slot = Array();
var stats = Array("Strength", "Dexterity", "Intellect", "Luck", "HP", "MP", "Weapon Attack", "Magic Attack", "Weapon Defense", "Magic Defense", "Accuracy", "Avoidability", "Hands", "Speed", "Jump", "Slots", "Vicious Hammer", "Used slot");
var selected;
var statsSel;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status >= 0 && mode == 0) {
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;

    if (status == 0) {
	if (cm.getPlayerStat("GM") == 1) {
	    cm.sendSimple("What do you want from me?#b\r\n#L0#Max Dual Blade Skills!\r\n#L1#Max my skills!\r\n#L2#Modify my equip's stats!#k");
	} else {
	    cm.dispose();
	}
    } else if (status == 1) {
	if (selection == 0) {
	    if (cm.getPlayerStat("GM") == 1) {
		cm.maxStats();
		cm.sendOk("I have maxed your stats. Happy Mapling!");
	    }
	    cm.dispose();
	} else if (selection == 1) {
	    if (cm.getPlayerStat("GM") == 1) {
			cm.teachSkill(4000000, 20, 20); //
			cm.teachSkill(4001002, 20, 20);
			cm.teachSkill(4001003, 20, 20);
			cm.teachSkill(4001334, 20, 20);
			cm.teachSkill(4300000, 20, 20); //
			cm.teachSkill(4301001, 10, 10);
			cm.teachSkill(4301002, 20, 20);
			cm.teachSkill(4310000, 20, 20); //
			cm.teachSkill(4311001, 20, 20);
			cm.teachSkill(4311002, 20, 20);
			cm.teachSkill(4311003, 20, 20);
			cm.teachSkill(4321000, 20, 20);//
			cm.teachSkill(4321001, 20, 20);
			cm.teachSkill(4321002, 20, 20);
			cm.teachSkill(4321003, 20, 20);
			cm.teachSkill(4331000, 10, 10);//
			cm.teachSkill(4330001, 20, 20);
			cm.teachSkill(4331002, 30, 30);
			cm.teachSkill(4331003, 20, 20);
			cm.teachSkill(4331004, 20, 20);
			cm.teachSkill(4331005, 20, 20);
			cm.teachSkill(4341000, 30, 30);//
			cm.teachSkill(4340001, 30, 30);
			cm.teachSkill(4341002, 30, 30);
			cm.teachSkill(4341003, 30, 30);
			cm.teachSkill(4341004, 30, 30);
			cm.teachSkill(4341005, 30, 30);
			cm.teachSkill(4341006, 30, 30);
			cm.teachSkill(4341007, 30, 30);
			cm.teachSkill(4341008, 5, 5);
	    }
	    cm.dispose();
	} else if (selection == 2) {
	    var avail = "";
	    for (var i = -1; i > -18; i--) {
		if (cm.getInventory(-1).getItem(i) != null) {
		    avail += "#L" + Math.abs(i) + "##t" + cm.getInventory(-1).getItem(i).getItemId() + "##l\r\n";
		}
		slot.push(i);
	    }
	    cm.sendSimple("Which one of your equips would you like to modify?\r\n#b" + avail);
	}
    } else if (status == 2) {
	selected = selection - 1;
	var text = "";
	for (var i = 0; i < stats.length; i++) {
	    text += "#L" + i + "#" + stats[i] + "#l\r\n";
	}
	cm.sendSimple("You have decided to modify your #b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k.\r\nWhich stat would you like to modify?\r\n#b" + text);
    } else if (status == 3) {
	statsSel = selection;
	cm.sendGetNumber("What would you like to set your #b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k's " + stats[statsSel] + " to?", 0, 0, 32767);
    } else if (status == 4) {
	cm.changeStat(slot[selected], statsSel, selection);
	cm.sendOk("Your #b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k's " + stats[statsSel] + " has been set to " + selection + ".");
	cm.dispose();
    }
}