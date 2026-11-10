package hu.bme.pacman.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CHAOTIC (cyan): a chaotic ghost does not have a fixed way of choosing its target. It holds the other
 * three strategies (tracker, ambusher, wanderer), uses one of them, and every 15 seconds switches to a
 * randomly chosen one (it can pick the same one again), to keep the game unpredictable.
 *
 * This is the only strategy with a state of its own (the countdown and the active strategy).
 * Every chaotic ghost needs its own instance, so Ghost creates a new one for each of them.
 * Time is counted in calls of target(): the engine asks a chasing ghost once per step, so one call is one step.
 * A frightened or dead ghost is not asked, so the countdown pauses then; the 15 seconds are 15 seconds of chasing.
 */
public class ChaoticStrategy implements GhostStrategy {

    /** 15 seconds in steps. Multiply first, then divide, so integer division cannot cut off a fraction early. */
    public static final int SWITCH_STEPS = 15 * GameEngine.TICKS_PER_SECOND / GameEngine.TICKS_PER_STEP;

    private final List<GhostStrategy> strategies = new ArrayList<>();
    private final Random random; // Random is Serializable, so it is saved with the ghost
    private GhostStrategy current;
    private int stepsUntilSwitch; // time is counted in steps, not seconds

    /** A chaotic strategy with its own source of randomness. */
    public ChaoticStrategy() {
        this(new Random());
    }

    /** With a given Random, so a test can pass new Random(42) and get the same switches on every run. */
    public ChaoticStrategy(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("No random source given");
        }
        this.random = random;
        strategies.add(new TrackerStrategy());
        strategies.add(new AmbusherStrategy());
        strategies.add(new WandererStrategy());
        // Without a starting strategy the first target() call would fail with a NullPointerException
        this.current = pickRandomStrategy();
        this.stepsUntilSwitch = SWITCH_STEPS;
    }

    @Override
    public Tile target(Ghost self, PacMan pacman, Labyrinth map) {
        if (stepsUntilSwitch <= 0) {
            current = pickRandomStrategy();
            stepsUntilSwitch = SWITCH_STEPS;
        }
        stepsUntilSwitch--;

        // We let the current strategy decide the target
        return current.target(self, pacman, map);
    }

    /** A random one of the three; nextInt gives a valid list index, 0 .. size - 1. */
    private GhostStrategy pickRandomStrategy() {
        return strategies.get(random.nextInt(strategies.size()));
    }

    /** Which strategy is active now; only for tests. */
    GhostStrategy getCurrent() {
        return current;
    }
}
