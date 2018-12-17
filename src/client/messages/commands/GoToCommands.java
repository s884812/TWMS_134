package client.messages.commands;

import java.util.HashMap;

import client.messages.CommandDefinition;
import client.messages.Command;
import client.messages.IllegalCommandSyntaxException;
import server.MaplePortal;
import server.maps.MapleMap;
import client.MapleClient;

public class GoToCommands implements Command { // GM2

	private static final HashMap<String, Integer> gotomaps = new HashMap<String, Integer>();

	public GoToCommands() {
		gotomaps.put("gmmap", 180000000);
		gotomaps.put("southperry", 60000);
		gotomaps.put("amherst", 1010000);
		gotomaps.put("henesys", 100000000);
		gotomaps.put("ellinia", 101000000);
		gotomaps.put("perion", 102000000);
		gotomaps.put("kerning", 103000000);
		gotomaps.put("lithharbour", 104000000);
		gotomaps.put("sleepywood", 105040300);
		gotomaps.put("florina", 110000000);
		gotomaps.put("orbis", 200000000);
		gotomaps.put("happyville", 209000000);
		gotomaps.put("elnath", 211000000);
		gotomaps.put("ludibrium", 220000000);
		gotomaps.put("aquaroad", 230000000);
		gotomaps.put("leafre", 240000000);
		gotomaps.put("mulung", 250000000);
		gotomaps.put("herbtown", 251000000);
		gotomaps.put("omegasector", 221000000);
		gotomaps.put("koreanfolktown", 222000000);
		gotomaps.put("newleafcity", 600000000);
		gotomaps.put("sharenian", 990000000);
		gotomaps.put("pianus", 230040420);
		gotomaps.put("horntail", 240060200);
		gotomaps.put("mushmom", 100000005);
		gotomaps.put("griffey", 240020101);
		gotomaps.put("manon", 240020401);
		gotomaps.put("zakum", 280030000);
		gotomaps.put("papulatus", 220080001);
		gotomaps.put("showatown", 801000000);
		gotomaps.put("zipangu", 800000000);
		gotomaps.put("ariant", 260000100);
		gotomaps.put("nautilus", 120000000);
		gotomaps.put("boatquay", 541000000);
		gotomaps.put("malaysia", 550000000);
		gotomaps.put("taiwan", 740000000);
		gotomaps.put("thailand", 500000000);
		gotomaps.put("erev", 130000000);
		gotomaps.put("ellinforest", 300000000);
		gotomaps.put("kampung", 551000000);
		gotomaps.put("singapore", 540000000);
		gotomaps.put("amoria", 680000000);
		gotomaps.put("timetemple", 270000000);
		gotomaps.put("pinkbean", 270050100);
		gotomaps.put("peachblossom", 700000000);
		gotomaps.put("fm", 910000000);
	}

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		if (splitted.length < 2) {
			c.getPlayer().dropMessage(6, "Syntax: !goto <mapname>");
		} else {
			if (gotomaps.containsKey(splitted[1])) {
				MapleMap target = c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(gotomaps.get(splitted[1]));
				MaplePortal targetPortal = target.getPortal(0);
				c.getPlayer().changeMap(target, targetPortal);
			} else {
				if (splitted[1].equals("locations")) {
					c.getPlayer().dropMessage(6, "Use !goto <location>. Locations are as follows:");
					StringBuilder sb = new StringBuilder();
					for (String s : gotomaps.keySet()) {
						sb.append(s + ", ");
					}
					c.getPlayer().dropMessage(6, sb.substring(0, sb.length() - 2));
				} else {
					c.getPlayer().dropMessage(6, "Invalid command syntax - Use !goto <location>. For a list of locations, use !goto locations.");
				}
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("goto", "?", "go <town/map name>", 2)
		};
	}
}