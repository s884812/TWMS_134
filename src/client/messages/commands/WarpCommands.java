package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import handling.channel.ChannelServer;
import server.MaplePortal;
import server.maps.MapleMap;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import tools.MaplePacketCreator;

public class WarpCommands implements Command {

	private MapleCharacter victim;

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		ChannelServer cserv = c.getChannelServer();
		if (splitted[0].equalsIgnoreCase("!warp")) {
			victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			if ((victim != null)) {
				if (splitted.length == 2) {
					c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
				} else {
					MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(Integer.parseInt(splitted[2]));
					victim.changeMap(target, target.getPortal(0));
				}
			} else {
				try {
					victim = c.getPlayer();
					int victimw = cserv.getPlayerStorage().getCharacterByName(splitted[1]).getWorld();
					MapleMap target = cserv.getMapFactory(c.getPlayer().getWorld()).getMap(Integer.parseInt(splitted[1]));
					if (c.getChannelServer().getWorldInterface().getLocation(splitted[1]) == null) {
						c.getPlayer().changeMap(target, target.getPortal(0));
					} else {
						c.getPlayer().dropMessage(6, "Please make sure that you are on the same channel as the user.");
					}
				} catch (Exception e) {
					c.getPlayer().dropMessage(6, "Something went wrong " + e.getMessage());
				}
			}
		} else if (splitted[0].equalsIgnoreCase("!worldtrip")) {
			victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			for (int i = 1; i <= 10; i++) {
				MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(200000000);
				MaplePortal targetPortal = target.getPortal(0);
				victim.changeMap(target, targetPortal);
				MapleMap target1 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(102000000);
				MaplePortal targetPortal1 = target.getPortal(0);
				victim.changeMap(target1, targetPortal1);
				MapleMap target2 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(103000000);
				MaplePortal targetPortal2 = target.getPortal(0);
				victim.changeMap(target2, targetPortal2);
				MapleMap target3 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(100000000);
				MaplePortal targetPortal3 = target.getPortal(0);
				victim.changeMap(target3, targetPortal3);
				MapleMap target4 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(200000000);
				MaplePortal targetPortal4 = target.getPortal(0);
				victim.changeMap(target4, targetPortal4);
				MapleMap target5 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(211000000);
				MaplePortal targetPortal5 = target.getPortal(0);
				victim.changeMap(target5, targetPortal5);
				MapleMap target6 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(230000000);
				MaplePortal targetPortal6 = target.getPortal(0);
				victim.changeMap(target6, targetPortal6);
				MapleMap target7 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(222000000);
				MaplePortal targetPortal7 = target.getPortal(0);
				victim.changeMap(target7, targetPortal7);
				MapleMap target8 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(251000000);
				MaplePortal targetPortal8 = target.getPortal(0);
				victim.changeMap(target8, targetPortal8);
				MapleMap target9 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(220000000);
				MaplePortal targetPortal9 = target.getPortal(0);
				victim.changeMap(target9, targetPortal9);
				MapleMap target10 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(221000000);
				MaplePortal targetPortal10 = target.getPortal(0);
				victim.changeMap(target10, targetPortal10);
				MapleMap target11 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(240000000);
				MaplePortal targetPortal11 = target.getPortal(0);
				victim.changeMap(target11, targetPortal11);
				MapleMap target12 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(600000000);
				MaplePortal targetPortal12 = target.getPortal(0);
				victim.changeMap(target12, targetPortal12);
				MapleMap target13 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(800000000);
				MaplePortal targetPortal13 = target.getPortal(0);
				victim.changeMap(target13, targetPortal13);
				MapleMap target14 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(680000000);
				MaplePortal targetPortal14 = target.getPortal(0);
				victim.changeMap(target14, targetPortal14);
				MapleMap target15 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(105040300);
				MaplePortal targetPortal15 = target.getPortal(0);
				victim.changeMap(target15, targetPortal15);
				MapleMap target16 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(990000000);
				MaplePortal targetPortal16 = target.getPortal(0);
				victim.changeMap(target16, targetPortal16);
				MapleMap target17 = ChannelServer.getInstance(c.getChannel()).getMapFactory(c.getPlayer().getWorld()).getMap(100000001);
				MaplePortal targetPortal17 = target.getPortal(0);
				victim.changeMap(target17, targetPortal17);
			}
			victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(
					c.getPlayer().getPosition()));
		} else if (splitted[0].equalsIgnoreCase("!warphere")) {
			int victimw = cserv.getPlayerStorage().getCharacterByName(splitted[1]).getWorld();
			victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));
		} else if (splitted[0].equalsIgnoreCase("!slime")) {
			MapleMonster mob0 = MapleLifeFactory.getMonster(9400202);
			c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob0, c.getPlayer().getPosition());
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(0, "[Event] EXP slimes!"));
		} else if (splitted[0].equalsIgnoreCase("!lolcastle")) {
			if (splitted.length != 2) {
				c.getPlayer().dropMessage(6, "Syntax: !lolcastle level (level = 1-5)");
			}
			MapleMap target = c.getChannelServer().getEventSM(c.getPlayer().getWorld()).getEventManager("lolcastle").getInstance("lolcastle" + splitted[1]).getMapFactory().getMap(990000300, false, false);
			c.getPlayer().changeMap(target, target.getPortal(0));
		} else if (splitted[0].equalsIgnoreCase("!map")) {
			MapleMap target = cserv.getMapFactory(c.getPlayer().getWorld()).getMap(Integer.parseInt(splitted[1]));
			MaplePortal targetPortal = null;
			if (splitted.length > 2) {
				try {
					targetPortal = target.getPortal(Integer.parseInt(splitted[2]));
				} catch (IndexOutOfBoundsException e) {
					c.getPlayer().dropMessage(5, "Invalid portal selected.");
				} catch (NumberFormatException a) {
					c.getPlayer().dropMessage(5, "Invalid map id.");
				}
			}
			if (targetPortal == null) {
				targetPortal = target.getPortal(0);
			}
			c.getPlayer().changeMap(target, targetPortal);
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("warp", "playername [targetid]", "Warps yourself to the player with the given name. When targetid is specified warps the player to the given mapid", 2),
			new CommandDefinition("warphere", "playername", "Warps the player with the given name to yourself", 3),
			new CommandDefinition("lolcastle", "[1-5]", "Warps you into Field of Judgement with the given level", 5),
			new CommandDefinition("map", "mapid", "Warps you to the given mapid (use /m instead)", 3),
			new CommandDefinition("worldtrip", "name", "Warps you to the given mapid (use /m instead)", 4),
			new CommandDefinition("slime", "monsterid", "summons nxx slime", 3)
		};
	}
}