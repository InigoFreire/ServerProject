/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userLogicTier;

import java.net.ServerSocket;
import java.util.logging.Logger;

import dataAccessTier.KeyStrokeListenerThread;
import dataAccessTier.WorkThread;

/**
 *
 * @author inifr
 */
public abstract class ServerApplication {

    private ServerSocket socket;
    private WorkThread worker;
    private Logger logger;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    }
    
}