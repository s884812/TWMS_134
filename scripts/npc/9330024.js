/*
	Reddem Vote Point
*/
var status = -1;
var x = 0
var items = Array(1012070, 1012071, 1012072, 1012073, 2022179, 2049100, 2340000, 1132004, 1002959);
var price = Array(1, 1, 1, 1, 2, 1, 2, 5, 888);

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		status--;
		cm.dispose();
	}
	if (status == 0) {
		var talk = ("You have #r"+ cm.getVPoints() +"#k vote points. #b(Items are drawn in random stats)#k\r\n Please select a item:");
			for(var m=0; m < items.length; m++){
				talk += "\r\n#L"+ m +"##i"+ items[m] +":# #b#t"+ items[m] +"##k for #r"+ price[m] +"#k points."; // #t"+ items[m] +"# item name
			}
		cm.sendSimple(talk);
	} else if (status == 1) {
		if(cm.getVPoints() >= price[selection]){
			cm.gainVPoints(-price[selection]);
			cm.gainItem(items[selection], 1, true, 2617200000); // exactly 30 days | System and game time diff is 7 hour exactly
			cm.sendOk("Enjoy your items and remember to vote for us and get more items!");
			cm.dispose();
		} else {
			cm.sendOk("Not enough vote points!");
			cm.dispose();
		}
	}
}