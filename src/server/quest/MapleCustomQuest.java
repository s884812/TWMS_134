package server.quest;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import database.DatabaseConnection;

public class MapleCustomQuest extends MapleQuest implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;

    public MapleCustomQuest(int id) {
	try {
	    this.id = id;
	    startActs = new LinkedList<MapleQuestAction>();
	    completeActs = new LinkedList<MapleQuestAction>();
	    startReqs = new LinkedList<MapleQuestRequirement>();
	    completeReqs = new LinkedList<MapleQuestRequirement>();

	    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM questrequirements WHERE questid = ?");
	    ps.setInt(1, id);
	    ResultSet rs = ps.executeQuery();
	    MapleQuestRequirement req;
	    MapleCustomQuestData data;
	    while (rs.next()) {
		Blob blob = rs.getBlob("data");
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(blob.getBytes(1, (int) blob.length())));
		data = (MapleCustomQuestData) ois.readObject();
		req = new MapleQuestRequirement(this, MapleQuestRequirementType.getByWZName(data.getName()), data);
		final byte status = rs.getByte("status");
		if (status == 0) {
		    startReqs.add(req);
		} else if (status == 1) {
		    completeReqs.add(req);
		}
	    }
	    rs.close();
	    ps.close();

	    ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM questactions WHERE questid = ?");
	    ps.setInt(1, id);
	    rs = ps.executeQuery();
	    MapleQuestAction act;
	    while (rs.next()) {
		Blob blob = rs.getBlob("data");
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(blob.getBytes(1, (int) blob.length())));
		data = (MapleCustomQuestData) ois.readObject();
		act = new MapleQuestAction(MapleQuestActionType.getByWZName(data.getName()), data, this);
		final byte status = rs.getByte("status");
		if (status == 0) {
		    startActs.add(act);
		} else if (status == 1) {
		    completeActs.add(act);
		}
	    }
	    rs.close();
	    ps.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.err.println("Error loading custom quest from SQL." + ex);
	}
    }
}
