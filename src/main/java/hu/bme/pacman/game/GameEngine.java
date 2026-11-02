package hu.bme.pacman.game;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;

public class GameEngine extends JPanel 
{
    private Timer timer;
    private int pacmanX = 100;

    public GameEngine() 
    {
        // classic black background
        setBackground(Color.BLACK); 
        
        // gamee loop : ~60 FPS (1000ms / 16ms)
        timer = new Timer(16, e -> updateGame());
        timer.start();
    }

    private void updateGame() 
    {
        // later this is where we will move pacman and the ghosts
        pacmanX += 1; 

        // comes back if it goes out ouf the screen
        if(pacmanX > 700) pacmanX = -40;
        
        // this call PaintComponent()
        repaint(); 
    }

    @Override
    protected void paintComponent(Graphics g) 
    {
        // deletes previous frame
        super.paintComponent(g); 
        
        // Test: draw a pac-man figure (yellow, 40x40 pixel, open mouth)
        g.setColor(Color.YELLOW);
        g.fillArc(pacmanX, 100, 40, 40, 45, 270);
    }
}