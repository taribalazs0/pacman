package hu.bme.pacman.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.bme.enums.CellType;

class LabyrinthTest {

    //   # . o #     one DOT at (1,1), one POWERPELLET at (2,1)
    private Labyrinth map;

    @BeforeEach
    void setUp() {
        CellType W = CellType.WALL;
        CellType[][] grid = {
                {W, W, W, W},
                {W, CellType.DOT, CellType.POWERPELLET, W},
                {W, W, W, W}
        };
        map = new Labyrinth(grid);
    }

    @Test
    void hasDotsLeftIsTrueWhileAnythingEdibleRemains() {
        // Arrange: eat the dot, only the power pellet stays
        map.setCell(1, 1, CellType.EMPTY);

        // Act + Assert: a power pellet also counts, the level is not cleared yet
        assertTrue(map.hasDotsLeft());
    }

    @Test
    void hasDotsLeftIsFalseOnceEverythingIsEaten() {
        // Arrange
        map.setCell(1, 1, CellType.EMPTY);
        map.setCell(2, 1, CellType.EMPTY);

        // Act
        boolean left = map.hasDotsLeft();

        // Assert
        assertFalse(left);
    }
}
