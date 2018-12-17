//TIMELESS/REVERSE EXCHANGE


var status = -1;
var tritems = Array(1302081, 1302086, 1312037, 1312038, 1322060, 1322061, 1332073, 1332074, 1332075, 1332076, 1372044, 1372045, 1382057, 1382059, 1402046, 1402047, 1412033, 1412034, 1422037, 1422038, 1432047, 1432049, 1442063, 1442067, 1452057, 1452059, 1462050, 1462051, 1472068, 1472071, 1482023, 1482024, 1492023, 1492025);
var rewards = Array(2340000, 1102041, 1122001);
var price = Array(2, 10, 40);

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (status == 0) {
		var talk = "Hello, i am SteadyMaples' Reverse/Timeless item trader. \r\n I can exchange your reverse/timeless items for varius rewards.\r\nPlease select a reward:\r\n";
		var has = false;
	    for(var i = 0; i < tritems.length; i++){
		for(var r = 0; r < rewards.length; r++){
		if(cm.haveItem(tritems[i], price[r])){
		var sel = i * 10 + r;
		talk += "#L"+ sel +"# Trade "+price[r]+"#i"+tritems[i]+"# #t"+ tritems[i] + "# for #i"+ rewards[r] +"# #t"+ rewards[r] + "# #l\r\n";
		has = true;
		}
		}
		}
		
		//Start check for not enough
		if(!has){
		talk += "Sorry, you do not have 10 of any timeless/reverse item.";
		cm.sendNext(talk);
		cm.dispose();
		//end check for not enough
		} else {
		cm.sendSimple(talk);
		}
		
	
   	} else if (status == 1) {
		var rewardss = selection % 10;
	    	var items = (selection - rewardss) / 10;
		if( cm.haveItem(tritems[items], price[rewardss]) ){
			cm.gainItem(tritems[items], -price[rewardss]);
			cm.gainItem(rewards[rewardss], 1);
			cm.sendNext("Enjoy your item");
			cm.dispose();
		} else {
			cm.sendNext("You don't have enough items.");
			cm.dispose();
		}
		cm.dispose();

	
}
}