package client.messages.commands;

import static client.messages.CommandProcessor.getOptionalIntArg;
import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.packet.*;

public class TestCommands implements Command {

	@Override
	public void execute(final MapleClient c, final String[] splitted) throws Exception, IllegalCommandSyntaxException {
		if (splitted[0].equalsIgnoreCase("!test")) {
			c.getSession().write(MTSCSPacket.warpCS(c));
		} else if (splitted[0].equalsIgnoreCase("!clock")) {
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getClock(getOptionalIntArg(splitted, 1, 60)));
		} else if (splitted[0].equalsIgnoreCase("!packet")) {
			if (splitted.length > 1) {
				c.getSession().write(MaplePacketCreator.getPacketFromHexString(StringUtil.joinStringFrom(splitted, 1)));
			} else {
				c.getPlayer().dropMessage(6, "Please enter packet data!");
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("test", "?", "Probably does something", 5),
			new CommandDefinition("clock", "[time]", "Shows a clock to everyone in the map", 5),
			new CommandDefinition("packet", "[hex byte]", "Write the input byte(s) as output", 5)
		};
	}
}