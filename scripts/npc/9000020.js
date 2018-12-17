var bossmaps = Array(100000000, 100000000);
var monstermaps = Array(100000000, 100000000);
var townmaps = Array(
100000000, 101000000, 102000000, 103000000, 104000000,
140000000, 680000000, 230000000, 260000000, 211000000, 110000000, 130000200, 222000000, 240000000, 220000000, 800000000, 802000101, 120000000, 600000000, 221000000, 200000000, 300000000, 801000000, 540000000, 541000000, 105040300, 250000000, 251000000, 551000000, 550000000, 800040000, 261000000, 541020000, 270000000, 229000000, 700000000, 700000100, 700000200, 701000000, 500000000
);
var chosenMap;
var typee;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 1)
		status++;
	else {
		if (status == 3 && mode == 0)
		cm.sendNext("Okay, you seem to be busy. See you next time then!");
		cm.dispose();
		return;
	}
	if (status == 0) {
		if (selection == 0)
		typee = townmaps;
		else if (selection == 1)
		typee = monstermaps;
		else
		typee = bossmaps;
		var selStr = "#fUI/UIWindow.img/QuestIcon/3/0#";
		for (var i = 0; i < typee.length; i++) {
			selStr += "\r\n#L" + i + "##m" + typee[i] + "#";
		}
		cm.sendSimple(selStr);
	} else if (status == 1) {
		chosenMap = typee[selection];
		cm.sendYesNo("Do you want to go to #r#m" + chosenMap + "##k?");
	} else if (status == 2) {
		cm.warp(chosenMap, 0);
		cm.dispose();
	}
}