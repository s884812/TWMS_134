package client.messages.commands;

import java.rmi.RemoteException;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class NoticeCommand implements Command { // GM3

	private static int getNoticeType(String typestring) {
		if (typestring.equals("n")) {
			return 0;
		} else if (typestring.equals("p")) {
			return 1;
		} else if (typestring.equals("l")) {
			return 2;
		} else if (typestring.equals("nv")) {
			return 5;
		} else if (typestring.equals("v")) {
			return 5;
		} else if (typestring.equals("b")) {
			return 6;
		}
		return -1;
	}

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		int joinmod = 1;
		int range = -1;
		if (splitted[1].equals("m")) {
			range = 0;
		} else if (splitted[1].equals("c")) {
			range = 1;
		} else if (splitted[1].equals("w")) {
			range = 2;
		}
		int tfrom = 2;
		if (range == -1) {
			range = 2;
			tfrom = 1;
		}
		int type = getNoticeType(splitted[tfrom]);
		if (type == -1) {
			type = 0;
			joinmod = 0;
		}
		StringBuilder sb = new StringBuilder();
		if (splitted[tfrom].equals("nv")) {
			sb.append("[Notice]");
		} else {
			sb.append("");
		}
		joinmod += tfrom;
		sb.append(StringUtil.joinStringFrom(splitted, joinmod));
		MaplePacket packet = MaplePacketCreator.serverNotice(type, sb.toString());
		if (range == 0) {
			c.getPlayer().getMap().broadcastMessage(packet);
		} else if (range == 1) {
			ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
		} else if (range == 2) {
			try {
				ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(packet.getBytes());
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("notice", "[mcw] [n/p/l/nv/v/b] message", "", 3)
		};
	}
}