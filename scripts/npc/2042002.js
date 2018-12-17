/* 
 * Spiegelmann - Monster Carnival
 */

var status = -1;
var rank = "C";
var exp = 0;

function start() {
    if (cm.getCarnivalParty() != null) {
        status = 99;
    }
    action(1, 0, 0);
}
 
function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    
    if (status == 0) {
        cm.sendSimple("What would you like to do? If you have never participated in the Monster Carnival, you'll need to know a thing or two about it before joining.\r\n#b#L0# Go to the Monster Carnival Field.#l\r\n#L1# Learn about the Monster Carnival.#l\r\n#L2# Trade #t4001129#.#l");
    } else if (status == 1) {
        switch (selection) {
            case 0: {
                var level = cm.getPlayerStat("LVL");
                if ( level < 30 || level > 50 ) {
                    cm.sendOk("I'm sorry, but only the users within Level 30~50 may participate in Monster Carnival.");
                } else {
                    cm.warp( 980000000, "st00" );
                }
                cm.dispose();
            }
            default: {
                cm.dispose();
                break;
            }
            break;
        }
    } else if (status == 100) {
        var carnivalparty = cm.getCarnivalParty();
        if (carnivalparty.getTotalCP() >= 500) {
            rank = "A";
            exp = 30000;
        } else if (carnivalparty.getTotalCP() >= 300) {
            rank = "B";
            exp = 22500;
        } else if (carnivalparty.getTotalCP() >= 0) {
            rank = "C";
            exp = 16500;
        }
        if (carnivalparty.isWinner()) {
            cm.sendOk("You won the battle, despite your amazing performance. Victory is yours. \r\n#bMonster Carnival Rank : " + rank);
        } else {
            cm.sendOk("Unfortunately, you have either tied or lost the battle, despite your amazing performance. Victory should be yours the next time up. \r\n#bMonster Carnival Rank : " + rank);
        }
    } else if (status == 101) {
        var carnivalparty = cm.getCarnivalParty();
        if (carnivalparty.isWinner()) {
            carnivalparty.removeMember(cm.getChar());
            cm.gainExp(exp * 20);
            cm.warp(980000000);
            cm.dispose();
        } else {
            carnivalparty.removeMember(cm.getChar());
            cm.gainExp(exp / 2 * 20);
            cm.warp(980000000);
            cm.dispose();
        }
    }

}

//30000 : 500
//22500 : 300
//16500 : 50