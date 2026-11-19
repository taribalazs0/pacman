package hu.bme.pacman.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import hu.bme.pacman.game.GameEngine;
import hu.bme.pacman.game.LevelData;

/**
 * The start screen: four buttons, each asking the owning MainFrame to switch screens.
 * "Start new game" and "Load previous game" also pick and load the file; MainFrame then shows the game.
 */
public class MainMenu extends JPanel {
    private static final Dimension PANEL_SIZE = new Dimension(480, 560);
    private static final Dimension BUTTON_SIZE = new Dimension(220, 40);

    private final JButton startBtn = new JButton("Start new game");
    private final JButton loadGameBtn = new JButton("Load previous game");
    private final JButton leaderboardBtn = new JButton("Leaderboard");
    private final JButton editorBtn = new JButton("Create map");

    public MainMenu(MainFrame frame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.BLACK);
        setPreferredSize(PANEL_SIZE);

        startBtn.addActionListener(event -> startNewGame(frame));
        loadGameBtn.addActionListener(event -> loadGame(frame));
        leaderboardBtn.addActionListener(event -> frame.switchScreen(MainFrame.SCREEN_LEADERBOARD));
        editorBtn.addActionListener(event -> frame.openEditor());

        JLabel title = new JLabel("PAC-MAN");
        title.setForeground(Color.YELLOW);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        title.setAlignmentX(CENTER_ALIGNMENT);

        // Vertical glue above and below the buttons pushes them to the middle of the panel.
        add(Box.createVerticalGlue());
        add(title);
        add(Box.createVerticalStrut(30));
        addCentered(startBtn);
        add(Box.createVerticalStrut(12));
        addCentered(loadGameBtn);
        add(Box.createVerticalStrut(12));
        addCentered(leaderboardBtn);
        add(Box.createVerticalStrut(12));
        addCentered(editorBtn);
        add(Box.createVerticalGlue());
    }

    /**
     * "Start new game": the player picks a map file (the dialog opens in the maps folder with
     * level1.txt selected), then a fresh game starts on it: score 0, 3 lives, level 1.
     * Cancelling does nothing; a missing or invalid map only shows an error, the menu stays.
     */
    private void startNewGame(MainFrame frame) {
        JFileChooser chooser = MapEditor.createMapChooser();
        chooser.setDialogTitle("Choose a map");
        chooser.setSelectedFile(new File(LevelManager.MAPS_DIR, LevelManager.DEFAULT_MAP_NAME));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            LevelData level = LevelManager.loadFromCSV(chooser.getSelectedFile().getPath());
            frame.startGame(new GameEngine(level));
        } catch (IOException | IllegalArgumentException e) {
            // IllegalArgumentException: the file exists but is not a valid map (the message names the line)
            JOptionPane.showMessageDialog(this, "Could not load the map: " + e.getMessage(),
                    "Load failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Lets the player browse for any saved game (the dialog opens in the saves folder) and
     * continues it. Cancelling does nothing; a bad file only shows an error, the menu stays.
     */
    private void loadGame(MainFrame frame) {
        JFileChooser chooser = GamePanel.createSaveChooser();
        chooser.setDialogTitle("Load game");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            GameEngine engine = GameEngine.loadGameState(chooser.getSelectedFile().getPath());
            frame.startGame(engine);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Could not load the saved game: " + e.getMessage(),
                    "Load failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Fixes the button's width/height and centres it on the X axis of this BoxLayout. */
    private void addCentered(JButton button) {
        button.setMaximumSize(BUTTON_SIZE);
        button.setPreferredSize(BUTTON_SIZE);
        button.setAlignmentX(CENTER_ALIGNMENT);
        add(button);
    }
}
