package hu.bme.pacman.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.bme.enums.CellType;
import hu.bme.enums.Directions;
import hu.bme.enums.GhostPersonality;
import hu.bme.enums.GhostState;

class GameEngineTest {

    @Test
    void facingAWallDoesNotChangePosition() {
        CellType[][] grid = {
                {CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.WALL, CellType.EMPTY, CellType.WALL},
                {CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Labyrinth map = new Labyrinth(grid);
        PacMan pacman = new PacMan(1, 1);
        pacman.setDirection(Directions.LEFT);
        GameEngine engine = new GameEngine(new LevelData(map, pacman, List.of()));

        engine.step();

        assertEquals(1, pacman.getX());
        assertEquals(1, pacman.getY());
    }

    @Test
    void steppingOnADotRaisesTheScoreAndClearsTheTile() {
        CellType[][] grid = {
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.WALL, CellType.EMPTY, CellType.DOT, CellType.DOT, CellType.WALL},
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Labyrinth map = new Labyrinth(grid);
        PacMan pacman = new PacMan(1, 1);
        pacman.setDirection(Directions.RIGHT);
        GameEngine engine = new GameEngine(new LevelData(map, pacman, List.of()));

        engine.step();

        assertEquals(2, pacman.getX());
        assertEquals(1, pacman.getY());
        assertEquals(GameEngine.DOT_SCORE, engine.getScore());
        assertEquals(CellType.EMPTY, map.getCell(2, 1));
    }

    @Test
    void aBlockedTurnDoesNotCancelTheCurrentStep() {
        CellType[][] grid = {
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.WALL, CellType.EMPTY, CellType.EMPTY, CellType.DOT, CellType.WALL},
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
        };
        Labyrinth map = new Labyrinth(grid);
        PacMan pacman = new PacMan(1, 1);
        pacman.setDirection(Directions.RIGHT);
        pacman.setCurrentCommand(new MoveCommand(pacman, map, Directions.UP));
        GameEngine engine = new GameEngine(new LevelData(map, pacman, List.of()));

        engine.step();

        assertEquals(2, pacman.getX());
        assertEquals(1, pacman.getY());
        assertEquals(Directions.RIGHT, pacman.getDirection());
    }

    @Test
    void aPlusShapedTileIsAnIntersection() {
        CellType[][] grid = {
                {CellType.WALL, CellType.EMPTY, CellType.WALL},
                {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY},
                {CellType.WALL, CellType.EMPTY, CellType.WALL}
        };
        Labyrinth map = new Labyrinth(grid);

        assertTrue(map.isIntersection(1, 1));
        assertFalse(map.isIntersection(1, 0));
    }

    // ---------------------------------------------------------------
    // Shared fixture for the tests below: a one-row corridor
    //
    //   x:  0 1 2 3 4 5 6 7 8
    //       # P o _ _ _ _ . #      (row 1; rows 0 and 2 are walls)
    //
    // The DOT at (7,1) is there so the level is never cleared by accident:
    // with no DOT and no POWERPELLET left, step() would call nextLevel() and reset everyone.
    // ---------------------------------------------------------------

    private static final int PACMAN_START_X = 1;
    private static final int ROW = 1;

    private Labyrinth map;
    private PacMan pacman;

    @BeforeEach
    void setUp() {
        CellType W = CellType.WALL;
        CellType E = CellType.EMPTY;
        CellType[][] grid = {
                {W, W, W, W, W, W, W, W, W},
                {W, E, CellType.POWERPELLET, E, E, E, E, CellType.DOT, W},
                {W, W, W, W, W, W, W, W, W}
        };
        map = new Labyrinth(grid);
        pacman = new PacMan(PACMAN_START_X, ROW);
    }

    /** An engine on the fixture map with the given ghosts; a fixed seed keeps frightened ghosts repeatable. */
    private GameEngine engineWith(Ghost... ghosts) {
        return new GameEngine(new LevelData(map, pacman, List.of(ghosts)), new Random(42));
    }

    @Test
    void eatingAPowerPelletScoresAndFrightensTheGhosts() {
        // Arrange: the ghost starts far away, so it cannot reach Pac-Man in one step
        Ghost ghost = new Ghost(6, ROW, GhostPersonality.TRACKER);
        pacman.setDirection(Directions.RIGHT);
        GameEngine engine = engineWith(ghost);

        // Act: Pac-Man steps onto the pellet, the ghost steps left towards him (6 -> 5)
        engine.step();

        // Assert
        assertEquals(GameEngine.POWERPELLET_SCORE, engine.getScore());
        assertEquals(CellType.EMPTY, map.getCell(2, ROW));
        assertEquals(GhostState.FRIGHTENED, ghost.getState());
        // frighten() turns the ghost around once: it was walking LEFT
        assertEquals(Directions.RIGHT, ghost.getDirection());
    }

    @Test
    void meetingAChasingGhostCostsALifeAndResetsEveryone() {
        // Arrange: Pac-Man stands still, the tracker walks to him: 6 -> 5 -> 4 -> 3 -> 2 -> 1
        Ghost ghost = new Ghost(6, ROW, GhostPersonality.TRACKER);
        GameEngine engine = engineWith(ghost);

        // Act
        for (int i = 0; i < 5; i++) {
            engine.step();
        }

        // Assert
        assertEquals(2, engine.getLives());
        assertEquals(PACMAN_START_X, pacman.getX());
        assertEquals(6, ghost.getX(), "the ghost goes back to its start tile");
        assertEquals(GhostState.CHASE, ghost.getState());
    }

    @Test
    void eatingAFrightenedGhostScoresAndKeepsTheLife() {
        // Arrange: the ghost is right behind the pellet, so both arrive on (2,1) in the same step
        Ghost ghost = new Ghost(3, ROW, GhostPersonality.TRACKER);
        pacman.setDirection(Directions.RIGHT);
        GameEngine engine = engineWith(ghost);

        // Act: pellet first (ghost becomes FRIGHTENED), then the collision eats it
        engine.step();

        // Assert
        assertEquals(GhostState.DEAD, ghost.getState());
        assertEquals(3, engine.getLives());
        assertEquals(GameEngine.POWERPELLET_SCORE + GameEngine.GHOST_SCORE, engine.getScore());
    }

    @Test
    void afterTheLastLifeTheGameIsOverAndStops() {
        // Arrange
        Ghost ghost = new Ghost(6, ROW, GhostPersonality.TRACKER);
        GameEngine engine = engineWith(ghost);

        // Act: 3 deaths, 5 steps each (see the test above)
        for (int i = 0; i < 3 * 5; i++) {
            engine.step();
        }
        engine.step(); // one more step after game over

        // Assert
        assertEquals(0, engine.getLives());
        assertTrue(engine.isGameOver());
        assertEquals(6, ghost.getX(), "after game over step() must not move anyone");
    }

    @Test
    void theAmbusherAimsFourTilesAheadOfPacMan() {
        // Arrange
        Ghost ghost = new Ghost(6, ROW, GhostPersonality.AMBUSHER);
        AmbusherStrategy strategy = new AmbusherStrategy();
        pacman.setDirection(Directions.RIGHT);

        // Act
        Tile ahead = strategy.target(ghost, pacman, map);
        pacman.setDirection(Directions.IDLE);
        Tile standingStill = strategy.target(ghost, pacman, map);

        // Assert: Tile is a record, so assertEquals compares x and y
        assertEquals(new Tile(PACMAN_START_X + AmbusherStrategy.TILES_AHEAD, ROW), ahead);
        assertEquals(new Tile(PACMAN_START_X, ROW), standingStill);
    }

    @Test
    void nextLevelRaisesTheLevelAndRestoresTheEatenTiles() {
        // Arrange: Pac-Man eats the power pellet at (2,1)
        pacman.setDirection(Directions.RIGHT);
        GameEngine engine = engineWith();
        engine.step();
        assertEquals(CellType.EMPTY, map.getCell(2, ROW)); // precondition: really eaten

        // Act
        engine.nextLevel();

        // Assert
        assertEquals(2, engine.getCurrentLevel());
        assertEquals(CellType.POWERPELLET, map.getCell(2, ROW), "the maze is restored from the original");
        assertEquals(PACMAN_START_X, pacman.getX(), "Pac-Man starts the new level on his start tile");
        assertEquals(GameEngine.POWERPELLET_SCORE, engine.getScore(), "the score is kept between levels");
    }
}
