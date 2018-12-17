/* singapore legend guy
	Kerning VIP Hair/Hair Color Change.
*/
var status = -1;
var beauty = 0;
var hair_Colo_new;
var ticket = 5150039;

function action(mode, type, selection) {
    if (mode == 0) {
	cm.dispose();
	return;
    } else {
	status++;
    }

    if (status == 0) {
	cm.sendSimple("Hello! I'm the legend hair guy. #b#t" + ticket + "##k #k, why don't you let me take care of the rest? Decide what you want to do with your hair... \r\n#L0#Haircut: #i" + ticket + "##t" + ticket + "##l\r\n");
    } else if (status == 1) {
	if (selection == 0) {
	    var hair = cm.getPlayerStat("HAIR");
	    hair_Colo_new = [];
	    beauty = 1;

	    if (cm.getPlayerStat("GENDER") == 0) {
		hair_Colo_new = [30000, 30020, 30030, 30040, 30050, 30100, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290, 30300, 30310, 30320, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30430, 30440, 30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670, 30700, 30710, 30730, 30750, 30760, 30770, 30790, 30800, 30810, 30820, 30840];
	    } else {
		hair_Colo_new = [31000, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190, 31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300, 31310, 31320, 31330, 31340, 31350, 31400, 31410, 31420, 31430, 31440, 31450, 31460, 31470, 31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31580, 31590, 31600, 31610, 31620, 31630, 31640, 31650, 31670, 31680, 31690, 31700, 31710, 31720, 31730, 31740, 31750, 31780, 31790, 31800, 31810];
	    }
	    for (var i = 0; i < hair_Colo_new.length; i++) {
		hair_Colo_new[i] = hair_Colo_new[i] + (hair % 10);
	    }
	    cm.askAvatar("I can totally change up your hairstyle and make it look so good. Why don't you change it up a bit? If you have #b#t5150003##k I'll change it for you. Choose the one to your liking~.", hair_Colo_new);
	} else if (selection == 1) {
	    var currenthaircolo = Math.floor((cm.getPlayerStat("HAIR") / 10)) * 10;
	    hair_Colo_new = [];
	    beauty = 2;

	    for (var i = 0; i < 8; i++) {
		hair_Colo_new[i] = currenthaircolo + i;
	    }
	    cm.askAvatar("I can totally change your haircolor and make it look so good. Why don't you change it up a bit? With #b#t5151003##k I'll change it for you. Choose the one to your liking.", hair_Colo_new);
	}
    } else if (status == 2){
	if (beauty == 1){
	    if (cm.setAvatar(ticket, hair_Colo_new[selection]) == 1) {
		cm.sendOk("Enjoy your new and improved hairstyle!");
	    } else {
		cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't give you a haircut without it. I'm sorry...");
	    }
	} else {
	    if (cm.setAvatar(ticket, hair_Colo_new[selection]) == 1) {
		cm.sendOk("Enjoy your new and improved haircolor!");
	    } else {
		cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't dye your hair without it. I'm sorry...");
	    }
	}
	cm.safeDispose();
    }
}
