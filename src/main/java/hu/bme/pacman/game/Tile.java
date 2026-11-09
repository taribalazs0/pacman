package hu.bme.pacman.game;

import java.io.Serializable;

/**
 * One tile of the maze as a single value: x is the column, y is the row.
 * A strategy returns its target as a Tile, because a method can only return one value.
 * A record gets its constructor, x(), y(), equals and toString for free, and cannot be changed.
 * It may point outside the maze or onto a wall; a target only has to be measurable, not reachable.
 */
public record Tile(int x, int y) implements Serializable {
}
