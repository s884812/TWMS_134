package server.movement;

import java.awt.Point;

import tools.data.output.LittleEndianWriter;

public class AbsoluteLifeMovement extends AbstractLifeMovement {

    private Point pixelsPerSecond, offset;
    private int unk;

    public AbsoluteLifeMovement(int type, Point position, int duration, int newstate) {
	super(type, position, duration, newstate);
    }

    public Point getPixelsPerSecond() {
	return pixelsPerSecond;
    }

    public void setPixelsPerSecond(Point wobble) {
	this.pixelsPerSecond = wobble;
    }

    public Point getOffset() {
	return offset;
    }

    public void setOffset(Point wobble) {
	this.offset = wobble;
    }

    public int getUnk() {
	return unk;
    }

    public void setUnk(int unk) {
	this.unk = unk;
    }

    @Override
    public void serialize(LittleEndianWriter lew) {
	lew.write(getType());
	lew.writePos(getPosition());
	lew.writePos(pixelsPerSecond);
	lew.writeShort(unk);
	lew.writePos(offset);
	lew.write(getNewstate());
	lew.writeShort(getDuration());
    }
}
