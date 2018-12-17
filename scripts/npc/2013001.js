/**
	Starter Pack + Rules npc
	Credits to - MaplePuppet
	Credits to - Haithem (For the Rules and Regulations)
**/

var status = -1;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 1)
		status++;
	else
		status--;
	if (status == -1) {
		cm.sendOk("#bYou cannot enter the server until you accept the rules and regulations. Make sure to read and accept the rules, if you break the rules, you will be banned.");
		cm.dispose();
	} else if (status == 0) {
		cm.askAcceptDecline("#rPlease accept the Rules and Regulations of #bOdinSEA#r#r \r\n\r\n\ #b1.#r Any user must respect all staff members; threatening, harassing, cursing, or any verbal insult towards staff members is a top offence. \r\n\r\n\#b2.#r Any user who was caught with any unauthorized 3rd party programs that directly/indirectly interfere with the game play of the server which allows the user to gain an advantage in anyway will result in a ban. \r\n\r\n\#b3. #rOdinSEA#r carries no responsibility of your account's security, if you give out your password; you are putting your account in jeopardy of being stolen. \r\n\r\n\#b4. #rYou obey the game rules, found on our website at the rules page; caught breaking any rule may result in any sort of penalty. \r\n\r\n\#b5. #rIn any case of any user's access being removed from the server; staff members have the right to restore or not to restore anyone's account/data of their free will. \r\n\r\n\#b6. #rYou are responsible for rollbacks. YOU are responsible to use @save.");
	} else if (status == 1) {
		cm.gainItem(3010168, 1); // My Friends Chair [Chair]
		cm.gainMeso(10000); // 10k Mesos
		cm.gainItem(5150039, 1); // Legendary Hair Coupon
		cm.gainItem(5072000, 10); // Super Megaphone
		cm.gainItem(1002419, 1); // Mark of the beta
		cm.gainItem(2000011, 150); // Mana Elixir Pill
		cm.gainItem(2000009, 150); // White Pill
		cm.gainItem(1082149, 1); // Brown Work Gloves
		cm.gainNX(1000); // @cash
		cm.warp(100000000, 0);
		cm.sendOk("#rThanks, enjoy #bOdinSEA.#r 1million NX Cash has been added to your account.Remember to check out our forum for more information.");
		cm.dispose();
	}
}  