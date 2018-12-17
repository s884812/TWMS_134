package tools.packet;

import java.util.List;

import client.MaplePet;
import client.MapleStat;
import client.MapleCharacter;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import server.movement.LifeMovementFragment;
import tools.HexTool;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PetPacket {
	private final static byte[] ITEM_MAGIC = new byte[]{(byte) 0x80, 5};

	public static final MaplePacket updatePet(final MaplePet pet, final boolean alive) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(0);
		mplew.write(2);
		mplew.write(3);
		mplew.write(5);
		mplew.write(pet.getInventoryPosition());
		mplew.writeShort(0);
		mplew.write(5);
		mplew.write(pet.getInventoryPosition());
		mplew.write(0);
		mplew.write(3);
		mplew.writeInt(pet.getPetItemId());
		mplew.write(1);
		mplew.writeInt(pet.getUniqueId());
		mplew.writeInt(0);
		mplew.write(HexTool.getByteArrayFromHexString("00 80 F9 58 8D 3B C7 24"));
		mplew.writeAsciiString(pet.getName() , 13);
		mplew.write(pet.getLevel());
		mplew.writeShort(pet.getCloseness());
		mplew.write(pet.getFullness());
		if (alive) {
			mplew.writeLong(PacketHelper.getKoreanTimestamp(System.currentTimeMillis()));
		} else {
			mplew.write(0);
			mplew.write(ITEM_MAGIC);
			mplew.write(HexTool.getByteArrayFromHexString("bb 46 e6 17 02"));
		}
		mplew.writeShort(0);
		mplew.writeInt(1);
		mplew.writeInt(0);
		mplew.writeZeroBytes(5);
		return mplew.getPacket();
	}

	public static final MaplePacket showPet(final MapleCharacter chr, final MaplePet pet, final boolean remove, final boolean hunger) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getPetIndex(pet));
		if (remove) {
			mplew.write(0);
			mplew.write(hunger ? 1 : 0);
		} else {
			mplew.write(1);
			mplew.write(1);
			mplew.writeInt(pet.getPetItemId());
			mplew.writeMapleAsciiString(pet.getName());
			mplew.writeInt(pet.getUniqueId());
			mplew.writeInt(0);
			mplew.writeShort(pet.getPos().x);
			mplew.writeShort(pet.getPos().y - 20);
			mplew.write(pet.getStance());
			mplew.writeInt(pet.getFh());
		}
		return mplew.getPacket();
	}

	public static final MaplePacket movePet(final int cid, final int pid, final byte slot, final List<LifeMovementFragment> moves) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_PET.getValue());
		mplew.writeInt(cid);
		mplew.write(slot);
		mplew.writeLong(pid);
		PacketHelper.serializeMovementList(mplew, moves);

		return mplew.getPacket();
	}

	public static final MaplePacket petChat(final int cid, final int un, final String text, final byte slot) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PET_CHAT.getValue());
		mplew.writeInt(cid);
		mplew.write(slot);
		mplew.write(0);
		mplew.write(un);
		mplew.writeMapleAsciiString(text);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static final MaplePacket commandResponse(final int cid, final byte command, final byte slot, final boolean success, final boolean food) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PET_COMMAND.getValue());
		mplew.writeInt(cid);
		mplew.write(slot);
		mplew.write(command == 1 ? 1 : 0);
		mplew.write(command);
		if (command == 1) {
			mplew.write(0);
		} else {
			mplew.writeShort(success ? 1 : 0);
		}

		return mplew.getPacket();
	}

	public static final MaplePacket showOwnPetLevelUp(final byte index) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(4);
		mplew.write(0);
		mplew.write(index); // Pet Index

		return mplew.getPacket();
	}

	public static final MaplePacket showPetLevelUp(final MapleCharacter chr, final byte index) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(4);
		mplew.write(0);
		mplew.write(index);

		return mplew.getPacket();
	}

	public static final MaplePacket emptyStatUpdate() {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
		mplew.write(1);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static final MaplePacket petStatUpdate(final MapleCharacter chr) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
		mplew.write(0);

		int mask = 0;
		mask |= MapleStat.PET.getValue();
		mplew.writeInt(mask);

		byte count = 0;
		for (final MaplePet pet : chr.getPets()) {
			if (pet.getSummoned()) {
				mplew.writeInt(pet.getUniqueId());
				mplew.writeZeroBytes(4);
				count++;
			}
		}
		while (count < 3) {
			mplew.writeZeroBytes(8);
			count++;
		}
		mplew.write(0);

		return mplew.getPacket();
	}

	public static final MaplePacket weirdStatUpdate(final MaplePet pet) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
		mplew.write(0);
		mplew.write(8);
		mplew.write(0);
		mplew.write(0x18);
		mplew.write(0);
		mplew.writeInt(pet.getUniqueId());
		mplew.writeLong(0);
		mplew.writeLong(0);
		mplew.writeInt(0);
		mplew.write(1);

		return mplew.getPacket();
	}
}