/*
	NPC : Vavaan
	Function : Job Advancer + Part of Auto Job system
	Map : not sure lol / but is part of FM NPC
*/

var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 0) {
		cm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if (cm.getJob() == 110 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(111);
				cm.dispose();
			} else if (cm.getJob() == 111 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(112);
				cm.dispose();
			} else if (cm.getJob() == 120 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(121);
				cm.dispose();
			} else if (cm.getJob() == 121 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(122);
				cm.dispose();
			} else if (cm.getJob() == 130 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(131);
				cm.dispose();
			} else if (cm.getJob() == 131 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(132);
				cm.dispose();
			} else if (cm.getJob() == 210 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(211);
				cm.dispose();
			} else if (cm.getJob() == 211 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(212);
				cm.dispose();
			} else if (cm.getJob() == 220 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(221);
				cm.dispose();
			} else if (cm.getJob() == 221 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(222);
				cm.dispose();
			} else if (cm.getJob() == 230 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(231);
				cm.dispose();
			} else if (cm.getJob() == 231 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(232);
				cm.dispose();
			} else if (cm.getJob() == 310 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(311);
				cm.dispose();
			} else if (cm.getJob() == 311 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(312);
				cm.dispose();
			} else if (cm.getJob() == 320 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(321);
				cm.dispose();
			} else if (cm.getJob() == 321 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(322);
				cm.dispose();
			} else if (cm.getJob() == 410 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(411);
				cm.dispose();
			} else if (cm.getJob() == 411 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(412);
				cm.dispose();
			} else if (cm.getJob() == 420 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(421);
				cm.dispose();
			} else if (cm.getJob() == 421 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(422);
				cm.dispose();
			} else if (cm.getJob() == 430 && cm.getPlayerStat("LVL") == 30) {
				cm.changeJob(431);
				cm.dispose();
			} else if (cm.getJob() == 431 && cm.getPlayerStat("LVL") == 55) {
				cm.changeJob(432);
				cm.dispose();
			} else if (cm.getJob() == 432 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(433);
				cm.dispose();
			} else if (cm.getJob() == 433 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(434);
				cm.dispose();
			} else if (cm.getJob() == 510 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(511);
				cm.dispose();
			} else if (cm.getJob() == 511 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(512);
				cm.dispose();
			} else if (cm.getJob() == 520 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(521);
				cm.dispose();
			} else if (cm.getJob() == 521 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(522);
				cm.dispose();
			} else if (cm.getJob() == 1100 && cm.getPlayerStat("LVL") == 30) {
				cm.changeJob(1110);
				cm.dispose();
			} else if (cm.getJob() == 1110 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(1111);
				cm.dispose();
			} else if (cm.getJob() == 1200 && cm.getPlayerStat("LVL") == 30) {
				cm.changeJob(1210);
				cm.dispose();
			} else if (cm.getJob() == 1210 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(1211);
				cm.dispose();
			} else if (cm.getJob() == 1300 && cm.getPlayerStat("LVL") == 30) {
				cm.changeJob(1310);
				cm.dispose();
			} else if (cm.getJob() == 1310 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(1311);
				cm.dispose();
			} else if (cm.getJob() == 1400 && cm.getPlayerStat("LVL") == 30) {
				cm.changeJob(1410);
				cm.dispose();
			} else if (cm.getJob() == 1410 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(1411);
				cm.dispose();
			} else if (cm.getJob() == 1500 && cm.getPlayerStat("LVL") == 30) {
				cm.changeJob(1510);
				cm.dispose();
			} else if (cm.getJob() == 1510 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(1511);
				cm.dispose();
			} else if (cm.getJob() == 2000 && cm.getPlayerStat("LVL") == 10) {
				cm.changeJob(2100);
				cm.dispose();
			} else if (cm.getJob() == 2100 && cm.getPlayerStat("LVL") == 30) {
				cm.changeJob(2110);
				cm.dispose();
			} else if (cm.getJob() == 2110 && cm.getPlayerStat("LVL") == 70) {
				cm.changeJob(2111);
				cm.dispose();
			} else if (cm.getJob() == 2111 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(2112);
				cm.dispose();
			} else if (cm.getJob() == 2001 && cm.getPlayerStat("LVL") == 10) {
				cm.changeJob(2200);
				cm.dispose();
			} else if (cm.getJob() == 2200 && cm.getPlayerStat("LVL") == 20) {
				cm.changeJob(2210);
				cm.dispose();
			} else if (cm.getJob() == 2210 && cm.getPlayerStat("LVL") == 30) {
				cm.changeJob(2211);
				cm.dispose();
			} else if (cm.getJob() == 2211 && cm.getPlayerStat("LVL") == 40) {
				cm.changeJob(2212);
				cm.dispose();
			} else if (cm.getJob() == 2212 && cm.getPlayerStat("LVL") == 50) {
				cm.changeJob(2213);
				cm.dispose();
			} else if (cm.getJob() == 2213 && cm.getPlayerStat("LVL") == 60) {
				cm.changeJob(2214);
				cm.dispose();
			} else if (cm.getJob() == 2214 && cm.getPlayerStat("LVL") == 80) {
				cm.changeJob(2215);
				cm.dispose();
			} else if (cm.getJob() == 2215 && cm.getPlayerStat("LVL") == 100) {
				cm.changeJob(2216);
				cm.dispose();
			} else if (cm.getJob() == 2216 && cm.getPlayerStat("LVL") == 120) {
				cm.changeJob(2217);
				cm.dispose();
			} else if (cm.getJob() == 2217 && cm.getPlayerStat("LVL") == 160) {
				cm.changeJob(2218);
				cm.dispose();
			} else if (cm.getJob() == 900) {
				cm.sendOk("Oh hai GM");
				cm.dispose();
			} else if (cm.getJob() == 0 && cm.getPlayerStat("LVL") == 8) {
				cm.sendSimple("You seem to be eligible to be a Magician, would you like to be a Magician?\r\n#L0#Yes#l\r\n#L1#No#l");
			} else if (cm.getJob() == 0 && cm.getPlayerStat("LVL") == 10) {
				cm.sendSimple("Please select a job that you wish to be.\r\n#L2#Warrior#l\r\n#L3#Archer#l\r\n#L4#Theif#l\r\n#L5#Pirate#l");
			} else if (cm.getJob() == 100 && cm.getPlayerStat("LVL") == 30) {
				cm.sendSimple("Please select a job that you wish to be.\r\n#L6#Fighter#l\r\n#L7#Page#l\r\n#L8#Spearman#l");
			} else if (cm.getJob() == 200 && cm.getPlayerStat("LVL") == 30) {
				cm.sendSimple("Please select a job that you wish to be.\r\n#L9#Wizard (Fire/Poison)#l\r\n#L10#Wizard (Ice/Lightning)#l\r\n#L11#Cleric#l");
			} else if (cm.getJob() == 300 && cm.getPlayerStat("LVL") == 30) {
				cm.sendSimple("Please select a job that you wish to be.\r\n#L12#Hunter#l\r\n#L13#Crossbowman#ll");
			} else if (cm.getJob() == 400 && cm.getPlayerStat("LVL") == 30) {
				cm.sendSimple("Please select a job that you wish to be.\r\n#L14#Assassin#l\r\n#L15#Bandit#l");
			} else if (cm.getJob() == 400 && cm.getPlayerStat("LVL") == 20) {
				cm.sendSimple("You seem to be eligible to be a Dual Blade, would you like to be a Dual Blade?\r\n#L16#Yes#l\r\n#L17#No#l");
			} else if (cm.getJob() == 500 && cm.getPlayerStat("LVL") == 30) {
				cm.sendSimple("Please select a job that you wish to be.\r\n#L18#Infighter#l\r\n#L19#Gunslinger#l");
			} else if (cm.getJob() == 1000 && cm.getPlayerStat("LVL") == 10) {
				cm.sendSimple("Please select a job that you wish to be.\r\n#L20#Soul Master#l\r\n#L21#Flame Wizard#l\r\n#L22#Wind Breaker#l\r\n#L23#Night Walker#l\r\n#L24#Striker#l");
			} else {
				cm.dispose();
			}
		} else if (status == 1) {
			if (selection == 0){ // Be Magician
				cm.changeJob(200);
			} else if (selection == 1){ // Not to be Magician
				cm.sendOk("Well, its your choice then. Good luck~");
			} else if (selection == 2){
				cm.changeJob(100);
			} else if (selection == 3){
				cm.changeJob(300);
			} else if (selection == 4){
				cm.changeJob(400);
			} else if (selection == 5){
				cm.changeJob(500);
			} else if (selection == 6){
				cm.changeJob(110);
			} else if (selection == 7){
				cm.changeJob(120);
			} else if (selection == 8){
				cm.changeJob(130);
			} else if (selection == 9){
				cm.changeJob(210);
			} else if (selection == 10){
				cm.changeJob(220);
			} else if (selection == 11){
				cm.changeJob(230);
			} else if (selection == 12){
				cm.changeJob(310);
			} else if (selection == 13){
				cm.changeJob(320);
			} else if (selection == 14){
				cm.changeJob(410);
			} else if (selection == 15){
				cm.changeJob(420);
			} else if (selection == 16){
				cm.changeJob(430);
			} else if (selection == 17){
				cm.sendOk("Well, its your choice then. Good luck~");
			} else if (selection == 18){
				cm.changeJob(510);
			} else if (selection == 19){
				cm.changeJob(520);
			} else if (selection == 20){
				cm.changeJob(1100);
			} else if (selection == 21){
				cm.changeJob(1200);
			} else if (selection == 22){
				cm.changeJob(1300);
			} else if (selection == 23){
				cm.changeJob(1400);
			} else if (selection == 24){
				cm.changeJob(1500);
			}
			cm.dispose();
		}
	}
}