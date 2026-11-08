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
}
