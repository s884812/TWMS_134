package client.messages.commands;

import client.MapleCharacter;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import handling.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;

public class GM1Commands implements Command {

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		ChannelServer cserv = c.getChannelServer();
		final MapleCharacter chr = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
		if (splitted[0].equalsIgnoreCase("!song")) {
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(splitted[1]));
		} else if (splitted[0].equalsIgnoreCase("!shop")) {
			MapleShopFactory shop = MapleShopFactory.getInstance();
			int shopId = Integer.parseInt(splitted[1]);
			if (shop.getShop(shopId) != null) {
				shop.getShop(shopId).sendShop(c);
			}
		} else if (splitted[0].equalsIgnoreCase("!whereami")) {
			c.getPlayer().dropMessage(5, "You are on map " + c.getPlayer().getMap().getId());
		} else if (splitted[0].equalsIgnoreCase("!search")) {
			if (splitted.length == 1) {
				c.getPlayer().dropMessage(6, splitted[0] + ": <NPC> <MOB> <ITEM> <MAP> <SKILL>");
			} else {
				String type = splitted[1];
				String search = StringUtil.joinStringFrom(splitted, 2);
				MapleData data = null;
				MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz"));
				c.getPlayer().dropMessage(6, "<<Type: " + type + " | Search: " + search + ">>");
				if (type.equalsIgnoreCase("NPC")) {
					List<String> retNpcs = new ArrayList<String>();
					data = dataProvider.getData("Npc.img");
					List<Pair<Integer, String>> npcPairList = new LinkedList<Pair<Integer, String>>();
					for (MapleData npcIdData : data.getChildren()) {
						npcPairList.add(new Pair<Integer, String>(Integer.parseInt(npcIdData.getName()), MapleDataTool.getString(npcIdData.getChildByPath("name"), "NO-NAME")));
					}
					for (Pair<Integer, String> npcPair : npcPairList) {
						if (npcPair.getRight().toLowerCase().contains(search.toLowerCase())) {
							retNpcs.add(npcPair.getLeft() + " - " + npcPair.getRight());
						}
					}
					if (retNpcs != null && retNpcs.size() > 0) {
						for (String singleRetNpc : retNpcs) {
							c.getPlayer().dropMessage(6, singleRetNpc);
						}
					} else {
						c.getPlayer().dropMessage(6, "No NPC's Found");
					}
				} else if (type.equalsIgnoreCase("MAP")) {
					List<String> retMaps = new ArrayList<String>();
					data = dataProvider.getData("Map.img");
					List<Pair<Integer, String>> mapPairList = new LinkedList<Pair<Integer, String>>();
					for (MapleData mapAreaData : data.getChildren()) {
						for (MapleData mapIdData : mapAreaData.getChildren()) {
							mapPairList.add(new Pair<Integer, String>(Integer.parseInt(mapIdData.getName()), MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME")));
						}
					}
					for (Pair<Integer, String> mapPair : mapPairList) {
						if (mapPair.getRight().toLowerCase().contains(search.toLowerCase())) {
							retMaps.add(mapPair.getLeft() + " - " + mapPair.getRight());
						}
					}
					if (retMaps != null && retMaps.size() > 0) {
						for (String singleRetMap : retMaps) {
							c.getPlayer().dropMessage(6, singleRetMap);
						}
					} else {
						c.getPlayer().dropMessage(6, "No Maps Found");
					}
				} else if (type.equalsIgnoreCase("MOB")) {
					List<String> retMobs = new ArrayList<String>();
					data = dataProvider.getData("Mob.img");
					List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
					for (MapleData mobIdData : data.getChildren()) {
						mobPairList.add(new Pair<Integer, String>(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
					}
					for (Pair<Integer, String> mobPair : mobPairList) {
						if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
							retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
						}
					}
					if (retMobs != null && retMobs.size() > 0) {
						for (String singleRetMob : retMobs) {
							c.getPlayer().dropMessage(6, singleRetMob);
						}
					} else {
						c.getPlayer().dropMessage(6, "No Mob's Found");
					}
				} else if (type.equalsIgnoreCase("REACTOR")) {
					c.getPlayer().dropMessage(6, "Not available at this moment");
				} else if (type.equalsIgnoreCase("ITEM")) {
					List<String> retItems = new ArrayList<String>();
					for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
						if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
							retItems.add(itemPair.getLeft() + " - " + itemPair.getRight());
						}
					}
					if (retItems != null && retItems.size() > 0) {
						for (String singleRetItem : retItems) {
							c.getPlayer().dropMessage(6, singleRetItem);
						}
					} else {
						c.getPlayer().dropMessage(6, "No Item's Found");
					}
				} else if (type.equalsIgnoreCase("SKILL")) {
					List<String> retSkills = new ArrayList<String>();
					data = dataProvider.getData("Skill.img");
					List<Pair<Integer, String>> skillPairList = new LinkedList<Pair<Integer, String>>();
					for (MapleData skillIdData : data.getChildren()) {
						skillPairList.add(new Pair<Integer, String>(Integer.parseInt(skillIdData.getName()), MapleDataTool.getString(skillIdData.getChildByPath("name"), "NO-NAME")));
					}
					for (Pair<Integer, String> skillPair : skillPairList) {
						if (skillPair.getRight().toLowerCase().contains(search.toLowerCase())) {
							retSkills.add(skillPair.getLeft() + " - " + skillPair.getRight());
						}
					}
					if (retSkills != null && retSkills.size() > 0) {
						for (String singleRetSkill : retSkills) {
							c.getPlayer().dropMessage(6, singleRetSkill);
						}
					} else {
						c.getPlayer().dropMessage(6, "No Skills Found");
					}
				} else {
					c.getPlayer().dropMessage(6, "Sorry, that search call is unavailable");
				}
			}
		} else if (splitted[0].equalsIgnoreCase("!fakerelog")) {
			c.getSession().write(MaplePacketCreator.getCharInfo(chr));
			chr.getMap().removePlayer(chr);
			chr.getMap().addPlayer(chr);
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[]{
			new CommandDefinition("song", "", "", 1),
			new CommandDefinition("shop", "", "", 1),
			new CommandDefinition("whereami", "", "", 1),
			new CommandDefinition("search", "", "", 1),
			new CommandDefinition("fakerelog", "", "", 1)
		};
	}
}