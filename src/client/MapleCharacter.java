package client;

import java.awt.Point;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;

import client.anticheat.CheatTracker;
import database.*;
import handling.MaplePacket;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.world.*;
import handling.world.guild.MapleAlliance;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.remote.WorldChannelInterface;
import java.util.Map.Entry;
import scripting.EventInstanceManager;
import scripting.NPCScriptManager;
import server.*;
import server.life.MapleMonster;
import server.maps.*;
import server.quest.MapleQuest;
import server.shops.IMaplePlayerShop;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packet.*;
import server.MapleCarnivalChallenge;
import server.MapleInventoryManipulator;
import server.life.MobSkill;
import server.maps.MapleDragon;
import server.movement.LifeMovementFragment;

public class MapleCharacter extends AbstractAnimatedMapleMapObject implements InventoryContainer, Serializable {

	private String name, chalktext, BlessOfFairy_Origin;
	private transient int linkMid = 0;
	private long lastCombo, lastfametime, keydown_skill;
	private byte dojoRecord, gmLevel, gender; // Make this a quest record, TODO : Transfer it somehow with the current data
	private short level, mulung_energy, combo, availableCP, totalCP;
	private int accountid, id, meso, exp, job, rank, rankMove, jobRank, jobRankMove, mpApUsed, hpApUsed, hair, face, skinColor, remainingAp,
				fame, score, mapid, initialSpawnPoint, bookCover, dojo, guildid, guildrank, allianceRank, fallcounter, maplepoints, acash, messengerposition,
				chair, itemEffect, subcategory;
	private int[] remainingSp = new int[10];
	private transient MapleDragon dragon;
	public int reborns;
	public int world;
	private boolean canDoor, Berserk, smega, hidden;
	private int[] wishlist, rocks, savedLocations;
	private transient AtomicInteger inst;
	private transient List<LifeMovementFragment> lastres;
	private List<Integer> lastmonthfameids;
	private List<MapleDoor> doors;
	private List<MaplePet> pets;
	public int vpoints;
	private boolean ondmg = true, callgm = true;
	private transient Set<MapleMonster> controlled;
	private transient Set<MapleMapObject> visibleMapObjects;
	private Map<MapleQuest, MapleQuestStatus> quests;
	private Map<Integer, String> questinfo;
	private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
	private transient Map<MapleBuffStat, MapleBuffStatValueHolder> effects = new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>(50);
	private transient Map<Integer, MapleSummon> summons;
	private transient Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>(50);
	private transient Map<MapleDisease, DiseaseValueHolder> diseases;
	private MapleAlliance alliance;
	private transient Deque<MapleCarnivalChallenge> pendingCarnivalRequests;
	private transient MapleCarnivalParty carnivalParty;
	private BuddyList buddylist;
	private MonsterBook monsterbook;
	private transient CheatTracker anticheat;
	private MapleClient client;
	private PlayerStats stats;
	private PlayerRandomStream CRand;
	private transient MapleMap map;
	private transient MapleShop shop;
	private MapleStorage storage;
	private transient MapleTrade trade;
	private MapleMount mount;
	private MapleMessenger messenger;
	private IMaplePlayerShop playerShop;
	private MapleParty party;
	private MapleGuildCharacter mgc;
	private transient EventInstanceManager eventInstance;
	private MapleInventory[] inventory;
	private SkillMacro[] skillMacros = new SkillMacro[5];
	private MapleKeyLayout keylayout;
	private transient ScheduledFuture<?> fullnessSchedule, fullnessSchedule_1, fullnessSchedule_2, hpDecreaseTask;
	private transient ScheduledFuture<?> beholderHealingSchedule, beholderBuffSchedule, BerserkSchedule;
	private transient ScheduledFuture<?> dragonBloodSchedule;
	private transient ScheduledFuture<?> mapTimeLimitTask, fishing;
	public static int damageCap = 100000000; //Global damage cap
	public static int magicCap = 999999999;//
	public static int unlimitedSlotItem = 1082174; //Lunar Glove

	private MapleCharacter(final boolean ChannelServer) {
		setStance(0);
		setPosition(new Point(0, 0));
		inventory = new MapleInventory[MapleInventoryType.values().length];
		for (MapleInventoryType type : MapleInventoryType.values()) {
			inventory[type.ordinal()] = new MapleInventory(type);
		}
		quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>(); // Stupid erev quest.
		stats = new PlayerStats(this);
		for (int i = 0; i < remainingSp.length; i++) {
			remainingSp[i] = 0;
		}
		if (ChannelServer) {
			lastCombo = 0;
			mulung_energy = 0;
			combo = 0;
			keydown_skill = 0;
			messengerposition = 4;
			canDoor = true;
			Berserk = false;
			smega = true;
			wishlist = new int[10];
			rocks = new int[10];
			inst = new AtomicInteger();
			inst.set(0); // 1 = NPC/ Quest, 2 = Duey, 3 = Hired Merch store, 4 = Storage
			keylayout = new MapleKeyLayout();
			doors = new ArrayList<MapleDoor>();
			diseases = new LinkedHashMap<MapleDisease, DiseaseValueHolder>(5);
			controlled = new LinkedHashSet<MapleMonster>();
			summons = new LinkedHashMap<Integer, MapleSummon>();
			visibleMapObjects = new LinkedHashSet<MapleMapObject>();
			pendingCarnivalRequests = new LinkedList<MapleCarnivalChallenge>();
			savedLocations = new int[SavedLocationType.values().length];
			for (int i = 0; i < SavedLocationType.values().length; i++) {
				savedLocations[i] = -1;
			}
			questinfo = new LinkedHashMap<Integer, String>();
			anticheat = new CheatTracker(this);
			pets = new ArrayList<MaplePet>();
		}
	}

	public static MapleCharacter getDefault(final MapleClient client, final int type) {
		MapleCharacter ret = new MapleCharacter(false);
		ret.client = client;
		ret.map = null;
		ret.exp = 0;
		ret.gmLevel = 0;
		ret.job = type == 0 ? 3000 : (type == 1 ? 0 : (type == 2 ? 1000 : (type == 3 ? 2000 : 2001)));
		ret.meso = 0;
		ret.level = 1;
		ret.remainingAp = 0;
		ret.fame = 0;
                ret.score = 0;
		ret.accountid = client.getAccID();
		ret.buddylist = new BuddyList(20);
		ret.stats.str = 12;
		ret.stats.dex = 4;
		ret.stats.int_ = 4;
		ret.stats.luk = 4;
		ret.stats.hp = 50;
		ret.stats.maxhp = 50;
		ret.stats.mp = 5;
		ret.stats.maxmp = 5;
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
			ps.setInt(1, ret.accountid);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ret.client.setAccountName(rs.getString("name"));
				ret.acash = rs.getInt("ACash");
				ret.maplepoints = rs.getInt("mPoints");
				ret.vpoints = rs.getInt("vpoints");
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error getting character default" + e);
		}
		return ret;
	}

	public final static MapleCharacter ReconstructChr(final CharacterTransfer ct, final MapleClient client, final boolean isChannel) {
		final MapleCharacter ret = new MapleCharacter(true); // Always true, it's change channel
		ret.client = client;
		if (!isChannel) {
			ret.client.setChannel(ct.channel);
		}
		ret.id = ct.characterid;
		ret.name = ct.name;
		ret.level = ct.level;
		ret.fame = ct.fame;
                ret.score = ct.score;
		ret.CRand = new PlayerRandomStream();
		ret.stats.setStr(ct.str);
		ret.stats.setDex(ct.dex);
		ret.stats.setInt(ct.int_);
		ret.stats.setLuk(ct.luk);
		ret.stats.setMaxHp(ct.maxhp);
		ret.stats.setMaxMp(ct.maxmp);
		ret.stats.setHp(ct.hp);
		ret.stats.setMp(ct.mp);
		ret.exp = ct.exp;
		ret.hpApUsed = ct.hpApUsed;
		ret.mpApUsed = ct.mpApUsed;
		ret.remainingSp = (int[]) ct.remainingSp;
		ret.remainingAp = ct.remainingAp;
		ret.meso = ct.meso;
		ret.gmLevel = ct.gmLevel;
		ret.skinColor = ct.skinColor;
		ret.gender = ct.gender;
		ret.job = ct.job;
		ret.hair = ct.hair;
		ret.face = ct.face;
		ret.accountid = ct.accountid;
		ret.mapid = ct.mapid;
		ret.initialSpawnPoint = ct.initialSpawnPoint;
		ret.world = ct.world;
		ret.rank = ct.rank;
		ret.rankMove = ct.rankMove;
		ret.jobRank = ct.jobRank;
		ret.jobRankMove = ct.jobRankMove;
		ret.bookCover = ct.mBookCover;
		ret.dojo = ct.dojo;
		ret.dojoRecord = ct.dojoRecord;
		ret.reborns = ct.reborns;
		ret.guildid = ct.guildid;
		ret.guildrank = ct.guildrank;
		ret.allianceRank = ct.alliancerank;
		ret.subcategory = ct.subcategory;
		ret.ondmg = ct.ondmg;
		ret.callgm = ct.callgm;
		if (ret.guildid > 0) {
			ret.mgc = new MapleGuildCharacter(ret);
		}
		ret.buddylist = new BuddyList(ct.buddysize);
		if (isChannel) {
			final MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory(ret.world);
			ret.map = mapFactory.getMap(ret.mapid);
			if (ret.map == null) { //char is on a map that doesn't exist warp it to henesys
				ret.map = mapFactory.getMap(100000000);
			} else {
				if (ret.map.getForcedReturnId() != 999999999) {
					ret.map = ret.map.getForcedReturnMap();
				}
			}
			MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
			if (portal == null) {
				portal = ret.map.getPortal(0); // char is on a spawnpoint that doesn't exist - select the first spawnpoint instead
				ret.initialSpawnPoint = 0;
			}
			ret.setPosition(portal.getPosition());
			int partyid = ct.partyid;
			if (partyid >= 0) {
				try {
					MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
					if (party != null && party.getMemberById(ret.id) != null) {
						ret.party = party;
					}
				} catch (RemoteException e) {
					client.getChannelServer().reconnectWorld();
				}
			}
			final int messengerid = ct.messengerid;
			final int position = ct.messengerposition;
			if (messengerid > 0 && position < 4 && position > -1) {
				try {
					WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
					MapleMessenger messenger = wci.getMessenger(messengerid);
					if (messenger != null) {
						ret.messenger = messenger;
						ret.messengerposition = position;
					}
				} catch (RemoteException e) {
					client.getChannelServer().reconnectWorld();
				}
			}
		} else {
			int partyid = ct.partyid;
			if (partyid >= 0) {
				try {
					MapleParty party = CashShopServer.getInstance().getCSInterface().getParty(partyid);
					if (party != null && party.getMemberById(ret.id) != null) {
						ret.party = party;
					}
				} catch (RemoteException e) {
					client.getChannelServer().reconnectWorld();
				}
			}
			ret.messenger = null;
			ret.messengerposition = ct.messengerposition;
		}
		MapleQuestStatus queststatus;
		MapleQuestStatus queststatus_from;
		MapleQuest quest;
		for (final Map.Entry<Integer, Object> qs : ct.Quest.entrySet()) {
			quest = MapleQuest.getInstance(qs.getKey());
			queststatus_from = (MapleQuestStatus) qs.getValue();
			queststatus = new MapleQuestStatus(quest, queststatus_from.getStatus());
			queststatus.setForfeited(queststatus_from.getForfeited());
			queststatus.setCustomData(queststatus_from.getCustomData());
			queststatus.setCompletionTime(queststatus_from.getCompletionTime());
			if (queststatus_from.getMobKills() != null) {
				for (final Map.Entry<Integer, Integer> mobkills : queststatus_from.getMobKills().entrySet()) {
					queststatus.setMobKills(mobkills.getKey(), mobkills.getValue());
				}
			}
			ret.quests.put(quest, queststatus);
		}
		for (final Map.Entry<Integer, Object> qs : ct.Skills.entrySet()) {
			ret.skills.put(SkillFactory.getSkill(qs.getKey()), (SkillEntry) qs.getValue());
		}
		ret.monsterbook = (MonsterBook) ct.monsterbook;
		ret.inventory = (MapleInventory[]) ct.inventorys;
		ret.BlessOfFairy_Origin = ct.BlessOfFairy;
		ret.skillMacros = (SkillMacro[]) ct.skillmacro;
		ret.keylayout = (MapleKeyLayout) ct.keymap;
		ret.questinfo = (Map<Integer, String>) ct.InfoQuest;
		ret.savedLocations = (int[]) ct.savedlocation;
		ret.wishlist = (int[]) ct.wishlist;
		ret.rocks = (int[]) ct.rocks;
		ret.buddylist.loadFromTransfer(ct.buddies);
		ret.keydown_skill = 0; // Keydown skill can't be brought over
		ret.lastfametime = ct.lastfametime;
		ret.lastmonthfameids = (List<Integer>) ct.famedcharacters;
		ret.storage = (MapleStorage) ct.storage;
		client.setAccountName(ct.accountname);
		ret.acash = ct.ACash;
		ret.vpoints = ct.vpoints;
		ret.maplepoints = ct.MaplePoints;
		ret.mount = new MapleMount(ret, ct.mount_itemid, 1004, ct.mount_Fatigue, ct.mount_level, ct.mount_exp);
		ret.stats.recalcLocalStats();
		ret.silentEnforceMaxHpMp();
		return ret;
	}

	public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) {
		final MapleCharacter ret = new MapleCharacter(channelserver);
		ret.client = client;
		ret.id = charid;
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		PreparedStatement pse = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
			ps.setInt(1, charid);
			rs = ps.executeQuery();
			if (!rs.next()) {
				throw new RuntimeException("Loading the Char Failed (char not found)");
			}
			ret.name = rs.getString("name");
			ret.level = rs.getShort("level");
			ret.fame = rs.getInt("fame");
                        ret.score = rs.getInt("score");
			ret.stats = new PlayerStats(ret);
			if (channelserver) {
				ret.stats.setStr(rs.getInt("str"));
				ret.stats.setDex(rs.getInt("dex"));
				ret.stats.setInt(rs.getInt("int"));
				ret.stats.setLuk(rs.getInt("luk"));
				ret.stats.setMaxHp(rs.getInt("maxhp"));
				ret.stats.setMaxMp(rs.getInt("maxmp"));
				ret.stats.setHp(rs.getInt("hp"));
				ret.stats.setMp(rs.getInt("mp"));
			} else {
				ret.stats.str = rs.getInt("str");
				ret.stats.dex = rs.getInt("dex");
				ret.stats.int_ = rs.getInt("int");
				ret.stats.luk = rs.getInt("luk");
				ret.stats.maxhp = rs.getInt("maxhp");
				ret.stats.maxmp = rs.getInt("maxmp");
				ret.stats.hp = rs.getInt("hp");
				ret.stats.mp = rs.getInt("mp");
			}
			ret.exp = rs.getInt("exp");
			ret.hpApUsed = rs.getInt("hpApUsed");
			ret.mpApUsed = rs.getInt("mpApUsed");
			final String[] sp = rs.getString("sp").split(",");
			for (int i = 0; i < ret.remainingSp.length; i++) {
				ret.remainingSp[i] = Integer.parseInt(sp[i]);
			}
			ret.remainingAp = rs.getInt("ap");
			ret.subcategory = rs.getInt("subcategory");
			ret.meso = rs.getInt("meso");
			ret.gmLevel = rs.getByte("gm");
			ret.skinColor = rs.getInt("skincolor");
			ret.gender = rs.getByte("gender");
			ret.job = rs.getInt("job");
			ret.hair = rs.getInt("hair");
			ret.face = rs.getInt("face");
			ret.accountid = rs.getInt("accountid");
			ret.mapid = rs.getInt("map");
			ret.initialSpawnPoint = rs.getInt("spawnpoint");
			ret.world = rs.getInt("world");
			ret.rank = rs.getInt("rank");
			ret.rankMove = rs.getInt("rankMove");
			ret.jobRank = rs.getInt("jobRank");
			ret.jobRankMove = rs.getInt("jobRankMove");
			ret.guildid = rs.getInt("guildid");
			ret.guildrank = rs.getInt("guildrank");
			ret.allianceRank = rs.getInt("allianceRank");
			ret.reborns = rs.getInt("reborns");
			if (ret.guildid > 0) {
				ret.mgc = new MapleGuildCharacter(ret);
			}
			ret.buddylist = new BuddyList(rs.getInt("buddyCapacity"));
			if (channelserver) {
				MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory(ret.world);
				ret.map = mapFactory.getMap(ret.mapid);
				if (ret.map == null) {
					ret.map = mapFactory.getMap(100000000);
				}
				MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
				if (portal == null) {
					portal = ret.map.getPortal(0);
					ret.initialSpawnPoint = 0;
				}
				ret.setPosition(portal.getPosition());
				int partyid = rs.getInt("party");
				if (partyid >= 0) {
					try {
						MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
						if (party != null && party.getMemberById(ret.id) != null) {
							ret.party = party;
						}
					} catch (RemoteException e) {
						client.getChannelServer().reconnectWorld();
					}
				}
				final int messengerid = rs.getInt("messengerid");
				final int position = rs.getInt("messengerposition");
				if (messengerid > 0 && position < 4 && position > -1) {
					try {
						WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
						MapleMessenger messenger = wci.getMessenger(messengerid);
						if (messenger != null) {
							ret.messenger = messenger;
							ret.messengerposition = position;
						}
					} catch (RemoteException e) {
						client.getChannelServer().reconnectWorld();
					}
				}
				ret.bookCover = rs.getInt("monsterbookcover");
				ret.dojo = rs.getInt("dojo_pts");
				ret.dojoRecord = rs.getByte("dojoRecord");
			}
			rs.close();
			ps.close();
			ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
			ps.setInt(1, charid);
			rs = ps.executeQuery();
			pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
			while (rs.next()) {
				final int id = rs.getInt("quest");
				final MapleQuest q = MapleQuest.getInstance(id);
				final MapleQuestStatus status = new MapleQuestStatus(q, rs.getByte("status"));
				final long cTime = rs.getLong("time");
				if (cTime > -1) {
					status.setCompletionTime(cTime * 1000);
				}
				status.setForfeited(rs.getInt("forfeited"));
				status.setCustomData(rs.getString("customData"));
				ret.quests.put(q, status);
				pse.setInt(1, rs.getInt("queststatusid"));
				final ResultSet rsMobs = pse.executeQuery();
				while (rsMobs.next()) {
					status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
				}
				rsMobs.close();
			}
			rs.close();
			ps.close();
			pse.close();
			if (channelserver) {
				ret.CRand = new PlayerRandomStream();
				ret.monsterbook = new MonsterBook();
				ret.monsterbook.loadCards(charid);
				ps = con.prepareStatement("SELECT * FROM inventoryslot where characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				if (!rs.next()) {
					throw new RuntimeException("No Inventory slot column found in SQL. [inventoryslot]");
				} else {
					ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equip"));
					ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("use"));
					ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setup"));
					ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etc"));
					ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getByte("cash"));
				}
				ps.close();
				rs.close();
				ps = con.prepareStatement("SELECT * FROM inventoryitems LEFT JOIN inventoryequipment USING (inventoryitemid) WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				long expiration;
				MapleInventoryType type;
				Item item;
				MaplePet pet;
				while (rs.next()) {
					type = MapleInventoryType.getByType(rs.getByte("inventorytype"));
					expiration = rs.getLong("expiredate");
					if (type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.EQUIPPED)) {
						final Equip equip = new Equip(rs.getInt("itemid"), rs.getByte("position"), rs.getInt("ringid"), rs.getByte("flag"));
						equip.setOwner(rs.getString("owner"));
						equip.setQuantity(rs.getShort("quantity"));
						equip.setAcc(rs.getShort("acc"));
						equip.setAvoid(rs.getShort("avoid"));
						equip.setDex(rs.getShort("dex"));
						equip.setHands(rs.getShort("hands"));
						equip.setHp(rs.getShort("hp"));
						equip.setInt(rs.getShort("int"));
						equip.setJump(rs.getShort("jump"));
						equip.setLuk(rs.getShort("luk"));
						equip.setMatk(rs.getShort("matk"));
						equip.setMdef(rs.getShort("mdef"));
						equip.setMp(rs.getShort("mp"));
						equip.setSpeed(rs.getShort("speed"));
						equip.setStr(rs.getShort("str"));
						equip.setWatk(rs.getShort("watk"));
						equip.setWdef(rs.getShort("wdef"));
						equip.setItemLevel(rs.getByte("itemLevel"));
						equip.setItemEXP(rs.getShort("itemEXP"));
						equip.setViciousHammer(rs.getByte("ViciousHammer"));
						equip.setUpgradeSlots(rs.getByte("upgradeslots"));
						equip.setLevel(rs.getByte("level"));
						equip.setPotential(rs.getByte("potential"));
						equip.setPStars(rs.getByte("pstars"));
						equip.setPotential_1(rs.getByte("potential_1"));
						equip.setPotential_2(rs.getByte("potential_2"));
						equip.setPotential_3(rs.getByte("potential_3"));
						equip.setExpiration(expiration);
						equip.setGMLog(rs.getString("GM_Log"));
						ret.getInventory(type).addFromDB(equip);
					} else {
						item = new Item(rs.getInt("itemid"), rs.getByte("position"), rs.getShort("quantity"), rs.getByte("flag"));
						item.setOwner(rs.getString("owner"));
						item.setExpiration(expiration);
						item.setGMLog(rs.getString("GM_Log"));
						ret.getInventory(type).addFromDB(item);
						if (rs.getInt("petid") > -1) {
							pet = MaplePet.loadFromDb(item.getItemId(), rs.getInt("petid"), item.getPosition());
							ret.pets.add(pet);
							item.setPet(pet);
						}
					}
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
				ps.setInt(1, ret.accountid);
				rs = ps.executeQuery();
				if (rs.next()) {
					ret.getClient().setAccountName(rs.getString("name"));
					ret.acash = rs.getInt("ACash");
					ret.vpoints = rs.getInt("vpoints");
					ret.maplepoints = rs.getInt("mPoints");
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT * FROM questinfo WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				while (rs.next()) {
					ret.questinfo.put(rs.getInt("quest"), rs.getString("customData"));
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT skillid, skilllevel, masterlevel FROM skills WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				while (rs.next()) {
					if (GameConstants.isApplicableSkill(rs.getInt("skillid"))) {
						ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getByte("skilllevel"), rs.getByte("masterlevel")));
					}
				}
				rs.close();
				ps.close();
				// Bless of Fairy handling
				ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? ORDER BY level DESC");
				ps.setInt(1, ret.accountid);
				rs = ps.executeQuery();
				while (rs.next()) {
					if (rs.getInt("id") != charid) { // Not this character
						byte maxlevel = (byte) (rs.getInt("level") / 10);
						if (maxlevel > 20) {
							maxlevel = 20;
						}
						ret.BlessOfFairy_Origin = rs.getString("name");
						ret.skills.put(SkillFactory.getSkill(GameConstants.getBOF_ForJob(ret.job)), new SkillEntry(maxlevel, (byte) 0));
						break;
					}
				}
				ps.close();
				rs.close();
				// END
				ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				int position;
				while (rs.next()) {
					position = rs.getInt("position");
					SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
					ret.skillMacros[position] = macro;
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				final Map<Integer, MapleKeyBinding> keyb = ret.keylayout.Layout();
				while (rs.next()) {
					keyb.put(Integer.valueOf(rs.getInt("key")), new MapleKeyBinding(rs.getInt("type"), rs.getInt("action")));
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				while (rs.next()) {
					ret.savedLocations[rs.getInt("locationtype")] = rs.getInt("map");
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				ret.lastfametime = 0;
				ret.lastmonthfameids = new ArrayList<Integer>(31);
				while (rs.next()) {
					ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
					ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
				}
				rs.close();
				ps.close();
				ret.buddylist.loadFromDb(charid);
				ret.storage = MapleStorage.loadStorage(ret.accountid);
				ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				int i = 0;
				while (rs.next()) {
					ret.wishlist[i] = rs.getInt("sn");
					i++;
				}
				while (i < 10) {
					ret.wishlist[i] = 0;
					i++;
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT mapid FROM trocklocations WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				int r = 0;
				while (rs.next()) {
					ret.rocks[r] = rs.getInt("mapid");
					r++;
				}
				while (r < 10) {
					ret.rocks[r] = 999999999;
					r++;
				}
				rs.close();
				ps.close();
				ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				if (!rs.next()) {
					throw new RuntimeException("No mount data found on SQL column");
				}
				final IItem mount = ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -22);
				ret.mount = new MapleMount(ret, mount != null ? mount.getItemId() : 0, 1004, rs.getInt("Fatigue"), rs.getInt("Level"), rs.getInt("Exp"));
				ps.close();
				rs.close();
				ret.stats.recalcLocalStats();
				ret.silentEnforceMaxHpMp();
			} else { // Not channel server
				ps = con.prepareStatement("SELECT * FROM inventoryitems LEFT JOIN inventoryequipment USING (inventoryitemid) WHERE characterid = ? AND inventorytype = -1");
				ps.setInt(1, charid);
				rs = ps.executeQuery();
				MapleInventoryType type;
				while (rs.next()) {
					type = MapleInventoryType.getByType(rs.getByte("inventorytype"));
					if (type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.EQUIPPED)) {
						final Equip equip = new Equip(rs.getInt("itemid"), rs.getByte("position"), rs.getInt("ringid"), rs.getByte("flag"));
						ret.getInventory(type).addFromDB(equip);
					}
				}
				rs.close();
				ps.close();
			}
		} catch (SQLException ess) {
			ess.printStackTrace();
			System.out.println("Failed to load character..");
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException ignore) {
				System.out.println(ignore);
			}
		}
		return ret;
	}

	public static void saveNewCharToDB(final MapleCharacter chr, final int type, final boolean db) {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		PreparedStatement pse = null;
		ResultSet rs = null;
		try {
			con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			con.setAutoCommit(false);
			ps = con.prepareStatement("INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, hp, mp, maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpApUsed, mpApUsed, spawnpoint, party, buddyCapacity, messengerid, messengerposition, monsterbookcover, dojo_pts, dojoRecord, accountid, name, world, reborns, subcategory) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
			ps.setInt(1, 1); // Level
			ps.setInt(2, 0); // Fame
			final PlayerStats stat = chr.stats;
			ps.setInt(3, stat.getStr());
			ps.setInt(4, stat.getDex());
			ps.setInt(5, stat.getInt());
			ps.setInt(6, stat.getLuk());
			ps.setInt(7, 0);
			ps.setInt(8, stat.getHp());
			ps.setInt(9, stat.getMp());
			ps.setInt(10, stat.getMaxHp());
			ps.setInt(11, stat.getMaxMp());
			ps.setString(12, "0,0,0,0,0,0,0,0,0,0"); // Remaining SP
			ps.setInt(13, 0); // Remaining AP
			ps.setInt(14, 0); // GM Level
			ps.setInt(15, chr.skinColor);
			ps.setInt(16, chr.gender);
			ps.setInt(17, chr.job);
			ps.setInt(18, chr.hair);
			ps.setInt(19, chr.face);
			ps.setInt(20, type == 0 ? 931000000 : (type == 1 ? 10000 : (type == 2 ? 130030000 : (type == 3 ? 914000000 : 900010000))));
			ps.setInt(21, chr.meso);
			ps.setInt(22, 0); // HP ap used
			ps.setInt(23, 0); // MP ap used
			ps.setInt(24, 0); // Spawnpoint
			ps.setInt(25, -1); // Party
			ps.setInt(26, chr.buddylist.getCapacity());
			ps.setInt(27, 0); // MessengerId
			ps.setInt(28, 4); // Messenger Position
			ps.setInt(29, 0); // Monster book cover
			ps.setInt(30, 0); // Dojo
			ps.setInt(31, 0); // Dojo record
			ps.setInt(32, chr.getAccountID());
			ps.setString(33, chr.name);
			ps.setInt(34, chr.world);
			ps.setInt(35, chr.reborns);
			ps.setInt(36, db ? 1 : 0); //for now
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				chr.id = rs.getInt(1);
			} else {
				throw new DatabaseException(":: Failed to create new character ::");
			}
			ps.close();
			rs.close();
			ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
			pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
			ps.setInt(1, chr.id);
			for (final MapleQuestStatus q : chr.quests.values()) {
				ps.setInt(2, q.getQuest().getId());
				ps.setInt(3, q.getStatus());
				ps.setInt(4, (int) (q.getCompletionTime() / 1000));
				ps.setInt(5, q.getForfeited());
				ps.setString(6, q.getCustomData());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				rs.next();
				if (q.hasMobKills()) {
					for (int mob : q.getMobKills().keySet()) {
						pse.setInt(1, rs.getInt(1));
						pse.setInt(2, mob);
						pse.setInt(3, q.getMobKills(mob));
						pse.executeUpdate();
					}
				}
				rs.close();
			}
			ps.close();
			pse.close();
			ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
			ps.setInt(1, chr.id);
			ps.setInt(2, 24); // Eq
			ps.setInt(3, 24); // Use
			ps.setInt(4, 24); // Setup
			ps.setInt(5, 24); // ETC
			ps.setInt(6, 96); // Cash
			ps.execute();
			ps.close();
			ps = con.prepareStatement("INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES (?, ?, ?, ?)");
			ps.setInt(1, chr.id);
			ps.setInt(2, 1);
			ps.setInt(3, 0);
			ps.setInt(4, 0);
			ps.execute();
			ps.close();
			ps = con.prepareStatement("INSERT INTO inventoryitems (characterid, itemid, inventorytype, position, quantity, owner, GM_Log, petid, expiredate, flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
			pse = con.prepareStatement("INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			IEquip equip;
			for (final MapleInventory iv : chr.inventory) {
				ps.setInt(3, iv.getType().getType());
				for (final IItem item : iv.list()) {
					ps.setInt(1, chr.id);
					ps.setInt(2, item.getItemId());
					ps.setInt(4, item.getPosition());
					ps.setInt(5, item.getQuantity());
					ps.setString(6, item.getOwner());
					ps.setString(7, item.getGMLog());
					ps.setInt(8, -1); // Pet cant be loaded on logins + new char doesn't have.
					ps.setLong(9, item.getExpiration());
					ps.setByte(10, item.getFlag());
					ps.executeUpdate();
					rs = ps.getGeneratedKeys();
					int itemid;
					if (rs.next()) {
						itemid = rs.getInt(1);
					} else {
						throw new DatabaseException(":: Failed inserting character ::");
					}
					rs.close();
					if (iv.getType().equals(MapleInventoryType.EQUIP) || iv.getType().equals(MapleInventoryType.EQUIPPED)) {
						pse.setInt(1, itemid);
						equip = (IEquip) item;
						pse.setInt(2, equip.getUpgradeSlots());
						pse.setInt(3, equip.getLevel());
						pse.setInt(4, equip.getStr());
						pse.setInt(5, equip.getDex());
						pse.setInt(6, equip.getInt());
						pse.setInt(7, equip.getLuk());
						pse.setInt(8, equip.getHp());
						pse.setInt(9, equip.getMp());
						pse.setInt(10, equip.getWatk());
						pse.setInt(11, equip.getMatk());
						pse.setInt(12, equip.getWdef());
						pse.setInt(13, equip.getMdef());
						pse.setInt(14, equip.getAcc());
						pse.setInt(15, equip.getAvoid());
						pse.setInt(16, equip.getHands());
						pse.setInt(17, equip.getSpeed());
						pse.setInt(18, equip.getJump());
						pse.setInt(19, equip.getRingId());
						pse.setInt(20, equip.getViciousHammer());
						pse.setInt(21, 0);
						pse.setInt(22, 0);
						pse.setInt(23, equip.getPotential());
						pse.setInt(24, equip.getPStars());
						pse.setInt(25, equip.getPotential_1());
						pse.setInt(26, equip.getPotential_2());
						pse.setInt(27, equip.getPotential_3());
						pse.executeUpdate();
					}
				}
			}
			ps.close();
			pse.close();
			ps = con.prepareStatement("INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES (?, ?, ?, ?)");
			ps.setInt(1, chr.id);
			ps.setInt(2, 1);
			ps.setInt(3, 0);
			ps.setInt(4, 0);
			ps.execute();
			ps.close();
			/*final int[] array1 = {2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 23, 25, 26, 27, 31, 34, 37, 38, 41, 44, 45, 46, 50, 57, 59, 60, 61, 62, 63, 64, 65, 8, 9, 24, 30};
			final int[] array2 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 4, 5, 6, 6, 6, 6, 6, 6, 6, 4, 4, 4, 4};
			final int[] array3 = {10, 12, 13, 18, 6, 11, 8, 5, 0, 4, 1, 19, 14, 15, 3, 17, 9, 20, 22, 50, 51, 52, 7, 53, 100, 101, 102, 103, 104, 105, 106, 16, 23, 24, 2};
			ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
			ps.setInt(1, chr.id);
			for (int i = 0; i < array1.length; i++) {
				ps.setInt(2, array1[i]);
				ps.setInt(3, array2[i]);
				ps.setInt(4, array3[i]);
				ps.execute();
			}
			ps.close();*/
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(":: Failed saving character data ::");
			try {
				con.rollback();
			} catch (SQLException ex) {
				e.printStackTrace();
				System.err.println(":: Failed to save character data, rooling back ::");
			}
		} finally {
			try {
				if (pse != null) {
					pse.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
				con.setAutoCommit(true);
				con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println(":: Failed to save character data, going back to autocommit mode ::");
			}
		}
	}

	public void saveToDB(boolean dc, boolean fromcs) {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		PreparedStatement pse = null;
		ResultSet rs = null;
		try {
			con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			con.setAutoCommit(false);
			ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, score = ?,str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpApUsed = ?, mpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, monsterbookcover = ?, dojo_pts = ?, dojoRecord = ?, reborns = ?, subcategory = ? WHERE id = ?", DatabaseConnection.RETURN_GENERATED_KEYS);
			ps.setInt(1, level);
			ps.setInt(2, fame);
                        ps.setInt(3, score);
			ps.setInt(4, stats.getStr());
			ps.setInt(5, stats.getDex());
			ps.setInt(6, stats.getLuk());
			ps.setInt(7, stats.getInt());
			ps.setInt(8, exp);
			ps.setInt(9, stats.getHp() < 1 ? 50 : stats.getHp());
			ps.setInt(10, stats.getMp());
			ps.setInt(11, stats.getMaxHp());
			ps.setInt(12, stats.getMaxMp());
			final StringBuilder sps = new StringBuilder();
			for (int i = 0; i < remainingSp.length; i++) {
				sps.append(remainingSp[i]);
				sps.append(",");
			}
			final String sp = sps.toString();
			ps.setString(13, sp.substring(0, sp.length() - 1));
			ps.setInt(14, remainingAp);
			ps.setInt(15, gmLevel);
			ps.setInt(16, skinColor);
			ps.setInt(17, gender);
			ps.setInt(18, job);
			ps.setInt(19, hair);
			ps.setInt(20, face);
			if (!fromcs) {
				if (map.getForcedReturnId() != 999999999) {
					ps.setInt(21, map.getForcedReturnId());
				} else {
					ps.setInt(21, stats.getHp() < 1 ? map.getReturnMapId() : map.getId());
				}
			} else {
				ps.setInt(21, mapid);
			}
			ps.setInt(22, meso);
			ps.setInt(23, hpApUsed);
			ps.setInt(24, mpApUsed);
			if (map == null) {
				ps.setInt(25, 0);
			} else {
				final MaplePortal closest = map.findClosestSpawnpoint(getPosition());
				ps.setInt(25, closest != null ? closest.getId() : 0);
			}
			ps.setInt(26, party != null ? party.getId() : -1);
			ps.setInt(27, buddylist.getCapacity());
			if (messenger != null) {
				ps.setInt(28, messenger.getId());
				ps.setInt(29, messengerposition);
			} else {
				ps.setInt(28, 0);
				ps.setInt(29, 4);
			}
			ps.setInt(30, bookCover);
			ps.setInt(31, dojo);
			ps.setInt(32, dojoRecord);
			ps.setInt(33, getReborns());
			ps.setInt(34, subcategory);
			ps.setInt(35, id);
			if (ps.executeUpdate() < 1) {
				throw new DatabaseException(":: Character not in database (" + id + ") ::");
			}
			ps.close();
			for (final MaplePet pet : pets) {
				if (pet.getSummoned()) {
					pet.saveToDb(); // Only save those summoned :P
				}
			}
			deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
			for (int i = 0; i < 5; i++) {
				final SkillMacro macro = skillMacros[i];
				if (macro != null) {
					ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
					ps.setInt(1, id);
					ps.setInt(2, macro.getSkill1());
					ps.setInt(3, macro.getSkill2());
					ps.setInt(4, macro.getSkill3());
					ps.setString(5, macro.getName());
					ps.setInt(6, macro.getShout());
					ps.setInt(7, i);
					ps.execute();
					ps.close();
				}
			}
			deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
			ps.setInt(1, id);
			ps.setInt(2, getInventory(MapleInventoryType.EQUIP).getSlotLimit());
			ps.setInt(3, getInventory(MapleInventoryType.USE).getSlotLimit());
			ps.setInt(4, getInventory(MapleInventoryType.SETUP).getSlotLimit());
			ps.setInt(5, getInventory(MapleInventoryType.ETC).getSlotLimit());
			ps.setInt(6, getInventory(MapleInventoryType.CASH).getSlotLimit());
			ps.execute();
			ps.close();
			deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO inventoryitems (characterid, itemid, inventorytype, position, quantity, owner, GM_Log, petid, expiredate, flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
			pse = con.prepareStatement("INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			for (final MapleInventory iv : inventory) {
				ps.setInt(3, iv.getType().getType());
				for (final IItem item : iv.list()) {
					ps.setInt(1, id);
					ps.setInt(2, item.getItemId());
					ps.setInt(4, item.getPosition());
					ps.setInt(5, item.getQuantity());
					ps.setString(6, item.getOwner());
					ps.setString(7, item.getGMLog());
					ps.setInt(8, item.getPet() != null ? item.getPet().getUniqueId() : -1);
					ps.setLong(9, item.getExpiration());
					ps.setByte(10, item.getFlag());
					ps.executeUpdate();
					rs = ps.getGeneratedKeys();
					int itemid;
					if (rs.next()) {
						itemid = rs.getInt(1);
					} else {
						throw new DatabaseException(":: Failed inserting character ::");
					}
					if (iv.getType().equals(MapleInventoryType.EQUIP) || iv.getType().equals(MapleInventoryType.EQUIPPED)) {
						pse.setInt(1, itemid);
						IEquip equip = (IEquip) item;
						pse.setInt(2, equip.getUpgradeSlots());
						pse.setInt(3, equip.getLevel());
						pse.setInt(4, equip.getStr());
						pse.setInt(5, equip.getDex());
						pse.setInt(6, equip.getInt());
						pse.setInt(7, equip.getLuk());
						pse.setInt(8, equip.getHp());
						pse.setInt(9, equip.getMp());
						pse.setInt(10, equip.getWatk());
						pse.setInt(11, equip.getMatk());
						pse.setInt(12, equip.getWdef());
						pse.setInt(13, equip.getMdef());
						pse.setInt(14, equip.getAcc());
						pse.setInt(15, equip.getAvoid());
						pse.setInt(16, equip.getHands());
						pse.setInt(17, equip.getSpeed());
						pse.setInt(18, equip.getJump());
						pse.setInt(19, equip.getRingId());
						pse.setInt(20, equip.getViciousHammer());
						pse.setInt(21, equip.getItemLevel());
						pse.setInt(22, equip.getItemEXP());
						pse.setInt(23, equip.getPotential());
						pse.setInt(24, equip.getPStars());
						pse.setInt(25, equip.getPotential_1());
						pse.setInt(26, equip.getPotential_2());
						pse.setInt(27, equip.getPotential_3());
						pse.executeUpdate();
					}
				}
			}
			ps.close();
			pse.close();
			deleteWhereCharacterId(con, "DELETE FROM questinfo WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO questinfo (`characterid`, `quest`, `data`) VALUES (?, ?, ?)");
			ps.setInt(1, id);
			for (final Entry<Integer, String> q : questinfo.entrySet()) {
				ps.setInt(2, q.getKey());
				ps.setString(3, q.getValue());
				ps.execute();
			}
			ps.close();
			deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
			pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
			ps.setInt(1, id);
			for (final MapleQuestStatus q : quests.values()) {
				ps.setInt(2, q.getQuest().getId());
				ps.setInt(3, q.getStatus());
				ps.setInt(4, (int) (q.getCompletionTime() / 1000));
				ps.setInt(5, q.getForfeited());
				ps.setString(6, q.getCustomData());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				rs.next();
				if (q.hasMobKills()) {
					for (int mob : q.getMobKills().keySet()) {
						pse.setInt(1, rs.getInt(1));
						pse.setInt(2, mob);
						pse.setInt(3, q.getMobKills(mob));
						pse.executeUpdate();
					}
				}
				rs.close();
			}
			ps.close();
			pse.close();
			deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel) VALUES (?, ?, ?, ?)");
			ps.setInt(1, id);
			for (final Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
				ps.setInt(2, skill.getKey().getId());
				ps.setInt(3, skill.getValue().skillevel);
				ps.setInt(4, skill.getValue().masterlevel);
				ps.execute();
			}
			ps.close();
			if (dc && getAllCooldowns().size() > 0) {
				for (final PlayerCoolDownValueHolder cooling : getAllCooldowns()) {
					ps = con.prepareStatement("INSERT INTO skills_cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
					ps.setInt(1, getId());
					ps.setInt(2, cooling.skillId);
					ps.setLong(3, cooling.startTime);
					ps.setLong(4, cooling.length);
					ps.execute();
				}
				ps.close();
			}
			deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
			ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
			ps.setInt(1, id);
			for (final SavedLocationType savedLocationType : SavedLocationType.values()) {
				if (savedLocations[savedLocationType.ordinal()] != -1) {
					ps.setInt(2, savedLocationType.ordinal());
					ps.setInt(3, savedLocations[savedLocationType.ordinal()]);
					ps.execute();
				}
			}
			ps.close();
			deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
			ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, 0)");
			ps.setInt(1, id);
			for (BuddylistEntry entry : buddylist.getBuddies()) {
				if (entry.isVisible()) {
					ps.setInt(2, entry.getCharacterId());
					ps.execute();
				}
			}
			ps.close();
			ps = con.prepareStatement("UPDATE accounts SET `ACash` = ?, `mPoints` = ?, `vpoints` = ? WHERE id = ?");
			ps.setInt(1, acash);
			ps.setInt(2, maplepoints);
			ps.setInt(3, getVPoints());
			ps.setInt(4, client.getAccID());
			ps.execute();
			ps.close();
			if (storage != null) {
				storage.saveToDB();
			}
			keylayout.saveKeys(id);
			mount.saveMount(id);
			monsterbook.saveCards(id);
			deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?");
			for (int i = 0; i < getWishlistSize(); i++) {
				ps = con.prepareStatement("INSERT INTO wishlist(characterid, sn) VALUES(?, ?) ");
				ps.setInt(1, getId());
				ps.setInt(2, wishlist[i]);
				ps.execute();
				ps.close();
			}
			deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
			for (int i = 0; i < getRockSize(); i++) {
				if (rocks[i] != 999999999) {
					ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid) VALUES(?, ?) ");
					ps.setInt(1, getId());
					ps.setInt(2, rocks[i]);
					ps.execute();
					ps.close();
				}
			}
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(MapleClient.getLogMessage(this, ":: Failed to save character data ") + e + " ::");
			try {
				con.rollback();
			} catch (SQLException ex) {
				System.err.println(MapleClient.getLogMessage(this, "[charsave] Error Rolling Back") + e);
			}
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (pse != null) {
					pse.close();
				}
				if (rs != null) {
					rs.close();
				}
				con.setAutoCommit(true);
				con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			} catch (SQLException e) {
				System.err.println(MapleClient.getLogMessage(this, "[charsave] Error going back to autocommit mode") + e);
			}
		}
	}

	private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setInt(1, id);
		ps.executeUpdate();
		ps.close();
	}

	public final PlayerStats getStat() {
		return stats;
	}

	public final PlayerRandomStream CRand() {
		return CRand;
	}

	public final void QuestInfoPacket(final tools.data.output.MaplePacketLittleEndianWriter mplew) {
		mplew.writeShort(questinfo.size());

		for (final Entry<Integer, String> q : questinfo.entrySet()) {
			mplew.writeShort(q.getKey());
			mplew.writeMapleAsciiString(q.getValue() == null ? "" : q.getValue());
		}
		mplew.writeInt(0); //PQ rank and stuff
	}

	public final void updateInfoQuest(final int questid, final String data) {
		questinfo.put(questid, data);
		client.getSession().write(MaplePacketCreator.updateInfoQuest(questid, data));
	}

	public final String getInfoQuest(final int questid) {
		if (questinfo.containsKey(questid)) {
			return questinfo.get(questid);
		}
		return "";
	}

	public final int getNumQuest() {
		int i = 0;
		for (final MapleQuestStatus q : quests.values()) {
			if (q.getStatus() == 2 && !q.isCustomQuest()) {
				i++;
			}
		}
		return i;
	}

	public final byte getQuestStatus(final int quest) {
		for (final MapleQuestStatus q : quests.values()) {
			if (q.getQuest().getId() == quest) {
				return q.getStatus();
			}
		}
		return 0;
	}

	public final MapleQuestStatus getQuest(final MapleQuest quest) {
		if (!quests.containsKey(quest)) {
			return new MapleQuestStatus(quest, (byte) 0);
		}
		return quests.get(quest);
	}

	public final MapleQuestStatus getQuestNAdd(final MapleQuest quest) {
		if (!quests.containsKey(quest)) {
			final MapleQuestStatus status = new MapleQuestStatus(quest, (byte) 0);
			quests.put(quest, status);
			return status;
		}
		return quests.get(quest);
	}

	public final void updateQuest(final MapleQuestStatus quest) {
		quests.put(quest.getQuest(), quest);
		if (!quest.isCustomQuest()) {

			if (quest.getStatus() == 1) {
				client.getSession().write(MaplePacketCreator.startQuest(this, (short) quest.getQuest().getId(), quest.getCustomData()));
				client.getSession().write(MaplePacketCreator.updateQuestInfo(this, (short) quest.getQuest().getId(), quest.getNpc(), (byte) 10));
			} else if (quest.getStatus() == 2) {
				client.getSession().write(MaplePacketCreator.completeQuest((short) quest.getQuest().getId()));
                                client.getSession().write(MaplePacketCreator.updateQuestInfo(this, (short) quest.getQuest().getId(), quest.getNpc(), (byte) 10));
			} else if (quest.getStatus() == 0) {
				client.getSession().write(MaplePacketCreator.forfeitQuest(this, (short) quest.getQuest().getId()));
			}
		}
	}

	public final Map<Integer, String> getInfoQuest_Map() {
		return questinfo;
	}

	public final Map<MapleQuest, MapleQuestStatus> getQuest_Map() {
		return quests;
	}

	public boolean isActiveBuffedValue(int skillid) {
		LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
		for (MapleBuffStatValueHolder mbsvh : allBuffs) {
			if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
				return true;
			}
		}
		return false;
	}

	public Integer getBuffedValue(MapleBuffStat effect) {
		final MapleBuffStatValueHolder mbsvh = effects.get(effect);
		return mbsvh == null ? null : Integer.valueOf(mbsvh.value);
	}

	public final Integer getBuffedSkill_X(final MapleBuffStat effect) {
		final MapleBuffStatValueHolder mbsvh = effects.get(effect);
		if (mbsvh == null) {
			return null;
		}
		return mbsvh.effect.getX();
	}

	public final Integer getBuffedSkill_Y(final MapleBuffStat effect) {
		final MapleBuffStatValueHolder mbsvh = effects.get(effect);
		if (mbsvh == null) {
			return null;
		}
		return mbsvh.effect.getY();
	}

	public final MapleStatEffect getBuffedSkillEffect(final MapleBuffStat effect) {
		final MapleBuffStatValueHolder mbsvh = effects.get(effect);
		if (mbsvh == null) {
			return null;
		}
		return mbsvh.effect;
	}

	public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
		final MapleBuffStatValueHolder mbsvh = effects.get(stat);
		if (mbsvh == null) {
			return false;
		}
		return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
	}

	public int getBuffSource(MapleBuffStat stat) {
		final MapleBuffStatValueHolder mbsvh = effects.get(stat);
		return mbsvh == null ? -1 : mbsvh.effect.getSourceId();
	}

	public int getItemQuantity(int itemid, boolean checkEquipped) {
		int possesed = inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
		if (checkEquipped) {
			possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
		}
		return possesed;
	}

	public int getReborns() {
		return reborns;
	}

	public int getVPoints() {
		return vpoints;
	}

	public int getSnipeDamage() {
		return Math.min(damageCap, (500000 * getReborns()) + 500000);
	}

	public int getMaxStats() {
		return (getJob() > 999 && getJob() < 2000 ? 15000 : 32000);

	}

	public int getNX() {
		return acash;
	}

	public void gainVPoints(int gainedpoints) {
		this.vpoints += gainedpoints;
	}

	public final void gainItem(final int id, final short quantity, final boolean randomStats, final long period) {
		if (quantity >= 0) {
			final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			final MapleInventoryType type = GameConstants.getInventoryType(id);

			if (!MapleInventoryManipulator.checkSpace(OdinSEA.c, id, quantity, "")) {
				return;
			}
			if (type.equals(MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
				final IItem item = randomStats ? ii.randomizeStats((Equip) ii.getEquipById(id)) : ii.getEquipById(id);
				if (period > 0) {
					item.setExpiration(System.currentTimeMillis() + period);
				}
				MapleInventoryManipulator.addbyItem(OdinSEA.c, item);
			} else {
				MapleInventoryManipulator.addById(OdinSEA.c, id, quantity, "", null, period);
			}
		} else {
			MapleInventoryManipulator.removeById(OdinSEA.c, GameConstants.getInventoryType(id), id, -quantity, true, false);
		}
		OdinSEA.c.getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
	}

	public int getWorld() {
		return world;
	}

	public void setBuffedValue(MapleBuffStat effect, int value) {
		final MapleBuffStatValueHolder mbsvh = effects.get(effect);
		if (mbsvh == null) {
			return;
		}
		mbsvh.value = value;
	}

	public Long getBuffedStarttime(MapleBuffStat effect) {
		final MapleBuffStatValueHolder mbsvh = effects.get(effect);
		return mbsvh == null ? null : Long.valueOf(mbsvh.startTime);
	}

	public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
		final MapleBuffStatValueHolder mbsvh = effects.get(effect);
		return mbsvh == null ? null : mbsvh.effect;
	}

	private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
		if (dragonBloodSchedule != null) {
			dragonBloodSchedule.cancel(false);
		}
		dragonBloodSchedule = TimerManager.getInstance().register(new Runnable() {

			@Override
			public void run() {
				if (stats.getHp() - bloodEffect.getX() > 1) {
					cancelBuffStats(MapleBuffStat.DRAGONBLOOD);
				} else {
					addHP(-bloodEffect.getX());
					client.getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
					map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
				}
			}
		}, 4000, 4000);
	}

	public void startFullnessSchedule(final int decrease, final MaplePet pet, int petSlot) {
		ScheduledFuture<?> schedule = TimerManager.getInstance().register(new Runnable() {

			@Override
			public void run() {
				int newFullness = pet.getFullness() - decrease;
				if (newFullness <= 5) {
					pet.setFullness(15);
					unequipPet(pet, true, true);
				} else {
					pet.setFullness(newFullness);
					client.getSession().write(PetPacket.updatePet(pet, true));
				}
			}
		}, 60000, 60000);
		switch (petSlot) {
			case 0:
				fullnessSchedule = schedule;
				break;
			case 1:
				fullnessSchedule_1 = schedule;
				break;
			case 2:
				fullnessSchedule_2 = schedule;
				break;
		}
	}

	public void cancelFullnessSchedule(int petSlot) {
		switch (petSlot) {
			case 0:
				if (fullnessSchedule != null) {
					fullnessSchedule.cancel(false);
				}
				break;
			case 1:
				if (fullnessSchedule_1 != null) {
					fullnessSchedule_1.cancel(false);
				}
				break;
			case 2:
				if (fullnessSchedule_2 != null) {
					fullnessSchedule_2.cancel(false);
				}
				break;
		}
	}

	public void startMapTimeLimitTask(int time, final MapleMap to) {
		client.getSession().write(MaplePacketCreator.getClock(time));

		time *= 1000;
		mapTimeLimitTask = TimerManager.getInstance().register(new Runnable() {

			@Override
			public void run() {
				changeMap(to, to.getPortal(0));
			}
		}, time, time);
	}

	public void startFishingTask(final boolean VIP) {
		final int time = VIP ? 30000 : 60000;
		cancelFishingTask();

		fishing = TimerManager.getInstance().register(new Runnable() {

			@Override
			public void run() {
				if (!haveItem(2300000, 1, false, true)) {
					cancelFishingTask();
					return;
				}
				MapleInventoryManipulator.removeById(client, MapleInventoryType.USE, 2300000, 1, false, false);

				final int randval = RandomRewards.getInstance().getFishingReward();

				switch (randval) {
					case 0: // Meso
						final int money = Randomizer.rand(10, 50000);
						gainMeso(money, true);
						client.getSession().write(UIPacket.fishingUpdate((byte) 1, money));
						break;
					case 1: // EXP
						final int experi = Randomizer.nextInt(GameConstants.getExpNeededForLevel(level) / 200);
						gainExp(experi, true, false, true);
						client.getSession().write(UIPacket.fishingUpdate((byte) 2, experi));
						break;
					default:
						MapleInventoryManipulator.addById(client, randval, (short) 1);
						client.getSession().write(UIPacket.fishingUpdate((byte) 0, randval));
						break;
				}
				map.broadcastMessage(UIPacket.fishingCaught(id));
			}
		}, time, time);
	}

	public void cancelMapTimeLimitTask() {
		if (mapTimeLimitTask != null) {
			mapTimeLimitTask.cancel(false);
		}
	}

	public void cancelFishingTask() {
		if (fishing != null) {
			fishing.cancel(false);
		}
	}

	public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
		if (effect.isHide()) {
			this.hidden = true;
			map.broadcastMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
		} else if (effect.isDragonBlood()) {
			prepareDragonBlood(effect);
		} else if (effect.isBerserk()) {
			checkBerserk();
		} else if (effect.isBeholder()) {
			prepareBeholderEffect();
		}
		for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
			effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, statup.getRight().intValue()));
		}
		stats.recalcLocalStats();
	}

	public List<MapleBuffStat> getBuffStats(final MapleStatEffect effect, final long startTime) {
		final List<MapleBuffStat> bstats = new ArrayList<MapleBuffStat>();

		for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
			final MapleBuffStatValueHolder mbsvh = stateffect.getValue();
			if (mbsvh.effect.sameSource(effect) && (startTime == -1 || startTime == mbsvh.startTime)) {
				bstats.add(stateffect.getKey());
			}
		}
		return bstats;
	}

	private void deregisterBuffStats(List<MapleBuffStat> stats) {
		List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
		for (MapleBuffStat stat : stats) {
			final MapleBuffStatValueHolder mbsvh = effects.get(stat);
			if (mbsvh != null) {
				effects.remove(stat);
				boolean addMbsvh = true;
				for (MapleBuffStatValueHolder contained : effectsToCancel) {
					if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
						addMbsvh = false;
					}
				}
				if (addMbsvh) {
					effectsToCancel.add(mbsvh);
				}
				if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET || stat == MapleBuffStat.MIRROR_TARGET) {
					final int summonId = mbsvh.effect.getSourceId();
					final MapleSummon summon = summons.get(summonId);
					if (summon != null) {
						map.broadcastMessage(MaplePacketCreator.removeSummon(summon, true));
						map.removeMapObject(summon);
						removeVisibleMapObject(summon);
						summons.remove(summonId);
					}
					if (summon.getSkill() == 1321007) {
						if (beholderHealingSchedule != null) {
							beholderHealingSchedule.cancel(false);
							beholderHealingSchedule = null;
						}
						if (beholderBuffSchedule != null) {
							beholderBuffSchedule.cancel(false);
							beholderBuffSchedule = null;
						}
					}
				} else if (stat == MapleBuffStat.DRAGONBLOOD) {
					if (dragonBloodSchedule != null) {
						dragonBloodSchedule.cancel(false);
						dragonBloodSchedule = null;
					}
				}
			}
		}
		for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
			if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).size() == 0) {
				if (cancelEffectCancelTasks.schedule != null) {
					cancelEffectCancelTasks.schedule.cancel(false);
				}
			}
		}
	}

	/**
	 * @param effect
	 * @param overwrite when overwrite is set no data is sent and all the Buffstats in the StatEffect are deregistered
	 * @param startTime
	 */
	public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
		List<MapleBuffStat> buffstats;
		if (!overwrite) {
			buffstats = getBuffStats(effect, startTime);
		} else {
			List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
			buffstats = new ArrayList<MapleBuffStat>(statups.size());
			for (Pair<MapleBuffStat, Integer> statup : statups) {
				buffstats.add(statup.getLeft());
			}
		}
		deregisterBuffStats(buffstats);
		if (effect.isMagicDoor()) {
			// remove for all on maps
			if (!getDoors().isEmpty()) {
				final MapleDoor door = getDoors().iterator().next();
				for (final MapleCharacter chr : door.getTarget().getCharacters()) {
					door.sendDestroyData(chr.getClient());
				}
				for (final MapleCharacter chr : door.getTown().getCharacters()) {
					door.sendDestroyData(chr.getClient());
				}
				for (final MapleDoor destroyDoor : getDoors()) {
					door.getTarget().removeMapObject(destroyDoor);
					door.getTown().removeMapObject(destroyDoor);
				}
				clearDoors();
				silentPartyUpdate();
			}
		} else if (effect.isMonsterRiding()) {
//	    if (effect.getSourceId() != 5221006) {
//		getMount().cancelSchedule();
//	    }
		} else if (effect.isAranCombo()) {
			combo = 0;
		}
		// check if we are still logged in o.o
		if (!overwrite) {
			cancelPlayerBuffs(buffstats);
			if (effect.isHide() && (MapleCharacter) map.getMapObject(getObjectId()) != null) {
				this.hidden = false;
				//map.broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);

				for (final MaplePet pet : pets) {
					if (pet.getSummoned()) {
						map.broadcastMessage(this, PetPacket.showPet(this, pet, false, false), false);
					}
				}
			}
		}
	}

	public void cancelBuffStats(MapleBuffStat stat) {
		List<MapleBuffStat> buffStatList = Arrays.asList(stat);
		deregisterBuffStats(buffStatList);
		cancelPlayerBuffs(buffStatList);
	}

	public void cancelEffectFromBuffStat(MapleBuffStat stat) {
		cancelEffect(effects.get(stat).effect, false, -1);
	}

	private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
		if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) { // are we still connected ?
			if (buffstats.contains(MapleBuffStat.HOMING_BEACON)) {
				client.getSession().write(MaplePacketCreator.cancelHoming());
			} else {
				stats.recalcLocalStats();
				enforceMaxHpMp();
				client.getSession().write(MaplePacketCreator.cancelBuff(buffstats, buffstats.contains(MapleBuffStat.MONSTER_RIDING)));
				map.broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
			}
		}
		if (buffstats.contains(MapleBuffStat.MONSTER_RIDING) && GameConstants.isEvan(job) && job >= 2200) {
			makeDragon();
			map.spawnDragon(dragon);
		}
	}

	public void dispel() {
		if (!isHidden()) {
			final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
			for (MapleBuffStatValueHolder mbsvh : allBuffs) {
				if (mbsvh.effect.isSkill() && mbsvh.schedule != null && !mbsvh.effect.isMorph()) {
					cancelEffect(mbsvh.effect, false, mbsvh.startTime);
				}
			}
		}
	}

	public void dispelSkill(int skillid) {
		final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());

		for (MapleBuffStatValueHolder mbsvh : allBuffs) {
			if (skillid == 0) {
				if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() == 1004 || mbsvh.effect.getSourceId() == 10001004 || mbsvh.effect.getSourceId() == 20001004 || mbsvh.effect.getSourceId() == 20011004 || mbsvh.effect.getSourceId() == 1321007 || mbsvh.effect.getSourceId() == 2121005 || mbsvh.effect.getSourceId() == 2221005 || mbsvh.effect.getSourceId() == 2311006 || mbsvh.effect.getSourceId() == 2321003 || mbsvh.effect.getSourceId() == 3111002 || mbsvh.effect.getSourceId() == 3111005 || mbsvh.effect.getSourceId() == 3211002 || mbsvh.effect.getSourceId() == 3211005 || mbsvh.effect.getSourceId() == 4111002)) {
					cancelEffect(mbsvh.effect, false, mbsvh.startTime);
					break;
				}
			} else {
				if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
					cancelEffect(mbsvh.effect, false, mbsvh.startTime);
					break;
				}
			}
		}
	}

	public void cancelAllBuffs_() {
		effects.clear();
	}

	public void cancelAllBuffs() {
		final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());

		for (MapleBuffStatValueHolder mbsvh : allBuffs) {
			cancelEffect(mbsvh.effect, false, mbsvh.startTime);
		}
	}

	public void cancelMorphs() {
		final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());

		for (MapleBuffStatValueHolder mbsvh : allBuffs) {
			switch (mbsvh.effect.getSourceId()) {
				case 5111005:
				case 5121003:
				case 15111002:
				case 13111005:
					return; // Since we can't have more than 1, save up on loops
				default:
					if (mbsvh.effect.isMorph()) {
						cancelEffect(mbsvh.effect, false, mbsvh.startTime);
						continue;
					}
			}
		}
	}

	public int getMorphState() {
		LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
		for (MapleBuffStatValueHolder mbsvh : allBuffs) {
			if (mbsvh.effect.isMorph()) {
				return mbsvh.effect.getSourceId();
			}
		}
		return -1;
	}

	public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
		for (PlayerBuffValueHolder mbsvh : buffs) {
			mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
		}
	}

	public List<PlayerBuffValueHolder> getAllBuffs() {
		List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
		for (MapleBuffStatValueHolder mbsvh : effects.values()) {
			ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
		}
		return ret;
	}

	public void cancelMagicDoor() {
		final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());

		for (MapleBuffStatValueHolder mbsvh : allBuffs) {
			if (mbsvh.effect.isMagicDoor()) {
				cancelEffect(mbsvh.effect, false, mbsvh.startTime);
				break;
			}
		}
	}

	public final void handleEnergyCharge(final int skillid, final byte targets) {
		final ISkill echskill = SkillFactory.getSkill(skillid);
		final byte skilllevel = getSkillLevel(echskill);
		if (skilllevel > 0) {
			if (targets > 0) {
				if (getBuffedValue(MapleBuffStat.ENERGY_CHARGE) == null) {
					echskill.getEffect(skilllevel).applyEnergyBuff(this, true); // Infinity time
				} else {
					Integer energyLevel = getBuffedValue(MapleBuffStat.ENERGY_CHARGE);

					if (energyLevel < 10000) {
						energyLevel += (100 * targets);
						setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energyLevel);
						client.getSession().write(MaplePacketCreator.giveEnergyChargeTest(energyLevel));

						if (energyLevel >= 10000) {
							energyLevel = 10001;
						}
					} else if (energyLevel == 10001) {
						echskill.getEffect(skilllevel).applyEnergyBuff(this, false); // One with time
						energyLevel = 10002;
					}
				}
			}
		}
	}

	public final void handleOrbgain() {
		int orbcount = getBuffedValue(MapleBuffStat.COMBO);
		ISkill combo;
		ISkill advcombo;

		switch (getJob()) {
			case 1110:
			case 1111:
				combo = SkillFactory.getSkill(11111001);
				advcombo = SkillFactory.getSkill(11110005);
				break;
			default:
				combo = SkillFactory.getSkill(1111002);
				advcombo = SkillFactory.getSkill(1120003);
				break;
		}

		MapleStatEffect ceffect = null;
		int advComboSkillLevel = getSkillLevel(advcombo);
		if (advComboSkillLevel > 0) {
			ceffect = advcombo.getEffect(advComboSkillLevel);
		} else {
			ceffect = combo.getEffect(getSkillLevel(combo));
		}

		if (orbcount < ceffect.getX() + 1) {
			int neworbcount = orbcount + 1;
			if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
				if (neworbcount < ceffect.getX() + 1) {
					neworbcount++;
				}
			}
			List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, neworbcount));
			setBuffedValue(MapleBuffStat.COMBO, neworbcount);
			int duration = ceffect.getDuration();
			duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

			client.getSession().write(MaplePacketCreator.giveBuff(1111002, duration, stat, ceffect));
			map.broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, ceffect), false);
		}
	}

	public void handleOrbconsume() {
		ISkill combo;

		switch (getJob()) {
			case 1110:
			case 1111:
				combo = SkillFactory.getSkill(11111001);
				break;
			default:
				combo = SkillFactory.getSkill(1111002);
				break;
		}

		MapleStatEffect ceffect = combo.getEffect(getSkillLevel(combo));
		List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
		setBuffedValue(MapleBuffStat.COMBO, 1);
		int duration = ceffect.getDuration();
		duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

		client.getSession().write(MaplePacketCreator.giveBuff(1111002, duration, stat, ceffect));
		map.broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, ceffect), false);
	}

	private void silentEnforceMaxHpMp() {
		stats.setMp(stats.getMp());
		stats.setHp(stats.getHp(), true);
	}

	private void enforceMaxHpMp() {
		List<Pair<MapleStat, Integer>> statups = new ArrayList<Pair<MapleStat, Integer>>(2);
		if (stats.getMp() > stats.getCurrentMaxMp()) {
			stats.setMp(stats.getMp());
			statups.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(stats.getMp())));
		}
		if (stats.getHp() > stats.getCurrentMaxHp()) {
			stats.setHp(stats.getHp());
			statups.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(stats.getHp())));
		}
		if (statups.size() > 0) {
			client.getSession().write(MaplePacketCreator.updatePlayerStats(statups, getJob()));
		}
	}

	public MapleMap getMap() {
		return map;
	}

	public MonsterBook getMonsterBook() {
		return monsterbook;
	}

	public void setMap(MapleMap newmap) {
		this.map = newmap;
	}

	public void setMap(int PmapId) {
		this.mapid = PmapId;
	}

	public int getMapId() {
		if (map != null) {
			return map.getId();
		}
		return mapid;
	}

	public int getInitialSpawnpoint() {
		return initialSpawnPoint;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getWorldName() {
		return name;
	}

	public static int getDamageCap() {
		return damageCap;
	}

	public final String getBlessOfFairyOrigin() {
		return this.BlessOfFairy_Origin;
	}

	public final short getLevel() {
		return level;
	}

	public int getRank() {
		return rank;
	}

	public int getRankMove() {
		return rankMove;
	}

	public int getJobRank() {
		return jobRank;
	}

	public int getJobRankMove() {
		return jobRankMove;
	}

	public int getFame() {
		return fame;
	}
        
        public int getScore() {
		return score;
	}

	public final int getDojo() {
		return dojo;
	}

	public final int getDojoRecord() {
		return dojoRecord;
	}

	public final int getFallCounter() {
		return fallcounter;
	}

	public final MapleClient getClient() {
		return client;
	}

	public final void setClient(final MapleClient client) {
		this.client = client;
	}

	public int getExp() {
		return exp;
	}

	public int getRemainingAp() {
		return remainingAp;
	}

	public int[] getRemainingSps() {
		return remainingSp;
	}

	public int getRemainingSp() {
		return remainingSp[GameConstants.getSkillBook(job)]; //default
	}

	public int getRemainingSp(final int skillbook) {
		return remainingSp[skillbook];
	}

	public int getRemainingSpSize() {
		int ret = 0;
		for (int i = 0; i < remainingSp.length; i++) {
			if (remainingSp[i] > 0) {
				ret++;
			}

		}
		return ret;
	}

	public int getMpApUsed() {
		return mpApUsed;
	}

	public void setMpApUsed(int mpApUsed) {
		this.mpApUsed = mpApUsed;
	}

	public int getHpApUsed() {
		return hpApUsed;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHpApUsed(int hpApUsed) {
		this.hpApUsed = hpApUsed;
	}

	public int getSkinColor() {
		return skinColor;
	}

	public void setSkinColor(int skinColor) {
		this.skinColor = skinColor;
	}

	public int getJob() {
		return job;
	}

	public int getGender() {
		return gender;
	}

	public int getHair() {
		return hair;
	}

	public int getFace() {
		return face;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public void setHair(int hair) {
		this.hair = hair;
	}

	public void setFace(int face) {
		this.face = face;
	}

	public void setFame(int fame) {
		this.fame = fame;
	}

	public void setDojo(final int dojo) {
		this.dojo = dojo;
	}

	public void setDojoRecord(final boolean reset) {
		if (reset) {
			dojo = 0;
			dojoRecord = 0;
		} else {
			dojoRecord++;
		}
	}

	public void setFallCounter(int fallcounter) {
		this.fallcounter = fallcounter;
	}

	public void setRemainingAp(int remainingAp) {
		this.remainingAp = remainingAp;
	}

	public void setRemainingSp(int remainingSp) {
		this.remainingSp[GameConstants.getSkillBook(job)] = remainingSp; //default
	}

	public void setRemainingSp(int remainingSp, final int skillbook) {
		this.remainingSp[skillbook] = remainingSp;
	}

	public void setGender(byte gender) {
		this.gender = gender;
	}

	public CheatTracker getCheatTracker() {
		return anticheat;
	}

	public BuddyList getBuddylist() {
		return buddylist;
	}

	public void addFame(int famechange) {
		this.fame += famechange;
	}

	public void changeMapBanish(final int mapid, final String portal, final String msg) {
		dropMessage(5, msg);
		final MapleMap map = client.getChannelServer().getMapFactory(world).getMap(mapid);
		changeMap(map, map.getPortal(portal));
	}

	public void changeMap(final MapleMap to, final Point pos) {
		changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x81, this));
	}

	public void changeMap(final MapleMap to, final MaplePortal pto) {
		changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this));
	}

	private void changeMapInternal(final MapleMap to, final Point pos, MaplePacket warpPacket) {
		if (eventInstance != null) {
			eventInstance.changedMap(this, to.getId());
		}
		client.getSession().write(warpPacket);
		map.removePlayer(this);
		if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
			map = to;
			setPosition(pos);
			to.addPlayer(this);
			stats.relocHeal();
		}
	}

	public void leaveMap() {
		controlled.clear();
		visibleMapObjects.clear();
		if (chair != 0) {
			cancelFishingTask();
			chair = 0;
		}
		if (hpDecreaseTask != null) {
			hpDecreaseTask.cancel(false);
		}
		cancelMapTimeLimitTask();
	}

	public void resetStats(final int str, final int dex, final int int_, final int luk) {
		List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>(2);
		final MapleCharacter chr = this;
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
		client.getSession().write(MaplePacketCreator.updatePlayerStats(stats, false, chr.getJob()));
	}

	public void startHurtHp() {
		hpDecreaseTask = TimerManager.getInstance().register(new Runnable() {

			@Override
			public void run() {
				if (map.getHPDec() < 1 || !isAlive()) {
					return;
				} else if (getInventory(MapleInventoryType.EQUIPPED).findById(map.getHPDecProtect()) == null) {
					addHP(-map.getHPDec());
				}
			}
		}, 10000);
	}

	public void changeJob(int newJob) {
		final boolean isEv = GameConstants.isEvan(job);
		this.job = (short) newJob;
		if (newJob != 0 && newJob != 1000 && newJob != 2000 && newJob != 2001) {
			if (GameConstants.isEvan(newJob)) {
				remainingSp[GameConstants.getSkillBook(newJob)] += 3;
			} else {
				remainingSp[GameConstants.getSkillBook(newJob)]++;
				if (newJob % 10 >= 2) {
					remainingSp[GameConstants.getSkillBook(newJob)] += 2;
				}
			}
		}
		if (!isGM()) {
			if (newJob % 1000 == 100) { //first job = warrior
				resetStats(25, 4, 4, 4);
			} else if (newJob % 1000 == 200) {
				resetStats(4, 4, 20, 4);
			} else if (newJob % 1000 == 300 || newJob % 1000 == 400) {
				resetStats(4, 25, 4, 4);
			} else if (newJob % 1000 == 500) {
				resetStats(4, 20, 4, 4);
			}
		}
		client.getSession().write(MaplePacketCreator.updateSp(this, false, isEv));
		updateSingleStat(MapleStat.JOB, newJob);

		int maxhp = stats.getMaxHp(), maxmp = stats.getMaxMp();

		switch (job) {
			case 100: // Warrior
			case 1100: // Soul Master
			case 2100: // Aran
				maxhp += Randomizer.rand(200, 250);
				break;
			case 200: // Magician
			case 2200:
			case 2210:
				maxmp += Randomizer.rand(100, 150);
				break;
			case 300: // Bowman
			case 400: // Thief
			case 500: // Pirate
				maxhp += Randomizer.rand(100, 150);
				maxmp += Randomizer.rand(25, 50);
				break;
			case 110: // Fighter
				maxhp += Randomizer.rand(300, 350);
				break;
			case 120: // Page
			case 130: // Spearman
			case 1110: // Soul Master
			case 2110: // Aran
				maxhp += Randomizer.rand(300, 350);
				break;
			case 210: // FP
			case 220: // IL
			case 230: // Cleric
				maxmp += Randomizer.rand(400, 450);
				break;
			case 310: // Bowman
			case 320: // Crossbowman
			case 410: // Assasin
			case 420: // Bandit
			case 430:
			case 1310: // Wind Breaker
			case 1410: // Night Walker
				maxhp += Randomizer.rand(300, 350);
				maxhp += Randomizer.rand(150, 200);
				break;
			case 900: // GM
			case 800: // Manager
				maxhp += 30000;
				maxhp += 30000;
				break;
		}
		if (maxhp >= 30000) {
			maxhp = 30000;
		}
		if (maxmp >= 30000) {
			maxmp = 30000;
		}
		stats.setMaxHp(maxhp);
		stats.setMaxMp(maxmp);
		stats.setHp(maxhp);
		stats.setMp(maxmp);
		List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(2);
		statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
		statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
		stats.recalcLocalStats();
		client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, getJob()));
		map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 8), false);
		silentPartyUpdate();
		guildUpdate();
		if (dragon != null) {
			map.broadcastMessage(MaplePacketCreator.removeDragon(this.id));
			map.removeMapObject(dragon);
			dragon = null;
		}
		if (newJob >= 2200 && newJob <= 2218) { //make new
			if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
				cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
			}
			makeDragon();
			map.spawnDragon(dragon);
			if (newJob == 2217) {
				for (int i : Skill.evanskills1) {
					final ISkill skil = SkillFactory.getSkill(i);
					if (skil != null && getSkillLevel(skil) <= 0 && getMasterLevel(skil) <= 0) {
						changeSkillLevel(skil, (byte) skil.getMaxLevel(), (byte) skil.getMaxLevel());
					}
				}
			} else if (newJob == 2218) {
				for (int i : Skill.evanskills2) {
					final ISkill skil = SkillFactory.getSkill(i);
					if (skil != null && getSkillLevel(skil) <= 0 && getMasterLevel(skil) <= 0) {
						changeSkillLevel(skil, (byte) skil.getMaxLevel(), (byte) skil.getMaxLevel());
					}
				}
			}

		} else if (newJob >= 431 && newJob <= 434) { //master skills
			for (int i : Skill.skills) {
				final ISkill skil = SkillFactory.getSkill(i);
				if (skil != null && getSkillLevel(skil) <= 0 && getMasterLevel(skil) <= 0) {
					changeSkillLevel(skil, (byte) 0, (byte) skil.getMasterLevel());
				}
			}
		}
	}

	public void makeDragon() {
		dragon = new MapleDragon(this);
	}

	public MapleDragon getDragon() {
		return dragon;
	}

	public void gainAp(int ap) {
		this.remainingAp += ap;
		updateSingleStat(MapleStat.AVAILABLEAP, this.remainingAp);
	}

	public void gainSP(int sp) {
		this.remainingSp[GameConstants.getSkillBook(job)] += sp; //default
		client.getSession().write(MaplePacketCreator.updateSp(this, false));
		client.getSession().write(UIPacket.getSPMsg((byte) sp));
	}

	public void changeSkillLevel(final ISkill skill, byte newLevel, byte newMasterlevel) {
		if (skill == null || (!GameConstants.isApplicableSkill(skill.getId()) && !GameConstants.isApplicableSkill_(skill.getId()))) {

			return;

		}
		if (newLevel == 0 && newMasterlevel == 0) {
			if (skills.containsKey(skill)) {
				skills.remove(skill);
			}
		} else {
			if (newLevel < 0) {
				newLevel = 0;
			}
			if (newMasterlevel < 0) {
				newMasterlevel = 0;
			}
			skills.put(skill, new SkillEntry(newLevel, newMasterlevel));
		}
		if (GameConstants.isRecoveryIncSkill(skill.getId())) {
			stats.relocHeal();
		} else if (GameConstants.isElementAmp_Skill(skill.getId())) {
			stats.recalcLocalStats();
		}
		client.getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel));
	}

	public void playerDead() {
		if (getEventInstance() != null) {
			getEventInstance().playerKilled(this);
		}
		dispelSkill(0);
		if (getBuffedValue(MapleBuffStat.MORPH) != null) {
			cancelEffectFromBuffStat(MapleBuffStat.MORPH);
		}
		if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
			cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
		}
		if (getBuffedValue(MapleBuffStat.SUMMON) != null) {
			cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
		}
		if (getBuffedValue(MapleBuffStat.PUPPET) != null) {
			cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
		}
		if (getBuffedValue(MapleBuffStat.MIRROR_TARGET) != null) {
			cancelEffectFromBuffStat(MapleBuffStat.MIRROR_TARGET);
		}

		if (job != 0 && job != 1000 && job != 2000 && job != 2001) {
			int charms = getItemQuantity(5130000, false);
			if (charms > 0) {
				MapleInventoryManipulator.removeById(client, MapleInventoryType.CASH, 5130000, 1, true, false);

				charms--;
				if (charms > 0xFF) {
					charms = 0xFF;
				}
				client.getSession().write(MTSCSPacket.useCharm((byte) charms, (byte) 0));
			} else {
				float diepercentage = 0.0f;
				int expforlevel = GameConstants.getExpNeededForLevel(level);
				if (map.isTown() || FieldLimitType.RegularExpLoss.check(map.getFieldLimit())) {
					diepercentage = 0.01f;
				} else {
					float v8 = 0.0f;
					if (this.job / 100 == 3) {
						v8 = 0.08f;
					} else {
						v8 = 0.2f;
					}
					diepercentage = (float) (v8 / this.stats.getLuk() + 0.05);
				}
				int v10 = (int) (exp - (long) ((double) expforlevel * diepercentage));
				if (v10 < 0) {
					v10 = 0;
				}
				this.exp = v10;
			}
		}
		this.updateSingleStat(MapleStat.EXP, this.exp);
	}

	public void updatePartyMemberHP() {
		if (party != null) {
			final int channel = client.getChannel();
			for (MaplePartyCharacter partychar : party.getMembers()) {
				if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
					final MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
					if (other != null) {
						other.getClient().getSession().write(MaplePacketCreator.updatePartyMemberHP(getId(), stats.getHp(), stats.getCurrentMaxHp()));
					}
				}
			}
		}
	}

	public void receivePartyMemberHP() {
		int channel = client.getChannel();
		for (MaplePartyCharacter partychar : party.getMembers()) {
			if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
				MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
				if (other != null) {
					client.getSession().write(MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getStat().getHp(), other.getStat().getCurrentMaxHp()));
				}
			}
		}
	}

	/**
	 * Convenience function which adds the supplied parameter to the current hp then directly does a updateSingleStat.
	 *
	 * @see MapleCharacter#setHp(int)
	 * @param delta
	 */
	public void addHP(int delta) {
		if (stats.setHp(stats.getHp() + delta)) {
			updateSingleStat(MapleStat.HP, stats.getHp());
		}
	}

	/**
	 * Convenience function which adds the supplied parameter to the current mp then directly does a updateSingleStat.
	 *
	 * @see MapleCharacter#setMp(int)
	 * @param delta
	 */
	public void addMP(int delta) {
		if (stats.setMp(stats.getMp() + delta)) {
			updateSingleStat(MapleStat.MP, stats.getMp());
		}
	}

	public void addMPHP(int hpDiff, int mpDiff) {
		List<Pair<MapleStat, Integer>> statups = new ArrayList<Pair<MapleStat, Integer>>();

		if (stats.setHp(stats.getHp() + hpDiff)) {
			statups.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(stats.getHp())));
		}
		if (stats.setMp(stats.getMp() + mpDiff)) {
			statups.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(stats.getMp())));
		}
		if (statups.size() > 0) {
			client.getSession().write(MaplePacketCreator.updatePlayerStats(statups, getJob()));
		}
	}

	public void updateSingleStat(MapleStat stat, int newval) {
		updateSingleStat(stat, newval, false);
	}

	/**
	 * Updates a single stat of this MapleCharacter for the client. This method only creates and sends an update packet,
	 * it does not update the stat stored in this MapleCharacter instance.
	 *
	 * @param stat
	 * @param newval
	 * @param itemReaction
	 */
	public void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
		if (stat == MapleStat.AVAILABLESP) {
			client.getSession().write(MaplePacketCreator.updateSp(this, itemReaction, false));
			return;
		}
		client.getSession().write(MaplePacketCreator.updatePlayerStats(Collections.singletonList(new Pair<MapleStat, Integer>(stat, Integer.valueOf(newval))), itemReaction, client.getPlayer().getJob())); 
	}

	public void gainExp(final int total, final boolean show, final boolean inChat, final boolean white) {
		if (level == 200 || (GameConstants.isKOC(job) && level == 120)) {
			final int needed = GameConstants.getExpNeededForLevel(level);
			if (exp + total > needed) {
				setExp(needed);
			} else {
				exp += total;
			}
		} else {
			if (exp + total >= GameConstants.getExpNeededForLevel(level)) {
				exp += total;
				levelUp();

				final int needed = GameConstants.getExpNeededForLevel(level);
				if (exp > needed) {
					setExp(needed);
				}
			} else {
				exp += total;
			}
		}
		if (total != 0) {
			if (exp < 0) { // After adding, and negative
				if (total > 0) {
					setExp(GameConstants.getExpNeededForLevel(level));
				} else if (total < 0) {
					setExp(0);
				}
			}
			updateSingleStat(MapleStat.EXP, getExp());
			if (show) { // still show the expgain even if it's not there
				client.getSession().write(MaplePacketCreator.GainEXP_Others(total, inChat, white));
			}
		}
	}

	public void gainExpMonster(final int gain, final boolean show, final boolean white, final byte pty, final int CLASS_EXP) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault());
		int day = cal.get(Calendar.DAY_OF_WEEK);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int Base_EXP = 0;
		int Event_EXP = 0;
		int Wedding_EXP = 0;
		int Party_Ring_EXP = 0;
		int Party_EXP = 0;
		int Premium_EXP = 0;
		int Item_EXP = 0;
		int Rainbow_EXP = 0;
		if ((haveItem(5210006, 1, false, true) && hour > 22 && hour < 2) || (haveItem(5210007, 1, false, true) && hour > 2 && hour < 6) || (haveItem(5210008, 1, false, true) && hour > 6 && hour < 10) || (haveItem(5210009, 1, false, true) && hour > 10 && hour < 14) || (haveItem(5210010, 1, false, true) && hour > 14 && hour < 18) || (haveItem(5210011, 1, false, true) && hour > 18 && hour < 22)) {
			Base_EXP = gain * 2;
		} else {
			Base_EXP = gain;
		}
		if (level >= 1 && level <= 10) {
			Event_EXP = (int) (Base_EXP * 0.1);
		}
		if ((haveItem(1112127, 1, true, true))) {
			Party_Ring_EXP = (int) (Base_EXP * 0.8);
		}
		if (pty > 1) {
			Party_EXP = (int) (((float) (Base_EXP / 10)) * (pty + 1)); // 10%
		}
		if ((haveItem(11220171, 1, true, true))) { // Fairy's Pendant
			Item_EXP = (int) (Base_EXP * 0.1);
		}
		if (day == 7 || day == 1) {
			Rainbow_EXP = (int) (Base_EXP * 0.1);
		}
		if (level == 200 || (GameConstants.isKOC(job) && level == 120)) {
			final int needed = GameConstants.getExpNeededForLevel(level);
			if (exp + Base_EXP > needed) {
				setExp(needed);
			} else {
				exp += Base_EXP + Event_EXP + Wedding_EXP + Party_Ring_EXP + Party_EXP + Premium_EXP + Item_EXP + Rainbow_EXP + CLASS_EXP;
			}
		} else {
			if (exp + Base_EXP >= GameConstants.getExpNeededForLevel(level)) {
				exp += Base_EXP;
				levelUp();
				final int needed = GameConstants.getExpNeededForLevel(level);
				if (exp > needed) {
					setExp(needed);
				}
			} else {
				exp += Base_EXP + Event_EXP + Wedding_EXP + Party_Ring_EXP + Party_EXP + Premium_EXP + Item_EXP + Rainbow_EXP + CLASS_EXP;
			}
		}
		if (gain != 0) {
			if (exp < 0) { // After adding, and negative
				if (gain > 0) {
					setExp(GameConstants.getExpNeededForLevel(level));
				} else if (gain < 0) {
					setExp(0);
				}
			}
			updateSingleStat(MapleStat.EXP, getExp());
			if (show) { // still show the expgain even if it's not there
				client.getSession().write(MaplePacketCreator.GainEXP_Monster(Base_EXP, white, Event_EXP, Wedding_EXP, Party_Ring_EXP, Party_EXP, Premium_EXP, Item_EXP, Rainbow_EXP, CLASS_EXP));
			}
		}
	}

	public void silentPartyUpdate() {
		if (party != null) {
			try {
				client.getChannelServer().getWorldInterface().updateParty(party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(this));
			} catch (RemoteException e) {
				System.err.println("REMOTE THROW, silentPartyUpdate" + e);
				client.getChannelServer().reconnectWorld();
			}
		}
	}

	public boolean isGM() {
		return gmLevel > 0;
	}

	public int getGMLevel() {
		return gmLevel;
	}

	public boolean hasGmLevel(int level) {
		return gmLevel >= level;
	}

	public final MapleInventory getInventory(MapleInventoryType type) {
		return inventory[type.ordinal()];
	}

	public final MapleInventory[] getInventorys() {
		return inventory;
	}

	public final void expirationTask() {
		long expiration;
		final long currenttime = System.currentTimeMillis();
		final List<IItem> toberemove = new ArrayList<IItem>(); // This is here to prevent deadlock.

		for (final MapleInventory inv : inventory) {
			for (final IItem item : inv.list()) {
				expiration = item.getExpiration();

				if (expiration != -1 && !GameConstants.isPet(item.getItemId())) {
					byte flag = item.getFlag();

					if (ItemFlag.LOCK.check(flag)) {
						if (currenttime > expiration) {
							item.setExpiration(-1);
							item.setFlag((byte) (flag - ItemFlag.LOCK.getValue()));
							client.getSession().write(MaplePacketCreator.updateSpecialItemUse(item, item.getType()));
						}
					} else if (currenttime > expiration) {
						client.getSession().write(MTSCSPacket.itemExpired(item.getItemId()));
						toberemove.add(item);
					}
				}
			}
			for (final IItem item : toberemove) {
				MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), false);
			}
		}
	}

	public MapleShop getShop() {
		return shop;
	}

	public void setShop(MapleShop shop) {
		this.shop = shop;
	}

	public int getMeso() {
		return meso;
	}

	public final int[] getSavedLocations() {
		return savedLocations;
	}

	public int getSavedLocation(SavedLocationType type) {
		return savedLocations[type.ordinal()];
	}

	public void saveLocation(SavedLocationType type) {
		savedLocations[type.ordinal()] = getMapId();
	}

	public void clearSavedLocation(SavedLocationType type) {
		savedLocations[type.ordinal()] = -1;
	}

	public void gainMeso(int gain, boolean show) {
		gainMeso(gain, show, false, false);
	}

	public void gainMeso(int gain, boolean show, boolean enableActions) {
		gainMeso(gain, show, enableActions, false);
	}

	public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
		if (meso + gain < 0) {
			client.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		meso += gain;
		updateSingleStat(MapleStat.MESO, meso, enableActions);
		if (show) {
			client.getSession().write(MaplePacketCreator.showMesoGain(gain, inChat));
		}
	}

	public void controlMonster(MapleMonster monster, boolean aggro) {
		monster.setController(this);
		controlled.add(monster);
		client.getSession().write(MobPacket.controlMonster(monster, false, aggro));
	}

	public void stopControllingMonster(MapleMonster monster) {
		controlled.remove(monster);
	}

	public void checkMonsterAggro(MapleMonster monster) {
		if (monster.getController() == this) {
			monster.setControllerHasAggro(true);
		} else {
			monster.switchController(this, true);
		}
	}

	public Collection<MapleMonster> getControlledMonsters() {
		return Collections.unmodifiableCollection(controlled);
	}

	public int getAccountID() {
		return accountid;
	}

	public void mobKilled(final int id) {
		try {
			for (MapleQuestStatus q : quests.values()) {
				if (q.getStatus() != 1 || !q.hasMobKills()) {
					continue;
				}
				if (q.mobKilled(id)) {
					client.getSession().write(MaplePacketCreator.updateQuestMobKills(q));
					if (q.getQuest().canComplete(this, null)) {
						client.getSession().write(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
					}
				}
			}
		} catch (NullPointerException e) {
		}
	}

	public final List<MapleQuestStatus> getStartedQuests() {
		List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
		for (MapleQuestStatus q : quests.values()) {
			if (q.getStatus() == 1 && !q.isCustomQuest()) {
				ret.add(q);
			}
		}
		return Collections.unmodifiableList(ret);
	}

	public final List<MapleQuestStatus> getCompletedQuests() {
		List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
		for (MapleQuestStatus q : quests.values()) {
			if (q.getStatus() == 2 && !q.isCustomQuest()) {
				ret.add(q);
			}
		}
		return Collections.unmodifiableList(ret);
	}

	public Map<ISkill, SkillEntry> getSkills() {
		return Collections.unmodifiableMap(skills);
	}

	public byte getSkillLevel(final ISkill skill) {
		final SkillEntry ret = skills.get(skill);
		if (ret == null) {
			return 0;
		}
		return ret.skillevel;
	}

	public byte getMasterLevel(final ISkill skill) {
		final SkillEntry ret = skills.get(skill);
		if (ret == null) {
			return 0;
		}
		return ret.masterlevel;
	}

	public void levelUp() {
		if (GameConstants.isKOC(job)) {
			if (level <= 70) {
				remainingAp += 6;
			} else {
				remainingAp += 5;
			}
		} else {
			remainingAp += 5;
		}
		int maxhp = stats.getMaxHp();
		int maxmp = stats.getMaxMp();
		if (job == 0 || job == 1000 || job == 2000) { // Beginner
			maxhp += Randomizer.rand(12, 16);
			maxmp += Randomizer.rand(10, 12);
		} else if (job >= 100 && job <= 132) { // Warrior
			final ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
			final int slevel = getSkillLevel(improvingMaxHP);
			if (slevel > 0) {
				maxhp += improvingMaxHP.getEffect(slevel).getX();
			}
			maxhp += Randomizer.rand(24, 28);
			maxmp += Randomizer.rand(4, 6);
		} else if (job >= 200 && job <= 232) { // Magician
			final ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
			final int slevel = getSkillLevel(improvingMaxMP);
			if (slevel > 0) {
				maxmp += improvingMaxMP.getEffect(slevel).getX() * 2;
			}
			maxhp += Randomizer.rand(10, 14);
			maxmp += Randomizer.rand(22, 24);
		} else if ((job >= 300 && job <= 322) || (job >= 400 && job <= 434) || (job >= 1300 && job <= 1311) || (job >= 1400 && job <= 1411)) { // Bowman, Thief, Wind Breaker and Night Walker
			maxhp += Randomizer.rand(20, 24);
			maxmp += Randomizer.rand(14, 16);
		} else if (job >= 500 && job <= 522) { // Pirate
			final ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
			final int slevel = getSkillLevel(improvingMaxHP);
			if (slevel > 0) {
				maxhp += improvingMaxHP.getEffect(slevel).getX();
			}
			maxhp += Randomizer.rand(22, 26);
			maxmp += Randomizer.rand(18, 22);
		} else if (job >= 1100 && job <= 1111) { // Soul Master
			final ISkill improvingMaxHP = SkillFactory.getSkill(11000000);
			final int slevel = getSkillLevel(improvingMaxHP);
			if (slevel > 0) {
				maxhp += improvingMaxHP.getEffect(slevel).getX();
			}
			maxhp += Randomizer.rand(24, 28);
			maxmp += Randomizer.rand(4, 6);
		} else if (job >= 1200 && job <= 1211) { // Flame Wizard
			final ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
			final int slevel = getSkillLevel(improvingMaxMP);
			if (slevel > 0) {
				maxmp += improvingMaxMP.getEffect(slevel).getX() * 2;
			}
			maxhp += Randomizer.rand(10, 14);
			maxmp += Randomizer.rand(22, 24);
		} else if (job >= 2200 && job <= 2218) { // Evan
			maxhp += Randomizer.rand(12, 16);
			maxmp += Randomizer.rand(50, 52);
		} else if (job >= 1500 && job <= 1512) { // Pirate
			final ISkill improvingMaxHP = SkillFactory.getSkill(15100000);
			final int slevel = getSkillLevel(improvingMaxHP);
			if (slevel > 0) {
				maxhp += improvingMaxHP.getEffect(slevel).getX();
			}
			maxhp += Randomizer.rand(22, 26);
			maxmp += Randomizer.rand(18, 22);
		} else if (job >= 2100 && job <= 2112) { // Aran
			maxhp += Randomizer.rand(50, 52);
			maxmp += Randomizer.rand(4, 6);
		} else { // GameMaster
			maxhp += Randomizer.rand(50, 100);
			maxmp += Randomizer.rand(50, 100);
		}
		maxmp += stats.getTotalInt() / 10;
		exp -= GameConstants.getExpNeededForLevel(level);
		level += 1;
		if (level == 200 && !isGM()) {
			try {
				final StringBuilder sb = new StringBuilder("[Congratulation] ");
				final IItem medal = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
				if (medal != null) { // Medal
					sb.append("<");
					sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
					sb.append("> ");
				}
				sb.append(getName());
				sb.append(" has achieved Level 200. Let us Celebrate! Maplers!");
				client.getChannelServer().getWorldInterface().broadcastMessage(MaplePacketCreator.serverNotice(6, sb.toString()).getBytes());
			} catch (RemoteException e) {
				client.getChannelServer().reconnectWorld();
			}
		}
		maxhp = Math.min(30000, maxhp);
		maxmp = Math.min(30000, maxmp);
		final List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(8);
                statup.add(new Pair<MapleStat, Integer>(MapleStat.EXP, exp));
		statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, maxmp));
		statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, maxmp));
		statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, maxhp));
		statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, maxhp));
		if (job != 0 && job != 1000 && job != 2000 && job != 2001) { // Not Beginner, Nobless and Legend
			remainingSp[GameConstants.getSkillBook(job)] += 3;
			client.getSession().write(MaplePacketCreator.updateSp(this, false));
		} else {
			if (level <= 10) {
				stats.setStr(stats.getStr() + remainingAp);
				remainingAp = 0;
				statup.add(new Pair<MapleStat, Integer>(MapleStat.STR, stats.getStr()));
			} else {
                            statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, remainingAp));
                        }
		}
                statup.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, (int) level));
		stats.setMaxHp(maxhp);
		stats.setMaxMp(maxmp);
		stats.setHp(maxhp);
		stats.setMp(maxmp);
		client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, getJob()));
		map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
		stats.recalcLocalStats();
		silentPartyUpdate();
		guildUpdate();
		NPCScriptManager.getInstance().start(getClient(), 9105010); // Vavaan
	}

	public void changeKeybinding(int key, MapleKeyBinding keybinding) {
		if (keybinding.getType() != 0) {
			keylayout.Layout().put(Integer.valueOf(key), keybinding);
		} else {
			keylayout.Layout().remove(Integer.valueOf(key));
		}
	}

	public void sendMacros() {
		for (int i = 0; i < 5; i++) {
			if (skillMacros[i] != null) {
				client.getSession().write(MaplePacketCreator.getMacros(skillMacros));
				break;
			}
		}
	}

	public void updateMacros(int position, SkillMacro updateMacro) {
		skillMacros[position] = updateMacro;
	}

	public final SkillMacro[] getMacros() {
		return skillMacros;
	}

	public void tempban(String reason, Calendar duration, int greason, boolean IPMac) {
		if (IPMac) {
			client.banMacs();
		}

		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
			ps.setString(1, client.getSession().getRemoteAddress().toString().split(":")[0]);
			ps.execute();
			ps.close();

			client.getSession().close();

			ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
			Timestamp TS = new Timestamp(duration.getTimeInMillis());
			ps.setTimestamp(1, TS);
			ps.setString(2, reason);
			ps.setInt(3, greason);
			ps.setInt(4, accountid);
			ps.execute();
			ps.close();
		} catch (SQLException ex) {
			System.err.println("Error while tempbanning" + ex);
		}

	}

	public final boolean ban(String reason, boolean IPMac, boolean autoban) {
		if (lastmonthfameids == null) {
			throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
		}
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
			ps.setInt(1, autoban ? 2 : 1);
			ps.setString(2, reason);
			ps.setInt(3, accountid);
			ps.execute();
			ps.close();

			if (IPMac) {
				client.banMacs();
				ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
				String[] ipSplit = client.getSession().getRemoteAddress().toString().split(":");
				ps.setString(1, ipSplit[0]);
				ps.execute();
				ps.close();
			}
		} catch (SQLException ex) {
			System.err.println("Error while banning" + ex);
			return false;
		}
		return true;
	}

	public static boolean ban(String id, String reason, boolean accountId) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps;
			if (id.matches("/[0-9]{1,3}\\..*")) {
				ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
				ps.setString(1, id);
				ps.execute();
				ps.close();
				return true;
			}
			if (accountId) {
				ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
			} else {
				ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
			}
			boolean ret = false;
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
				psb.setString(1, reason);
				psb.setInt(2, rs.getInt(1));
				psb.execute();
				psb.close();
				ret = true;
			}
			rs.close();
			ps.close();
			return ret;
		} catch (SQLException ex) {
			System.err.println("Error while banning" + ex);
		}
		return false;
	}

	/**
	 * Oid of players is always = the cid
	 */
	@Override
	public int getObjectId() {
		return getId();
	}

	/**
	 * Throws unsupported operation exception, oid of players is read only
	 */
	@Override
	public void setObjectId(int id) {
		throw new UnsupportedOperationException();
	}

	public MapleStorage getStorage() {
		return storage;
	}

	public void addVisibleMapObject(MapleMapObject mo) {
		visibleMapObjects.add(mo);
	}

	public void removeVisibleMapObject(MapleMapObject mo) {
		visibleMapObjects.remove(mo);
	}

	public boolean isMapObjectVisible(MapleMapObject mo) {
		return visibleMapObjects.contains(mo);
	}

	public Collection<MapleMapObject> getVisibleMapObjects() {
		return Collections.unmodifiableCollection(visibleMapObjects);
	}

	public boolean isAlive() {
		return stats.getHp() > 0;
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.getSession().write(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
	}

	@Override
	public void sendSpawnData(MapleClient client) {
		if (!isHidden()) {
			client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));

			for (final MaplePet pet : pets) {
				if (pet.getSummoned()) {
					client.getSession().write(PetPacket.showPet(this, pet, false, false));
				}
			}
			if (dragon != null) {
				client.getSession().write(MaplePacketCreator.spawnDragon(dragon));
			}
		}
	}

	public void setDragon(MapleDragon d) {
		this.dragon = d;
	}

	public final void equipChanged() {
		map.broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
		stats.recalcLocalStats();
		enforceMaxHpMp();
		if (client.getPlayer().getMessenger() != null) {
			WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
			try {
				wci.updateMessenger(client.getPlayer().getMessenger().getId(), client.getPlayer().getName(), client.getChannel());
			} catch (final RemoteException e) {
				client.getChannelServer().reconnectWorld();
			}
		}
	}

	public final MaplePet getPet(final int index) {
		byte count = 0;
		for (final MaplePet pet : pets) {
			if (pet.getSummoned()) {
				if (count == index) {
					return pet;
				}
				count++;
			}
		}
		return null;
	}

	public void addPet(final MaplePet pet) {
		pets.remove(pet);
		pets.add(pet);
		// So that the pet will be at the last
		// Pet index logic :(
	}

	public void removePet(MaplePet pet, boolean shiftLeft) {
		pet.setSummoned(false);
		/*	int slot = -1;
		for (int i = 0; i < 3; i++) {
		if (pets[i] != null) {
		if (pets[i].getUniqueId() == pet.getUniqueId()) {
		pets[i] = null;
		slot = i;
		break;
		}
		}
		}
		if (shiftLeft) {
		if (slot > -1) {
		for (int i = slot; i < 3; i++) {
		if (i != 2) {
		pets[i] = pets[i + 1];
		} else {
		pets[i] = null;
		}
		}
		}
		}*/
	}

	public final byte getPetIndex(final MaplePet petz) {
		byte count = 0;
		for (final MaplePet pet : pets) {
			if (pet.getSummoned()) {
				if (pet == petz) {
					return count;
				}
				count++;
			}
		}
		return -1;
	}

	public final byte getPetIndex(final int petId) {
		byte count = 0;
		for (final MaplePet pet : pets) {
			if (pet.getSummoned()) {
				if (pet.getUniqueId() == petId) {
					return count;
				}
				count++;
			}
		}
		return -1;
	}

	public final List<MaplePet> getPets() {
		return pets;
	}

	public final void unequipAllPets() {
		for (final MaplePet pet : pets) {
			if (pet != null) {
				unequipPet(pet, true, false);
			}
		}
	}

	public void unequipPet(MaplePet pet, boolean shiftLeft, boolean hunger) {
		cancelFullnessSchedule(getPetIndex(pet));
		pet.saveToDb();
		map.broadcastMessage(this, PetPacket.showPet(this, pet, true, hunger), true);
		List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
		stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));
		client.getSession().write(PetPacket.petStatUpdate(this));
		client.getSession().write(MaplePacketCreator.enableActions());
		removePet(pet, shiftLeft);
	}

	/*    public void shiftPetsRight() {
	if (pets[2] == null) {
	pets[2] = pets[1];
	pets[1] = pets[0];
	pets[0] = null;
	}
	}*/
	public final long getLastFameTime() {
		return lastfametime;
	}

	public final List<Integer> getFamedCharacters() {
		return lastmonthfameids;
	}

	public FameStatus canGiveFame(MapleCharacter from) {
		if (lastfametime >= System.currentTimeMillis() - 60 * 60 * 24 * 1000) {
			return FameStatus.NOT_TODAY;
		} else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
			return FameStatus.NOT_THIS_MONTH;
		}
		return FameStatus.OK;
	}

	public void hasGivenFame(MapleCharacter to) {
		lastfametime = System.currentTimeMillis();
		lastmonthfameids.add(Integer.valueOf(to.getId()));
		Connection con = DatabaseConnection.getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
			ps.setInt(1, getId());
			ps.setInt(2, to.getId());
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			System.err.println("ERROR writing famelog for char " + getName() + " to " + to.getName() + e);
		}
	}

	public final MapleKeyLayout getKeyLayout() {
		return this.keylayout;
	}

	public MapleParty getParty() {
		return party;
	}

	public int getPartyId() {
		return (party != null ? party.getId() : -1);
	}

	public void setWorld(int world) {
		this.world = world;
	}

	public void setParty(MapleParty party) {
		this.party = party;
	}

	public MapleTrade getTrade() {
		return trade;
	}

	public void setTrade(MapleTrade trade) {
		this.trade = trade;
	}

	public EventInstanceManager getEventInstance() {
		return eventInstance;
	}

	public void setEventInstance(EventInstanceManager eventInstance) {
		this.eventInstance = eventInstance;
	}

	public void addDoor(MapleDoor door) {
		doors.add(door);
	}

	public void clearDoors() {
		doors.clear();
	}

	public List<MapleDoor> getDoors() {
		return new ArrayList<MapleDoor>(doors);
	}

	public void setSmega() {
		if (smega) {
			smega = false;
			dropMessage(5, "You have set megaphone to disabled mode");
		} else {
			smega = true;
			dropMessage(5, "You have set megaphone to enabled mode");
		}
	}

	public boolean getSmega() {
		return smega;
	}

	public boolean canDoor() {
		return canDoor;
	}

	public void disableDoor() {
		canDoor = false;
		TimerManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				canDoor = true;
			}
		}, 5000);
	}

	public Map<Integer, MapleSummon> getSummons() {
		return summons;
	}

	public int getChair() {
		return chair;
	}

	public int getItemEffect() {
		return itemEffect;
	}

	public void setChair(int chair) {
		this.chair = chair;
		stats.relocHeal();
	}

	public void setItemEffect(int itemEffect) {
		this.itemEffect = itemEffect;
	}

	@Override
	public Collection<MapleInventory> allInventories() {
		return Arrays.asList(inventory);
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.PLAYER;
	}

	public int getGuildId() {
		return guildid;
	}

	public int getGuildRank() {
		return guildrank;
	}

	public void setGuildId(int _id) {
		guildid = _id;
		if (guildid > 0) {
			if (mgc == null) {
				mgc = new MapleGuildCharacter(this);
			} else {
				mgc.setGuildId(guildid);
			}
		} else {
			mgc = null;
		}
	}

	public void setGuildRank(int _rank) {
		guildrank = _rank;
		if (mgc != null) {
			mgc.setGuildRank(_rank);
		}
	}

	public MapleGuildCharacter getMGC() {
		return mgc;
	}

	public void setAllianceRank(int rank) {
		allianceRank = rank;
	}

	public int getAllianceRank() {
		return allianceRank;
	}

	public MapleGuild getGuild() {
		try {
			return client.getChannelServer().getWorldInterface().getGuild(getGuildId(), null);
		} catch (RemoteException e) {
			client.getChannelServer().reconnectWorld();
		}
		return null;
	}

	public void guildUpdate() {
		if (guildid <= 0) {
			return;
		}
		mgc.setLevel((short) level);
		mgc.setJobId(job);

		try {
			client.getChannelServer().getWorldInterface().memberLevelJobUpdate(mgc);
		} catch (RemoteException re) {
			System.err.println("RemoteExcept while trying to update level/job in guild." + re);
		}
	}

	public void setReborns(int reborns) {
		this.reborns = reborns;
	}

	public void saveGuildStatus() {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ? WHERE id = ?");
			ps.setInt(1, guildid);
			ps.setInt(2, guildrank);
			ps.setInt(3, id);
			ps.execute();
			ps.close();
		} catch (SQLException se) {
			System.err.println("SQL error: " + se.getLocalizedMessage() + se);
		}
	}

	public MapleAlliance getAlliance() {
		return alliance;
	}

	public void modifyCSPoints(int type, int quantity, boolean show) {
		if (getNX() < 0) {
			acash = 0;
		}
		if (getNX() > 1000000) {
			acash = 900000;
		}
		if (getNX() + quantity < 900000) {
			switch (type) {
				case 1:
					acash += quantity;
					break;
				case 2:
					maplepoints += quantity;
					break;
				default:
					break;
			}
			if (show) {
				dropMessage(5, "You have gained " + quantity + " cash.");
				client.getSession().write(MaplePacketCreator.showSpecialEffect(19));
			}
		} else {
			dropMessage(5, "You have reached the maximum ammount of @cash");
		}
	}

	public int getCSPoints(int type) {
		switch (type) {
			case 1:
				return acash;
			case 2:
				return maplepoints;
			default:
				return 0;
		}
	}

	public final boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
		int possesed = inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
		if (checkEquipped) {
			possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
		}
		if (greaterOrEquals) {
			return possesed >= quantity;
		} else {
			return possesed == quantity;
		}
	}

	public void setLevel(int level) {
		this.level = (short) level;
	}

	public int getSkillLevel(int skill) {
		SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
		if (ret == null) {
			return 0;
		}
		return ret.skillevel;
	}

	public static enum FameStatus {

		OK, NOT_TODAY, NOT_THIS_MONTH
	}

	public int getBuddyCapacity() {
		return buddylist.getCapacity();
	}

	public void setBuddyCapacity(int capacity) {
		buddylist.setCapacity(capacity);
		client.getSession().write(MaplePacketCreator.updateBuddyCapacity(capacity));
	}

	public MapleMessenger getMessenger() {
		return messenger;
	}

	public void setMessenger(MapleMessenger messenger) {
		this.messenger = messenger;
	}

	public int getMessengerPosition() {
		return messengerposition;
	}

	public void setMessengerPosition(int position) {
		this.messengerposition = position;
	}

	public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
		coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length, timer));
	}

	public void removeCooldown(int skillId) {
		if (coolDowns.containsKey(Integer.valueOf(skillId))) {
			coolDowns.remove(Integer.valueOf(skillId));
		}
	}

	public boolean skillisCooling(int skillId) {
		return coolDowns.containsKey(Integer.valueOf(skillId));
	}

	public void giveCoolDowns(final int skillid, long starttime, long length) {
		int time = (int) ((length + starttime) - System.currentTimeMillis());
		ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, skillid), time);
		addCooldown(skillid, System.currentTimeMillis(), time, timer);
	}

	public void giveCoolDowns(final List<PlayerCoolDownValueHolder> cooldowns) {
		int time;
		if (cooldowns != null) {
			for (PlayerCoolDownValueHolder cooldown : cooldowns) {
				time = (int) ((cooldown.length + cooldown.startTime) - System.currentTimeMillis());
				ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, cooldown.skillId), time);
				addCooldown(cooldown.skillId, System.currentTimeMillis(), time, timer);
			}
		} else {
			try {
				Connection con = DatabaseConnection.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM skills_cooldowns WHERE charid = ?");
				ps.setInt(1, getId());
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() <= 0) {
						continue;
					}
					giveCoolDowns(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
				}
				deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");

			} catch (SQLException e) {
				System.err.println("Error while retriving cooldown from SQL storage");
			}
		}
	}

	public List<PlayerCoolDownValueHolder> getAllCooldowns() {
		List<PlayerCoolDownValueHolder> ret = new ArrayList<PlayerCoolDownValueHolder>();
		for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
			ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
		}
		return ret;
	}

	public final List<PlayerDiseaseValueHolder> getAllDiseases() {
		final List<PlayerDiseaseValueHolder> ret = new ArrayList<PlayerDiseaseValueHolder>(5);

		DiseaseValueHolder vh;
		for (Entry<MapleDisease, DiseaseValueHolder> disease : diseases.entrySet()) {
			vh = disease.getValue();
			ret.add(new PlayerDiseaseValueHolder(disease.getKey(), vh.startTime, vh.length));
		}
		return ret;
	}

	public final boolean hasDisease(final MapleDisease dis) {
		for (final MapleDisease disease : diseases.keySet()) {
			if (disease == dis) {
				return true;
			}
		}
		return false;
	}

	public void giveDebuff(final MapleDisease disease, MobSkill skill) {
		final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));

		if (!hasDisease(disease) && diseases.size() < 2) {
			if (!(disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN)) {
				if (isActiveBuffedValue(2321005)) {
					return;
				}
			}
			TimerManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					dispelDebuff(disease);
				}
			}, skill.getDuration());

			diseases.put(disease, new DiseaseValueHolder(System.currentTimeMillis(), skill.getDuration()));
			client.getSession().write(MaplePacketCreator.giveDebuff(debuff, skill));
			map.broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(id, debuff, skill), false);
		}
	}

	public final void giveSilentDebuff(final List<PlayerDiseaseValueHolder> ld) {
		if (ld != null) {
			for (final PlayerDiseaseValueHolder disease : ld) {

				TimerManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						dispelDebuff(disease.disease);
					}
				}, (disease.length + disease.startTime) - System.currentTimeMillis());

				diseases.put(disease.disease, new DiseaseValueHolder(disease.startTime, disease.length));
			}
		}
	}

	public void dispelDebuff(MapleDisease debuff) {
		if (hasDisease(debuff)) {
			long mask = debuff.getValue();
			client.getSession().write(MaplePacketCreator.cancelDebuff(mask));
			map.broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);

			diseases.remove(debuff);
		}
	}

	public void dispelDebuffs() {
		dispelDebuff(MapleDisease.CURSE);
		dispelDebuff(MapleDisease.DARKNESS);
		dispelDebuff(MapleDisease.POISON);
		dispelDebuff(MapleDisease.SEAL);
		dispelDebuff(MapleDisease.WEAKEN);
	}

	public void cancelAllDebuffs() {
		diseases.clear();
	}

	public void setLevel(final short level) {
		this.level = (short) (level - 1);
	}

	public void sendNote(String to, String msg) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
			ps.setString(1, to);
			ps.setString(2, getName());
			ps.setString(3, msg);
			ps.setLong(4, System.currentTimeMillis());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Unable to send note" + e);
		}
	}

	public void showNote() {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ps.setString(1, getName());
			ResultSet rs = ps.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.first();
			client.getSession().write(MTSCSPacket.showNotes(rs, count));
			ps.close();
		} catch (SQLException e) {
			System.err.println("Unable to show note" + e);
		}
	}

	public void deleteNote(int id) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
			ps.setInt(1, id);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Unable to delete note" + e);
		}
	}

	public void mulung_EnergyModify(boolean inc) {
		if (inc) {
			if (mulung_energy + 100 > 10000) {
				mulung_energy = 10000;
			} else {
				mulung_energy += 100;
			}
		} else {
			mulung_energy = 0;
		}
		client.getSession().write(MaplePacketCreator.MulungEnergy(mulung_energy));
	}

	public void writeMulungEnergy() {
		client.getSession().write(MaplePacketCreator.MulungEnergy(mulung_energy));
	}

	public final short getCombo() {
		return combo;
	}

	public void setCombo(final short combo) {
		this.combo = combo;
	}

	public final long getLastCombo() {
		return lastCombo;
	}

	public void setLastCombo(final long combo) {
		this.lastCombo = combo;
	}

	public final long getKeyDownSkill_Time() {
		return keydown_skill;
	}

	public void setKeyDownSkill_Time(final long keydown_skill) {
		this.keydown_skill = keydown_skill;
	}

	public void checkBerserk() {
		if (BerserkSchedule != null) {
			BerserkSchedule.cancel(false);
			BerserkSchedule = null;
		}

		ISkill BerserkX = SkillFactory.getSkill(1320006);
		final int skilllevel = getSkillLevel(BerserkX);
		if (skilllevel >= 1) {
			MapleStatEffect ampStat = BerserkX.getEffect(skilllevel);

			if (stats.getHp() * 100 / stats.getMaxHp() > ampStat.getX()) {
				Berserk = false;
			} else {
				Berserk = true;
			}

			client.getSession().write(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
			map.broadcastMessage(this, MaplePacketCreator.showBerserk(getId(), skilllevel, Berserk), false);

			BerserkSchedule = TimerManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					checkBerserk();
				}
			}, 10000);
		}
	}

	private void prepareBeholderEffect() {
		if (beholderHealingSchedule != null) {
			beholderHealingSchedule.cancel(false);
		}
		if (beholderBuffSchedule != null) {
			beholderBuffSchedule.cancel(false);
		}
		ISkill bHealing = SkillFactory.getSkill(1320008);
		int bHealingLvl = getSkillLevel(bHealing);
		if (bHealingLvl > 0) {
			final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
			int healInterval = healEffect.getX() * 1000;
			beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {

				@Override
				public void run() {
					addHP(healEffect.getHp());
					client.getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
					map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), 1321007, 5), true);
					map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), 1321007, 2), false);
				}
			}, healInterval, healInterval);
		}
		ISkill bBuff = SkillFactory.getSkill(1320009);
		int bBuffLvl = getSkillLevel(bBuff);
		if (bBuffLvl > 0) {
			final MapleStatEffect buffEffect = bBuff.getEffect(bBuffLvl);
			int buffInterval = buffEffect.getX() * 1000;
			beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {

				@Override
				public void run() {
					buffEffect.applyTo(MapleCharacter.this);
					client.getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
					map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), 1321007, (int) (Math.random() * 3) + 6), true);
					map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), 1321007, 2), false);
				}
			}, buffInterval, buffInterval);
		}
	}

	public void setChalkboard(String text) {
		this.chalktext = text;
		map.broadcastMessage(MTSCSPacket.useChalkboard(getId(), text));
	}

	public String getChalkboard() {
		return chalktext;
	}

	public MapleMount getMount() {
		return mount;
	}

	public int[] getWishlist() {
		return wishlist;
	}

	public void clearWishlist() {
		for (int i = 0; i < 10; i++) {
			wishlist[i] = 0;
		}
	}

	public int getWishlistSize() {
		int ret = 0;
		for (int i = 0; i < 10; i++) {
			if (wishlist[i] > 0) {
				ret++;
			}
		}
		return ret;
	}

	public int[] getRocks() {
		return rocks;
	}

	public int getRockSize() {
		int ret = 0;
		for (int i = 0; i < 10; i++) {
			if (rocks[i] > 0) {
				ret++;
			}
		}
		return ret;
	}

	public void deleteFromRocks(int map) {
		for (int i = 0; i < 10; i++) {
			if (rocks[i] == map) {
				rocks[i] = -1;
				break;
			}
		}
	}

	public void addRockMap() {
		if (getRockSize() >= 10) {
			return;
		}
		rocks[getRockSize()] = getMapId();
	}

	public boolean isRockMap(int id) {
		for (int i = 0; i < 10; i++) {
			if (rocks[i] == id) {
				return true;
			}
		}
		return false;
	}

	public List<LifeMovementFragment> getLastRes() {
		return lastres;
	}

	public void setLastRes(List<LifeMovementFragment> lastres) {
		this.lastres = lastres;
	}

	public void setMonsterBookCover(int bookCover) {
		this.bookCover = bookCover;
	}

	public int getMonsterBookCover() {
		return bookCover;
	}

	public void dropMessage(int type, String message) {
		client.getSession().write(MaplePacketCreator.serverNotice(type, message));
	}

	public IMaplePlayerShop getPlayerShop() {
		return playerShop;
	}

	public void setPlayerShop(IMaplePlayerShop playerShop) {
		this.playerShop = playerShop;
	}

	public int getConversation() {
		return inst.get();
	}

	public void setConversation(int inst) {
		this.inst.set(inst);
	}

	public MapleCarnivalParty getCarnivalParty() {
		return carnivalParty;
	}

	public void setCarnivalParty(MapleCarnivalParty party) {
		carnivalParty = party;
	}

	public void addCP(int ammount) {
		totalCP += ammount;
		availableCP += ammount;
	}

	public void useCP(int ammount) {
		availableCP -= ammount;
	}

	public int getAvailableCP() {
		return availableCP;
	}

	public int getTotalCP() {
		return totalCP;
	}

	public void resetCP() {
		totalCP = 0;
		availableCP = 0;
	}

	public void addCarnivalRequest(MapleCarnivalChallenge request) {
		pendingCarnivalRequests.add(request);
	}

	public final MapleCarnivalChallenge getNextCarnivalRequest() {
		return pendingCarnivalRequests.pollLast();
	}

	public void clearCarnivalRequests() {
		pendingCarnivalRequests = new LinkedList<MapleCarnivalChallenge>();
	}

	public void startMonsterCarnival(final int enemyavailable, final int enemytotal) {
		client.getSession().write(MonsterCarnivalPacket.startMonsterCarnival(this, enemyavailable, enemytotal));
	}

	public void CPUpdate(final boolean party, final int available, final int total, final int team) {
		client.getSession().write(MonsterCarnivalPacket.CPUpdate(party, available, total, team));
	}

	public void playerDiedCPQ(final String name, final int lostCP, final int team) {
		client.getSession().write(MonsterCarnivalPacket.playerDiedMessage(name, lostCP, team));
	}

	public int getSubcategory() {
		if (job >= 430 && job <= 434) {
			return 1; //dont set it
		}
		return subcategory;
	}

	public int getLinkMid() {
		return linkMid;
	}

	public void setLinkMid(int lm) {
		this.linkMid = lm;
	}

	public boolean isOnDMG() {
		return ondmg;
	}

	public boolean isCallGM() {
		return callgm;
	}

	public void setCallGM(boolean b) {
		this.callgm = b;
	}

	public void setOnDMG(boolean b) {
		this.ondmg = b;
	}
}
