package handling.channel.handler;

import client.MapleClient;
import handling.world.guild.MapleAlliance;
import tools.data.input.SeekableLittleEndianAccessor;

public class AllianceHandler {

    public static final void AllianceOperatopn(final SeekableLittleEndianAccessor slea, final MapleClient c) {
	final byte mode = slea.readByte();

	final MapleAlliance alliance = new MapleAlliance(c, c.getChannelServer().getGuildSummary(c.getPlayer().getGuildId()).getAllianceId());

	switch (mode) {
	    case 0x01: // show info?
		//c.getSession().write(MaplePacketCreator.showAllianceInfo(c.getPlayer()));
		//c.getSession().write(MaplePacketCreator.showAllianceMembers(c.getPlayer()));
		break;
	    case 0x08: // change titles
		String[] ranks = new String[5];
		for (int i = 0; i < 5; i++) {
		    ranks[i] = slea.readMapleAsciiString();
		}
		alliance.setTitles(ranks);
		break;
	    case 0x0A: // change notice
		String notice = slea.readMapleAsciiString(); // new notice (100 is de max)
		alliance.setNotice(notice);
		break;
	    default:
		System.out.println("Unknown Alliance operation:\r\n" + slea.toString());
		break;
	}
    }
}
