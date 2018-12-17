package server.quest;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

import client.ISkill;
import client.GameConstants;
import client.InventoryException;
import client.MapleCharacter;
import client.MapleInventoryType;
import client.MapleQuestStatus;
import client.MapleStat;
import client.SkillFactory;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import tools.MaplePacketCreator;

public class MapleQuestAction implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private MapleQuestActionType type;
    private MapleData data;
    private MapleQuest quest;

    /** Creates a new instance of MapleQuestAction */
    public MapleQuestAction(MapleQuestActionType type, MapleData data, MapleQuest quest) {
	this.type = type;
	this.data = data;
	this.quest = quest;
    }

    private static boolean canGetItem(MapleData item, MapleCharacter c) {
	if (item.getChildByPath("gender") != null) {
	    final int gender = MapleDataTool.getInt(item.getChildByPath("gender"));
	    if (gender != 2 && gender != c.getGender()) {
		return false;
	    }
	}
	if (item.getChildByPath("job") != null) {
	    final int job = MapleDataTool.getInt(item.getChildByPath("job"));
	    if (job < 100) {
		final int codec = getJobBy5ByteEncoding(job);
		if (codec / 100 != c.getJob() / 100) {
		    return false;
		}
	    } else if (job > 3000) {
		final int playerjob = c.getJob();
		final int codec = getJobByEncoding(job, playerjob);
		if (codec >= 1000) {
		    if (codec / 1000 != c.getJob() / 1000) {
			return false;
		    }
		} else {
		    if (codec / 100 != c.getJob() / 100) {
			return false;
		    }
		}
	    } else {
		if (job != c.getJob()) {
		    return false;
		}
	    }
	}
	return true;
    }

    public final boolean RestoreLostItem(final MapleCharacter c, final int itemid) {
	if (type == MapleQuestActionType.item) {
	    int retitem;

	    for (final MapleData iEntry : data.getChildren()) {
		retitem = MapleDataTool.getInt(iEntry.getChildByPath("id"), -1);
		if (retitem == itemid) {
		    if (!c.haveItem(retitem, 1, true, false)) {
			MapleInventoryManipulator.addById(c.getClient(), retitem, (short) 1);
		    }
		    return true;
		}
	    }
	}
	return false;
    }

    public void runStart(MapleCharacter c, Integer extSelection) {
	MapleQuestStatus status;
	switch (type) {
	    case exp:
		status = c.getQuest(quest);
		if (status.getForfeited() > 0) {
		    break;
		}
		c.gainExp((MapleDataTool.getInt(data, 0) * (c.getLevel() <= 10 ? 1 : c.getClient().getChannelServer().getExpRate())), true, true, true);
		break;
	    case item:
		// first check for randomness in item selection
		Map<Integer, Integer> props = new HashMap<Integer, Integer>();
		MapleData prop;
		for (MapleData iEntry : data.getChildren()) {
		    prop = iEntry.getChildByPath("prop");
		    if (prop != null && MapleDataTool.getInt(prop) != -1 && canGetItem(iEntry, c)) {
			for (int i = 0; i < MapleDataTool.getInt(iEntry.getChildByPath("prop")); i++) {
			    props.put(props.size(), MapleDataTool.getInt(iEntry.getChildByPath("id")));
			}
		    }
		}
		int selection = 0;
		int extNum = 0;
		if (props.size() > 0) {
		    selection = props.get(Randomizer.nextInt(props.size()));
		}
		for (MapleData iEntry : data.getChildren()) {
		    if (!canGetItem(iEntry, c)) {
			continue;
		    }
		    final int id = MapleDataTool.getInt(iEntry.getChildByPath("id"), -1);
		    if (iEntry.getChildByPath("prop") != null) {
			if (MapleDataTool.getInt(iEntry.getChildByPath("prop")) == -1) {
			    if (extSelection != extNum++) {
				continue;
			    }
			} else if (id != selection) {
			    continue;
			}
		    }
		    final short count = (short) MapleDataTool.getInt(iEntry.getChildByPath("count"), 1);
		    if (count < 0) { // remove items
			try {
			    MapleInventoryManipulator.removeById(c.getClient(), GameConstants.getInventoryType(id), id, (count * -1), true, false);
			} catch (InventoryException ie) {
			    // it's better to catch this here so we'll atleast try to remove the other items
			    System.err.println("[h4x] Completing a quest without meeting the requirements" + ie);
			}
			c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
		    } else { // add items
//			final int period = MapleDataTool.getInt(iEntry.getChildByPath("period"), 0);
			MapleInventoryManipulator.addById(c.getClient(), id, count/*, "", -1, 0*/);
			c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
		    }
		}
		break;
//			case NEXTQUEST:
//				int nextquest = MapleDataTool.getInt(data);
//				Need to somehow make the chat popup for the next quest...
//				break;
	    case money:
		status = c.getQuest(quest);
		if (status.getForfeited() > 0) {
		    break;
		}
		c.gainMeso(MapleDataTool.getInt(data, 0), true, false, true);
		break;
	    case QUEST:
		for (MapleData qEntry : data) {
		    c.updateQuest(
			    new MapleQuestStatus(MapleQuest.getInstance(MapleDataTool.getInt(qEntry.getChildByPath("id"))),
			    (byte) MapleDataTool.getInt(qEntry.getChildByPath("state"), 0)));
		}
		break;
	    case skill:
		//TODO needs gain/lost message?
		for (MapleData sEntry : data) {
		    final int skillid = MapleDataTool.getInt(sEntry.getChildByPath("id"));
		    int skillLevel = MapleDataTool.getInt(sEntry.getChildByPath("skillLevel"), 0);
		    int masterLevel = MapleDataTool.getInt(sEntry.getChildByPath("masterLevel"), 0);
		    final ISkill skillObject = SkillFactory.getSkill(skillid);

		    for (MapleData applicableJob : sEntry.getChildByPath("job")) {
			if (skillObject.isBeginnerSkill() || c.getJob() == MapleDataTool.getInt(applicableJob)) {
			    c.changeSkillLevel(skillObject,
				    (byte) Math.max(skillLevel, c.getSkillLevel(skillObject)),
				    (byte) Math.max(masterLevel, c.getMasterLevel(skillObject)));
			    break;
			}
		    }
		}
		break;
	    case pop:
		status = c.getQuest(quest);
		if (status.getForfeited() > 0) {
		    break;
		}
		final int fameGain = MapleDataTool.getInt(data, 0);
		c.addFame(fameGain);
		c.updateSingleStat(MapleStat.FAME, c.getFame());
		c.getClient().getSession().write(MaplePacketCreator.getShowFameGain(fameGain));
		break;
	    case buffItemID:
		status = c.getQuest(quest);
		if (status.getForfeited() > 0) {
		    break;
		}
		final int tobuff = MapleDataTool.getInt(data, -1);
		if (tobuff == -1) {
		    break;
		}
		MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(c);
		break;
	    case infoNumber: {
//		System.out.println("quest : "+MapleDataTool.getInt(data, 0)+"");
//		MapleQuest.getInstance(MapleDataTool.getInt(data, 0)).forceComplete(c, 0);
//		break;
	    }
	    default:
		break;
	}
    }

    public boolean checkEnd(MapleCharacter c, Integer extSelection) {
	switch (type) {
	    case item: {
		// first check for randomness in item selection
		final Map<Integer, Integer> props = new HashMap<Integer, Integer>();

		for (MapleData iEntry : data.getChildren()) {
		    final MapleData prop = iEntry.getChildByPath("prop");
		    if (prop != null && MapleDataTool.getInt(prop) != -1 && canGetItem(iEntry, c)) {
			for (int i = 0; i < MapleDataTool.getInt(iEntry.getChildByPath("prop")); i++) {
			    props.put(props.size(), MapleDataTool.getInt(iEntry.getChildByPath("id")));
			}
		    }
		}
		int selection = 0;
		int extNum = 0;
		if (props.size() > 0) {
		    selection = props.get(Randomizer.nextInt(props.size()));
		}
		byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;

		for (MapleData iEntry : data.getChildren()) {
		    if (!canGetItem(iEntry, c)) {
			continue;
		    }
		    final int id = MapleDataTool.getInt(iEntry.getChildByPath("id"), -1);
		    if (iEntry.getChildByPath("prop") != null) {
			if (MapleDataTool.getInt(iEntry.getChildByPath("prop")) == -1) {
			    if (extSelection != extNum++) {
				continue;
			    }
			} else if (id != selection) {
			    continue;
			}
		    }
		    final short count = (short) MapleDataTool.getInt(iEntry.getChildByPath("count"), 1);
		    if (count < 0) { // remove items
			if (!c.haveItem(id, count, false, true)) {
			    c.dropMessage(1, "You are short of some item to complete quest.");
			    return false;
			}
		    } else { // add items
			switch (GameConstants.getInventoryType(id)) {
			    case EQUIP:
				eq++;
				break;
			    case USE:
				use++;
				break;
			    case SETUP:
				setup++;
				break;
			    case ETC:
				etc++;
				break;
			    case CASH:
				cash++;
				break;
			}
		    }
		}
		if (c.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() <= eq) {
		    c.dropMessage(1, "Plaase make space for your Equip inventory.");
		    return false;
		} else if (c.getInventory(MapleInventoryType.USE).getNumFreeSlot() <= use) {
		    c.dropMessage(1, "Plaase make space for your Use inventory.");
		    return false;
		} else if (c.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() <= setup) {
		    c.dropMessage(1, "Plaase make space for your Setup inventory.");
		    return false;
		} else if (c.getInventory(MapleInventoryType.ETC).getNumFreeSlot() <= etc) {
		    c.dropMessage(1, "Plaase make space for your Etc inventory.");
		    return false;
		} else if (c.getInventory(MapleInventoryType.CASH).getNumFreeSlot() <= cash) {
		    c.dropMessage(1, "Plaase make space for your Cash inventory.");
		    return false;
		}
		return true;
	    }
	    case money: {
		final int meso = MapleDataTool.getInt(data, 0);
		if (c.getMeso() + meso < 0) { // Giving, overflow
		    c.dropMessage(1, "Meso exceed the max amount, 2147483647.");
		    return false;
		} else if (c.getMeso() < meso) {
		    c.dropMessage(1, "Insufficient meso.");
		    return false;
		}
		return true;
	    }
	}
	return true;
    }

    public void runEnd(MapleCharacter c, Integer extSelection) {
	switch (type) {
	    case exp: {
		c.gainExp((MapleDataTool.getInt(data, 0) * (c.getLevel() <= 10 ? 1 : c.getClient().getChannelServer().getExpRate())), true, true, true);
		break;
	    }
	    case item: {
		// first check for randomness in item selection
		Map<Integer, Integer> props = new HashMap<Integer, Integer>();

		for (MapleData iEntry : data.getChildren()) {
		    final MapleData prop = iEntry.getChildByPath("prop");
		    if (prop != null && MapleDataTool.getInt(prop) != -1 && canGetItem(iEntry, c)) {
			for (int i = 0; i < MapleDataTool.getInt(iEntry.getChildByPath("prop")); i++) {
			    props.put(props.size(), MapleDataTool.getInt(iEntry.getChildByPath("id")));
			}
		    }
		}
		int selection = 0;
		int extNum = 0;
		if (props.size() > 0) {
		    selection = props.get(Randomizer.nextInt(props.size()));
		}
		for (MapleData iEntry : data.getChildren()) {
		    if (!canGetItem(iEntry, c)) {
			continue;
		    }
		    final int id = MapleDataTool.getInt(iEntry.getChildByPath("id"), -1);
		    if (iEntry.getChildByPath("prop") != null) {
			if (MapleDataTool.getInt(iEntry.getChildByPath("prop")) == -1) {
			    if (extSelection != extNum++) {
				continue;
			    }
			} else if (id != selection) {
			    continue;
			}
		    }
		    final short count = (short) MapleDataTool.getInt(iEntry.getChildByPath("count"), 1);
		    if (count < 0) { // remove items
			MapleInventoryManipulator.removeById(c.getClient(), GameConstants.getInventoryType(id), id, (count * -1), true, false);
			c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
		    } else { // add items
//			final int period = MapleDataTool.getInt(iEntry.getChildByPath("period"), 0);
			MapleInventoryManipulator.addById(c.getClient(), id, count, ""/*, -1, period * 60 * 1000*/);
			c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
		    }
		}
		break;
	    }
//			case NEXTQUEST:
//				int nextquest = MapleDataTool.getInt(data);
//				Need to somehow make the chat popup for the next quest...
//				break;
	    case money: {
		c.gainMeso(MapleDataTool.getInt(data, 0), true, false, true);
		break;
	    }
	    case QUEST: {
		for (MapleData qEntry : data) {
		    c.updateQuest(
			    new MapleQuestStatus(MapleQuest.getInstance(MapleDataTool.getInt(qEntry.getChildByPath("id"))),
			    (byte) MapleDataTool.getInt(qEntry.getChildByPath("state"), 0)));
		}
		break;
	    }
	    case skill: {
		for (MapleData sEntry : data) {
		    final int skillid = MapleDataTool.getInt(sEntry.getChildByPath("id"));
		    int skillLevel = MapleDataTool.getInt(sEntry.getChildByPath("skillLevel"), 0);
		    int masterLevel = MapleDataTool.getInt(sEntry.getChildByPath("masterLevel"), 0);
		    final ISkill skillObject = SkillFactory.getSkill(skillid);

		    for (MapleData applicableJob : sEntry.getChildByPath("job")) {
			if (skillObject.isBeginnerSkill() || c.getJob() == MapleDataTool.getInt(applicableJob)) {
			    c.changeSkillLevel(skillObject,
				    (byte) Math.max(skillLevel, c.getSkillLevel(skillObject)),
				    (byte) Math.max(masterLevel, c.getMasterLevel(skillObject)));
			    break;
			}
		    }
		}
		break;
	    }
	    case pop: {
		final int fameGain = MapleDataTool.getInt(data, 0);
		c.addFame(fameGain);
		c.updateSingleStat(MapleStat.FAME, c.getFame());
		c.getClient().getSession().write(MaplePacketCreator.getShowFameGain(fameGain));
		break;
	    }
	    case buffItemID: {
		final int tobuff = MapleDataTool.getInt(data, -1);
		if (tobuff == -1) {
		    break;
		}
		MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(c);
		break;
	    }
	    case infoNumber: {
//		System.out.println("quest : "+MapleDataTool.getInt(data, 0)+"");
//		MapleQuest.getInstance(MapleDataTool.getInt(data, 0)).forceComplete(c, 0);
//		break;
	    }
	    default:
		break;
	}
    }

    private static int getJobBy5ByteEncoding(int encoded) {
	switch (encoded) {
	    case 2:
	    case 3:
		return 100;
	    case 4:
		return 200;
	    case 8:
		return 300;
	    case 16:
		return 400;
	    case 32:
	    case 63:
		return 500;
	    default:
		return 0;
	}
    }

    private static int getJobByEncoding(int encoded, int playerjob) {
	switch (encoded) {
	    case 1049601:
		if (GameConstants.isKOC(playerjob)) {
		    return 1000;
		}
		return 0;
	    case 2099202:
		if (GameConstants.isKOC(playerjob)) {
		    return 1100;
		} else if (GameConstants.isAran(playerjob)) {
		    return 2000;
		}
		return 100;
	    case 4100:
		if (GameConstants.isKOC(playerjob)) {
		    return 1200;
		}
		return 200;
	    case 8200:
		if (GameConstants.isKOC(playerjob)) {
		    return 1300;
		}
		return 300;
	    case 16400:
		if (GameConstants.isKOC(playerjob)) {
		    return 1400;
		}
		return 400;
	    case 32800:
		if (GameConstants.isKOC(playerjob)) {
		    return 1500;
		}
		return 500;
	    default:
		return 0;
	}
    }

    public MapleQuestActionType getType() {
	return type;
    }

    @Override
    public String toString() {
	return type + ": " + data;
    }
}
