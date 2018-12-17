package client.messages.commands;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.CommandProcessor;
import client.messages.IllegalCommandSyntaxException;

public class HelpCommand implements Command {

	@Override
	public void execute(MapleClient c, String[] splittedLine) throws Exception, IllegalCommandSyntaxException {
		CommandProcessor.getInstance().dropHelp(c.getPlayer(), CommandProcessor.getOptionalIntArg(splittedLine, 1, 1));
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("help", "[page - defaults to 1]", "Shows the help", 1)
		};
	}
}