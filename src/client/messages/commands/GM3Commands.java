package client.messages.commands;

import java.rmi.RemoteException;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class GM3Commands implements Command {

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		ChannelServer cserv = c.getChannelServer();
		if (splitted[0].equalsIgnoreCase("!say")) {
			if (splitted.length > 1) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				sb.append(c.getPlayer().getName());
				sb.append("] ");
				sb.append(StringUtil.joinStringFrom(splitted, 1));
				MaplePacket packet = MaplePacketCreator.serverNotice(6, sb.toString());
				try {
					ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(packet.getBytes());
				} catch (RemoteException e) {
					c.getChannelServer().reconnectWorld();
				}
			} else {
				c.getPlayer().dropMessage(6, "Syntax: !say <message>");
			}
		} else if (splitted[0].equalsIgnoreCase("!level")) {
			c.getPlayer().setLevel(Short.parseShort(splitted[1]));
			c.getPlayer().levelUp();
			if (c.getPlayer().getExp() < 0) {
				c.getPlayer().gainExp(-c.getPlayer().getExp(), false, false, true);
			}
		} else if (splitted[0].equalsIgnoreCase("!cleardrops")) {
			MapleMap map = c.getPlayer().getMap();
			List<MapleMapObject> items = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
			for (MapleMapObject i : items) {
				map.removeMapObject(i);
				map.broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, c.getPlayer().getId()));
			}
			c.getPlayer().dropMessage(6, "You have destroyed " + items.size() + " items on the ground.");
		} else if (splitted[0].equalsIgnoreCase("!servermessage")) {
			Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
			String outputMessage = StringUtil.joinStringFrom(splitted, 1);
			cserv.setServerMessage(outputMessage);
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("say", "", "", 3),
			new CommandDefinition("level", "", "", 3),
			new CommandDefinition("cleardrops", "", "", 3),
			new CommandDefinition("servermessage", "<new message>", "Changes the servermessage to the new message", 3)
		};
	}
}