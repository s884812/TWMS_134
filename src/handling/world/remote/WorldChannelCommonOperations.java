package handling.world.remote;

import java.rmi.RemoteException;
import java.util.List;

public interface WorldChannelCommonOperations {

    public boolean isConnected(String charName) throws RemoteException;

    public boolean isCharacterListConnected(List<String> charName) throws RemoteException;

    public void broadcastMessage(byte[] message) throws RemoteException;

    public void broadcastSmega(byte[] message) throws RemoteException;

    public void broadcastGMMessage(byte[] message) throws RemoteException;

    public void whisper(String sender, String target, int channel, String message) throws RemoteException;

    public void shutdown(int time) throws RemoteException;

    public void loggedOn(String name, int characterId, int channel, int[] buddies) throws RemoteException;

    public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException;

    public List<CheaterData> getCheaters() throws RemoteException;

    public void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) throws RemoteException;

    public void messengerInvite(String sender, int messengerid, String target, int fromchannel) throws RemoteException;
}
