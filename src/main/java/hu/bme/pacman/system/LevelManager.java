package hu.bme.pacman.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

    /**
     * Writes a level to a map file, in the same format loadFromCSV reads.
     * The actors are written where they stand now, so pass a level whose actors are on their start tiles.
     *
     * @param file  path of the map file; an existing file is overwritten
     * @param level the maze and the actors to save
     * @throws IOException if the file cannot be written
     * @throws IllegalArgumentException if no file name or level is given, or the level is not a valid map
     */
    public static void saveToCSV(String file, LevelData level) throws IOException {
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("No map file given");
        }
        if (level == null) {
            throw new IllegalArgumentException("No level given");
        }

        // Build the whole picture in memory first. Opening the file for writing empties it at once,
        // so every check must pass before that, or a bad level would wipe a good file.
        Labyrinth map = level.getMap();
        char[][] picture = new char[map.getHeight()][map.getWidth()];
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                picture[y][x] = symbolOf(map.getCell(x, y));
            }
        }

        PacMan pacman = level.getPacman();
        placeActor(picture, map, pacman.getX(), pacman.getY(), PACMAN);
        for (Ghost ghost : level.getGhosts()) {
            placeActor(picture, map, ghost.getX(), ghost.getY(), symbolOf(ghost.getPersonality()));
        }

        // try block without catch, IOException gets to the caller
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(file), StandardCharsets.UTF_8)) {
            for (char[] row : picture) {
                writer.write(row);
                writer.newLine();
            }
        }
    }

    /**
     * Puts an actor's letter on its tile. The tile must be inside the map, EMPTY in the maze,
     * and not taken by another actor; otherwise loading the file back would not give the same level.
     */
    private static void placeActor(char[][] picture, Labyrinth map, int x, int y, char symbol) {
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) {
            throw new IllegalArgumentException("'" + symbol + "' is outside the map at " + x + ", " + y);
        }
        if (map.getCell(x, y) != CellType.EMPTY) {
            throw new IllegalArgumentException("'" + symbol + "' stands on " + map.getCell(x, y)
                    + " at " + x + ", " + y + ", but actors must stand on EMPTY");
        }
        if (picture[y][x] != EMPTY) {
            throw new IllegalArgumentException("'" + symbol + "' and '" + picture[y][x]
                    + "' share the tile " + x + ", " + y);
        }
        picture[y][x] = symbol;
    }

    /**
     * The character a tile is written as.
     * Left of the arrow is the CellType constant, right of it our char constant, hence the class name.
     */
    private static char symbolOf(CellType cell) {
        return switch (cell) {
            case WALL -> LevelManager.WALL;
            case DOT -> LevelManager.DOT;
            case POWERPELLET -> LevelManager.POWERPELLET;
            case EMPTY -> LevelManager.EMPTY;
        };
    }

    /** The letter a ghost is written as. */
    private static char symbolOf(GhostPersonality personality) {
        return switch (personality) {
            case TRACKER -> GHOST_TRACKER;
            case AMBUSHER -> GHOST_AMBUSHER;
            case WANDERER -> GHOST_WANDERER;
            case CHAOTIC -> GHOST_CHAOTIC;
        };
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
