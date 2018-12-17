package handling.channel.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import server.maps.AnimatedMapleMapObject;
import server.movement.*;
import tools.data.input.LittleEndianAccessor;

public class MovementParse {

    public static final List<LifeMovementFragment> parseMovement(final LittleEndianAccessor lea) {
	final List<LifeMovementFragment> res = new ArrayList<LifeMovementFragment>();
	final byte numCommands = lea.readByte();

	for (byte i = 0; i < numCommands; i++) {
	    final byte command = lea.readByte();
	    switch (command) {
		case -1: // Bounce movement?
		{
		    final short xpos = lea.readShort();
		    final short ypos = lea.readShort();
		    final short unk = lea.readShort();
		    final short fh = lea.readShort();
		    final byte newstate = lea.readByte();
		    final short duration = lea.readShort();
		    final BounceMovement bm = new BounceMovement(command, new Point(xpos, ypos), duration, newstate);
		    bm.setFH(fh);
		    bm.setUnk(unk);
		    res.add(bm);
		    break;
		}
		case 0: // normal move
		case 5:
		case 14:
		case 17: // Float
		{
		    final short xpos = lea.readShort();
		    final short ypos = lea.readShort();
		    final short xwobble = lea.readShort();
		    final short ywobble = lea.readShort();
		    final short unk = lea.readShort();
			final short xoffset = lea.readShort();
			final short yoffset = lea.readShort();
		    final byte newstate = lea.readByte();
		    final short duration = lea.readShort();
		    final AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
		    alm.setUnk(unk);
		    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
		alm.setOffset(new Point(xoffset, yoffset));
		    // log.trace("Move to {},{} command {} wobble {},{} ? {} state {} duration {}", new Object[] { xpos,
		    // xpos, command, xwobble, ywobble, newstate, duration });
		    res.add(alm);
		    break;
		}
		case 1:
		case 2:
		case 13: // Shot-jump-back thing
		case 18:
		case 16: { // Float
		    final short xmod = lea.readShort();
		    final short ymod = lea.readShort();
		    final byte newstate = lea.readByte();
		    final short duration = lea.readShort();
		    final RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xmod, ymod), duration, newstate);
		    res.add(rlm);
		    // log.trace("Relative move {},{} state {}, duration {}", new Object[] { xmod, ymod, newstate,
		    // duration });
		    break;
		}
		case 3:
		case 4: // tele... -.-
		case 6: // assaulter
		case 8: // assassinate
		case 11: // rush ?
		{
		    final short xpos = lea.readShort();
		    final short ypos = lea.readShort();
		    final short xwobble = lea.readShort();
		    final short ywobble = lea.readShort();
		    final byte newstate = lea.readByte();
		    final TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), newstate);
		    tm.setPixelsPerSecond(new Point(xwobble, ywobble));
		    res.add(tm);
		    break;
		}
		case 9: {// change equip ???
		    res.add(new ChangeEquipSpecialAwesome(command, lea.readByte()));
		    break;
		}
		case 7: //same structure
		case 10: // chair ???
		{
		    final short xpos = lea.readShort();
		    final short ypos = lea.readShort();
		    final short unk = lea.readShort();
		    final byte newstate = lea.readByte();
		    final short duration = lea.readShort();
		    final ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
		    cm.setUnk(unk);
		    res.add(cm);
		    break;
		}
		case 23: //?
		case 24: //?
		case 25: //?
		case 26: //?
		case 27: //? <- has no offsets
		case 19:
		case 15:
		case 21: // Aran Combat Step
		case 22: {
		    final byte newstate = lea.readByte();
		    final short unk = lea.readShort();
		    final AranMovement am = new AranMovement(command, new Point(0, 0), unk, newstate);

		    res.add(am);
		    break;
		}
		case 12: { // Jump Down
		    final short xpos = lea.readShort();
		    final short ypos = lea.readShort();
		    final short xwobble = lea.readShort();
		    final short ywobble = lea.readShort();
		    final short unk = lea.readShort();
		    final short fh = lea.readShort();
			final short xoffset = lea.readShort();
			final short yoffset = lea.readShort();
		    final byte newstate = lea.readByte();
		    final short duration = lea.readShort();
		    final JumpDownMovement jdm = new JumpDownMovement(command, new Point(xpos, ypos), duration, newstate);
		    jdm.setUnk(unk);
		    jdm.setPixelsPerSecond(new Point(xwobble, ywobble));
			jdm.setOffset(new Point(xoffset, yoffset));
		    jdm.setFH(fh);
		    
		    res.add(jdm);
		    break;
		}
		case 30: { //?... 00 00 7A 03 0F 02 64 01 00 00 0F 00 04 5A 00
			final short unk = lea.readShort(); //always 0?
			final short xpos = lea.readShort(); //not xpos
			final short ypos = lea.readShort(); //not ypos
			final short xwobble = lea.readShort();
			final short ywobble = lea.readShort();
			final short fh = lea.readShort();
			final byte newstate = lea.readByte();
			final short duration = lea.readShort();
		    final UnknownMovement um = new UnknownMovement(command, new Point(xpos, ypos), duration, newstate);
		    um.setUnk(unk);
		    um.setPixelsPerSecond(new Point(xwobble, ywobble));
		    um.setFH(fh);
		    
		    res.add(um);
			break;
		}
		case 20: { //fj
			final short xpos = lea.readShort();
			final short ypos = lea.readShort();
			final short xwobble = lea.readShort();
			final short ywobble = lea.readShort();
			final byte newstate = lea.readByte();
			final short duration = lea.readShort();
		    final FlashMovement um = new FlashMovement(command, new Point(xpos, ypos), duration, newstate);
		    um.setPixelsPerSecond(new Point(xwobble, ywobble));
		    res.add(um);
			break;
		}
		default:
//		    System.out.println("Remaining : "+(numCommands - res.size())+" New type of movement ID : "+command+", packet : " + lea.toString());
		    return null;
	    }
	}
	if (numCommands != res.size()) {
//	    System.out.println("error in movement");
	    return null; // Probably hack
	}
	return res;
    }

    public static final void updatePosition(final List<LifeMovementFragment> movement, final AnimatedMapleMapObject target, final int yoffset) {
	for (final LifeMovementFragment move : movement) {
	    if (move instanceof LifeMovement) {
		if (move instanceof AbsoluteLifeMovement) {
		    Point position = ((LifeMovement) move).getPosition();
		    position.y += yoffset;
		    target.setPosition(position);
		}
		target.setStance(((LifeMovement) move).getNewstate());
	    }
	}
    }
}