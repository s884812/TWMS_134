/*
Zakum Altar - Summons Zakum.
*/

function act() {
    rm.changeMusic("Bgm06/FinalFight");
    rm.spawnFakeMonster(8800000, -10, -215);
    rm.spawnMonster(8800003, -10, -215);
    rm.spawnMonster(8800004, -10, -215);
    rm.spawnMonster(8800005, -10, -215);
    rm.spawnMonster(8800006, -10, -215);
    rm.spawnMonster(8800007, -10, -215);
    rm.spawnMonster(8800008, -10, -215);
    rm.spawnMonster(8800009, -10, -215);
    rm.spawnMonster(8800010, -10, -215);
    rm.mapMessage("Zakum is summoned by the force of eye of fire.")
}
