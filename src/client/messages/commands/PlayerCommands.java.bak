package net.sf.odinms.client.messages.commands;

import java.rmi.RemoteException;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.StringUtil;

public class PlayerCommands implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        splitted[0] = splitted[0].toLowerCase();
        MapleCharacter player = c.getPlayer();
        if (splitted[0].equals("@command") || splitted[0].equals("@commands") || splitted[0].equals("@help")) {
            mc.dropMessage("================================================================");
            mc.dropMessage("                  " + c.getChannelServer().getServerName() + " Commands");
            mc.dropMessage("================================================================");
            mc.dropMessage("@checkstat - | - Displays your stats.");
            mc.dropMessage("@save - | - Saves your progress.");
            mc.dropMessage("@expfix - | - Fixes your negative experience.");
            mc.dropMessage("@dispose - | - Unstucks you.");
            mc.dropMessage("@emo - | - Sets your HP zero.");
            mc.dropMessage("@rebirth - | - Resets your HP/MP and sets your level to 1 to be stronger.");
            mc.dropMessage("@togglesmega - | - Turn smegas OFF/ON.");
            mc.dropMessage("@str/@dex/@int/@luk <number> - | - Automatically add AP to your stats.");
            mc.dropMessage("@gm <message> - | - Sends a message to the GM's online.");
            mc.dropMessage("@revive - | - Revives anyone on the channel besides yourself.");
            mc.dropMessage("@afk - | - Shows how long a perosn has been AFK.");
            mc.dropMessage("@onlinetime - | - Shows how long a person has been online.");
            if (player.getClient().getChannelServer().extraCommands()) {
                mc.dropMessage("@cody/@storage/@news/@kin/@nimakin/@reward/@reward1/@fredrick/@spinel/@clan");
                mc.dropMessage("@banme - | - This command will ban you, SGM's will not unban you from this.");
                mc.dropMessage("@goafk - | - Uses a CB to say that you are AFK.");
                mc.dropMessage("@slime - | - For a small cost, it summons smiles for you.");
                mc.dropMessage("@go - | - Takes you to many towns and fighting areas.");
                mc.dropMessage("@buynx - | - You can purchase NX with this command.");
            }
        } else if (splitted[0].equals("@checkstats")) {
            mc.dropMessage("Your stats are:");
            mc.dropMessage("Str: " + player.getStr());
            mc.dropMessage("Dex: " + player.getDex());
            mc.dropMessage("Int: " + player.getInt());
            mc.dropMessage("Luk: " + player.getLuk());
            mc.dropMessage("Available AP: " + player.getRemainingAp());
            mc.dropMessage("Rebirths: " + player.getReborns());
        } else if (splitted[0].equals("@save")) {
            if (!player.getCheatTracker().Spam(900000, 0)) { // 15 minutes
                player.saveToDB(true, true);
                mc.dropMessage("Saved.");
            } else {
                mc.dropMessage("You cannot save more than once every 15 minutes.");
            }
        } else if (splitted[0].equals("@expfix")) {
            player.setExp(0);
            player.updateSingleStat(MapleStat.EXP, player.getExp());
        } else if (splitted[0].equals("@dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            mc.dropMessage("You have been disposed.");
        } else if (splitted[0].equals("@emo")) {
            player.setHp(0);
            player.updateSingleStat(MapleStat.HP, 0);
        } else if (splitted[0].equals("@rebirth") || splitted[0].equals("@reborn")) {
            if (player.getLevel() >= 200) {
                player.doReborn();
            } else {
                mc.dropMessage("You must be at least level 200.");
            }
        } else if (splitted[0].equals("@togglesmega")) {
            if (player.getMeso() >= 10000000) {
                player.setSmegaEnabled(!player.getSmegaEnabled());
                String text = (!player.getSmegaEnabled() ? "[Disable] Smegas are now disable." : "[Enable] Smegas are now enable.");
                mc.dropMessage(text);
                player.gainMeso(-10000000, true);
            } else {
                mc.dropMessage("You need 10,000,000 mesos to toggle smegas.");
            }
        } else if (splitted[0].equals("@str") || splitted[0].equals("@dex") || splitted[0].equals("@int") || splitted[0].equals("@luk") || splitted[0].equals("@hp") || splitted[0].equals("@mp")) {
            if (splitted.length != 2) {
                mc.dropMessage("Syntax: @<Stat> <amount>");
                mc.dropMessage("Stat: <STR> <DEX> <INT> <LUK> <HP> <MP>");
                return;
            }
            int x = Integer.parseInt(splitted[1]), max = 30000;
            if (x > 0 && x <= player.getRemainingAp() && x < Short.MAX_VALUE) {
                if (splitted[0].equals("@str") && x + player.getStr() < max) {
                    player.addAP(c, 1, x);
                } else if (splitted[0].equals("@dex") && x + player.getDex() < max) {
                    player.addAP(c, 2, x);
                } else if (splitted[0].equals("@int") && x + player.getInt() < max) {
                    player.addAP(c, 3, x);
                } else if (splitted[0].equals("@luk") && x + player.getLuk() < max) {
                    player.addAP(c, 4, x);
                } else if (splitted[0].equals("@hp") && x + player.getMaxHp() < max) {
                    player.addAP(c, 5, x);
                } else if (splitted[0].equals("@mp") && x + player.getMaxMp() < max) {
                    player.addAP(c, 6, x);
                } else {
                    mc.dropMessage("Make sure the stat you are trying to raise will not be over " + Short.MAX_VALUE + ".");
                }
            } else {
                mc.dropMessage("Please make sure your AP is valid.");
            }
        } else if (splitted[0].equals("@gm")) {
            if (splitted.length < 2) {
                return;
            }
            if (!player.getCheatTracker().Spam(300000, 1)) { // 5 minutes.
                try {
                    c.getChannelServer().getWorldInterface().broadcastGMMessage(null, MaplePacketCreator.serverNotice(6, "Channel: " + c.getChannel() + "  " + player.getName() + ": " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                } catch (RemoteException ex) {
                    c.getChannelServer().reconnectWorld();
                }
                mc.dropMessage("Message sent.");
            } else {
                player.dropMessage(1, "Please don't flood GMs with your messages.");
            }
        } else if (splitted[0].equals("@revive")) {
            if (splitted.length == 2) {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (player != victim) {
                    if (player.getMeso() >= 50000000) { // 50 mil
                        if (victim != null) {
                            if (!victim.isAlive()) {
                                victim.setHp((victim.getMaxHp() / 2));
                                player.gainMeso(-50000000);
                                victim.updateSingleStat(MapleStat.HP, (victim.getMaxHp() / 2));
                                mc.dropMessage("You have revived " + victim.getName() + ".");
                            } else {
                                mc.dropMessage(victim.getName() + " is not dead.");
                            }
                        } else {
                            mc.dropMessage("The player is not online.");
                        }
                    } else {
                        mc.dropMessage("You need 50 million mesos to do this.");
                    }
                } else {
                    mc.dropMessage("You can't revive yourself.");
                }
            } else {
                mc.dropMessage("Syntax: @revive <player name>");
            }
        } else if (splitted[0].equals("@afk")) {
            if (splitted.length >= 2) {
                String name = splitted[1];
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
                if (victim == null) {
                    try {
                        WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
                        int channel = wci.find(name);
                        if (channel == -1 || victim.isGM()) {
                            mc.dropMessage("This player is not online.");
                            return;
                        }
                        victim = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(name);
                    } catch (RemoteException re) {
                        c.getChannelServer().reconnectWorld();
                    }
                }
                long blahblah = System.currentTimeMillis() - victim.getAfkTime();
                if (Math.floor(blahblah / 60000) == 0) { // less than a minute
                    mc.dropMessage("Player has not been afk!");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(victim.getName());
                    sb.append(" has been afk for");
                    compareTime(sb, blahblah);
                    mc.dropMessage(sb.toString());
                }
            } else {
                mc.dropMessage("Incorrect Syntax.");
            }
        } else if (splitted[0].equals("@onlinetime")) {
            if (splitted.length >= 2) {
                String name = splitted[1];
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
                if (victim == null) {
                    try {
                        WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
                        int channel = wci.find(name);
                        if (channel == -1 || victim.isGM()) {
                            mc.dropMessage("This player is not online.");
                            return;
                        }
                        victim = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(name);
                    } catch (RemoteException re) {
                        c.getChannelServer().reconnectWorld();
                    }
                }
                long blahblah = System.currentTimeMillis() - victim.getLastLogin();
                StringBuilder sb = new StringBuilder();
                sb.append(victim.getName());
                sb.append(" has been online for");
                compareTime(sb, blahblah);
                mc.dropMessage(sb.toString());
            } else {
                mc.dropMessage("Incorrect Syntax.");
            }
        }
    }

    private void compareTime(StringBuilder sb, long timeDiff) {
        double secondsAway = timeDiff / 1000;
        double minutesAway = 0;
        double hoursAway = 0;

        while (secondsAway > 60) {
            minutesAway++;
            secondsAway -= 60;
        }
        while (minutesAway > 60) {
            hoursAway++;
            minutesAway -= 60;
        }
        boolean hours = false;
        boolean minutes = false;
        if (hoursAway > 0) {
            sb.append(" ");
            sb.append((int) hoursAway);
            sb.append(" hours");
            hours = true;
        }
        if (minutesAway > 0) {
            if (hours) {
                sb.append(" -");
            }
            sb.append(" ");
            sb.append((int) minutesAway);
            sb.append(" minutes");
            minutes = true;
        }
        if (secondsAway > 0) {
            if (minutes) {
                sb.append(" and");
            }
            sb.append(" ");
            sb.append((int) secondsAway);
            sb.append(" seconds !");
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
            new CommandDefinition("command", 0),
            new CommandDefinition("commands", 0),
            new CommandDefinition("help", 0),
            new CommandDefinition("checkstats", 0),
            new CommandDefinition("save", 0),
            new CommandDefinition("expfix", 0),
            new CommandDefinition("dispose", 0),
            new CommandDefinition("emo", 0),
            new CommandDefinition("rebirth", 0),
            new CommandDefinition("reborn", 0),
            new CommandDefinition("togglesmega", 0),
            new CommandDefinition("str", 0),
            new CommandDefinition("dex", 0),
            new CommandDefinition("int", 0),
            new CommandDefinition("luk", 0),
            new CommandDefinition("hp", 0),
            new CommandDefinition("mp", 0),
            new CommandDefinition("gm", 0),
            new CommandDefinition("revive", 0),
            new CommandDefinition("afk", 0),
            new CommandDefinition("onlinetime", 0)
        };
    }
}