package hu.bme.pacman.game;

import java.io.Serializable;

/**
 * Strategy pattern: how one ghost personality picks its target tile while chasing.
 * The ghost holds one as a field, so the CHAOTIC ghost can swap it at runtime.
 * Serializable, because every ghost is saved together with its strategy.
 * A strategy only says WHERE to go; Ghost.chooseDirectionTowards decides HOW to get closer.
 */
public interface GhostStrategy extends Serializable {

    /**
     * @param self   the ghost asking (WANDERER needs its own position and its start tile in the Ghost House)
     * @param pacman Pac-Man, with his tile and direction
     * @param map    the maze (no strategy needs it yet, but later ones may, e.g. for its size)
     * @return the tile the ghost should head for
     */
    Tile target(Ghost self, PacMan pacman, Labyrinth map);
}
