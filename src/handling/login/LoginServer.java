package handling.login;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import database.DatabaseConnection;
import handling.ServerConstants;
import handling.ServerType;
import handling.MapleServerHandler;
import handling.mina.MapleCodecFactory;
import handling.login.remote.LoginWorldInterface;
import handling.world.remote.WorldLoginInterface;
import handling.world.remote.WorldRegistry;
import server.TimerManager;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class LoginServer {

	public static final int PORT = 8484;
	private static InetSocketAddress InetSocketadd;
	private IoAcceptor acceptor;
	private static WorldRegistry worldRegistry = null;
	private final Map<Integer, String> channelServer = new HashMap<Integer, String>();
	private LoginWorldInterface lwi;
	private WorldLoginInterface wli;
	private Properties prop = new Properties();
	private final Properties initialProp = new Properties();
	private Boolean worldReady = Boolean.TRUE;
	private final Properties subnetInfo = new Properties();
	private Map<Integer, Integer> load = new HashMap<Integer, Integer>();
	private String serverName, eventMessage;
	private byte flag;
	private int maxCharacters, userLimit;
	private static final LoginServer instance = new LoginServer();

	public static final LoginServer getInstance() {
		return instance;
	}

	public final Set<Integer> getChannels() {
		return channelServer.keySet();
	}

	public final void addChannel(final int channel, final String ip) {
		channelServer.put(channel, ip);
		load.put(channel, 0);
	}

	public final void removeChannel(final int channel) {
		channelServer.remove(channel);
		load.remove(channel);
	}

	public final String getIP(final int channel) {
		return channelServer.get(channel);
	}

	public final void reconnectWorld() {
		try {
			wli.isAvailable();
		} catch (RemoteException ex) {
			synchronized (worldReady) {
				worldReady = Boolean.FALSE;
			}
			synchronized (lwi) {
				synchronized (worldReady) {
					if (worldReady) {
						return;
					}
				}
				System.out.println(":: Reconnecting to world server ::");
				synchronized (wli) {
					try {
						FileReader fileReader = new FileReader(System.getProperty("login.config"));
						initialProp.load(fileReader);
						fileReader.close();
						Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
						worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
						lwi = new LoginWorldInterfaceImpl();
						wli = worldRegistry.registerLoginServer(ServerConstants.Login_Key, lwi);
						Properties dbProp = new Properties();
						fileReader = new FileReader("settings.ini"); // db.properties
						dbProp.load(fileReader);
						fileReader.close();
						DatabaseConnection.setProps(dbProp);
						DatabaseConnection.getConnection();
						prop = wli.getWorldProperties();
						userLimit = Integer.parseInt(prop.getProperty("login.userlimit"));
						serverName = prop.getProperty("login.serverName");
						eventMessage = prop.getProperty("login.eventMessage");
						flag = Byte.parseByte(prop.getProperty("login.flag"));
						maxCharacters = Integer.parseInt(prop.getProperty("login.maxCharacters"));
					} catch (Exception e) {
						System.err.println(":: Reconnecting failed" + e + " ::");
					}
					worldReady = Boolean.TRUE;
				}
			}
			synchronized (worldReady) {
				worldReady.notifyAll();
			}
		}
	}

	public final void run_startup_configurations() {
		try {
			FileReader fileReader = new FileReader(System.getProperty("login.config"));
			initialProp.load(fileReader);
			fileReader.close();
			Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
			worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
			lwi = new LoginWorldInterfaceImpl();
			wli = worldRegistry.registerLoginServer(ServerConstants.Login_Key, lwi);
			Properties dbProp = new Properties();
			fileReader = new FileReader("settings.ini"); // db.properties
			dbProp.load(fileReader);
			fileReader.close();
			DatabaseConnection.setProps(dbProp);
			DatabaseConnection.getConnection();
			prop = wli.getWorldProperties();
			userLimit = Integer.parseInt(prop.getProperty("login.userlimit"));
			serverName = prop.getProperty("login.serverName");
			eventMessage = prop.getProperty("login.eventMessage");
			flag = Byte.parseByte(prop.getProperty("login.flag"));
			maxCharacters = Integer.parseInt(prop.getProperty("login.maxCharacters"));
			try {
				final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET loggedin = 0");
				ps.executeUpdate();
				ps.close();
			} catch (SQLException ex) {
				throw new RuntimeException("[Exception] Please check if the SQL server is active.");
			}
		} catch (RemoteException re) {
			throw new RuntimeException("[Exception] Could not connect to world server. Please check settings.ini if it's configured properly.");
		} catch (FileNotFoundException fnfe) {
			throw new RuntimeException("[Exception] File for login or database configuration not found, please check again.");
		} catch (IOException ioe) {
			throw new RuntimeException("[Exception] Failed or interrupted I/O operations.");
		} catch (NotBoundException nbe) {
			throw new RuntimeException("[Exception] Attempting lookup or unbind in the registry a name that has no associated binding.");
		}
		ByteBuffer.setUseDirectBuffers(false);
		ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
		acceptor = new SocketAcceptor();
		final SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		cfg.getSessionConfig().setTcpNoDelay(true);
		cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
		TimerManager.getInstance().start();
		LoginInformationProvider.getInstance();
		try {
			InetSocketadd = new InetSocketAddress(PORT);
			acceptor.bind(InetSocketadd, new MapleServerHandler(ServerType.LOGIN), cfg);
			System.out.println(":: Listening on port " + PORT + " ::");
		} catch (IOException e) {
			System.err.println(":: Binding to port " + PORT + " failed" + e + " ::");
		}
	}

	public final void shutdown() {
		System.out.println(":: Shutting Down ::");
		try {
			worldRegistry.deregisterLoginServer(lwi);
		} catch (RemoteException e) {
			System.out.println(":: [Login Server] Error Shutting Down " + e + " ::");
		}
		TimerManager.getInstance().stop();
		System.exit(0);
	}

	public final WorldLoginInterface getWorldInterface() {
		synchronized (worldReady) {
			while (!worldReady) {
				try {
					worldReady.wait();
				} catch (InterruptedException e) {
					System.out.println(":: [Login Server] Error Getting World Interface " + e + " ::");
				}
			}
		}
		return wli;
	}

	public static final void startLogin_Main() {
		try {
			LoginServer.getInstance().run_startup_configurations();
		} catch (Exception ex) {
			System.err.println(":: [Login Server] Error Initializing Login Server " + ex + " ::");
		}
	}

	public final Properties getSubnetInfo() {
		return subnetInfo;
	}

	public final String getServerName() {
		return serverName;
	}

	public final String getEventMessage() {
		return eventMessage;
	}

	public final byte getFlag() {
		return flag;
	}

	public final int getMaxCharacters() {
		return maxCharacters;
	}

	public final Map<Integer, Integer> getLoad() {
		return load;
	}

	public void setLoad(final Map<Integer, Integer> load) {
		this.load = load;
	}

	public final void setEventMessage(final String newMessage) {
		this.eventMessage = newMessage;
	}

	public final void setFlag(final byte newflag) {
		flag = newflag;
	}

	public final int getNumberOfSessions() {
		return acceptor.getManagedSessions(InetSocketadd).size();
	}

	public final int getUserLimit() {
		return userLimit;
	}

	public final void setUserLimit(final int newLimit) {
		userLimit = newLimit;
	}
}