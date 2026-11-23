package hu.bme.pacman.game;

import java.io.Serializable;

/**
 * One tile of the maze as a single value: x is the column, y is the row.
 * A strategy returns its target as a Tile, because a method can only return one value.
 * It may point outside the maze or onto a wall; a target only has to be measurable, not reachable.
 * Immutable: both fields are final and there are no setters, so a Tile can be shared safely.
 */
public final class Tile implements Serializable {
    private final int x;
    private final int y;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Two tiles are equal if both their column and row match (value equality).
     * Without this, equals would compare memory addresses, and two new Tile(3, 5) would differ.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tile tile)) {
            return false;
        }
        return x == tile.x && y == tile.y;
    }

    /** Must agree with equals: equal tiles give the same hash code (needed by HashMap / HashSet). */
    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "Tile(" + x + ", " + y + ")";
    }
}
