package hu.bme.pacman.game;

import java.io.Serializable;

import hu.bme.enums.Directions;

/**
 * Anything that walks the maze: Pac-Man and every ghost.
 * x is the column, y is the row, both counted in tiles. speed is how many
 * tiles one step covers. The engine decides whether a step is legal;
 * move() only applies it.
 */
public abstract class GameObject implements Serializable {
    protected int x;
    protected int y;
    protected int speed;
    protected Directions direction;

    // Start tiles to get back to after a hit (life loss)
    private final int startX;
    private final int startY;

    // previous cell of a character
    private int prevX;
    private int prevY;

    protected GameObject(int x, int y, int speed, Directions direction) {
        this.prevX = x;
        this.prevY = y;
        this.startX = x;
        this.startY = y;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
    }

    /** Copies the current x,y values into the previous x,y values 
     *  Call once at the start of every step, even if the object does not move
    */
    public void rememberPosition() {
        prevX = x;
        prevY = y;
    }
    
    public void move() {
        switch (direction) {
            case UP -> y -= speed;
            case DOWN -> y += speed;
            case LEFT -> x -= speed;
            case RIGHT -> x += speed;
            case IDLE -> {
            }
        }
    }

    /**Puts the object back on its start tile, standing still. We do not reset the maze*/
    public void resetToStart() {
        this.x = startX;
        this.y = startY;

        this.prevX = startX;
        this.prevY = startY;

        this.direction = Directions.IDLE;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpeed() {
        return speed;
    }

    public int getPrevX() {
        return prevX;
    }

    public int getPrevY() {
        return prevY;
    }

    public Directions getDirection() {
        return direction;
    }

    public void setDirection(Directions direction) {
        this.direction = direction;
    }
}
