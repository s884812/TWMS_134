package handling.channel;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import client.MapleCharacterUtil;
import client.MapleCharacter;
import handling.MaplePacket;
import handling.world.CharacterTransfer;
import handling.world.remote.CheaterData;
import java.util.Collection;
import server.TimerManager;

public class PlayerStorage {

	private final Lock mutex = new ReentrantLock();
	private final Lock mutex2 = new ReentrantLock();
	private final Map<String, MapleCharacter> nameToChar = new HashMap<String, MapleCharacter>();
	private final Map<Integer, MapleCharacter> idToChar = new HashMap<Integer, MapleCharacter>();
	private final Map<Integer, CharacterTransfer> PendingCharacter = new HashMap<Integer, CharacterTransfer>();

	public PlayerStorage() { // Prune once every 15 minutes
		TimerManager.getInstance().schedule(new PersistingTask(), 900000);
	}

	public final void registerPlayer(final MapleCharacter chr) {
		mutex.lock();
		try {
			nameToChar.put(chr.getName().toLowerCase(), chr);
			idToChar.put(chr.getId(), chr);
		} finally {
			mutex.unlock();
		}
	}

	public final void registerPendingPlayer(final CharacterTransfer chr, final int playerid) {
		mutex2.lock();
		try {
			PendingCharacter.put(playerid, chr);//new Pair(System.currentTimeMillis(), chr));
		} finally {
			mutex2.unlock();
		}
	}

	public final void deregisterPlayer(final MapleCharacter chr) {
		mutex.lock();
		try {
			nameToChar.remove(chr.getName().toLowerCase());
			idToChar.remove(chr.getId());
		} finally {
			mutex.unlock();
		}
	}

	public final void deregisterPendingPlayer(final int charid) {
		mutex2.lock();
		try {
			PendingCharacter.remove(charid);
		} finally {
			mutex2.unlock();
		}
	}

	public final CharacterTransfer getPendingCharacter(final int charid) {
		final CharacterTransfer toreturn = PendingCharacter.get(charid);//.right;
		if (toreturn != null) {
			deregisterPendingPlayer(charid);
		}
		return toreturn;
	}

	public final MapleCharacter getCharacterByName(final String name) {
		return nameToChar.get(name.toLowerCase());
	}

	public final MapleCharacter getCharacterById(final int id) {
		return idToChar.get(id);
	}

	public final int getConnectedClients() {
		return idToChar.size();
	}

	public final List<CheaterData> getCheaters() {
		final List<CheaterData> cheaters = new ArrayList<CheaterData>();

		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
			MapleCharacter chr;
			while (itr.hasNext()) {
			chr = itr.next();

			if (chr.getCheatTracker().getPoints() > 0) {
				cheaters.add(new CheaterData(chr.getCheatTracker().getPoints(), MapleCharacterUtil.makeMapleReadable(chr.getName()) + " (" + chr.getCheatTracker().getPoints() + ") " + chr.getCheatTracker().getSummary()));
			}
			}
		} finally {
			mutex.unlock();
		}
		return cheaters;
	}

	public final void disconnectAll() {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
			MapleCharacter chr;
			while (itr.hasNext()) {
			chr = itr.next();

			if (!chr.isGM()) {
				chr.getClient().disconnect(false, false);
				chr.getClient().getSession().close();
				itr.remove();
			}
			}
		} finally {
			mutex.unlock();
		}
	}

	public final String getOnlinePlayers(final boolean byGM) {
		final StringBuilder sb = new StringBuilder();
		if (byGM) {
			mutex.lock();
			try {
				final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
				while (itr.hasNext()) {
					sb.append(MapleCharacterUtil.makeMapleReadable(itr.next().getWorldName()));
					sb.append(", ");
				}
			} finally {
				mutex.unlock();
			}
		} else {
			mutex.lock();
			try {
				final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
				MapleCharacter chr;
				while (itr.hasNext()) {
					chr = itr.next();
					if (!chr.isGM()) {  
						sb.append(MapleCharacterUtil.makeMapleReadable(chr.getWorldName()));
						sb.append(", ");
					}
				}
			} finally {
				mutex.unlock();
			}
		}
		return sb.toString();
	}

	public final void broadcastPacket(final MaplePacket data) {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
			while (itr.hasNext()) {
			itr.next().getClient().getSession().write(data);
			}
		} finally {
			mutex.unlock();
		}
	}

	public final void broadcastSmegaPacket(final MaplePacket data) {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
			MapleCharacter chr;
			while (itr.hasNext()) {
			chr = itr.next();

			if (chr.getClient().isLoggedIn() && chr.getSmega()) {
				chr.getClient().getSession().write(data);
			}
			}
		} finally {
			mutex.unlock();
		}
	}

	public final void broadcastGMPacket(final MaplePacket data) {
		mutex.lock();
		try {
			final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
			MapleCharacter chr;
			while (itr.hasNext()) {
			chr = itr.next();

			if (chr.getClient().isLoggedIn() && chr.isGM() && chr.isCallGM()) {
				chr.getClient().getSession().write(data);
			}
			}
		} finally {
			mutex.unlock();
		}
	}

	public class PersistingTask implements Runnable {
		@Override
		public void run() {
			mutex2.lock();
			try {
			final long currenttime = System.currentTimeMillis();
			final Iterator<Map.Entry<Integer, CharacterTransfer>> itr = PendingCharacter.entrySet().iterator();

			while (itr.hasNext()) {
				if (currenttime - itr.next().getValue().TranferTime > 40000) { // 40 sec
				itr.remove();
				}
			}
			TimerManager.getInstance().schedule(new PersistingTask(), 900000);
			} finally {
			mutex2.unlock();
			}
		}
	}

	public Collection<MapleCharacter> getAllCharacters() {
		return nameToChar.values();
	}
}