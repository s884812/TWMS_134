package handling;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import client.MapleClient;
import handling.channel.ChannelServer;
import handling.cashshop.handler.*;
import handling.channel.handler.*;
import handling.login.handler.*;
import handling.mina.MaplePacketDecoder;
import handling.world.handler.WorldServerInteractionHandler;
import server.Randomizer;
import tools.MapleAESOFB;
import tools.HexTool;
import tools.packet.LoginPacket;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.Pair;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

public class MapleServerHandler extends IoHandlerAdapter {

	private int channel = -1;
	private ServerType type = null;
	private final List<String> BlockedIP = new ArrayList();
	private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<String, Pair<Long, Byte>>();
	private final static byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};

	public MapleServerHandler(final ServerType type) {
		this.type = type;
	}

	public MapleServerHandler(final ServerType type, final int channel) {
		this.channel = channel;
		this.type = type;
	}

	@Override
	public void messageSent(final IoSession session, final Object message) throws Exception {
		if (ServerConstants.DebugMode == true) {
                    System.out.println("Packet Sent : " + HexTool.toString(((MaplePacket) message).getBytes()) + "\n" + HexTool.toStringFromAscii(((MaplePacket) message).getBytes()) + "\n");
                }
		final Runnable r = ((MaplePacket) message).getOnSend();
		if (r != null) {
			r.run();
		}
		super.messageSent(session, message);
	}

	@Override
	public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
            if (ServerConstants.DebugMode == true) {
		System.out.println(session + " " + cause);
            }
	}

	@Override
	public void sessionOpened(final IoSession session) throws Exception {
		final String address = session.getRemoteAddress().toString().split(":")[0];
		if (BlockedIP.contains(address)) {
			session.close();
			return;
		}
		final Pair<Long, Byte> track = tracker.get(address);
		byte count;
		if (track == null) {
			count = 1;
		} else {
			count = track.right;
			final long difference = System.currentTimeMillis() - track.left;
			if (difference < 2000) { // Less than 2 sec
				count++;
			} else if (difference > 20000) { // Over 20 sec
				count = 1;
			}
			if (count >= 10) {
				BlockedIP.add(address);
				tracker.remove(address);
				session.close();
				return;
			}
		}
		tracker.put(address, new Pair(System.currentTimeMillis(), count));
		if (channel > -1) {
			if (ChannelServer.getInstance(channel).isShutdown()) {
				session.close();
				return;
			}
		}
		final byte ivRecv[] = {70, 114, 122, (byte) Randomizer.nextInt(255)};
		final byte ivSend[] = {82, 48, 120, (byte) Randomizer.nextInt(255)};
		final MapleClient client = new MapleClient(
			new MapleAESOFB(key, ivSend, (short) (0xFFFF - ServerConstants.MAPLE_VERSION)), // Sent Cypher
			new MapleAESOFB(key, ivRecv, ServerConstants.MAPLE_VERSION), // Recv Cypher
			session
                );
		client.setChannel(channel);
		MaplePacketDecoder.DecoderState decoderState = new MaplePacketDecoder.DecoderState();
		session.setAttribute(MaplePacketDecoder.DECODER_STATE_KEY, decoderState);
		session.write(LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ivSend, ivRecv));
		session.setAttribute(MapleClient.CLIENT_KEY, client);
		session.setIdleTime(IdleStatus.READER_IDLE, 30);
		session.setIdleTime(IdleStatus.WRITER_IDLE, 30);
		System.out.println(":: IoSession opened " + address + " ::");
	}

	@Override
	public void sessionClosed(final IoSession session) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
		if (client != null) {
			try {
				client.disconnect(true, type == ServerType.CASHSHOP ? true : false);
			} finally {
				session.close();
				session.removeAttribute(MapleClient.CLIENT_KEY);
			}
		}
		super.sessionClosed(session);
	}

	@Override
	public void messageReceived(final IoSession session, final Object message) throws Exception {
		if (ServerConstants.DebugMode == true) {
                    System.out.println("Packet Received : " + HexTool.toString((byte[]) message) + "\n" + HexTool.toStringFromAscii((byte[]) message) + "\n");
                }
		final SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream((byte[]) message));
		final short header_num = slea.readShort();
		for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
			if (recv.getValue() == header_num) {
				final MapleClient c = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
				if (recv.NeedsChecking()) {
					if (!c.isLoggedIn()) {
						return;
					}
				}
				handlePacket(recv, slea, c, type);
				return;
			}
		}
		//System.out.println(":: Unhandled Packet : " + HexTool.toString((byte[]) message) + " ::\n:: " + HexTool.toStringFromAscii((byte[]) message) + " ::");
	}

	@Override
	public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
		final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
		if (client != null) {
			client.sendPing();
		}
		super.sessionIdle(session, status);
	}

	public static final void handlePacket(final RecvPacketOpcode header, final SeekableLittleEndianAccessor slea, final MapleClient c, final ServerType type) {
		switch (header) {
			case PONG:
				c.pongReceived();
				break;
			case LOGIN_PASSWORD:
				CharLoginHandler.login(slea, c);
				break;
                        case SELECT_GENDER_REQUEST:
                                CharLoginHandler.SelectGender(slea, c);
                                break;
			case SERVERLIST_REQUEST:
				CharLoginHandler.ServerListRequest(c);
				break;
			case CHARLIST_REQUEST:
				CharLoginHandler.CharlistRequest(slea, c);
				break;
			case SERVERSTATUS_REQUEST:
				CharLoginHandler.ServerStatusRequest(c);
				break;
			case CHECK_CHAR_NAME:
				CharLoginHandler.CheckCharName(slea.readMapleAsciiString(), c);
				break;
			case CREATE_CHAR:
				CharLoginHandler.CreateChar(slea, c);
				break;
			case DELETE_CHAR:
				CharLoginHandler.DeleteChar(slea, c);
				break;
			case ERROR_38:
				WorldServerInteractionHandler.Error38(slea, c);
				break;
			case CHAR_SELECT:
				CharLoginHandler.Character_WithoutSecondPassword(slea, c);
				break;
			case AUTH_SECOND_PASSWORD:
				CharLoginHandler.Character_WithSecondPassword(slea, c);
				break;
			case RSA_KEY:
				c.getSession().write(LoginPacket.RSA_KEY());
				break;
			case LOGIN_KEY:
				c.getSession().write(LoginPacket.LOGIN_KEY());
				break;
			case CHANGE_CHANNEL:
				InterServerHandler.ChangeChannel(slea, c, c.getPlayer());
				break;
			case PLAYER_LOGGEDIN:
				final int playerid = slea.readInt();
				if (type == ServerType.CHANNEL) {
					InterServerHandler.Loggedin(playerid, c);
				} else {
					CashShopOperation.EnterCS(playerid, c);
				}
				break;
			case ENTER_CASH_SHOP:
				InterServerHandler.EnterCS(slea, c, c.getPlayer());
				break;
			case ENTER_MTS:
				InterServerHandler.EnterMTS(c);
				break;
			case MOVE_PLAYER:
				PlayerHandler.MovePlayer(slea, c, c.getPlayer());
				break;
			case CHAR_INFO_REQUEST:
				slea.skip(4);
				PlayerHandler.CharInfoRequest(slea.readInt(), c, c.getPlayer());
				break;
			case CLOSE_RANGE_ATTACK:
				PlayerHandler.closeRangeAttack(slea, c, c.getPlayer());
				break;
			case RANGED_ATTACK:
				PlayerHandler.rangedAttack(slea, c, c.getPlayer());
				break;
			case MAGIC_ATTACK:
				PlayerHandler.MagicDamage(slea, c, c.getPlayer());
				break;
			case SPECIAL_MOVE:
				PlayerHandler.SpecialMove(slea, c, c.getPlayer());
				break;
			case PASSIVE_ENERGY:
				break;
			case FACE_EXPRESSION:
				PlayerHandler.ChangeEmotion(slea.readInt(), c.getPlayer());
				break;
			case TAKE_DAMAGE:
				PlayerHandler.TakeDamage(slea, c, c.getPlayer());
				break;
			case HEAL_OVER_TIME:
				PlayerHandler.Heal(slea, c.getPlayer());
				break;
			case CANCEL_BUFF:
				PlayerHandler.CancelBuffHandler(slea.readInt(), c.getPlayer());
				break;
			case CANCEL_ITEM_EFFECT:
				PlayerHandler.CancelItemEffect(slea.readInt(), c.getPlayer());
				break;
			case USE_CHAIR:
				PlayerHandler.UseChair(slea.readInt(), c, c.getPlayer());
				break;
			case CANCEL_CHAIR:
				PlayerHandler.CancelChair(slea.readShort(), c, c.getPlayer());
				break;
			case USE_ITEMEFFECT:
				PlayerHandler.UseItemEffect(slea.readInt(), c, c.getPlayer());
				break;
			case SKILL_EFFECT:
                                PlayerHandler.SkillEffect(slea, c.getPlayer());
				break;
			case MESO_DROP:
				slea.skip(4);
				PlayerHandler.DropMeso(slea.readInt(), c.getPlayer());
				break;
			case WHEEL_OF_FORTUNE:
				PlayerHandler.WheelOfFortuneEffect(slea.readInt(), c.getPlayer());
				break;
			case MONSTER_BOOK_COVER:
				PlayerHandler.ChangeMonsterBookCover(slea.readInt(), c, c.getPlayer());
				break;
			case CHANGE_KEYMAP:
				PlayerHandler.ChangeKeymap(slea, c.getPlayer());
				break;
			case CHANGE_MAP:
				if (type == ServerType.CHANNEL) {
					PlayerHandler.ChangeMap(slea, c, c.getPlayer());
				} else {
					CashShopOperation.LeaveCS(slea, c, c.getPlayer());
				}
				break;
			case CHANGE_MAP_SPECIAL:
				slea.skip(1);
				PlayerHandler.ChangeMapSpecial(slea.readMapleAsciiString(), c, c.getPlayer());
				break;
			case USE_INNER_PORTAL:
				slea.skip(1);
				PlayerHandler.InnerPortal(slea, c, c.getPlayer());
				break;
			case TROCK_ADD_MAP:
				PlayerHandler.TrockAddMap(slea, c, c.getPlayer());
				break;
			case ARAN_COMBO:
				PlayerHandler.AranCombo(c, c.getPlayer());
				break;
			case SKILL_MACRO:
				PlayerHandler.ChangeSkillMacro(slea, c.getPlayer());
				break;
			case GIVE_FAME:
				PlayersHandler.GiveFame(slea, c, c.getPlayer());
				break;
			case TRANSFORM_PLAYER:
				PlayersHandler.TransformPlayer(slea, c, c.getPlayer());
				break;
			case NOTE_ACTION:
				PlayersHandler.Note(slea, c.getPlayer());
				break;
			case USE_DOOR:
				PlayersHandler.UseDoor(slea, c.getPlayer());
				break;
			case DAMAGE_REACTOR:
				PlayersHandler.HitReactor(slea, c);
				break;
			case CLOSE_CHALKBOARD:
				c.getPlayer().setChalkboard(null);
				break;
			case ITEM_MAKER:
				ItemMakerHandler.ItemMaker(slea, c);
				break;
			case ITEM_SORT:
				InventoryHandler.ItemSort(slea, c);
				break;
                        case ITEM_GATHER:
				InventoryHandler.ItemSort(slea, c);
				break;
			case ITEM_MOVE:
				InventoryHandler.ItemMove(slea, c);
				break;
			case ITEM_PICKUP:
				InventoryHandler.Pickup_Player(slea, c, c.getPlayer());
				break;
			case USE_CASH_ITEM:
				InventoryHandler.UseCashItem(slea, c);
				break;
			case USE_ITEM:
				InventoryHandler.UseItem(slea, c, c.getPlayer());
				break;
			case USE_SCRIPTED_NPC_ITEM:
				InventoryHandler.UseScriptedNPCItem(slea, c, c.getPlayer());
				break;
			case USE_RETURN_SCROLL:
				InventoryHandler.UseReturnScroll(slea, c, c.getPlayer());
				break;
			case USE_UPGRADE_SCROLL:
				InventoryHandler.UseUpgradeScroll(slea, c, c.getPlayer());
				break;
			case USE_SUMMON_BAG:
				InventoryHandler.UseSummonBag(slea, c, c.getPlayer());
				break;
			case USE_TREASUER_CHEST:
				InventoryHandler.UseTreasureChest(slea, c, c.getPlayer());
				break;
			case USE_SKILL_BOOK:
				InventoryHandler.UseSkillBook(slea, c, c.getPlayer());
				break;
			case USE_CATCH_ITEM:
				InventoryHandler.UseCatchItem(slea, c, c.getPlayer());
				break;
			case USE_MOUNT_FOOD:
				InventoryHandler.UseMountFood(slea, c, c.getPlayer());
				break;
			case REWARD_ITEM:
				InventoryHandler.UseRewardItem(slea, c, c.getPlayer());
				break;
			case HYPNOTIZE_DMG:
				MobHandler.HypnotizeDmg(slea, c.getPlayer());
				break;
			case MOVE_LIFE:
				MobHandler.MoveMonster(slea, c, c.getPlayer());
				break;
			case AUTO_AGGRO:
				MobHandler.AutoAggro(slea.readInt(), c.getPlayer());
				break;
			case FRIENDLY_DAMAGE:
				MobHandler.FriendlyDamage(slea, c.getPlayer());
				break;
			case MONSTER_BOMB:
				MobHandler.MonsterBomb(slea.readInt(), c.getPlayer());
				break;
			case NPC_SHOP:
				NPCHandler.NPCShop(slea, c, c.getPlayer());
				break;
			case NPC_TALK:
				NPCHandler.NPCTalk(slea, c, c.getPlayer());
				break;
			case NPC_TALK_MORE:
				NPCHandler.NPCMoreTalk(slea, c);
				break;
			case NPC_ACTION:
				NPCHandler.NPCAnimation(slea, c);
				break;
			case QUEST_ACTION:
				NPCHandler.QuestAction(slea, c, c.getPlayer());
				break;
			case STORAGE:
				NPCHandler.Storage(slea, c, c.getPlayer());
				break;
			case GENERAL_CHAT:
				slea.skip(4);
				ChatHandler.GeneralChat(slea.readMapleAsciiString(), slea.readByte(), c, c.getPlayer());
				break;
			case PARTYCHAT:
				ChatHandler.Others(slea, c, c.getPlayer());
				break;
			case WHISPER:
				ChatHandler.Whisper_Find(slea, c);
				break;
			case MESSENGER:
				ChatHandler.Messenger(slea, c);
				break;
			case AUTO_ASSIGN_AP:
				StatsHandling.AutoAssignAP(slea, c, c.getPlayer());
				break;
			case USE_EQUIP_SCROLL:
				InventoryHandler.EnhancementScroll(slea, c);
				break;
			case USE_POTENTIAL_SCROLL:
				InventoryHandler.PotentialScroll(slea, c);
				break;
			case USE_MAGNIFY_GLASS:
				InventoryHandler.MagnifyingGlass(slea, c);
				break;
			case DISTRIBUTE_AP:
				StatsHandling.DistributeAP(slea, c, c.getPlayer());
				break;
			case DISTRIBUTE_SP:
				slea.skip(4);
				StatsHandling.DistributeSP(slea.readInt(), c, c.getPlayer());
				break;
			case PLAYER_INTERACTION:
				PlayerInteractionHandler.PlayerInteraction(slea, c, c.getPlayer());
				break;
			case GUILD_OPERATION:
				GuildHandler.Guild(slea, c);
				break;
			case DENY_GUILD_REQUEST:
				slea.skip(1);
				GuildHandler.DenyGuildRequest(slea.readMapleAsciiString(), c);
				break;
			case ALLIANCE_OPERATION:
				AllianceHandler.AllianceOperatopn(slea, c);
				break;
			case BBS_OPERATION:
				BBSHandler.BBSOperatopn(slea, c);
				break;
			case REQUEST_FAMILY:
				FamilyHandler.RequestFamily(slea);
				break;
			case PARTY_OPERATION:
				PartyHandler.PartyOperatopn(slea, c);
				break;
			case DENY_PARTY_REQUEST:
				PartyHandler.DenyPartyRequest(slea, c);
				break;
			case BUDDYLIST_MODIFY:
				BuddyListHandler.BuddyOperation(slea, c);
				break;
			case CYGNUS_SUMMON:
				UserInterfaceHandler.CygnusSummon_NPCRequest(c);
				break;
			case SHIP_OBJECT:
				UserInterfaceHandler.ShipObjectRequest(slea.readInt(), c);
				break;
			case BUY_CS_ITEM:
				CashShopOperation.BuyCashItem(slea, c, c.getPlayer());
				break;
			case CS_UPDATE:
				CashShopOperation.CSUpdate(c, c.getPlayer());
				break;
			case DAMAGE_SUMMON:
				slea.skip(4);
				SummonHandler.DamageSummon(slea, c.getPlayer());
				break;
			case MOVE_SUMMON:
				SummonHandler.MoveSummon(slea, c.getPlayer());
				break;
			case SUMMON_ATTACK:
				SummonHandler.SummonAttack(slea, c, c.getPlayer());
				break;
			case SPAWN_PET:
				PetHandler.SpawnPet(slea, c, c.getPlayer());
				break;
			case MOVE_PET:
				PetHandler.MovePet(slea, c.getPlayer());
				break;
			case PET_CHAT:
				PetHandler.PetChat(slea.readInt(), slea.readShort(), slea.readMapleAsciiString(), c.getPlayer());
				break;
			case PET_COMMAND:
				PetHandler.PetCommand(slea, c, c.getPlayer());
				break;
			case PET_FOOD:
				PetHandler.PetFood(slea, c, c.getPlayer());
				break;
			case PET_LOOT:
				InventoryHandler.Pickup_Pet(slea, c, c.getPlayer());
				break;
			case PET_AUTO_POT:
				PetHandler.Pet_AutoPotion(slea, c, c.getPlayer());
				break;
			case MONSTER_CARNIVAL:
				MonsterCarnivalHandler.MonsterCarnival(slea, c);
				break;
			case DUEY_ACTION:
				DueyHandler.DueyOperation(slea, c);
				break;
			case USE_HIRED_MERCHANT:
				HiredMerchantHandler.UseHiredMerchant(slea, c);
				break;
			case MERCH_ITEM_STORE:
				HiredMerchantHandler.MerchantItemStore(slea, c);
				break;
			case CANCEL_DEBUFF:
				break;
			case MAPLETV:
				break;
			case MOVE_DRAGON:
				SummonHandler.MoveDragon(slea, c.getPlayer());
				break;
			default:
				System.out.println(":: [" + header.toString() + "] is not being handled ::");
				break;
		}
	}
}