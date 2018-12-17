package server;

import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.WorldServer;
import handling.cashshop.CashShopServer;
import server.TimerManager;

public class Start {
	public final static void main(final String args[]) {
		if (args[0].equals("CHANNEL")) {
			ChannelServer.startChannel_Main();
		} else if (args[0].equals("LOGIN")) {
			LoginServer.startLogin_Main();
		} else if (args[0].equals("WORLD")) {
			WorldServer.startWorld_Main();
		} else if (args[0].equals("CASHSHOP")) {
			CashShopServer.startCashShop_main();
		} else {
			System.out.println("Invalid input for selected servers: 'CASHSHOP', 'CHANNEL', 'LOGIN' and 'WORLD'.");
		}
	}
}