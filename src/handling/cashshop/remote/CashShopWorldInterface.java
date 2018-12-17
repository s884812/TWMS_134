package handling.cashshop.remote;

import handling.world.CharacterTransfer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CashShopWorldInterface extends Remote {

    public boolean isAvailable() throws RemoteException;

    public String getIP() throws RemoteException;

    public void ChannelChange_Data(CharacterTransfer transfer, int characterid) throws RemoteException;

    public void shutdown() throws RemoteException;

    public boolean isCharacterInCS(String name) throws RemoteException;
}