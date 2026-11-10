package hu.bme.pacman.game;

/**
 * TRACKER (RED)
 * TRACKER directly follows PacMan in the maze, targeting the current tile of Pacman
 */
public class TrackerStrategy implements GhostStrategy {
    @Override 
    public Tile target(Ghost self, PacMan pacman, Labyrinth map) {
        int x = pacman.getX() + pacman.getDirection().dx();
        int y = pacman.getY() + pacman.getDirection().dy();
        return new Tile(x, y);
    }

}