package handling.world.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import handling.cashshop.remote.CashShopWorldInterface;
import handling.channel.remote.ChannelWorldInterface;
import handling.login.remote.LoginWorldInterface;

public interface WorldRegistry extends Remote {

    public CashShopInterface registerCSServer(String authKey, String IP, CashShopWorldInterface cb) throws RemoteException;
    public WorldLoginInterface registerLoginServer(String authKey, LoginWorldInterface cb) throws RemoteException;
    public WorldChannelInterface registerChannelServer(String authKey, ChannelWorldInterface cb) throws RemoteException;

    public void deregisterCSServer() throws RemoteException;
    public void deregisterLoginServer(LoginWorldInterface cb) throws RemoteException;
    public void deregisterChannelServer(int channel) throws RemoteException;

    public String getStatus() throws RemoteException;
}
