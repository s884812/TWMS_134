package tools.packet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import client.MapleClient;
import client.MapleCharacter;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import handling.ServerConstants;
import handling.login.LoginServer;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.HexTool;

public class LoginPacket {

	public static final MaplePacket getHello(final short mapleVersion, final byte[] sendIv, final byte[] recvIv) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

		mplew.writeShort(14); // 0x0E
		mplew.writeShort(mapleVersion);
		mplew.writeMapleAsciiString("1"); // Revision
		mplew.write(recvIv);
		mplew.write(sendIv);
		mplew.write(6);

		return mplew.getPacket();
	}

	public static final MaplePacket getPing() {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

		mplew.writeShort(SendPacketOpcode.PING.getValue());

		return mplew.getPacket();
	}

	public static final MaplePacket RSA_KEY() {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

		mplew.writeShort(SendPacketOpcode.RSA_KEY.getValue());
		mplew.writeMapleAsciiString("30819F300D06092A864886F70D010101050003818D0030818902818100994F4E66B003A7843C944E67BE4375203DAA203C676908E59839C9BADE95F53E848AAFE61DB9C09E80F48675CA2696F4E897B7F18CCB6398D221C4EC5823D11CA1FB9764A78F84711B8B6FCA9F01B171A51EC66C02CDA9308887CEE8E59C4FF0B146BF71F697EB11EDCEBFCE02FB0101A7076A3FEB64F6F6022C8417EB6B87270203010001");

		return mplew.getPacket();
	}

	public static final MaplePacket LOGIN_KEY() {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

		mplew.writeShort(SendPacketOpcode.LOGIN_KEY.getValue());
                
		mplew.writeMapleAsciiString("MapLogin" + ServerConstants.MapLoginNumber);
                mplew.write(HexTool.getByteArrayFromHexString("12 86 DE 77"));
		return mplew.getPacket();
	}

	public static final MaplePacket getLoginFailed(final int reason) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

		/* 3: This is an ID that has been deleted or blocked from connection.
		 * 4: This is an incorrect password.
		 * 5: This is not a registered ID.
		 * 6: Trouble loggin into the game?
		 * 7: This is an ID that is already logged in, or the server is under inspection.
		 * 8: Trouble loggin into the game?
		 * 9: Trouble loggin into the game?
		 * 10: Could not be processed due to too many connection requests to the server.
		 * 11: Only those who are 20 years old or older can use this.
		 * 13: Unable to log-on as a master at IP.
		 * 17/19: Your Asiasoft Passport Account has been suspended.
		 * 23: Please enter @Key number.
		 * 24: You have entered incorrect @Key number.
		 * 25: You have entered incorrect @Key number for more than five times.
		 * 26: @Key system is under maintenance now.
		 * 28: This is an ID that has been deleted or blocked from connection.
		 * 29: You have either selected the wrong gateway, or you have yet to change your personal information. */

		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.writeInt(reason);
		mplew.writeShort(0);

		return mplew.getPacket();
	}

	public static final MaplePacket getPermBan(final byte reason) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.writeShort(2); // Account is banned
		mplew.write(0);
		mplew.write(reason);
		mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));

		return mplew.getPacket();
	}

	public static final MaplePacket getTempBan(final long timestampTill, final byte reason) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.write(2);
		mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00"));
		mplew.write(reason);
		mplew.writeLong(timestampTill); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.

		return mplew.getPacket();
	}
        
        public static final MaplePacket getSelectGender(final MapleClient client) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

		mplew.writeShort(SendPacketOpcode.SELECT_GENDER.getValue());
		mplew.writeMapleAsciiString(client.getAccountName());

		return mplew.getPacket();
	}
        
        public static final MaplePacket getSelectGenderSuccess(final MapleClient client) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

		mplew.writeShort(SendPacketOpcode.SELECT_GENDER_RESPONSE.getValue());
		mplew.writeMapleAsciiString(client.getAccountName());

		return mplew.getPacket();
	}

	public static final MaplePacket getAuthSuccessRequest(final MapleClient client, final String tpin) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.write(0);
		mplew.writeInt(client.getAccID());
		mplew.write(client.getGender());
		mplew.writeShort(client.isGm() ? 1 : 0); // Admin byte
		mplew.writeInt(0);
                mplew.writeMapleAsciiString(client.getAccountName());
		mplew.write(HexTool.getByteArrayFromHexString("B2 84 0C 00"));
                mplew.writeInt(1);
		mplew.writeZeroBytes(16);
                
		return mplew.getPacket();
	}

	public static final MaplePacket deleteCharResponse(final int cid, final int state) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
		mplew.writeInt(cid);
		mplew.write(state);

		return mplew.getPacket();
	}

	public static final MaplePacket secondPwError(final byte mode) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

		mplew.writeShort(SendPacketOpcode.SECONDPW_ERROR.getValue());
		mplew.write(mode);

		return mplew.getPacket();
	}

	public static final MaplePacket getServerList(final int serverId, final String serverName, final Map<Integer, Integer> channelLoad) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
		mplew.write(serverId);
		mplew.writeMapleAsciiString(serverName);
		mplew.write(LoginServer.getInstance().getFlag());
		mplew.writeMapleAsciiString(LoginServer.getInstance().getEventMessage());
		mplew.writeShort(100);
		mplew.writeShort(100);
		int lastChannel = 1;
		Set<Integer> channels = channelLoad.keySet();
		for (int i = 30; i > 0; i--) {
			if (channels.contains(i)) {
				lastChannel = i;
				break;
			}
		}
		mplew.write(lastChannel);
		int load;
		for (int i = 1; i <= lastChannel; i++) {
			if (channels.contains(i)) {
				load = channelLoad.get(i);
			} else {
				load = 1200;
			}
			mplew.writeMapleAsciiString(serverName + "-" + i);
			mplew.writeInt(load);
			mplew.write(serverId);
			mplew.writeShort(i - 1);
		}
		mplew.writeZeroBytes(6);

		return mplew.getPacket();
	}

	public static final MaplePacket getEndOfServerList() {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
		mplew.write(0xFF);

		return mplew.getPacket();
	}

	public static final MaplePacket getServerStatus(final int status) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
		mplew.writeShort(status);

		return mplew.getPacket();
	}

	public static final MaplePacket getCharList(final int status, final List<MapleCharacter> chars) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
		mplew.write(status);
                mplew.write(HexTool.getByteArrayFromHexString("40 42 0F 00"));
		mplew.write(chars.size());
		for (final MapleCharacter chr : chars) {
			addCharEntry(mplew, chr);
		}
		mplew.writeShort(3);
		mplew.writeInt(LoginServer.getInstance().getMaxCharacters());
                mplew.writeLong(0);
                mplew.writeLong(0);

		return mplew.getPacket();
	}

	public static final MaplePacket addNewCharEntry(final MapleCharacter chr, final boolean worked) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
		mplew.write(worked ? 0 : 1);
		addCharEntry(mplew, chr);

		return mplew.getPacket();
	}

	public static final MaplePacket charNameResponse(final String charname, final boolean nameUsed) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
		mplew.writeMapleAsciiString(charname);
		mplew.write(nameUsed ? 1 : 0);

		return mplew.getPacket();
	}

	private static final void addCharEntry(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
		PacketHelper.addCharStats(mplew, chr);
		PacketHelper.addCharLook(mplew, chr, true);
		mplew.writeShort(0);
	}
}