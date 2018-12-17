package handling.login.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Calendar;

import client.IItem;
import client.Item;
import client.LoginCrypto;
import client.MapleClient;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleInventory;
import client.MapleInventoryType;
import handling.channel.ChannelServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.login.LoginWorker;
import tools.MaplePacketCreator;
import tools.packet.LoginPacket;
import tools.KoreanDateUtil;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharLoginHandler {

	private static final boolean loginFailCount(final MapleClient c) {
		c.loginAttempt++;
		if (c.loginAttempt > 5) {
			return true;
		}
		return false;
	}

	public static final void login(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		final String login = slea.readMapleAsciiString();
		final String pwd = slea.readMapleAsciiString();
		c.setAccountName(login);
		final boolean ipBan = c.hasBannedIP();
		final boolean macBan = false; // MSEA doesn't sent mac
		int loginok = c.login(login, pwd, ipBan || macBan);
		final Calendar tempbannedTill = c.getTempBanCalendar();
		if (loginok == 0 && (ipBan || macBan)) {
			loginok = 3;
			if (macBan) {
				MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Enforcing account ban, account " + login, false);
			}
		}
		if (loginok != 0) {
			if (!loginFailCount(c)) {
				c.getSession().write(LoginPacket.getLoginFailed(loginok));
			}
		} else if (tempbannedTill.getTimeInMillis() != 0) {
			if (!loginFailCount(c)) {
				c.getSession().write(LoginPacket.getTempBan(KoreanDateUtil.getTempBanTimestamp(tempbannedTill.getTimeInMillis()), c.getBanReason()));
			}
		} else {
                        if (c.getGender() != 0 && c.getGender() != 1) {
                            c.getSession().write(LoginPacket.getSelectGender(c));
                        } else {
			c.loginAttempt = 0;
			LoginWorker.registerClient(c);
                        }
		}
	}
        
        public static final void SelectGender(final SeekableLittleEndianAccessor slea, final MapleClient c) {
                String AccountName = slea.readMapleAsciiString();
                String Password = slea.readMapleAsciiString();
                int gender = slea.readByte();
                c.setSelectGender(Password, gender);
		c.getSession().write(LoginPacket.getSelectGenderSuccess(c));
	}

	public static final void ServerListRequest(final MapleClient c) {
		final LoginServer ls = LoginServer.getInstance();
		c.getSession().write(LoginPacket.getServerList(18, "MapleTW", ls.getLoad()));
		c.getSession().write(LoginPacket.getEndOfServerList());
	}

	public static final void ServerStatusRequest(final MapleClient c) {
		
	}

	public static final void CharlistRequest(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		slea.skip(1);
                final int server = slea.readByte();
		final int channel = slea.readByte() + 1;
                int numPlayer = 0;
                int status = 1;
		for (ChannelServer cserv : ChannelServer.getAllInstances()) {
			numPlayer += cserv.getPlayerStorage().getConnectedClients();
		}
		final int userLimit = LoginServer.getInstance().getUserLimit();
		if (numPlayer >= userLimit) {
			status = 2;
		} else if (numPlayer * 2 >= userLimit) {
			status = 1;
		} else {
			status = 0;
		}
		c.setWorld(server);
		System.out.println(":: Client is connecting to server " + server + " channel " + channel + " ::");
		c.setChannel(channel);
		final List<MapleCharacter> chars = c.loadCharacters(server);
		if (chars != null) {
			c.getSession().write(LoginPacket.getCharList(status, chars));
		} else {
			c.getSession().close();
		}
	}

	public static final void CheckCharName(final String name, final MapleClient c) {
		c.getSession().write(LoginPacket.charNameResponse(name,
				!MapleCharacterUtil.canCreateChar(name) || LoginInformationProvider.getInstance().isForbiddenName(name)));
	}

	public static final void CreateChar(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		final String name = slea.readMapleAsciiString();
		final int JobType = slea.readInt();
		final short db = slea.readShort();
		final int face = slea.readInt();
		final int hair = slea.readInt();
		final int hairColor = 0;
		final int skinColor = 0;
		final int top = slea.readInt();
		final int bottom = slea.readInt();
		final int shoes = slea.readInt();
		final int weapon = slea.readInt();
		final byte gender = c.getGender();

		MapleCharacter newchar = MapleCharacter.getDefault(c, JobType);
		newchar.setWorld(c.getWorld());
		newchar.setFace(face);
		newchar.setHair(hair + hairColor);
		newchar.setGender(gender);
		newchar.setName(name);
		newchar.setSkinColor(skinColor);
                if (JobType == 0) {
                        newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161054, (short) 1, (short) 1, (byte) 0));
                } else if (JobType == 1) { // Adventurer
                        newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (short) 1, (short) 1, (byte) 0));
                } else if (JobType == 2) {  // Knights of Cygnus
                        newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (short) 1, (short) 1, (byte) 0));
                } else if (JobType == 3) { // Aran
                        newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (short) 1, (short) 1, (byte) 0));
                } else if (JobType == 4) { // Evan
                        newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161052, (short) 1, (short) 1, (byte) 0));
                }
		MapleInventory equipped = newchar.getInventory(MapleInventoryType.EQUIPPED);
		final LoginInformationProvider li = LoginInformationProvider.getInstance();
		IItem item = li.getEquipById(top);
		item.setPosition((byte) -5);
		equipped.addFromDB(item);
		if (JobType != 0) { // Resistance is 0 bottom, will cause bug if input 0
			item = li.getEquipById(bottom);
			item.setPosition((byte) -6);
			equipped.addFromDB(item);
		}
		item = li.getEquipById(shoes);
		item.setPosition((byte) -7);
		equipped.addFromDB(item);
		item = li.getEquipById(weapon);
		item.setPosition((byte) -11);
		equipped.addFromDB(item);

		if (MapleCharacterUtil.canCreateChar(name) && !li.isForbiddenName(name)) {
			MapleCharacter.saveNewCharToDB(newchar, JobType, JobType == 1 && db > 0);
			c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
			c.createdChar(newchar.getId());
		} else {
			c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
		}
	}

	public static final void DeleteChar(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		String Secondpw_Client = null;
		if (slea.readByte() >= 0) {
			Secondpw_Client = slea.readMapleAsciiString();
		}
		//final String AS13Digit = slea.readMapleAsciiString();
		final int Character_ID = slea.readInt();
		if (!c.login_Auth(Character_ID)) {
			c.getSession().close();
			return;
		}
		byte state = 0;
		if (c.getSecondPassword() != null) {
			if (Secondpw_Client == null) {
				c.getSession().close();
				return;
			} else {
				if (!c.CheckSecondPassword(Secondpw_Client)) {
					state = 16;
				}
			}
		}
		if (state == 0) {
			if (!c.deleteCharacter(Character_ID)) {
				state = 1;
			}
		}
		c.getSession().write(LoginPacket.deleteCharResponse(Character_ID, state));
	}

	public static final void Character_WithoutSecondPassword(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		//slea.skip(1);
		final int charId = slea.readInt();
		/*final String currentpw = c.getSecondPassword();
		if (slea.available() != 0) {
			if (currentpw != null) { // Hack
				c.getSession().close();
				return;
			}
			final String setpassword = slea.readMapleAsciiString();
			if (setpassword.length() >= 4 && setpassword.length() <= 16) {
				c.setSecondPassword(setpassword);
				c.updateSecondPassword();
				if (!c.login_Auth(charId)) {
					c.getSession().close();
					return;
				}
			} else {
				c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
				return;
			}
		} else if (loginFailCount(c) || currentpw != null || !c.login_Auth(charId)) {
			c.getSession().close();
			return;
		}*/
		try {
			if (c.getIdleTask() != null) {
				c.getIdleTask().cancel(true);
			}
			c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
                        
			final String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getSession().getRemoteAddress().toString().replace("/", "").split(":")[0], c.getChannel());
                        if (channelServerIP.equals("0.0.0.0")) {
				String[] socket = LoginServer.getInstance().getIP(c.getChannel()).split(":");
                                c.getSession().write(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
			} else {
				final String[] socket = LoginServer.getInstance().getIP(c.getChannel()).split(":");
				c.getSession().write(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(socket[1]), charId));
			}
		} catch (UnknownHostException e) {
			System.err.println(":: UnknownHostException found " + e + " ::");
		}
	}

	public static final void Character_WithSecondPassword(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		final String password = slea.readMapleAsciiString();
		final int charId = slea.readInt();
		if (loginFailCount(c) || c.getSecondPassword() == null || !c.login_Auth(charId)) {
			c.getSession().close();
			return;
		}
		if (c.CheckSecondPassword(password)) {
			try {
				if (c.getIdleTask() != null) {
					c.getIdleTask().cancel(true);
				}
				c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());

				final String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getSession().getRemoteAddress().toString().replace("/", "").split(":")[0], c.getChannel());
				if (channelServerIP.equals("0.0.0.0")) {
					final String[] socket = LoginServer.getInstance().getIP(c.getChannel()).split(":");
					c.getSession().write(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
				} else {
					final String[] socket = LoginServer.getInstance().getIP(c.getChannel()).split(":");
					c.getSession().write(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(socket[1]), charId));
				}
			} catch (UnknownHostException e) {
				System.err.println(":: UnknownHostException found " + e + " ::");
			}
		} else {
			c.getSession().write(LoginPacket.secondPwError((byte) 14));
		}
	}
}