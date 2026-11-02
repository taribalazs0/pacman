package hu.bme.pacman.game;

interface Command {
    // execute command
    public void execute();
}

public class PacMan extends GameObject implements Command{
    private Command currentCommand;

    @Override
    public void execute() {
        // TODO Auto-generated method stub
        
    }
}
