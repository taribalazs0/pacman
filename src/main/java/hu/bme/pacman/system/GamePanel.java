package hu.bme.pacman.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import javax.swing.JPanel;
import javax.swing.Timer;

import hu.bme.enums.CellType;
import hu.bme.enums.Directions;
import hu.bme.pacman.game.GameEngine;
import hu.bme.pacman.game.Ghost;
import hu.bme.pacman.game.Labyrinth;
import hu.bme.pacman.game.LevelData;
import hu.bme.pacman.game.MoveCommand;
import hu.bme.pacman.game.PacMan;

/**
 * The screen. It asks the engine what the world looks like, then draws it.
 * It never decides whether a move is legal.
 */

public class GamePanel extends JPanel {
    private static final int TILE = 32;
    private static final int HUD = 36;

    // Default level/map
    private static final String DEFAULT_LEVEL = "maps/level1.txt";

    private final GameEngine engine;

    public GamePanel() {
        this(createDefaultEngine());
    }


    public GamePanel(GameEngine engine) {
        this.engine = engine;
        setBackground(Color.BLACK);
        setFocusable(true);

        /* Loading the map: Original, or edited */
        Labyrinth map = engine.getMap();


        setPreferredSize(new Dimension(map.getWidth() * TILE, map.getHeight() * TILE + HUD));

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                Directions chosen = switch (event.getKeyCode()) {
                    case KeyEvent.VK_W -> Directions.UP;
                    case KeyEvent.VK_S -> Directions.DOWN;
                    case KeyEvent.VK_A -> Directions.LEFT;
                    case KeyEvent.VK_D -> Directions.RIGHT;
                    default -> null;
                };
                if (chosen != null) {
                    PacMan pacman = engine.getPacman();
                    pacman.setCurrentCommand(new MoveCommand(pacman, engine.getMap(), chosen));
                }
            }
        });

        // One engine tick per frame, about 60 times a second (1000 / 60 = 16 ms).
        // The engine moves the actors one tile every TICKS_PER_STEP ticks; in between we only redraw.
        Timer timer = new Timer(1000 / GameEngine.TICKS_PER_SECOND, event -> {
            engine.update();
            repaint();
        });
        timer.start();
    }

    /**
     * Where an actor is drawn along one axis, in pixels from the edge of the maze.
     * progress 0.0 gives the previous tile, 1.0 the current tile, values in between slide from one to the other.
     */
    private static int interpolate(int previous, int current, double progress) {
        double tile = previous + (current - previous) * progress;
        // Math.round(double) gives a long, so it has to be narrowed back to int
        return (int) Math.round(tile * TILE);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Read once, so every actor in this frame is drawn at the same moment of the step
        double stepProgress = engine.getStepProgress();
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + engine.getScore() + "    Lives: " + engine.getLives(), 12, 24);

        Labyrinth map = engine.getMap();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                drawTile(g, x, y, map.getCell(x, y));
            }
        }
        for (Ghost ghost : engine.getGhosts()) {
            drawGhost(g,
                    interpolate(ghost.getPrevX(), ghost.getX(), stepProgress),
                    interpolate(ghost.getPrevY(), ghost.getY(), stepProgress));
        }
        PacMan pacman = engine.getPacman();
        drawPacman(g,
                interpolate(pacman.getPrevX(), pacman.getX(), stepProgress),
                interpolate(pacman.getPrevY(), pacman.getY(), stepProgress),
                pacman.getDirection());
    }

    private void drawTile(Graphics g, int column, int row, CellType cell) {
        int pixelX = column * TILE;
        int pixelY = HUD + row * TILE;
        switch (cell) {
            case WALL -> {
                g.setColor(new Color(33, 33, 222));
                g.fillRect(pixelX, pixelY, TILE, TILE);
            }
            case DOT -> {
                g.setColor(Color.WHITE);
                g.fillOval(pixelX + 12, pixelY + 12, 8, 8);
            }
            case POWERPELLET -> {
                g.setColor(Color.WHITE);
                g.fillOval(pixelX + 8, pixelY + 8, 16, 16);
            }
            case EMPTY -> {
            }
        }
    }

    /** mazeX, mazeY: the top-left corner of Pac-Man's tile in pixels, measured from the maze, not the panel. */
    private void drawPacman(Graphics g, int mazeX, int mazeY, Directions direction) {
        int pixelX = mazeX + 2;
        int pixelY = HUD + mazeY + 2;
        g.setColor(Color.YELLOW);
        int startAngle = switch (direction) {
            case RIGHT -> 45;
            case UP -> 135;
            case LEFT -> 225;
            case DOWN -> 315;
            case IDLE -> 0;
        };
        int arc = direction == Directions.IDLE ? 360 : 270;
        g.fillArc(pixelX, pixelY, TILE - 4, TILE - 4, startAngle, arc);
    }

    /** mazeX, mazeY: the top-left corner of the ghost's tile in pixels, measured from the maze, not the panel. */
    private void drawGhost(Graphics g, int mazeX, int mazeY) {
        int pixelX = mazeX + 2;
        int pixelY = HUD + mazeY + 2;
        g.setColor(Color.RED);
        g.fillOval(pixelX, pixelY, TILE - 4, TILE - 4);
    }

    /**
    *  Loads the default map and builds a new game on it.
    * Loading belongs to the UI side; the model never touches files.
    * static: it runs inside this(...), before the GamePanel object exists.
    */
    private static GameEngine createDefaultEngine() {
        try {
            LevelData level = LevelManager.loadFromCSV(DEFAULT_LEVEL);
            return new GameEngine(level);
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Could not load the default map: " + Path.of(DEFAULT_LEVEL).toAbsolutePath(), e);
        }
}


}
