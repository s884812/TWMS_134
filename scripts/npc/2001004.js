/*
var status = -1;
var x = 0
var mobs = Array(210100, 1110100, 2230101, 5130107, 9410010, 6130101, 9400205, 6300005, 8190003, 9400549);
var boss = Array(8500001, 8510000, 9400014, 8820001, 9420546, 9410014, 8800002, 8810026, 9420541, 8180000, 8180001, 9400549, 9300028, 9400270, 9400271, 9400296, 9400409, 9420520, 8800100, 8810130);

function action(mode, type, selection) {
if (mode == 1) {
status++;
} else {
if (status == 2) {
cm.sendNext("test");
}
status--;
}
if (status == 0) {
if (cm.getWorld() == 6) {
cm.sendNext("Sorry but i only spawn bosses in the Fornax world.");
cm.dispose();
} if (cm.getMapId() == 910000001) {
cm.sendNext("Sorry, FM1 is used for player shops.");
cm.dispose();

} else if(cm.getWorld() == 5) {
var talk = "Hello i am the Fornax training NPC! I can help you train by spawning varius different types of monsters and bosses.\r\n\r\n#d Please support the server by clicking google ads ontop of www.steadymaple.com \r\n Without those ads, we will not be able to afford the server!";
var ttalk = "#r Please select a training monster:\r\n #b";
for(var t = 0; t < mobs.length; t++){
ttalk += "#L"+ t +"##o"+ mobs[t] +"# x15#l\r\n";
}
var btalk = "\r\n #rPlease select a boss:#b\r\n";
for(var b = mobs.length; b < mobs.length + boss.length; b++){
btalk += "#L"+ b +"##o"+ boss[b - mobs.length] +"##l\r\n";
}
talk += ttalk;
talk += btalk;
cm.sendSimple(talk);

}


} else if (status == 1) {

if(cm.getMapId() != 910000000){

if(selection >= mobs.length){

cm.spawnMob(boss[selection - mobs.length], 1, 0);

cm.dispose();

}

else {
for(var a = 0; a <= 15; a++){
cm.spawnMob(mobs[selection], 1, 0);
}

cm.dispose();

}
} else {
cm.dispose();
}

}
}
*/