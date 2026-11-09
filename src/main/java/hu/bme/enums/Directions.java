package hu.bme.enums;

public enum Directions {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    IDLE;


    /**
     * Ghosts can not do a full 180 degree turnaround
     * When a ghost gets to an intersection, it has to decide which way it will go 
     * (it can not go backwards)
     */ 
    public Directions opposite() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            case RIGHT: return LEFT;
            case LEFT: return RIGHT;

            // idle -> idle
            default: return IDLE;
        }
    }

    /** How the column changes in one step this way: -1 left, +1 right, 0 otherwise. */
    public int dx() {
        return switch (this) {
            case LEFT -> -1;
            case RIGHT -> 1;
            case UP, DOWN, IDLE -> 0;
        };
    }

    /** How the row changes in one step this way: -1 up (rows grow downwards), +1 down, 0 otherwise. */
    public int dy() {
        return switch (this) {
            case UP -> -1;
            case DOWN -> 1;
            case LEFT, RIGHT, IDLE -> 0;
        };
    }
}
