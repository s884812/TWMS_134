package handling.login;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import client.MapleClient;
import server.TimerManager;
import tools.packet.LoginPacket;
import tools.Pair;
import tools.FileoutputUtil;
import tools.packet.TestPacket;

public class LoginWorker {

	private static Runnable persister;
	private static final List<Pair<Integer, String>> IPLog = new LinkedList<Pair<Integer, String>>();
	private static long lastUpdate = 0;
	private static final Lock mutex = new ReentrantLock();

	protected LoginWorker() {
		persister = new PersistingTask();
		TimerManager.getInstance().register(persister, 1800000); // 30 min once
	}

	private static class PersistingTask implements Runnable {
		@Override
		public void run() {
			final StringBuilder sb = new StringBuilder();
			mutex.lock();
			try {
				final String time = FileoutputUtil.CurrentReadable_Time();
				for (Pair<Integer, String> logentry : IPLog) {
					sb.append("ACCID : ");
					sb.append(logentry.getLeft());
					sb.append(", IP : ");
					sb.append(logentry.getRight());
					sb.append(", TIME : ");
					sb.append(time);
					sb.append("\n");
				}
				IPLog.clear();
			} finally {
				mutex.unlock();
			}
			FileoutputUtil.log(FileoutputUtil.IP_Log, sb.toString());
		}
	}

	public static void registerClient(final MapleClient c) {
		if (c.finishLogin() == 0) {
			final String tpin = "T13333333337W"; // todo make something cool?
			c.getSession().write(LoginPacket.getAuthSuccessRequest(c, tpin));
			c.setIdleTask(TimerManager.getInstance().schedule(new Runnable() {
				public void run() {
					c.getSession().close();
				}
			}, 10 * 60 * 10000));
		} else {
			c.getSession().write(LoginPacket.getLoginFailed(7));
			return;
		}
		final LoginServer LS = LoginServer.getInstance();
		if (System.currentTimeMillis() - lastUpdate > 300000) { // Update once every 5 minutes
			lastUpdate = System.currentTimeMillis();
			try {
				final Map<Integer, Integer> load = LS.getWorldInterface().getChannelLoad();
				if (load == null) { // In an unfortunate event that client logged in before load
					lastUpdate = 0;
					c.getSession().write(LoginPacket.getLoginFailed(7));
					return;
				}
				final double loadFactor = 1200 / ((double) LS.getUserLimit() / load.size());
				for (Entry<Integer, Integer> entry : load.entrySet()) {
					load.put(entry.getKey(), Math.min(1200, (int) (entry.getValue() * loadFactor)));
				}
				LS.setLoad(load);
			} catch (RemoteException ex) {
				LoginServer.getInstance().reconnectWorld();
			}
		}
		c.getSession().write(LoginPacket.getServerList(18, "MapleTW", LS.getLoad()));
		c.getSession().write(LoginPacket.getEndOfServerList());
		mutex.lock();
		try {
			IPLog.add(new Pair<Integer, String>(c.getAccID(), c.getSession().getRemoteAddress().toString()));
		} finally {
			mutex.unlock();
		}
	}
}