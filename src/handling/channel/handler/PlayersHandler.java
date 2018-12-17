package handling.channel.handler;

import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleStat;
import client.anticheat.CheatingOffense;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleDoor;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PlayersHandler {

	public static final void Note(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
		final byte type = slea.readByte();

		switch (type) {
			case 1:
				final byte num = slea.readByte();
				slea.skip(2);

				for (int i = 0; i < num; i++) {
					final int id = slea.readInt();
					slea.skip(1);
					chr.deleteNote(id);
				}
				break;
			default:
				System.out.println("Unhandled note action, " + type + "");
		}
	}

	public static final void GiveFame(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final int who = slea.readInt();
		final int mode = slea.readByte();

		final int famechange = mode == 0 ? -1 : 1;
		final MapleCharacter target = (MapleCharacter) chr.getMap().getMapObject(who);

		if (target == chr) { // faming self
			chr.getCheatTracker().registerOffense(CheatingOffense.FAMING_SELF);
			return;
		} else if (chr.getLevel() < 15) {
			chr.getCheatTracker().registerOffense(CheatingOffense.FAMING_UNDER_15);
			return;
		}
		switch (chr.canGiveFame(target)) {
			case OK:
				if (Math.abs(target.getFame() + famechange) <= 30000) {
					target.addFame(famechange);
					target.updateSingleStat(MapleStat.FAME, target.getFame());
				}
				if (!chr.isGM()) {
					chr.hasGivenFame(target);
				}
				c.getSession().write(MaplePacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
				target.getClient().getSession().write(MaplePacketCreator.receiveFame(mode, chr.getName()));
				break;
			case NOT_TODAY:
				c.getSession().write(MaplePacketCreator.giveFameErrorResponse(3));
				break;
			case NOT_THIS_MONTH:
				c.getSession().write(MaplePacketCreator.giveFameErrorResponse(4));
				break;
		}
	}

	public static final void UseDoor(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
		final int oid = slea.readInt();
		final boolean mode = slea.readByte() == 0; // specifies if backwarp or not, 1 town to target, 0 target to town

		for (MapleMapObject obj : chr.getMap().getAllDoor()) {
			final MapleDoor door = (MapleDoor) obj;
			if (door.getOwner().getId() == oid) {
				door.warp(chr, mode);
				break;
			}
		}
	}

	public static final void TransformPlayer(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		// D9 A4 FD 00
		// 11 00
		// A0 C0 21 00
		// 07 00 64 66 62 64 66 62 64
		slea.skip(4); // Timestamp
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final String target = slea.readMapleAsciiString().toLowerCase();

		final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		switch (itemId) {
			case 2212000:
				for (final MapleCharacter search_chr : c.getPlayer().getMap().getCharacters()) {
					if (search_chr.getName().toLowerCase().equals(target)) {
						MapleItemInformationProvider.getInstance().getItemEffect(2210023).applyTo(search_chr);
						search_chr.dropMessage(6, chr.getName() + " has played a prank on you!");
						MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
					}
				}
				break;
		}
	}

	public static final void HitReactor(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		final int oid = slea.readInt();
		final int charPos = slea.readInt();
		final short stance = slea.readShort();
		final MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);

		if (reactor == null || !reactor.isAlive()) {
			return;
		}
		reactor.hitReactor(charPos, stance, c);
	}
}