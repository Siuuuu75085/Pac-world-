import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private Game game;
    private Timer repaintTimer;
    
    public GamePanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(game.getBoard().getBoardWidth(), game.getBoard().getBoardHeight()));
        setBackground(Color.BLACK);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                game.handleKeyPress(e.getKeyCode());
            }
        });
        
        // Add a timer to continuously repaint the panel
        repaintTimer = new Timer(16, e -> repaint());
        repaintTimer.start();
        
        setFocusable(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.render(g);
    }
    
    // Stop the timer when the panel is removed
    public void stopTimer() {
        if (repaintTimer != null) {
            repaintTimer.stop();
        }
    }
}