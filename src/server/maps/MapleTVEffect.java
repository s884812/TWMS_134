package server.maps;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import client.MapleClient;
import client.MapleCharacter;
import handling.MaplePacket;
import handling.world.remote.WorldChannelInterface;
import server.TimerManager;
import tools.MaplePacketCreator;

public class MapleTVEffect {

    private static List<String> message = new LinkedList<String>();
    private static MapleCharacter user;
    private static boolean active;
    private static int type;
    private static MapleCharacter partner = null;
    MapleClient c;

    public MapleTVEffect(MapleCharacter User, MapleCharacter Partner, List<String> Msg, int Type) {
        message = Msg;
        user = User;
        type = Type;
        partner = Partner;
        broadCastTV(true);
    }

    public static boolean isActive() {
        return active;
    }

    private static void setActive(boolean set) {
        active = set;
    }

    private static MaplePacket removeTV() {
        return MaplePacketCreator.removeTV();
    }

    public static MaplePacket startTV() {
        return MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner);
    }

    public static void broadCastTV(boolean isActive) {
        setActive(isActive);
        WorldChannelInterface wci = user.getClient().getChannelServer().getWorldInterface();
        try {
            if (isActive) {
                wci.broadcastMessage(MaplePacketCreator.enableTV().getBytes());
                wci.broadcastMessage(startTV().getBytes());

                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        broadCastTV(false);
                    }
                }, getDelayTime(type));

            } else {
                wci.broadcastMessage(removeTV().getBytes());
            }
        } catch (RemoteException e) {
        }
    }

    public static int getDelayTime(int type) {
        switch (type) {
            case 0:
            case 3:
                return 15000;
            case 1:
            case 4:
                return 30000;
            case 2:
            case 5:
                return 60000;
        }
        return 0;
    }
}  