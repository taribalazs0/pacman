package hu.bme.pacman.game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import hu.bme.pacman.system.GamePanel;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Pac-Man");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(new GamePanel());
            window.pack();
            window.setResizable(false);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
