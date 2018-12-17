package client;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.io.Serializable;

import javax.script.ScriptEngine;

import database.DatabaseConnection;
import database.DatabaseException;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.MapleMessengerCharacter;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.guild.MapleGuildCharacter;
import scripting.NPCScriptManager;
import server.MapleTrade;
import server.TimerManager;
import server.shops.IMaplePlayerShop;
import tools.FileoutputUtil;
import tools.IPAddressTool;
import tools.MapleAESOFB;
import tools.packet.LoginPacket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.common.IoSession;

public class MapleClient implements Serializable {

	private static final long serialVersionUID = 9179541993413738569L;
	public static final transient byte LOGIN_NOTLOGGEDIN = 0,
		LOGIN_SERVER_TRANSITION = 1,
		LOGIN_LOGGEDIN = 2,
		LOGIN_WAITING = 3,
		CASH_SHOP_TRANSITION = 4,
		LOGIN_CS_LOGGEDIN = 5,
		CHANGE_CHANNEL = 6;
	public static final String CLIENT_KEY = "CLIENT";
	private transient MapleAESOFB send, receive;
	private transient IoSession session;
	private MapleCharacter player;
	private int channel = 1, accId = 1, world;
	private boolean loggedIn = false, serverTransition = false;
	private transient Calendar birthday = null, tempban = null;
	private String accountName;
	private transient long lastPong;
	private boolean gm;
	private byte greason = 1, gender = -1;
	public transient short loginAttempt = 0;
	private transient List<Integer> allowedChar = new LinkedList<Integer>();
	private transient Set<String> macs = new HashSet<String>();
	private transient Map<String, ScriptEngine> engines = new HashMap<String, ScriptEngine>();
	private transient ScheduledFuture<?> idleTask = null;
	private transient String secondPassword, salt2; // To be used only on login
	private final transient Lock mutex = new ReentrantLock(true);

	public MapleClient(MapleAESOFB send, MapleAESOFB receive, IoSession session) {
	this.send = send;
	this.receive = receive;
	this.session = session;
	}

	public final MapleAESOFB getReceiveCrypto() {
	return receive;
	}

	public final MapleAESOFB getSendCrypto() {
	return send;
	}

	public final IoSession getSession() {
	return session;
	}

	public final Lock getLock() {
	return mutex;
	}

	public MapleCharacter getPlayer() {
	return player;
	}

	public void setPlayer(MapleCharacter player) {
	this.player = player;
	}

	public void createdChar(final int id) {
	allowedChar.add(id);
	}

	public final boolean login_Auth(final int id) {
	return allowedChar.contains(id);
	}

	public final List<MapleCharacter> loadCharacters(final int serverId) { // TODO make this less costly zZz
	final List<MapleCharacter> chars = new LinkedList<MapleCharacter>();

	for (final CharNameAndId cni : loadCharactersInternal(serverId)) {
		final MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, this, false);
		chars.add(chr);
		allowedChar.add(chr.getId());
	}
	return chars;
	}

	public List<String> loadCharacterNames(int serverId) {
	List<String> chars = new LinkedList<String>();
	for (CharNameAndId cni : loadCharactersInternal(serverId)) {
		chars.add(cni.name);
	}
	return chars;
	}

	private List<CharNameAndId> loadCharactersInternal(int serverId) {
	List<CharNameAndId> chars = new LinkedList<CharNameAndId>();
	try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ? AND world = ?");
		ps.setInt(1, accId);
		ps.setInt(2, serverId);

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
		chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
		}
		rs.close();
		ps.close();
	} catch (SQLException e) {
		System.err.println("error loading characters internal" + e);
	}
	return chars;
	}

	public boolean isLoggedIn() {
	return loggedIn;
	}

	private Calendar getTempBanCalendar(ResultSet rs) throws SQLException {
	Calendar lTempban = Calendar.getInstance();
	if (rs.getLong("tempban") == 0) { // basically if timestamp in db is 0000-00-00
		lTempban.setTimeInMillis(0);
		return lTempban;
	}
	Calendar today = Calendar.getInstance();
	lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
	if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
		return lTempban;
	}

	lTempban.setTimeInMillis(0);
	return lTempban;
	}

	public Calendar getTempBanCalendar() {
	return tempban;
	}

	public byte getBanReason() {
	return greason;
	}

	public boolean hasBannedIP() {
	boolean ret = false;
	try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')");
		ps.setString(1, session.getRemoteAddress().toString());
		ResultSet rs = ps.executeQuery();
		rs.next();
		if (rs.getInt(1) > 0) {
		ret = true;
		}
		rs.close();
		ps.close();
	} catch (SQLException ex) {
		System.err.println("Error checking ip bans" + ex);
	}
	return ret;
	}

	public boolean hasBannedMac() {
	if (macs.isEmpty()) {
		return false;
	}
	boolean ret = false;
	int i = 0;
	try {
		Connection con = DatabaseConnection.getConnection();
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");
		for (i = 0; i < macs.size(); i++) {
		sql.append("?");
		if (i != macs.size() - 1) {
			sql.append(", ");
		}
		}
		sql.append(")");
		PreparedStatement ps = con.prepareStatement(sql.toString());
		i = 0;
		for (String mac : macs) {
		i++;
		ps.setString(i, mac);
		}
		ResultSet rs = ps.executeQuery();
		rs.next();
		if (rs.getInt(1) > 0) {
		ret = true;
		}
		rs.close();
		ps.close();
	} catch (SQLException ex) {
		System.err.println("Error checking mac bans" + ex);
	}
	return ret;
	}

	private void loadMacsIfNescessary() throws SQLException {
	if (macs.isEmpty()) {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT macs FROM accounts WHERE id = ?");
		ps.setInt(1, accId);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
		String[] macData = rs.getString("macs").split(", ");
		for (String mac : macData) {
			if (!mac.equals("")) {
			macs.add(mac);
			}
		}
		} else {
		throw new RuntimeException("No valid account associated with this client.");
		}
		rs.close();
		ps.close();
	}
	}

	public void banMacs() {
	Connection con = DatabaseConnection.getConnection();
	try {
		loadMacsIfNescessary();
		List<String> filtered = new LinkedList<String>();
		PreparedStatement ps = con.prepareStatement("SELECT filter FROM macfilters");
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
		filtered.add(rs.getString("filter"));
		}
		rs.close();
		ps.close();

		ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)");
		for (String mac : macs) {
		boolean matched = false;
		for (String filter : filtered) {
			if (mac.matches(filter)) {
			matched = true;
			break;
			}
		}
		if (!matched) {
			ps.setString(1, mac);
			try {
			ps.executeUpdate();
			} catch (SQLException e) {
			// can fail because of UNIQUE key, we dont care
			}
		}
		}
		ps.close();
	} catch (SQLException e) {
		System.err.println("Error banning MACs" + e);
	}
	}

	/**
	 * Returns 0 on success, a state to be used for
	 * {@link MaplePacketCreator#getLoginFailed(int)} otherwise.
	 *
	 * @param success
	 * @return The state of the login.
	 */
	public int finishLogin() {
	synchronized (MapleClient.class) {
		final byte state = getLoginState();
		if (state > MapleClient.LOGIN_NOTLOGGEDIN && state != MapleClient.LOGIN_WAITING) { // already loggedin
		loggedIn = false;
		return 7;
		}
		updateLoginState(MapleClient.LOGIN_LOGGEDIN, null);
	}
	return 0;
	}

	public int login(String login, String pwd, boolean ipMacBanned) {
	int loginok = 5;
	try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
		ps.setString(1, login);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
		final int banned = rs.getInt("banned");
		final String passhash = rs.getString("password");
		final String salt = rs.getString("salt");

		accId = rs.getInt("id");
		secondPassword = rs.getString("2ndpassword");
		salt2 = rs.getString("salt2");
		gm = rs.getInt("gm") > 0;
		greason = rs.getByte("greason");
		tempban = getTempBanCalendar(rs);
		gender = rs.getByte("gender");

		/*if (secondPassword != null && salt2 != null) {
			secondPassword = LoginCrypto.rand_r(secondPassword);
		}*/
		ps.close();

		if (banned > 0) {
			loginok = 3;
		} else {
			if (banned == -1) {
			unban();
			}
			byte loginstate = getLoginState();
			if (loginstate > MapleClient.LOGIN_NOTLOGGEDIN) { // already loggedin
			loggedIn = false;
			loginok = 7;
			} else {
			//boolean updatePasswordHash = false;
			// Check if the passwords are correct here. :B
			//if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(pwd, passhash)) {
                        if (passhash.equals(pwd)) {
				// Check if a password upgrade is needed.
				loginok = 0;
			/*	updatePasswordHash = true;
			} else if (salt == null && LoginCrypto.checkSha1Hash(passhash, pwd)) {
				loginok = 0;
				updatePasswordHash = true;
			} else if (LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt)) {
				loginok = 0;*/
			} else {
				loggedIn = false;
				loginok = 4;
			}
			/*if (updatePasswordHash) {
				PreparedStatement pss = con.prepareStatement("UPDATE `accounts` SET `password` = ?, `salt` = ? WHERE id = ?");
				try {
				final String newSalt = LoginCrypto.makeSalt();
				pss.setString(1, LoginCrypto.makeSaltedSha512Hash(pwd, newSalt));
				pss.setString(2, newSalt);
				pss.setInt(3, accId);
				pss.executeUpdate();
				} finally {
				pss.close();
				}
			}*/
			}
		}
		}
		rs.close();
		ps.close();
	} catch (SQLException e) {
		System.err.println("ERROR" + e);
	}
	return loginok;
	}

	public boolean CheckSecondPassword(String in) {
	boolean allow = false;
	boolean updatePasswordHash = false;

	// Check if the passwords are correct here. :B
	if (in.equals(secondPassword)) {
		// Check if a password upgrade is needed.
		allow = true;
		//updatePasswordHash = true;
	/*} else if (salt2 == null && LoginCrypto.checkSha1Hash(secondPassword, in)) {
		allow = true;
		updatePasswordHash = true;
	} else if (LoginCrypto.checkSaltedSha512Hash(secondPassword, in, salt2)) {
		allow = true;*/
	}
	/*if (updatePasswordHash) {
		Connection con = DatabaseConnection.getConnection();
		try {
		PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `salt2` = ? WHERE id = ?");
		final String newSalt = LoginCrypto.makeSalt();
		ps.setString(1, LoginCrypto.rand_s(LoginCrypto.makeSaltedSha512Hash(in, newSalt)));
		ps.setString(2, newSalt);
		ps.setInt(3, accId);
		ps.executeUpdate();
		ps.close();
		} catch (SQLException e) {
		return false;
		}
	}*/
	return allow;
	}
        
        public boolean setSelectGender(String pwd, int gender) {
        boolean allow = false;
		
        Connection con = DatabaseConnection.getConnection();
		try {
		PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `gender` = ? WHERE id = ?");
		ps.setString(1, pwd);
		ps.setInt(2, gender);
		ps.setInt(3, accId);
		ps.executeUpdate();
		ps.close();
		} catch (SQLException e) {
		return false;
		}
	return allow;
	}

	/**
	 * Gets the special server IP if the client matches a certain subnet.
	 *
	 * @param subnetInfo A <code>Properties</code> instance containing all the subnet info.
	 * @param clientIPAddress The IP address of the client as a dotted quad.
	 * @param channel The requested channel to match with the subnet.
	 * @return <code>0.0.0.0</code> if no subnet matched, or the IP if the subnet matched.
	 */
	public static String getChannelServerIPFromSubnet(String clientIPAddress, int channel) {
	long ipAddress = IPAddressTool.dottedQuadToLong(clientIPAddress);
	Properties subnetInfo = LoginServer.getInstance().getSubnetInfo();

	if (subnetInfo.contains("net.login.subnetcount")) {
		int subnetCount = Integer.parseInt(subnetInfo.getProperty("net.login.subnetcount"));
		for (int i = 0; i < subnetCount; i++) {
		String[] connectionInfo = subnetInfo.getProperty("net.login.subnet." + i).split(":");
		long subnet = IPAddressTool.dottedQuadToLong(connectionInfo[0]);
		long channelIP = IPAddressTool.dottedQuadToLong(connectionInfo[1]);
		int channelNumber = Integer.parseInt(connectionInfo[2]);

		if (((ipAddress & subnet) == (channelIP & subnet)) && (channel == channelNumber)) {
			return connectionInfo[1];
		}
		}
	}
	return "0.0.0.0";
	}

	private void unban() {
	try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?");
		ps.setInt(1, accId);
		ps.executeUpdate();
		ps.close();
	} catch (SQLException e) {
		System.err.println("Error while unbanning" + e);
	}
	}

	public byte unban(String charname) {
	try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
		ps.setString(1, charname);

		ResultSet rs = ps.executeQuery();
		if (!rs.next()) {
		return -1;
		}
		final int accid = rs.getInt(1);
		rs.close();
		ps.close();

		ps = con.prepareStatement("UPDATE accounts SET banned = 0 and banreason = '' WHERE id = ?");
		ps.setInt(1, accid);
		ps.executeUpdate();
		ps.close();
	} catch (SQLException e) {
		System.err.println("Error while unbanning" + e);
		return -2;
	}
	return 0;
	}

	public void updateMacs(String macData) {
	for (String mac : macData.split(", ")) {
		macs.add(mac);
	}
	StringBuilder newMacData = new StringBuilder();
	Iterator<String> iter = macs.iterator();
	while (iter.hasNext()) {
		newMacData.append(iter.next());
		if (iter.hasNext()) {
		newMacData.append(", ");
		}
	}
	try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?");
		ps.setString(1, newMacData.toString());
		ps.setInt(2, accId);
		ps.executeUpdate();
		ps.close();
	} catch (SQLException e) {
		System.err.println("Error saving MACs" + e);
	}
	}

	public void setAccID(int id) {
	this.accId = id;
	}

	public int getAccID() {
	return this.accId;
	}

	public final void updateLoginState(final int newstate, final String SessionID) { // TODO hide?
	try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?");
		ps.setInt(1, newstate);
		ps.setString(2, SessionID);
		ps.setInt(3, getAccID());
		ps.executeUpdate();
		ps.close();
	} catch (SQLException e) {
		System.err.println("error updating login state" + e);
	}
	if (newstate == MapleClient.LOGIN_NOTLOGGEDIN || newstate == MapleClient.LOGIN_WAITING) {
		loggedIn = false;
		serverTransition = false;
	} else {
		serverTransition = (newstate == MapleClient.LOGIN_SERVER_TRANSITION || newstate == MapleClient.CHANGE_CHANNEL);
		loggedIn = !serverTransition;
	}
	}

	public final void updateSecondPassword() {
	try {
		final Connection con = DatabaseConnection.getConnection();

		PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `salt2` = ? WHERE id = ?");
		final String newSalt = LoginCrypto.makeSalt();
		ps.setString(1, LoginCrypto.rand_s(LoginCrypto.makeSaltedSha512Hash(secondPassword, newSalt)));
		ps.setString(2, newSalt);
		ps.setInt(3, accId);
		ps.executeUpdate();
		ps.close();

	} catch (SQLException e) {
		System.err.println("error updating login state" + e);
	}
	}

	public final byte getLoginState() { // TODO hide?
	Connection con = DatabaseConnection.getConnection();
	try {
		PreparedStatement ps;
		ps = con.prepareStatement("SELECT loggedin, lastlogin, UNIX_TIMESTAMP(birthday) as birthday FROM accounts WHERE id = ?");
		ps.setInt(1, getAccID());
		ResultSet rs = ps.executeQuery();
		if (!rs.next()) {
		ps.close();
		throw new DatabaseException("Everything sucks");
		}
		birthday = Calendar.getInstance();
		long blubb = rs.getLong("birthday");
		if (blubb > 0) {
		birthday.setTimeInMillis(blubb * 1000);
		}
		byte state = rs.getByte("loggedin");
                System.out.println(">> state : " + state);
		if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
		if (rs.getTimestamp("lastlogin").getTime() + 20000 < System.currentTimeMillis()) { // connecting to chanserver timeout
			state = MapleClient.LOGIN_NOTLOGGEDIN;
			updateLoginState(state, null);
		}
		}
		rs.close();
		ps.close();
                System.out.println(">> state : " + state);
		if (state == MapleClient.LOGIN_LOGGEDIN) {
		loggedIn = true;
		} else {
		loggedIn = false;
		}
                System.out.println(">> state : " + state);
		return state;
	} catch (SQLException e) {
		loggedIn = false;
		throw new DatabaseException("error getting login state", e);
	}
	}

	public final boolean checkBirthDate(final Calendar date) {
	if (date.get(Calendar.YEAR) == birthday.get(Calendar.YEAR) && date.get(Calendar.MONTH) == birthday.get(Calendar.MONTH) && date.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH)) {
		return true;
	}
	return false;
	}

	public final void removalTask() {
	try {
		if (!player.getAllBuffs().isEmpty()) {
		player.cancelAllBuffs_();
		}
		if (!player.getAllDiseases().isEmpty()) {
		player.cancelAllDebuffs();
		}
		if (player.getTrade() != null) {
		MapleTrade.cancelTrade(player.getTrade());
		}
		NPCScriptManager.getInstance().dispose(this);

		if (player.getEventInstance() != null) {
		player.getEventInstance().playerDisconnected(player);
		}
		player.getCheatTracker().dispose();
		if (player.getMap() != null) {
		player.getMap().removePlayer(player);
		}

		final IMaplePlayerShop shop = player.getPlayerShop();
		if (shop != null) {
		shop.removeVisitor(player);
		if (shop.isOwner(player)) {
			shop.setOpen(true);
		}
		}
	} catch (final Throwable e) {
		FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
	}
	}

	public final void disconnect(final boolean RemoveInChannelServer, final boolean fromCS) {
	if (player != null && isLoggedIn()) {
		removalTask();

		player.saveToDB(true, fromCS);


		if (!fromCS) {
		final ChannelServer ch = ChannelServer.getInstance(channel);

		try {
			if (player.getMessenger() != null) {
			ch.getWorldInterface().leaveMessenger(player.getMessenger().getId(), new MapleMessengerCharacter(player));
			player.setMessenger(null);
			}
			if (player.getParty() != null) {
			final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
			chrp.setOnline(false);
			ch.getWorldInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, chrp);
			}
			if (!serverTransition && isLoggedIn()) {
			ch.getWorldInterface().loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
			} else { // Change channel
			ch.getWorldInterface().loggedOn(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
			}
			if (player.getGuildId() > 0) {
			ch.getWorldInterface().setGuildMemberOnline(player.getMGC(), false, -1);
			}

		} catch (final RemoteException e) {
			ch.reconnectWorld();
			player.setMessenger(null);
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.println(getLogMessage(this, "ERROR") + e);
		} finally {
			if (RemoveInChannelServer && ch != null) {
			ch.removePlayer(player);
			}
			player = null;
		}
		} else {
		final CashShopServer cs = CashShopServer.getInstance();
		try {
			if (player.getParty() != null) {
			final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
			chrp.setOnline(false);
			cs.getCSInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, chrp);
			}
			if (!serverTransition && isLoggedIn()) {
			cs.getCSInterface().loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
			} else { // Change channel
			cs.getCSInterface().loggedOn(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
			}
			if (player.getGuildId() > 0) {
			cs.getCSInterface().setGuildMemberOnline(player.getMGC(), false, -1);
			}

		} catch (final RemoteException e) {
			cs.reconnectWorld();
			player.setMessenger(null);
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.println(getLogMessage(this, "ERROR") + e);
		} finally {
			if (RemoveInChannelServer && cs != null) {
			cs.getPlayerStorage().deregisterPlayer(player);
			}
			player = null;
		}
		}
	}
	if (!serverTransition && isLoggedIn()) {
		updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, null);
	}
	}

	public final String getSessionIPAddress() {
	return session.getRemoteAddress().toString().split(":")[0];
	}

	public final boolean CheckIPAddress() {
	try {
		final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT SessionIP FROM accounts WHERE id = ?");
		ps.setInt(1, this.accId);
		final ResultSet rs = ps.executeQuery();

		boolean canlogin = false;

		if (rs.next()) {
		final String sessionIP = rs.getString("SessionIP");

		if (sessionIP != null) { // Probably a login proced skipper?
			canlogin = getSessionIPAddress().equals(sessionIP.split(":")[0]);
		}
		}
		rs.close();
		ps.close();

		return canlogin;
	} catch (final SQLException e) {
		System.out.println("Failed in checking IP address for client.");
	}
	return false;
	}

	public final void DebugMessage(final StringBuilder sb) {
	sb.append(getSession().getRemoteAddress());
	sb.append("Connected: ");
	sb.append(getSession().isConnected());
	sb.append(" Closing: ");
	sb.append(getSession().isClosing());
	sb.append(" ClientKeySet: ");
	sb.append(getSession().getAttribute(MapleClient.CLIENT_KEY) != null);
	sb.append(" loggedin: ");
	sb.append(isLoggedIn());
	sb.append(" has char: ");
	sb.append(getPlayer() != null);
	}

	public final int getChannel() {
	return channel;
	}

	public final ChannelServer getChannelServer() {
	return ChannelServer.getInstance(channel);
	}

	public final boolean deleteCharacter(final int cid) {
	try {
		final Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT id, guildid, guildrank, name FROM characters WHERE id = ? AND accountid = ?");
		ps.setInt(1, cid);
		ps.setInt(2, accId);
		ResultSet rs = ps.executeQuery();

		if (!rs.next()) {
		rs.close();
		ps.close();
		return false;
		}
		if (rs.getInt("guildid") > 0) { // is in a guild when deleted
		final MapleGuildCharacter mgc = new MapleGuildCharacter(cid, (short) 0, rs.getString("name"), (byte) -1, 0, rs.getInt("guildrank"), rs.getInt("guildid"), false);
		try {
			LoginServer.getInstance().getWorldInterface().deleteGuildCharacter(mgc);
		} catch (RemoteException e) {
			return false;
		}
		}
		rs.close();
		ps.close();

		ps = con.prepareStatement("DELETE FROM characters WHERE id = ?");
		ps.setInt(1, cid);
		ps.executeUpdate();
		ps.close();

		ps = con.prepareStatement("DELETE FROM hiredmerch WHERE characterid = ?");
		ps.setInt(1, cid);
		ps.executeUpdate();
		ps.close();

		ps = con.prepareStatement("DELETE FROM mountdata WHERE characterid = ?");
		ps.setInt(1, cid);
		ps.executeUpdate();
		ps.close();

		ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
		ps.setInt(1, cid);
		ps.executeUpdate();
		ps.close();

		return true;
	} catch (final SQLException e) {
		System.err.println("DeleteChar error" + e);
	}
	return false;
	}

	public final byte getGender() {
	return gender;
	}

	public final void setGender(final byte gender) {
	this.gender = gender;
	}

	public final String getSecondPassword() {
	return secondPassword;
	}

	public final void setSecondPassword(final String secondPassword) {
	this.secondPassword = secondPassword;
	}

	public final String getAccountName() {
	return accountName;
	}

	public final void setAccountName(final String accountName) {
	this.accountName = accountName;
	}

	public final void setChannel(final int channel) {
	this.channel = channel;
	}

	public final int getWorld() {
	return world;
	}

	public final void setWorld(final int world) {
	this.world = world;
	}

	public final long getLastPong() {
	return lastPong;
	}

	public final void pongReceived() {
	lastPong = System.currentTimeMillis();
	}

	public final void sendPing() {
	final long then = System.currentTimeMillis();
	session.write(LoginPacket.getPing());

	TimerManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		try {
			if (lastPong - then < 0) {
			if (getSession().isConnected()) {
				getSession().close();
			}
			}
		} catch (final NullPointerException e) {
			// client already gone
		}
		}
	}, 150000000); // note: idletime gets added to this too
	}

	public static final String getLogMessage(final MapleClient cfor, final String message) {
	return getLogMessage(cfor, message, new Object[0]);
	}

	public static final String getLogMessage(final MapleCharacter cfor, final String message) {
	return getLogMessage(cfor == null ? null : cfor.getClient(), message);
	}

	public static final String getLogMessage(final MapleCharacter cfor, final String message, final Object... parms) {
	return getLogMessage(cfor == null ? null : cfor.getClient(), message, parms);
	}

	public static final String getLogMessage(final MapleClient cfor, final String message, final Object... parms) {
	final StringBuilder builder = new StringBuilder();
	if (cfor != null) {
		if (cfor.getPlayer() != null) {
		builder.append("<");
		builder.append(MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName()));
		builder.append(" (cid: ");
		builder.append(cfor.getPlayer().getId());
		builder.append(")> ");
		}
		if (cfor.getAccountName() != null) {
		builder.append("(Account: ");
		builder.append(cfor.getAccountName());
		builder.append(") ");
		}
	}
	builder.append(message);
	int start;
	for (final Object parm : parms) {
		start = builder.indexOf("{}");
		builder.replace(start, start + 2, parm.toString());
	}
	return builder.toString();
	}

	public static final int findAccIdForCharacterName(final String charName) {
	try {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
		ps.setString(1, charName);
		ResultSet rs = ps.executeQuery();

		int ret = -1;
		if (rs.next()) {
		ret = rs.getInt("accountid");
		}
		rs.close();
		ps.close();

		return ret;
	} catch (final SQLException e) {
		System.err.println("findAccIdForCharacterName SQL error");
	}
	return -1;
	}

	public final Set<String> getMacs() {
	return Collections.unmodifiableSet(macs);
	}

	public final boolean isGm() {
	return gm;
	}

	public final void setScriptEngine(final String name, final ScriptEngine e) {
	engines.put(name, e);
	}

	public final ScriptEngine getScriptEngine(final String name) {
	return engines.get(name);
	}

	public final void removeScriptEngine(final String name) {
	engines.remove(name);
	}

	public final ScheduledFuture<?> getIdleTask() {
	return idleTask;
	}

	public final void setIdleTask(final ScheduledFuture<?> idleTask) {
	this.idleTask = idleTask;
	}

	protected static final class CharNameAndId {

	public final String name;
	public final int id;

	public CharNameAndId(final String name, final int id) {
		super();
		this.name = name;
		this.id = id;
	}
	}
}
