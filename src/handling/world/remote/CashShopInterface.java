package handling.world.remote;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.CharacterTransfer;
import handling.world.guild.MapleGuildCharacter;

public interface CashShopInterface extends Remote {

    public boolean isAvailable() throws RemoteException;

    public String getIP() throws RemoteException;

    public String getChannelIP(int channel) throws RemoteException;

    public boolean isCharacterListConnected(List<String> charName) throws RemoteException;

    public void ChannelChange_Data(CharacterTransfer Data, int characterid, int toChannel) throws RemoteException;

    public MapleParty getParty(int partyid) throws RemoteException;

    public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) throws RemoteException;

    public void loggedOn(String name, int characterId, int channel, int[] buddies) throws RemoteException;

    public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException;

    public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) throws RemoteException;
}
