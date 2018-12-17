package handling.channel.handler;

import tools.data.input.SeekableLittleEndianAccessor;

public class FamilyHandler {

	public static final void RequestFamily(final SeekableLittleEndianAccessor slea) {
		final String reqName = slea.readMapleAsciiString();
	}
}