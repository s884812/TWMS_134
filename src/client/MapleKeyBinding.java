
package client;

import java.io.Serializable;

public class MapleKeyBinding implements Serializable {

    private static final long serialVersionUID = 91795419538569L;
    private int type, action;

    public MapleKeyBinding(int type, int action) {
        super();
        this.type = type;
        this.action = action;
    }

    public int getType() {
        return type;
    }

    public int getAction() {
        return action;
    }
}
