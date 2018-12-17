package client.messages;

import client.MapleClient;

public interface Command {
	CommandDefinition[] getDefinition();
	void execute(final MapleClient c, final String[] splittedLine) throws Exception, IllegalCommandSyntaxException;
}