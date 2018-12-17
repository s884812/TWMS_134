package server;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import tools.FileoutputUtil;

public class TimerManager {

	private static TimerManager instance = new TimerManager();

	public static class getInstance {
		public getInstance() {
			//
		}
	}
	private ScheduledThreadPoolExecutor ses;

	public static TimerManager getInstance() {
		return instance;
	}

	public void start() {
		if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
			return; //starting the same timermanager twice is no - op
		}

		final ThreadFactory thread = new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				final Thread t = new Thread(r);
				t.setName("Timermanager-Worker-" + threadNumber.getAndIncrement());
				return t;
			}
		};

		final ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(3, thread);
		stpe.setKeepAliveTime(10, TimeUnit.MINUTES);
		;
		stpe.allowCoreThreadTimeOut(true);
		stpe.setCorePoolSize(3);
		stpe.setMaximumPoolSize(5);
		stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		ses = stpe;
	}

	public void stop() {
		ses.shutdown();
	}

	public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
		return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), delay, repeatTime, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> register(Runnable r, long repeatTime) {
		return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), 0, repeatTime, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay) {
		return ses.schedule(new LoggingSaveRunnable(r), delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
		return schedule(r, timestamp - System.currentTimeMillis());
	}

	private class LoggingSaveRunnable implements Runnable {

		Runnable r;

		public LoggingSaveRunnable(final Runnable r) {
			this.r = r;
		}

		@Override
		public void run() {
			try {
				r.run();
			} catch (Throwable t) {
				FileoutputUtil.outputFileError(FileoutputUtil.Timer_Log, t);
			}
		}
	}
}