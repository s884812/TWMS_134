package handling.channel.handler;

import java.util.Arrays;

import client.IItem;
import client.ItemFlag;
import client.GameConstants;
import client.MapleClient;
import client.MapleCharacter;
import client.MapleInventoryType;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import server.shops.MaplePlayerShop;
import server.shops.MaplePlayerShopItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.shops.AbstractPlayerStore;
import tools.MaplePacketCreator;
import tools.packet.PlayerShopPacket;
import tools.data.input.SeekableLittleEndianAccessor;

public class PlayerInteractionHandler {

	private static final byte CREATE = 0x00,
		INVITE_TRADE = 0x02,
		DENY_TRADE = 0x03,
		VISIT = 0x04,
		CHAT = 0x06,
		EXIT = 0x0A,
		OPEN = 0x0B,
		SET_ITEMS = 0x0E,
		SET_MESO = 0x0F,
		CONFIRM_TRADE = 0x10,
		MERCHANT_EXIT = 0x1C,
		ADD_ITEM = 0x1F,
		BUY_ITEM_STORE = 0x20,
		BUY_ITEM_HIREDMERCHANT = 0x22,
		REMOVE_ITEM = 0x24,
		MAINTANCE_OFF = 0x25,
		MAINTANCE_ORGANISE = 0x26,
		CLOSE_MERCHANT = 0x27,
		ADMIN_STORE_NAMECHANGE = 0x2B,
		VIEW_MERCHANT_VISITOR = 0x2C,
		VIEW_MERCHANT_BLACKLIST = 0x2D;

	public static final void PlayerInteraction(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final byte action = slea.readByte();

		switch (action) { // Mode
			case CREATE: {
				final byte createType = slea.readByte();
				if (createType == 1) { // omok
					// nvm
				} else if (createType == 3) { // trade
					MapleTrade.startTrade(chr);
				} else if (createType == 4 || createType == 5) { // shop
					if (chr.getMap().getMapObjectsInRange(chr.getPosition(), 19500, Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT)).size() != 0) {
						chr.dropMessage(1, "You may not establish a store here.");
						return;
					}
					final String desc = slea.readMapleAsciiString();
					slea.skip(3);
					final int itemId = slea.readInt();
					if (createType == 4) {
						chr.setPlayerShop(new MaplePlayerShop(chr, itemId, desc));
						c.getSession().write(PlayerShopPacket.getPlayerStore(chr, true));
					} else {
						final HiredMerchant merch = new HiredMerchant(chr, itemId, desc);
						chr.setPlayerShop(merch);
						c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merch, true));
					}
				}
				break;
			}
			case INVITE_TRADE: {
				MapleCharacter ochr = chr.getMap().getCharacterById_InMap(slea.readInt());
				if (ochr.getWorld() != chr.getWorld()) {
					chr.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Cannot find player"));
					return;
				}
				MapleTrade.inviteTrade(chr, ochr);
				break;
			}
			case DENY_TRADE: {
				MapleTrade.declineTrade(chr);
				break;
			}
			case VISIT: {
				if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
					MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
				} else {
					final MapleMapObject ob = chr.getMap().getMapObject(slea.readInt());

					if (ob instanceof IMaplePlayerShop && chr.getPlayerShop() == null) {
						final IMaplePlayerShop ips = (IMaplePlayerShop) ob;

						if (ob instanceof HiredMerchant) {
							final HiredMerchant merchant = (HiredMerchant) ips;
							if (merchant.isOwner(chr)) {
								merchant.setOpen(false);
								merchant.broadcastToVisitors(PlayerShopPacket.shopErrorMessage(0x0D, 1));
								merchant.removeAllVisitors((byte) 16, (byte) 0);
								chr.setPlayerShop(ips);
								c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
							} else {
								if (!merchant.isOpen()) {
									chr.dropMessage(1, "This shop is in maintenance, please come by later.");
								} else {
									if (ips.getFreeSlot() == -1) {
										chr.dropMessage(1, "This shop has reached it's maximum capacity, please come by later.");
									} else {
										chr.setPlayerShop(ips);
										merchant.addVisitor(chr);
										c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
									}
								}
							}
						} else if (ips.getShopType() == 2) {
							if (((MaplePlayerShop) ips).isBanned(chr.getName())) {
								chr.dropMessage(1, "You have been banned from this store.");
								return;
							}
						} else {
							if (ips.getFreeSlot() == -1) {
								chr.dropMessage(1, "This shop has reached it's maximum capacity, please come by later.");
							} else {
								chr.setPlayerShop(ips);
								ips.addVisitor(chr);
								c.getSession().write(PlayerShopPacket.getPlayerStore(chr, false));
							}
						}
					}
				}
				break;
			}
			case CHAT: {
				slea.readInt();
				if (chr.getTrade() != null) {
					chr.getTrade().chat(slea.readMapleAsciiString());
				} else if (chr.getPlayerShop() != null) {
					final IMaplePlayerShop ips = chr.getPlayerShop();
					final String message = slea.readMapleAsciiString();
					ips.broadcastToVisitors(PlayerShopPacket.shopChat(chr.getName() + " : " + message, ips.isOwner(chr) ? 0 : ips.getVisitorSlot(chr)));
				}
				break;
			}

			case EXIT: {
				if (chr.getTrade() != null) {
					MapleTrade.cancelTrade(chr.getTrade());
				} else {
					final IMaplePlayerShop ips = chr.getPlayerShop();
					if (ips == null) {
						return;
					}
					if (ips.isOwner(chr)) {
						if (ips.getShopType() == 2) {
							boolean save = false;
							for (MaplePlayerShopItem items : ips.getItems()) {
								if (items.bundles > 0) {
									if (MapleInventoryManipulator.addFromDrop(c, items.item, false)) {
										items.bundles = 0;
									} else {
										save = true;
										break;
									}
								}
							}
							ips.removeAllVisitors(3, 1);
							ips.closeShop(save, true);
						}
					} else {
						ips.removeVisitor(chr);
					}
					chr.setPlayerShop(null);
				}
				break;
			}
			case OPEN: {
				// c.getPlayer().haveItem(mode, 1, false, true)
				if (chr.getMap().allowPersonalShop()) {
					final IMaplePlayerShop shop = chr.getPlayerShop();
					if (shop != null && shop.isOwner(chr)) {
						chr.getMap().addMapObject((AbstractPlayerStore) shop);

						if (shop.getShopType() == 1) {
							final HiredMerchant merchant = (HiredMerchant) shop;
							merchant.setStoreid(c.getChannelServer().addMerchant(merchant));
							merchant.setOpen(true);
							chr.getMap().broadcastMessage(PlayerShopPacket.spawnHiredMerchant(merchant));
							chr.setPlayerShop(null);

						} else if (shop.getShopType() == 2) {
							chr.getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(chr));
						}
						slea.readByte();
					}
				} else {
					c.getSession().close();
				}
				break;
			}
			case SET_ITEMS: {
				final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
				final MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
				final IItem item = chr.getInventory(ivType).getItem((byte) slea.readShort());
				final short quantity = slea.readShort();
				final byte targetSlot = slea.readByte();

				if (chr.getTrade() != null && item != null) {
					if ((quantity <= item.getQuantity() && quantity >= 0) || GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
						final byte flag = item.getFlag();

						if (ItemFlag.UNTRADEABLE.check(flag) || ItemFlag.LOCK.check(flag)) {
							c.getSession().write(MaplePacketCreator.enableActions());
							return;
						}
						if (ii.isDropRestricted(item.getItemId())) {
							if (!(ItemFlag.KARMA_EQ.check(flag) || ItemFlag.KARMA_USE.check(flag))) {
								c.getSession().write(MaplePacketCreator.enableActions());
								return;
							}
						}
						IItem tradeItem = item.copy();
						if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
							tradeItem.setQuantity(item.getQuantity());
							MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), item.getQuantity(), true);
						} else {
							tradeItem.setQuantity(quantity);
							MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), quantity, true);
						}
						tradeItem.setPosition(targetSlot);
						chr.getTrade().addItem(tradeItem);
					}
				}
				break;
			}
			case SET_MESO: {
				final MapleTrade trade = chr.getTrade();
				if (trade != null) {
					trade.setMeso(slea.readInt());
				}
				break;
			}
			case CONFIRM_TRADE: {
				if (chr.getTrade() != null) {
					MapleTrade.completeTrade(chr);
				}
				break;
			}
			case MERCHANT_EXIT: {
				/*		final IMaplePlayerShop shop = chr.getPlayerShop();
				if (shop != null && shop instanceof HiredMerchant && shop.isOwner(chr)) {
				shop.setOpen(true);
				chr.setPlayerShop(null);
				}*/
				break;
			}
			case ADD_ITEM: {
				final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
				final byte slot = (byte) slea.readShort();
				final short bundles = slea.readShort(); // How many in a bundle
				final short perBundle = slea.readShort(); // Price per bundle
				final int price = slea.readInt();

				if (price <= 0 || bundles <= 0 || perBundle <= 0) {
					return;
				}
				final IMaplePlayerShop shop = chr.getPlayerShop();

				if (shop == null || !shop.isOwner(chr)) {
					return;
				}
				final IItem ivItem = chr.getInventory(type).getItem(slot);

				if (ivItem != null) {
					final short bundles_perbundle = (short) (bundles * perBundle);
					if (bundles_perbundle < 0) { // int_16 overflow
						return;
					}
					if (ivItem.getQuantity() >= bundles_perbundle) {
						final byte flag = ivItem.getFlag();

						if (ItemFlag.UNTRADEABLE.check(flag) || ItemFlag.LOCK.check(flag)) {
							c.getSession().write(MaplePacketCreator.enableActions());
							return;
						}
						if (MapleItemInformationProvider.getInstance().isDropRestricted(ivItem.getItemId())) {
							if (!(ItemFlag.KARMA_EQ.check(flag) || ItemFlag.KARMA_USE.check(flag))) {
								c.getSession().write(MaplePacketCreator.enableActions());
								return;
							}
						}
						if (GameConstants.isThrowingStar(ivItem.getItemId()) || GameConstants.isBullet(ivItem.getItemId())) {
							// Ignore the bundles
							MapleInventoryManipulator.removeFromSlot(c, type, slot, ivItem.getQuantity(), true);

							final IItem sellItem = ivItem.copy();
							shop.addItem(new MaplePlayerShopItem(sellItem, (short) 1, price));
						} else {
							MapleInventoryManipulator.removeFromSlot(c, type, slot, bundles_perbundle, true);

							final IItem sellItem = ivItem.copy();
							sellItem.setQuantity(perBundle);
							shop.addItem(new MaplePlayerShopItem(sellItem, bundles, price));
						}
						c.getSession().write(PlayerShopPacket.shopItemUpdate(shop));
					}
				}
				break;
			}
			case BUY_ITEM_STORE:
			case BUY_ITEM_HIREDMERCHANT: { // Buy and Merchant buy
				final int item = slea.readByte();
				final short quantity = slea.readShort();
				final IMaplePlayerShop shop = chr.getPlayerShop();

				if (shop == null || shop.isOwner(chr)) {
					return;
				}
				final MaplePlayerShopItem tobuy = shop.getItems().get(item);

				if (quantity < 0
						|| tobuy == null
						|| tobuy.bundles < quantity
						|| (tobuy.bundles % quantity != 0 && GameConstants.isEquip(tobuy.item.getItemId())) // Buying
						|| ((short) (tobuy.bundles * quantity)) < 0
						|| (quantity * tobuy.price) < 0
						|| quantity * tobuy.item.getQuantity() < 0
						|| chr.getMeso() - (quantity * tobuy.price) < 0
						|| shop.getMeso() + (quantity * tobuy.price) < 0) {
					return;
				}
				shop.buy(c, item, quantity);
				shop.broadcastToVisitors(PlayerShopPacket.shopItemUpdate(shop));
				break;
			}
			case REMOVE_ITEM: {
				final int slot = slea.readShort();
				final IMaplePlayerShop shop = chr.getPlayerShop();

				if (shop == null || !shop.isOwner(chr)) {
					return;
				}
				final MaplePlayerShopItem item = shop.getItems().get(slot);

				if (item != null) {
					if (item.bundles > 0) {
						IItem item_get = item.item.copy();
						item_get.setQuantity((short) (item.bundles * item.item.getQuantity()));
						if (MapleInventoryManipulator.addFromDrop(c, item_get, false)) {
							item.bundles = 0;
							shop.removeFromSlot(slot);
						}
					}
				}
				c.getSession().write(PlayerShopPacket.shopItemUpdate(shop));
				break;
			}
			case MAINTANCE_OFF: {
				final IMaplePlayerShop shop = chr.getPlayerShop();
				if (shop != null && shop instanceof HiredMerchant && shop.isOwner(chr)) {
					shop.setOpen(true);
					chr.setPlayerShop(null);
				}
				break;
			}
			case MAINTANCE_ORGANISE: {
				final IMaplePlayerShop imps = chr.getPlayerShop();
				if (imps.isOwner(chr)) {
					for (int i = 0; i < imps.getItems().size(); i++) {
						if (imps.getItems().get(i).bundles == 0) {
							imps.getItems().remove(i);
						}
					}
					if (chr.getMeso() + imps.getMeso() < 0) {
						c.getSession().write(PlayerShopPacket.shopItemUpdate(imps));
					} else {
						chr.gainMeso(imps.getMeso(), false);
						imps.setMeso(0);
						c.getSession().write(PlayerShopPacket.shopItemUpdate(imps));
					}
				}
				break;
			}
			case CLOSE_MERCHANT: {
				final IMaplePlayerShop merchant = chr.getPlayerShop();
				if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
					boolean save = false;

					if (chr.getMeso() + merchant.getMeso() < 0) {
						save = true;
					} else {
						if (merchant.getMeso() > 0) {
							chr.gainMeso(merchant.getMeso(), false);
						}
						merchant.setMeso(0);

						if (merchant.getItems().size() > 0) {
							for (MaplePlayerShopItem items : merchant.getItems()) {
								if (items.bundles > 0) {
									IItem item_get = items.item.copy();
									item_get.setQuantity((short) (items.bundles * items.item.getQuantity()));
									if (MapleInventoryManipulator.addFromDrop(c, item_get, false)) {
										items.bundles = 0;
									} else {
										save = true;
										break;
									}
								}
							}
						}
					}
					c.getSession().write(PlayerShopPacket.shopErrorMessage(0x10, 0));
					merchant.closeShop(save, true);
					chr.setPlayerShop(null);
				}
				break;
			}
			case ADMIN_STORE_NAMECHANGE: { // Changing store name, only Admin
				// 01 00 00 00
				break;
			}
			case VIEW_MERCHANT_VISITOR: {
				break;
			}
			case VIEW_MERCHANT_BLACKLIST: {
				break;
			}
			default: {
				System.out.println("Unhandled interaction action : " + action + ", " + slea.toString());
				break;
			}
		}
	}
}