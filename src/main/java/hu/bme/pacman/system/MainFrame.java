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

    public MainFrame() {
        super("Pac-Man");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardPanel.add(new MainMenu(this), SCREEN_MENU);
        // Placeholders until MapEditor and the leaderboard screen are built out (they are JPanel-less stubs today).
        cardPanel.add(new JPanel(), SCREEN_EDITOR);
        cardPanel.add(new JPanel(), SCREEN_LEADERBOARD);

        add(cardPanel);
        switchScreen(SCREEN_MENU);

        pack();
        setLocationRelativeTo(null);
    }

    /** Shows the named screen; every other card registered in the CardLayout is hidden. */
    public void switchScreen(String screenName) {
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
