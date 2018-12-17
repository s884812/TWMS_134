package client.messages.commands;

import java.awt.Point;
import java.util.List;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import tools.MaplePacketCreator;

public class NPCSpawningCommands implements Command {

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		if (splitted[0].equalsIgnoreCase("!npc")) {
			int npcId = Integer.parseInt(splitted[1]);
			MapleNPC npc = MapleLifeFactory.getNPC(npcId);
			if (npc != null && !npc.getName().equals("MISSINGNO")) {
				npc.setPosition(c.getPlayer().getPosition());
				npc.setCy(c.getPlayer().getPosition().y);
				npc.setRx0(c.getPlayer().getPosition().x + 50);
				npc.setRx1(c.getPlayer().getPosition().x - 50);
				npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
				npc.setCustom(true);
				c.getPlayer().getMap().addMapObject(npc);
				c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
			} else {
				c.getPlayer().dropMessage(6, "You have entered an invalid Npc-Id");
			}
		} else if (splitted[0].equalsIgnoreCase("!removenpcs")) {
			List<MapleMapObject> npcs = c.getPlayer().getMap().getAllNPC();
			for (MapleMapObject npcmo : npcs) {
				MapleNPC npc = (MapleNPC) npcmo;
				if (npc.isCustom()) {
					c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, false));
					c.getPlayer().getMap().removeMapObject(npc.getObjectId());
				}
			}
		} else if (splitted[0].equalsIgnoreCase("!mynpcpos")) {
			Point pos = c.getPlayer().getPosition();
			c.getPlayer().dropMessage(6, "CY: " + pos.y + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getMap().getFootholds().findBelow(pos).getId());
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("npc", "npcid", "Spawns the npc with the given id at the player position", 5),
			new CommandDefinition("removenpcs", "", "Removes all custom spawned npcs from the map - requires reentering the map", 5),
			new CommandDefinition("mynpcpos", "", "Gets the info for making an npc", 5)
		};
	}
}