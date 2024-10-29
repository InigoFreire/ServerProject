package dataAccessTier;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level; 
import java.util.logging.Logger;

public class KeyStrokeListenerThread extends Thread implements KeyListener {
    private static final Logger logger = Logger.getLogger(KeyStrokeListenerThread.class.getName());
    @Override
    public void run() {
        while (true) {
            try {
                int keyCode = System.in.read();
                if (keyCode == KeyEvent.VK_ESCAPE) {    
                    System.exit(0);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error en KeyStrokeListener: {0}", e.getMessage());    
            }
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }
    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyPressed'");
    }
    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyReleased'");
    }
}