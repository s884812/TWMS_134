package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public class DatabaseConnection {

	private static final ThreadLocal<Connection> con = new ThreadLocalConnection();
	private static Properties props = null;
	public static final int CLOSE_CURRENT_RESULT = 1;
	public static final int KEEP_CURRENT_RESULT = 2;
	public static final int CLOSE_ALL_RESULTS = 3;
	public static final int SUCCESS_NO_INFO = -2;
	public static final int EXECUTE_FAILED = -3;
	public static final int RETURN_GENERATED_KEYS = 1;
	public static final int NO_GENERATED_KEYS = 2;

	public static final Connection getConnection() {
		if (props == null) {
			throw new RuntimeException("DatabaseConnection not initialized");
		}
		return con.get();
	}

	public static final void setProps(final Properties aProps) {
		props = aProps;
	}

	public static final void closeAll() throws SQLException {
		for (final Connection con : ThreadLocalConnection.allConnections) {
			con.close();
		}
	}

	private static final class ThreadLocalConnection extends ThreadLocal<Connection> {
		public static final Collection<Connection> allConnections = new LinkedList<Connection>();
		@Override
		protected final Connection initialValue() {
			String driver = props.getProperty("driver"); // qualified class name of your JDBC driver
			String url = props.getProperty("url"); // JDBC URL to your database
			String user = props.getProperty("user"); // credentials for database access
			String password = props.getProperty("pass");
			try {
				Class.forName(driver);
			} catch (final ClassNotFoundException e) {
				System.err.println("ERROR" + e);
			}
			try {
				final Connection con = DriverManager.getConnection(url, user, password);
				allConnections.add(con);
				return con;
			} catch (SQLException e) {
				System.err.println("ERROR" + e);
				return null;
			}
		}
	}
}