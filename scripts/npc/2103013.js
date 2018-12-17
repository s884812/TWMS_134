/*
	Nett' Pyramid NPC =.= undone
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
			cm.sendSimple("My name is Duarte.#b\r\n\r\n#L0# Ask about the Pyramid.#l\r\n#e#L1# Enter the Pyramid.#l\r\n#n#L2# Look for people to party with.#l\r\n\\r\n#L3# Head towards Yeti Pharaoh's Tomb.#l\r\n#L4# Hear a story on Yeti Pharaoh's jewelry.#l\r\n#L5# Receive a medal of <Protector of Pharaoh>.#l");
		} else if (status == 1) {
			cm.dispose();
		} if (selection == 5) {
			cm.sendOk("Since i am so cool, i am going to give it for free");
			cm.gainItem(1142142, 1);
			cm.dispose();
		}
	}
}