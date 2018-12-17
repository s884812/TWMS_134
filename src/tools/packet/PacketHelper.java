package tools.packet;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import client.IEquip;
import client.Item;
import client.ISkill;
import client.GameConstants;
import client.MapleRing;
import client.MaplePet;
import client.MapleCharacter;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleQuestStatus;
import client.IItem;
import client.SkillEntry;
import handling.world.PlayerCoolDownValueHolder;
import server.movement.LifeMovementFragment;
import tools.HexTool;
import tools.KoreanDateUtil;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.StringUtil;

public class PacketHelper {

	private final static long FT_UT_OFFSET = 116444592000000000L; // EDT
	public static final byte unk1[] = new byte[]{(byte) 0x00, (byte) 0x40, (byte) 0xE0, (byte) 0xFD};
	public static final byte unk2[] = new byte[]{(byte) 0x3B, (byte) 0x37, (byte) 0x4F, (byte) 0x01};

	public static final long getKoreanTimestamp(final long realTimestamp) {
		long time = (realTimestamp / 1000 / 60); // convert to minutes
		return ((time * 600000000) + FT_UT_OFFSET);
	}

	public static final long getTime(final long realTimestamp) {
		long time = (realTimestamp / 1000); // convert to seconds
		return ((time * 10000000) + FT_UT_OFFSET);
	}

	public static void addQuestInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
		final List<MapleQuestStatus> started = chr.getStartedQuests();
		mplew.writeShort(started.size());
		for (final MapleQuestStatus q : started) {
			mplew.writeShort(q.getQuest().getId());
			mplew.writeMapleAsciiString(q.getCustomData() != null ? q.getCustomData() : "");
		}
		final List<MapleQuestStatus> completed = chr.getCompletedQuests();
		int time;
		mplew.writeShort(completed.size());
		for (final MapleQuestStatus q : completed) {
			mplew.writeShort(q.getQuest().getId());
                        mplew.writeShort(0);
			time = KoreanDateUtil.getQuestTimestamp(q.getCompletionTime());
			mplew.writeInt(time); // maybe start time? no effect.
			mplew.write(HexTool.getByteArrayFromHexString("07 47 CC 01")); // completion time
		}
	}

	public static final void addSkillInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
		final Map<ISkill, SkillEntry> skills = chr.getSkills();
		mplew.writeShort(skills.size());
		for (final Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
			mplew.writeInt(skill.getKey().getId());
			mplew.writeInt(skill.getValue().skillevel);
			mplew.write(0);
			addExpirationTime(mplew, -1);
			if (skill.getKey().isFourthJob()) {
				mplew.writeInt(skill.getValue().masterlevel);
			}
		}
	}

	public static final void addCoolDownInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
		mplew.writeShort(chr.getAllCooldowns().size());
		for (final PlayerCoolDownValueHolder cooling : chr.getAllCooldowns()) {
			mplew.writeInt(cooling.skillId);
			mplew.writeShort((int) (cooling.length + cooling.startTime - System.currentTimeMillis()) / 1000);
		}
	}

	public static final void addRocksInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
		for (int i = 0; i < 18; i++) { // Teleport maps (TODO)
                        mplew.writeInt(999999999); 
                }
		final int[] map = chr.getRocks();
		for (int i = 0; i < 10; i++) { // VIP teleport map
			mplew.writeInt(map[i]);
		}
	}

	public static final void addMonsterBookInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
		//mplew.writeInt(chr.getMonsterBookCover());
		//mplew.write(0);
		chr.getMonsterBook().addCardPacket(mplew);
	}

	public static final void addRingInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
		List<MapleRing> rings = new ArrayList<MapleRing>();
		MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
		for (final IItem item : iv.list()) {
			if (((IEquip) item).getRingId() > -1) {
				rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
			}
		}
		iv = chr.getInventory(MapleInventoryType.EQUIP);
		for (final IItem item : iv.list()) {
			if (((IEquip) item).getRingId() > -1) {
				rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
			}
		}
		Collections.sort(rings);
		boolean FR_last = false;
		for (final MapleRing ring : rings) {
			if ((ring.getItemId() >= 1112800 && ring.getItemId() <= 1112803 || ring.getItemId() <= 1112806 || ring.getItemId() <= 1112807 || ring.getItemId() <= 1112809) && rings.indexOf(ring) == 0) {
				mplew.writeShort(0);
			}
			mplew.writeShort(0);
			mplew.writeShort(1);
			mplew.writeInt(ring.getPartnerChrId());
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(ring.getPartnerName(), '\0', 15));
			mplew.writeInt(ring.getRingId());
			mplew.writeInt(0);
			mplew.writeInt(ring.getPartnerRingId());
			if (ring.getItemId() >= 1112800 && ring.getItemId() <= 1112803 || ring.getItemId() <= 1112806 || ring.getItemId() <= 1112807 || ring.getItemId() <= 1112809) {
				FR_last = true;
				mplew.writeInt(0);
				mplew.writeInt(ring.getItemId());
				mplew.writeShort(0);
			} else {
				if (rings.size() > 1) {
					mplew.writeShort(0);
				}
				FR_last = false;
			}
		}
		if (!FR_last) { // no ring
			mplew.writeLong(0);
		}
	}

	public static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.writeInt(chr.getMeso());
                mplew.writeInt(chr.getId());
                mplew.writeLong(0);
		mplew.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
		mplew.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit());
		mplew.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit());
		mplew.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit());
		mplew.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit());
		mplew.write(unk1);
		mplew.write(unk2);
		MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
		Collection<IItem> equippedC = iv.list();
		List<Item> equipped = new ArrayList<Item>(equippedC.size());
		for (IItem item : equippedC) {
			equipped.add((Item) item);
		}
		Collections.sort(equipped);
		for (Item item : equipped) { // equipped item
			if (item.getPosition() < 0 && item.getPosition() > -100) {
				addItemInfo(mplew, item, false, false);
			}
		}
		mplew.writeShort(0);
		for (Item item : equipped) { // equipped nx
			if (item.getPosition() < -100) {
				addItemInfo(mplew, item, false, false);
			}
		}
		mplew.writeShort(0); // v133
		iv = chr.getInventory(MapleInventoryType.EQUIP);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item, false, false);
		}
                mplew.writeShort(0);
		mplew.writeInt(0);
		iv = chr.getInventory(MapleInventoryType.USE);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item, false, false);
		}
		mplew.writeShort(0);
		iv = chr.getInventory(MapleInventoryType.SETUP);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item, false, false);
		}
		mplew.writeShort(0);
		iv = chr.getInventory(MapleInventoryType.ETC);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item, false, false);
		}
                mplew.writeShort(0);
                mplew.writeInt(-1);
                mplew.writeInt(0);
		iv = chr.getInventory(MapleInventoryType.CASH);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item, false, false);
		}
		mplew.write(0);
	}

	public static final void addCharStats(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
		mplew.writeInt(chr.getId()); // character id
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 15));
		mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
		mplew.write(chr.getSkinColor()); // skin color
		mplew.writeInt(chr.getFace()); // face
		mplew.writeInt(chr.getHair()); // hair
		mplew.write(chr.getLevel()); // level
		mplew.writeShort(chr.getJob()); // job
		chr.getStat().connectData(mplew);
		mplew.writeShort(chr.getRemainingAp()); // remaining ap
		if (GameConstants.isEvan(chr.getJob()) || GameConstants.isResistance(chr.getJob())) {
			final int size = chr.getRemainingSpSize();
			mplew.write(size);
			for (int i = 0; i < chr.getRemainingSps().length; i++) {
				if (chr.getRemainingSp(i) > 0) {
					mplew.write(i + 1);
					mplew.write(chr.getRemainingSp(i));
				}
			}
		} else {
			mplew.writeShort(chr.getRemainingSp()); // remaining sp
		}
		mplew.writeInt(chr.getExp()); // exp
		mplew.writeShort(chr.getFame()); // fame
		mplew.writeInt(0); // Gacha-EXP
                mplew.writeShort(0);
                mplew.write(HexTool.getByteArrayFromHexString("00 40 E0 FD 3B 37 4F 01"));
		mplew.writeInt(chr.getMapId()); // current map id
		mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
		mplew.writeShort(chr.getSubcategory()); // 1 = Dual Blade
                mplew.write(0);
                mplew.write(HexTool.getByteArrayFromHexString("F3 82 DE 77"));
                mplew.writeZeroBytes(36);
                mplew.writeInt(0);
                mplew.write(chr.getScore()); //評分
                mplew.writeInt(0);
                mplew.write(5); //?
                mplew.writeZeroBytes(30);
                
	}

	public static final void addCharLook(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr, final boolean mega) {
		mplew.write(chr.getGender());
		mplew.write(chr.getSkinColor());
		mplew.writeInt(chr.getFace());
                mplew.writeShort(chr.getJob());
		mplew.write(mega ? 0 : 1);
                mplew.writeShort(0);
		mplew.writeInt(chr.getHair());
		final Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
		final Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
		MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
		for (final IItem item : equip.list()) {
			byte pos = (byte) (item.getPosition() * -1);
			if (pos < 100 && myEquip.get(pos) == null) {
				myEquip.put(pos, item.getItemId());
			} else if (pos > 100 && pos != 111) {
				pos -= 100;
				if (myEquip.get(pos) != null) {
					maskedEquip.put(pos, myEquip.get(pos));
				}
				myEquip.put(pos, item.getItemId());
			} else if (myEquip.get(pos) != null) {
				maskedEquip.put(pos, item.getItemId());
			}
		}
		for (final Entry<Byte, Integer> entry : myEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF); // end of visible itens
		// masked itens
		for (final Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF); // ending markers
		final IItem cWeapon = equip.getItem((byte) -111);
		mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
		mplew.writeInt(0);
		mplew.writeLong(0);
	}

	public static final void addExpirationTime(final MaplePacketLittleEndianWriter mplew, final long time) {
		mplew.writeShort(1408); // 80 05
		if (time != -1) {
			mplew.writeInt(KoreanDateUtil.getItemTimestamp(time));
			mplew.write(1);
		} else {
			mplew.writeInt(400967355); // BB 46 E6 17
			mplew.write(2);
		}
	}

	public static final void addItemInfo(final MaplePacketLittleEndianWriter mplew, final IItem item, final boolean zeroPosition, final boolean leaveOut) {
		addItemInfo(mplew, item, zeroPosition, leaveOut, false);
	}

	public static final void addItemInfo(final MaplePacketLittleEndianWriter mplew, final IItem item, final boolean zeroPosition, final boolean leaveOut, final boolean trade) {
		short pos = item.getPosition();
		if (zeroPosition) {
			if (!leaveOut) {
				mplew.write(0);
			}
		} else {
			if (pos <= -1) {
				pos *= -1;
				if (pos > 100) {
					pos -= 100;
				}
			}
			if (!trade && item.getType() == 1) {
				mplew.writeShort(pos);
			} else {
				mplew.write(pos);
			}
		}
		mplew.write(item.getPet() != null ? 3 : item.getType());
		mplew.writeInt(item.getItemId());
		if (item.getPet() != null) {
			final MaplePet pet = item.getPet();
			mplew.write(1);
			mplew.writeInt(pet.getUniqueId());
			mplew.writeZeroBytes(5);
			addExpirationTime(mplew, item.getExpiration());
			mplew.writeAsciiString(pet.getName(), 13);
			mplew.write(pet.getLevel());
			mplew.writeShort(pet.getCloseness());
			mplew.write(pet.getFullness());
			mplew.writeLong(getTime(System.currentTimeMillis()));
			mplew.writeShort(0);
			mplew.writeInt(1);
			mplew.writeInt(0);
			mplew.writeZeroBytes(5);
		} else {
			mplew.writeShort(0);
			addExpirationTime(mplew, item.getExpiration());
                        mplew.writeInt(-1);
			if (item.getType() == 1) {
				final IEquip equip = (IEquip) item;
				mplew.write(equip.getUpgradeSlots());
				mplew.write(equip.getLevel());
				mplew.writeShort(equip.getStr());
				mplew.writeShort(equip.getDex());
				mplew.writeShort(equip.getInt());
				mplew.writeShort(equip.getLuk());
				mplew.writeShort(equip.getHp());
				mplew.writeShort(equip.getMp());
				mplew.writeShort(equip.getWatk());
				mplew.writeShort(equip.getMatk());
				mplew.writeShort(equip.getWdef());
				mplew.writeShort(equip.getMdef());
				mplew.writeShort(equip.getAcc());
				mplew.writeShort(equip.getAvoid());
				mplew.writeShort(equip.getHands());
				mplew.writeShort(equip.getSpeed());
				mplew.writeShort(equip.getJump());
				mplew.writeMapleAsciiString(equip.getOwner()); // this own 2 bytes, not empty bytes
				mplew.writeShort(equip.getFlag());
				mplew.write(0);
				mplew.write(equip.getItemLevel());
				mplew.writeShort(0);
				mplew.writeShort(equip.getItemEXP());
				mplew.writeInt(-1);
				mplew.writeInt(equip.getViciousHammer());
				mplew.write(equip.getPotential()); // 5 : Rare Item 6 : Epic Item 7 : Unique Item
				mplew.write(equip.getPStars());
				mplew.writeShort(equip.getPotential_1());
				mplew.writeShort(equip.getPotential_2());
				mplew.writeShort(equip.getPotential_3());
				mplew.writeInt(0);
                                mplew.writeShort(0);
				mplew.writeLong(-1);
				mplew.write(unk1);
				mplew.write(unk2);
				mplew.writeInt(-1);
			} else {
				mplew.writeShort(item.getQuantity());
				mplew.writeMapleAsciiString(item.getOwner());
				mplew.writeShort(item.getFlag());
                                
				if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
					mplew.writeInt(2);
					mplew.writeShort(0x54);
					mplew.write(0);
					mplew.write(0x34);
				}
			}
		}
	}

	public static final void serializeMovementList(final LittleEndianWriter lew, final List<LifeMovementFragment> moves) {
		lew.write(moves.size());
		for (LifeMovementFragment move : moves) {
			move.serialize(lew);
		}
	}
}