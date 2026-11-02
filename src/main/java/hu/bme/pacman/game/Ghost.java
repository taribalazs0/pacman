package hu.bme.pacman.game;

import hu.bme.enums.Directions;
import hu.bme.enums.GhostPersonality;
import hu.bme.enums.GhostState;

/**
 * One enemy. It already has a tile, a personality, and a state,
 * but it does not walk yet. Chasing, scattering, and fleeing come later.
 */
public class Ghost extends GameObject {
    private GhostState state;
    private GhostPersonality personality;
    private int targetX;
    private int targetY;

    public Ghost(int x, int y, GhostPersonality personality) {
        super(x, y, 1, Directions.IDLE);
        this.state = GhostState.CHASE;
        this.personality = personality;
        this.targetX = x;
        this.targetY = y;
    }

    public void updateState() {
    }

    public GhostState getState() {
        return state;
    }

    public GhostPersonality getPersonality() {
        return personality;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }
}
