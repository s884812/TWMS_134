var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendSimple("Hello #b#h ##k, welcome to #dOdinSEA Investment Center#k\r\n#L1#Trade #r1#k #v4032733# for #r2, 000#k mesos#l\r\n#L2#Trade #r1#k #v4001126# for #r3, 000#k mesos.#l\r\n#L3#Trade #r1#k #v4001168# for #r5, 000#k mesos.#l");
		} else if (status == 1) {
			if (selection == 1) {
				if (cm.itemQuantity(4032733) >= 1 && cm.getMeso() <= 2000000000) {
					cm.gainMeso(2000);
					cm.gainItem(4032733, -1);
					cm.dispose();
				} else {
					cm.sendOk("Either that you don't have any #v4032733# or you mesos tank are full.");
					cm.dispose();
				}
			} else if (selection == 2) {
				if (cm.itemQuantity(4001126) >= 1 && cm.getMeso() <= 2000000000) {
					cm.gainMeso(3000);
					cm.gainItem(4001126, -1); 
					cm.dispose();
				} else {
					cm.sendOk("Either that you don't have any #v4001126# or you mesos tank are full.");
					cm.dispose();
				}
			} else if (selection == 3) {
				if (cm.itemQuantity(4001168) >= 1 && cm.getMeso() <= 2000000000) { // 2147483647 max mesos
					cm.sendOk("Thanks for choosing Alliance Bank Malaysia Berhad!");
					cm.gainMeso(5000);
					cm.gainItem(4001168, -1);
					cm.dispose();
				} else {
					cm.sendOk("Either that you don't have any #v4001168# or you mesos tank are full.");
					cm.dispose();
				}
			}
		}
	}
}