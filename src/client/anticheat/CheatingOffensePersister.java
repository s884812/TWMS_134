package client.anticheat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import client.messages.CommandProcessor.PersistingTask;
import server.TimerManager;

public class CheatingOffensePersister {
	private final static CheatingOffensePersister instance = new CheatingOffensePersister();
	private final Set<CheatingOffenseEntry> toPersist = new LinkedHashSet<CheatingOffenseEntry>();
	private final Lock mutex = new ReentrantLock();

	public static CheatingOffensePersister getInstance() {
		return instance;
	}

	public void persistEntry(CheatingOffenseEntry coe) {
		mutex.lock();
		try {
			toPersist.remove(coe); //equal/hashCode h4x
			toPersist.add(coe);
		} finally {
			mutex.unlock();
		}
	}
}