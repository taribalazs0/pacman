package hu.bme.pacman.game;

import hu.bme.enums.Directions;

/**
 * The player. Holds the latest command from the keyboard and runs it
 * when the engine asks. Pac-Man is not itself a command.
 */
public class PacMan extends GameObject {
    // transient: when making an bytestream from this object (serializing) java skips this attribute.
    // after loading this will be null.
    private transient Command currentCommand;

    public PacMan(int x, int y) {
        super(x, y, 1, Directions.IDLE);
    }

    /** Makes buffered turn possible. Checked by GamePanel's keyPressed after each WASD button press. 
     * Only stores the command, does not execute.
    */
    public void setCurrentCommand(Command currentCommand) {
        this.currentCommand = currentCommand;
    }

    /* If we have a valid incoming command (through setCurrentCommand()), we execute the Command interface */
    public void processInput() {
        if (currentCommand != null) {
            currentCommand.execute();
        }
    }

    /**Back to the start tile, standing still, with no buffered command.*/
    @Override 
    public void resetToStart() {
        super.resetToStart();
        currentCommand = null;
    }
}
