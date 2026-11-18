package hu.bme.pacman.game;

import javax.swing.SwingUtilities;

import hu.bme.pacman.system.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame window = new MainFrame();
            window.setVisible(true);
        });
    }
}
