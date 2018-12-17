package handling.world.guild;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import client.MapleCharacter;
import client.MapleClient;
import database.DatabaseConnection;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import handling.channel.remote.ChannelWorldInterface;
import handling.world.WorldRegistryImpl;
import tools.StringUtil;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MapleGuild implements java.io.Serializable {
	private static enum BCOp {
		NONE, DISBAND, EMBELMCHANGE
	}
	public static final long serialVersionUID = 6322150443228168192L;
	private final List<MapleGuildCharacter> members;
	private final String rankTitles[] = new String[5]; // 1 = master, 2 = jr, 5 = lowest member
	private String name, notice;
	private int id, gp, logo, logoColor, leader, capacity, logoBG, logoBGColor, signature;
	private final Map<Integer, List<Integer>> notifications = new LinkedHashMap<Integer, List<Integer>>();
	private boolean bDirty = true;
	private int allianceid = 0;
	private MapleAlliance ally;
	private Lock lock = new ReentrantLock();

	public MapleGuild(final MapleGuildCharacter initiator) {
		super();
		int guildid = initiator.getGuildId();
		members = new CopyOnWriteArrayList<MapleGuildCharacter>();
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid=" + guildid);
			ResultSet rs = ps.executeQuery();
			if (!rs.first()) {
				rs.close();
				ps.close();
				id = -1;
				return;
			}
			id = guildid;
			name = rs.getString("name");
			gp = rs.getInt("GP");
			logo = rs.getInt("logo");
			logoColor = rs.getInt("logoColor");
			logoBG = rs.getInt("logoBG");
			logoBGColor = rs.getInt("logoBGColor");
			capacity = rs.getInt("capacity");
			rankTitles[0] = rs.getString("rank1title");
			rankTitles[1] = rs.getString("rank2title");
			rankTitles[2] = rs.getString("rank3title");
			rankTitles[3] = rs.getString("rank4title");
			rankTitles[4] = rs.getString("rank5title");
			leader = rs.getInt("leader");
			notice = rs.getString("notice");
			signature = rs.getInt("signature");
			allianceid = rs.getInt("alliance");
			rs.close();
			ps.close();
			ps = con.prepareStatement("SELECT id, name, level, job, guildrank FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC");
			ps.setInt(1, guildid);
			rs = ps.executeQuery();
			if (!rs.first()) {
				System.err.println("No members in guild.  Impossible...");
				rs.close();
				ps.close();
				return;
			}
			do {
				members.add(new MapleGuildCharacter(rs.getInt("id"), rs.getShort("level"), rs.getString("name"), (byte) -1, rs.getInt("job"), rs.getInt("guildrank"), guildid, false));
			} while (rs.next());
			setOnline(initiator.getId(), true, initiator.getChannel());
			rs.close();
			ps.close();
		} catch (SQLException se) {
			System.err.println("unable to read guild information from sql" + se);
			return;
		}
	}

	public MapleGuild(final int guildid) { // retrieves the guild from database, with guildid
		members = new CopyOnWriteArrayList<MapleGuildCharacter>();
		try { // first read the guild information
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid=" + guildid);
			ResultSet rs = ps.executeQuery();
			if (!rs.first()) { // no result... most likely to be someone from a disbanded guild that got rolled back
				rs.close();
				ps.close();
				id = -1;
				return;
			}
			id = guildid;
			name = rs.getString("name");
			gp = rs.getInt("GP");
			logo = rs.getInt("logo");
			logoColor = rs.getInt("logoColor");
			logoBG = rs.getInt("logoBG");
			logoBGColor = rs.getInt("logoBGColor");
			capacity = rs.getInt("capacity");
			rankTitles[0] = rs.getString("rank1title");
			rankTitles[1] = rs.getString("rank2title");
			rankTitles[2] = rs.getString("rank3title");
			rankTitles[3] = rs.getString("rank4title");
			rankTitles[4] = rs.getString("rank5title");
			leader = rs.getInt("leader");
			notice = rs.getString("notice");
			signature = rs.getInt("signature");
			allianceid = rs.getInt("alliance");
			rs.close();
			ps.close();
		} catch (SQLException se) {
			System.err.println("unable to read guild information from sql" + se);
			return;
		}
	}

	private final void writeToDB(final boolean bDisband) {
		try {
			Connection con = DatabaseConnection.getConnection();
			if (!bDisband) {
				StringBuilder buf = new StringBuilder("UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");
				for (int i = 1; i < 6; i++) {
					buf.append("rank" + i + "title = ?, ");
				}
				buf.append("capacity = ?, " + "notice = ?, alliance = ? WHERE guildid = ?");
				PreparedStatement ps = con.prepareStatement(buf.toString());
				ps.setInt(1, gp);
				ps.setInt(2, logo);
				ps.setInt(3, logoColor);
				ps.setInt(4, logoBG);
				ps.setInt(5, logoBGColor);
				ps.setString(6, rankTitles[0]);
				ps.setString(7, rankTitles[1]);
				ps.setString(8, rankTitles[2]);
				ps.setString(9, rankTitles[3]);
				ps.setString(10, rankTitles[4]);
				ps.setInt(11, capacity);
				ps.setString(12, notice);
				ps.setInt(13, allianceid);
				ps.setInt(14, id);
				ps.execute();
				ps.close();
			} else {
				PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?");
				ps.setInt(1, id);
				ps.execute();
				ps.close();
				//delete the alliance
				if (allianceid > 0) {
					if (getAlliance(null).getGuilds().get(0).getLeaderId() == getLeaderId()) {
						ps = con.prepareStatement("DELETE FROM alliances WHERE id = ?");
						ps.setInt(1, allianceid);
						ps.execute();
						ps.close();
					}
				}
				ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
				ps.setInt(1, id);
				ps.execute();
				ps.close();
				broadcast(MaplePacketCreator.guildDisband(id));
			}
		} catch (SQLException se) {
			System.err.println("Error saving guild to SQL" + se);
		}
	}

	public final int getId() {
		return id;
	}

	public final int getLeaderId() {
		return leader;
	}

	public final MapleCharacter getLeader(final MapleClient c) {
		return c.getChannelServer().getPlayerStorage().getCharacterById(leader);
	}

	public final int getGP() {
		return gp;
	}

	public final int getLogo() {
		return logo;
	}

	public final void setLogo(final int l) {
		logo = l;
	}

	public final int getLogoColor() {
		return logoColor;
	}

	public final void setLogoColor(final int c) {
		logoColor = c;
	}

	public final int getLogoBG() {
		return logoBG;
	}

	public final void setLogoBG(final int bg) {
		logoBG = bg;
	}

	public final int getLogoBGColor() {
		return logoBGColor;
	}

	public final void setLogoBGColor(final int c) {
		logoBGColor = c;
	}

	public final String getNotice() {
		if (notice == null) {
			return "";
		}
		return notice;
	}

	public final int getAllianceId() {
		return allianceid;
	}

	public final MapleAlliance getAlliance(final MapleClient c) {
		if (ally != null) {
			return ally;
		} else if (allianceid > 0) {
			final MapleAlliance al = new MapleAlliance(c, allianceid);
			ally = al;
			return al;
		} else {
			return null;
		}
	}

	public final String getName() {
		return name;
	}

	public final int getCapacity() {
		return capacity;
	}

	public final int getSignature() {
		return signature;
	}

	public final void broadcast(final MaplePacket packet) {
		broadcast(packet, -1, BCOp.NONE);
	}

	public final void broadcast(final MaplePacket packet, final int exception) {
		broadcast(packet, exception, BCOp.NONE);
	}

	// multi-purpose function that reaches every member of guild (except the character with exceptionId) in all channels with as little access to rmi as possible
	public final void broadcast(final MaplePacket packet, final int exceptionId, final BCOp bcop) {
		final WorldRegistryImpl wr = WorldRegistryImpl.getInstance();
		final Set<Integer> chs = wr.getChannelServer();
		lock.lock();
		try {
			buildNotifications();
			try { // now call the channelworldinterface
				for (final Integer ch : chs) {
					final ChannelWorldInterface cwi = wr.getChannel(ch);
					if (notifications.get(ch).size() > 0) {
						if (bcop == BCOp.DISBAND) {
							cwi.setGuildAndRank(notifications.get(ch), 0, 5, exceptionId);
						} else if (bcop == BCOp.EMBELMCHANGE) {
							cwi.changeEmblem(id, notifications.get(ch), new MapleGuildSummary(this));
						} else {
							cwi.sendPacket(notifications.get(ch), packet, exceptionId);
						}
					}
				}
			} catch (RemoteException re) {
				System.err.println("Failed to contact channel(s) for broadcast." + re);
			}
		} finally {
			lock.unlock();
		}
	}

	private final void buildNotifications() {
		// any function that calls this should be wrapped in synchronized(notifications) to make sure that it doesn't change before that function finishes with the updated notifications
		if (!bDirty) {
			return;
		}
		final Set<Integer> chs = WorldRegistryImpl.getInstance().getChannelServer();
		if (notifications.keySet().size() != chs.size()) {
			notifications.clear();
			for (final Integer ch : chs) {
			notifications.put(ch, new java.util.LinkedList<Integer>());
			}
		} else {
			for (List<Integer> l : notifications.values()) {
				l.clear();
			}
		}
		for (final MapleGuildCharacter mgc : members) {
			if (!mgc.isOnline()) {
				continue;
			}
			final List<Integer> ch = notifications.get(mgc.getChannel());
			if (ch == null) {
				System.err.println("Unable to connect to channel " + mgc.getChannel());
			} else {
				ch.add(mgc.getId());
			}
		}
		bDirty = false;
	}

	public final void guildMessage(final MaplePacket serverNotice) {
		for (final MapleGuildCharacter mgc : members) {
			for (final ChannelServer cs : ChannelServer.getAllInstances()) {
				if (cs.getPlayerStorage().getCharacterById(mgc.getId()) != null) {
					final MapleCharacter chr = cs.getPlayerStorage().getCharacterById(mgc.getId());
					if (serverNotice != null) {
						chr.getClient().getSession().write(serverNotice);
					} else {
						chr.getMap().removePlayer(chr);
						chr.getMap().addPlayer(chr);
					}
				}
			}
		}
	}

	public final void setOnline(final int cid, final boolean online, final int channel) {
		boolean bBroadcast = true;
		for (final MapleGuildCharacter mgc : members) {
			if (mgc.getId() == cid) {
				if (mgc.isOnline() && online) {
					bBroadcast = false;
				}
				mgc.setOnline(online);
				mgc.setChannel((byte) channel);
				break;
			}
		}
		if (bBroadcast) {
			broadcast(MaplePacketCreator.guildMemberOnline(id, cid, online), cid);
		}
		bDirty = true; // member formation has changed, update notifications
	}

	public final void guildChat(final String name, final int cid, final String msg) {
		broadcast(MaplePacketCreator.multiChat(name, msg, 2), cid);
	}

	public final void allianceChat(final String name, final int cid, final String msg) {
		broadcast(MaplePacketCreator.multiChat(name, msg, 3), cid);
	}

	public final String getRankTitle(final int rank) {
		return rankTitles[rank - 1];
	}

	// function to create guild, returns the guild id if successful, 0 if not
	public static final int createGuild(final int leaderId, final String name) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {// name taken
				rs.close();
				ps.close();
				return 0;
			}
			ps.close();
			rs.close();
			ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `signature`, `alliance`) VALUES (?, ?, ?, 0)");
			ps.setInt(1, leaderId);
			ps.setString(2, name);
			ps.setInt(3, (int) System.currentTimeMillis());
			ps.execute();
			ps.close();
			ps = con.prepareStatement("SELECT guildid FROM guilds WHERE leader = ?");
			ps.setInt(1, leaderId);
			rs = ps.executeQuery();
			rs.first();
			final int result = rs.getInt("guildid");
			rs.close();
			ps.close();
			return result;
		} catch (SQLException se) {
			System.err.println("SQL THROW" + se);
			return 0;
		}
	}

	public final int addGuildMember(final MapleGuildCharacter mgc) {
		// first of all, insert it into the members keeping alphabetical order of lowest ranks ;)
		lock.lock();
		try {
			if (members.size() >= capacity) {
				return 0;
			}
			for (int i = members.size() - 1; i >= 0; i--) {
				if (members.get(i).getGuildRank() < 5 || members.get(i).getName().compareTo(mgc.getName()) < 0) {
				members.add(i + 1, mgc);
				bDirty = true;
				break;
				}
			}
		} finally {
			lock.unlock();
		}
		broadcast(MaplePacketCreator.newGuildMember(mgc));
		return 1;
	}

	public final void leaveGuild(final MapleGuildCharacter mgc) {
		broadcast(MaplePacketCreator.memberLeft(mgc, false));
		lock.lock();
		try {
			members.remove(mgc);
			bDirty = true;
		} finally {
			lock.unlock();
		}
	}

	public final void expelMember(final MapleGuildCharacter initiator, final String name, final int cid) {
		final Iterator<MapleGuildCharacter> itr = members.iterator();
		while (itr.hasNext()) {
			final MapleGuildCharacter mgc = itr.next();
			if (mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()) {
				broadcast(MaplePacketCreator.memberLeft(mgc, true));
				bDirty = true;
				members.remove(mgc);
				try {
					if (mgc.isOnline()) {
						WorldRegistryImpl.getInstance().getChannel(mgc.getChannel()).setGuildAndRank(cid, 0, 5);
					} else {
						try {
							Connection con = DatabaseConnection.getConnection();
							PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
							ps.setString(1, mgc.getName());
							ps.setString(2, initiator.getName());
							ps.setString(3, "You have been expelled from the guild.");
							ps.setLong(4, System.currentTimeMillis());
							ps.executeUpdate();
							ps.close();
						} catch (SQLException e) {
							System.err.println("Error sending guild msg 'expelled'." + e);
						}
						WorldRegistryImpl.getInstance().getChannel(1).setOfflineGuildStatus((short) 0, (byte) 5, cid);
					}
				} catch (RemoteException re) {
					re.printStackTrace();
					return;
				}
			}
		}
	}

	public final void changeRank(final int cid, final int newRank) {
		for (final MapleGuildCharacter mgc : members) {
			if (cid == mgc.getId()) {
				try {
					if (mgc.isOnline()) {
						WorldRegistryImpl.getInstance().getChannel(mgc.getChannel()).setGuildAndRank(cid, this.id, newRank);
					} else {
						WorldRegistryImpl.getInstance().getChannel(1).setOfflineGuildStatus((short) this.id, (byte) newRank, cid);
					}
				} catch (RemoteException re) {
					re.printStackTrace();
					return;
				}
				mgc.setGuildRank(newRank);
				broadcast(MaplePacketCreator.changeRank(mgc));
				return;
			}
		}
		// it should never get to this point unless cid was incorrect o_O
		System.err.println("INFO: unable to find the correct id for changeRank({" + cid + "}, {" + newRank + "})");
	}

	public final void setGuildNotice(final String notice) {
		this.notice = notice;
		broadcast(MaplePacketCreator.guildNotice(id, notice));
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE guilds SET notice = ? WHERE guildid = ?");
			ps.setString(1, notice);
			ps.setInt(2, id);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Saving notice ERROR" + e);
		}
	}

	public final void createAlliance(final MapleClient c, final String name) {
		if (allianceid != 0) {
			c.getPlayer().dropMessage(1, "You are already in an Alliance!");
			return;
		}
		if (checkAllianceName(name)) {
			try {
				if (name.equals("") || id <= 0) {
					return;
				}
				Connection con = DatabaseConnection.getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO `alliances` (notice, name, guild1, guild2, guild3, guild4, guild5, rank1, rank2, rank3, rank4, rank5) VALUES ('', ?, ?, 0, 0, 0, 0, 'Master', 'Jr. Master', 'Member', 'Member', 'Member')");
				ps.setString(1, name);
				ps.setInt(2, id);
				ps.executeUpdate();
				ps.close();
				ps = con.prepareStatement("SELECT id FROM alliances WHERE guild1 = ?");
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					allianceid = rs.getInt("id");
				}
				rs.close();
				ps.close();
				writeToDB(false);
				c.getPlayer().dropMessage(1, "Alliance successfully created!");
			} catch (SQLException a) {
				//
			}
		} else {
			c.getPlayer().dropMessage(1, "This name already exists.");
		}
	}

	public final void memberLevelJobUpdate(final MapleGuildCharacter mgc) {
		for (final MapleGuildCharacter member : members) {
			if (member.getId() == mgc.getId()) {
				member.setJobId(mgc.getJobId());
				member.setLevel((short) mgc.getLevel());
				broadcast(MaplePacketCreator.guildMemberLevelJobUpdate(mgc));
				break;
			}
		}
	}

	public final void changeRankTitle(final String[] ranks) {
		for (int i = 0; i < 5; i++) {
			rankTitles[i] = ranks[i];
		}
		broadcast(MaplePacketCreator.rankTitleChange(id, ranks));
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE guilds SET rank1title = ?, rank2title = ?, rank3title = ?, rank4title = ?, rank5title = ? WHERE guildid = ?");
			for (int i = 0; i < 5; i++) {
				ps.setString(i + 1, rankTitles[i]);
			}
			ps.setInt(6, id);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Saving rankTitle ERROR" + e);
		}
	}

	public final void disbandGuild() {
		writeToDB(true);
		broadcast(null, -1, BCOp.DISBAND);
	}

	public final void setGuildEmblem(final short bg, final byte bgcolor, final short logo, final byte logocolor) {
		this.logoBG = bg;
		this.logoBGColor = bgcolor;
		this.logo = logo;
		this.logoColor = logocolor;
		broadcast(null, -1, BCOp.EMBELMCHANGE);
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE guilds SET logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ? WHERE guildid = ?");
			ps.setInt(1, logo);
			ps.setInt(2, logoColor);
			ps.setInt(3, logoBG);
			ps.setInt(4, logoBGColor);
			ps.setInt(5, id);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Saving guild logo / BG colo ERROR" + e);
		}
	}

	public final MapleGuildCharacter getMGC(final int cid) {
		for (final MapleGuildCharacter mgc : members) {
			if (mgc.getId() == cid) {
				return mgc;
			}
		}
		return null;
	}

	public final boolean increaseCapacity() {
		if (capacity >= 100 || ((capacity + 5) > 100)) {
			return false;
		}
		capacity += 5;
		broadcast(MaplePacketCreator.guildCapacityChange(this.id, this.capacity));
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE guilds SET capacity = ? WHERE guildid = ?");
			ps.setInt(1, this.capacity);
			ps.setInt(2, this.id);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Saving guild capacity ERROR" + e);
		}
		return true;
	}

	public final void gainGP(final int amount) {
		gp += amount;
		guildMessage(MaplePacketCreator.updateGP(id, gp));
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE guilds SET gp = ? WHERE guildid = ?");
			ps.setInt(1, this.gp);
			ps.setInt(2, this.id);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Saving guild point ERROR" + e);
		}
	}

	public final void addMemberData(final MaplePacketLittleEndianWriter mplew) {
		mplew.write(members.size());
		for (final MapleGuildCharacter mgc : members) {
			mplew.writeInt(mgc.getId());
		}
		for (final MapleGuildCharacter mgc : members) {
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\0', 13));
			mplew.writeInt(mgc.getJobId());
			mplew.writeInt(mgc.getLevel());
			mplew.writeInt(mgc.getGuildRank());
			mplew.writeInt(mgc.isOnline() ? 1 : 0);
			mplew.writeInt(signature);
			mplew.writeInt(mgc.getGuildRank());
		}
	}

	// null indicates successful invitation being sent
	// keep in mind that this will be called by a handler most of the time
	// so this will be running mostly on a channel server, unlike the rest
	// of the class
	public static final MapleGuildResponse sendInvite(final MapleClient c, final String targetName) {
		final MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
		if (mc == null) {
			return MapleGuildResponse.NOT_IN_CHANNEL;
		}
		if (mc.getGuildId() > 0) {
			return MapleGuildResponse.ALREADY_IN_GUILD;
		}
		mc.getClient().getSession().write(MaplePacketCreator.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob()));
		return null;
	}

	public final boolean checkAllianceName(final String name) {
		boolean canCreate = true;
		if (name.length() < 4 && name.length() > 13) {
			canCreate = false;
		}
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM alliances WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				canCreate = false;
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			//
		}
		return canCreate;
	}
}