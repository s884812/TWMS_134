package server.maps;

public enum SummonMovementType {
    STATIONARY(0),
    FOLLOW(1),
    CIRCLE_FOLLOW(4);

    private final int val;

    private SummonMovementType(int val) {
	this.val = val;
    }

    public int getValue() {
	return val;
    }
}
