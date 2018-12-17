package server.quest;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import client.MapleCharacter;
import client.MapleQuestStatus;
import scripting.NPCScriptManager;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.MaplePacketCreator;

public class MapleQuest implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private static Map<Integer, MapleQuest> quests = new LinkedHashMap<Integer, MapleQuest>();
    protected int id;
    protected List<MapleQuestRequirement> startReqs;
    protected List<MapleQuestRequirement> completeReqs;
    protected List<MapleQuestAction> startActs;
    protected List<MapleQuestAction> completeActs;
    protected Map<Integer, Integer> relevantMobs;
    private boolean autoStart;
    private boolean autoPreComplete;
    private boolean repeatable = false, customend = false;
    private static final MapleDataProvider questData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Quest.wz"));
    private static final MapleData actions = questData.getData("Act.img");
    private static final MapleData requirements = questData.getData("Check.img");
    private static final MapleData info = questData.getData("QuestInfo.img");

    protected MapleQuest() {
	relevantMobs = new LinkedHashMap<Integer, Integer>();
    }

    /** Creates a new instance of MapleQuest */
    private static boolean loadQuest(MapleQuest ret, int id) {
	ret.id = id;
	ret.relevantMobs = new LinkedHashMap<Integer, Integer>();
	// read reqs
	final MapleData basedata1 = requirements.getChildByPath(String.valueOf(id));
	final MapleData basedata2 = actions.getChildByPath(String.valueOf(id));

	if (basedata1 == null && basedata2 == null) {
	    return false;
	}
	//-------------------------------------------------
	final MapleData startReqData = basedata1.getChildByPath("0");
	ret.startReqs = new LinkedList<MapleQuestRequirement>();
	if (startReqData != null) {
	    for (MapleData startReq : startReqData.getChildren()) {
		final MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(startReq.getName());
		if (type.equals(MapleQuestRequirementType.interval)) {
		    ret.repeatable = true;
		}
		final MapleQuestRequirement req = new MapleQuestRequirement(ret, type, startReq);
		if (req.getType().equals(MapleQuestRequirementType.mob)) {
		    for (MapleData mob : startReq.getChildren()) {
			ret.relevantMobs.put(
				MapleDataTool.getInt(mob.getChildByPath("id")),
				MapleDataTool.getInt(mob.getChildByPath("count"), 0));
		    }
		}
		ret.startReqs.add(req);
	    }

	}
	//-------------------------------------------------
	final MapleData completeReqData = basedata1.getChildByPath("1");
	if (completeReqData.getChildByPath("endscript") != null) {
	    ret.customend = true;
	}
	ret.completeReqs = new LinkedList<MapleQuestRequirement>();
	if (completeReqData != null) {
	    for (MapleData completeReq : completeReqData.getChildren()) {
		MapleQuestRequirement req = new MapleQuestRequirement(ret, MapleQuestRequirementType.getByWZName(completeReq.getName()), completeReq);
		if (req.getType().equals(MapleQuestRequirementType.mob)) {
		    for (MapleData mob : completeReq.getChildren()) {
			ret.relevantMobs.put(
				MapleDataTool.getInt(mob.getChildByPath("id")),
				MapleDataTool.getInt(mob.getChildByPath("count"), 0));
		    }
		}
		ret.completeReqs.add(req);
	    }
	}
	// read acts
	final MapleData startActData = basedata2.getChildByPath("0");
	ret.startActs = new LinkedList<MapleQuestAction>();
	if (startActData != null) {
	    for (MapleData startAct : startActData.getChildren()) {
		ret.startActs.add(new MapleQuestAction(MapleQuestActionType.getByWZName(startAct.getName()), startAct, ret));
	    }
	}
	final MapleData completeActData = basedata2.getChildByPath("1");
	ret.completeActs = new LinkedList<MapleQuestAction>();

	if (completeActData != null) {
	    for (MapleData completeAct : completeActData.getChildren()) {
		ret.completeActs.add(new MapleQuestAction(MapleQuestActionType.getByWZName(completeAct.getName()), completeAct, ret));
	    }
	}
	final MapleData questInfo = info.getChildByPath(String.valueOf(id));
	ret.autoStart = MapleDataTool.getInt("autoStart", questInfo, 0) == 1;
	ret.autoPreComplete = MapleDataTool.getInt("autoPreComplete", questInfo, 0) == 1;

	return true;
    }

    public static MapleQuest getInstance(int id) {
	MapleQuest ret = quests.get(id);
	if (ret == null) {
	    ret = new MapleQuest();
	    if (!loadQuest(ret, id)) {
		ret = new MapleCustomQuest(id);
	    }
	    quests.put(id, ret);
	}
	return ret;
    }

    public boolean canStart(MapleCharacter c, Integer npcid) {
	if (c.getQuest(this).getStatus() != 0 && !(c.getQuest(this).getStatus() == 2 && repeatable)) {
	    return false;
	}
	for (MapleQuestRequirement r : startReqs) {
	    if (!r.check(c, npcid)) {
		return false;
	    }
	}
	return true;
    }

    public boolean canComplete(MapleCharacter c, Integer npcid) {
	if (c.getQuest(this).getStatus() != 1) {
	    return false;
	}
	for (MapleQuestRequirement r : completeReqs) {
	    if (!r.check(c, npcid)) {
		return false;
	    }
	}
	return true;
    }

    public final void RestoreLostItem(final MapleCharacter c, final int itemid) {
	for (final MapleQuestAction a : startActs) {
	    if (a.RestoreLostItem(c, itemid)) {
		break;
	    }
	}
    }

    public void start(MapleCharacter c, int npc) {
	if ((autoStart || checkNPCOnMap(c, npc)) && canStart(c, npc)) {
	    for (MapleQuestAction a : startActs) {
		a.runStart(c, null);
	    }
	    if (!customend) {
		final MapleQuestStatus oldStatus = c.getQuest(this);
		final MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 1, npc);
		newStatus.setCompletionTime(oldStatus.getCompletionTime());
		newStatus.setForfeited(oldStatus.getForfeited());
		c.updateQuest(newStatus);
	    } else {
		NPCScriptManager.getInstance().endQuest(c.getClient(), npc, getId(), true);
	    }
	}
    }

    public void complete(MapleCharacter c, int npc) {
	complete(c, npc, null);
    }

    public void complete(MapleCharacter c, int npc, Integer selection) {
	if ((autoPreComplete || checkNPCOnMap(c, npc)) && canComplete(c, npc)) {
	    for (MapleQuestAction a : completeActs) {
		if (!a.checkEnd(c, selection)) {
		    return;
		}
	    }
	    for (MapleQuestAction a : completeActs) {
		a.runEnd(c, selection);
	    }
	    // we save forfeits only for logging purposes, they shouldn't matter anymore
	    // completion time is set by the constructor
	    final MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 2, npc);
	    newStatus.setForfeited(c.getQuest(this).getForfeited());
	    c.updateQuest(newStatus);

	    c.getClient().getSession().write(MaplePacketCreator.showSpecialEffect(12)); // Quest completion
	    c.getMap().broadcastMessage(c, MaplePacketCreator.showSpecialEffect(c.getId(), 12), false);
	}
    }

    public void forfeit(MapleCharacter c) {
	if (c.getQuest(this).getStatus() != (byte) 1) {
	    return;
	}
	final MapleQuestStatus oldStatus = c.getQuest(this);
	final MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 0);
	newStatus.setForfeited(oldStatus.getForfeited() + 1);
	newStatus.setCompletionTime(oldStatus.getCompletionTime());
	c.updateQuest(newStatus);
    }

    public void forceStart(MapleCharacter c, int npc, String customData) {
	final MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 1, npc);
	newStatus.setForfeited(c.getQuest(this).getForfeited());
	newStatus.setCustomData(customData);
	c.updateQuest(newStatus);
    }

    public void forceComplete(MapleCharacter c, int npc) {
	final MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 2, npc);
	newStatus.setForfeited(c.getQuest(this).getForfeited());
	c.updateQuest(newStatus);
    }

    public int getId() {
	return id;
    }

    public Map<Integer, Integer> getRelevantMobs() {
	return relevantMobs;
    }

    private boolean checkNPCOnMap(MapleCharacter player, int npcid) {
	return player.getMap().containsNPC(npcid) != -1;
    }
}
