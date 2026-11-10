package hu.bme.pacman.game;


/**
 * The wanderer follows Pacman from a distanc, but whenever he gets in a distance of 8 or closer to it,
 * he flees and goes back to the ghost house.
 */
public class WandererStrategy implements GhostStrategy {
    
    /** Whenever the Wanderer gets in 8 distance to PacMan, he flees */
    public static final int FLEE_DISTANCE = 8;
    
    @Override
    public Tile target (Ghost self, PacMan pacman, Labyrinth map) {

        // calculate the distance between Wanderer and Pacman
        int dx = self.getX() - pacman.getX();
        int dy = self.getY() - pacman.getY();

        // distance: squared (but no squareroot)
        int distance = dx * dx + dy * dy;

        // if he gets within distance than he retreats
        if(distance <= (FLEE_DISTANCE * FLEE_DISTANCE)) {
            return new Tile(self.getStartX(), self.getStartY());
        }
        else {
            int x = pacman.getX();
            int y = pacman.getY();
            return new Tile(x, y);        
        }
       
    }
}