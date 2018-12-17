package client.messages;

public class CommandDefinition {

	private String command;
	private String parameterDescription;
	private String help;
	private int requiredLevel; // gm level

	public CommandDefinition(String command, String parameterDescription, String help, int requiredLevel) {
		this.command = command;
		this.help = help;
		this.parameterDescription = parameterDescription;
		this.requiredLevel = requiredLevel;
	}

	public String getCommand() {
		return command;
	}

	public String getHelp() {
		return help;
	}

	public String getParameterDescription() {
		return parameterDescription;
	}

	public int getRequiredLevel() {
		return requiredLevel;
	}
}