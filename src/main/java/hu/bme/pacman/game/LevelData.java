package hu.bme.pacman.game;

import java.util.List;
import java.util.Objects;

/**
 * Everything a level file describes: the maze, and where each actor starts.
 * LevelManager builds it from a file, GameEngine receives its parts.
 */
public final class LevelData {
    private final Labyrinth map;
    private final PacMan pacman;
    private final List<Ghost> ghosts;

    public LevelData(Labyrinth map, PacMan pacman, List<Ghost> ghosts) {
        this.map = Objects.requireNonNull(map, "map");
        this.pacman = Objects.requireNonNull(pacman, "pacman");
        this.ghosts = Objects.requireNonNull(ghosts, "ghosts");
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
