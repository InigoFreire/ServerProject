/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userLogicTier;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author InigoFreire
 */
public class ServerApplication {
    
    private ServerSocket socket;
    private static final ResourceBundle configFile = ResourceBundle.getBundle("config.properties");
    private static final int maxConnections = Integer.parseInt(ResourceBundle.getBundle("config.properties").getString("maxConnections"));
    private static int connections = 0;

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        ServerApplication server = new ServerApplication();
        server.startServer();
    }
    
    public void startServer(){
        try {
            int port = Integer.parseInt(configFile.getString("PORT"));
            
            
            
        }catch (IOException e){
            Logger.getLogger(ServerApplication.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
