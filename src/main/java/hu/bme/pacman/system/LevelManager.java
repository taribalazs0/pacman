package hu.bme.pacman.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import hu.bme.enums.CellType;
import hu.bme.enums.GhostPersonality;
import hu.bme.pacman.game.Ghost;
import hu.bme.pacman.game.Labyrinth;
import hu.bme.pacman.game.LevelData;
import hu.bme.pacman.game.PacMan;

/**
 * Stateless I/O helper for map files.
 * A map file is a text picture of the maze: one row per line, one character per tile.
 *
 * #  wall           .  dot            o  power pellet     _  empty
 * P  Pac-Man's start (exactly one)
 * T  TRACKER    A  AMBUSHER    W  WANDERER    C  CHAOTIC   (ghost starts)
 *
 * Under P and the ghost letters the tile is EMPTY; the letters only mark where an actor starts.
 */

// Final class: Can not have child class
public final class LevelManager {

    public static final char WALL = '#';
    public static final char DOT = '.';
    public static final char POWERPELLET = 'o';
    public static final char EMPTY = '_';
    public static final char PACMAN = 'P';
    public static final char GHOST_TRACKER = 'T';
    public static final char GHOST_AMBUSHER = 'A';
    public static final char GHOST_WANDERER = 'W';
    public static final char GHOST_CHAOTIC = 'C';

    /** Only static methods, so nobody needs an instance. */
    private LevelManager() {
    }

    /**
     * Factory: reads a map file and builds the maze, Pac-Man, and the ghosts from it.
     *
     * @param file path of the map file
     * @return the maze and the actors standing on their start tiles
     * @throws IOException if the file is missing or cannot be read
     * @throws IllegalArgumentException if no file name is given or the content is not a valid map
     */
    public static LevelData loadFromCSV(String file) throws IOException {
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("No map file given");
        }

        // A List, because we do not know the number of rows beforehand
        List<CellType[]> rows = new ArrayList<>();
        List<Ghost> ghosts = new ArrayList<>();
        PacMan pacman = null;

        // try block without catch, IOException gets to the caller
        try (BufferedReader reader = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Editors may add or remove spaces at the end of a line; no symbol is a space, so drop them
                line = line.stripTrailing();
                if (line.isEmpty()) {
                    continue;
                }

                // The row index in the maze; skipped blank lines do not count
                int y = rows.size();
                CellType[] row = new CellType[line.length()];

                for (int x = 0; x < line.length(); x++) {
                    char symbol = line.charAt(x);
                    switch (symbol) {
                        case WALL -> row[x] = CellType.WALL;
                        case DOT -> row[x] = CellType.DOT;
                        case POWERPELLET -> row[x] = CellType.POWERPELLET;
                        case EMPTY -> row[x] = CellType.EMPTY;
                        case PACMAN -> {
                            if (pacman != null) {
                                throw new IllegalArgumentException("Second Pac-Man at line "
                                        + lineNumber + ", column " + (x + 1));
                            }
                            pacman = new PacMan(x, y);
                            row[x] = CellType.EMPTY;
                        }
                        case GHOST_TRACKER, GHOST_AMBUSHER, GHOST_WANDERER, GHOST_CHAOTIC -> {
                            ghosts.add(new Ghost(x, y, personalityOf(symbol)));
                            row[x] = CellType.EMPTY;
                        }
                        default -> throw new IllegalArgumentException("Unknown symbol '" + symbol
                                + "' at line " + lineNumber + ", column " + (x + 1));
                    }
                }

                if (!rows.isEmpty() && row.length != rows.get(0).length) {
                    throw new IllegalArgumentException("Line " + lineNumber + " has " + row.length
                            + " tiles, but the first row has " + rows.get(0).length);
                }
                rows.add(row);
            }
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Map file has no rows: " + file);
        }
        if (pacman == null) {
            throw new IllegalArgumentException("Map has no Pac-Man (" + PACMAN + "): " + file);
        }

        Labyrinth map = new Labyrinth(rows.toArray(new CellType[0][]));
        return new LevelData(map, pacman, ghosts);
    }

    /** Which personality a ghost letter stands for. */
    private static GhostPersonality personalityOf(char symbol) {
        return switch (symbol) {
            case GHOST_TRACKER -> GhostPersonality.TRACKER;
            case GHOST_AMBUSHER -> GhostPersonality.AMBUSHER;
            case GHOST_WANDERER -> GhostPersonality.WANDERER;
            case GHOST_CHAOTIC -> GhostPersonality.CHAOTIC;
            default -> throw new IllegalArgumentException("Not a ghost symbol: " + symbol);
        };
    }
}
