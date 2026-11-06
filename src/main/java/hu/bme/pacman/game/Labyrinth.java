package hu.bme.pacman.game;

import java.io.Serializable;

import hu.bme.enums.CellType;

/**
 * The maze. grid[y][x] is the tile at column x, row y.
 * A missing tile (outside the array) is treated as a wall,
 * so nothing can step off the map.
 */
public class Labyrinth implements Serializable {
    private final CellType[][] grid;

    public Labyrinth(CellType[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            throw new IllegalArgumentException("Map is empty");
        }
        int width = grid[0].length;
        for (CellType[] row : grid) {
            if (row.length != width) {
                throw new IllegalArgumentException("Every map row must have the same length");
            }
        }
        this.grid = grid;
    }

    public int getWidth() {
        return grid[0].length;
    }

    public int getHeight() {
        return grid.length;
    }

    public CellType getCell(int x, int y) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) {
            return CellType.WALL;
        }
        return grid[y][x];
    }

    public void setCell(int x, int y, CellType type) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) {
            throw new IllegalArgumentException("Tile is outside the map: " + x + ", " + y);
        }
        grid[y][x] = type;
    }

    /** A direction in the maze is walkable, if it is not a wall */
    public boolean isWalkable(int x, int y) {
        return getCell(x, y) != CellType.WALL;
    }

    /**
     * A junction: a walkable tile with three or four open neighbours.
     * A straight corridor has two, a corner has two, a dead end has one.
     */
    public boolean isIntersection(int x, int y) {
        if (!isWalkable(x, y)) {
            return false;
        }
        int openNeighbours = 0;
        if (isWalkable(x, y - 1)) {
            openNeighbours++;
        }
        if (isWalkable(x, y + 1)) {
            openNeighbours++;
        }
        if (isWalkable(x - 1, y)) {
            openNeighbours++;
        }
        if (isWalkable(x + 1, y)) {
            openNeighbours++;
        }
        return openNeighbours >= 3;
    }

    public void loadMap(String filename) {
        throw new UnsupportedOperationException("Reading a map file comes later, in LevelManager");
    }
}
