package client;

import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.Serializable;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import database.DatabaseConnection;
import server.MapleItemInformationProvider;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;

public class MaplePet implements Serializable {
	private static final long serialVersionUID = 9179541993413738569L;
	private String name;
	private int Fh = 0, stance = 0, fullness = 100, level = 1, closeness = 0, uniqueid, petitemid;
	private Point pos;
	private short inventorypos = 0;
	private boolean summoned;

	private MaplePet(final int petitemid) {
		this.petitemid = petitemid;
	}

	private MaplePet(final int petitemid, final int uniqueid, final short inventorypos) {
		this.petitemid = petitemid;
		this.uniqueid = uniqueid;
		this.summoned = false;
		this.inventorypos = inventorypos;
	}

	public static final MaplePet loadFromDb(final int itemid, final int petid, final short inventorypos) {
	try {
		final MaplePet ret = new MaplePet(itemid, petid, inventorypos);

		Connection con = DatabaseConnection.getConnection(); // Get a connection to the database
		PreparedStatement ps = con.prepareStatement("SELECT * FROM pets WHERE petid = ?"); // Get pet details..
		ps.setInt(1, petid);

		final ResultSet rs = ps.executeQuery();
		rs.next();

		ret.setName(rs.getString("name"));
		ret.setCloseness(rs.getInt("closeness"));
		ret.setLevel(rs.getInt("level"));
		ret.setFullness(rs.getInt("fullness"));

		rs.close();
		ps.close();

		return ret;
	} catch (SQLException ex) {
		Logger.getLogger(MaplePet.class.getName()).log(Level.SEVERE, null, ex);
		return null;
	}
	}

	public final void saveToDb() {
	try {
		final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ? WHERE petid = ?");
		ps.setString(1, name); // Set name
		ps.setInt(2, level); // Set Level
		ps.setInt(3, closeness); // Set Closeness
		ps.setInt(4, fullness); // Set Fullness
		ps.setInt(5, uniqueid); // Set ID
		ps.executeUpdate(); // Execute statement
		ps.close();
	} catch (final SQLException ex) {
		ex.printStackTrace();
	}
	}

	public static final MaplePet createPet(final int itemid) {
	int ret;
	try { // Commit to db first
		final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO pets (name, level, closeness, fullness) VALUES (?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
		ps.setString(1, MapleItemInformationProvider.getInstance().getName(itemid));
		ps.setInt(2, 1);
		ps.setInt(3, 0);
		ps.setInt(4, 100);
		ps.executeUpdate();

		final ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		ret = rs.getInt(1);
		rs.close();
		ps.close();
	} catch (final SQLException ex) {
		ex.printStackTrace();
		return null;
	}
	final MaplePet pet = new MaplePet(itemid);
	pet.setName(MapleItemInformationProvider.getInstance().getName(itemid));
	pet.setLevel(1);
	pet.setCloseness(0);
	pet.setFullness(100);
	pet.setUniqueId(ret);

	return pet;
	}

	public final String getName() {
	return name;
	}

	public final void setName(final String name) {
	this.name = name;
	}

	public final boolean getSummoned() {
	return summoned;
	}

	public final void setSummoned(final boolean summoned) {
	this.summoned = summoned;
	}

	public final short getInventoryPosition() {
	return inventorypos;
	}

	public final void setInventoryPosition(final short inventorypos) {
	this.inventorypos = inventorypos;
	}

	public int getUniqueId() {
	return uniqueid;
	}

	public void setUniqueId(int id) {
	this.uniqueid = id;
	}

	public final int getCloseness() {
	return closeness;
	}

	public final void setCloseness(final int closeness) {
	this.closeness = closeness;
	}

	public final int getLevel() {
	return level;
	}

	public final void setLevel(final int level) {
	this.level = level;
	}

	public final int getFullness() {
	return fullness;
	}

	public final void setFullness(final int fullness) {
	this.fullness = fullness;
	}

	public final int getFh() {
	return Fh;
	}

	public final void setFh(final int Fh) {
	this.Fh = Fh;
	}

	public final Point getPos() {
	return pos;
	}

	public final void setPos(final Point pos) {
	this.pos = pos;
	}

	public final int getStance() {
	return stance;
	}

	public final void setStance(final int stance) {
	this.stance = stance;
	}

	public final int getPetItemId() {
	return petitemid;
	}

	public final boolean canConsume(final int itemId) {
	final MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
	for (final int petId : mii.petsCanConsume(itemId)) {
		if (petId == petitemid) {
		return true;
		}
	}
	return false;
	}

	public final void updatePosition(final List<LifeMovementFragment> movement) {
	for (final LifeMovementFragment move : movement) {
		if (move instanceof LifeMovement) {
		if (move instanceof AbsoluteLifeMovement) {
			setPos(((LifeMovement) move).getPosition());
		}
		setStance(((LifeMovement) move).getNewstate());
		}
	}
	}
}