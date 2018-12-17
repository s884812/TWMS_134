package handling.cashshop.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Calendar;

import client.GameConstants;
import client.MapleClient;
import client.MapleCharacter;
import client.MapleInventoryType;
import client.MaplePet;
import handling.cashshop.CashShopServer;
import handling.world.CharacterTransfer;
import handling.world.remote.CashShopInterface;
import server.CashItemFactory;
import server.CashItemInfo;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.packet.MTSCSPacket;
import tools.data.input.SeekableLittleEndianAccessor;

public class CashShopOperation {

	public static final void LeaveCS(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final CashShopServer cs = CashShopServer.getInstance();
		cs.getPlayerStorage().deregisterPlayer(chr);
		c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
		try {
			final CashShopInterface wci = cs.getCSInterface();
			wci.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), c.getChannel());
			final String ip = wci.getChannelIP(c.getChannel());
			final String[] socket = ip.split(":");
			c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
		} catch (RemoteException e) {
			c.getChannelServer().reconnectWorld();
		} catch (UnknownHostException e) {
			System.out.println(":: UnknownHostException found " + e + " ::");
		} finally {
			c.getSession().close();
			chr.saveToDB(false, true);
			c.setPlayer(null);
		}
	}

	public static final void EnterCS(final int playerid, final MapleClient c) {
		final CashShopServer cs = CashShopServer.getInstance();
		final CharacterTransfer transfer = cs.getPlayerStorage().getPendingCharacter(playerid);
		if (transfer == null) {
			c.getSession().close();
			return;
		}
                System.out.println("Test 1 OK!");
		MapleCharacter chr = MapleCharacter.ReconstructChr(transfer, c, false);
                System.out.println("Test 2 OK!");
		c.setPlayer(chr);
                System.out.println("Test 30 OK!");
		c.setAccID(chr.getAccountID());
                System.out.println("Test 31 OK!");
		if (!c.CheckIPAddress()) { // Remote hack
			c.getSession().close();
			return;
		}
		//final int state = c.getLoginState();
                final int state = MapleClient.CHANGE_CHANNEL;
		boolean allowLogin = false;
                System.out.println("Test 32 OK!");
		try {
			if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
				if (!cs.getCSInterface().isCharacterListConnected(c.loadCharacterNames(c.getWorld()))) {
					allowLogin = true;
				}
			}
		} catch (RemoteException e) {
			cs.reconnectWorld();
		}
                System.out.println("Test 33 OK!");
		if (!allowLogin) {
			c.setPlayer(null);
			c.getSession().close();
			return;
		}
                System.out.println("Test 34 OK!");
                System.out.println("Test 3 OK!");
		c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
		cs.getPlayerStorage().registerPlayer(chr);
                System.out.println("Test 4 OK!");
		c.getSession().write(MTSCSPacket.warpCS(c));
                System.out.println("Test 5 OK!");
		/*c.getSession().write(MTSCSPacket.enableUse0());
		c.getSession().write(MTSCSPacket.enableUse1());
		c.getSession().write(MTSCSPacket.enableUse2());
		c.getSession().write(MTSCSPacket.enableUse3());
		c.getSession().write(MTSCSPacket.enableUse4());*/
		//c.getSession().write(MTSCSPacket.showNXMapleTokens(chr));
		//c.getSession().write(MTSCSPacket.sendWishList(chr, false));
	}

	public static final void CSUpdate(final MapleClient c, final MapleCharacter chr) {
		c.getSession().write(MTSCSPacket.showNXMapleTokens(c.getPlayer()));
	}

	public static final void BuyCashItem(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
		final int action = slea.readByte();
		if (action == 3) {
			slea.skip(1);
			final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
			if (item != null && chr.getCSPoints(1) >= item.getPrice()) {
				if (chr.getInventory(GameConstants.getInventoryType(item.getId())).getNextFreeSlot() > -1) {
					chr.modifyCSPoints(1, -item.getPrice(), false);
					if (GameConstants.isPet(item.getId())) {
						final MaplePet pet = MaplePet.createPet(item.getId());
						if (pet != null) {
							MapleInventoryManipulator.addById(c, item.getId(), (short) 1, null, pet, item.getPeriod());
						}
					} else {
						MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), null, null, item.getPeriod());
					}
					c.getSession().write(MTSCSPacket.showBoughtCSItem(item.getId(), c.getAccID()));
					System.out.println(":: " + c.getAccID() + " has bought " + item.getId() + " from the cash shop ::");
				}
			}
		} else if (action == 5) { // Wishlist
			chr.clearWishlist();
			int[] wishlist = new int[10];
			for (int i = 0; i < 10; i++) {
				wishlist[i] = slea.readInt();
			}
			c.getSession().write(MTSCSPacket.sendWishList(chr, true));
		} else if (action == 6) { // Increase inv
			slea.skip(1);
			final boolean coupon = slea.readByte() > 0;
			if (coupon) {
				final MapleInventoryType type = getInventoryType(slea.readInt());
				if (chr.getCSPoints(1) >= 12000 && chr.getInventory(type).getSlotLimit() < 96) {
					chr.modifyCSPoints(1, -12000, false);
					chr.getInventory(type).addSlot((byte) 8);
					chr.dropMessage(1, "Inventory slot increased");
				} else {
					chr.dropMessage(1, "You have reached the Maxinum inventory slot.");
				}
			} else {
				final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
				if (chr.getCSPoints(1) >= 8000 && chr.getInventory(type).getSlotLimit() < 96) {
					chr.modifyCSPoints(1, -8000, false);
					chr.getInventory(type).addSlot((byte) 4);
					chr.dropMessage(1, "Inventory slot increased");
				} else {
					chr.dropMessage(1, "You have reached the Maxinum inventory slot.");
				}
			}
		} else if (action == 7) { // Increase slot space
			if (chr.getCSPoints(1) >= 8000 && chr.getStorage().getSlots() < 48) {
				chr.modifyCSPoints(1, -8000, false);
				chr.getStorage().increaseSlots((byte) 4);
				chr.dropMessage(1, "Storage slot increased");
			} else {
				chr.dropMessage(1, "You have reached the max storage slot.");
			}
		} else if (action == 14) { // transferFromCSToInv
			c.getSession().write(MaplePacketCreator.enableActions());
		} else if (action == 15) { // transferFromInvToCS
			c.getSession().write(MaplePacketCreator.enableActions());
		} else if (action == 30 || action == 36) {
			final int idate = slea.readInt();
			final int toCharge = slea.readInt();
			final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
			final String recipient = slea.readMapleAsciiString();
			final String msg = slea.readMapleAsciiString();
			final int year = idate / 10000;
			final int month = (idate - year * 10000) / 100;
			final int day = idate - year * 10000 - month * 100;
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			cal.set(year, month - 1, day);
		} else if (action == 31) { // cash package
			c.getSession().write(MaplePacketCreator.enableActions());
		} else if (action == 33) { // quest item
			final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
			if (item != null && chr.getMeso() >= item.getPrice()) {
				if (MapleItemInformationProvider.getInstance().isQuestItem(item.getId())) {
					if (chr.getInventory(GameConstants.getInventoryType(item.getId())).getNextFreeSlot() > -1) {
						chr.gainMeso(-item.getPrice(), false);
						MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount());
						c.getSession().write(MTSCSPacket.showBoughtCSQuestItem(chr.getInventory(MapleInventoryType.ETC).findById(item.getId()).getPosition(), item.getId()));
					}
				}
			}
		}
		c.getSession().write(MTSCSPacket.showNXMapleTokens(chr));
	}

	private static final MapleInventoryType getInventoryType(final int id) {
		switch (id) {
			case 50200075:
				return MapleInventoryType.EQUIP;
			case 50200074:
				return MapleInventoryType.USE;
			case 50200073:
				return MapleInventoryType.ETC;
			default:
				return MapleInventoryType.UNDEFINED;
		}
	}
}