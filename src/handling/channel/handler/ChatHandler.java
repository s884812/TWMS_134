package handling.channel.handler;

import java.rmi.RemoteException;
import java.util.Collection;

import client.MapleClient;
import client.MapleCharacter;
import client.messages.CommandProcessor;
import handling.channel.ChannelServer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.remote.WorldChannelInterface;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChatHandler {

	public static final void GeneralChat(final String text, final byte unk, final MapleClient c, final MapleCharacter chr) {
	if (!CommandProcessor.getInstance().processCommand(c, text)) {
		if (!chr.isGM() && text.length() >= 80) {
				return;
		}
			chr.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), text, c.getPlayer().isGM(), unk), c.getPlayer().getPosition());
		}
	}

	public static final void Others(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
	final int type = slea.readByte();
	final byte numRecipients = slea.readByte();
	int recipients[] = new int[numRecipients];

	for (byte i = 0; i < numRecipients; i++) {
		recipients[i] = slea.readInt();
	}
	final String chattext = slea.readMapleAsciiString();

	try {
		switch (type) {
		case 0:
			c.getChannelServer().getWorldInterface().buddyChat(recipients, chr.getId(), chr.getName(), chattext);
			break;
		case 1:
			c.getChannelServer().getWorldInterface().partyChat(chr.getParty().getId(), chattext, chr.getName());
			break;
		case 2:
			c.getChannelServer().getWorldInterface().guildChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
			break;
		case 3:
			c.getChannelServer().getWorldInterface().allianceChat(chr.getGuildId(), chr.getName(),chr.getId(), chattext);
			break;
		}
	} catch (RemoteException e) {
		c.getChannelServer().reconnectWorld();
	}
	}

	public static final void Messenger(final SeekableLittleEndianAccessor slea, final MapleClient c) {
		String input;
		final WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
		MapleMessenger messenger = c.getPlayer().getMessenger();

		switch (slea.readByte()) {
			case 0x00: // open
				if (messenger == null) {
					int messengerid = slea.readInt();
					if (messengerid == 0) { // create
						try {
							final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
							messenger = wci.createMessenger(messengerplayer);
							c.getPlayer().setMessenger(messenger);
							c.getPlayer().setMessengerPosition(0);
						} catch (RemoteException e) {
							c.getChannelServer().reconnectWorld();
						}
					} else { // join
						try {
							messenger = wci.getMessenger(messengerid);
							final int position = messenger.getLowestPosition();
							final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer(), position);
							if (messenger != null) {
								if (messenger.getMembers().size() < 3) {
									c.getPlayer().setMessenger(messenger);
									c.getPlayer().setMessengerPosition(position);
									wci.joinMessenger(messenger.getId(), messengerplayer, c.getPlayer().getName(), messengerplayer.getChannel());
								}
							}
						} catch (RemoteException e) {
							c.getChannelServer().reconnectWorld();
						}
					}
				}
				break;
			case 0x02: // exit
				if (messenger != null) {
					final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
					try {
						wci.leaveMessenger(messenger.getId(), messengerplayer);
					} catch (RemoteException e) {
						c.getChannelServer().reconnectWorld();
					}
					c.getPlayer().setMessenger(null);
					c.getPlayer().setMessengerPosition(4);
				}
				break;
			case 0x03: // invite
				if (messenger.getMembers().size() < 3) {
					input = slea.readMapleAsciiString();
					final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);

					if (target != null) {
						if (target.getMessenger() == null) {
							target.getClient().getSession().write(MaplePacketCreator.messengerInvite(c.getPlayer().getName(), messenger.getId()));

							if (!target.isGM()) {
								c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 1));
							} else {
				c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 0));
				}
						} else {
							c.getSession().write(MaplePacketCreator.messengerChat(c.getPlayer().getName() + " : " + input + " is already using Maple Messenger"));
						}
					} else {
						try {
							if (wci.isConnected(input)) {
								wci.messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel());
							} else {
								c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 0));
							}
						} catch (RemoteException e) {
							c.getChannelServer().reconnectWorld();
						}
					}
				}
				break;
			case 0x05: // decline
				final String targeted = slea.readMapleAsciiString();
				final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
				if (target != null) { // This channel
					if (target.getMessenger() != null) {
						target.getClient().getSession().write(MaplePacketCreator.messengerNote(c.getPlayer().getName(), 5, 0));
					}
				} else { // Other channel
					try {
						if (!c.getPlayer().isGM()) {
							wci.declineChat(targeted, c.getPlayer().getName());
						}
					} catch (RemoteException e) {
						c.getChannelServer().reconnectWorld();
					}
				}
				break;
			case 0x06: // message
				if (messenger != null) {
					final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
					input = slea.readMapleAsciiString();
					try {
						wci.messengerChat(messenger.getId(), input, messengerplayer.getName());
					} catch (RemoteException e) {
						c.getChannelServer().reconnectWorld();
					}
				}
				break;
		}
	}

	public static final void Whisper_Find(final SeekableLittleEndianAccessor slea, final MapleClient c) {
	final byte mode = slea.readByte();
	slea.readInt();
	switch (mode) {
		case 5: { // Find
		final String recipient = slea.readMapleAsciiString();
		MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
		if (player != null) {
			if (!player.isGM() || c.getPlayer().isGM() && player.isGM()) {
				if (player == null) { // cs? lol
					c.getSession().write(MaplePacketCreator.getFindReplyWithCS(player.getName()));
				} else {
					c.getSession().write(MaplePacketCreator.getFindReplyWithMap(player.getName(), player.getMap().getId()));
				}
			} else {
				c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
			}
		} else { // Not found
			final Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
			for (ChannelServer cserv : cservs) {
			player = cserv.getPlayerStorage().getCharacterByName(recipient);
			if (player != null) {
				if (!player.isGM() || c.getPlayer().isGM() && player.isGM()) {
				c.getSession().write(MaplePacketCreator.getFindReply(player.getName(), (byte) player.getClient().getChannel()));
				} else {
				c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
				}
				return;
			}
			}
					try {
						if (c.getChannelServer().getWorldInterface().isCharacterInCS(recipient)) {
				c.getSession().write(MaplePacketCreator.getFindReplyWithCS(player.getName()));
			} else {
				c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
			}
					} catch (RemoteException e) {
			c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
						c.getChannelServer().reconnectWorld();
					}
		}
		break;
		}
		case 6: { // Whisper
		final String recipient = slea.readMapleAsciiString();
		final String text = slea.readMapleAsciiString();

		MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
		if (player != null) {
			player.getClient().getSession().write(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
			if (player.isGM()) {
			c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
			} else {
			c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
			}
		} else { // Not found
			final Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
			for (ChannelServer cserv : cservs) {
			player = cserv.getPlayerStorage().getCharacterByName(recipient);
			if (player != null) {
				break;
			}
			}
			if (player != null) {
			try {
				ChannelServer.getInstance(c.getChannel()).getWorldInterface().whisper(c.getPlayer().getName(), player.getName(), c.getChannel(), text);
				if (!c.getPlayer().isGM() && player.isGM()) {
				c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
				} else {
				c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
				}
			} catch (RemoteException re) {
				c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
				c.getChannelServer().reconnectWorld();
			}
			} else {
			c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
			}
		}
		break;
		}
	}
	}
}
