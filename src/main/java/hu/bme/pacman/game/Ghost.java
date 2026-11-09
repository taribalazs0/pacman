package hu.bme.pacman.game;

import hu.bme.enums.Directions;
import hu.bme.enums.GhostPersonality;
import hu.bme.enums.GhostState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    /** This decides about the possible tiles a ghost can move to
     * This is the base for everything: random movement, FRIGHTENED ghost state, the 4 AI's will choose from this list
     * Only gives only the possible, available tiles. Does not decide the final direction
    */
    public List<Directions> possibleDirections(Labyrinth map) {
        List<Directions> result = new ArrayList<>();
        Directions back = direction.opposite();

        for (Directions d : Directions.values()) {
            if (d == Directions.IDLE || d == back) {
               continue;
            }
            if (canStep(map, d)) {
                result.add(d);
            }

        }

        // Dead end: turning back is the only way out
        if (result.isEmpty() && back != Directions.IDLE && canStep(map, back)) {
            result.add(back);
        }
        return result;
    }

    /** Whether the tile one step away in direction d is walkable. */
    private boolean canStep(Labyrinth map, Directions d) {
        int nextX = x;
        int nextY = y;
        switch (d) {
            case UP:
                nextY -= speed;
                break;
            case DOWN: 
                nextY += speed;
                break;
            case LEFT:
                nextX -= speed;
                break;
            case RIGHT:
                nextX += speed;
                break;
            case IDLE:
                return false; // idle is not a step
        }
        return map.isWalkable(nextX, nextY);
    }

    /**
     * Picks the direction for the next step and turns the ghost that way.
     * For now the pick is random; the AI strategies will replace this later.
     *
     * In a corridor or a corner there is only one option, so the ghost simply follows it.
     * A real choice only happens where three or more ways meet, so ghosts decide at
     * intersections only, as the rules require, without calling isIntersection.
     *
     */
    public void chooseDirection(Labyrinth map, Random random) {
        
        // Our options come from the previous function
        List<Directions> options = possibleDirections(map);
        if (options.isEmpty()) {
            // Walled in: stand still instead of walking into a wall
            direction = Directions.IDLE;
            return;
        }
        // get a random direction
        direction = options.get(random.nextInt(options.size()));
    }


    /**
     * Pac-Man ate a power pellet: the ghost becomes FRIGHTENED.
     * A DEAD ghost is not affected.
     * On the first fright the ghost turns around; this is the only allowed 180-degree turn,
     * and it shows the player that something happened. An already frightened ghost keeps its way,
     * otherwise every new pellet would jerk it back and forth.
     * Moving slower is the engine's job (see GameEngine.step), not a change of speed:
     * speed counts whole tiles per step
     */
    public void frighten() {
        if (state == GhostState.DEAD) {
            return;
        }
        if (state != GhostState.FRIGHTENED) {
            direction = direction.opposite();
        }
        state = GhostState.FRIGHTENED;
    }

    /** Pair of frighten(). The ghost goes back to CHASE */
    public void calmDown() {
        if(state != GhostState.FRIGHTENED) { return; }
        state = GhostState.CHASE;
    }

     /**Back to the start tile (ghosthouse), standing still.*/
    @Override 
    public void resetToStart() {
        super.resetToStart();
        state = GhostState.CHASE;
    }

    /**After a ghost gets eaten in FRIGHTENED state
     * 
     */
    public void eaten() {
        if(state != GhostState.FRIGHTENED) { return; }
        state = GhostState.DEAD;
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
