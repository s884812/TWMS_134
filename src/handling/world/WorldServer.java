package handling.world;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import database.DatabaseConnection;

public class WorldServer {

	private final static WorldServer instance = new WorldServer();
	private int worldId;
	private Properties dbProp = new Properties();
	private Properties worldProp = new Properties();

	private WorldServer() {
		try {
			InputStreamReader is = new FileReader("settings.ini"); // db.properties
			dbProp.load(is);
			is.close();
			DatabaseConnection.setProps(dbProp);
			DatabaseConnection.getConnection();
			is = new FileReader("settings.ini"); // world.properties
			worldProp.load(is);
			is.close();
		} catch (final Exception e) {
			System.err.println("Could not configuration" + e);
		}
	}

	public static final WorldServer getInstance() {
		return instance;
	}

	public final int getWorldId() {
		return worldId;
	}

	public final Properties getDbProp() {
		return dbProp;
	}

	public final Properties getWorldProp() {
		return worldProp;
	}

	public static final void startWorld_Main() {
		try {
			final Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
			registry.rebind("WorldRegistry", WorldRegistryImpl.getInstance());
		} catch (final RemoteException ex) {
			System.err.println("Could not initialize RMI system" + ex);
		}
	}
}