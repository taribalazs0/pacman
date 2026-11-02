package hu.bme.pacman.game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main 
{
    public static void main(String[] args) 
    {
        // the graphical methods of Swing
        SwingUtilities.invokeLater(() -> 
        {
            JFrame window = new JFrame("Pac-Man");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // here we will have the gamespace (GameEngine, JPanel)
            
            window.setSize(700, 750);
            window.setResizable(false);
            // set window to the middle of the screen
            window.setLocationRelativeTo(null); 
            window.setVisible(true);
        });
    }
}