package hu.bme.pacman.system;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The top-level window. It owns a CardLayout and swaps which screen is visible
 * (menu, game, editor, leaderboard). It does not know any rule of the game itself.
 */
public class MainFrame extends JFrame {
    public static final String SCREEN_MENU = "MENU";
    public static final String SCREEN_GAME = "GAME";
    public static final String SCREEN_EDITOR = "EDITOR";
    public static final String SCREEN_LEADERBOARD = "LEADERBOARD";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    // Kept as a field (not just added to cardPanel and forgotten) because switchScreen needs
    // to call refresh() on this exact instance right before the leaderboard becomes visible.
    private final LeaderboardPanel leaderboardPanel = new LeaderboardPanel(this);

    public MainFrame() {
        super("Pac-Man");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardPanel.add(new MainMenu(this), SCREEN_MENU);
        // Placeholder until MapEditor is built out (it is a JPanel-less stub today).
        cardPanel.add(new JPanel(), SCREEN_EDITOR);
        cardPanel.add(leaderboardPanel, SCREEN_LEADERBOARD);

        add(cardPanel);
        switchScreen(SCREEN_MENU);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Shows the named screen; every other card registered in the CardLayout is hidden.
     *
     * DESIGN DECISION: switching to the leaderboard screen is the one special case, because
     * highscores.csv may have changed since this screen was last shown (a game may have just
     * ended). Refreshing here, in the one place every screen switch goes through, means no
     * button anywhere else needs to remember to call leaderboardPanel.refresh() itself.
     */
    public void switchScreen(String screenName) {
        if (SCREEN_LEADERBOARD.equals(screenName)) {
            leaderboardPanel.refresh();
        }
        cardLayout.show(cardPanel, screenName);
    }

    /**
     * Registers a freshly built game screen under SCREEN_GAME and shows it.
     * Called with a new GamePanel for "Start new game", or with one built from
     * a loaded GameEngine once GameEngine.loadGameState exists, for "Load previous game".
     */
    public void startGame(GamePanel gamePanel) {
        cardPanel.add(gamePanel, SCREEN_GAME);
        switchScreen(SCREEN_GAME);
        // The frame was packed to the menu's size; now that a (likely larger) GamePanel
        // is showing, resize the window to fit it.
        pack();
        gamePanel.requestFocusInWindow();
    }
}
