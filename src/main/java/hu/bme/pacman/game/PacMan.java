package hu.bme.pacman.game;

import hu.bme.enums.Directions;

/**
 * The player. Holds the latest command from the keyboard and runs it
 * when the engine asks. Pac-Man is not itself a command.
 */
public class PacMan extends GameObject {
    private Command currentCommand;

    public PacMan(int x, int y) {
        super(x, y, 1, Directions.IDLE);
    }

    public void setCurrentCommand(Command currentCommand) {
        this.currentCommand = currentCommand;
    }

    public void processInput() {
        if (currentCommand != null) {
            currentCommand.execute();
        }
    }
}
