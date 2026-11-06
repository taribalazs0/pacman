package hu.bme.pacman.game;

import hu.bme.enums.Directions;

/**
 * Asks Pac-Man to face one direction.
 * If that neighbour tile is a wall, the current direction is left alone.
 * That is the corner pre-turn: the wish is remembered, the turn happens
 * on the first tick where the tile is open. This way with using input buffering we make
 *  the game feel like the original.
 */
public class MoveCommand implements Command {
    private final PacMan pacman;
    private final Labyrinth map;
    private final Directions direction;

    public MoveCommand(PacMan pacman, Labyrinth map, Directions direction) {
        this.pacman = pacman;
        this.map = map;
        this.direction = direction;
    }

    @Override 
    public void execute() {
        int nextX = pacman.getX();
        int nextY = pacman.getY();
        int step = pacman.getSpeed();

        switch (direction) {
            case UP -> nextY -= step;
            case DOWN -> nextY += step;
            case LEFT -> nextX -= step;
            case RIGHT -> nextX += step;
            case IDLE -> {
                return;
            }
        }

        if (map.isWalkable(nextX, nextY)) {
            pacman.setDirection(direction);
        }
    }
}
