package handling.channel.handler;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;

import client.Equip;
import client.IEquip;
import client.IEquip.ScrollResult;
import client.IItem;
import client.ISkill;
import client.ItemFlag;
import client.MaplePet;
import client.MapleMount;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleInventory;
import client.MapleStat;
import client.PlayerStats;
import client.GameConstants;
import client.SkillFactory;
import client.anticheat.CheatingOffense;
import handling.world.MaplePartyCharacter;
import server.Randomizer;
import server.RandomRewards;
import server.MapleShopFactory;
import server.MapleItemInformationProvider;
import server.MapleInventoryManipulator;
import server.StructRewardItem;
import server.quest.MapleQuest;
import server.maps.SavedLocationType;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import scripting.NPCScriptManager;
import tools.Pair;
import tools.packet.MTSCSPacket;
import tools.packet.PetPacket;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.MaplePacketCreator;

public class InventoryHandler {

	private static int scrollId;
	private static short itemOption1;
	private static short itemOption2;
	private static short itemOption3 = 0;
	private static byte itemStat;
	IItem item;
	final IEquip equip = (IEquip) this.item;

	public static final void ItemMove(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		slea.skip(4);
		final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
		final byte src = (byte) slea.readShort();
		final byte dst = (byte) slea.readShort();
		final short quantity = slea.readShort();

		if (src < 0 && dst > 0) {
			MapleInventoryManipulator.unequip(c, src, dst);
		} else if (dst < 0) {
			MapleInventoryManipulator.equip(c, src, dst);
		} else if (dst == 0) {
			MapleInventoryManipulator.drop(c, type, src, quantity);
		} else {
			MapleInventoryManipulator.move(c, type, src, dst);
		}
	}

	public static final void ItemSort(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		slea.skip(4);
		byte mode = slea.readByte();
		MapleInventoryType pInvType = MapleInventoryType.getByType(mode);
		MapleInventory pInv = c.getPlayer().getInventory(pInvType);
		boolean sorted = false;
		while (!sorted) {
			byte freeSlot = (byte) pInv.getNextFreeSlot();
			if (freeSlot != -1) {
				byte itemSlot = -1;
				for (byte i = (byte) (freeSlot + 1); i <= 100; i++) {
					if (pInv.getItem(i) != null) {
						itemSlot = i;
						break;
					}
				}
				if (itemSlot <= 100 && itemSlot > 0) {
					MapleInventoryManipulator.move(c, pInvType, itemSlot, freeSlot);
				} else {
                                        c.getSession().write(MaplePacketCreator.getInventoryFull2());
					sorted = true;
				}
			}
		}
		c.getSession().write(MaplePacketCreator.finishedSort(mode));
		c.getSession().write(MaplePacketCreator.enableActions());
	}

        public static final void ItemGather(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		slea.skip(4);
		byte mode = slea.readByte();
		MapleInventoryType pInvType = MapleInventoryType.getByType(mode);
		MapleInventory pInv = c.getPlayer().getInventory(pInvType);
		boolean sorted = false;
		while (!sorted) {
			byte freeSlot = (byte) pInv.getNextFreeSlot();
			if (freeSlot != -1) {
				byte itemSlot = -1;
				for (byte i = (byte) (freeSlot + 1); i <= 100; i++) {
					if (pInv.getItem(i) != null) {
						itemSlot = i;
						break;
					}
				}
				if (itemSlot <= 100 && itemSlot > 0) {
					MapleInventoryManipulator.move(c, pInvType, itemSlot, freeSlot);
				} else {
					c.getSession().write(MaplePacketCreator.getInventoryFull2());
					sorted = true;
				}
			}
		}
		c.getSession().write(MaplePacketCreator.finishedGather(mode));
		c.getSession().write(MaplePacketCreator.enableActions());
	}
        
	public static final void ItemSort2(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		/*slea.skip(4);
		byte mode = slea.readByte();
		if (mode < 0 || mode > 5) {
		return;
		}
		MapleInventory Inv = c.getPlayer().getInventory(MapleInventoryType.getByType(mode));
		ArrayList<Item> itemarray = new ArrayList<Item>();
		for (Iterator<IItem> it = Inv.iterator(); it.hasNext();) {
		Item item = (Item) it.next();
		itemarray.add((Item) (item.copy()));
		}
		Collections.sort(itemarray);
		for (IItem item : itemarray) {
		MapleInventoryManipulator.removeById(c, MapleInventoryType.getByType(mode), item.getItemId(), item.getQuantity(), false, false);
		}
		for (Item i : itemarray) {
		MapleInventoryManipulator.addFromDrop(c, i, false, false);
		}
		c.getSession().write(MaplePacketCreator.finishedSort2(mode));
		c.getSession().write(MaplePacketCreator.enableActions());*/
	}

	public static final void UseRewardItem(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
			if (chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1
					&& chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1
					&& chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1
					&& chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1) {
				final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
				final Pair<Integer, List<StructRewardItem>> rewards = ii.getRewardItem(itemId);

				if (rewards != null) {
					for (StructRewardItem reward : rewards.getRight()) {
						if (Randomizer.nextInt(rewards.getLeft()) < reward.prob) { // Total prob
							if (GameConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
								final IItem item = ii.getEquipById(reward.itemid);
								if (reward.period != -1) {
									item.setExpiration(System.currentTimeMillis() + (reward.period * 60 * 60 * 10));
								}
								MapleInventoryManipulator.addbyItem(c, item);
							} else {
								MapleInventoryManipulator.addById(c, reward.itemid, reward.quantity);
							}
							MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, false, false);

							c.getSession().write(MaplePacketCreator.showRewardItemAnimation(reward.itemid, reward.effect));
							chr.getMap().broadcastMessage(chr, MaplePacketCreator.showRewardItemAnimation(reward.itemid, reward.effect, chr.getId()), false);
							break;
						}
					}
				}
			} else {
				chr.dropMessage(6, "Insufficient inventory slot.");
			}
		}
		c.getSession().write(MaplePacketCreator.enableActions());
	}

	public static final void UseItem(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		if (!chr.isAlive()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
			MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr);
		} else {
			c.getSession().write(MaplePacketCreator.enableActions());
		}
	}

	public static final void UseReturnScroll(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		if (!chr.isAlive()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
			if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyReturnScroll(chr)) {
				MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
			} else {
				c.getSession().write(MaplePacketCreator.enableActions());
			}
		} else {
			c.getSession().write(MaplePacketCreator.enableActions());
		}
	}

	public static final void UseUpgradeScroll(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		//Lunar Gloves unlimited scroll
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final byte dst = (byte) slea.readShort();
		final byte ws = (byte) slea.readShort();
		boolean whiteScroll = false; // white scroll being used?
		boolean legendarySpirit = false; // legendary spirit skill
		final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

		if ((ws & 2) == 2) {
			whiteScroll = true;
		}

		IEquip toScroll;
		if (dst < 0) {
			toScroll = (IEquip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
		} else { // legendary spirit
			legendarySpirit = true;
			toScroll = (IEquip) chr.getInventory(MapleInventoryType.EQUIP).getItem(dst);
		}
		final byte oldLevel = toScroll.getLevel();
		final byte oldFlag = toScroll.getFlag();

		if (!GameConstants.isSpecialScroll(toScroll.getItemId()) && !GameConstants.isCleanSlate(toScroll.getItemId())) {
			if (toScroll.getUpgradeSlots() < 1) {
				c.getSession().write(MaplePacketCreator.getInventoryFull());
				return;
			}
		}
		IItem scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
		IItem wscroll = null;

		// Anti cheat and validation
		List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
		if (scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			return;
		}

		if (whiteScroll) {
			wscroll = chr.getInventory(MapleInventoryType.USE).findById(2340000);
			if (wscroll == null || wscroll.getItemId() != 2340000) {
				whiteScroll = false;
			}
		}
		if (!GameConstants.isChaosScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId())) {
			if (!ii.canScroll(scroll.getItemId(), toScroll.getItemId())) {
				return;
			}
		}
		if (scroll.getQuantity() <= 0) {
			return;
		}

		if (legendarySpirit) {
			if (chr.getSkillLevel(SkillFactory.getSkill(1003)) <= 0) {
				//AutobanManager.getInstance().addPoints(c, 50, 120000, "Using the Skill 'Legendary Spirit' without having it.");
				return;
			}
		}

		// Scroll Success/ Failure/ Curse
		final IEquip scrolled = (IEquip) ii.scrollEquipWithId(toScroll, scroll.getItemId(), whiteScroll);
		ScrollResult scrollSuccess;
		if (scrolled == null) {
			scrollSuccess = IEquip.ScrollResult.CURSE;
		} else if (scrolled.getLevel() > oldLevel) {
			scrollSuccess = IEquip.ScrollResult.SUCCESS;
		} else if ((GameConstants.isCleanSlate(scroll.getItemId()) && scrolled.getLevel() == oldLevel + 1)) {
			scrollSuccess = IEquip.ScrollResult.SUCCESS;
		} else if ((GameConstants.isSpecialScroll(scroll.getItemId()) && scrolled.getFlag() > oldFlag)) {
			scrollSuccess = IEquip.ScrollResult.SUCCESS;
		} else {
			scrollSuccess = IEquip.ScrollResult.FAIL;
		}

		// Update
		chr.getInventory(MapleInventoryType.USE).removeItem(scroll.getPosition(), (short) 1, false);
		if (whiteScroll) {
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, wscroll.getPosition(), (short) 1, false, false);
		}

		if (scrollSuccess == IEquip.ScrollResult.CURSE) {
			c.getSession().write(MaplePacketCreator.scrolledItem(scroll, toScroll, true));
			if (dst < 0) {
				if (toScroll.getItemId() != MapleCharacter.unlimitedSlotItem) { //unlimited slot item check
					chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
				}
			} else {
				if (toScroll.getItemId() != MapleCharacter.unlimitedSlotItem) { //unlimited slot item check
					chr.getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
				}
			}
		} else {
			c.getSession().write(MaplePacketCreator.scrolledItem(scroll, scrolled, false));
		}

		chr.getMap().broadcastMessage(MaplePacketCreator.getScrollEffect(c.getPlayer().getId(), scrollSuccess, legendarySpirit));

		// equipped item was scrolled and changed
		if (dst < 0 && (scrollSuccess == IEquip.ScrollResult.SUCCESS || scrollSuccess == IEquip.ScrollResult.CURSE)) {
			chr.equipChanged();
		}
	}

	public static final void UseSkillBook(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
			return;
		}
		final Map<String, Integer> skilldata = MapleItemInformationProvider.getInstance().getSkillStats(toUse.getItemId());
		if (skilldata == null) { // Hacking or used an unknown item
			return;
		}
		boolean canuse = false, success = false;
		int skill = 0, maxlevel = 0;

		final int SuccessRate = skilldata.get("success");
		final int ReqSkillLevel = skilldata.get("reqSkillLevel");
		final int MasterLevel = skilldata.get("masterLevel");

		byte i = 0;
		Integer CurrentLoopedSkillId;
		for (;;) {
			CurrentLoopedSkillId = skilldata.get("skillid" + i);
			i++;
			if (CurrentLoopedSkillId == null) {
				break; // End of data
			}
			if (Math.floor(CurrentLoopedSkillId / 100000) == chr.getJob() / 10) {
				final ISkill CurrSkillData = SkillFactory.getSkill(CurrentLoopedSkillId);
				if (chr.getSkillLevel(CurrSkillData) >= ReqSkillLevel && chr.getMasterLevel(CurrSkillData) < MasterLevel) {
					canuse = true;
					if (Randomizer.nextInt(99) <= SuccessRate && SuccessRate != 0) {
						success = true;
						final ISkill skill2 = CurrSkillData;
						chr.changeSkillLevel(skill2, chr.getSkillLevel(skill2), (byte) MasterLevel);
					} else {
						success = false;
					}
					MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
					break;
				} else { // Failed to meet skill requirements
					canuse = false;
				}
			}
		}
		c.getSession().write(MaplePacketCreator.useSkillBook(chr, skill, maxlevel, canuse, success));
	}

	public static final void UseCatchItem(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final int itemid = slea.readInt();
		final MapleMonster mob = chr.getMap().getMonsterByOid(slea.readInt());
		final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mob != null) {
			switch (itemid) {
				case 2270002: { // Characteristic Stone
					final MapleMap map = chr.getMap();

					if (mob.getHp() <= mob.getMobMaxHp() / 2) {
						map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
						map.killMonster(mob, chr, true, false, (byte) 0);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
					} else {
						map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 0));
						chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
					}
					break;
				}
				case 2270000: { // Pheromone Perfume
					if (mob.getId() != 9300101) {
						break;
					}
					final MapleMap map = c.getPlayer().getMap();

					map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
					map.killMonster(mob, chr, true, false, (byte) 0);
					MapleInventoryManipulator.addById(c, 1902000, (short) 1, null);
					MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
					break;
				}
				case 2270003: { // Cliff's Magic Cane
					if (mob.getId() != 9500320) {
						break;
					}
					final MapleMap map = c.getPlayer().getMap();

					if (mob.getHp() <= mob.getMobMaxHp() / 2) {
						map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
						map.killMonster(mob, chr, true, false, (byte) 0);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
					} else {
						map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 0));
						chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
					}
					break;
				}
			}
		}
		c.getSession().write(MaplePacketCreator.enableActions());
		//   c.getPlayer().setAPQScore(c.getPlayer().getAPQScore() + 1);
		// c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(c.getPlayer().getName(), c.getPlayer().getAPQScore(), false));
	}

	public static final void UseMountFood(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final int itemid = slea.readInt(); //2260000 usually
		final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
		final MapleMount mount = chr.getMount();

		if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mount != null) {
			final int fatigue = mount.getFatigue();

			boolean levelup = false;
			mount.setFatigue(-30);

			if (fatigue > 0) {
				mount.increaseExp();
				final int level = mount.getLevel();
				if (mount.getExp() >= GameConstants.getMountExpNeededForLevel(level + 1) && level < 31) {
					mount.setLevel(level + 1);
					levelup = true;
				}
			}
			chr.getMap().broadcastMessage(MaplePacketCreator.updateMount(chr, levelup));
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
		}
		c.getSession().write(MaplePacketCreator.enableActions());
	}

	public static final void UseScriptedNPCItem(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
			switch (toUse.getItemId()) {
				case 2430007: // Blank Compass
				{
					final MapleInventory inventory = chr.getInventory(MapleInventoryType.SETUP);
					MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);

					if (inventory.countById(3994102) >= 20 // Compass Letter "North"
							&& inventory.countById(3994103) >= 20 // Compass Letter "South"
							&& inventory.countById(3994104) >= 20 // Compass Letter "East"
							&& inventory.countById(3994105) >= 20) { // Compass Letter "West"
						MapleInventoryManipulator.addById(c, 2430008, (short) 1); // Gold Compass
						MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994102, 20, false, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994103, 20, false, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994104, 20, false, false);
						MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994105, 20, false, false);
					} else {
						MapleInventoryManipulator.addById(c, 2430007, (short) 1); // Blank Compass
					}
					NPCScriptManager.getInstance().start(c, 2084001);
					break;
				}
				case 2430008: // Gold Compass
				{
					chr.saveLocation(SavedLocationType.RICHIE);
					MapleMap map;
					boolean warped = false;

					for (int i = 390001000; i <= 390001004; i++) {
						map = c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(i);

						if (map.getCharactersSize() == 0) {
							chr.changeMap(map, map.getPortal(0));
							warped = true;
							break;
						}
					}
					if (warped) { // Removal of gold compass
						MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, 2430008, 1, false, false);
					} else { // Or mabe some other message.
						c.getPlayer().dropMessage(5, "All maps are currently in use, please try again later.");
					}
					break;
				}
			}
		}
		c.getSession().write(MaplePacketCreator.enableActions());
	}

	public static final void UseSummonBag(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		if (!chr.isAlive()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

		if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {

			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);

			if (c.getPlayer().isGM() || !FieldLimitType.SummoningBag.check(chr.getMap().getFieldLimit())) {
				final List<Pair<Integer, Integer>> toSpawn = MapleItemInformationProvider.getInstance().getSummonMobs(itemId);

				if (toSpawn == null) {
					c.getSession().write(MaplePacketCreator.enableActions());
					return;
				}
				MapleMonster ht;
				int type = 0;

				for (int i = 0; i < toSpawn.size(); i++) {
					if (Randomizer.nextInt(99) <= toSpawn.get(i).getRight()) {
						ht = MapleLifeFactory.getMonster(toSpawn.get(i).getLeft());
						chr.getMap().spawnMonster_sSack(ht, chr.getPosition(), type);
					}
				}
			}
		}
		c.getSession().write(MaplePacketCreator.enableActions());
	}

	public static final void UseTreasureChest(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final short slot = slea.readShort();
		final int itemid = slea.readInt();

		final IItem toUse = chr.getInventory(MapleInventoryType.ETC).getItem((byte) slot);
		if (toUse == null || toUse.getQuantity() <= 0 || toUse.getItemId() != itemid) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		int reward;
		int keyIDforRemoval = 0;

		switch (toUse.getItemId()) {
			case 4280000: // Gold box
				reward = RandomRewards.getInstance().getGoldBoxReward();
				keyIDforRemoval = 5490000;
				break;
			case 4280001: // Silver box
				reward = RandomRewards.getInstance().getSilverBoxReward();
				keyIDforRemoval = 5490001;
				break;
			default: // Up to no good
				c.getSession().close();
				return;
		}

		// Get the quantity
		int amount = 1;
		switch (reward) {
			case 2000004:
				amount = 200; // Elixir
				break;
			case 2000005:
				amount = 100; // Power Elixir
				break;
		}
		if (chr.getInventory(MapleInventoryType.CASH).countById(keyIDforRemoval) > 0) {
			final IItem item = MapleInventoryManipulator.addbyId_Gachapon(c, reward, (short) amount);
			if (item == null) {
				chr.dropMessage(5, "Please check your item inventory and see if you have a Master Key, or if the inventory is full.");
				c.getSession().write(MaplePacketCreator.enableActions());
				return;
			}
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, (byte) slot, (short) 1, true);
			MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, keyIDforRemoval, 1, true, false);
			c.getSession().write(MaplePacketCreator.getShowItemGain(reward, (short) amount, true));
			if (GameConstants.gachaponRareItem(item.getItemId()) > 0) {
				try {
					c.getChannelServer().getWorldInterface().broadcastMessage(MaplePacketCreator.getGachaponMega(c.getPlayer().getName(), " : Lucky winner of Gachapon! Congratulations~", item, (byte) 2).getBytes());
				} catch (RemoteException e) {
					c.getChannelServer().reconnectWorld();
				}
			}
		} else {
			chr.dropMessage(5, "Please check your item inventory and see if you have a Master Key, or if the inventory is full.");
			c.getSession().write(MaplePacketCreator.enableActions());
		}
	}

	public static final void UseCashItem(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		slea.skip(4);
		final byte slot = (byte) slea.readShort();
		final int itemId = slea.readInt();
		final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot);
		if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		boolean used = false;
		switch (itemId) {
			case 5043000: { // NPC Teleport Rock
				final short questid = slea.readShort();
				final int npcid = slea.readInt();
				final MapleQuest quest = MapleQuest.getInstance(questid);

				if (c.getPlayer().getQuest(quest).getStatus() == 1 && quest.canComplete(c.getPlayer(), npcid)) {
					final MapleMap map = c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(MapleLifeFactory.getNPCLocation(npcid));
					if (map.containsNPC(npcid) != -1) {
						c.getPlayer().changeMap(map, map.getPortal(0));
					}
					used = true;
				}
				break;
			}
			case 2320000: // The Teleport Rock
			case 5041000: // VIP Teleport Rock
			case 5040000: // The Teleport Rock
			case 5040001: { // Teleport Coke
				if (slea.readByte() == 0) { // Rocktype
					final MapleMap target = c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(slea.readInt());
					if (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) { //Makes sure this map doesn't have a forced return map
						c.getPlayer().changeMap(target, target.getPortal(0));
						used = true;
					}
				} else {
					final MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
					if (victim != null && !victim.isGM()) {
						if (!FieldLimitType.VipRock.check(c.getChannelServer().getMapFactory(c.getPlayer().getWorld()).getMap(victim.getMapId()).getFieldLimit())) {
							if (itemId == 5041000 || (victim.getMapId() / 100000000) == (c.getPlayer().getMapId() / 100000000)) { // Viprock or same continent
								c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
								used = true;
							}
						}
					}
				}
				break;
			}
			case 5050000: { // AP Reset
				List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
				final int apto = slea.readInt();
				final int apfrom = slea.readInt();

				if (apto == apfrom) {
					break; // Hack
				}
				final int job = c.getPlayer().getJob();
				final PlayerStats playerst = c.getPlayer().getStat();
				used = true;

				switch (apto) { // AP to
					case 64: // str
						if (playerst.getStr() >= 999) {
							used = false;
						}
						break;
					case 128: // dex
						if (playerst.getDex() >= 999) {
							used = false;
						}
						break;
					case 256: // int
						if (playerst.getInt() >= 999) {
							used = false;
						}
						break;
					case 512: // luk
						if (playerst.getLuk() >= 999) {
							used = false;
						}
						break;
					case 2048: // hp
						if (playerst.getMaxHp() >= 30000) {
							used = false;
						}
					case 8192: // mp
						if (playerst.getMaxMp() >= 30000) {
							used = false;
						}
				}
				switch (apfrom) { // AP to
					case 64: // str
						if (playerst.getStr() <= 4) {
							used = false;
						}
						break;
					case 128: // dex
						if (playerst.getDex() <= 4) {
							used = false;
						}
						break;
					case 256: // int
						if (playerst.getInt() <= 4) {
							used = false;
						}
						break;
					case 512: // luk
						if (playerst.getLuk() <= 4) {
							used = false;
						}
						break;
					case 2048: // hp
						if (/*playerst.getMaxMp() < ((c.getPlayer().getLevel() * 14) + 134) || */c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() >= 10000) {
							used = false;
						}
					case 8192: // mp
						if (/*playerst.getMaxMp() < ((c.getPlayer().getLevel() * 14) + 134) || */c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() >= 10000) {
							used = false;
						}
				}
				if (used) {
					switch (apto) { // AP to
						case 64: { // str
							final int toSet = playerst.getStr() + 1;
							playerst.setStr(toSet);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, toSet));
							break;
						}
						case 128: { // dex
							final int toSet = playerst.getDex() + 1;
							playerst.setDex(toSet);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, toSet));
							break;
						}
						case 256: { // int
							final int toSet = playerst.getInt() + 1;
							playerst.setInt(toSet);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, toSet));
							break;
						}
						case 512: { // luk
							final int toSet = playerst.getLuk() + 1;
							playerst.setLuk(toSet);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, toSet));
							break;
						}
						case 2048: // hp
							int maxhp = playerst.getMaxHp();

							if (job == 0) { // Beginner
								maxhp += Randomizer.rand(8, 12);
							} else if (job >= 100 && job <= 132) { // Warrior
								ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
								int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
								maxhp += Randomizer.rand(20, 25);
								if (improvingMaxHPLevel >= 1) {
									maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
								}
							} else if (job >= 200 && job <= 232) { // Magician
								maxhp += Randomizer.rand(10, 20);
							} else if (job >= 300 && job <= 322) { // Bowman
								maxhp += Randomizer.rand(16, 20);
							} else if (job >= 400 && job <= 434) { // Thief
								maxhp += Randomizer.rand(16, 20);
							} else if (job >= 500 && job <= 522) { // Pirate
								ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
								int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
								maxhp += 20;
								if (improvingMaxHPLevel >= 1) {
									maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
								}
							} else if (job >= 1100 && job <= 1111) { // Soul Master
								ISkill improvingMaxHP = SkillFactory.getSkill(11000000);
								int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
								maxhp += Randomizer.rand(36, 42);
								if (improvingMaxHPLevel >= 1) {
									maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
								}
							} else if (job >= 1200 && job <= 1211) { // Flame Wizard
								maxhp += Randomizer.rand(15, 21);
							} else if ((job >= 1300 && job <= 1311) || (job >= 1400 && job <= 1411)) { // Wind Breaker and Night Walker
								maxhp += Randomizer.rand(30, 36);
							} else if (job >= 2000 && job <= 2112) { // Aran
								maxhp += Randomizer.rand(20, 25);
							} else { // GameMaster
								maxhp += Randomizer.rand(50, 100);
							}
							maxhp = Math.min(30000, maxhp);
							c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() + 1);
							playerst.setMaxHp(maxhp);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, maxhp));
							break;

						case 8192: // mp
							int maxmp = playerst.getMaxMp();

							if (job == 0) { // Beginner
								maxmp += Randomizer.rand(6, 8);
							} else if (job >= 100 && job <= 132) { // Warrior
								maxmp += Randomizer.rand(2, 4);
							} else if (job >= 200 && job <= 232) { // Magician
								ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
								int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
								maxmp += Randomizer.rand(18, 20);
								if (improvingMaxMPLevel >= 1) {
									maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
								}
							} else if (job >= 300 && job <= 322) { // Bowman
								maxmp += Randomizer.rand(10, 12);
							} else if (job >= 400 && job <= 434) { // Thief
								maxmp += Randomizer.rand(10, 12);
							} else if (job >= 500 && job <= 522) { // Pirate
								maxmp += Randomizer.rand(10, 12);
							} else if (job >= 1100 && job <= 1111) { // Soul Master
								maxmp += Randomizer.rand(6, 9);
							} else if (job >= 1200 && job <= 1211) { // Flame Wizard
								ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
								int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
								maxmp += Randomizer.rand(33, 36);
								if (improvingMaxMPLevel >= 1) {
									maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
								}
							} else if ((job >= 1300 && job <= 1311) || (job >= 1400 && job <= 1411)) { // Wind Breaker and Night Walker
								maxmp += Randomizer.rand(21, 24);
							} else if (job >= 2000 && job <= 2112) { // Aran
								maxmp += Randomizer.rand(4, 6);
							} else { // GameMaster
								maxmp += Randomizer.rand(50, 100);
							}
							maxmp = Math.min(30000, maxmp);
							c.getPlayer().setMpApUsed(c.getPlayer().getMpApUsed() + 1);
							playerst.setMaxMp(maxmp);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, maxmp));
							break;
					}
					switch (apfrom) { // AP from
						case 64: { // str
							final int toSet = playerst.getStr() - 1;
							playerst.setStr(toSet);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, toSet));
							break;
						}
						case 128: { // dex
							final int toSet = playerst.getDex() - 1;
							playerst.setDex(toSet);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, toSet));
							break;
						}
						case 256: { // int
							final int toSet = playerst.getInt() - 1;
							playerst.setInt(toSet);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, toSet));
							break;
						}
						case 512: { // luk
							final int toSet = playerst.getLuk() - 1;
							playerst.setLuk(toSet);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, toSet));
							break;
						}
						case 2048: // HP
							int maxhp = playerst.getMaxHp();
							if (job == 0) { // Beginner
								maxhp -= 12;
							} else if (job >= 100 && job <= 132) { // Warrior
								ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
								int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
								maxhp -= 24;
								if (improvingMaxHPLevel >= 1) {
									maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
								}
							} else if (job >= 200 && job <= 232) { // Magician
								maxhp -= 10;
							} else if (job >= 300 && job <= 322 || job >= 400 && job <= 434) { // Bowman, Thief
								maxhp -= 15;
							} else if (job >= 500 && job <= 522) { // Pirate
								ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
								int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
								maxhp -= 15;
								if (improvingMaxHPLevel > 0) {
									maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
								}
							} else if (job >= 1100 && job <= 1111) { // Soul Master
								ISkill improvingMaxHP = SkillFactory.getSkill(11000000);
								int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
								maxhp -= 27;
								if (improvingMaxHPLevel >= 1) {
									maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
								}
							} else if (job >= 1200 && job <= 1211) { // Flame Wizard
								maxhp -= 12;
							} else if ((job >= 1300 && job <= 1311) || (job >= 1400 && job <= 1411)) { // Wind Breaker and Night Walker
								maxhp -= 17;
							} else if (job >= 2000 && job <= 2112) { // Aran
								maxhp -= 20;
							} else { // GameMaster
								maxhp -= 20;
							}
							c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() - 1);
							playerst.setHp(maxhp);
							playerst.setMaxHp(maxhp);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, maxhp));
							break;
						case 8192: // MP
							int maxmp = playerst.getMaxMp();
							if (job == 0) { // Beginner
								maxmp -= 8;
							} else if (job >= 100 && job <= 132) { // Warrior
								maxmp -= 4;
							} else if (job >= 200 && job <= 232) { // Magician
								ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
								int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
								maxmp -= 20;
								if (improvingMaxMPLevel >= 1) {
									maxmp -= improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
								}
							} else if ((job >= 500 && job <= 522) || (job >= 300 && job <= 322) || (job >= 400 && job <= 434)) { // Pirate, Bowman. Thief
								maxmp -= 10;
							} else if (job >= 1100 && job <= 1111) { // Soul Master
								maxmp -= 6;
							} else if (job >= 1200 && job <= 1211) { // Flame Wizard
								ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
								int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
								maxmp -= 25;
								if (improvingMaxMPLevel >= 1) {
									maxmp -= improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
								}
							} else if ((job >= 1300 && job <= 1311) || (job >= 1400 && job <= 1411)) { // Wind Breaker and Night Walker
								maxmp -= 15;
							} else if (job >= 2000 && job <= 2112) { // Aran
								maxmp -= 5;
							} else { // GameMaster
								maxmp -= 20;
							}
							c.getPlayer().setMpApUsed(c.getPlayer().getMpApUsed() - 1);
							playerst.setMp(maxmp);
							playerst.setMaxMp(maxmp);
							statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, maxmp));
							break;
					}
					c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, c.getPlayer().getJob()));
				}
				break;
			}
			case 5050005:
			case 5050006:
			case 5050007:
			case 5050008:
			case 5050009:
			case 5050001: // SP Reset (1st job)
			case 5050002: // SP Reset (2nd job)
			case 5050003: // SP Reset (3rd job)
			case 5050004: { // SP Reset (4th job)
				int skill1 = slea.readInt();
				int skill2 = slea.readInt();

				ISkill skillSPTo = SkillFactory.getSkill(skill1);
				ISkill skillSPFrom = SkillFactory.getSkill(skill2);

				if (skillSPTo.isBeginnerSkill() || skillSPFrom.isBeginnerSkill()) {
					break;
				}
				if ((c.getPlayer().getSkillLevel(skillSPTo) + 1 <= skillSPTo.getMaxLevel()) && c.getPlayer().getSkillLevel(skillSPFrom) > 0) {
					c.getPlayer().changeSkillLevel(skillSPFrom, (byte) (c.getPlayer().getSkillLevel(skillSPFrom) - 1), c.getPlayer().getMasterLevel(skillSPFrom));
					c.getPlayer().changeSkillLevel(skillSPTo, (byte) (c.getPlayer().getSkillLevel(skillSPTo) + 1), c.getPlayer().getMasterLevel(skillSPTo));
					used = true;
				}
				break;
			}
			case 5060000: { // Item Tag
				final IItem item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readByte());

				if (item != null && item.getOwner().equals("")) {
					item.setOwner(c.getPlayer().getName());
					used = true;
				}
				break;
			}
			case 5520001:
			case 5520000: { // Karma
				final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
				final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());

				if (item != null) {
					if (MapleItemInformationProvider.getInstance().isKarmaEnabled(item.getItemId(), itemId)) {
						byte flag = item.getFlag();
						if (type == MapleInventoryType.EQUIP) {
							flag |= ItemFlag.KARMA_EQ.getValue();
						} else {
							flag |= ItemFlag.KARMA_USE.getValue();
						}
						item.setFlag(flag);

						c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
						used = true;
					}
				}
				break;
			}
			case 5570000: { // Vicious Hammer
				final byte invType = (byte) slea.readInt(); // Inventory type, Hammered eq is always EQ.
				final Equip item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
				// another int here, D3 49 DC 00
				if (item != null) {
					if (item.getViciousHammer() <= 2) {
						item.setViciousHammer((byte) (item.getViciousHammer() + 1));
						item.setUpgradeSlots((byte) (item.getUpgradeSlots() + 1));

						c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item, invType));
						//c.getSession().write(MTSCSPacket.ViciousHammer(true, (byte) item.getViciousHammer()));
						//c.getSession().write(MTSCSPacket.ViciousHammer(false, (byte) 0));
						used = true;
					}
				}
				break;
			}
			case 5060001: { // Sealing Lock
				final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
				final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
				// another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
				if (item != null && item.getExpiration() == -1) {
					byte flag = item.getFlag();
					flag |= ItemFlag.LOCK.getValue();
					item.setFlag(flag);

					c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
					used = true;
				}
				break;
			}
			case 5061000: { // Sealing Lock 7 days
				final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
				final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
				// another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
				if (item != null && item.getExpiration() == -1) {
					byte flag = item.getFlag();
					flag |= ItemFlag.LOCK.getValue();
					item.setFlag(flag);
					item.setExpiration(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000));

					c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
					used = true;
				}
				break;
			}
			case 5061001: { // Sealing Lock 30 days
				final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
				final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
				// another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
				if (item != null && item.getExpiration() == -1) {
					byte flag = item.getFlag();
					flag |= ItemFlag.LOCK.getValue();
					item.setFlag(flag);

					item.setExpiration(System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000));

					c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
					used = true;
				}
				break;
			}
			case 5061002: { // Sealing Lock 90 days
				final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
				final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
				// another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
				if (item != null && item.getExpiration() == -1) {
					byte flag = item.getFlag();
					flag |= ItemFlag.LOCK.getValue();
					item.setFlag(flag);

					item.setExpiration(System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000));

					c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
					used = true;
				}
				break;
			}
			case 5071000: { // Megaphone
				if (!c.getChannelServer().getMegaphoneMuteState()) {
					final String message = slea.readMapleAsciiString();

					if (message.length() > 65) {
						break;
					}
					final StringBuilder sb = new StringBuilder();
					addMedalString(c.getPlayer(), sb);
					sb.append(c.getPlayer().getName());
					sb.append(" : ");
					sb.append(message);

					c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(2, sb.toString()));
					used = true;
				} else {
					c.getPlayer().dropMessage(5, "The usage of Megapone is currently disabled.");
				}
				break;
			}
			case 5077000: { // 3 line Megaphone
				if (!c.getChannelServer().getMegaphoneMuteState()) {
					final byte numLines = slea.readByte();
					if (numLines > 3) {
						return;
					}
					final List<String> messages = new LinkedList<String>();
					String message;
					for (int i = 0; i < numLines; i++) {
						message = slea.readMapleAsciiString();
						if (message.length() > 65) {
							break;
						}
						messages.add(c.getPlayer().getName() + " : " + message);
					}
					final boolean ear = slea.readByte() > 0;

					try {
						c.getChannelServer().getWorldInterface().broadcastSmega(MaplePacketCreator.tripleSmega(messages, ear, c.getChannel()).getBytes());
						used = true;
					} catch (RemoteException e) {
						System.out.println("RemoteException occured, triple megaphone");
					}
				} else {
					c.getPlayer().dropMessage(5, "The usage of Megapone is currently disabled.");
				}
				break;
			}
			case 5073000: { // Heart Megaphone
				if (!c.getChannelServer().getMegaphoneMuteState()) {
					final String message = slea.readMapleAsciiString();

					if (message.length() > 65) {
						break;
					}
					final StringBuilder sb = new StringBuilder();
					addMedalString(c.getPlayer(), sb);
					sb.append(c.getPlayer().getName());
					sb.append(" : ");
					sb.append(message);

					final boolean ear = slea.readByte() != 0;

					try {
						c.getChannelServer().getWorldInterface().broadcastSmega(MaplePacketCreator.serverNotice(9, c.getChannel(), sb.toString(), ear).getBytes());
						used = true;
					} catch (RemoteException e) {
						System.out.println("RemoteException occured, heart megaphone");
					}
				} else {
					c.getPlayer().dropMessage(5, "The usage of Megapone is currently disabled.");
				}
				break;
			}
			case 5074000: { // Skull Megaphone
				if (!c.getChannelServer().getMegaphoneMuteState()) {
					final String message = slea.readMapleAsciiString();

					if (message.length() > 65) {
						break;
					}
					final StringBuilder sb = new StringBuilder();
					addMedalString(c.getPlayer(), sb);
					sb.append(c.getPlayer().getName());
					sb.append(" : ");
					sb.append(message);

					final boolean ear = slea.readByte() != 0;

					try {
						c.getChannelServer().getWorldInterface().broadcastSmega(MaplePacketCreator.serverNotice(10, c.getChannel(), sb.toString(), ear).getBytes());
						used = true;
					} catch (RemoteException e) {
						System.out.println("RemoteException occured, skull megaphone");
					}
				} else {
					c.getPlayer().dropMessage(5, "The usage of Megapone is currently disabled.");
				}
				break;
			}
			case 5072000: { // Super Megaphone
				if (!c.getChannelServer().getMegaphoneMuteState()) {
					final String message = slea.readMapleAsciiString();
					if (message.length() > 65) {
						break;
					}
					final StringBuilder sb = new StringBuilder();
					addMedalString(c.getPlayer(), sb);
					sb.append(c.getPlayer().getName());
					sb.append(" : ");
					sb.append(message);
					final boolean ear = slea.readByte() != 0;
					try {
						c.getChannelServer().getWorldInterface().broadcastSmega(MaplePacketCreator.serverNotice(3, c.getChannel(), sb.toString(), ear).getBytes());
						used = true;
					} catch (RemoteException e) {
						System.out.println("RemoteException occured, super megaphone");
					}
				} else {
					c.getPlayer().dropMessage(5, "The usage of Megapone is currently disabled.");
				}
				break;
			}
			case 5076000: { // Item Megaphone
				if (!c.getChannelServer().getMegaphoneMuteState()) {
					final String message = slea.readMapleAsciiString();

					if (message.length() > 65) {
						break;
					}
					final StringBuilder sb = new StringBuilder();
					addMedalString(c.getPlayer(), sb);
					sb.append(c.getPlayer().getName());
					sb.append(" : ");
					sb.append(message);

					final boolean ear = slea.readByte() > 0;

					IItem item = null;
					if (slea.readByte() == 1) { //item
						byte invType = (byte) slea.readInt();
						byte pos = (byte) slea.readInt();
						item = c.getPlayer().getInventory(MapleInventoryType.getByType(invType)).getItem(pos);
					}

					try {
						c.getChannelServer().getWorldInterface().broadcastSmega(MaplePacketCreator.itemMegaphone(sb.toString(), ear, c.getChannel(), item).getBytes());
						used = true;
					} catch (RemoteException e) {
						System.out.println("RemoteException occured, item megaphone");
					}
				} else {
					c.getPlayer().dropMessage(5, "The usage of Megapone is currently disabled.");
				}
				break;
			}
			case 5075000: // MapleTV Messenger
			case 5075001: // MapleTV Star Messenger
			case 5075002: // MapleTV Heart Messenger
			case 5090100: // Wedding Invitation Card
			case 5090000: { // Note
				final String sendTo = slea.readMapleAsciiString();
				final String msg = slea.readMapleAsciiString();
				c.getPlayer().sendNote(sendTo, msg);
				used = true;
				break;
			}
			case 5100000: { // Congratulatory Song
				c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange("Jukebox/Congratulation"));
				used = true;
				break;
			}
			case 5170000: { // Pet name change
				if (c.getPlayer().getPet(0) == null) {
					break;
				}
				String nName = slea.readMapleAsciiString();
				if (MapleCharacterUtil.canChangePetName(nName)) {
					c.getPlayer().getPet(0).setName(nName);
					c.getSession().write(PetPacket.updatePet(c.getPlayer().getPet(0), true));
					c.getSession().write(MaplePacketCreator.enableActions());
					c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MTSCSPacket.changePetName(c.getPlayer(), nName, 1), true);
					used = true;
				}
				break;
			}
			case 5200000: { // Bronze Sack of Mesos
				c.getPlayer().gainMeso(1000000, true, false, true);
				c.getSession().write(MaplePacketCreator.enableActions());
				used = true;
				break;
			}
			case 5200001: { // Silver Sack of Mesos
				c.getPlayer().gainMeso(5000000, true, false, true);
				c.getSession().write(MaplePacketCreator.enableActions());
				used = true;
				break;
			}
			case 5200002: { // Gold Sack of Mesos
				c.getPlayer().gainMeso(10000000, true, false, true);
				c.getSession().write(MaplePacketCreator.enableActions());
				used = true;
				break;
			}
			case 5240000:
			case 5240001:
			case 5240002:
			case 5240003:
			case 5240004:
			case 5240005:
			case 5240006:
			case 5240007:
			case 5240008:
			case 5240009:
			case 5240010:
			case 5240011:
			case 5240012:
			case 5240013:
			case 5240014:
			case 5240015:
			case 5240016:
			case 5240017:
			case 5240018:
			case 5240019:
			case 5240020:
			case 5240021:
			case 5240022:
			case 5240023:
			case 5240025:
			case 5240026:
			case 5240027:
			case 5240028:
			case 5240024: { // Pet food
				MaplePet pet = c.getPlayer().getPet(0);

				if (pet == null) {
					break;
				}
				if (!pet.canConsume(itemId)) {
					pet = c.getPlayer().getPet(1);
					if (pet != null) {
						if (!pet.canConsume(itemId)) {
							pet = c.getPlayer().getPet(2);
							if (pet != null) {
								if (!pet.canConsume(itemId)) {
									break;
								}
							} else {
								break;
							}
						}
					} else {
						break;
					}
				}
				final byte petindex = c.getPlayer().getPetIndex(pet);
				pet.setFullness(100);
				if (pet.getCloseness() < 30000) {
					if (pet.getCloseness() + 100 > 30000) {
						pet.setCloseness(30000);
					} else {
						pet.setCloseness(pet.getCloseness() + 100);
					}
					if (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
						pet.setLevel(pet.getLevel() + 1);
						c.getSession().write(PetPacket.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
						c.getPlayer().getMap().broadcastMessage(PetPacket.showPetLevelUp(c.getPlayer(), petindex));
					}
				}
				c.getSession().write(PetPacket.updatePet(pet, true));
				c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(c.getPlayer().getId(), (byte) 1, petindex, true, true), true);
				used = true;
				break;
			}
			case 5281000: // Passed gas
			case 5370000: { // Chalkboard
				c.getPlayer().setChalkboard(slea.readMapleAsciiString());
				break;
			}
			case 5370001: { // BlackBoard
				if (c.getPlayer().getMapId() / 1000000 == 910) {
					c.getPlayer().setChalkboard(slea.readMapleAsciiString());
				}
				break;
			}
			case 5390000: // Diablo Messenger
			case 5390001: // Cloud 9 Messenger
			case 5390002: // Loveholic Messenger
			case 5390003: // New Year Megassenger 1
			case 5390004: // New Year Megassenger 2
			case 5390005: // Cute Tiger Messenger
			case 5390006: { // Tiger Roar's Messenger
				if (!c.getChannelServer().getMegaphoneMuteState()) {
					final String text = slea.readMapleAsciiString();
					if (text.length() > 55) {
						break;
					}
					final boolean ear = slea.readByte() != 0;
					try {
						c.getChannelServer().getWorldInterface().broadcastSmega(MaplePacketCreator.getAvatarMega(c.getPlayer(), c.getChannel(), itemId, text, ear).getBytes());
						used = true;
					} catch (RemoteException e) {
						System.out.println("RemoteException occured, TV megaphone");
					}
				} else {
					c.getPlayer().dropMessage(5, "The usage of Megapone is currently disabled.");
				}
				break;
			}
			case 5450000: { // Mu Mu the Travelling Merchant
				MapleShopFactory.getInstance().getShop(61).sendShop(c);
				used = true;
				break;
			}
			default:
				if (itemId / 10000 == 512) {
					final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
					final String msg = ii.getMsg(itemId).replaceFirst("%s", c.getPlayer().getName()).replaceFirst("%s", slea.readMapleAsciiString());
					c.getPlayer().getMap().startMapEffect(msg, itemId);

					final int buff = ii.getStateChangeItem(itemId);
					if (buff != 0) {
						for (MapleCharacter mChar : c.getPlayer().getMap().getCharacters()) {
							ii.getItemEffect(buff).applyTo(mChar);
						}
					}
					used = true;
				} else {
					System.out.println(":: Unhandled CS item : " + itemId + " ::");
					System.out.println(slea.toString());
				}
				break;
		}

		if (used) {
			MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
		} else {
			c.getSession().write(MaplePacketCreator.enableActions());
		}
	}

	public static final void Pickup_Player(final SeekableLittleEndianAccessor slea, MapleClient c, final MapleCharacter chr) {
		slea.skip(5); // [4] Seems to be tickcount, [1] always 0
		final Point Client_Reportedpos = slea.readPos();
		final MapleMapObject ob = chr.getMap().getMapObject(slea.readInt());

		if (ob == null || ob.getType() != MapleMapObjectType.ITEM) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		final MapleMapItem mapitem = (MapleMapItem) ob;

		if (mapitem.isPickedUp()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		if (mapitem.getOwner() != chr.getId() && chr.getMap().getEverlast()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		final double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
		if (Distance > 2500) {
			chr.getCheatTracker().registerOffense(CheatingOffense.ITEMVAC_CLIENT, String.valueOf(Distance));
		} else if (chr.getPosition().distanceSq(mapitem.getPosition()) > 90000.0) {
			chr.getCheatTracker().registerOffense(CheatingOffense.ITEMVAC_SERVER);
		}
		if (mapitem.getMeso() > 0) {
			if (chr.getParty() != null && mapitem.getOwner() == chr.getId()) {
				final List<MapleCharacter> toGive = new LinkedList<MapleCharacter>();

				for (final MapleCharacter m : c.getChannelServer().getPartyMembers(chr.getParty())) { // TODO, store info in MaplePartyCharacter instead
					if (m != null) {
						if (m.getMapId() == chr.getMapId()) {
							toGive.add(m);
						}
					}
				}
				for (final MapleCharacter m : toGive) {
					m.gainMeso(mapitem.getMeso() / toGive.size(), true, true);
				}
			} else {
				chr.gainMeso(mapitem.getMeso(), true, true);
			}
			removeItem(chr, mapitem, ob);
		} else {
			if (useItem(c, mapitem.getItemId())) {
				removeItem(c.getPlayer(), mapitem, ob);
			} else {
				if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
					removeItem(chr, mapitem, ob);
				}
			}
		}
	}

	public static final void Pickup_Pet(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final MaplePet pet = chr.getPet(chr.getPetIndex(slea.readInt()));
		slea.skip(9); // [4] Zero, [4] Seems to be tickcount, [1] Always zero
		final Point Client_Reportedpos = slea.readPos();
		final MapleMapObject ob = chr.getMap().getMapObject(slea.readInt());

		if (ob == null || pet == null || ob.getType() != MapleMapObjectType.ITEM) {
			return;
		}
		final MapleMapItem mapitem = (MapleMapItem) ob;

		if (mapitem.isPickedUp()) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			return;
		}
		if (mapitem.getOwner() != chr.getId() || mapitem.isPlayerDrop()) {
			return;
		}
		final double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
		if (Distance > 2500) {
			chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_CLIENT, String.valueOf(Distance));
		} else if (pet.getPos().distanceSq(mapitem.getPosition()) > 90000.0) {
			chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_SERVER);
		}

		if (mapitem.getMeso() > 0) {
			if (chr.getInventory(MapleInventoryType.EQUIPPED).findById(1812000) == null) {
				c.getSession().write(MaplePacketCreator.enableActions());
				return;
			}
			if (chr.getParty() != null && mapitem.getOwner() == chr.getId()) {
				final List<MapleCharacter> toGive = new LinkedList<MapleCharacter>();

				for (final MapleCharacter m : c.getChannelServer().getPartyMembers(chr.getParty())) { // TODO, store info in MaplePartyCharacter instead
					if (m != null) {
						if (m.getMapId() == chr.getMapId()) {
							toGive.add(m);
						}
					}
				}
				for (final MapleCharacter m : toGive) {
					m.gainMeso(mapitem.getMeso() / toGive.size(), true, true);
				}
			} else {
				chr.gainMeso(mapitem.getMeso(), true, true);
			}
			removeItem(chr, mapitem, ob);
		} else {
			if (useItem(c, mapitem.getItemId())) {
				removeItem(chr, mapitem, ob);
			} else {
				if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
					removeItem(chr, mapitem, ob);
				}
			}
		}
	}

	private static final boolean useItem(final MapleClient c, final int id) {
		if (GameConstants.isUse(id)) { // TO prevent caching of everything, waste of mem
			final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			final byte consumeval = ii.isConsumeOnPickup(id);

			if (consumeval > 0) {
				if (consumeval == 2) {
					if (c.getPlayer().getParty() != null) {
						for (final MaplePartyCharacter pc : c.getPlayer().getParty().getMembers()) {
							final MapleCharacter chr = c.getPlayer().getMap().getCharacterById_InMap(pc.getId());
							if (chr != null) {
								ii.getItemEffect(id).applyTo(chr);
							}
						}
					} else {
						ii.getItemEffect(id).applyTo(c.getPlayer());
					}
				} else {
					ii.getItemEffect(id).applyTo(c.getPlayer());
				}
				c.getSession().write(MaplePacketCreator.getShowItemGain(id, (byte) 1));
				return true;
			}
		}
		return false;
	}

	private static final void removeItem(final MapleCharacter chr, final MapleMapItem mapitem, final MapleMapObject ob) {
		mapitem.setPickedUp(true);
		chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
		chr.getMap().removeMapObject(ob);
	}

	private static final void addMedalString(final MapleCharacter c, final StringBuilder sb) {
		final IItem medal = c.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
		if (medal != null) { // Medal
			sb.append("<");
			sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
			sb.append("> ");
		}
	}

	public static final void EnhancementScroll(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt();
		byte slot = (byte) slea.readShort();
		byte dst = (byte) slea.readShort();
		MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
		IItem scroll = useInventory.getItem((short) slot);
		IEquip toScroll = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) dst);
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		List scrollReqs = ii.getScrollReqs(scroll.getItemId());
		if ((scrollReqs.size() > 0) && (!scrollReqs.contains(Integer.valueOf(toScroll.getItemId())))) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			return;
		}
		if (scroll.getQuantity() < 1) {
			return;
		}
		IEquip scrolled = (IEquip) ii.scrollEnhancement(toScroll, scroll.getItemId());
		IEquip.ScrollResult scrollSuccess = IEquip.ScrollResult.FAIL;
		if (scrolled == null) {
			scrollSuccess = IEquip.ScrollResult.CURSE;
		} else if (isCleanSlate(scroll.getItemId())) {
			scrollSuccess = IEquip.ScrollResult.SUCCESS;
		}
		useInventory.removeItem(scroll.getPosition(), (short) 1, false);
		if (scrollSuccess == IEquip.ScrollResult.CURSE) {
			c.getSession().write(MaplePacketCreator.scrolledItem(scroll, toScroll, true, false));
			if (dst < 0) {
				c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
			} else {
				c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
			}
		} else {
			c.getSession().write(MaplePacketCreator.scrolledItem(scroll, scrolled, false, false));
		}
		c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEnhancementEffect(c.getPlayer().getId()));
		if ((dst < 0) && ((scrollSuccess == IEquip.ScrollResult.SUCCESS) || (scrollSuccess == IEquip.ScrollResult.CURSE))) {
			c.getPlayer().equipChanged();
		}
	}

	public static final void PotentialScroll(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt();
		byte slot = (byte) slea.readShort();
		byte dst = (byte) slea.readShort();
		MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
		IItem scroll = useInventory.getItem((short) slot);
		IEquip toScroll = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) dst);
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		List scrollReqs = ii.getScrollReqs(scroll.getItemId());
		if ((scrollReqs.size() > 0) && (!scrollReqs.contains(Integer.valueOf(toScroll.getItemId())))) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			return;
		}
		if (scroll.getQuantity() < 1) {
			return;
		}
		IEquip scrolled = (IEquip) ii.scrollHiddenPotential(toScroll, scroll.getItemId());
		IEquip.ScrollResult scrollSuccess = IEquip.ScrollResult.FAIL;
		if (scrolled == null) {
			scrollSuccess = IEquip.ScrollResult.CURSE;
		} else if (isCleanSlate(scroll.getItemId())) {
			scrollSuccess = IEquip.ScrollResult.SUCCESS;
		}
		useInventory.removeItem(scroll.getPosition(), (short) 1, false);
		if (scrollSuccess == IEquip.ScrollResult.CURSE) {
			c.getSession().write(MaplePacketCreator.scrolledItem(scroll, toScroll, true, false));
			if (dst < 0) {
				c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
			} else {
				c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
			}
		} else {
			c.getSession().write(MaplePacketCreator.scrolledItem(scroll, scrolled, false, false));
		}
		c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPotentialffect(c.getPlayer().getId()));
		if ((dst < 0) && ((scrollSuccess == IEquip.ScrollResult.SUCCESS) || (scrollSuccess == IEquip.ScrollResult.CURSE))) {
			c.getPlayer().equipChanged();
		}
	}

	private static final boolean isCleanSlate(int itemId) {
		return (scrollId > 20489999) && (scrollId < 2049004);
	}

	public static final void MagnifyingGlass(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt();
		byte slot = (byte) slea.readShort();
		byte applyto = (byte) slea.readShort();
		boolean Equipment = false;
		MapleInventory invItem = c.getPlayer().getInventory(MapleInventoryType.USE);
		IItem magnifyGlass = invItem.getItem((short) slot);
		IEquip myEquip = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) applyto);
		IEquip myEquipped = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) applyto);
		if (magnifyGlass.getQuantity() < 1) {
			return;
		}

		int[] itemPercentPlus = {10001, 10002, 10003, 10004, 10005, 10006, 10007, 10008, 10009, 10010, 10011, 10012, 10013, 10014, 10041, 10042, 10043, 10044, 10045, 10046, 10047, 10048, 10051, 10052, 10053, 10054, 10055, 10070, 10081, 20041, 20042, 20043, 20044, 20045, 20046, 20047, 20048, 20051, 20052, 20053, 20054, 20055, 20070, 20086, 30041, 30042, 30043, 30044, 30045, 30046, 30047, 30048, 30051, 30052, 30053, 30054, 30055, 30070, 30086};

		if (Math.ceil(Math.random() * 100.0D) < 15.0D) {
			itemOption1 = (short) itemPercentPlus[(int) (58.0D * Math.random() + 1.0D)];
			itemOption2 = (short) itemPercentPlus[(int) (58.0D * Math.random() + 1.0D)];
			if (Math.ceil(Math.random() * 100.0D) <= 25.0D) {
				itemOption3 = (short) itemPercentPlus[(int) (42.0D * Math.random() + 1.0D)];
			}
			itemStat = 7;
		} else if (Math.ceil(Math.random() * 100.0D) < 25.0D) {
			itemOption1 = (short) itemPercentPlus[(int) (42.0D * Math.random() + 1.0D)];
			itemOption2 = (short) itemPercentPlus[(int) (42.0D * Math.random() + 1.0D)];
			if (Math.ceil(Math.random() * 100.0D) <= 15.0D) {
				itemOption3 = (short) itemPercentPlus[(int) (28.0D * Math.random() + 1.0D)];
			}
			itemStat = 6;
		} else {
			itemOption1 = (short) itemPercentPlus[(int) (28.0D * Math.random() + 1.0D)];
			itemOption2 = (short) itemPercentPlus[(int) (28.0D * Math.random() + 1.0D)];
			if (Math.ceil(Math.random() * 100.0D) <= 15.0D) {
				itemOption3 = (short) itemPercentPlus[(int) (14.0D * Math.random() + 1.0D)];
			}
			itemStat = 5;
		}
		if (applyto >= 0) {
			myEquip.setPotential(itemStat);
			myEquip.setPotential_1(itemOption1);
			myEquip.setPotential_2(itemOption2);
			myEquip.setPotential_3(itemOption3);
			Equipment = true;
		} else {
			myEquipped.setPotential(itemStat);
			myEquipped.setPotential_1(itemOption1);
			myEquipped.setPotential_2(itemOption2);
			myEquipped.setPotential_3(itemOption3);
		}
		MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, magnifyGlass.getItemId(), 1, true, false);
		c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showMagnifyingEffect(c.getPlayer().getId(), applyto));
		c.getSession().write(MaplePacketCreator.updateEquipSlot(Equipment ? myEquip : myEquipped));
	}

	private static final short getRandomPotentialStat() {
		short[] potentials = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 10001, 10002, 10003, 10004, 10005, 10006, 10007, 10008, 10009, 10010, 10011, 10012, 10013, 10014, 10041, 10042, 10043, 10044, 10045, 10046, 10047, 10048, 10041, 10052, 10053, 10054, 10055, 10070, 10081, 10201, 10206, 10221, 10226, 10231, 10236, 10241, 10246, 10291, 20041, 20042, 20043, 20044, 20045, 20046, 20047, 20048, 20051, 20052, 20053, 20054, 20055, 20070, 20086, 20201, 20206, 20291, 20351, 20352, 20353, 20366, 20396, 20401, 20406, 20656, 30041, 30042, 30044, 30045, 30046, 30047, 30048, 30051, 30053, 30054, 30055, 30070, 30086, 30356, 30357, 30366, 30371, 30551, 30601, 30602, 31001, 31002, 31003, 31004};
		int rand = Randomizer.nextInt(potentials.length);
		return potentials[rand];
	}
}