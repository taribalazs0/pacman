package hu.bme.pacman.game;

import java.io.Serializable;

/**
 * Strategy pattern: how one ghost personality picks its target tile while chasing.
 * The ghost holds one as a field (composition), so the CHAOTIC ghost can swap it at runtime.
 * Serializable, because every ghost is saved together with its strategy.
 * A strategy only says WHERE to go; Ghost.chooseDirectionTowards decides HOW to get closer.
 */
public interface GhostStrategy extends Serializable {

    /**
     * @param self   the ghost asking (WANDERER needs its own position)
     * @param pacman Pac-Man, with his tile and direction
     * @param map    the maze (for its size, e.g. to find a corner)
     * @return the tile the ghost should head for
     */
    Tile target(Ghost self, PacMan pacman, Labyrinth map);
}
