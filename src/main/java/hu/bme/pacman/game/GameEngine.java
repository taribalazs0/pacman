package hu.bme.pacman.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hu.bme.enums.CellType;
import hu.bme.enums.Directions;
import hu.bme.enums.GhostState;

/**
 * The rules, with no window and no drawing.
 * One update() is one tick: read the buffered key, step one tile if it is open,
 * then pick up a dot if Pac-Man is standing on one.
 */
public class GameEngine implements Serializable {

    public static final int DOT_SCORE = 10;
    public static final int TICKS_PER_SECOND = 60; // 60 frames per second
    public static final int TICKS_PER_STEP = 10; // 60/10 = 6 cells every second

    /** how many ticks since the last step: [0 - TICKS_PER_STEP -1]*/
    private int tickInStep; 

    private int score;
    private int lives;
    private int currentLevel;
    private final Labyrinth map;
    private final PacMan pacman;
    private final List<Ghost> ghosts;
    // One shared source of randomness for every ghost. Random is Serializable, so it is saved with the game.
    private final Random random;

    /**
     * A new game on a level from a map file, the map editor, or a test.
     * Always starts fresh: score 0, 3 lives, level 1.
     * A saved game is not loaded through here; loadGameState restores the whole engine.
     */
    public GameEngine(LevelData level) {
        this(level, new Random());
    }

    /**
     * Same as above, but with a given Random. A test can pass new Random(42),
     * and then the ghosts make the same "random" choices on every run.
     */
    public GameEngine(LevelData level, Random random) {
        if (level == null) {
            throw new IllegalArgumentException("No level given");
        }
        if (random == null) {
            throw new IllegalArgumentException("No random source given");
        }

        this.map = level.getMap();
        this.pacman = level.getPacman();
        // Our own copy: nobody holding the original list can add or remove ghosts behind our back
        this.ghosts = new ArrayList<>(level.getGhosts());
        this.random = random;
        this.score = 0;
        this.lives = 3;
        this.currentLevel = 1;
        this.tickInStep = 0;
    }

    /** Calculates exactly 1 tick of change*/
    public void update() {
        tickInStep++;

        if(tickInStep >= TICKS_PER_STEP) { 
            tickInStep = 0;
            step();
        }
    }

    /** Movement of 1 cell*/
    public void step() {
        pacman.processInput();
        stepIfOpen(pacman);
        for (Ghost ghost : ghosts) {
            ghost.chooseDirection(map, random);
            stepIfOpen(ghost);
        }
        checkCollisions();
    }

    /**
     * Whether Pac-Man and this ghost met during the last step.
     * Only detects; what happens next (losing a life, eating the ghost) is the caller's job.
     * Call it after everyone has moved, so prev and current position describe the same step.
     */
    private boolean collidesWith(Ghost ghost) {
        // They end the step on the same tile
        if (sameTile(pacman.getX(), pacman.getY(), ghost.getX(), ghost.getY())) {
            return true;
        }
        // They swapped tiles: each now stands where the other stood before, so they passed through each other.
        // Both halves are needed; one alone is also true when Pac-Man simply follows the ghost.
        if(sameTile(pacman.getX(), pacman.getY(), ghost.getPrevX(), ghost.getPrevY()) && sameTile(ghost.getX(), ghost.getY(), pacman.getPrevX(), pacman.getPrevY())){
            return true;
        }

        return false;
    }

    /** A tile is the same only if both the column and the row match. */
    private static boolean sameTile(int x1, int y1, int x2, int y2) {
        return x1 == x2 && y1 == y2;
    }

    public void checkCollisions() {
        if (map.getCell(pacman.getX(), pacman.getY()) == CellType.DOT) {
            score += DOT_SCORE;
            map.setCell(pacman.getX(), pacman.getY(), CellType.EMPTY);
        }

        for(Ghost ghost : ghosts) {
            if(collidesWith(ghost)) {
                loseLife();
                break;
            }
        }
    }

    /**What happens after you collide with a ghost */
    private void loseLife() {
        lives--;
        pacman.resetToStart();

        for(Ghost ghost : ghosts) {
            ghost.resetToStart();
        }

    }

    /**Return double between 0.0 and 1.0 (progress of step) */
    public double getStepProgress() {
        double currentStepProgress = ((double)tickInStep / TICKS_PER_STEP); 
        return currentStepProgress;
    }

    /* Todo */
    public void saveGameState(String file) {
        throw new UnsupportedOperationException("Saving a game comes later");
    }
    
    /* Todo */
    public void loadGameState(String file) {
        throw new UnsupportedOperationException("Loading a game comes later");
    }

    private void stepIfOpen(GameObject entity) {
        entity.rememberPosition();
        if (entity.getDirection() == Directions.IDLE) {
            return;
        }
        int nextX = nextColumn(entity);
        int nextY = nextRow(entity);
        if (map.isWalkable(nextX, nextY)) {
            entity.move();
        }
    }

    

    private int nextColumn(GameObject entity) {
        int step = entity.getSpeed();
        return switch (entity.getDirection()) {
            case LEFT -> entity.getX() - step;
            case RIGHT -> entity.getX() + step;
            default -> entity.getX();
        };
    }

    private int nextRow(GameObject entity) {
        int step = entity.getSpeed();
        return switch (entity.getDirection()) {
            case UP -> entity.getY() - step;
            case DOWN -> entity.getY() + step;
            default -> entity.getY();
        };
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public Labyrinth getMap() {
        return map;
    }

    public PacMan getPacman() {
        return pacman;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

}