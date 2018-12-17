/* Author: Xterminator (Modified by RMZero213)
	NPC Name: 		Roger
	Map(s): 		Maple Road : Lower level of the Training Camp (2)
	Description: 		Quest - Roger's Apple
*/
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
	qm.dispose();
    } else {
	if (mode == 1)
	    status++;
	else
	    status--;
	
	if (status == 0) {
	    qm.sendNext("哈囉！有空嗎？我是幫助新生的冒險家們的教官，我的名字叫做#p2000#。");
	} else if (status == 1) {
	    qm.sendNextPrev("你說誰要我做這種事情啊？哇哈哈哈~你的好奇心還真重啊！很好很好~其實是我自願想要做的。");
	} else if (status == 2) {
	    qm.askAcceptDeclineNoESC("那…我就來開個玩笑吧！咦呀！");
	} else if (status == 3) {
	   //qm.getPlayer().getStat().setHp(35);
	   //qm.updateSingleStat(qm.getMapleStat().HP, 35);
	    if (!qm.haveItem(2010007)) {
		qm.gainItem(2010007, 1);
		qm.forceStartQuest();
		qm.sendNext("嚇到了吧？HP變成0可是件大事呢！來~把#r#t2010007##k送給你，你就吃下它吧！你會充滿活力喔！你就開啟道具視窗，再對要使用的道具點兩下滑鼠左鍵吧！#I");
		qm.getShowItemGain(2010007, 1);
	   }
	} else if (status == 4) {
	    qm.sendNextPrev("記得要我把給你的#t2010007#全部吃掉喔！不過只要靜止不動也可以慢慢的恢復HP…等HP完全恢復後，再跟我說話吧！#I");
	} else if (status == 5) {
	    qm.ShowWZEffect("UI/tutorial.img/28");
	    qm.dispose();
	}
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
	qm.dispose();
    } else {
	if (mode == 1)
	    status++;
	else
	    status--;
	if (status == 0) {
//	    if (qm.getPlayerStat("HP") < 50) {
//		qm.sendNext("Hey, your HP is not fully recovered yet. Did you take all the Roger's Apple that I gave you? Are you sure?");
//		qm.dispose();
//	    } else {
		qm.sendNext("使用道具…如何？很簡單吧！在畫面右下方欄位上，還可以設置#b熱鍵#k喔！沒想到吧？ 哈哈~");
//	    }
	} else if (status == 1) {
	    qm.sendNextPrev("很好！看來你已經學會了不少東西…那我就送個禮物給你吧！如果想去世界各地旅行，這可是必須要學會的，所以你應該感激我啊！你可以在危急的時候使用。");
	} else if (status == 2) {
	    qm.sendNextPrev("我能教你的也就到此為止了，就算覺得依依不捨，但也還是要道別了！記得要注意自己身體喔！那…再見囉！\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2010000# #t2010000# 3個\r\n#v2010009# #t2010009# 3個\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 10 exp");
	} else if (status == 3) {
	    qm.gainExp(10);
	    qm.gainItem(2010000, 3);
	    qm.gainItem(2010009, 3);
	    qm.forceCompleteQuest();
	    qm.dispose();
	}
    }
}