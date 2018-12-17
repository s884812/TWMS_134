package scripting;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.rmi.RemoteException;

import client.Equip;
import client.ISkill;
import client.IItem;
import client.MapleCharacter;
import client.GameConstants;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.SkillFactory;
import client.SkillEntry;
import client.MapleStat;
import server.MapleCarnivalParty;
import server.Randomizer;
import server.MapleInventoryManipulator;
import server.MapleShopFactory;
import server.MapleSquad;
import server.maps.MapleMap;
import server.maps.Event_DojoAgent;
import server.maps.AramiaFireWorks;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packet.PlayerShopPacket;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import database.DatabaseConnection;
import handling.world.MaplePartyCharacter;
import server.MapleCarnivalChallenge;
import server.MapleItemInformationProvider;

public class NPCConversationManager extends AbstractPlayerInteraction {

	private MapleClient c;
	private int npc, questid;
	private String getText;
	private byte type; // -1 = NPC, 0 = start quest, 1 = end quest
	public boolean pendingDisposal = false;

	public NPCConversationManager(MapleClient c, int npc, int questid, byte type) {
		super(c);
		this.c = c;
		this.npc = npc;
		this.questid = questid;
		this.type = type;
	}

	public int getNpc() {
		return npc;
	}

	public static int MAX_REBORNS = 3;

	public int getReborns() {
		return getPlayer().getReborns();
	}

	public int getVPoints(){
		return getPlayer().getVPoints();
	}

	public void gainVPoints(int gainedpoints){
		c.getPlayer().gainVPoints(gainedpoints);
	}

	public int getNX() {
		return getPlayer().getNX();
	}

	public int getWorld() {
		return getPlayer().getWorld();
	}

	public int getQuest() {
		return questid;
	}

	public void giveBuff(int skill, int level) {
		SkillFactory.getSkill(skill).getEffect(level).applyTo(c.getPlayer());
	}

	public byte getType() {
		return type;
	}

	public void safeDispose() {
		pendingDisposal = true;
	}

	public void dispose() {
		NPCScriptManager.getInstance().dispose(c);
	}

	public void askMapSelection(final String sel) {
		c.getSession().write(MaplePacketCreator.getMapSelection(npc, sel));
	}

	public void sendNext(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
	}

	public void sendNextS(String text, byte type) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", type));
	}

	public void sendPrev(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
	}

	public void sendPrevS(String text, byte type) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", type));
	}

	public void sendNextPrev(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
	}

	public void sendNextPrevS(String text, byte type) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", type));
	}

	public void sendOk(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
	}

	public void sendOkS(String text, byte type) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", type));
	}

	public void sendYesNo(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 2, text, "", (byte) 0));
	}

	public void sendYesNoS(String text, byte type) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 2, text, "", type));
	}

	public void askAcceptDecline(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", (byte) 0));
	}

	public void askAcceptDeclineNoESC(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0E, text, "", (byte) 0));
	}

	public void askAvatar(String text, int... args) {
		c.getSession().write(MaplePacketCreator.getNPCTalkStyle(npc, text, args));
	}

	public void sendSimple(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 5, text, "", (byte) 0));
	}

	public void sendGetNumber(String text, int def, int min, int max) {
		c.getSession().write(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
	}

	public void sendGetText(String text) {
		c.getSession().write(MaplePacketCreator.getNPCTalkText(npc, text));
	}

	public void setGetText(String text) {
		this.getText = text;
	}

	public String getText() {
		return getText;
	}



	public int setRandomAvatar(int ticket, int... args_all) {
		if (!haveItem(ticket)) {
			return -1;
		}
		gainItem(ticket, (short) -1);
		int args = args_all[Randomizer.nextInt(args_all.length)];
		if (args < 100) {
			c.getPlayer().setSkinColor(args);
			c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
		} else if (args < 30000) {
			c.getPlayer().setFace(args);
			c.getPlayer().updateSingleStat(MapleStat.FACE, args);
		} else {
			c.getPlayer().setHair(args);
			c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
		}
		c.getPlayer().equipChanged();
		return 1;
	}

	public int setAvatar(int ticket, int args) {
		if (!haveItem(ticket)) {
			return -1;
		}
		gainItem(ticket, (short) -1);
		if (args < 100) {
			c.getPlayer().setSkinColor(args);
			c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
		} else if (args < 30000) {
			c.getPlayer().setFace(args);
			c.getPlayer().updateSingleStat(MapleStat.FACE, args);
		} else {
			c.getPlayer().setHair(args);
			c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
		}
		c.getPlayer().equipChanged();
		return 1;
	}

	public void sendStorage() {
		c.getPlayer().setConversation(4);
		c.getPlayer().getStorage().sendStorage(c, npc);
	}

	public void openShop(int id) {
		MapleShopFactory.getInstance().getShop(id).sendShop(c);
	}

	public int gainGachaponItem(int id, int quantity) {
		final IItem item = MapleInventoryManipulator.addbyId_Gachapon(c, id, (short) quantity);
		if (item == null) {
			return -1;
		}
		final byte rareness = GameConstants.gachaponRareItem(item.getItemId());
		if (rareness > 0) {
			try {
				c.getChannelServer().getWorldInterface().broadcastMessage(MaplePacketCreator.getGachaponMega(c.getPlayer().getName(), " : Lucky winner of Gachapon! Congratulations~", item, rareness).getBytes());
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
		}
		return item.getItemId();
	}

	public void changeJob(int job) {
		c.getPlayer().changeJob(job);
	}

	public void startQuest(int id) {
		MapleQuest.getInstance(id).start(getPlayer(), npc);
	}

	public void completeQuest(int id) {
		MapleQuest.getInstance(id).complete(getPlayer(), npc);
	}

	public void forfeitQuest(int id) {
		MapleQuest.getInstance(id).forfeit(getPlayer());
	}

	public void forceStartQuest() {
		MapleQuest.getInstance(questid).forceStart(getPlayer(), getNpc(), null);
	}

	public void forceStartQuest(int id) {
		MapleQuest.getInstance(id).forceStart(getPlayer(), getNpc(), null);
	}

	public void forceStartQuest(String customData) {
		MapleQuest.getInstance(questid).forceStart(getPlayer(), getNpc(), customData);
	}

	public void forceCompleteQuest() {
		MapleQuest.getInstance(questid).forceComplete(getPlayer(), getNpc());
	}

	public String getQuestCustomData() {
		return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(questid)).getCustomData();
	}

	public void setQuestCustomData(String customData) {
		getPlayer().getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(customData);
	}

	public int getMeso() {
		return getPlayer().getMeso();
	}

	public void gainAp(final int amount) {
		c.getPlayer().gainAp(amount);
	}

	public void gainMeso(int gain) {
		c.getPlayer().gainMeso(gain, true, false, true);
	}

	public void gainExp(int gain) {
		c.getPlayer().gainExp(gain, true, true, true);
	}

	public void expandInventory(byte type, int amt) {
		c.getPlayer().getInventory(MapleInventoryType.getByType(type)).addSlot((byte) 4);
	}

	public void unequipEverything() {
		MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
		MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
		List<Short> ids = new LinkedList<Short>();
		for (IItem item : equipped.list()) {
			ids.add(item.getPosition());
		}
		for (short id : ids) {
			MapleInventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
		}
	}

	public final void clearSkills() {
		Map<ISkill, SkillEntry> skills = getPlayer().getSkills();
		for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
			getPlayer().changeSkillLevel(skill.getKey(), (byte) 0, (byte) 0);
		}
	}

	public final boolean isCash(final int itemid) {
		return MapleItemInformationProvider.getInstance().isCash(itemid);
	}

	public boolean hasSkill(int skillid) {
		ISkill theSkill = SkillFactory.getSkill(skillid);
		if (theSkill != null) {
			return c.getPlayer().getSkillLevel(theSkill) > 0;
		}
		return false;
	}

	public MapleCharacter getChar() {
		return getPlayer();
	}

	public MapleClient getC() {
		return c;
	}

	public void showEffect(boolean broadcast, String effect) {
		if (broadcast) {
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
		} else {
			c.getSession().write(MaplePacketCreator.showEffect(effect));
		}
	}

	public void playSound(boolean broadcast, String sound) {
		if (broadcast) {
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
		} else {
			c.getSession().write(MaplePacketCreator.playSound(sound));
		}
	}

	public void environmentChange(boolean broadcast, String env) {
		if (broadcast) {
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, 2));
		} else {
			c.getSession().write(MaplePacketCreator.environmentChange(env, 2));
		}
	}

	public void updateBuddyCapacity(int capacity) {
		c.getPlayer().setBuddyCapacity(capacity);
	}

	public int getBuddyCapacity() {
		return c.getPlayer().getBuddyCapacity();
	}

	public int partyMembersInMap() {
		int inMap = 0;
		for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
			if (char2.getParty() == getPlayer().getParty()) {
			inMap++;
			}
		}
		return inMap;
	}

	public List<MapleCharacter> getPartyMembers() {
		if (getPlayer().getParty() == null) {
			return null;
		}
		List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); // creates an empty array full of shit..
		for (ChannelServer channel : ChannelServer.getAllInstances()) {
			for (MapleCharacter chr : channel.getPartyMembers(getPlayer().getParty())) {
				if (chr != null) { // double check <3
					chars.add(chr);
				}
			}
		}
		return chars;
	}

	public void warpPartyWithExp(int mapId, int exp) {
		MapleMap target = getMap(mapId);
		for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
			MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
			if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
			curChar.changeMap(target, target.getPortal(0));
			curChar.gainExp(exp, true, false, true);
			}
		}
	}

	public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
		MapleMap target = getMap(mapId);
		for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
			MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
			if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
			curChar.changeMap(target, target.getPortal(0));
			curChar.gainExp(exp, true, false, true);
			curChar.gainMeso(meso, true);
			}
		}
	}

	public int itemQuantity(int itemid) {
		return getPlayer().getInventory(GameConstants.getInventoryType(itemid)).countById(itemid);
	}

	public int getSkillLevel(int skillid){
		return getPlayer().getSkillLevel(skillid);
	}

	public MapleSquad getSquad(String type) {
		return c.getChannelServer().getMapleSquad(type);
	}

	public int getSquadAvailability(String type) {
		final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad == null) {
			return -1;
		}
		return squad.getStatus();
	}

	public void registerSquad(String type, int minutes, String startText) {
		final MapleSquad squad = new MapleSquad(c.getChannel(), type, c.getPlayer(), minutes * 60 * 1000);
		final MapleMap map = c.getPlayer().getMap();

		map.broadcastMessage(MaplePacketCreator.getClock(minutes * 60));
		map.broadcastMessage(MaplePacketCreator.serverNotice(6, c.getPlayer().getName() + startText));
		c.getChannelServer().addMapleSquad(squad, type);
	}

	public boolean getSquadList(String type, byte type_) {
		final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad == null) {
			return false;
		}
		if (type_ == 0) { // Normal viewing
			sendNext(squad.getSquadMemberString(type_));
		} else if (type_ == 1) { // Squad Leader banning, Check out banned participant
			sendSimple(squad.getSquadMemberString(type_));
		} else if (type_ == 2) {
			if (squad.getBannedMemberSize() > 0) {
			sendSimple(squad.getSquadMemberString(type_));
			} else {
			sendNext(squad.getSquadMemberString(type_));
			}
		}
		return true;
	}

	public byte isSquadLeader(String type) {
		final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad == null) {
			return -1;
		} else {
			if (squad.getLeader().getId() == c.getPlayer().getId()) {
			return 1;
			} else {
			return 0;
			}
		}
	}

	public void banMember(String type, int pos) {
		final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.banMember(pos);
		}
	}

	public void acceptMember(String type, int pos) {
		final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.acceptMember(pos);
		}
	}

	public int addMember(String type, boolean join) {
		final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			return squad.addMember(c.getPlayer(), join);
		}
		return -1;
	}

	public byte isSquadMember(String type) {
		final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad == null) {
			return -1;
		} else {
			if (squad.getMembers().contains(c.getPlayer())) {
			return 1;
			} else if (squad.isBanned(c.getPlayer())) {
			return 2;
			} else {
			return 0;
			}
		}
	}

	public void resetReactors() {
		getPlayer().getMap().resetReactors();
	}

	public void genericGuildMessage(int code) {
		c.getSession().write(MaplePacketCreator.genericGuildMessage((byte) code));
	}

	public void disbandGuild() {
		final int gid = c.getPlayer().getGuildId();
		if (gid <= 0 || c.getPlayer().getGuildRank() != 1) {
			return;
		}
		try {
			c.getChannelServer().getWorldInterface().disbandGuild(gid);
		} catch (RemoteException e) {
			System.err.println("Error while disbanding guild." + e);
		}
	}

	public void doReborn() {
		if(getWorld() == 2){
			MAX_REBORNS += 3;
		}
		if (getPlayer().getReborns() < MAX_REBORNS) {
			getPlayer().setReborns(getPlayer().getReborns() + 1);
			//unequipEverything();
			List<Pair<MapleStat, Integer>> reborns = new ArrayList<Pair<MapleStat, Integer>>(4);
			getPlayer().setLevel(1);
			getPlayer().setExp(0);
			reborns.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, Integer.valueOf(1)));
			reborns.add(new Pair<MapleStat, Integer>(MapleStat.EXP, Integer.valueOf(0)));
			//getPlayer().getClient().getSession().write(MaplePacketCreator.updatePlayerStats(reborns));
			//getPlayer().getMap().broadcastMessage(getPlayer(), MaplePacketCreator.showJobChange(getPlayer().getId()), false);
		} else {
			getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, "You have reached the maximum amount of rebirths!"));
		}
	}

	public void increaseGuildCapacity() {
		if (c.getPlayer().getMeso() < 5000000) {
			c.getSession().write(MaplePacketCreator.serverNotice(1, "You do not have enough mesos."));
			return;
		}
		final int gid = c.getPlayer().getGuildId();
		if (gid <= 0) {
			return;
		}
		try {
			c.getChannelServer().getWorldInterface().increaseGuildCapacity(gid);
		} catch (RemoteException e) {
			System.err.println("Error while increasing capacity." + e);
			return;
		}
		c.getPlayer().gainMeso(-5000000, true, false, true);
	}

	public void createAlliance(String name) {
		c.getPlayer().getGuild().createAlliance(c, name);
	}

	public boolean hasAlliance() {
		return c.getPlayer().getGuild().getAlliance(c) != null;
	}

	public void sendAllianceInvite(String charname) {
		MapleCharacter z = c.getChannelServer().getPlayerStorage().getCharacterByName(charname);
		if (z != null) {
			if (z.getGuild().getLeader(z.getClient()) == z) {
	//                z.dropMessage(getPlayer().getName() + " invites your guild to join his alliance");
	//               z.dropMessage("If you want to accept that offer type @accept, else type @decline");
	//               z.setAllianceInvited(getPlayer().getGuild().getAlliance(getPlayer().getClient()));
			c.getPlayer().getAlliance().addGuild(c, c.getPlayer().getGuildId());
			} else {
			getPlayer().dropMessage(0, "That character is not the leader of the guild");
			}
		} else {
			getPlayer().dropMessage(0, "That character is offline");
		}
	}

	public void displayGuildRanks() {
		c.getSession().write(MaplePacketCreator.showGuildRanks(npc, MapleGuildRanking.getInstance().getRank()));
	}

	public boolean removePlayerFromInstance() {
		if (c.getPlayer().getEventInstance() != null) {
			c.getPlayer().getEventInstance().removePlayer(c.getPlayer());
			return true;
		}
		return false;
	}

	public boolean isPlayerInstance() {
		if (c.getPlayer().getEventInstance() != null) {
			return true;
		}
		return false;
	}

	public void changeStat(byte slot, int type, short amount) {
		Equip sel = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
		switch (type) {
			case 0:
			sel.setStr(amount);
			break;
			case 1:
			sel.setDex(amount);
			break;
			case 2:
			sel.setInt(amount);
			break;
			case 3:
			sel.setLuk(amount);
			break;
			case 4:
			sel.setHp(amount);
			break;
			case 5:
			sel.setMp(amount);
			break;
			case 6:
			sel.setWatk(amount);
			break;
			case 7:
			sel.setMatk(amount);
			break;
			case 8:
			sel.setWdef(amount);
			break;
			case 9:
			sel.setMdef(amount);
			break;
			case 10:
			sel.setAcc(amount);
			break;
			case 11:
			sel.setAvoid(amount);
			break;
			case 12:
			sel.setHands(amount);
			break;
			case 13:
			sel.setSpeed(amount);
			break;
			case 14:
			sel.setJump(amount);
			break;
			case 15:
			sel.setUpgradeSlots((byte) amount);
			break;
			case 16:
			sel.setViciousHammer((byte) amount);
			break;
			case 17:
			sel.setLevel((byte) amount);
			break;
			default:
			break;
		}
		c.getPlayer().equipChanged();
	}

	public void giveMerchantMesos() {
		long mesos = 0;
		try {
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT * FROM hiredmerchants WHERE merchantid = ?");
			ps.setInt(1, getPlayer().getId());
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
			rs.close();
			ps.close();
			} else {
			mesos = rs.getLong("mesos");
			}
			rs.close();
			ps.close();

			ps = (PreparedStatement) con.prepareStatement("UPDATE hiredmerchants SET mesos = 0 WHERE merchantid = ?");
			ps.setInt(1, getPlayer().getId());
			ps.executeUpdate();
			ps.close();

		} catch (SQLException ex) {
			System.err.println("Error gaining mesos in hired merchant" + ex);
		}
		c.getPlayer().gainMeso((int) mesos, true);
	}

	public long getMerchantMesos() {
		long mesos = 0;
		try {
			Connection con = (Connection) DatabaseConnection.getConnection();
			PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT * FROM hiredmerchants WHERE merchantid = ?");
			ps.setInt(1, getPlayer().getId());
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
			rs.close();
			ps.close();
			} else {
			mesos = rs.getLong("mesos");
			}
			rs.close();
			ps.close();
		} catch (SQLException ex) {
			System.err.println("Error gaining mesos in hired merchant" + ex);
		}
		return mesos;
	}

	public void openDuey() {
		c.getPlayer().setConversation(2);
		c.getSession().write(MaplePacketCreator.sendDuey((byte) 9, null));
	}

	public void openMerchantItemStore() {
		c.getPlayer().setConversation(3);
		c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x22));
	}

	public final int getDojoPoints() {
		return c.getPlayer().getDojo();
	}

	public final int getDojoRecord() {
	return c.getPlayer().getDojoRecord();
		}

	public void setDojoRecord(final boolean reset) {
		c.getPlayer().setDojoRecord(reset);
	}

	public boolean start_DojoAgent(final boolean dojo, final boolean party) {
		if (dojo) {
			return Event_DojoAgent.warpStartDojo(c.getPlayer(), party);
		}
		return Event_DojoAgent.warpStartAgent(c.getPlayer(), party);
	}

	public final short getKegs() {
		return AramiaFireWorks.getInstance().getKegsPercentage();
	}

	public void giveKegs(final int kegs) {
		AramiaFireWorks.getInstance().giveKegs(c.getPlayer(), kegs);
	}

	public final MapleInventory getInventory(byte type) {
		return c.getPlayer().getInventory(MapleInventoryType.getByType(type));
	}

	public final MapleCarnivalParty getCarnivalParty() {
		return c.getPlayer().getCarnivalParty();
	}

	public final MapleCarnivalChallenge getNextCarnivalRequest() {
		return c.getPlayer().getNextCarnivalRequest();
	}

	public void resetStats(final int str, final int dex, final int int_, final int luk) {
		List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>(2);
		final MapleCharacter chr = c.getPlayer();
		int total = chr.getStat().getStr() + chr.getStat().getDex() + chr.getStat().getLuk() + chr.getStat().getInt() + chr.getRemainingAp();
		total -= str;
		chr.getStat().setStr(str);
		total -= dex;
		chr.getStat().setDex(dex);
		total -= int_;
		chr.getStat().setInt(int_);
		total -= luk;
		chr.getStat().setLuk(luk);
		chr.setRemainingAp(total);
		stats.add(new Pair<MapleStat, Integer>(MapleStat.STR, str));
		stats.add(new Pair<MapleStat, Integer>(MapleStat.DEX, dex));
		stats.add(new Pair<MapleStat, Integer>(MapleStat.INT, int_));
		stats.add(new Pair<MapleStat, Integer>(MapleStat.LUK, luk));
		stats.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, total));
		c.getSession().write(MaplePacketCreator.updatePlayerStats(stats, false, c.getPlayer().getJob()));
	}


	public final boolean dropItem(int slot, int invType, int quantity) {
		MapleInventoryType inv = MapleInventoryType.getByType((byte)invType);
		if (inv == null) {
			return false;
		}
		MapleInventoryManipulator.drop(c, inv, (short)slot, (short)quantity);
		return true;
	}

	public void maxStats() {
		List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(2);
		c.getPlayer().setRemainingAp(0);
		statup.add(new Pair(MapleStat.AVAILABLEAP, Integer.valueOf(0)));
		c.getPlayer().setRemainingSp(0);
		statup.add(new Pair(MapleStat.AVAILABLESP, Integer.valueOf(0)));
		c.getPlayer().getStat().setStr(32767);
		statup.add(new Pair(MapleStat.STR, Integer.valueOf(32767)));
		c.getPlayer().getStat().setDex(32767);
		statup.add(new Pair(MapleStat.DEX, Integer.valueOf(32767)));
		c.getPlayer().getStat().setInt(32767);
		statup.add(new Pair(MapleStat.INT, Integer.valueOf(32767)));
		c.getPlayer().getStat().setLuk(32767);
		statup.add(new Pair(MapleStat.LUK, Integer.valueOf(32767)));
		c.getPlayer().getStat().setHp(30000);
		statup.add(new Pair(MapleStat.HP, Integer.valueOf(30000)));
		c.getPlayer().getStat().setMaxHp(30000);
		statup.add(new Pair(MapleStat.MAXHP, Integer.valueOf(30000)));
		c.getPlayer().getStat().setMp(30000);
		statup.add(new Pair(MapleStat.MP, Integer.valueOf(30000)));
		c.getPlayer().getStat().setMaxMp(30000);
		statup.add(new Pair(MapleStat.MAXMP, Integer.valueOf(30000)));
		c.getSession().write(MaplePacketCreator.updatePlayerStats(statup, c.getPlayer().getJob()));
	}

	public void gainFame(int fame) {
		c.getPlayer().setFame(fame);
		c.getPlayer().updateSingleStat(MapleStat.FAME, Integer.valueOf(getPlayer().getFame()));
		c.getSession().write(MaplePacketCreator.serverNotice(6, "You have gained (+" + fame +") fame."));
	}
}