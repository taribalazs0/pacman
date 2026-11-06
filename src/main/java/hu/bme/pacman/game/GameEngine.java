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

    /** The maze you see when the program starts. */
    public GameEngine() {
        Demo demo = Demo.parse();
        this.map = demo.map;
        this.pacman = new PacMan(demo.pacmanX, demo.pacmanY);
        this.ghosts = new ArrayList<>();
        this.ghosts.add(new Ghost(demo.ghostX, demo.ghostY, GhostPersonality.TRACKER));
        this.score = 0;
        this.lives = 3;
        this.currentLevel = 1;
    }

    /** A map and actors built by a test, or by a loaded file if we load from a previous save */
    public GameEngine(Labyrinth map, PacMan pacman, List<Ghost> ghosts) {
        this.map = map;
        this.pacman = pacman;
        this.ghosts = ghosts == null ? new ArrayList<>() : ghosts;
        this.score = 0;
        this.lives = 3;
        this.currentLevel = 1;
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

    /**
     * '#' wall, '.' dot, 'P' Pac-Man's start, 'G' the ghost's start.
     * P and G are stored as empty tiles; the letters only mark where to place them.
     */
    private static final class Demo {
        private final Labyrinth map;
        private final int pacmanX;
        private final int pacmanY;
        private final int ghostX;
        private final int ghostY;

        private Demo(Labyrinth map, int pacmanX, int pacmanY, int ghostX, int ghostY) {
            this.map = map;
            this.pacmanX = pacmanX;
            this.pacmanY = pacmanY;
            this.ghostX = ghostX;
            this.ghostY = ghostY;
        }

        private static Demo parse() {
            String[] rows = {
    "############################",
    "#............##............#",
    "#o####.#####.##.#####.####o#",
    "#..........................#",
    "#.####.##.########.##.####.#",
    "#......##....##....##......#",
    "#.####.#####.##.#####.####.#",
    "#.####.##..........##.####.#",
    "#.####.##.###..###.##.####.#",
    "#.........# GGGG #.........#",
    "#.####.##.########.##.####.#",
    "#.####.##..........##.####.#",
    "#.####.##.########.##.####.#",
    "#............##............#",
    "#.####.#####.##.#####.####.#",
    "#o..##.......P........##..o#",
    "###.##.##.########.##.##.###",
    "#......##....##....##......#",
    "#.##########.##.##########.#",
    "#..........................#",
    "############################"
};

            int height = rows.length;
            int width = rows[0].length();
            CellType[][] grid = new CellType[height][width];
            int pacmanX = 1;
            int pacmanY = 1;
            int ghostX = 1;
            int ghostY = 1;

            for (int y = 0; y < height; y++) {
                if (rows[y].length() != width) {
                    throw new IllegalArgumentException("Demo map rows differ in length");
                }
                for (int x = 0; x < width; x++) {
                    char tile = rows[y].charAt(x);
                    switch (tile) {
                        case '#' -> grid[y][x] = CellType.WALL;
                        case '.' -> grid[y][x] = CellType.DOT;
                        case 'P' -> {
                            grid[y][x] = CellType.EMPTY;
                            pacmanX = x;
                            pacmanY = y;
                        }
                        case 'G' -> {
                            grid[y][x] = CellType.EMPTY;
                            ghostX = x;
                            ghostY = y;
                        }
                        default -> grid[y][x] = CellType.EMPTY;
                    }
                }
            }
            return new Demo(new Labyrinth(grid), pacmanX, pacmanY, ghostX, ghostY);
        }
    }
}
