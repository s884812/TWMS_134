/*
var status = 0;
var maps = Array(610030010, 280030000, 230040420, 551030200, 240060200, 541010100, 100000005, 105070002, 800010100, 800020130, 220080001, 270030500, 802000400, 800040208, 802000710, 802000500, 105040306, 541010010);
var rCost = Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
var costBeginner = Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
var cost = new Array("1", "1", "1", "1", "1","1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1");
var show;
var sCost;
var selectedMap = -1;


function action(mode, type, selection) {
if (mode == 1) {
status++;
} else {
if (status >= 2) {
cm.sendNext("Good bye.");
cm.safeDispose();
return;
}
status--;
}
if (status == 0) {
cm.sendNext("Hi i am the SteadyMS boss warper. Please select the bossmap you would like to go to.");
} else if (status == 1) {
if (!cm.haveItem(4032313)) {
var job = cm.getJob();
if (job == 0 || job == 1000 || job == 2000) {
var selStr = "We have a special 90% discount for beginners. Choose your destination, for fees will change from place to place.#b";
for (var i = 0; i < maps.length; i++) {
selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + costBeginner[i] + " mesos)#l";
}
} else {
var selStr = "Choose your destination, for fees will change from place to place.#b";
for (var i = 0; i < maps.length; i++) {
selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + cost[i] + " mesos)#l";
}
}
cm.sendSimple(selStr);
} else {
cm.sendNextPrev("Hey, since you have a Taxi Coupon, I can take you to the town indicated on the pass for free. It looks like your destination is #bHenesys#k!");
}
} else if (status == 2) {
if (!cm.haveItem(4032313)) {
var job = cm.getJob();
if (job == 0 || job == 1000 || job == 2000) {
sCost = costBeginner[selection];
show = costBeginner[selection];
} else {
sCost = rCost[selection];
show = cost[selection];
}
cm.sendYesNo("You don't have anything else to do here, huh? Do you really want to go to #b#m" + maps[selection] + "##k? It'll cost you #b" + show + " mesos#k.");
selectedMap = selection;
} else {
cm.gainItem(4032313, -1);
cm.warp(100000000, 6);
cm.dispose();
}
} else if (status == 3) {
if (cm.getMeso() < sCost) {
cm.sendNext("You don't have enough mesos. Sorry to say this, but without them, you won't be able to ride the cab.");
cm.safeDispose();
} else {
cm.gainMeso(-sCost);
cm.warp(maps[selectedMap]);
cm.dispose();
}
}
}
*/