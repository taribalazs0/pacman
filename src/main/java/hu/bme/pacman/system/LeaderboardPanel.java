package hu.bme.pacman.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Read-only view of highscores.csv, ranked from best to worst.
 *
 * ARCHITECTURE DECISION: this panel never writes to the file; GamePanel is the only writer
 * (right when a game ends), so this stays a plain Observer of whatever is on disk.
 * It is also never told to refresh by a timer: the leaderboard only changes between games,
 * so MainFrame.switchScreen calls refresh() once, right before this screen becomes visible.
 *
 * A single JTextArea is used instead of one JLabel per row: the number of rows is not fixed,
 * and a text area with a monospaced font lines the three columns up without extra layout code.
 */
public class LeaderboardPanel extends JPanel {
    private final JTextArea listArea = new JTextArea();

    public LeaderboardPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        listArea.setEditable(false);
        listArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        listArea.setBackground(Color.BLACK);
        listArea.setForeground(Color.YELLOW);

        JButton backBtn = new JButton("Back to menu");
        backBtn.addActionListener(event -> frame.switchScreen(MainFrame.SCREEN_MENU));

        add(new JScrollPane(listArea), BorderLayout.CENTER);
        add(backBtn, BorderLayout.SOUTH);
    }

    /**
     * Reloads highscores.csv and redraws the ranked list.
     * Call this every time the leaderboard screen is about to be shown, not just once at
     * startup: a game may have finished, and added a new row, since the last time it was open.
     */
    public void refresh() {
        List<HighscoreEntry> entries;
        try {
            entries = LevelManager.loadHighscores(LevelManager.HIGHSCORES_FILE);
        } catch (IOException e) {
            listArea.setText("Could not read the leaderboard: " + e.getMessage());
            return;
        }

        if (entries.isEmpty()) {
            listArea.setText("No scores yet. Be the first to finish a game!");
            return;
        }

        StringBuilder text = new StringBuilder();
        int rank = 1;
        for (HighscoreEntry entry : entries) {
            text.append(String.format("%2d. %-15s %6d   %s%n",
                    rank, entry.getPlayerName(), entry.getScore(), entry.getDate()));
            rank++;
        }
        listArea.setText(text.toString());
    }
}
