package handling.channel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import client.MapleCharacter;
import client.SkillFactory;
import client.OdinSEA;
import database.DatabaseConnection;
import handling.ServerConstants;
import handling.ServerType;
import handling.MaplePacket;
import handling.MapleServerHandler;
import handling.channel.remote.ChannelWorldInterface;
import handling.mina.MapleCodecFactory;
import handling.world.MaplePartyCharacter;
import handling.world.MapleParty;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.guild.MapleGuildSummary;
import handling.world.remote.WorldChannelInterface;
import handling.world.remote.WorldRegistry;
import scripting.EventScriptManager;
import server.AutobanManager;
import server.MapleSquad;
import server.ShutdownServer;
import server.TimerManager;
import server.ItemMakerFactory;
import server.RandomRewards;
import server.MapleItemInformationProvider;
import server.maps.MapTimer;
import server.maps.MapleMapFactory;
import server.shops.HiredMerchant;
import tools.MaplePacketCreator;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class ChannelServer {

	private byte expRate, mesoRate, dropRate;
	private short port;
	private int channel, running_MerchantID = 0;
	private String serverMessage, key, ip;
	private Boolean worldReady = true;
	private boolean shutdown = false, finishedShutdown = false, MegaphoneMuteState = false;
	private static InetSocketAddress InetSocketadd;
	private static Properties initialProp;
	private static WorldRegistry worldRegistry;
	private PlayerStorage players;
	private Properties props = new Properties();
	private ChannelWorldInterface cwi;
	private WorldChannelInterface wci = null;
	private IoAcceptor acceptor;
	private final MapleMapFactory mapFactories[] = new MapleMapFactory[2];
	private static final Map<Integer, ChannelServer> instances = new HashMap<Integer, ChannelServer>();
	private static final Map<String, ChannelServer> pendingInstances = new HashMap<String, ChannelServer>();
	private final Map<Integer, MapleGuildSummary> gsStore = new HashMap<Integer, MapleGuildSummary>();
	private final Map<String, MapleSquad> mapleSquads = new HashMap<String, MapleSquad>();
	private final Map<Integer, HiredMerchant> merchants = new HashMap<Integer, HiredMerchant>();
	private final Lock merchant_mutex = new ReentrantLock();
	private EventScriptManager eventManagers[] = new EventScriptManager[2];


	private ChannelServer(final String key) {
		this.key = key;
	}

	public int getChannel1(){
		return channel;
	}

	public static final WorldRegistry getWorldRegistry() {
		return worldRegistry;
	}

	public final void reconnectWorld() {
		try {
			wci.isAvailable();
		} catch (RemoteException ex) {
			synchronized (worldReady) {
				worldReady = false;
			}
			synchronized (cwi) {
				synchronized (worldReady) {
					if (worldReady) {
						return;
					}
				}
				System.out.println("Reconnecting to world server");
				synchronized (wci) {
					try {
						initialProp = new Properties();
						FileReader fr = new FileReader(System.getProperty("channel.config"));
						initialProp.load(fr);
						fr.close();
						final Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"),
						Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
						worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
						cwi = new ChannelWorldInterfaceImpl(this);
						wci = worldRegistry.registerChannelServer(key, cwi);
						props = wci.getGameProperties();
						expRate = Byte.parseByte(props.getProperty("world.exp"));
						mesoRate = Byte.parseByte(props.getProperty("world.meso"));
						dropRate = Byte.parseByte(props.getProperty("world.drop"));
						serverMessage = props.getProperty("world.serverMessage");
						Properties dbProp = new Properties();
						fr = new FileReader("settings.ini"); // db.properties
						dbProp.load(fr);
						fr.close();
						DatabaseConnection.setProps(dbProp);
						DatabaseConnection.getConnection();
						wci.serverReady();
					} catch (Exception e) {
						System.err.println("Reconnecting failed" + e);
					}
					worldReady = true;
				}
			}
			synchronized (worldReady) {
				worldReady.notifyAll();
			}
		}
	}

	public final void run_startup_configurations() {
		try {
			cwi = new ChannelWorldInterfaceImpl(this);
			wci = worldRegistry.registerChannelServer(key, cwi);
			props = wci.getGameProperties();
			expRate = Byte.parseByte(props.getProperty("world.exp"));
			mesoRate = Byte.parseByte(props.getProperty("world.meso"));
			dropRate = Byte.parseByte(props.getProperty("world.drop"));
			serverMessage = props.getProperty("world.serverMessage");
			for (int i = 0; i < eventManagers.length; i++) {
				eventManagers[i] = new EventScriptManager(this, props.getProperty("channel.events").split(","), i == 0 ? 6 : 5);
			}
			final Properties dbProp = new Properties();
			final FileReader fileReader = new FileReader("settings.ini"); // db.properties
			dbProp.load(fileReader);
			fileReader.close();
			DatabaseConnection.setProps(dbProp);
			DatabaseConnection.getConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		port = Short.parseShort(props.getProperty("channel.net.port"));
		ip = props.getProperty("channel.net.interface") + ":" + port;
		ByteBuffer.setUseDirectBuffers(false);
		ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
		acceptor = new SocketAcceptor();
		final SocketAcceptorConfig acceptor_config = new SocketAcceptorConfig();
		acceptor_config.getSessionConfig().setTcpNoDelay(true);
		acceptor_config.setDisconnectOnUnbind(true);
		acceptor_config.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
		TimerManager.getInstance().start();
		TimerManager.getInstance().register(AutobanManager.getInstance(), 60000);
		MapTimer.getInstance().start();
		ItemMakerFactory.getInstance();
		MapleItemInformationProvider.getInstance();
		RandomRewards.getInstance();
		SkillFactory.getSkill(99999999);
		players = new PlayerStorage();
		for (int i = 0; i < mapFactories.length; i++) {
			mapFactories[i] = new MapleMapFactory();
			mapFactories[i].setWorld(i == 0 ? 6 : 5);
			mapFactories[i].setChannel(this.channel);
		}
		try {
			final MapleServerHandler serverHandler = new MapleServerHandler(ServerType.CHANNEL, channel);
			InetSocketadd = new InetSocketAddress(port);
			acceptor.bind(InetSocketadd, serverHandler, acceptor_config);
			System.out.println(":: Channel " + getChannel() + ": Listening on " + ip + " ::");
			wci.serverReady();
			for (EventScriptManager esm : eventManagers) {
				esm.init();
			}
		} catch (IOException e) {
			System.out.println(":: Binding to " + ip + " failed (ch: " + getChannel() + ")" + e + " ::");
		}
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownListener()));
	}

	public final void shutdown() {
		shutdown = true;
		System.out.println("Channel " + channel + ", Saving hired merchants...");
		closeAllMerchant();
		System.out.println("Channel " + channel + ", Saving characters...");
		players.disconnectAll();
		System.out.println("Channel " + channel + ", Unbinding ports...");
		finishedShutdown = true;
		wci = null;
		cwi = null;
	}

	public final void unbind() {
		acceptor.unbindAll();
	}

	public final boolean hasFinishedShutdown() {
		return finishedShutdown;
	}

	public final MapleMapFactory getMapFactory(int world) {
		return mapFactories[world == 6 ? 0 : 1];
	}

	public static final ChannelServer newInstance(final String key) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
		final ChannelServer instance = new ChannelServer(key);
		pendingInstances.put(key, instance);
		return instance;
	}

	public static final ChannelServer getInstance(final int channel) {
		return instances.get(channel);
	}

	public final void addPlayer(final MapleCharacter chr) {
		players.registerPlayer(chr);
		chr.getClient().getSession().write(MaplePacketCreator.serverMessage(serverMessage));
	}

	public final PlayerStorage getPlayerStorage() {
		return players;
	}

	public final void removePlayer(final MapleCharacter chr) {
		players.deregisterPlayer(chr);
	}

	public final String getServerMessage() {
		return serverMessage;
	}

	public final void setServerMessage(final String newMessage) {
		serverMessage = newMessage;
		broadcastPacket(MaplePacketCreator.serverMessage(serverMessage));
	}

	public final void broadcastPacket(final MaplePacket data) {
		players.broadcastPacket(data);
	}

	public final void broadcastSmegaPacket(final MaplePacket data) {
		players.broadcastSmegaPacket(data);
	}

	public final void broadcastGMPacket(final MaplePacket data) {
		players.broadcastGMPacket(data);
	}

	public final int getChannel() {
		return channel;
	}

	public final void setChannel(final int channel) {
		if (pendingInstances.containsKey(key)) {
			pendingInstances.remove(key);
		}
		if (instances.containsKey(channel)) {
			instances.remove(channel);
		}
		instances.put(channel, this);
		this.channel = channel;
		for (MapleMapFactory mf : mapFactories) {
			if (mf != null) {
				mf.setChannel(channel);
			}
		}
	}

	public static final Collection<ChannelServer> getAllInstances() {
		return Collections.unmodifiableCollection(instances.values());
	}

	public final String getIP() {
		return ip;
	}

	public final String getIP(final int channel) {
		try {
			return getWorldInterface().getIP(channel);
		} catch (RemoteException e) {
			System.err.println("Lost connection to world server" + e);
			throw new RuntimeException("Lost connection to world server");
		}
	}

	public final WorldChannelInterface getWorldInterface() {
		synchronized (worldReady) {
			while (!worldReady) {
				try {
					worldReady.wait();
				} catch (InterruptedException e) {
				//
				}
			}
		}
		return wci;
	}

	public final String getProperty(final String name) {
		return props.getProperty(name);
	}

	public final boolean isShutdown() {
		return shutdown;
	}

	public final void shutdown(final int time) {
		TimerManager.getInstance().schedule(new ShutdownServer(getChannel()), time);
	}

	public final void shutdownWorld(final int time) {
		try {
			getWorldInterface().shutdown(time);
		} catch (RemoteException e) {
			reconnectWorld();
		}
	}

	public final void shutdownLogin() {
		try {
			getWorldInterface().shutdownLogin();
		} catch (RemoteException e) {
			reconnectWorld();
		}
	}

	public final int getLoadedMaps(int world) {
		return getMapFactory(world).getLoadedMaps();
	}

	public final EventScriptManager getEventSM(int world) {
		return eventManagers[world == 6 ? 0 : 1];
	}

	public final void reloadEvents() {
		for (int i = 0; i < eventManagers.length; i++) {
			eventManagers[i].cancel();
			eventManagers[i] = new EventScriptManager(this, props.getProperty("channel.events").split(","), i == 0 ? 6 : 5);
			eventManagers[i].init();
		}
	}

	public final byte getExpRate() {
		return expRate;
	}

	public final void setExpRate(final byte expRate) {
		this.expRate = expRate;
	}

	public final byte getMesoRate() {
		return mesoRate;
	}

	public final void setMesoRate(final byte mesoRate) {
		this.mesoRate = mesoRate;
	}

	public final byte getDropRate() {
		return dropRate;
	}

	public final void setDropRate(final byte dropRate) {
		this.dropRate = dropRate;
	}

	public final MapleGuild getGuild(final MapleGuildCharacter mgc) {
		final int gid = mgc.getGuildId();
		MapleGuild g = null;
		try {
			g = getWorldInterface().getGuild(gid, mgc);
		} catch (RemoteException re) {
			System.err.println("RemoteException while fetching MapleGuild." + re);
			return null;
		}
		if (gsStore.get(gid) == null) {
			gsStore.put(gid, new MapleGuildSummary(g));
		}
		return g;
	}

	public final MapleGuildSummary getGuildSummary(final int gid) {
		if (gsStore.containsKey(gid)) {
			return gsStore.get(gid);
		}
		try {
			final MapleGuild g = this.getWorldInterface().getGuild(gid, null);
			if (g != null) {
				gsStore.put(gid, new MapleGuildSummary(g));
			}
			return gsStore.get(gid);
		} catch (RemoteException re) {
			System.err.println("RemoteException while fetching GuildSummary." + re);
			return null;
		}
	}

	public final void updateGuildSummary(final int gid, final MapleGuildSummary mgs) {
		gsStore.put(gid, mgs);
	}

	public static final void startChannel_Main() {
		try {
			initialProp = new Properties();
			final FileReader channelConfig = new FileReader(System.getProperty("channel.config"));
			initialProp.load(channelConfig);
			channelConfig.close();
			final Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
			worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
			for (int i = 0; i < Integer.parseInt(initialProp.getProperty("channel.count", "0")); i++) {
				newInstance(ServerConstants.Channel_Key[i]).run_startup_configurations();
			}
			OdinSEA.start();
			DatabaseConnection.getConnection(); // touch - so we see database problems early...
		} catch (FileNotFoundException fnfe) {
			System.out.println("[EXCEPTION] Channel configuration not found.");
		} catch (IOException ioe) {
			System.out.println("[EXCEPTION] Unable to load configuration properties, please check if the file is in use");
		} catch (NotBoundException nbe) {
			System.out.println("[EXCEPTION] The host channel IPs is out of bounds.");
		} catch (InstanceAlreadyExistsException iaee) {
			System.out.println("[EXCEPTION] The channel instance is already opened!");
		} catch (MBeanRegistrationException nmre) {
			System.out.println("[EXCEPTION] Something went wrong with Java MBEAN Registration.");
		} catch (NotCompliantMBeanException ncmbe) {
			System.out.println("[EXCEPTION] Something went wrong with Java MBEAN.");
		} catch (MalformedObjectNameException mone) {
			System.out.println("[EXCEPTION] The channel IPs is invalid.");
		}
	}

	private final class ShutDownListener implements Runnable {
		@Override
		public void run() {
			System.out.println("Saving all Hired Merchant...");
			closeAllMerchant();
			System.out.println("Saving all connected clients...");
			players.disconnectAll();
			acceptor.unbindAll();
			finishedShutdown = true;
			wci = null;
			cwi = null;
		}
	}

	public final MapleSquad getMapleSquad(final String type) {
		return mapleSquads.get(type);
	}

	public final boolean addMapleSquad(final MapleSquad squad, final String type) {
		if (mapleSquads.get(type) == null) {
			mapleSquads.remove(type);
			mapleSquads.put(type, squad);
			return true;
		}
		return false;
	}

	public final boolean removeMapleSquad(final String type) {
		if (mapleSquads.containsKey(type)) {
			mapleSquads.remove(type);
			return true;
		}
		return false;
	}

	public final void closeAllMerchant() {
		merchant_mutex.lock();
		final Iterator<HiredMerchant> merchants_ = merchants.values().iterator();
		try {
			while (merchants_.hasNext()) {
			merchants_.next().closeShop(true, false);
			merchants_.remove();
			}
		} finally {
			merchant_mutex.unlock();
		}
	}

	public final int addMerchant(final HiredMerchant hMerchant) {
		merchant_mutex.lock();
		int runningmer = 0;
		try {
			runningmer = running_MerchantID;
			merchants.put(running_MerchantID, hMerchant);
			running_MerchantID++;
		} finally {
			merchant_mutex.unlock();
		}
		return runningmer;
	}

	public final void removeMerchant(final HiredMerchant hMerchant) {
		merchant_mutex.lock();
		try {
			merchants.remove(hMerchant.getStoreId());
		} finally {
			merchant_mutex.unlock();
		}
	}

	public final boolean constainsMerchant(final int accid) {
		boolean contains = false;
		merchant_mutex.lock();
		try {
			final Iterator itr = merchants.values().iterator();
			while (itr.hasNext()) {
				if (((HiredMerchant) itr.next()).getOwnerAccId() == accid) {
					contains = true;
					break;
				}
			}
		} finally {
			merchant_mutex.unlock();
		}
		return contains;
	}

	public final void toggleMegaponeMuteState() {
		this.MegaphoneMuteState = !this.MegaphoneMuteState;
	}

	public final boolean getMegaphoneMuteState() {
		return MegaphoneMuteState;
	}

	public final List<MapleCharacter> getPartyMembers(final MapleParty party) {
		List<MapleCharacter> partym = new LinkedList<MapleCharacter>();
		for (final MaplePartyCharacter partychar : party.getMembers()) {
			if (partychar.getChannel() == getChannel()) { // Make sure the thing doesn't get duplicate plays due to ccing bug.
				MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
				if (chr != null) {
					partym.add(chr);
				}
			}
		}
		return partym;
	}
}