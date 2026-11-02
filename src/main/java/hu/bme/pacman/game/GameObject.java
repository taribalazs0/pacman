package hu.bme.pacman.game;

import java.io.Serializable;

import hu.bme.enums.Directions;

public class GameObject implements Serializable {
    protected int x, y;
    protected int speed;
    protected Directions direction;

    public void move() {

    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getSpeed() {
        return this.speed;
    }

    public Directions getDirection() {
        return this.direction;
    }


}
