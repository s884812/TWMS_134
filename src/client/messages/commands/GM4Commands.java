package client.messages.commands;

import static client.messages.CommandProcessor.getOptionalIntArg;
import client.Equip;
import client.GameConstants;
import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import handling.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import tools.packet.MobPacket;

public class GM4Commands implements Command {

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		ChannelServer cserv = c.getChannelServer();
		if (splitted[0].equalsIgnoreCase("!item")) {
			final int itemId = Integer.parseInt(splitted[1]);
			final short quantity = (short) getOptionalIntArg(splitted, 2, 1);
			if (itemId == 2100106 | itemId == 2100107) {
				c.getPlayer().dropMessage(5, "Item is blocked.");
				return;
			}
			if (GameConstants.isPet(itemId)) {
				c.getPlayer().dropMessage(5, "Please purshase a pet from the cash shop instead.");
			} else {
				IItem item;
				if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
					MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
					item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
				} else {
					item = new Item(itemId, (byte) 0, (short) quantity, (byte) 0);
				}
				item.setOwner(c.getPlayer().getName());
				item.setGMLog(c.getPlayer().getName());
				MapleInventoryManipulator.addbyItem(c, item);
			}
		} else if (splitted[0].equalsIgnoreCase("!drop")) {
			final int itemId = Integer.parseInt(splitted[1]);
			final short quantity = (short) (short) getOptionalIntArg(splitted, 2, 1);
			if (itemId == 2100106 | itemId == 2100107) {
				c.getPlayer().dropMessage(5, "Item is blocked.");
				return;
			}
			if (GameConstants.isPet(itemId)) {
				c.getPlayer().dropMessage(5, "Please purshase a pet from the cash shop instead.");
			} else {
				IItem toDrop;
				if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
					MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
					toDrop = ii.randomizeStats((Equip) ii.getEquipById(itemId));
				} else {
					toDrop = new Item(itemId, (byte) 0, (short) quantity, (byte) 0);
				}
				toDrop.setGMLog(c.getPlayer().getName());
				c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
			}
		} else if (splitted[0].equalsIgnoreCase("!saveall")) {
			for (ChannelServer chan : ChannelServer.getAllInstances()) {
				for (MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
					chr.saveToDB(false, false);
				}
			}
			c.getPlayer().dropMessage(6, "Done.");
		} else if (splitted[0].equalsIgnoreCase("!maxmesos")) {
			c.getPlayer().gainMeso(Integer.MAX_VALUE - c.getPlayer().getMeso(), true);
		} else if (splitted[0].equalsIgnoreCase("!vac")) {
			if (!c.getPlayer().isHidden()) {
				c.getPlayer().dropMessage(6, "You can only vac monsters while in hide.");
			} else {
				for (final MapleMapObject mmo : c.getPlayer().getMap().getAllMonster()) {
					final MapleMonster monster = (MapleMonster) mmo;
					c.getPlayer().getMap().broadcastMessage(MobPacket.moveMonster(false, -1, 0, 0, 0, 0, monster.getObjectId(), monster.getPosition(), c.getPlayer().getLastRes()));
					monster.setPosition(c.getPlayer().getPosition());
				}
			}
		} else if (splitted[0].equalsIgnoreCase("!jobperson")) {
			cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeJob(Integer.parseInt(splitted[1]));
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("item", "", "", 4),
			new CommandDefinition("drop", "", "", 4),
			new CommandDefinition("saveall", "", "", 4),
			new CommandDefinition("maxmesos", "", "", 4),
			new CommandDefinition("vac", "", "", 4),
			new CommandDefinition("jobperson", "", "", 4)
		};
	}
}