package client;

import handling.ServerConstants;
import handling.channel.ChannelServer;
import server.TimerManager;
import tools.MaplePacketCreator;
import server.Randomizer;

public class OdinSEA {

	public static int[] BlockedNPC = {
		// Nothing to block
	};

	public static MapleClient c;

	public OdinSEA(final MapleClient c) {
		OdinSEA.c = c;
	}

	public final MapleClient getClient() {
		return c;
	}

	public static void start() {
		/*TimerManager.getInstance().register(new Runnable() {
            
			public final void run() {
				String[] messages = {
					"Thanks for using OdinSEA",
					"http://www.facebook.com/johnlth93",
					"http://www.facebook.com/OdinSEA",
					"Try @dispose and @ea if you are unable to click on npc or attack",
					":: http://www.johnlth93.tk/ ::",
					":: http://www.odinsea.tk/ ::",
					":: http://www.ppstream-vip.tk/ ::",
					":: http://www.facebook.com/johnlth93 ::",};
				int totalmessages = messages.length;
				int crandom = Randomizer.nextInt(totalmessages);
				for (ChannelServer cserv : ChannelServer.getAllInstances()) {
					cserv.broadcastPacket(MaplePacketCreator.sendMapleTip("[MapleTip] " + messages[crandom]));
				}
			}
		}, 300000); // 5 minutes once*/
		TimerManager.getInstance().register(new Runnable() {
                    
			public final void run() {
				for (ChannelServer cserv : ChannelServer.getAllInstances()) {
					for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
						chr.saveToDB(false, false);
					}
				}
			}
		}, 300000); // 5 minutes once
		/*TimerManager.getInstance().register(new Runnable() {
		public void run() {
		int mapid = 24004611;
		int mapid2 = 24004612;
		//int eggid = 4001094; // Nine Spirit Egg
		int neweggid = 2041200; //Dragon Stone
		short gain = 1;
		short loose = -1;
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
		for (int w = 5; w <= 6; w++){
		for (MapleCharacter player : cserv.getMapFactory(w).getMap(mapid).getCharacters()) {
		if (player.haveItem(eggid, 1, true, true)){ // equipped
		MapleInventoryManipulator.removeById(player.getClient(), MapleInventoryType.ETC, eggid, loose, true, true);
		MapleInventoryManipulator.addById(player.getClient(), neweggid, gain);
		}
		}
		}
		}
		}
		}, 60000);// 1min??*/
		/*TimerManager.getInstance().register(new Runnable() {
		public final void run() {
		StringBuilder conStr = new StringBuilder("Connected Clients: ");
		Map<Integer, Integer> connected = null;
		try {
		connected = ChannelServer.getInstance(1).getWorldInterface().getConnected();
		} catch (RemoteException ex) {
		Logger.getLogger(ChannelServer.class.getName()).log(Level.SEVERE, null, ex);
		}
		boolean first = true;
		for (int i : connected.keySet()) {
		if (!first) {
		conStr.append(", ");
		} else {
		first = false;
		}
		if (i == 0) {
		conStr.append("Total: ");
		conStr.append(connected.get(i));
		} else {
		conStr.append("Channel");
		conStr.append(i);
		conStr.append(": ");
		conStr.append(connected.get(i));
		}
		}
		System.out.println(conStr.toString());
		}
		}, 120000);
		TimerManager.getInstance().register(new Runnable() {
		public final void run() {
		MapleMap map;
		for (int i = 1; i <= 22; i++) {
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
		map = cserv.getMapFactory(5).getMap(910000000 + i);
		if (map.getAllPlayer().size() <= 0) {
		map.killAllMonsters(false);
		}
		}
		}
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
		cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "FREE MARKET has just been optimized!"));
		}
		}
		}, 600000);*/
		System.out.println(":: Server kernel : OdinSEA Revision 154 ::");
                System.out.println(":: http://www.odinsea.tk/ ::");
                if (ServerConstants.DebugMode == true) {
			System.out.println("Debug Mode is on, please turn it off if you're not debugging this emulator!");
		}
		System.out.println("!!!  請注意此 SERVER EMULATOR 僅供學術和技術上使用  !!!");
		System.out.println("!!!           請勿使用於任何商業相關活動上          !!!");
		System.out.println("!!!    如果你使用了，請立即中止，否則後果自行承擔    !!!");
	}
}