/* 
 *   NPC   : Kenta
 *   Map   : Aquariun - Zoo
 */

function start() {
    if (cm.getPlayerStat("LVL") < 70) {
	cm.sendOk("Please be at least level 70 and find me again to acquire the skill.");
    } else if (cm.getJob() >= 1000 && cm.getJob() < 2000) {
	cm.teachSkill(10001004, 1, 0); // Maker
	cm.sendOk("I've taught you Monster Riding, please make good use of it.");
    } else if (cm.getJob() >= 2000) {
	cm.teachSkill(20001004, 1, 0); // Maker
	cm.sendOk("I've taught you Monster Riding, please make good use of it.");
    } else {
	cm.teachSkill(1004, 1, 0); // Maker
	cm.sendOk("I've taught you Monster Riding, please make good use of it.");
    }
}

function action(mode, type, selection) {
    cm.dispose();
}