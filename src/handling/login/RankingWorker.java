/*package handling.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DatabaseConnection;

public class RankingWorker implements Runnable {

    private Connection con;
    private long lastUpdate = System.currentTimeMillis();

    public void run() {
	try {
	    con = DatabaseConnection.getConnection();
	    con.setAutoCommit(false);
	    updateRanking(-1);
	    updateRanking(0);
	    updateRanking(100);
	    updateRanking(200);
	    updateRanking(300);
	    updateRanking(400);
	    updateRanking(500);
	    con.commit();
	    con.setAutoCommit(true);
	    lastUpdate = System.currentTimeMillis();
	} catch (SQLException ex) {
	    try {
		con.rollback();
		con.setAutoCommit(true);
		System.err.println("Could not update rankings" + ex);
	    } catch (SQLException ex2) {
		System.err.println("Could not rollback unfinished ranking transaction" + ex2);
	    }
	}
    }

    private void updateRanking(int job) throws SQLException {
	StringBuilder sb = new StringBuilder();
	sb.append("SELECT c.id, ");
	sb.append(job != -1 ? "c.jobRank, c.jobRankMove" : "c.rank, c.rankMove");
	sb.append(", a.lastlogin AS lastlogin, a.loggedin FROM characters AS c LEFT JOIN accounts AS a ON c.accountid = a.id WHERE c.gm = 0 ");
	if (job != -1) {
	    sb.append("AND c.job DIV 100 = ? ");
	}
	sb.append("ORDER BY c.level DESC , c.exp DESC , c.fame DESC , c.meso DESC");

	PreparedStatement charSelect = con.prepareStatement(sb.toString());
	if (job != -1) {
	    charSelect.setInt(1, job / 100);
	}
	ResultSet rs = charSelect.executeQuery();
	PreparedStatement ps = con.prepareStatement("UPDATE characters SET " + (job != -1 ? "jobRank = ?, jobRankMove = ? " : "rank = ?, rankMove = ? ") + "WHERE id = ?");
	int rank = 0;
	while (rs.next()) {
	    int rankMove = 0;
	    rank++;
	    if (rs.getLong("lastlogin") < lastUpdate || rs.getInt("loggedin") > 0) {
		rankMove = rs.getInt((job != -1 ? "jobRankMove" : "rankMove"));
	    }
	    rankMove += rs.getInt((job != -1 ? "jobRank" : "rank")) - rank;
	    ps.setInt(1, rank);
	    ps.setInt(2, rankMove);
	    ps.setInt(3, rs.getInt("id"));
	    ps.executeUpdate();
	}
	rs.close();
	charSelect.close();
	ps.close();
    }
}*/