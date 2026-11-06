package hu.bme.enums;

/**
 * GhostState
 * 
 * CHASE :       Each ghost uses its unique artificial intelligence to actively target and corner Pac-Man.
 * SCATTER:      Ghosts temporarily stop pursuing Pac-Man and retreat
 *               to patrol their own designated corners of the maze.
 * FRIGHTENED:   Ghosts turn blue, move slowly and randomly, and become vulnerable
 *               to being eaten after Pac-Man consumes a Power Pellet.
 * DEAD (Eaten): Reduced to just a pair of floating eyes, the ghost rushes
 *               back to the central Ghost House to respawn and regain its body.
*/


public enum GhostState {
    CHASE,
    SCATTER,
    FRIGHTENED,
    DEAD
}
