package hu.bme.pacman.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import hu.bme.enums.CellType;
import hu.bme.enums.Directions;

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
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.WALL, CellType.EMPTY, CellType.DOT, CellType.WALL},
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
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
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
                {CellType.WALL, CellType.EMPTY, CellType.EMPTY, CellType.WALL},
                {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}
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
}
