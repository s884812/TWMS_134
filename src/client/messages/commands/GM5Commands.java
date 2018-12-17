package client.messages.commands;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import client.Equip;
import client.GameConstants;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.anticheat.CheatingOffense;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import database.DatabaseConnection;
import handling.SendPacketOpcode;
import handling.channel.ChannelServer;
import scripting.PortalScriptManager;
import scripting.ReactorScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleShopFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.maps.MapleReactorStats;
import server.quest.MapleQuest;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;

public class GM5Commands implements Command {

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		ChannelServer cserv = c.getChannelServer();
		final MapleCharacter chr = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
		if (splitted[0].equalsIgnoreCase("!proitem")) {
			if (splitted.length == 3) {
				int itemid;
				short multiply;
				try {
					itemid = Integer.parseInt(splitted[1]);
					multiply = Short.parseShort(splitted[2]);
				} catch (NumberFormatException asd) {
					return;
				}
				MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
				IItem item = ii.getEquipById(itemid);
				MapleInventoryType type = GameConstants.getInventoryType(itemid);
				if (type.equals(MapleInventoryType.EQUIP)) {
					MapleInventoryManipulator.addFromDrop(c, ii.hardcoreItem((Equip) item, multiply), true);
				} else {
					c.getPlayer().dropMessage(6, "Make sure it's an equippable item.");
				}
			} else {
				c.getPlayer().dropMessage(6, "Invalid syntax.(!proitem (Item ID) (Stat) Example: !proitem 9999999 32767");
			}
		} else if (splitted[0].equalsIgnoreCase("!mutecall")) {
			c.getPlayer().setCallGM(!c.getPlayer().isCallGM());
			c.getPlayer().dropMessage(6, "GM Messages set to " + c.getPlayer().isCallGM());
		} else if (splitted[0].equalsIgnoreCase("!clearinv")) {
			Map<Pair<Short, Short>, MapleInventoryType> eqs = new ArrayMap<Pair<Short, Short>, MapleInventoryType>();
			if (splitted[1].equals("all")) {
				for (MapleInventoryType type : MapleInventoryType.values()) {
					for (IItem item : c.getPlayer().getInventory(type)) {
						eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), type);
					}
				}
			} else if (splitted[1].equals("eqp")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIPPED);
				}
			} else if (splitted[1].equals("eq")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIP);
				}
			} else if (splitted[1].equals("u")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.USE);
				}
			} else if (splitted[1].equals("s")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.SETUP);
				}
			} else if (splitted[1].equals("e")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.ETC);
				}
			} else if (splitted[1].equals("c")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.CASH);
				}
			} else {
				c.getPlayer().dropMessage(6, "[all/eqp/eq/u/s/e/c]");
			}
			for (Entry<Pair<Short, Short>, MapleInventoryType> eq : eqs.entrySet()) {
				MapleInventoryManipulator.removeFromSlot(c, eq.getValue(), eq.getKey().left, eq.getKey().right, false, false);
			}
		} else if (splitted[0].equalsIgnoreCase("!ban")) {
			if (splitted.length < 3) {
				return;
			}
			final StringBuilder sb = new StringBuilder(c.getPlayer().getName());
			sb.append(" banned ").append(splitted[1]).append(": ").append(StringUtil.joinStringFrom(splitted, 2));
			if (chr != null) {
				sb.append(" (IP: ").append(chr.getClient().getSession().getRemoteAddress().toString().split(":")[0]).append(")");
				if (chr.ban(sb.toString(), false, false)) {
					c.getPlayer().dropMessage(6, "Successfully banned.");
				} else {
					c.getPlayer().dropMessage(6, "Failed to ban.");
				}
			} else {
				if (MapleCharacter.ban(splitted[1], sb.toString(), false)) {
					sb.append(" (IP: ").append(chr.getClient().getSession().getRemoteAddress().toString().split(":")[0]).append(")");
				} else {
					c.getPlayer().dropMessage(6, "Failed to ban " + splitted[1]);
				}
			}
		} else if (splitted[0].equalsIgnoreCase("!tempban")) {
			final MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			final int reason = Integer.parseInt(splitted[2]);
			final int numDay = Integer.parseInt(splitted[3]);
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, numDay);
			final DateFormat df = DateFormat.getInstance();
			if (victim == null) {
				c.getPlayer().dropMessage(6, "Unable to find character");
				return;
			}
			victim.tempban("Temp banned by : " + c.getPlayer().getName() + "", cal, reason, true);
			c.getPlayer().dropMessage(6, "The character " + splitted[1] + " has been successfully tempbanned till " + df.format(cal.getTime()));
		} else if (splitted[0].equalsIgnoreCase("!unban")) {
			if (splitted.length < 1) {
				c.getPlayer().dropMessage(6, "!unban <Character name>");
			} else {
				final byte result = c.unban(splitted[1]);
				if (result == -1) {
					c.getPlayer().dropMessage(6, "No character found with that name.");
				} else if (result == -2) {
					c.getPlayer().dropMessage(6, "Error occured while unbanning, please try again later.");
				} else {
					c.getPlayer().dropMessage(6, "Character successfully unbanned.");
				}
			}
		} else if (splitted[0].equalsIgnoreCase("!dc")) {
			int level = 0;
			MapleCharacter victim;
			if (splitted[1].charAt(0) == '-') {
				level = StringUtil.countCharacters(splitted[1], 'f');
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
			} else {
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			}
			if (level < 2) {
				victim.getClient().getSession().close();
				if (level >= 1) {
					victim.getClient().disconnect(true, false);
				}
			} else {
				c.getPlayer().dropMessage(6, "Please use dc -f instead.");
			}
		} else if (splitted[0].equalsIgnoreCase("!resetquest")) {
			MapleQuest.getInstance(Integer.parseInt(splitted[1])).forfeit(c.getPlayer());
		} else if (splitted[0].equalsIgnoreCase("!nearestPortal")) {
			final MaplePortal portal = chr.getMap().findClosestSpawnpoint(chr.getPosition());
			c.getPlayer().dropMessage(6, portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());
		} else if (splitted[0].equalsIgnoreCase("!spawndebug")) {
			c.getPlayer().dropMessage(6, c.getPlayer().getMap().spawnDebug());
		} else if (splitted[0].equalsIgnoreCase("!threads")) {
			Thread[] threads = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);
			String filter = "";
			if (splitted.length > 1) {
				filter = splitted[1];
			}
			for (int i = 0; i < threads.length; i++) {
				String tstring = threads[i].toString();
				if (tstring.toLowerCase().indexOf(filter.toLowerCase()) > -1) {
					c.getPlayer().dropMessage(6, i + ": " + tstring);
				}
			}
		} else if (splitted[0].equalsIgnoreCase("!showtrace")) {
			if (splitted.length < 2) {
				throw new IllegalCommandSyntaxException(2);
			}
			Thread[] threads = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);
			Thread t = threads[Integer.parseInt(splitted[1])];
			c.getPlayer().dropMessage(6, t.toString() + ":");
			for (StackTraceElement elem : t.getStackTrace()) {
				c.getPlayer().dropMessage(6, elem.toString());
			}
		} else if (splitted[0].equalsIgnoreCase("!toggleoffense")) {
			try {
				CheatingOffense co = CheatingOffense.valueOf(splitted[1]);
				co.setEnabled(!co.isEnabled());
			} catch (IllegalArgumentException iae) {
				c.getPlayer().dropMessage(6, "Offense " + splitted[1] + " not found");
			}
		} else if (splitted[0].equalsIgnoreCase("!tdrops")) {
			chr.getMap().toggleDrops();
		} else if (splitted[0].equalsIgnoreCase("!tmegaphone")) {
			try {
				c.getChannelServer().getWorldInterface().toggleMegaphoneMuteState();
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
			c.getPlayer().dropMessage(6, "Megaphone state : " + (c.getChannelServer().getMegaphoneMuteState() ? "Enabled" : "Disabled"));
		} else if (splitted[0].equalsIgnoreCase("!sreactor")) {
			MapleReactorStats reactorSt = MapleReactorFactory.getReactor(Integer.parseInt(splitted[1]));
			MapleReactor reactor = new MapleReactor(reactorSt, Integer.parseInt(splitted[1]));
			reactor.setDelay(-1);
			reactor.setPosition(c.getPlayer().getPosition());
			c.getPlayer().getMap().spawnReactor(reactor);
		} else if (splitted[0].equalsIgnoreCase("!hreactor")) {
			c.getPlayer().getMap().getReactorByOid(Integer.parseInt(splitted[1])).hitReactor(c);
		} else if (splitted[0].equalsIgnoreCase("!lreactor")) {
			MapleMap map = c.getPlayer().getMap();
			List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
			for (MapleMapObject reactorL : reactors) {
				MapleReactor reactor2l = (MapleReactor) reactorL;
				c.getPlayer().dropMessage(6, "Reactor: oID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getReactorId() + " Position: " + reactor2l.getPosition().toString() + " State: " + reactor2l.getState());
			}
		} else if (splitted[0].equalsIgnoreCase("!dreactor")) {
			MapleMap map = c.getPlayer().getMap();
			List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
			if (splitted[1].equals("all")) {
				for (MapleMapObject reactorL : reactors) {
					MapleReactor reactor2l = (MapleReactor) reactorL;
					c.getPlayer().getMap().destroyReactor(reactor2l.getObjectId());
				}
			} else {
				c.getPlayer().getMap().destroyReactor(Integer.parseInt(splitted[1]));
			}
		} else if (splitted[0].equalsIgnoreCase("!resetreactor")) {
			c.getPlayer().getMap().resetReactors();
		} else if (splitted[0].equalsIgnoreCase("!setreactor")) {
			c.getPlayer().getMap().setReactorState();
		} else if (splitted[0].equalsIgnoreCase("!removedrops")) {
			List<MapleMapObject> items = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
			for (MapleMapObject i : items) {
				c.getPlayer().getMap().removeMapObject(i);
				c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, 0), i.getPosition());
			}
		} else if (splitted[0].equalsIgnoreCase("!exprate")) {
			if (splitted.length > 1) {
				final byte rate = Byte.parseByte(splitted[1]);
				c.getChannelServer().setExpRate(rate);
				c.getPlayer().dropMessage(6, "Exprate has been changed to " + rate + "x");
			} else {
				c.getPlayer().dropMessage(6, "Syntax: !exprate <number>");
			}
		} else if (splitted[0].equalsIgnoreCase("!droprate")) {
			if (splitted.length > 1) {
				final byte rate = Byte.parseByte(splitted[1]);
				c.getChannelServer().setDropRate(rate);
				c.getPlayer().dropMessage(6, "Drop Rate has been changed to " + rate + "x");
			} else {
				c.getPlayer().dropMessage(6, "Syntax: !droprate <number>");
			}
		} else if (splitted[0].equalsIgnoreCase("!dcall")) {
			c.getChannelServer().getPlayerStorage().disconnectAll();
		} else if (splitted[0].equalsIgnoreCase("!pnpc")) {
			int npcId = Integer.parseInt(splitted[1]);
			MapleNPC npc = MapleLifeFactory.getNPC(npcId);
			int xpos = c.getPlayer().getPosition().x;
			int ypos = c.getPlayer().getPosition().y;
			int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
			if (npc != null && !npc.getName().equals("MISSINGNO")) {
				npc.setPosition(c.getPlayer().getPosition());
				npc.setCy(ypos);
				npc.setRx0(xpos + 50);
				npc.setRx1(xpos - 50);
				npc.setFh(fh);
				npc.setCustom(true);
				try {
					Connection con = DatabaseConnection.getConnection();
					PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
					ps.setInt(1, npcId);
					ps.setInt(2, 0);
					ps.setInt(3, fh);
					ps.setInt(4, ypos);
					ps.setInt(5, xpos + 50);
					ps.setInt(6, xpos - 50);
					ps.setString(7, "n");
					ps.setInt(8, xpos);
					ps.setInt(9, ypos);
					ps.setInt(10, c.getPlayer().getMapId());
					ps.executeUpdate();
				} catch (SQLException e) {
					c.getPlayer().dropMessage(6, "Failed to save NPC to the database");
				}
				c.getPlayer().getMap().addMapObject(npc);
				c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
			} else {
				c.getPlayer().dropMessage(6, "You have entered an invalid Npc-Id");
			}
		} /*else if (splitted[0].equalsIgnoreCase("!pmob")) {
		int npcId = Integer.parseInt(splitted[1]);
		int mobTime = Integer.parseInt(splitted[2]);
		int xpos = c.getPlayer().getPosition().x;
		int ypos = c.getPlayer().getPosition().y;
		int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
		if (splitted[2] == null) {
		mobTime = 0;
		}
		MapleMonster mob = MapleLifeFactory.getMonster(npcId);
		if (mob != null && !mob.getName().equals("MISSINGNO")) {
		mob.setPosition(c.getPlayer().getPosition());
		mob.setCy(ypos);
		mob.setRx0(xpos + 50);
		mob.setRx1(xpos - 50);
		mob.setFh(fh);
		try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
		ps.setInt(1, npcId);
		ps.setInt(2, 0);
		ps.setInt(3, fh);
		ps.setInt(4, ypos);
		ps.setInt(5, xpos + 50);
		ps.setInt(6, xpos - 50);
		ps.setString(7, "m");
		ps.setInt(8, xpos);
		ps.setInt(9, ypos);
		ps.setInt(10, c.getPlayer().getMapId());
		ps.setInt(11, mobTime);
		ps.executeUpdate();
		} catch (SQLException e) {
		c.getPlayer().dropMessage(6, "Failed to save MOB to the database");
		}
		c.getPlayer().getMap().addMonsterSpawn(mob, mobTime);
		} else {
		c.getPlayer().dropMessage(6, "You have entered an invalid Mob-Id");
		}
		} */ else if (splitted[0].equalsIgnoreCase("!reloadops")) {
			SendPacketOpcode.reloadValues();
		} else if (splitted[0].equalsIgnoreCase("!reloaddrops")) {
			MapleMonsterInformationProvider.getInstance().clearDrops();
			ReactorScriptManager.getInstance().clearDrops();
		} else if (splitted[0].equalsIgnoreCase("!reloadportal")) {
			PortalScriptManager.getInstance().clearScripts();
		} else if (splitted[0].equalsIgnoreCase("!clearshops")) {
			MapleShopFactory.getInstance().clear();
		} else if (splitted[0].equalsIgnoreCase("!clearevents")) {
			for (ChannelServer instance : ChannelServer.getAllInstances()) {
				instance.reloadEvents();
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("proitem", "", "", 5),
			new CommandDefinition("mutecall", "", "", 5),
			new CommandDefinition("clearinv", "", "", 5),
			new CommandDefinition("ban", "charname reason", "Permanently ip, mac and accountbans the given character", 5),
			new CommandDefinition("tempban", "<name> <reason> <numDay>", "Tempbans the given account", 5),
			new CommandDefinition("dc", "[-f] name", "Disconnects player matching name provided. Use -f only if player is persistent!", 5),
			new CommandDefinition("dcall", "", "Disconnects every players", 5),
			new CommandDefinition("removedrops", "", "", 5),
			new CommandDefinition("resetquest", "", "", 5),
			new CommandDefinition("nearestPortal", "", "", 5),
			new CommandDefinition("spawndebug", "", "", 5),
			new CommandDefinition("tmegaphone", "", "", 5),
			new CommandDefinition("threads", "", "", 5),
			new CommandDefinition("showtrace", "", "", 5),
			new CommandDefinition("toggleoffense", "", "", 5),
			new CommandDefinition("tdrops", "", "", 5),
			new CommandDefinition("sreactor", "[id]", "Spawn a Reactor", 5),
			new CommandDefinition("hreactor", "[object ID]", "Hit reactor", 5),
			new CommandDefinition("resetreactor", "", "Resets all reactors", 5),
			new CommandDefinition("lreactor", "", "List reactors", 5),
			new CommandDefinition("dreactor", "", "Remove a Reactor", 5),
			new CommandDefinition("setreactor", "", "Set reactor state", 5),
			new CommandDefinition("exprate", "rate", "Changes the exp rate", 5),
			new CommandDefinition("droprate", "rate", "Changes the drop rate", 5),
			new CommandDefinition("pnpc", "npc id", "Placing a NPC on the spot permanently", 5),
			new CommandDefinition("reloadops", "", "", 5),
			new CommandDefinition("reloadportal", "", "", 5),
			new CommandDefinition("reloaddrops", "", "", 5),
			new CommandDefinition("clearshops", "", "", 5),
			new CommandDefinition("clearevents", "", "", 5)
		};
	}
}