package hu.bme.pacman.game;

/**
 * AMBUSHER (pink): aims 4 tiles ahead of Pac-Man in the direction he is facing,
 * to cut him off instead of following him.
 * If Pac-Man stands still (IDLE), dx and dy are 0, so the target is Pac-Man himself.
 * The target may fall on a wall or outside the maze; that is fine, it is only used to measure distance.
 */
public class AmbusherStrategy implements GhostStrategy {

    /** How many tiles ahead of Pac-Man the ambusher aims. */
    public static final int TILES_AHEAD = 4;

    @Override
    public Tile target(Ghost self, PacMan pacman, Labyrinth map) {
        int x = pacman.getX() + pacman.getDirection().dx() * TILES_AHEAD;
        int y = pacman.getY() + pacman.getDirection().dy() * TILES_AHEAD;
        return new Tile(x, y);
    }
}
