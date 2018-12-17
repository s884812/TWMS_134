//trophey trader
var status = 0;
var currency = 4000038; //trophey
var items = Array(3010067, 3012000, 3010009, 3010018, 3010068, 3010051, 3010023, 3010022, 3010045, 3010012, 3010019, 3010041, 1092049, 1302059, 1312031, 1322052, 1332049, 1332050, 1372032, 1382035, 1402036, 1412026, 1422028, 1432038, 1442045, 1452044, 1462039, 1472051, 1472085, 1492013, 1482013, 5490000, 3010073, 3010126, 3010127, 3010128, 3010129, 3010131, 3010133, 3010049, 1902002, 1902018);
var price = Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 5, 8);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
    }
    else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("Goodbye");
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        }
        else {
            status--;
        }

        if (status == 0) {
		var talk = "Hello. I am the SteadyMaple Trophy Trader.\r\nPlease select a item\r\n";
		for (var m = 0; m < items.length; m++ ){
		talk += "#L"+ m +"##i"+ items[m] +"##t"+ items[m] +"# for "+ price[m] +"#i"+ currency +"##l\r\n";
		}
		cm.sendSimple(talk);
		
		
        

        }  else if (status == 1) {
		if ( cm.haveItem(currency, price[selection])) {
		cm.gainItem(currency, -price[selection]);
		cm.gainItem(items[selection], 1);
		cm.sendNext("Enjoy your item!");
        cm.dispose();
		} else {
		cm.sendNext("You do not have"+ price[selection] +"#i"+ currency +"#.");
		cm.dispose();
		}
        
  }



    }
    
}

        
    
