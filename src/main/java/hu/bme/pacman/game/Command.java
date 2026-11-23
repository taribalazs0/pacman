package hu.bme.pacman.game;

/**
 * One player action, stored and run later.
 * WASD does not move Pac-Man on the spot. It stores a command,
 * and the engine calls execute() on the next tick.
 * 
 * Implemented by MoveCommand
 */
public interface Command {
    void execute();
}
