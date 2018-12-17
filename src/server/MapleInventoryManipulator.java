package server;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import client.GameConstants;
import client.Equip;
import client.IItem;
import client.InventoryException;
import client.Item;
import client.ItemFlag;
import client.PlayerStats;
import client.MapleBuffStat;
import client.MaplePet;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import tools.MaplePacketCreator;

public class MapleInventoryManipulator {

    public static boolean addRing(final MapleCharacter chr, final int itemId, final int ringId) {
	MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
	MapleInventoryType type = GameConstants.getInventoryType(itemId);
	IItem nEquip = ii.getEquipById(itemId, ringId);

	short newSlot = chr.getInventory(type).addItem(nEquip);
	if (newSlot == -1) {
	    return false;
	}
	chr.getClient().getSession().write(MaplePacketCreator.addInventorySlot(type, nEquip));
	return true;
    }

    public static boolean addbyItem(final MapleClient c, final IItem item) {
	final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());
	final short newSlot = c.getPlayer().getInventory(type).addItem(item);
	if (newSlot == -1) {
	    c.getSession().write(MaplePacketCreator.getInventoryFull());
	    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
	    return false;
	} 
        c.getSession().write(MaplePacketCreator.getInventoryFull());
	c.getSession().write(MaplePacketCreator.addInventorySlot(type, item));
	return true;
    }

    public static boolean addById(MapleClient c, int itemId, short quantity) {
	return addById(c, itemId, quantity, null, null, 0);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner) {
	return addById(c, itemId, quantity, owner, null, 0);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet) {
	return addById(c, itemId, quantity, owner, pet, 0);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, long period) {
	final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
	final MapleInventoryType type = GameConstants.getInventoryType(itemId);

	if (!type.equals(MapleInventoryType.EQUIP)) {
	    final short slotMax = ii.getSlotMax(c, itemId);
	    final List<IItem> existing = c.getPlayer().getInventory(type).listById(itemId);
	    if (!GameConstants.isThrowingStar(itemId) && !GameConstants.isBullet(itemId)) {
		if (existing.size() > 0) { // first update all existing slots to slotMax
		    Iterator<IItem> i = existing.iterator();
		    while (quantity > 0) {
			if (i.hasNext()) {
			    Item eItem = (Item) i.next();
			    short oldQ = eItem.getQuantity();
			    if (oldQ < slotMax && (eItem.getOwner().equals(owner) || owner == null) && eItem.getExpiration() == -1) {
				short newQ = (short) Math.min(oldQ + quantity, slotMax);
				quantity -= (newQ - oldQ);
				eItem.setQuantity(newQ);
				c.getSession().write(MaplePacketCreator.updateInventorySlot(type, eItem, false));
			    }
			} else {
			    break;
			}
		    }
		}
		short inventorypos;
		Item nItem;
		// add new slots if there is still something left
		while (quantity > 0) {
		    short newQ = (short) Math.min(quantity, slotMax);
		    if (newQ != 0) {
			quantity -= newQ;
			nItem = new Item(itemId, (byte) 0, newQ, (byte) 0);

			inventorypos = c.getPlayer().getInventory(type).addItem(nItem);
			if (inventorypos == -1) {
			    c.getSession().write(MaplePacketCreator.getInventoryFull());
			    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
			    return false;
			}
			if (owner != null) {
			    nItem.setOwner(owner);
			}
			if (period > 0) {
			    nItem.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
			}
			if (pet != null) {
			    nItem.setPet(pet);
			    pet.setInventoryPosition(inventorypos);
			}
			c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem));
			if ((GameConstants.isThrowingStar(itemId) || GameConstants.isBullet(itemId)) && quantity == 0) {
			    break;
			}
		    } else {
			c.getSession().write(MaplePacketCreator.enableActions());
			return false;
		    }
		}
	    } else {
		// Throwing Stars and Bullets - Add all into one slot regardless of quantity.
		final Item nItem = new Item(itemId, (byte) 0, quantity, (byte) 0);
		final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);

		if (newSlot == -1) {
		    c.getSession().write(MaplePacketCreator.getInventoryFull());
		    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
		    return false;
		}
		if (period > 0) {
		    nItem.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
		}

		c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem));
		c.getSession().write(MaplePacketCreator.enableActions());
	    }
	} else {
	    if (quantity == 1) {
		final IItem nEquip = ii.getEquipById(itemId);
		if (owner != null) {
		    nEquip.setOwner(owner);
		}
		if (period > 0) {
		    nEquip.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
		}
		short newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
		if (newSlot == -1) {
		    c.getSession().write(MaplePacketCreator.getInventoryFull());
		    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
		    return false;
		}
		c.getSession().write(MaplePacketCreator.addInventorySlot(type, nEquip));
	    } else {
		throw new InventoryException("Trying to create equip with non-one quantity");
	    }
	}
	return true;
    }

    public static IItem addbyId_Gachapon(final MapleClient c, final int itemId, short quantity) {
	if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() == -1
		|| c.getPlayer().getInventory(MapleInventoryType.USE).getNextFreeSlot() == -1
		|| c.getPlayer().getInventory(MapleInventoryType.ETC).getNextFreeSlot() == -1
		|| c.getPlayer().getInventory(MapleInventoryType.SETUP).getNextFreeSlot() == -1) {
	    return null;
	}
	final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
	final MapleInventoryType type = GameConstants.getInventoryType(itemId);

	if (!type.equals(MapleInventoryType.EQUIP)) {
	    short slotMax = ii.getSlotMax(c, itemId);
	    final List<IItem> existing = c.getPlayer().getInventory(type).listById(itemId);

	    if (!GameConstants.isThrowingStar(itemId) && !GameConstants.isBullet(itemId)) {
		IItem nItem = null;
		boolean recieved = false;

		if (existing.size() > 0) { // first update all existing slots to slotMax
		    Iterator<IItem> i = existing.iterator();
		    while (quantity > 0) {
			if (i.hasNext()) {
			    nItem = (Item) i.next();
			    short oldQ = nItem.getQuantity();

			    if (oldQ < slotMax) {
				recieved = true;

				short newQ = (short) Math.min(oldQ + quantity, slotMax);
				quantity -= (newQ - oldQ);
				nItem.setQuantity(newQ);
				c.getSession().write(MaplePacketCreator.updateInventorySlot(type, nItem, false));
			    }
			} else {
			    break;
			}
		    }
		}
		// add new slots if there is still something left
		while (quantity > 0) {
		    short newQ = (short) Math.min(quantity, slotMax);
		    if (newQ != 0) {
			quantity -= newQ;
			nItem = new Item(itemId, (byte) 0, newQ, (byte) 0);
			final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
			if (newSlot == -1 && recieved) {
			    return nItem;
			} else if (newSlot == -1) {
			    return null;
			}
			recieved = true;
			c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem));
			if ((GameConstants.isThrowingStar(itemId) || GameConstants.isBullet(itemId)) && quantity == 0) {
			    break;
			}
		    } else {
			break;
		    }
		}
		if (recieved) {
		    return nItem;
		}
	    } else {
		// Throwing Stars and Bullets - Add all into one slot regardless of quantity.
		final Item nItem = new Item(itemId, (byte) 0, quantity, (byte) 0);
		final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);

		if (newSlot == -1) {
		    return null;
		}
		c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem));
		return nItem;
	    }
	} else {
	    if (quantity == 1) {
		final IItem item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
		final short newSlot = c.getPlayer().getInventory(type).addItem(item);

		if (newSlot == -1) {
		    return null;
		}
		c.getSession().write(MaplePacketCreator.addInventorySlot(type, item, true));
		return item;
	    } else {
		throw new InventoryException("Trying to create equip with non-one quantity");
	    }
	}
	return null;
    }

    public static boolean addFromDrop(final MapleClient c, final IItem item, final boolean show) {
	final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

	if (ii.isPickupRestricted(item.getItemId()) && c.getPlayer().haveItem(item.getItemId(), 1, true, false)) {
	    c.getSession().write(MaplePacketCreator.getInventoryFull());
	    c.getSession().write(MaplePacketCreator.showItemUnavailable());
	    return false;
	}

	short quantity = item.getQuantity();
	final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());

	if (!type.equals(MapleInventoryType.EQUIP)) {
	    final short slotMax = ii.getSlotMax(c, item.getItemId());
	    final List<IItem> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
	    if (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId())) {
		if (existing.size() > 0) { // first update all existing slots to slotMax
		    Iterator<IItem> i = existing.iterator();
		    while (quantity > 0) {
			if (i.hasNext()) {
			    final Item eItem = (Item) i.next();
			    final short oldQ = eItem.getQuantity();
			    if (oldQ < slotMax && item.getOwner().equals(eItem.getOwner()) && item.getExpiration() == eItem.getExpiration()) {
				final short newQ = (short) Math.min(oldQ + quantity, slotMax);
				quantity -= (newQ - oldQ);
				eItem.setQuantity(newQ);
				c.getSession().write(MaplePacketCreator.updateInventorySlot(type, eItem, true));
			    }
			} else {
			    break;
			}
		    }
		}
		// add new slots if there is still something left
		while (quantity > 0) {
		    final short newQ = (short) Math.min(quantity, slotMax);
		    quantity -= newQ;
		    final Item nItem = new Item(item.getItemId(), (byte) 0, newQ, (byte) 0);
		    nItem.setExpiration(item.getExpiration());
		    nItem.setOwner(item.getOwner());

		    final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
		    if (newSlot == -1) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			c.getSession().write(MaplePacketCreator.getShowInventoryFull());
			item.setQuantity((short) (quantity + newQ));
			return false;
		    }
		    c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem, true));
		}
	    } else {
		// Throwing Stars and Bullets - Add all into one slot regardless of quantity.
		final Item nItem = new Item(item.getItemId(), (byte) 0, quantity, (byte) 0);
		nItem.setExpiration(item.getExpiration());
		nItem.setOwner(item.getOwner());

		final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
		if (newSlot == -1) {
		    c.getSession().write(MaplePacketCreator.getInventoryFull());
		    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
		    return false;
		}
		c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem));
		c.getSession().write(MaplePacketCreator.enableActions());
	    }
	} else {
	    if (quantity == 1) {
		final short newSlot = c.getPlayer().getInventory(type).addItem(item);

		if (newSlot == -1) {
		    c.getSession().write(MaplePacketCreator.getInventoryFull());
		    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
		    return false;
		}
		c.getSession().write(MaplePacketCreator.addInventorySlot(type, item, true));
	    } else {
		throw new RuntimeException("Trying to create equip with non-one quantity");
	    }
	}
	if (show) {
	    c.getSession().write(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity()));
	}
	return true;
    }

    public static boolean checkSpace(final MapleClient c, final int itemid, int quantity, final String owner) {
	final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
	final MapleInventoryType type = GameConstants.getInventoryType(itemid);

	if (!type.equals(MapleInventoryType.EQUIP)) {
	    final short slotMax = ii.getSlotMax(c, itemid);
	    final List<IItem> existing = c.getPlayer().getInventory(type).listById(itemid);
	    if (!GameConstants.isThrowingStar(itemid) && !GameConstants.isBullet(itemid)) {
		if (existing.size() > 0) { // first update all existing slots to slotMax
		    for (IItem eItem : existing) {
			final short oldQ = eItem.getQuantity();
			if (oldQ < slotMax && owner.equals(eItem.getOwner())) {
			    final short newQ = (short) Math.min(oldQ + quantity, slotMax);
			    quantity -= (newQ - oldQ);
			}
			if (quantity <= 0) {
			    break;
			}
		    }
		}
	    }
	    // add new slots if there is still something left
	    final int numSlotsNeeded;
	    if (slotMax > 0) {
		numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
	    } else {
		numSlotsNeeded = 1;
	    }
	    return !c.getPlayer().getInventory(type).isFull(numSlotsNeeded - 1);
	} else {
	    return !c.getPlayer().getInventory(type).isFull();
	}
    }

    public static void removeFromSlot(final MapleClient c, final MapleInventoryType type, final short slot, final short quantity, final boolean fromDrop) {
	removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }

    public static void removeFromSlot(final MapleClient c, final MapleInventoryType type, final short slot, short quantity, final boolean fromDrop, final boolean consume) {
	final IItem item = c.getPlayer().getInventory(type).getItem(slot);
	final boolean allowZero = consume && (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId()));
	c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);

	if (item.getQuantity() == 0 && !allowZero) {
	    c.getSession().write(MaplePacketCreator.clearInventoryItem(type, item.getPosition(), fromDrop));
	} else {
	    c.getSession().write(MaplePacketCreator.updateInventorySlot(type, (Item) item, fromDrop));
	}
    }

    public static void removeById(final MapleClient c, final MapleInventoryType type, final int itemId, final int quantity, final boolean fromDrop, final boolean consume) {
	int remremove = quantity;
	for (IItem item : c.getPlayer().getInventory(type).listById(itemId)) {
	    if (remremove <= item.getQuantity()) {
		removeFromSlot(c, type, item.getPosition(), (short) remremove, fromDrop, consume);
		remremove = 0;
		break;
	    } else {
		remremove -= item.getQuantity();
		removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume);
	    }
	}
	if (remremove > 0) {
	    throw new InventoryException("Not enough cheese available ( ItemID:" + itemId + ", Remove Amount:" + (quantity - remremove) + "| Current Amount:" + quantity + ")");
	}
    }

    public static void move(final MapleClient c, final MapleInventoryType type, final byte src, final byte dst) {
	if (src < 0 || dst < 0) {
	    return;
	}
	final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
	final IItem source = c.getPlayer().getInventory(type).getItem(src);
	final IItem initialTarget = c.getPlayer().getInventory(type).getItem(dst);
	if (source == null) {
	    return;
	}
	short olddstQ = -1;
	if (initialTarget != null) {
	    olddstQ = initialTarget.getQuantity();
	}
	final short oldsrcQ = source.getQuantity();
	final short slotMax = ii.getSlotMax(c, source.getItemId());
	c.getPlayer().getInventory(type).move(src, dst, slotMax);

	if (!type.equals(MapleInventoryType.EQUIP) && initialTarget != null && 
		initialTarget.getItemId() == source.getItemId() &&
		initialTarget.getExpiration() == source.getExpiration() &&
		!GameConstants.isThrowingStar(source.getItemId()) &&
		!GameConstants.isBullet(source.getItemId())) {
	    if ((olddstQ + oldsrcQ) > slotMax) {
		c.getSession().write(MaplePacketCreator.moveAndMergeWithRestInventoryItem(type, src, dst, (short) ((olddstQ + oldsrcQ) - slotMax), slotMax));
	    } else {
		c.getSession().write(MaplePacketCreator.moveAndMergeInventoryItem(type, src, dst, ((Item) c.getPlayer().getInventory(type).getItem(dst)).getQuantity()));
	    }
	} else {
	    c.getSession().write(MaplePacketCreator.moveInventoryItem(type, src, dst));
	}
    }

    public static void equip(final MapleClient c, final byte src, byte dst) {
	final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
	final MapleCharacter chr = c.getPlayer();
	final PlayerStats statst = c.getPlayer().getStat();
	Equip source = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(src);
	Equip target = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);

	if (source == null) {
	    c.getSession().write(MaplePacketCreator.enableActions());
	    return;
	}

	final Map<String, Integer> stats = ii.getEquipStats(source.getItemId());
	if (dst < -99 && stats.get("cash") == 0) {
	    c.getSession().write(MaplePacketCreator.enableActions());
	    return;
	}
	if (!ii.canEquip(stats, source.getItemId(), chr.getLevel(), chr.getJob(), chr.getFame(), statst.getTotalStr(), statst.getTotalDex(), statst.getTotalLuk(), statst.getTotalInt())) {
	    c.getSession().write(MaplePacketCreator.enableActions());
	    return;
	}
	if (GameConstants.isWeapon(source.getItemId()) && (dst != -10 && dst != -11)) {
	    ////AutobanManager.getInstance().autoban(c, "Equipment hack, itemid " + source.getItemId() + " to slot " + dst);
	    return;
	}
	if (GameConstants.isKatara(source.getItemId())) {
		dst = (byte)-10; //shield slot
	}
	if (GameConstants.isEvanDragonItem(source.getItemId()) && (chr.getJob() < 2200 || chr.getJob() > 2218)) {
		c.getSession().write(MaplePacketCreator.enableActions());
		return;
	}
	switch (dst) {
	    case -6: { // Top
		final IItem top = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
		if (top != null && GameConstants.isOverall(top.getItemId())) {
		    if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			c.getSession().write(MaplePacketCreator.getShowInventoryFull());
			return;
		    }
		    unequip(c, (byte) -5, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
		}
		break;
	    }
	    case -5: {
		final IItem top = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
		final IItem bottom = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -6);
		if (top != null && GameConstants.isOverall(source.getItemId())) {
		    if (chr.getInventory(MapleInventoryType.EQUIP).isFull(bottom != null && GameConstants.isOverall(source.getItemId()) ? 1 : 0)) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			c.getSession().write(MaplePacketCreator.getShowInventoryFull());
			return;
		    }
		    unequip(c, (byte) -5, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
		}
		if (bottom != null && GameConstants.isOverall(source.getItemId())) {
		    if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			c.getSession().write(MaplePacketCreator.getShowInventoryFull());
			return;
		    }
		    unequip(c, (byte) -6, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
		}
		break;
	    }
	    case -10: { // Weapon
		IItem weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
		if (GameConstants.isKatara(source.getItemId())) {
			if ((chr.getJob() != 900 && (chr.getJob() < 430 || chr.getJob() > 434)) || weapon == null || !GameConstants.isDagger(weapon.getItemId())) {
				c.getSession().write(MaplePacketCreator.getInventoryFull());
				c.getSession().write(MaplePacketCreator.getShowInventoryFull());
				return;
			}
		} else if (weapon != null && GameConstants.isTwoHanded(weapon.getItemId())) {
		    if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			c.getSession().write(MaplePacketCreator.getShowInventoryFull());
			return;
		    }
		    unequip(c, (byte) -11, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
		}
		break;
	    }
	    case -11: { // Shield
		IItem shield = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
		if (shield != null && GameConstants.isTwoHanded(source.getItemId())) {
		    if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			c.getSession().write(MaplePacketCreator.getShowInventoryFull());
			return;
		    }
		    unequip(c, (byte) -10, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
		}
		break;
	    }
	}
	source = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(src); // Equip
	target = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst); // Currently equipping

	chr.getInventory(MapleInventoryType.EQUIP).removeSlot(src);
	if (target != null) {
	    chr.getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
	}
	source.setPosition(dst);
	chr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
	if (target != null) {
	    target.setPosition(src);
	    chr.getInventory(MapleInventoryType.EQUIP).addFromDB(target);
	}
	if (chr.getBuffedValue(MapleBuffStat.BOOSTER) != null && GameConstants.isWeapon(source.getItemId())) {
	    chr.cancelBuffStats(MapleBuffStat.BOOSTER);
	}
	c.getSession().write(MaplePacketCreator.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 2));
	chr.equipChanged();

	if (stats.get("equipTradeBlock") == 1) { // Block trade when equipped.
	    byte flag = source.getFlag();
	    if (!ItemFlag.UNTRADEABLE.check(flag)) {
		flag |= ItemFlag.UNTRADEABLE.getValue();
		source.setFlag(flag);
		c.getSession().write(MaplePacketCreator.updateSpecialItemUse(target, GameConstants.getInventoryType(source.getItemId()).getType()));
	    }
	}
    }

    public static void unequip(final MapleClient c, final short src, final short dst) {
	Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(src);
	Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);

	if (dst < 0 || source == null) {
	    return;
	}
	if (target != null && src <= 0) { // do not allow switching with equip
	    c.getSession().write(MaplePacketCreator.getInventoryFull());
	    return;
	}
	c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
	if (target != null) {
	    c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(dst);
	}
	source.setPosition(dst);
	c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(source);
	if (target != null) {
	    target.setPosition(src);
	    c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
	}

	if (c.getPlayer().getBuffedValue(MapleBuffStat.BOOSTER) != null && GameConstants.isWeapon(source.getItemId())) {
	    c.getPlayer().cancelBuffStats(MapleBuffStat.BOOSTER);
	}

	c.getSession().write(MaplePacketCreator.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 1));
	c.getPlayer().equipChanged();
    }

    public static void drop(final MapleClient c, MapleInventoryType type, final short src, final short quantity) {
	final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
	if (src < 0) {
	    type = MapleInventoryType.EQUIPPED;
	}
	final IItem source = c.getPlayer().getInventory(type).getItem(src);
	if (quantity < 0 || source == null || GameConstants.isPet(source.getItemId()) || quantity == 0 && !GameConstants.isThrowingStar(source.getItemId()) && !GameConstants.isBullet(source.getItemId())) {
	    c.getSession().write(MaplePacketCreator.enableActions());
	    return;
	}
	final byte flag = source.getFlag();
	if (ItemFlag.LOCK.check(flag)) { // hack
	    c.getSession().write(MaplePacketCreator.enableActions());
	    return;
	}
	final Point dropPos = new Point(c.getPlayer().getPosition());

	if (quantity < source.getQuantity() && !GameConstants.isThrowingStar(source.getItemId()) && !GameConstants.isBullet(source.getItemId())) {
	    final IItem target = source.copy();
	    target.setQuantity(quantity);
	    source.setQuantity((short) (source.getQuantity() - quantity));
	    c.getSession().write(MaplePacketCreator.dropInventoryItemUpdate(type, source));

	    if (ii.isDropRestricted(target.getItemId())) {
		if (ItemFlag.KARMA_EQ.check(flag)) {
		    target.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
		    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
		} else if (ItemFlag.KARMA_USE.check(flag)) {
		    target.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
		    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
		} else {
		    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
		}
	    } else {
		if (ItemFlag.UNTRADEABLE.check(flag)) {
		    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
		} else {
		    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
		}
	    }
	} else {
	    c.getPlayer().getInventory(type).removeSlot(src);
	    c.getSession().write(MaplePacketCreator.dropInventoryItem((src < 0 ? MapleInventoryType.EQUIP : type), src));
	    if (src < 0) {
		c.getPlayer().equipChanged();
	    }
	    if (ii.isDropRestricted(source.getItemId())) {
		if (ItemFlag.KARMA_EQ.check(flag)) {
		    source.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
		    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
		} else if (ItemFlag.KARMA_USE.check(flag)) {
		    source.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
		    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
		} else {
		    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
		}
	    } else {
		if (ItemFlag.UNTRADEABLE.check(flag)) {
		    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
		} else {
		    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
		}
	    }
	}
    }
}
