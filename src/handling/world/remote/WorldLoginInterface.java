package handling.world.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;

import handling.world.guild.MapleGuildCharacter;

public interface WorldLoginInterface extends Remote {

    public Properties getDatabaseProperties() throws RemoteException;

    public Properties getWorldProperties() throws RemoteException;

    public Map<Integer, Integer> getChannelLoad() throws RemoteException;

    public boolean isAvailable() throws RemoteException;

    public void deleteGuildCharacter(MapleGuildCharacter mgc) throws RemoteException;
}