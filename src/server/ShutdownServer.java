package server;

import java.sql.SQLException;

import database.DatabaseConnection;
import handling.channel.ChannelServer;

public class ShutdownServer implements Runnable {
	private int channel;
	public ShutdownServer(int channel) {
		this.channel = channel;
	}

	@Override
	public void run() {
	try {
		ChannelServer.getInstance(channel).shutdown();
	} catch (Throwable t) {
		System.err.println("SHUTDOWN ERROR" + t);
	}

	while (ChannelServer.getInstance(channel).getPlayerStorage().getConnectedClients() > 0) {
		try {
		Thread.sleep(1000);
		} catch (InterruptedException e) {
		System.err.println("ERROR" + e);
		}
	}

	System.out.println("Channel " + channel + ", Deregistering channel");

	try {
		ChannelServer.getWorldRegistry().deregisterChannelServer(channel);
	} catch (Exception e) {
		// we are shutting down
	}

	System.out.println("Channel " + channel + ", Unbinding ports...");

	boolean error = true;
	while (error) {
		try {
		ChannelServer.getInstance(channel).unbind();
		error = false;
		} catch (Exception e) {
		error = true;
		}
	}

	System.out.println("Channel " + channel + ", closing...");

	for (ChannelServer cserv : ChannelServer.getAllInstances()) {
		while (!cserv.hasFinishedShutdown()) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.err.println("ERROR" + e);
		}
		}
	}
	TimerManager.getInstance().stop();
	try {
		DatabaseConnection.closeAll();
	} catch (SQLException e) {
		System.err.println("THROW" + e);
	}
	System.exit(0);
	}
}