package handling.cashshop;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import client.SkillFactory;
import database.DatabaseConnection;
import handling.MapleServerHandler;
import handling.ServerConstants;
import handling.ServerType;
import handling.mina.MapleCodecFactory;
import handling.world.remote.WorldRegistry;
import handling.cashshop.remote.CashShopWorldInterface;
import handling.world.remote.CashShopInterface;
import server.TimerManager;
import server.MapleItemInformationProvider;
import server.CashItemFactory;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class CashShopServer {

	private String ip;
	private short PORT;
	private InetSocketAddress InetSocketadd;
	private IoAcceptor acceptor;
	private CashShopWorldInterface lwi;
	private CashShopInterface wli;
	private Properties initialProp, csProp;
	private Boolean worldReady = Boolean.TRUE;
	private WorldRegistry worldRegistry = null;
	private PlayerStorage_CS players;
	private static final CashShopServer instance = new CashShopServer();

	public static final CashShopServer getInstance() {
		return instance;
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
						FileReader fileReader = new FileReader(System.getProperty("channel.config")); // cs.config
						initialProp.load(fileReader);
						fileReader.close();
						ip = initialProp.getProperty("cs.net.interface");
						PORT = Short.parseShort(initialProp.getProperty("cs.net.port"));
						Registry registry = LocateRegistry.getRegistry(ip, Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
						ip += ":" + PORT;
						worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
						lwi = new CashShopWorldInterfaceImpl();
						wli = worldRegistry.registerCSServer(ServerConstants.CashShop_Key, ip, lwi);
						Properties dbProp = new Properties();
						fileReader = new FileReader("settings.ini"); // db.properties
						dbProp.load(fileReader);
						DatabaseConnection.setProps(dbProp);
						DatabaseConnection.getConnection();
						dbProp.clear();
						fileReader.close();
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
			FileReader fileReader = new FileReader(System.getProperty("channel.config")); // cs.config
			csProp = new Properties();
			csProp.load(fileReader);
			fileReader.close();
			PORT = Short.parseShort(csProp.getProperty("cs.net.port"));
			ip = csProp.getProperty("cs.net.interface") + ":" + PORT;
			initialProp = new Properties();
			final FileReader channelConfig = new FileReader(System.getProperty("channel.config"));
			initialProp.load(channelConfig);
			channelConfig.close();
			final Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("cs.net.interface"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
			worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
			lwi = new CashShopWorldInterfaceImpl(this);
			wli = worldRegistry.registerCSServer(ServerConstants.CashShop_Key, ip, lwi);
			Properties dbProp = new Properties();
			fileReader = new FileReader("settings.ini"); // db.properties
			dbProp.load(fileReader);
			DatabaseConnection.setProps(dbProp);
			DatabaseConnection.getConnection();
			dbProp.clear();
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(":: Could not connect to world server ::", e);
		}
		ByteBuffer.setUseDirectBuffers(false);
		ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
		acceptor = new SocketAcceptor();
		final SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		cfg.getSessionConfig().setTcpNoDelay(true);
		cfg.setDisconnectOnUnbind(true);
		cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
		TimerManager.getInstance().start();
		CashItemFactory.getInstance();
		SkillFactory.getSkill(99999999); // Load
		MapleItemInformationProvider.getInstance();
		players = new PlayerStorage_CS();
		try {
			InetSocketadd = new InetSocketAddress(PORT);
			acceptor.bind(InetSocketadd, new MapleServerHandler(ServerType.CASHSHOP), cfg);
			System.out.println(":: Listening on " + ip + " ::");
		} catch (final IOException e) {
			System.err.println(":: Binding to " + ip + " failed" + e + " ::");
		}
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownListener()));
	}

	public static void startCashShop_main() {
		try {
			CashShopServer.instance.run_startup_configurations();
		} catch (final Exception ex) {
			System.err.println(":: Error initializing Cash Shop server" + ex + " ::");
		}
	}

	public final String getIP() {
		return ip;
	}

	public final PlayerStorage_CS getPlayerStorage() {
		return players;
	}

	public final CashShopInterface getCSInterface() {
		synchronized (worldReady) {
			while (!worldReady) {
				try {
					worldReady.wait();
				} catch (final InterruptedException e) {
				}
			}
		}
		return wli;
	}

	public final void shutdown2() {
		System.out.println(":: Shutting down ::");
		try {
			worldRegistry.deregisterCSServer();
		} catch (final RemoteException e) {
			
		}
		System.exit(0);
	}

	public final void shutdown() {
		System.out.println(":: Saving all connected clients ::");
		players.disconnectAll();
		acceptor.unbindAll();
		System.exit(0);
	}

	private final class ShutDownListener implements Runnable {
		@Override
		public void run() {
			System.out.println(":: Saving all connected clients ::");
			players.disconnectAll();
			acceptor.unbindAll();
			shutdown2();
		}
	}
}