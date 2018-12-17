package server.movement;

import java.awt.Point;

public interface LifeMovement extends LifeMovementFragment {

    @Override
    Point getPosition();

    int getNewstate();

    int getDuration();

    int getType();
}
