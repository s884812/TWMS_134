
package client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.Serializable;

import database.DatabaseConnection;
import server.MapleItemInformationProvider;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.MonsterBookPacket;

public class MonsterBook implements Serializable {

    private static final long serialVersionUID = 7179541993413738569L;
    private boolean changed = false;
    private int SpecialCard = 0, NormalCard = 0, BookLevel = 1;
    private final Map<Integer, Integer> cards = new LinkedHashMap<Integer, Integer>();

    public final int getTotalCards() {
	return SpecialCard + NormalCard;
    }

    public final void loadCards(final int charid) throws SQLException {
	final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM monsterbook WHERE charid = ? ORDER BY cardid ASC");
	ps.setInt(1, charid);
	final ResultSet rs = ps.executeQuery();

	int cardid, level;

	while (rs.next()) {
	    cardid = rs.getInt("cardid");
	    level = rs.getInt("level");

	    if (GameConstants.isSpecialCard(cardid)) {
		SpecialCard += level;
	    } else {
		NormalCard += level;
	    }
	    cards.put(cardid, level);
	}
	rs.close();
	ps.close();

	calculateLevel();
    }

    public final void saveCards(final int charid) throws SQLException {
	if (!changed || cards.size() == 0) {
	    return;
	}
	final Connection con = DatabaseConnection.getConnection();
	PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
	ps.setInt(1, charid);
	ps.execute();
	ps.close();

	boolean first = true;
	final StringBuilder query = new StringBuilder();

	for (final Entry<Integer, Integer> all : cards.entrySet()) {
	    if (first) {
		first = false;
		query.append("INSERT INTO monsterbook VALUES (DEFAULT,");
	    } else {
		query.append(",(DEFAULT,");
	    }
	    query.append(charid);
	    query.append(",");
	    query.append(all.getKey()); // Card ID
	    query.append(",");
	    query.append(all.getValue()); // Card level
	    query.append(")");
	}
	ps = con.prepareStatement(query.toString());
	ps.execute();
	ps.close();
    }

    private final void calculateLevel() {
	int Size = NormalCard + SpecialCard;
	BookLevel = 8;

	for (int i = 0; i < 8; i++) {
	    if (Size <= GameConstants.getBookLevel(i)) {
		BookLevel = (i + 1);
		break;
	    }
	}
    }

    public final void addCardPacket(final MaplePacketLittleEndianWriter mplew) {
	mplew.writeShort(cards.size());

	/*for (Entry<Integer, Integer> all : cards.entrySet()) {
	    mplew.writeShort(GameConstants.getCardShortId(all.getKey())); // Id
	    mplew.write(all.getValue()); // Level
	}*/
    }

    public final void addCharInfoPacket(final int bookcover, final MaplePacketLittleEndianWriter mplew) {
	//mplew.writeInt(BookLevel);
	mplew.writeInt(NormalCard);
	mplew.writeInt(SpecialCard);
	mplew.writeInt(NormalCard + SpecialCard);
	//mplew.writeInt(MapleItemInformationProvider.getInstance().getCardMobId(bookcover));
    }

    public final void updateCard(final MapleClient c, final int cardid) {
	c.getSession().write(MonsterBookPacket.changeCover(cardid));
    }

    public final void addCard(final MapleClient c, final int cardid) {
	changed = true;
//	c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MonsterBookPacket.showForeginCardEffect(c.getPlayer().getId()), false);

	for (final Entry<Integer, Integer> all : cards.entrySet()) {
	    if (all.getKey() == cardid) {

		if (all.getValue() >= 5) {
		    c.getSession().write(MonsterBookPacket.addCard(true, cardid, all.getValue()));
		} else {
		    c.getSession().write(MonsterBookPacket.addCard(false, cardid, all.getValue()));
		    c.getSession().write(MonsterBookPacket.showGainCard(cardid));
		    all.setValue(all.getValue() + 1);
		    calculateLevel();
		}
		return;
	    }
	}
	// New card
	cards.put(cardid, 1);
	c.getSession().write(MonsterBookPacket.addCard(false, cardid, 1));
	c.getSession().write(MonsterBookPacket.showGainCard(cardid));
	calculateLevel();
    }
}