package handling.channel.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import handling.MaplePacket;
import handling.world.CharacterTransfer;
import handling.world.MapleMessenger;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.guild.MapleGuildSummary;
import handling.world.remote.WorldChannelCommonOperations;

public interface ChannelWorldInterface extends Remote, WorldChannelCommonOperations {

    public void setChannelId(int id) throws RemoteException;

    public int getChannelId() throws RemoteException;

    public String getIP() throws RemoteException;

    public boolean isConnected(int characterId) throws RemoteException;

    public int getConnected() throws RemoteException;

    public int getLocation(String name) throws RemoteException;

    public void toggleMegaphoneMuteState() throws RemoteException;

    public boolean hasMerchant(int accountId) throws RemoteException;

    public void updateParty(MapleParty party, PartyOperation operation, MaplePartyCharacter target) throws RemoteException;

    public void partyChat(MapleParty party, String chattext, String namefrom) throws RemoteException;

    public boolean isAvailable() throws RemoteException;

    public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation op, int level, int job) throws RemoteException;

    public BuddyAddResult requestBuddyAdd(String addName, int channelFrom, int cidFrom, String nameFrom, int levelFrom, int jobFrom) throws RemoteException;

    public int[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException;

    public void ChannelChange_Data(CharacterTransfer transfer, int characterid) throws RemoteException;

    public void sendPacket(List<Integer> targetIds, MaplePacket packet, int exception) throws RemoteException;

    public void setGuildAndRank(int cid, int guildid, int rank) throws RemoteException;

    public void setOfflineGuildStatus(int guildid, byte guildrank, int cid) throws RemoteException;

    public void setGuildAndRank(List<Integer> cids, int guildid, int rank, int exception) throws RemoteException;

    public void changeEmblem(int gid, List<Integer> affectedPlayers, MapleGuildSummary mgs) throws RemoteException;

    public void addMessengerPlayer(MapleMessenger messenger, String namefrom, int fromchannel, int position) throws RemoteException;

    public void removeMessengerPlayer(MapleMessenger messenger, int position) throws RemoteException;

    public void messengerChat(MapleMessenger messenger, String chattext, String namefrom) throws RemoteException;

    public void declineChat(String target, String namefrom) throws RemoteException;

    public void updateMessenger(MapleMessenger messenger, String namefrom, int position, int fromchannel) throws RemoteException;
}
