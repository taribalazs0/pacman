package hu.bme.pacman.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import hu.bme.enums.CellType;
import hu.bme.enums.GhostPersonality;
import hu.bme.pacman.game.Ghost;
import hu.bme.pacman.game.Labyrinth;
import hu.bme.pacman.game.LevelData;
import hu.bme.pacman.game.PacMan;
import hu.bme.pacman.game.Tile;

/**
 * The map editor screen: a clickable grid in the middle, the tool palette on the right,
 * and the BACK and SAVE MAP buttons on top.
 *
 * Every map has the size of the built-in level (19 x 21 tiles), and the ghost house in the
 * middle is fixed: its tiles cannot be changed, and the four ghosts always start in it.
 * The player draws walls, dots, power pellets, and Pac-Man's start tile.
 * Left mouse button: draw with the selected tool (click, or hold and drag).
 * Right mouse button: erase the tile back to empty floor.
 *
 * A new editor is built every time the screen opens (see MainFrame.openEditor), so it
 * always starts from an empty black map.
 */
public class MapEditor extends JPanel {
    /** Size of every map, the size of maps/level1.txt. Not WIDTH/HEIGHT: Component already uses those names. */
    public static final int MAP_WIDTH = 19;
    public static final int MAP_HEIGHT = 21;
    /** Size of one tile on screen, the same as in the GamePanel. */
    private static final int TILE = 32;
    /** Size of the tile preview on a palette button. */
    private static final int ICON_SIZE = 24;

    /**
     * The fixed ghost house, copied from maps/level1.txt and written with the map-file symbols.
     * A space is not part of the house, the player may draw there. The house is the 5 x 3 block
     * of the last three lines; the TRACKER waits outside it, above the top opening.
     */
    private static final String[] GHOST_HOUSE = {
            "  T  ",
            "##_##",
            "#AWC#",
            "##_##"
    };
    /** Column and row of the template's top-left character in the map. */
    private static final int HOUSE_LEFT = 7;
    private static final int HOUSE_TOP = 7;
    private static final char OUTSIDE_HOUSE = ' ';

    private static final Color GRID_LINE = new Color(40, 40, 40);
    /** Floor of the locked tiles: a little lighter than black, so the player sees where not to draw. */
    private static final Color LOCKED_FLOOR = new Color(45, 45, 60);

    /** What the left mouse button draws. */
    private enum Tool {
        WALL("WALL", CellType.WALL),
        DOT("DOT", CellType.DOT),
        POWERPELLET("POWER PELLET", CellType.POWERPELLET),
        // Not a tile type: Pac-Man's start is a position on an EMPTY tile, like the P in a map file
        PACMAN("PAC-MAN START", null);

        private final String label;
        private final CellType cell;

        Tool(String label, CellType cell) {
            this.label = label;
            this.cell = cell;
        }
    }

    private final CellType[][] grid = new CellType[MAP_HEIGHT][MAP_WIDTH];
    /** The four ghosts on their fixed start tiles in the ghost house. */
    private final List<Ghost> ghosts = new ArrayList<>();
    /**
     * Pac-Man's start tile, or null while it is not placed yet.
     * One field, not a mark in the grid: there can never be two Pac-Mans, placing him again moves him.
     */
    private Tile pacmanStart;
    private Tool tool = Tool.WALL;
    /** Whether anything changed since the editor opened or since the last save. BACK asks if true. */
    private boolean unsaved;
    // Called by BACK. The editor does not know MainFrame, the same way GamePanel gets onQuit.
    private final Runnable onBack;

    public MapEditor(Runnable onBack) {
        this.onBack = onBack;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        for (CellType[] row : grid) {
            Arrays.fill(row, CellType.EMPTY);
        }
        placeGhostHouse();

        add(createTopBar(), BorderLayout.NORTH);
        add(new GridView(), BorderLayout.CENTER);
        add(createPalette(), BorderLayout.EAST);
    }

    /** Writes the fixed ghost house into the empty grid and creates the four ghosts. */
    private void placeGhostHouse() {
        for (int row = 0; row < GHOST_HOUSE.length; row++) {
            for (int col = 0; col < GHOST_HOUSE[row].length(); col++) {
                char symbol = GHOST_HOUSE[row].charAt(col);
                int x = HOUSE_LEFT + col;
                int y = HOUSE_TOP + row;
                switch (symbol) {
                    // A free tile, or the open floor of the house: it is EMPTY already
                    case OUTSIDE_HOUSE, LevelManager.EMPTY -> {
                    }
                    case LevelManager.WALL -> grid[y][x] = CellType.WALL;
                    // Every other symbol is a ghost letter; the tile under a ghost stays EMPTY
                    default -> ghosts.add(new Ghost(x, y, LevelManager.personalityOf(symbol)));
                }
            }
        }
    }

    /** Whether (x, y) belongs to the fixed ghost house; such a tile cannot be changed. */
    private static boolean isLocked(int x, int y) {
        int row = y - HOUSE_TOP;
        int col = x - HOUSE_LEFT;
        if (row < 0 || row >= GHOST_HOUSE.length || col < 0 || col >= GHOST_HOUSE[row].length()) {
            return false;
        }
        return GHOST_HOUSE[row].charAt(col) != OUTSIDE_HOUSE;
    }

    /**
     * Applies one mouse action to the tile (x, y): draws the selected tool, or erases.
     * Tiles outside the map and in the ghost house are ignored.
     * Pac-Man may only stand on empty floor, so drawing anything on his tile removes him.
     */
    private void editTile(int x, int y, boolean erase) {
        if (x < 0 || y < 0 || x >= MAP_WIDTH || y >= MAP_HEIGHT || isLocked(x, y)) {
            return;
        }

        // What the tile and Pac-Man look like now, and what they should look like after this action
        boolean pacmanHere = new Tile(x, y).equals(pacmanStart);
        boolean pacmanAfter = !erase && tool == Tool.PACMAN;
        CellType newCell = (erase || tool == Tool.PACMAN) ? CellType.EMPTY : tool.cell;

        // Dragging over a tile that already looks right changes nothing, so it is not "unsaved" either
        if (grid[y][x] == newCell && pacmanHere == pacmanAfter) {
            return;
        }

        grid[y][x] = newCell;
        if (pacmanAfter) {
            // The old start tile is simply replaced; it is EMPTY already, because Pac-Man only stands on EMPTY
            pacmanStart = new Tile(x, y);
        } else if (pacmanHere) {
            pacmanStart = null;
        }
        unsaved = true;
        repaint();
    }

    /**
     * SAVE MAP: checks the map, asks for a file name in the maps folder, and writes the map file.
     * The editor stays open afterwards, so the player can keep drawing or press BACK.
     */
    private void saveMap() {
        String problem = findProblem();
        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem, "Cannot save the map", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File file = chooseMapFile();
        if (file == null) {
            return;
        }
        try {
            LevelManager.saveToCSV(file.getPath(), toLevelData());
        } catch (IOException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Could not save the map: " + e.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        unsaved = false;
        JOptionPane.showMessageDialog(this, "Map saved as " + file.getName()
                + ".\nChoose it after Start new game to play on it.", "Map saved", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Why the map cannot be saved yet, or null if it can.
     * "Exactly one Pac-Man" only needs a null check: pacmanStart is a single field.
     */
    private String findProblem() {
        if (pacmanStart == null) {
            return "Place Pac-Man's start tile first (PAC-MAN START tool).";
        }
        if (!hasDot()) {
            return "The map needs at least one dot (DOT tool).";
        }
        return null;
    }

    private boolean hasDot() {
        for (CellType[] row : grid) {
            for (CellType cell : row) {
                if (cell == CellType.DOT) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The drawn map as a level, the form LevelManager.saveToCSV writes.
     * The Labyrinth wraps the editor's own grid without copying it: saveToCSV only reads it,
     * and the LevelData is thrown away right after saving.
     * Only call it after findProblem() returned null (Pac-Man must be placed).
     */
    private LevelData toLevelData() {
        PacMan pacman = new PacMan(pacmanStart.getX(), pacmanStart.getY());
        return new LevelData(new Labyrinth(grid), pacman, ghosts);
    }

    /**
     * Shows the save dialog in the maps folder. Adds ".txt" if the name has no extension,
     * refuses the built-in level1.txt, and asks before overwriting another map.
     *
     * @return the file to write, or null if the player cancelled
     */
    private File chooseMapFile() {
        JFileChooser chooser = createMapChooser();
        chooser.setDialogTitle("Save map");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        // Locale.ROOT: in some languages (e.g. Turkish) the default lower-casing changes the letter I
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith("." + LevelManager.MAP_EXTENSION)) {
            file = new File(file.getParentFile(), file.getName() + "." + LevelManager.MAP_EXTENSION);
        }
        if (isDefaultMap(file)) {
            JOptionPane.showMessageDialog(this, LevelManager.DEFAULT_MAP_NAME
                    + " is the built-in map and cannot be overwritten. Choose another name.",
                    "Save map", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (file.exists()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    file.getName() + " already exists. Overwrite it?", "Save map", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        return file;
    }

    /**
     * Whether the file is maps/level1.txt. Both paths are made absolute and normalized first,
     * so "maps/../maps/level1.txt" is caught too; on Windows the comparison ignores letter case.
     */
    private static boolean isDefaultMap(File file) {
        Path chosen = file.toPath().toAbsolutePath().normalize();
        Path builtIn = Path.of(LevelManager.MAPS_DIR, LevelManager.DEFAULT_MAP_NAME).toAbsolutePath().normalize();
        return chosen.equals(builtIn);
    }

    /**
     * A file chooser that opens in the maps folder and shows only .txt files.
     * Package-private and static, because MainMenu's "Start new game" picks the map with the same one.
     * The folder is created if missing, otherwise the dialog would fall back to the home folder.
     */
    static JFileChooser createMapChooser() {
        File mapsDir = new File(LevelManager.MAPS_DIR);
        mapsDir.mkdirs();
        JFileChooser chooser = new JFileChooser(mapsDir);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Pac-Man maps (*." + LevelManager.MAP_EXTENSION + ")", LevelManager.MAP_EXTENSION));
        return chooser;
    }

    /** BACK: goes to the menu, but asks first if the drawing has not been saved. */
    private void back() {
        if (unsaved) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "The map has unsaved changes. Leave without saving?", "Leave the editor",
                    JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        onBack.run();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBackground(Color.BLACK);

        JButton backBtn = new JButton("BACK");
        backBtn.addActionListener(event -> back());
        JButton saveBtn = new JButton("SAVE MAP");
        saveBtn.addActionListener(event -> saveMap());

        bar.add(backBtn);
        bar.add(saveBtn);
        return bar;
    }

    /** The four tool buttons on the right, and a short reminder of the mouse buttons below them. */
    private JPanel createPalette() {
        JPanel palette = new JPanel();
        palette.setLayout(new BoxLayout(palette, BoxLayout.Y_AXIS));
        palette.setBackground(Color.BLACK);
        palette.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // A ButtonGroup keeps exactly one toggle button pressed: selecting one releases the others
        ButtonGroup group = new ButtonGroup();
        for (Tool option : Tool.values()) {
            JToggleButton button = new JToggleButton(option.label, iconOf(option), option == tool);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setAlignmentX(LEFT_ALIGNMENT);
            // As wide as the palette, so the four buttons form one even column
            button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getPreferredSize().height));
            button.addActionListener(event -> tool = option);
            group.add(button);
            palette.add(button);
            palette.add(Box.createVerticalStrut(6));
        }

        JLabel hint = new JLabel("<html>Left button: draw<br>Right button: erase<br>Ghost house: locked</html>");
        hint.setForeground(Color.LIGHT_GRAY);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        palette.add(Box.createVerticalStrut(10));
        palette.add(hint);
        return palette;
    }

    /** A small preview of what the tool draws, painted with the same code as the grid. */
    private static Icon iconOf(Tool option) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(Color.BLACK);
                g.fillRect(x, y, ICON_SIZE, ICON_SIZE);
                if (option == Tool.PACMAN) {
                    drawPacman(g, x, y, ICON_SIZE);
                } else {
                    drawCell(g, option.cell, x, y, ICON_SIZE);
                }
            }

            @Override
            public int getIconWidth() {
                return ICON_SIZE;
            }

            @Override
            public int getIconHeight() {
                return ICON_SIZE;
            }
        };
    }

    /** Draws the whole map: grid lines, tiles, the locked house, the ghosts, and Pac-Man. */
    private void drawMap(Graphics g) {
        int mapPixelWidth = MAP_WIDTH * TILE;
        int mapPixelHeight = MAP_HEIGHT * TILE;

        // Faint lines first, so the player sees the tiles on the black floor; walls cover them later.
        // The last line is pulled in by one pixel, otherwise it would fall just outside the grid.
        g.setColor(GRID_LINE);
        for (int x = 0; x <= MAP_WIDTH; x++) {
            int px = Math.min(x * TILE, mapPixelWidth - 1);
            g.drawLine(px, 0, px, mapPixelHeight - 1);
        }
        for (int y = 0; y <= MAP_HEIGHT; y++) {
            int py = Math.min(y * TILE, mapPixelHeight - 1);
            g.drawLine(0, py, mapPixelWidth - 1, py);
        }

        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                if (isLocked(x, y) && grid[y][x] == CellType.EMPTY) {
                    g.setColor(LOCKED_FLOOR);
                    g.fillRect(x * TILE, y * TILE, TILE, TILE);
                }
                drawCell(g, grid[y][x], x * TILE, y * TILE, TILE);
            }
        }
        for (Ghost ghost : ghosts) {
            drawGhost(g, ghost.getPersonality(), ghost.getX() * TILE, ghost.getY() * TILE, TILE);
        }
        if (pacmanStart != null) {
            drawPacman(g, pacmanStart.getX() * TILE, pacmanStart.getY() * TILE, TILE);
        }
    }

    /**
     * Draws a tile's content in the size x size square whose top-left corner is (x, y).
     * At size 32 the dots and pellets come out exactly as in the GamePanel.
     */
    private static void drawCell(Graphics g, CellType cell, int x, int y, int size) {
        switch (cell) {
            case WALL -> {
                g.setColor(GamePanel.WALL_COLOR);
                g.fillRect(x, y, size, size);
            }
            case DOT -> fillCentredCircle(g, Color.WHITE, x, y, size, size / 4);
            case POWERPELLET -> fillCentredCircle(g, Color.WHITE, x, y, size, size / 2);
            case EMPTY -> {
            }
        }
    }

    private static void fillCentredCircle(Graphics g, Color color, int x, int y, int size, int diameter) {
        g.setColor(color);
        int offset = (size - diameter) / 2;
        g.fillOval(x + offset, y + offset, diameter, diameter);
    }

    /** Pac-Man facing right, as in the GamePanel. */
    private static void drawPacman(Graphics g, int x, int y, int size) {
        g.setColor(Color.YELLOW);
        g.fillArc(x + 2, y + 2, size - 4, size - 4, 45, 270);
    }

    private static void drawGhost(Graphics g, GhostPersonality personality, int x, int y, int size) {
        g.setColor(GamePanel.colorOf(personality));
        g.fillOval(x + 2, y + 2, size - 4, size - 4);
    }

    /**
     * The drawable grid. A separate component, so its pixel (0, 0) is the map's top-left corner,
     * and a mouse position divided by TILE is directly a column and a row.
     */
    private class GridView extends JPanel {
        GridView() {
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(MAP_WIDTH * TILE, MAP_HEIGHT * TILE));

            // One adapter for both kinds of event: a press draws on one tile, a drag on every tile it crosses
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    handle(event);
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    handle(event);
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private void handle(MouseEvent event) {
            // A drag may leave the grid. Integer division rounds towards zero, so a pixel of -5
            // would give column 0 and draw on the edge; check the pixel itself before dividing.
            if (event.getX() < 0 || event.getY() < 0) {
                return;
            }
            // SwingUtilities checks the buttons held down, which also works during a drag
            // (MouseEvent.getButton() reports no button while dragging)
            boolean erase = SwingUtilities.isRightMouseButton(event);
            if (!erase && !SwingUtilities.isLeftMouseButton(event)) {
                return; // e.g. the middle button
            }
            editTile(event.getX() / TILE, event.getY() / TILE, erase);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawMap(g);
        }
    }
}
