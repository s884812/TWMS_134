package server.movement;

import java.awt.Point;
import tools.data.output.LittleEndianWriter;

public class ChangeEquipSpecialAwesome implements LifeMovementFragment {

    private int type, wui;

    public ChangeEquipSpecialAwesome(int type, int wui) {
	this.type = type;
	this.wui = wui;
    }

    @Override
    public void serialize(LittleEndianWriter lew) {
	lew.write(type);
	lew.write(wui);
    }

    @Override
    public Point getPosition() {
	return new Point(0, 0);
    }
}
