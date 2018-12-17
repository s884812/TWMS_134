package handling.world;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map;

import client.MapleMount;
import client.MapleCharacter;
import client.MapleQuestStatus;
import client.ISkill;
import client.SkillEntry;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import server.quest.MapleQuest;

public class CharacterTransfer implements Externalizable {

    public int characterid, accountid, fame, score, str, dex, int_, luk, maxhp, maxmp, hp, mp, exp, hpApUsed, mpApUsed,
	    remainingAp, meso, skinColor, job, hair, face, mapid,
	    initialSpawnPoint, world, rank, rankMove, jobRank, jobRankMove, guildid,
	    buddysize, partyid, messengerid, messengerposition, mBookCover, dojo, ACash, vpoints, MaplePoints,
	    mount_level, mount_itemid, mount_Fatigue, mount_exp, reborns, subcategory;
    public byte channel, dojoRecord, gender, gmLevel, guildrank, alliancerank;
	public boolean ondmg, callgm;
    public long lastfametime, TranferTime;
    public String name, accountname, BlessOfFairy;
    public short level;
    public Object monsterbook, inventorys, skillmacro, keymap, savedlocation, famedcharacters,
	    storage, rocks, wishlist, InfoQuest, remainingSp;
    public final Map<CharacterNameAndId, Boolean> buddies = new LinkedHashMap<CharacterNameAndId, Boolean>();
    public final Map<Integer, Object> Quest = new LinkedHashMap<Integer, Object>(); // Questid instead of MapleQuest, as it's huge. Cant be transporting MapleQuest.java
    public final Map<Integer, Object> Skills = new LinkedHashMap<Integer, Object>(); // Skillid instead of Skill.java, as it's huge. Cant be transporting Skill.java and MapleStatEffect.java
  

    public CharacterTransfer() {
    }

    public CharacterTransfer(final MapleCharacter chr) {
	this.characterid = chr.getId();
	this.accountid = chr.getAccountID();
	this.accountname = chr.getClient().getAccountName();
	this.channel = (byte) chr.getClient().getChannel();
	this.ACash = chr.getCSPoints(1);
        this.vpoints = chr.getVPoints();
        this.vpoints = chr.getVPoints();
	this.MaplePoints = chr.getCSPoints(2);
	this.name = chr.getName();
	this.fame = chr.getFame();
        this.score = chr.getScore();
	this.gender = (byte) chr.getGender();
	this.level = chr.getLevel();
	this.str = chr.getStat().getStr();
	this.dex = chr.getStat().getDex();
	this.int_ = chr.getStat().getInt();
	this.luk = chr.getStat().getLuk();
	this.hp = chr.getStat().getHp();
	this.mp = chr.getStat().getMp();
	this.maxhp = chr.getStat().getMaxHp();
	this.maxmp = chr.getStat().getMaxMp();
	this.exp = chr.getExp();
	this.hpApUsed = chr.getHpApUsed();
	this.mpApUsed = chr.getMpApUsed();
	this.remainingAp = chr.getRemainingAp();
	this.remainingSp = chr.getRemainingSps();
	this.meso = chr.getMeso();
	this.skinColor = chr.getSkinColor();
	this.job = chr.getJob();
	this.hair = chr.getHair();
	this.face = chr.getFace();
	this.mapid = chr.getMapId();
	this.initialSpawnPoint = chr.getInitialSpawnpoint();
	this.world = chr.getWorld();
	this.rank = chr.getRank();
	this.rankMove = chr.getRankMove();
	this.jobRank = (byte) chr.getJobRank();
	this.jobRankMove = chr.getJobRankMove();
	this.guildid = chr.getGuildId();
	this.guildrank = (byte) chr.getGuildRank();
	this.alliancerank = (byte) chr.getAllianceRank();
	this.gmLevel = (byte) chr.getGMLevel();
	this.subcategory = chr.getSubcategory();
	this.ondmg = chr.isOnDMG();
	this.callgm = chr.isCallGM();
        
	for (final BuddylistEntry qs : chr.getBuddylist().getBuddies()) {
	    this.buddies.put(new CharacterNameAndId(qs.getCharacterId(), qs.getName(), qs.getLevel(), qs.getJob()), qs.isVisible());
	}
	this.buddysize = chr.getBuddyCapacity();

	this.partyid = chr.getPartyId();

	if (chr.getMessenger() != null) {
	    this.messengerid = chr.getMessenger().getId();
	    this.messengerposition = chr.getMessengerPosition();
	} else {
	    messengerid = 0;
	    messengerposition = 4;
	}

	this.mBookCover = chr.getMonsterBookCover();
	this.dojo = chr.getDojo();
	this.dojoRecord = (byte) chr.getDojoRecord();
        this.reborns = chr.getReborns();
	this.InfoQuest = chr.getInfoQuest_Map();

	for (final Map.Entry<MapleQuest, MapleQuestStatus> qs : chr.getQuest_Map().entrySet()) {
	    this.Quest.put(qs.getKey().getId(), qs.getValue());
	}

	this.monsterbook = chr.getMonsterBook();
	this.inventorys = chr.getInventorys();

	for (final Map.Entry<ISkill, SkillEntry> qs : chr.getSkills().entrySet()) {
	    this.Skills.put(qs.getKey().getId(), qs.getValue());
	}

	this.BlessOfFairy = chr.getBlessOfFairyOrigin();
	this.skillmacro = chr.getMacros();
	this.keymap = chr.getKeyLayout();
	this.savedlocation = chr.getSavedLocations();
	this.famedcharacters = chr.getFamedCharacters();
	this.lastfametime = chr.getLastFameTime();
	this.storage = chr.getStorage();
	this.rocks = chr.getRocks();
	this.wishlist = chr.getWishlist();

	final MapleMount mount = chr.getMount();
	this.mount_itemid = mount.getItemId();
	this.mount_Fatigue = mount.getFatigue();
	this.mount_level = mount.getLevel();
	this.mount_exp = mount.getExp();
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
	this.characterid = in.readInt();
	this.accountid = in.readInt();
	this.accountname = (String) in.readObject();
	this.channel = in.readByte();
	this.ACash = in.readInt();
        this.vpoints = in.readInt();
	this.MaplePoints = in.readInt();
	this.name = (String) in.readObject();
	this.fame = in.readInt();
	this.gender = in.readByte();
	this.level = in.readShort();
	this.str = in.readInt();
	this.dex = in.readInt();
	this.int_ = in.readInt();
	this.luk = in.readInt();
	this.hp = in.readInt();
	this.mp = in.readInt();
	this.maxhp = in.readInt();
	this.maxmp = in.readInt();
	this.exp = in.readInt();
	this.hpApUsed = in.readInt();
	this.mpApUsed = in.readInt();
	this.remainingAp = in.readInt();
	this.remainingSp = in.readObject();
	this.meso = in.readInt();
	this.skinColor = in.readInt();
	this.job = in.readInt();
	this.hair = in.readInt();
	this.face = in.readInt();
	this.mapid = in.readInt();
	this.initialSpawnPoint = in.readByte();
	this.world = in.readByte();
	this.rank = in.readInt();
	this.rankMove = in.readInt();
	this.jobRank = in.readInt();
	this.jobRankMove = in.readInt();
	this.guildid = in.readInt();
	this.guildrank = in.readByte();
	this.alliancerank = in.readByte();
	this.gmLevel = in.readByte();
        

	this.BlessOfFairy = (String) in.readObject();

	this.skillmacro = in.readObject();
	this.keymap = in.readObject();
	this.savedlocation = in.readObject();
	this.famedcharacters = in.readObject();
	this.lastfametime = in.readLong();
	this.storage = in.readObject();
	this.rocks = in.readObject();
	this.wishlist = in.readObject();
	this.mount_itemid = in.readInt();
	this.mount_Fatigue = in.readInt();
	this.mount_level = in.readInt();
	this.mount_exp = in.readInt();
	this.partyid = in.readInt();
	this.messengerid = in.readInt();
	this.messengerposition = in.readInt();
	this.mBookCover = in.readInt();
	this.dojo = in.readInt();
	this.dojoRecord = in.readByte();
        this.reborns = in.readInt();
	this.monsterbook = in.readObject();
	this.inventorys = in.readObject();
	this.InfoQuest = in.readObject();

	final int skillsize = in.readShort();
	int skillid;
	Object skill; // SkillEntry
	for (int i = 0; i < skillsize; i++) {
	    skillid = in.readInt();
	    skill = in.readObject();
	    this.Skills.put(skillid, skill);
	}

	this.buddysize = in.readShort();
	final short addedbuddysize = in.readShort();
	for (int i = 0; i < addedbuddysize; i++) {
	    buddies.put(new CharacterNameAndId(in.readInt(), in.readUTF(), in.readInt(), in.readInt()), in.readBoolean());
	}

	final int questsize = in.readShort();
	int quest;
	Object queststatus;
	for (int i = 0; i < questsize; i++) {
	    quest = in.readInt();
	    queststatus = in.readObject();
	    this.Quest.put(quest, queststatus);
	}
	this.ondmg = in.readByte() == 1;
	this.callgm = in.readByte() == 1;
	TranferTime = System.currentTimeMillis();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
	out.writeInt(this.characterid);
	out.writeInt(this.accountid);
	out.writeObject(this.accountname);
	out.write(this.channel);
	out.writeInt(this.ACash);
        out.writeInt(this.vpoints);
	out.writeInt(this.MaplePoints);
	out.writeObject(this.name);
	out.writeInt(this.fame);
	out.write(this.gender);
	out.writeShort(this.level);
	out.writeInt(this.str);
	out.writeInt(this.dex);
	out.writeInt(this.int_);
	out.writeInt(this.luk);
	out.writeInt(this.hp);
	out.writeInt(this.mp);
	out.writeInt(this.maxhp);
	out.writeInt(this.maxmp);
	out.writeInt(this.exp);
	out.writeInt(this.hpApUsed);
	out.writeInt(this.mpApUsed);
	out.writeInt(this.remainingAp);
	out.writeObject(this.remainingSp);
	out.writeInt(this.meso);
	out.writeInt(this.skinColor);
	out.writeInt(this.job);
	out.writeInt(this.hair);
	out.writeInt(this.face);
	out.writeInt(this.mapid);
	out.write(this.initialSpawnPoint);
	out.write(this.world);
	out.writeInt(this.rank);
	out.writeInt(this.rankMove);
	out.writeInt(this.jobRank);
	out.writeInt(this.jobRankMove);
	out.writeInt(this.guildid);
	out.write(this.guildrank);
	out.write(this.alliancerank);
	out.write(this.gmLevel);
        
	out.writeObject(this.BlessOfFairy);

	out.writeObject(this.skillmacro);
	out.writeObject(this.keymap);
	out.writeObject(this.savedlocation);
	out.writeObject(this.famedcharacters);
	out.writeLong(this.lastfametime);
	out.writeObject(this.storage);
	out.writeObject(this.rocks);
	out.writeObject(this.wishlist);
	out.writeInt(this.mount_itemid);
	out.writeInt(this.mount_Fatigue);
	out.writeInt(this.mount_level);
	out.writeInt(this.mount_exp);
	out.writeInt(this.partyid);
	out.writeInt(this.messengerid);
	out.writeInt(this.messengerposition);
	out.writeInt(this.mBookCover);
	out.writeInt(this.dojo);
	out.write(this.dojoRecord);
        out.writeInt(this.reborns);
	out.writeObject(this.monsterbook);
	out.writeObject(this.inventorys);
	out.writeObject(this.InfoQuest);

	out.writeShort(this.Skills.size());
	for (final Map.Entry<Integer, Object> qs : this.Skills.entrySet()) {
	    out.writeInt(qs.getKey()); // Questid instead of Skill, as it's huge :(
	    out.writeObject(qs.getValue());
	    // Bless of fairy is transported here too.
	}

	out.writeShort(this.buddysize);
	out.writeShort(this.buddies.size());
	for (final Map.Entry<CharacterNameAndId, Boolean> qs : this.buddies.entrySet()) {

	    out.writeInt(qs.getKey().getId());

	    out.writeUTF(qs.getKey().getName());

            out.writeInt(qs.getKey().getLevel());

            out.writeInt(qs.getKey().getJob());

	    out.writeBoolean(qs.getValue());
	}
	
	out.writeShort(this.Quest.size());
	for (final Map.Entry<Integer, Object> qs : this.Quest.entrySet()) {
	    out.writeInt(qs.getKey()); // Questid instead of MapleQuest, as it's huge :(
	    out.writeObject(qs.getValue());
	}

	out.writeByte(this.ondmg ? 1 : 0);
	out.writeByte(this.callgm ? 1 : 0);
    }
}
