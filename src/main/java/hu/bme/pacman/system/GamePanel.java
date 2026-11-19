package hu.bme.pacman.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import hu.bme.enums.CellType;
import hu.bme.enums.Directions;
import hu.bme.enums.GhostPersonality;
import hu.bme.enums.GhostState;
import hu.bme.pacman.game.GameEngine;
import hu.bme.pacman.game.Ghost;
import hu.bme.pacman.game.Labyrinth;
import hu.bme.pacman.game.MoveCommand;
import hu.bme.pacman.game.PacMan;

/**
 * The screen. It asks the engine what the world looks like, then draws it.
 * It never decides whether a move is legal.
 */

public class GamePanel extends JPanel {
    private static final int TILE = 32;
    private static final int HUD = 36;
    /** Extra pixels on top of the SAVE & QUIT button's measured width (see the constructor). */
    private static final int BUTTON_SPARE_WIDTH = 24;
    /** Colour of a wall tile. Package-private: the MapEditor draws walls the same way. */
    static final Color WALL_COLOR = new Color(33, 33, 222);

    private final GameEngine engine;
    // Guards the highscore write: isGameOver() stays true on every tick after the game ends,
    // so without this flag the same score would be appended to highscores.csv ~60 times a second.
    private boolean highscoreSaved;
    // A field (not a local variable) so the game can be paused while the save dialog is open,
    // and stopped for good when the player leaves this screen.
    private final Timer timer;
    // What to do after a successful save. GamePanel does not know MainFrame; the frame passes
    // in "go back to the menu" as a callback, so this panel only reports that the player quit.
    private final Runnable onQuit;

    /**
     * A game on an existing engine: a new one built from the map chosen in the menu,
     * or one loaded from a save file. Loading files is the menu's job, not this panel's.
     */
    public GamePanel(GameEngine engine, Runnable onQuit) {
        this.engine = engine;
        this.onQuit = onQuit;
        setBackground(Color.BLACK);
        setFocusable(true);

        /* Loading the map: Original, or edited */
        Labyrinth map = engine.getMap();

        int panelWidth = map.getWidth() * TILE;
        setPreferredSize(new Dimension(panelWidth, map.getHeight() * TILE + HUD));

        // ARCHITECTURE DECISION: paintComponent only draws (Score/Lives/Level text, the maze,
        // the actors); a real, clickable JButton cannot be drawn with Graphics, it has to be an
        // actual child component. setLayout(null) switches this panel to absolute positioning,
        // so the button can sit in a fixed pixel spot inside the HUD strip.
        setLayout(null);
        JButton saveBtn = new JButton("SAVE & QUIT");
        saveBtn.setMargin(new Insets(2, 6, 2, 6));
        // The button's own preferred size, plus spare width. With Windows display scaling
        // (125%, 150%) Swing measures the label in whole pixels but draws it at fractional ones,
        // so the drawn text can be a few pixels wider than measured, and Swing then cuts it to "...".
        // The spare width absorbs that. The button sits at the right end of the HUD strip.
        Dimension buttonSize = saveBtn.getPreferredSize();
        int buttonWidth = buttonSize.width + BUTTON_SPARE_WIDTH;
        saveBtn.setBounds(panelWidth - buttonWidth - 8, (HUD - buttonSize.height) / 2,
                buttonWidth, buttonSize.height);
        // Without this, a click moves the keyboard focus to the button, and the panel's
        // KeyListener stops getting WASD: that is why Pac-Man froze after the old SAVE click.
        saveBtn.setFocusable(false);
        saveBtn.addActionListener(event -> saveAndQuit());
        add(saveBtn);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_F5) {
                    saveAndQuit();
                    return;
                }
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
        timer = new Timer(1000 / GameEngine.TICKS_PER_SECOND, event -> {
            engine.update();
            if (engine.isGameOver() && !highscoreSaved) {
                highscoreSaved = true;
                saveHighscore();
            }
            repaint();
        });
        timer.start();
    }

    /**
     * Writes the just-finished game's score to highscores.csv.
     *
     * DESIGN DECISION: the player's name is never asked for on screen; the spec requires
     * System.getProperty("user.name") instead, so the leaderboard always uses the OS account
     * name. Saving happens here, in the UI layer, and not inside GameEngine: the model must
     * never touch files (the only exception is GameEngine's own saveGameState).
     *
     * A failed write here does not rethrow: the game has already ended and is on screen either
     * way, and a highscores.csv problem (e.g. a locked file) is not worth crashing that screen.
     */
    private void saveHighscore() {
        String playerName = System.getProperty("user.name");
        HighscoreEntry entry = new HighscoreEntry(playerName, engine.getScore(), LocalDate.now());
        try {
            LevelManager.appendHighscore(LevelManager.HIGHSCORES_FILE, entry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asks where to save, saves, then goes back to the menu. Both F5 and the SAVE & QUIT
     * button call this, so the two can never behave differently.
     * Cancelling the dialog, or a failed write, resumes the game instead: quitting then would
     * throw the player's progress away.
     * After Game Over it goes straight back to the menu, without the dialog: see below.
     */
    private void saveAndQuit() {
        // A modal dialog still runs Swing events while it is open, Timer ticks included,
        // so without stopping the timer the ghosts would keep playing behind the dialog.
        timer.stop();

        // A finished game is not worth saving: nothing can happen in it any more, and loading it
        // would only add the same score to the leaderboard again. The score is already recorded:
        // the timer tick that ended the game wrote it, before any click could be handled, because
        // both the Timer and the button run on the same thread (the EDT), one event after the other.
        if (engine.isGameOver()) {
            onQuit.run();
            return;
        }

        File file = chooseSaveFile();
        if (file == null) {
            resume();
            return;
        }
        try {
            engine.saveGameState(file.getPath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save the game: " + e.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
            resume();
            return;
        }
        onQuit.run();
    }

    /**
     * Shows the save dialog in the saves folder, offering savestate.dat as the name.
     * Adds ".dat" if the player typed a name without it, and asks before overwriting.
     *
     * @return the file to write, or null if the player cancelled
     */
    private File chooseSaveFile() {
        JFileChooser chooser = createSaveChooser();
        chooser.setDialogTitle("Save game");
        chooser.setSelectedFile(new File(LevelManager.SAVES_DIR, LevelManager.DEFAULT_SAVE_NAME));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith("." + LevelManager.SAVE_EXTENSION)) {
            file = new File(file.getParentFile(), file.getName() + "." + LevelManager.SAVE_EXTENSION);
        }
        if (file.exists()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    file.getName() + " already exists. Overwrite it?", "Save game", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        return file;
    }

    /**
     * A file chooser that opens in the saves folder and shows only .dat files.
     * Package-private and static, because MainMenu's "Load previous game" needs the same one.
     * The folder is created if missing, otherwise the dialog would fall back to the home folder.
     */
    static JFileChooser createSaveChooser() {
        File savesDir = new File(LevelManager.SAVES_DIR);
        savesDir.mkdirs();
        JFileChooser chooser = new JFileChooser(savesDir);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Pac-Man saves (*." + LevelManager.SAVE_EXTENSION + ")", LevelManager.SAVE_EXTENSION));
        return chooser;
    }

    /** Continues the game after a cancelled or failed save, with the keyboard focus back here. */
    private void resume() {
        timer.start();
        requestFocusInWindow();
    }

    /** Stops the game loop for good. MainFrame calls it when this screen is thrown away. */
    public void stop() {
        timer.stop();
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
        g.drawString("Score: " + engine.getScore() + "    Lives: " + engine.getLives()
                + "    Level: " + engine.getCurrentLevel(), 12, 24);
        if (engine.isGameOver()) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", 300, 24);
        }

        Labyrinth map = engine.getMap();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                drawTile(g, x, y, map.getCell(x, y));
            }
        }
        for (Ghost ghost : engine.getGhosts()) {
            int ghostX = interpolate(ghost.getPrevX(), ghost.getX(), stepProgress);
            int ghostY = interpolate(ghost.getPrevY(), ghost.getY(), stepProgress);
            drawGhost(g, ghostX, ghostY, ghost.getState(), ghost.getPersonality());
        }
        PacMan pacman = engine.getPacman();
        int pacmanX = interpolate(pacman.getPrevX(), pacman.getX(), stepProgress);
        int pacmanY = interpolate(pacman.getPrevY(), pacman.getY(), stepProgress);
        drawPacman(g, pacmanX, pacmanY, pacman.getDirection());
    }

    private void drawTile(Graphics g, int column, int row, CellType cell) {
        int pixelX = column * TILE;
        int pixelY = HUD + row * TILE;
        switch (cell) {
            case WALL -> {
                g.setColor(WALL_COLOR);
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

    /** Size of a DEAD ghost's "eyes" in pixels. */
    private static final int EYES = 10;

    /**
     * mazeX, mazeY: the top-left corner of the ghost's tile in pixels, measured from the maze, not the panel.
     * The state wins over the personality: a frightened ghost is white whatever its colour,
     * and a DEAD ghost is only a small grey dot (its eyes) hurrying home.
     */
    private void drawGhost(Graphics g, int mazeX, int mazeY, GhostState state, GhostPersonality personality) {
        switch (state) {
            case FRIGHTENED -> {
                // White, so the player sees it can be eaten
                g.setColor(Color.WHITE);
                g.fillOval(mazeX + 2, HUD + mazeY + 2, TILE - 4, TILE - 4);
            }
            case DEAD -> {
                // Centre of the tile minus half the dot, so the dot sits in the middle
                g.setColor(Color.LIGHT_GRAY);
                g.fillOval(mazeX + TILE / 2 - EYES / 2, HUD + mazeY + TILE / 2 - EYES / 2, EYES, EYES);
            }
            default -> {
                g.setColor(colorOf(personality));
                g.fillOval(mazeX + 2, HUD + mazeY + 2, TILE - 4, TILE - 4);
            }
        }
    }

    /**
     * The colour of each personality.
     * Package-private: the MapEditor shows the fixed ghosts in the same colours.
     */
    static Color colorOf(GhostPersonality personality) {
        return switch (personality) {
            case TRACKER -> Color.RED;
            case AMBUSHER -> Color.PINK;
            case WANDERER -> Color.ORANGE;
            case CHAOTIC -> Color.CYAN;
        };
    }
}
