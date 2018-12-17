package net.sf.odinms.client.messages.commands;

import java.util.HashMap;
import java.util.Map;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.MessageCallback;

public class Donator implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        splitted[0] = splitted[0].toLowerCase();
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equals("!buffme")) {
            int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
            for (int i = 0; i < array.length; i++) {
                SkillFactory.getSkill(array[i]).getEffect(SkillFactory.getSkill(array[i]).getMaxLevel()).applyTo(player);
            }
        } else if (splitted[0].equals("!goto")) {
            Map<String, Integer> maps = new HashMap<String, Integer>();
            maps.put("gmmap", 180000000);
            maps.put("southperry", 60000);
            maps.put("amherst", 1010000);
            maps.put("henesys", 100000000);
            maps.put("ellinia", 101000000);
            maps.put("perion", 102000000);
            maps.put("kerning", 103000000);
            maps.put("lith", 104000000);
            maps.put("sleepywood", 105040300);
            maps.put("florina", 110000000);
            maps.put("orbis", 200000000);
            maps.put("happy", 209000000);
            maps.put("elnath", 211000000);
            maps.put("ludi", 220000000);
            maps.put("aqua", 230000000);
            maps.put("leafre", 240000000);
            maps.put("mulung", 250000000);
            maps.put("herb", 251000000);
            maps.put("omega", 221000000);
            maps.put("korean", 222000000);
            maps.put("nlc", 600000000);
            maps.put("excavation", 990000000);
            maps.put("pianus", 230040420);
            maps.put("horntail", 240060200);
            maps.put("mushmom", 100000005);
            maps.put("griffey", 240020101);
            maps.put("manon", 240020401);
            maps.put("headless", 682000001);
            maps.put("balrog", 105090900);
            maps.put("zakum", 280030000);
            maps.put("papu", 220080001);
            maps.put("showa", 801000000);
            maps.put("guild", 200000301);
            maps.put("shrine", 800000000);
            maps.put("fm", 910000000);
            maps.put("skelegon", 240040511);
            maps.put("ariant", 260000100);
            if (splitted.length < 2) {
                mc.dropMessage("Syntax: !goto <mapname> <optional_target>, where target is char name and mapname is one of:");
                StringBuilder builder = new StringBuilder();
                int i = 0;
                for (String mapss : maps.keySet()) {
                    if (1 % 10 == 0) {
                        mc.dropMessage(builder.toString());
                    } else {
                        builder.append(mapss + ", ");
                    }
                }
                mc.dropMessage(builder.toString());
            } else {
                String message = splitted[1];
                if (maps.containsKey(message)) {
                    if (splitted.length == 2) {
                        player.changeMap(maps.get(message));
                    } else if (splitted.length == 3) {
                        MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
                        victim.changeMap(maps.get(message));
                    }
                } else {
                    mc.dropMessage("Could not find map");
                }
            }
            maps.clear();
        } else if (splitted[0].equals("!sexchange")) {
            if (player.getGender() == 1){
                player.setGender(0);
            } else {
                player.setGender(1);
            }
            mc.dropMessage(player.getName() + " sex change!");
        } else if (splitted[0].equals("!storage")) {
            c.getPlayer().getStorage().sendStorage(c, 2080005);
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
            new CommandDefinition("buffme", 1),
            new CommandDefinition("goto", 1),
            new CommandDefinition("sexchange", 1),
            new CommandDefinition("storage", 1)
        };
    }
}