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
    /** How this ghost picks its chase target (Strategy pattern); null while its personality has none yet. */
    private GhostStrategy strategy;

    public Ghost(int x, int y, GhostPersonality personality) {
        super(x, y, 1, Directions.IDLE);
        this.state = GhostState.CHASE;
        this.personality = personality;
        this.targetX = x;
        this.targetY = y;
        this.strategy = strategyFor(personality);
    }

    /**
     * The personality decides which strategy object the ghost gets.
     */
    private static GhostStrategy strategyFor(GhostPersonality personality) {
        return switch (personality) {
            case AMBUSHER -> new AmbusherStrategy();
            case TRACKER -> new TrackerStrategy();
            case WANDERER -> new WandererStrategy(); 
            case CHAOTIC -> new ChaoticStrategy();
        };
    }

    /**
     * One chasing decision: ask the strategy for a target tile, then step towards it.
     * Every personality has a strategy now; the random fallback only stays as a safety net for a ghost without one.
     */
    public void chooseChaseDirection(Labyrinth map, PacMan pacman, Random random) {
        if (strategy == null) {
            chooseDirection(map, random);
            return;
        }
        Tile target = strategy.target(this, pacman, map);
        chooseDirectionTowards(map, target.x(), target.y());
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
        if (d == Directions.IDLE) {
            return false; // idle is not a step
        }
        return map.isWalkable(nextX(d), nextY(d));
    }

    /** The column of the tile one step away in direction d. */
    private int nextX(Directions d) {
        return switch (d) {
            case LEFT -> x - speed;
            case RIGHT -> x + speed;
            default -> x;
        };
    }

    /** The row of the tile one step away in direction d. */
    private int nextY(Directions d) {
        return switch (d) {
            case UP -> y - speed;
            case DOWN -> y + speed;
            default -> y;
        };
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
     * Picks, from possibleDirections, the direction whose next tile is closest to the target tile,
     * and turns the ghost that way (greedy choice: best-looking step now).
     * Distance is the squared straight-line distance; the square root is skipped because
     * it does not change which one is smaller. On a tie the earlier direction in the list wins.
     */
    public void chooseDirectionTowards(Labyrinth map, int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;

        List<Directions> options = possibleDirections(map);
        if (options.isEmpty()) {
            direction = Directions.IDLE;
            return;
        }

        // Start from the first option, so best always has a value
        Directions best = options.get(0);
        // MAX_VALUE, so the first distance we compute is smaller
        int bestDistance = Integer.MAX_VALUE;

        for (Directions d : options) {
            int dx = nextX(d) - targetX;
            int dy = nextY(d) - targetY;
            int distance = dx * dx + dy * dy;

            // On a tie the earlier direction stays
            if (distance < bestDistance) {
                bestDistance = distance;
                best = d;
            }
        }
        direction = best;
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

    /**Ghost goes back to Ghost house*/
    public void respawn() {
        if(getState() != GhostState.DEAD) {
            return;
        }

        // Not home yet if EITHER the column OR the row differs (De Morgan: !(a && b) == !a || !b)
        if (getX() != getStartX() || getY() != getStartY()) {
            return;
        }

        state = GhostState.CHASE;
        direction = Directions.IDLE;
    }

    /**After a ghost gets eaten in FRIGHTENED state.
     * Pac-Man ate this frightened ghost: it becomes DEAD. Ghosts in any other state are not affected
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
