//medal seller
var status = 0;
var currency = 4001126; //Maple Leafs
var price = 1000;
var startm = 1142000;
var endm = 1142099;

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
		var talk = "Hello. I am the SteadyMaple Medal seller.\r\nEach medal costs "+ price +" #i"+ currency +"#.\r\nPlease select a medal\r\n";
		for (var m = startm; m <= endm; m++ ){
		talk += "#L"+ m +"##i"+ m +"##t"+ m +"##l\r\n";
		}
		cm.sendSimple(talk);
		
		
        

        }  else if (status == 1) {
		if ( cm.haveItem(currency, price)) {
		cm.gainItem(currency, -price);
		cm.gainItem(selection, 1);
		cm.sendNext("Enjoy your medal!");
        cm.dispose();
		} else {
		cm.sendNext("You do not have"+ price +"#i"+ currency +"#.");
		cm.dispose();
		}
        
  }



    }
    
}

        
    
