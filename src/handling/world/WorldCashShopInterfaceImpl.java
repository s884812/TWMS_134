package handling.world;

import java.util.List;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import handling.channel.remote.ChannelWorldInterface;
import handling.world.guild.MapleGuildCharacter;
import handling.world.remote.CashShopInterface;

public class WorldCashShopInterfaceImpl extends UnicastRemoteObject implements CashShopInterface {

    private static final long serialVersionUID = -4985323089596332908L;

    public WorldCashShopInterfaceImpl() throws RemoteException {
	super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    public boolean isAvailable() throws RemoteException {
	return true;
    }

    public String getIP() throws RemoteException {
	return WorldRegistryImpl.getInstance().getCSIP();
    }

    public final String getChannelIP(int channel) throws RemoteException {
	for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
	    if (channel == i) {
		final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
		try {
		    return cwi.getIP();
		} catch (RemoteException e) {
		    WorldRegistryImpl.getInstance().deregisterChannelServer(i);
		}
	    }
	}
	return null;
    }

    public boolean isCharacterListConnected(List<String> charName) throws RemoteException {
	for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
	    final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
	    try {
		if (cwi.isCharacterListConnected(charName)) {
		    return true;
		}
	    } catch (RemoteException e) {
		WorldRegistryImpl.getInstance().deregisterChannelServer(i);
	    }
	}
	return false;
    }

    // Back to channel
    public void ChannelChange_Data(CharacterTransfer Data, int characterid, int toChannel) throws RemoteException {
	for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
	    if (i == toChannel) {
		final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
		try {
		    cwi.ChannelChange_Data(Data, characterid);
		} catch (RemoteException e) {
		    WorldRegistryImpl.getInstance().deregisterChannelServer(i);
		}
	    }
	}
    }

    //TODO only notify channels where partymembers are?
    public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) throws RemoteException {
	final MapleParty party = WorldRegistryImpl.getInstance().getParty(partyid);
	if (party == null) {
	    throw new IllegalArgumentException("no party with the specified partyid exists");
	}
	switch (operation) {
	    case JOIN:
		party.addMember(target);
		break;
	    case EXPEL:
	    case LEAVE:
		party.removeMember(target);
		break;
	    case DISBAND:
		WorldRegistryImpl.getInstance().disbandParty(partyid);
		break;
	    case SILENT_UPDATE:
	    case LOG_ONOFF:
		party.updateMember(target);
		break;
	    case CHANGE_LEADER:
		party.setLeader(target);
		break;
	    default:
		throw new RuntimeException("Unhandeled updateParty operation " + operation.name());
	}
	for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
	    final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
	    try {
		cwi.updateParty(party, operation, target);
	    } catch (RemoteException e) {
		WorldRegistryImpl.getInstance().deregisterChannelServer(i);
	    }
	}
    }

    public MapleParty getParty(int partyid) throws RemoteException {
	return WorldRegistryImpl.getInstance().getParty(partyid);
    }

    public void loggedOn(String name, int characterId, int channel, int[] buddies) throws RemoteException {
	for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
	    final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
	    try {
		cwi.loggedOn(name, characterId, channel, buddies);
	    } catch (RemoteException e) {
		WorldRegistryImpl.getInstance().deregisterChannelServer(i);
	    }
	}
    }

    public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException {
	for (int i : WorldRegistryImpl.getInstance().getChannelServer()) {
	    final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
	    try {
		cwi.loggedOff(name, characterId, channel, buddies);
	    } catch (RemoteException e) {
		WorldRegistryImpl.getInstance().deregisterChannelServer(i);
	    }
	}
    }

    public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) throws RemoteException {
	WorldRegistryImpl.getInstance().setGuildMemberOnline(mgc, bOnline, channel);
    }
}
