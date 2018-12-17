package client;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.regex.Pattern;

import database.DatabaseConnection;

public class MapleCharacterUtil {
	private static final Pattern namePattern = Pattern.compile("[a-zA-Z0-9_-]{3,12}");
	private static final Pattern petPattern = Pattern.compile("[a-zA-Z0-9_-]{4,12}");
	public static final boolean canCreateChar(final String name) {
		if (name.length() < 3 || name.length() > 15 || getIdByName(name) != -1) {
			return false;
		}
		return true;
	}

	public static final boolean canChangePetName(final String name) {
		if (petPattern.matcher(name).matches()) {
			return true;
		}
		return false;
	}

	public static final String makeMapleReadable(final String in) {
		String wui = in.replace('I', 'i');
		wui = wui.replace('l', 'L');
		wui = wui.replace("rn", "Rn");
		wui = wui.replace("vv", "Vv");
		wui = wui.replace("VV", "Vv");
		return wui;
	}

	public static final int getIdByName(final String name) {
		Connection con = DatabaseConnection.getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?");
			ps.setString(1, name);
			final ResultSet rs = ps.executeQuery();

			if (!rs.next()) {
			rs.close();
			ps.close();
			return -1;
			}
			final int id = rs.getInt("id");
			rs.close();
			ps.close();

			return id;
		} catch (SQLException e) {
			System.err.println("error 'getIdByName' " + e);
		}
		return -1;
	}

	// -2 = An unknown error occured
	// -1 = Account not found on database
	// 0 = You do not have a second password set currently.
	// 1 = The password you have input is wrong
	// 2 = Password Changed successfully
	public static final int Change_SecondPassword(final int accid, final String password, final String newpassword) {
		Connection con = DatabaseConnection.getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * from accounts where id = ?");
			ps.setInt(1, accid);
			final ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
				return -1;
			}
			String secondPassword = rs.getString("2ndpassword");
			final String salt2 = rs.getString("salt2");
			if (secondPassword != null && salt2 != null) {
				secondPassword = LoginCrypto.rand_r(secondPassword);
			} else if (secondPassword == null && salt2 == null) {
				rs.close();
				ps.close();
				return 0;
			}
			if (!check_ifPasswordEquals(secondPassword, password, salt2)) {
				rs.close();
				ps.close();
				return 1;
			}
			rs.close();
			ps.close();
			String SHA1hashedsecond;
			try {
				SHA1hashedsecond = LoginCryptoLegacy.encodeSHA1(newpassword);
			} catch (Exception e) {
				return -2;
			}
			ps = con.prepareStatement("UPDATE accounts set 2ndpassword = ?, salt2 = ? where id = ?");
			ps.setString(1, SHA1hashedsecond);
			ps.setString(2, null);
			ps.setInt(3, accid);
			if (!ps.execute()) {
				ps.close();
				return 2;
			}
			ps.close();
			return -2;
		} catch (SQLException e) {
			System.err.println("error 'getIdByName' " + e);
			return -2;
		}
	}

	private static final boolean check_ifPasswordEquals(final String passhash, final String pwd, final String salt) {
		// Check if the passwords are correct here. :B
		if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(pwd, passhash)) {
			// Check if a password upgrade is needed.
			return true;
		} else if (salt == null && LoginCrypto.checkSha1Hash(passhash, pwd)) {
			return true;
		} else if (LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt)) {
			return true;
		}
		return false;
	}
}