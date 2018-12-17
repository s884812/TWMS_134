package handling.channel.handler;

import java.rmi.RemoteException;

import client.MapleCharacter;
import client.MapleClient;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.remote.WorldChannelInterface;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PartyHandler {
	public static final void DenyPartyRequest(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		final int action = slea.readByte();
		final int partyid = slea.readInt();
		if (c.getPlayer().getParty() == null) {
			try {
			WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
			MapleParty party = wci.getParty(partyid);
			if (party != null) {
				if (action == 0x1B) { //accept
					if (party.getMembers().size() < 6) {
						wci.updateParty(partyid, PartyOperation.JOIN, new MaplePartyCharacter(c.getPlayer()));
						c.getPlayer().receivePartyMemberHP();
						c.getPlayer().updatePartyMemberHP();
					} else {
						c.getSession().write(MaplePacketCreator.partyStatusMessage(17));
					}
				} else if (action != 0x16) {
					final MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterById(party.getLeader().getId());
					if (cfrom != null) {
						cfrom.getClient().getSession().write(MaplePacketCreator.partyStatusMessage(23, c.getPlayer().getName()));
					}
				}
			} else {
				c.getPlayer().dropMessage(5, "The party you are trying to join does not exist");
			}
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
		} else {
			c.getPlayer().dropMessage(5, "You can't join the party as you are already in one");
		}
	}

	public static final void PartyOperatopn(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		final int operation = slea.readByte();
		final WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
		MapleParty party = c.getPlayer().getParty();
		MaplePartyCharacter partyplayer = new MaplePartyCharacter(c.getPlayer());
		switch (operation) {
			case 1: // create
			if (c.getPlayer().getParty() == null) {
				try {
				party = wci.createParty(partyplayer);
				c.getPlayer().setParty(party);
				} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
				}
				c.getSession().write(MaplePacketCreator.partyCreated());
			} else {
				c.getPlayer().dropMessage(5, "You can't create a party as you are already in one");
			}
			break;
			case 2: // leave
			if (party != null) { //are we in a party? o.O"
				try {
					if (partyplayer.equals(party.getLeader())) { // disband
						wci.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
						if (c.getPlayer().getEventInstance() != null) {
							c.getPlayer().getEventInstance().disbandParty();
						}
					} else {
						wci.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
						if (c.getPlayer().getEventInstance() != null) {
							c.getPlayer().getEventInstance().leftParty(c.getPlayer());
						}
					}
				} catch (RemoteException e) {
					c.getChannelServer().reconnectWorld();
				}
				c.getPlayer().setParty(null);
			}
			break;
			case 3: // accept invitation
			final int partyid = slea.readInt();
			if (c.getPlayer().getParty() == null) {
				try {
					party = wci.getParty(partyid);
					if (party != null) {
						if (party.getMembers().size() < 6) {
							wci.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
							c.getPlayer().receivePartyMemberHP();
							c.getPlayer().updatePartyMemberHP();
						} else {
							c.getSession().write(MaplePacketCreator.partyStatusMessage(17));
						}
					} else {
						c.getPlayer().dropMessage(5, "The party you are trying to join does not exist");
					}
				} catch (RemoteException e) {
					c.getChannelServer().reconnectWorld();
				}
			} else {
				c.getPlayer().dropMessage(5, "You can't join the party as you are already in one");
			}
			break;
			case 4: // invite
			// TODO store pending invitations and check against them
			final MapleCharacter invited = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
			if (invited != null && invited.getWorld() == c.getPlayer().getWorld()) {
				if (invited.getParty() == null) {
					if (party.getMembers().size() < 6) {
						c.getSession().write(MaplePacketCreator.partyStatusMessage(22, invited.getName()));
						invited.getClient().getSession().write(MaplePacketCreator.partyInvite(c.getPlayer()));
					} else {
						c.getSession().write(MaplePacketCreator.partyStatusMessage(16));
					}
				} else {
					c.getSession().write(MaplePacketCreator.partyStatusMessage(17));
				}
			} else {
				c.getSession().write(MaplePacketCreator.partyStatusMessage(19));
			}
			break;
			case 5: // expel
			if (partyplayer.equals(party.getLeader())) {
				final MaplePartyCharacter expelled = party.getMemberById(slea.readInt());
				if (expelled != null) {
					try {
						wci.updateParty(party.getId(), PartyOperation.EXPEL, expelled);
						if (c.getPlayer().getEventInstance() != null) {
						/*if leader wants to boot someone, then the whole party gets expelled
						TODO: Find an easier way to get the character behind a MaplePartyCharacter
						possibly remove just the expellee.*/
							if (expelled.isOnline()) {
								c.getPlayer().getEventInstance().disbandParty();
							}
						}
					} catch (RemoteException e) {
						c.getChannelServer().reconnectWorld();
					}
				}
			}
			break;
			case 6: // change leader
			final MaplePartyCharacter newleader = party.getMemberById(slea.readInt());
			try {
				wci.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newleader);
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
			break;
			default:
			System.out.println("Unhandled Party function." + operation + "");
			break;
		}
	}
}