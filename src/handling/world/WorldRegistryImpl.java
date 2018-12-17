package handling.world;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import database.DatabaseConnection;
import handling.channel.remote.ChannelWorldInterface;
import handling.login.remote.LoginWorldInterface;
import handling.cashshop.remote.CashShopWorldInterface;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.remote.CashShopInterface;
import handling.world.remote.WorldChannelInterface;
import handling.world.remote.WorldLoginInterface;
import handling.world.remote.WorldRegistry;

public class WorldRegistryImpl extends UnicastRemoteObject implements WorldRegistry {
	private static final long serialVersionUID = -5170574938159280746L;
	private static WorldRegistryImpl instance = null;
	private String csIP = null;
	private final Map<Integer, ChannelWorldInterface> channelServer = new LinkedHashMap<Integer, ChannelWorldInterface>();
	private final List<LoginWorldInterface> loginServer = new LinkedList<LoginWorldInterface>();
	private CashShopWorldInterface cashshopServer = null;
	private final Map<Integer, MapleParty> parties = new HashMap<Integer, MapleParty>();
	private final AtomicInteger runningPartyId = new AtomicInteger();
	private final Map<Integer, MapleMessenger> messengers = new HashMap<Integer, MapleMessenger>();
	private final AtomicInteger runningMessengerId = new AtomicInteger();
	private final Map<Integer, MapleGuild> guilds = new LinkedHashMap<Integer, MapleGuild>();
	private final PlayerBuffStorage buffStorage = new PlayerBuffStorage();
	private final Lock Guild_Mutex = new ReentrantLock();

	private WorldRegistryImpl() throws RemoteException {
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
		DatabaseConnection.setProps(WorldServer.getInstance().getDbProp());
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT MAX(party)+1 FROM characters");
			ResultSet rs = ps.executeQuery();
			rs.next();
			runningPartyId.set(rs.getInt(1));
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runningMessengerId.set(1);
	}

	public static WorldRegistryImpl getInstance() {
		if (instance == null) {
			try {
				instance = new WorldRegistryImpl();
			} catch (RemoteException e) {
				// can't do much anyway we are fucked ^^
				throw new RuntimeException(e);
			}
		}
		return instance;
	}

	private int getFreeChannelId() {
		for (int i = 0; i < 20; i++) {
			if (!channelServer.containsKey(i)) {
				return i;
			}
		}
		return -1;
	}

	public WorldChannelInterface registerChannelServer(final String authKey, final ChannelWorldInterface cb) throws RemoteException {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM auth_server_channel WHERE `key` = SHA1(?) AND world = ?");
			ps.setString(1, authKey);
			ps.setInt(2, WorldServer.getInstance().getWorldId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				int channelId = rs.getInt("number");
				if (channelId < 1) {
					channelId = getFreeChannelId();
					if (channelId == -1) {
						throw new RuntimeException("Maximum channels reached");
					}
				} else {
					if (channelServer.containsKey(channelId)) {
						ChannelWorldInterface oldch = channelServer.get(channelId);
						try {
							oldch.shutdown(0);
						} catch (ConnectException ce) {
							// silently ignore as we assume that the server is offline
						}
						// int switchChannel = getFreeChannelId();
						// if (switchChannel == -1) {
						// throw new RuntimeException("Maximum channels reached");
						// }
						// ChannelWorldInterface switchIf = channelServer.get(channelId);
						// deregisterChannelServer(switchChannel);
						// channelServer.put(switchChannel, switchIf);
						// switchIf.setChannelId(switchChannel);
						// for (LoginWorldInterface wli : loginServer) {
						// wli.channelOnline(switchChannel, switchIf.getIP());
						// }
					}
				}
				channelServer.put(channelId, cb);
				cb.setChannelId(channelId);
				WorldChannelInterface ret = new WorldChannelInterfaceImpl(cb, rs.getInt("channelid"));
				rs.close();
				ps.close();
				return ret;
			}
			rs.close();
			ps.close();
		} catch (SQLException ex) {
			System.err.println("Encountered database error while authenticating channelserver" + ex);
		}
		throw new RuntimeException("Couldn't find a channel with the given key (" + authKey + ")");
	}

	public void deregisterChannelServer(final int channel) throws RemoteException {
		channelServer.remove(channel);
		for (final LoginWorldInterface wli : loginServer) {
			wli.channelOffline(channel);
		}
		System.out.println("Channel " + channel + " is offline.");
	}

	public WorldLoginInterface registerLoginServer(final String authKey, final LoginWorldInterface cb) throws RemoteException {
		WorldLoginInterface ret = null;
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM auth_server_login WHERE `key` = SHA1(?) AND world = ?");
			ps.setString(1, authKey);
			ps.setInt(2, WorldServer.getInstance().getWorldId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				loginServer.add(cb);
				for (ChannelWorldInterface cwi : channelServer.values()) {
					cb.channelOnline(cwi.getChannelId(), authKey);
				}
			}
			rs.close();
			ps.close();
			ret = new WorldLoginInterfaceImpl();
		} catch (Exception e) {
			System.err.println("Encountered database error while authenticating loginserver" + e);
		}
		return ret;
	}

	public void deregisterLoginServer(LoginWorldInterface cb) throws RemoteException {
		loginServer.remove(cb);
	}

	public CashShopInterface registerCSServer(final String authKey, final String IP, final CashShopWorldInterface cb) throws RemoteException {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM auth_server_cs WHERE `key` = SHA1(?) AND world = ?");
			ps.setString(1, authKey);
			ps.setInt(2, WorldServer.getInstance().getWorldId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				if (cashshopServer != null) {
					cashshopServer.shutdown();
				}
				cashshopServer = cb;
				csIP = IP;
				rs.close();
				ps.close();
				return new WorldCashShopInterfaceImpl();
			}
		} catch (Exception e) {
			System.err.println("Encountered database error while authenticating loginserver" + e);
		}
		throw new RuntimeException("Couldn't find a CS with the given key (" + authKey + ")");
	}

	public void deregisterCSServer() throws RemoteException {
		cashshopServer = null;
		csIP = null;
	}

	public List<LoginWorldInterface> getLoginServer() {
		return new LinkedList<LoginWorldInterface>(loginServer);
	}

	public String getCSIP() {
		return csIP;
	}

	public CashShopWorldInterface getCashShopServer() {
		return cashshopServer;
	}

	public ChannelWorldInterface getChannel(final int channel) {
		return channelServer.get(channel);
	}

	public Set<Integer> getChannelServer() {
		return new HashSet<Integer>(channelServer.keySet());
	}

	public Collection<ChannelWorldInterface> getAllChannelServers() {
		return channelServer.values();
	}

	public int getHighestChannelId() {
		int highest = 0;
		for (final Integer channel : channelServer.keySet()) {
			if (channel != null && channel.intValue() > highest) {
				highest = channel.intValue();
			}
		}
		return highest;
	}

	public MapleParty createParty(final MaplePartyCharacter chrfor) {
		final int partyid = runningPartyId.getAndIncrement();
		final MapleParty party = new MapleParty(partyid, chrfor);
		parties.put(party.getId(), party);
		return party;
	}

	public MapleParty getParty(final int partyid) {
		return parties.get(partyid);
	}

	public MapleParty disbandParty(final int partyid) {
		return parties.remove(partyid);
	}

	public final String getStatus() throws RemoteException {
		StringBuilder ret = new StringBuilder();
		List<Entry<Integer, ChannelWorldInterface>> channelServers = new ArrayList<Entry<Integer, ChannelWorldInterface>>(channelServer.entrySet());
		Collections.sort(channelServers, new Comparator<Entry<Integer, ChannelWorldInterface>>() {
			@Override
			public int compare(Entry<Integer, ChannelWorldInterface> o1, Entry<Integer, ChannelWorldInterface> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		int totalUsers = 0;
		for (final Entry<Integer, ChannelWorldInterface> cs : channelServers) {
			ret.append("Channel ");
			ret.append(cs.getKey());
			try {
				cs.getValue().isAvailable();
				ret.append(": online, ");
				int channelUsers = cs.getValue().getConnected();
				totalUsers += channelUsers;
				ret.append(channelUsers);
				ret.append(" users\n");
			} catch (RemoteException e) {
				ret.append(": offline\n");
			}
		}
		ret.append("Total users online: ");
		ret.append(totalUsers);
		ret.append("\n");
		//	Properties props = new Properties(WorldServer.getInstance().getWorldProp());
		for (LoginWorldInterface lwi : loginServer) {
			ret.append("Login: ");
			try {
				lwi.isAvailable();
				ret.append("online\n");
			} catch (RemoteException e) {
				ret.append("offline\n");
			}
		}
		return ret.toString();
	}

	public final int createGuild(final int leaderId, final String name) {
		return MapleGuild.createGuild(leaderId, name);
	}

	public final MapleGuild getGuild(final int id, final MapleGuildCharacter mgc) {
		Guild_Mutex.lock();
		try {
			if (guilds.get(id) != null) {
				return guilds.get(id);
			}
			if (mgc == null) {
				return null;
			}
			final MapleGuild g = new MapleGuild(mgc);
			if (g.getId() == -1) { //failed to load
				return null;
			}
			guilds.put(id, g);
			return g;
		} finally {
			Guild_Mutex.unlock();
		}
	}

	public void setGuildMemberOnline(final MapleGuildCharacter mgc, final boolean bOnline, final int channel) {
		getGuild(mgc.getGuildId(), mgc).setOnline(mgc.getId(), bOnline, channel);
	}

	public final int addGuildMember(final MapleGuildCharacter mgc) {
		final MapleGuild g = guilds.get(mgc.getGuildId());
		if (g != null) {
			return g.addGuildMember(mgc);
		}
		return 0;
	}

	public void leaveGuild(final MapleGuildCharacter mgc) {
		final MapleGuild g = guilds.get(mgc.getGuildId());
		if (g != null) {
			g.leaveGuild(mgc);
		}
	}

	public void allianceChat(final int gid, final String name, final int cid, final String msg) {
		final MapleGuild g = guilds.get(gid);
		if (g != null) {
			g.allianceChat(name, cid, msg);
		}
	}

	public void guildChat(final int gid, final String name, final int cid, final String msg) {
		final MapleGuild g = guilds.get(gid);
		if (g != null) {
			g.guildChat(name, cid, msg);
		}
	}

	public void changeRank(final int gid, final int cid, final int newRank) {
		final MapleGuild g = guilds.get(gid);
		if (g != null) {
			g.changeRank(cid, newRank);
		}
	}

	public void expelMember(final MapleGuildCharacter initiator, final String name, final int cid) {
		final MapleGuild g = guilds.get(initiator.getGuildId());
		if (g != null) {
			g.expelMember(initiator, name, cid);
		}
	}

	public void setGuildNotice(final int gid, final String notice) {
		final MapleGuild g = guilds.get(gid);
		if (g != null) {
			g.setGuildNotice(notice);
		}
	}

	public void memberLevelJobUpdate(final MapleGuildCharacter mgc) {
		final MapleGuild g = guilds.get(mgc.getGuildId());
		if (g != null) {
			g.memberLevelJobUpdate(mgc);
		}
	}

	public void changeRankTitle(final int gid, final String[] ranks) {
		final MapleGuild g = guilds.get(gid);
		if (g != null) {
			g.changeRankTitle(ranks);
		}
	}

	public void setGuildEmblem(final int gid, final short bg, final byte bgcolor, final short logo, final byte logocolor) {
		final MapleGuild g = guilds.get(gid);
		if (g != null) {
			g.setGuildEmblem(bg, bgcolor, logo, logocolor);
		}
	}

	public void disbandGuild(final int gid) {
		Guild_Mutex.lock();
		try {
			guilds.get(gid).disbandGuild();
			guilds.remove(gid);
		} finally {
			Guild_Mutex.unlock();
		}
	}

	public final boolean increaseGuildCapacity(final int gid) {
		final MapleGuild g = guilds.get(gid);
		if (g != null) {
			return g.increaseCapacity();
		}
		return false;
	}

	public void gainGP(final int gid, final int amount) {
		final MapleGuild g = guilds.get(gid);
		if (g != null) {
			g.gainGP(amount);
		}
	}

	public final MapleMessenger createMessenger(final MapleMessengerCharacter chrfor) {
		final int messengerid = runningMessengerId.getAndIncrement();
		final MapleMessenger messenger = new MapleMessenger(messengerid, chrfor);
		messengers.put(messenger.getId(), messenger);
		return messenger;
	}

	public final MapleMessenger getMessenger(final int messengerid) {
		return messengers.get(messengerid);
	}

	public final PlayerBuffStorage getPlayerBuffStorage() {
		return buffStorage;
	}
}