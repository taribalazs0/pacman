package hu.bme.pacman.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hu.bme.enums.CellType;
import hu.bme.enums.Directions;
import hu.bme.enums.GhostPersonality;

/**
 * The rules, with no window and no drawing.
 * One update() is one tick: read the buffered key, step one tile if it is open,
 * then pick up a dot if Pac-Man is standing on one.
 */
public class GameEngine implements Serializable {

    public static final int DOT_SCORE = 10;

    private int score;
    private int lives;
    private int currentLevel;
    private final Labyrinth map;
    private final PacMan pacman;
    private final List<Ghost> ghosts;    

    /** A map and actors built by a test, or by a loaded file if we load from a previous save */
    public GameEngine(LevelData level) {
        if(level == null) { throw new IllegalArgumentException(); }

        this.map = level.getMap();
        this.pacman = level.getPacman();
        this.ghosts = level.getGhosts() == null ? new ArrayList<>() : level.getGhosts();
        this.score = this.getScore();
        this.lives = this.getLives();
        this.currentLevel = getCurrentLevel();
    }

    public void update() {
        pacman.processInput();
        stepIfOpen(pacman);
        checkCollisions();
    }

    public void checkCollisions() {
        if (map.getCell(pacman.getX(), pacman.getY()) == CellType.DOT) {
            score += DOT_SCORE;
            map.setCell(pacman.getX(), pacman.getY(), CellType.EMPTY);
        }
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