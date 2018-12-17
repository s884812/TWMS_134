package server.shops;

import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacket;
import tools.Pair;

public interface IMaplePlayerShop {
    public final static byte HIRED_MERCHANT = 1;
    public final static byte PLAYER_SHOP = 2;
    
    public String getOwnerName();
    public String getDescription();

    public List<Pair<Byte, MapleCharacter>> getVisitors();
    public List<MaplePlayerShopItem> getItems();

    public boolean isOpen();
    public boolean removeItem(int item);
    public boolean isOwner(MapleCharacter chr);

    public byte getShopType();

    public byte getVisitorSlot(MapleCharacter visitor);
    public byte getFreeSlot();
    public int getItemId();
    public int getMeso();
    public int getOwnerId();
    public int getOwnerAccId();

    public void setOpen(boolean open);
    public void setMeso(int meso);
    public void addItem(MaplePlayerShopItem item);
    public void removeFromSlot(int slot);
    public void broadcastToVisitors(MaplePacket packet);
    public void addVisitor(MapleCharacter visitor);
    public void removeVisitor(MapleCharacter visitor);
    public void removeAllVisitors(int error, int type);
    public void buy(MapleClient c, int item, short quantity);
    public void closeShop(boolean saveItems, boolean remove);
}
