package hu.bme.pacman.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.bme.enums.Directions;
import hu.bme.enums.GhostPersonality;
import hu.bme.enums.GhostState;

class GhostTest {

    private static final int HOME_X = 3;
    private static final int HOME_Y = 2;

    private Ghost ghost;

    @BeforeEach
    void setUp() {
        // The start tile is the ghost's home in the Ghost House
        ghost = new Ghost(HOME_X, HOME_Y, GhostPersonality.TRACKER);
    }

    /** The only legal way to a DEAD ghost: it gets frightened, then Pac-Man eats it. */
    private void kill() {
        ghost.frighten();
        ghost.eaten();
    }

    @Test
    void aDeadGhostAwayFromHomeStaysDead() {
        // Arrange: one tile right of home, then killed
        ghost.setDirection(Directions.RIGHT);
        ghost.move();
        kill();

        // Act
        ghost.respawn();

        // Assert
        assertEquals(GhostState.DEAD, ghost.getState());
    }

    @Test
    void aDeadGhostBackHomeRespawnsAndStandsStill() {
        // Arrange: leave home, die, walk back home
        ghost.setDirection(Directions.RIGHT);
        ghost.move();
        kill(); // frighten() turned it around, so it now faces LEFT, towards home
        ghost.move();

        // Act
        ghost.respawn();

        // Assert
        assertEquals(HOME_X, ghost.getX());
        assertEquals(GhostState.CHASE, ghost.getState());
        assertEquals(Directions.IDLE, ghost.getDirection());
    }
}
