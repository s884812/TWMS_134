package handling;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public enum RecvPacketOpcode implements WritableIntValueHolder {
	PONG(false),

	LOGIN_PASSWORD(false),
        SELECT_GENDER_REQUEST(false),
	SERVERLIST_REQUEST,
	CHARLIST_REQUEST,
	SERVERSTATUS_REQUEST,
	CHECK_CHAR_NAME,
	CREATE_CHAR,
	DELETE_CHAR,
	ERROR_38,
	CHAR_SELECT,
	AUTH_SECOND_PASSWORD,
	RSA_KEY(false),
	LOGIN_KEY(false),

	PLAYER_LOGGEDIN(false),
	CHANGE_MAP,
	CHANGE_CHANNEL,
	ENTER_CASH_SHOP,
	MOVE_PLAYER,
	CANCEL_CHAIR,
	USE_CHAIR,
	CLOSE_RANGE_ATTACK,
	RANGED_ATTACK,
	MAGIC_ATTACK,
	PASSIVE_ENERGY,
	TAKE_DAMAGE,
	GENERAL_CHAT,
	CLOSE_CHALKBOARD,
	FACE_EXPRESSION,
	USE_ITEMEFFECT,
	WHEEL_OF_FORTUNE,
	MONSTER_BOOK_COVER,
	NPC_TALK,
	NPC_TALK_MORE,
	NPC_SHOP,
	STORAGE,
	USE_HIRED_MERCHANT,
	MERCH_ITEM_STORE,
	DUEY_ACTION,
	ITEM_SORT,
	ITEM_GATHER,
	ITEM_MOVE,
	USE_ITEM,
	CANCEL_ITEM_EFFECT,
	USE_SUMMON_BAG,
	PET_FOOD,
	USE_MOUNT_FOOD,
	USE_SCRIPTED_NPC_ITEM,
	USE_CASH_ITEM,
	USE_CATCH_ITEM,
	USE_SKILL_BOOK,
	USE_RETURN_SCROLL,
	USE_UPGRADE_SCROLL,
	USE_EQUIP_SCROLL,
	USE_POTENTIAL_SCROLL,
	USE_MAGNIFY_GLASS,
	DISTRIBUTE_AP,
	AUTO_ASSIGN_AP,
	HEAL_OVER_TIME,
	DISTRIBUTE_SP,
	SPECIAL_MOVE,
	CANCEL_BUFF,
	SKILL_EFFECT,
	MESO_DROP,
	GIVE_FAME,
	CHAR_INFO_REQUEST,
	SPAWN_PET,
	CANCEL_DEBUFF,
	CHANGE_MAP_SPECIAL,
	USE_INNER_PORTAL,
	TROCK_ADD_MAP,
	QUEST_ACTION,
	SKILL_MACRO,
	REWARD_ITEM,
	ITEM_MAKER,
	USE_TREASUER_CHEST,
	PARTYCHAT,
	WHISPER,
	MESSENGER,
	PLAYER_INTERACTION,
	PARTY_OPERATION,
	DENY_PARTY_REQUEST,
	GUILD_OPERATION,
	DENY_GUILD_REQUEST,
	BUDDYLIST_MODIFY,
	NOTE_ACTION,
	USE_DOOR,
	CHANGE_KEYMAP,
	ENTER_MTS,
	ALLIANCE_OPERATION,
	REQUEST_FAMILY,
	OPEN_FAMILY,
	FAMILY_OPERATION,
	DELETE_JUNIOR,
	DELETE_SENIOR,
	USE_FAMILY,
	FAMILY_PRECEPT,
	FAMILY_SUMMON,
	CYGNUS_SUMMON,
	ARAN_COMBO,
	BBS_OPERATION,
	TRANSFORM_PLAYER,
	MOVE_PET,
	PET_CHAT,
	PET_COMMAND,
	PET_LOOT,
	PET_AUTO_POT,
	MOVE_SUMMON,
	SUMMON_ATTACK,
	DAMAGE_SUMMON,
	MOVE_LIFE,
	AUTO_AGGRO,
	FRIENDLY_DAMAGE,
	MONSTER_BOMB,
	HYPNOTIZE_DMG,
	NPC_ACTION,
	ITEM_PICKUP,
	DAMAGE_REACTOR,
	SNOWBALL,
	LEFT_KNOCK_BACK,
	COCONUT,
	MONSTER_CARNIVAL,
	SHIP_OBJECT,
	CS_UPDATE,
	BUY_CS_ITEM,
	MAPLETV,
	MOVE_DRAGON;

	private int code = -2;

	@Override
	public void setValue(int code) {
		this.code = code;
	}

	@Override
	public final int getValue() {
		return code;
	}

	private boolean CheckState;

	private RecvPacketOpcode() {
		this.CheckState = true;
	}

	private RecvPacketOpcode(final boolean CheckState) {
		this.CheckState = CheckState;
	}

	public final boolean NeedsChecking() {
		return CheckState;
	}

	public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		FileInputStream fileInputStream = new FileInputStream("recvops.ini");
		props.load(fileInputStream);
		fileInputStream.close();
		return props;
	}

	static {
		reloadValues();
	}

	public static final void reloadValues() {
		try {
			ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
		} catch (IOException e) {
			throw new RuntimeException("Failed to load recvops", e);
		}
	}

	@Override
	public boolean isFirst() {
		return false;
	}
}