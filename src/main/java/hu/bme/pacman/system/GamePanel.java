package hu.bme.pacman.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.Timer;

import hu.bme.enums.CellType;
import hu.bme.enums.Directions;
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

    private final GameEngine engine;

    public GamePanel() {
        this(new GameEngine());
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

        // One tick every 160 ms. The panel tells the engine to advance, then draws the result.
        Timer timer = new Timer(160, event -> {
            engine.update();
            repaint();
        });
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
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
            drawGhost(g, ghost.getX(), ghost.getY());
        }
        PacMan pacman = engine.getPacman();
        drawPacman(g, pacman.getX(), pacman.getY(), pacman.getDirection());
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

    private void drawPacman(Graphics g, int column, int row, Directions direction) {
        int pixelX = column * TILE + 2;
        int pixelY = HUD + row * TILE + 2;
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

    private void drawGhost(Graphics g, int column, int row) {
        int pixelX = column * TILE + 2;
        int pixelY = HUD + row * TILE + 2;
        g.setColor(Color.RED);
        g.fillOval(pixelX, pixelY, TILE - 4, TILE - 4);
    }
}
