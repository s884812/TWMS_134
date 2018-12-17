package server.movement;

import java.awt.Point;
import tools.data.output.LittleEndianWriter;

public class FlashMovement extends AbstractLifeMovement {

    private Point pixelsPerSecond;

    public FlashMovement(int type, Point position, int duration, int newstate) {
	super(type, position, duration, newstate);
    }

	public Point getPixelsPerSecond() {
		return pixelsPerSecond;
	}

	public void setPixelsPerSecond(Point ye) {
		this.pixelsPerSecond = ye;
	}

    @Override
    public void serialize(LittleEndianWriter lew) {
        lew.write(getType());
	lew.writePos(getPosition());
	lew.writePos(pixelsPerSecond);
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
