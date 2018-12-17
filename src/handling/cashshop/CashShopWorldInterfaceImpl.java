package handling.cashshop;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import handling.cashshop.remote.CashShopWorldInterface;
import handling.world.CharacterTransfer;

public class CashShopWorldInterfaceImpl extends UnicastRemoteObject implements CashShopWorldInterface {

    private static final long serialVersionUID = -3405666366539470037L;
    private CashShopServer cs;

    public CashShopWorldInterfaceImpl() throws RemoteException {
	super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    public CashShopWorldInterfaceImpl(final CashShopServer cs) throws RemoteException {
	super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	this.cs = cs;
    }

    public boolean isAvailable() throws RemoteException {
	return true;
    }

    public final String getIP() throws RemoteException {
	return cs.getIP();
    }

    // World sending data for storing.
    public void ChannelChange_Data(CharacterTransfer transfer, int characterid) throws RemoteException {
	cs.getPlayerStorage().registerPendingPlayer(transfer, characterid);
    }

    public final void shutdown() throws RemoteException {
	cs.shutdown();
    }

    public final boolean isCharacterInCS(String name) throws RemoteException {
	return cs.getPlayerStorage().isCharacterConnected(name);
    }
}