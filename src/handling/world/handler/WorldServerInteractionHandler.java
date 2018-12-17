package handling.world.handler;

import client.MapleClient;
import tools.FileoutputUtil;
import tools.data.input.SeekableLittleEndianAccessor;

public class WorldServerInteractionHandler {

	public static final void Error38(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		if (slea.available() >= 6) {
			slea.skip(6);
			short badPacketSize = slea.readShort();
			// skipping cause i don't know what the rest means... slea.skip(2) is always equal to slea.skip(4) ..
			slea.skip(4); // skips all the way to the broken packet
			System.err.println("Packet error detected! Please check Error38.log");
			slea.skip(badPacketSize);
			FileoutputUtil.log(FileoutputUtil.Error38_Log, "Error 38 Detected :" + slea.toString());
		} else {
			System.err.println("ERROR : Detected packet error but unable to handle!!");
		}
	}
}