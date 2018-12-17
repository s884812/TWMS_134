package handling.world;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import database.DatabaseConnection;
import handling.channel.remote.ChannelWorldInterface;
import handling.login.remote.LoginWorldInterface;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.remote.CheaterData;
import handling.world.remote.WorldChannelInterface;
import handling.world.remote.WorldLocation;
import tools.CollectionUtil;

public class WorldChannelInterfaceImpl extends UnicastRemoteObject implements WorldChannelInterface {
	private static final long serialVersionUID = -5568606556235590482L;
	private ChannelWorldInterface cb;
	private int dbId;
	private boolean ready = false;

	public WorldChannelInterfaceImpl() throws RemoteException {
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	public WorldChannelInterfaceImpl(ChannelWorldInterface cb, int dbId) throws RemoteException {
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
		this.cb = cb;
		this.dbId = dbId;
	}

	public Properties getDatabaseProperties() throws RemoteException {
		return WorldServer.getInstance().getDbProp();
	}

	public Properties getGameProperties() throws RemoteException {
		Properties ret = new Properties(WorldServer.getInstance().getWorldProp());
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM auth_server_channel_ip WHERE channelid = ?");
			ps.setInt(1, dbId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ret.setProperty(rs.getString("name"), rs.getString("value"));
			}
			rs.close();
			ps.close();
		} catch (SQLException ex) {
			System.out.println("Could not retrieve channel configuration" + ex);
		}
		return ret;
	}

	public String getCSIP() {
		return WorldRegistryImpl.getInstance().getCSIP();
	}

	public void serverReady() throws RemoteException {
		ready = true;
		for (LoginWorldInterface wli : WorldRegistryImpl.getInstance().getLoginServer()) {
			try {
				wli.channelOnline(cb.getChannelId(), cb.getIP());
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterLoginServer(wli);
			}
		}
		System.out.println(":: Channel " + cb.getChannelId() + " is online ::");
	}

	public boolean isReady() {
		return ready;
	}

	public String getIP(int channel) throws RemoteException {
		final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(channel);
		if (cwi == null) {
			return "0.0.0.0:0";
		} else {
			try {
				return cwi.getIP();
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(channel);
				return "0.0.0.0:0";
			}
		}
	}

	public void whisper(String sender, String target, int channel, String message) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.whisper(sender, target, channel, message);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public boolean isConnected(String charName) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
				try {
				if (cwi.isConnected(charName)) {
					return true;
				}
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
		return false;
	}

	public boolean isCharacterListConnected(List<String> charName) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				if (cwi.isCharacterListConnected(charName)) {
					return true;
				}
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
		return false;
	}

	public void broadcastMessage(byte[] message) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.broadcastMessage(message);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void broadcastSmega(byte[] message) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.broadcastSmega(message);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void broadcastGMMessage(byte[] message) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.broadcastGMMessage(message);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void toggleMegaphoneMuteState() throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.toggleMegaphoneMuteState();
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public boolean hasMerchant(int accountId) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				return cwi.hasMerchant(accountId);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
		return false;
	}

	public int find(String charName) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				if (cwi.isConnected(charName)) {
					return cwi.getChannelId();
				}
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
		return -1;
	}

	// can we generify this
	@Override
	public int find(int characterId) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				if (cwi.isConnected(characterId)) {
					return cwi.getChannelId();
				}
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
		return -1;
	}

	@Override
	public boolean isCharacterInCS(String name) throws RemoteException {
		if (WorldRegistryImpl.getInstance().getCSIP() == null) {
			return false; // CS might not be init
		}
		return WorldRegistryImpl.getInstance().getCashShopServer().isCharacterInCS(name);
	}

	public void shutdownLogin() throws RemoteException {
		for (LoginWorldInterface lwi : WorldRegistryImpl.getInstance().getLoginServer()) {
			try {
				lwi.shutdown();
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterLoginServer(lwi);
			}
		}
	}

	public void shutdown(int time) throws RemoteException {
		for (LoginWorldInterface lwi : WorldRegistryImpl.getInstance().getLoginServer()) {
			try {
				lwi.shutdown();
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterLoginServer(lwi);
			}
		}
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.shutdown(time);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public Map<Integer, Integer> getConnected() throws RemoteException {
		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
		int total = 0;
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				int curConnected = cwi.getConnected();
				ret.put(i, curConnected);
				total += curConnected;
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
		ret.put(0, total);
		return ret;
	}

	public void loggedOn(String name, int characterId, int channel, int[] buddies) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.loggedOn(name, characterId, channel, buddies);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	@Override
	public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.loggedOff(name, characterId, channel, buddies);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	//TODO only notify channels where partymembers are?
	public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) throws RemoteException {
		final MapleParty party = WorldRegistryImpl.getInstance().getParty(partyid);
		if (party == null) {
			throw new IllegalArgumentException("no party with the specified partyid exists");
		}
		switch (operation) {
			case JOIN:
			party.addMember(target);
			break;
			case EXPEL:
			case LEAVE:
			party.removeMember(target);
			break;
			case DISBAND:
			WorldRegistryImpl.getInstance().disbandParty(partyid);
			break;
			case SILENT_UPDATE:
			case LOG_ONOFF:
			party.updateMember(target);
			break;
			case CHANGE_LEADER:
			party.setLeader(target);
			break;
			default:
			throw new RuntimeException("Unhandeled updateParty operation " + operation.name());
		}
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.updateParty(party, operation, target);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public MapleParty createParty(MaplePartyCharacter chrfor) throws RemoteException {
		return WorldRegistryImpl.getInstance().createParty(chrfor);
	}

	public MapleParty getParty(int partyid) throws RemoteException {
		return WorldRegistryImpl.getInstance().getParty(partyid);
	}

	public void allianceChat(int gid, String name, int cid, String msg) throws RemoteException {
		WorldRegistryImpl.getInstance().allianceChat(gid, name, cid, msg);
	}

	@Override
	public void partyChat(int partyid, String chattext, String namefrom) throws RemoteException {
		final MapleParty party = WorldRegistryImpl.getInstance().getParty(partyid);
		if (party == null) {
			throw new IllegalArgumentException("no party with the specified partyid exists");
		}
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.partyChat(party, chattext, namefrom);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public boolean isAvailable() throws RemoteException {
		return true;
	}

	public WorldLocation getLocation(String charName) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				if (cwi.isConnected(charName)) {
					return new WorldLocation(cwi.getLocation(charName), (byte) cwi.getChannelId());
				}
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
		return null;
	}

	public List<CheaterData> getCheaters() throws RemoteException {
		List<CheaterData> allCheaters = new ArrayList<CheaterData>();
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				allCheaters.addAll(cwi.getCheaters());
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
		Collections.sort(allCheaters);
		return CollectionUtil.copyFirst(allCheaters, 20);
	}

	@Override
	public ChannelWorldInterface getChannelInterface(int channel) {
		return WorldRegistryImpl.getInstance().getChannel(channel);
	}

	@Override
	public void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) throws RemoteException {
		for (ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getAllChannelServers()) {
			cwi.buddyChat(recipientCharacterIds, cidFrom, nameFrom, chattext);
		}
	}

	@Override
	public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException {
		List<CharacterIdChannelPair> foundsChars = new ArrayList<CharacterIdChannelPair>(characterIds.length);
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			for (int charid : cwi.multiBuddyFind(charIdFrom, characterIds)) {
				foundsChars.add(new CharacterIdChannelPair(charid, i));
			}
		}
		return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
	}

	@Override
	public void ChannelChange_Data(CharacterTransfer Data, int characterid, int toChannel) throws RemoteException {
		if (toChannel != -10) {
			for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
				if (i == toChannel) {
					final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
					try {
						cwi.ChannelChange_Data(Data, characterid);
					} catch (RemoteException e) {
						WorldRegistryImpl.getInstance().deregisterChannelServer(i);
					}
				}
			}
		} else {
			WorldRegistryImpl.getInstance().getCashShopServer().ChannelChange_Data(Data, characterid);
		}
	}

	@Override
	public MapleGuild getGuild(int id, MapleGuildCharacter mgc) throws RemoteException {
		return WorldRegistryImpl.getInstance().getGuild(id, mgc);
	}

	@Override
	public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) throws RemoteException {
		WorldRegistryImpl.getInstance().setGuildMemberOnline(mgc, bOnline, channel);
	}

	@Override
	public int addGuildMember(MapleGuildCharacter mgc) throws RemoteException {
		return WorldRegistryImpl.getInstance().addGuildMember(mgc);
	}

	@Override
	public void guildChat(int gid, String name, int cid, String msg) throws RemoteException {
		WorldRegistryImpl.getInstance().guildChat(gid, name, cid, msg);
	}

	@Override
	public void leaveGuild(MapleGuildCharacter mgc) throws RemoteException {
		WorldRegistryImpl.getInstance().leaveGuild(mgc);
	}

	@Override
	public void changeRank(int gid, int cid, int newRank) throws RemoteException {
		WorldRegistryImpl.getInstance().changeRank(gid, cid, newRank);
	}

	@Override
	public void expelMember(MapleGuildCharacter initiator, String name, int cid) throws RemoteException {
		WorldRegistryImpl.getInstance().expelMember(initiator, name, cid);
	}

	@Override
	public void setGuildNotice(int gid, String notice) throws RemoteException {
		WorldRegistryImpl.getInstance().setGuildNotice(gid, notice);
	}

	@Override
	public void memberLevelJobUpdate(MapleGuildCharacter mgc) throws RemoteException {
		WorldRegistryImpl.getInstance().memberLevelJobUpdate(mgc);
	}

	@Override
	public void changeRankTitle(int gid, String[] ranks) throws RemoteException {
		WorldRegistryImpl.getInstance().changeRankTitle(gid, ranks);
	}

	@Override
	public int createGuild(int leaderId, String name) throws RemoteException {
		return WorldRegistryImpl.getInstance().createGuild(leaderId, name);
	}

	@Override
	public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) throws RemoteException {
		WorldRegistryImpl.getInstance().setGuildEmblem(gid, bg, bgcolor, logo, logocolor);
	}

	@Override
	public void disbandGuild(int gid) throws RemoteException {
		WorldRegistryImpl.getInstance().disbandGuild(gid);
	}

	@Override
	public boolean increaseGuildCapacity(int gid) throws RemoteException {
		return WorldRegistryImpl.getInstance().increaseGuildCapacity(gid);
	}

	@Override
	public void gainGP(int gid, int amount) throws RemoteException {
		WorldRegistryImpl.getInstance().gainGP(gid, amount);
	}

	public MapleMessenger createMessenger(MapleMessengerCharacter chrfor) throws RemoteException {
		return WorldRegistryImpl.getInstance().createMessenger(chrfor);
	}

	public MapleMessenger getMessenger(int messengerid) throws RemoteException {
		return WorldRegistryImpl.getInstance().getMessenger(messengerid);
	}

	public void messengerInvite(String sender, int messengerid, String target, int fromchannel) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.messengerInvite(sender, messengerid, target, fromchannel);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void leaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException {
		final MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
		if (messenger == null) {
			throw new IllegalArgumentException("No messenger with the specified messengerid exists");
		}
		final int position = messenger.getPositionByName(target.getName());
		messenger.removeMember(target);
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.removeMessengerPlayer(messenger, position);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) throws RemoteException {
		final MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
		if (messenger == null) {
			throw new IllegalArgumentException("No messenger with the specified messengerid exists");
		}
		messenger.addMember(target);
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.addMessengerPlayer(messenger, from, fromchannel, target.getPosition());
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void messengerChat(int messengerid, String chattext, String namefrom) throws RemoteException {
		final MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
		if (messenger == null) {
			throw new IllegalArgumentException("No messenger with the specified messengerid exists");
		}
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.messengerChat(messenger, chattext, namefrom);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void declineChat(String target, String namefrom) throws RemoteException {
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.declineChat(target, namefrom);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void updateMessenger(int messengerid, String namefrom, int fromchannel) throws RemoteException {
		final MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
		final int position = messenger.getPositionByName(namefrom);
		for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
			final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
			try {
				cwi.updateMessenger(messenger, namefrom, position, fromchannel);
			} catch (RemoteException e) {
				WorldRegistryImpl.getInstance().deregisterChannelServer(i);
			}
		}
	}

	public void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException {
		final MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
		if (messenger == null) {
			throw new IllegalArgumentException("No messenger with the specified messengerid exists");
		}
		messenger.silentRemoveMember(target);
	}

	public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position) throws RemoteException {
		final MapleMessenger messenger = WorldRegistryImpl.getInstance().getMessenger(messengerid);
		if (messenger == null) {
			throw new IllegalArgumentException("No messenger with the specified messengerid exists");
		}
		messenger.silentAddMember(target, position);
	}

	public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) throws RemoteException {
		WorldRegistryImpl.getInstance().getPlayerBuffStorage().addBuffsToStorage(chrid, toStore);
	}

	public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) throws RemoteException {
		return WorldRegistryImpl.getInstance().getPlayerBuffStorage().getBuffsFromStorage(chrid);
	}

	public void addCooldownsToStorage(int chrid, List<PlayerCoolDownValueHolder> toStore) throws RemoteException {
		WorldRegistryImpl.getInstance().getPlayerBuffStorage().addCooldownsToStorage(chrid, toStore);
	}

	public List<PlayerCoolDownValueHolder> getCooldownsFromStorage(int chrid) throws RemoteException {
		return WorldRegistryImpl.getInstance().getPlayerBuffStorage().getCooldownsFromStorage(chrid);
	}

	public void addDiseaseToStorage(int chrid, List<PlayerDiseaseValueHolder> toStore) throws RemoteException {
		WorldRegistryImpl.getInstance().getPlayerBuffStorage().addDiseaseToStorage(chrid, toStore);
	}

	public List<PlayerDiseaseValueHolder> getDiseaseFromStorage(int chrid) throws RemoteException {
		return WorldRegistryImpl.getInstance().getPlayerBuffStorage().getDiseaseFromStorage(chrid);
	}
}