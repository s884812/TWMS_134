package client.messages.commands;

import client.ISkill;
import static client.messages.CommandProcessor.getOptionalIntArg;
import client.MapleClient;
import client.MapleStat;
import client.SkillFactory;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;

public class GM2Commands implements Command {

	@Override
	public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		if (splitted[0].equalsIgnoreCase("!online")) {
			c.getPlayer().dropMessage(6, "Characters connected to channel " + c.getChannel() + ":");
			c.getPlayer().dropMessage(6, c.getChannelServer().getPlayerStorage().getOnlinePlayers(true));
		} else if (splitted[0].equalsIgnoreCase("!job")) {
			c.getPlayer().changeJob(Integer.parseInt(splitted[1]));
		} else if (splitted[0].equalsIgnoreCase("!ap")) {
			c.getPlayer().setRemainingAp(getOptionalIntArg(splitted, 1, 1));
			c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
		} else if (splitted[0].equalsIgnoreCase("!sp")) {
			c.getPlayer().setRemainingSp(getOptionalIntArg(splitted, 1, 1));
			c.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, c.getPlayer().getRemainingSp());
		} else if (splitted[0].equalsIgnoreCase("!skill")) {
			ISkill skill = SkillFactory.getSkill(Integer.parseInt(splitted[1]));
			byte level = (byte) getOptionalIntArg(splitted, 2, 1);
			byte masterlevel = (byte) getOptionalIntArg(splitted, 3, 1);
			if (level > skill.getMaxLevel()) {
				level = skill.getMaxLevel();
			}
			c.getPlayer().changeSkillLevel(skill, level, masterlevel);
		} else if (splitted[0].equalsIgnoreCase("!heal")) {
			c.getPlayer().getStat().setHp(c.getPlayer().getStat().getMaxHp());
			c.getPlayer().getStat().setMp(c.getPlayer().getStat().getMaxMp());
			c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getStat().getMaxHp());
			c.getPlayer().updateSingleStat(MapleStat.MP, c.getPlayer().getStat().getMaxMp());
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("online", "check players online", "check players online", 2),
			new CommandDefinition("job", "", "", 2),
			new CommandDefinition("ap", "", "", 2),
			new CommandDefinition("sp", "", "", 2),
			new CommandDefinition("skill", "", "", 2),
			new CommandDefinition("heal", "", "", 2)
		};
	}
}