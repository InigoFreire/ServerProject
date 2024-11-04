package serverLogicTier;

import dataAccessTier.Pool;
import dataAccessTier.WorkThread;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;

public class ServerApplication {

    private static int port;
    private static int maxThreads;
    private static Integer threadCounter = 0; 
    private static ServerSocket serverSocket;
    public static volatile boolean isRunning = true;

    public static void main(String[] args) throws SQLException {
        if (loadConfiguration()) {
            ServerApplication server = new ServerApplication();
            server.startServer();
        } else {
            System.err.println("Error loading server configuration.");
        }
    }

    // Method to load configuration from the .properties file
    public static boolean loadConfiguration() {
        Properties properties = new Properties();
        try (FileInputStream configFile = new FileInputStream("serverConfig.properties")) {
            properties.load(configFile);
            port = Integer.parseInt(properties.getProperty("PORT"));
            maxThreads = Integer.parseInt(properties.getProperty("MAX_THREADS"));
            System.out.println("Configuration loaded: PORT=" + port + ", MAX_THREADS=" + maxThreads);
            return true;
        } catch (IOException | NumberFormatException e) {
            
            return false;
        }
    }

    public void startServer() throws SQLException {
        try {
            // Initialize ServerSocket and Connection Pool
            serverSocket = new ServerSocket(port);
            Pool.getDatabaseCredentials(); // Configure connection pool

            // Start thread to monitor keyboard input
            Thread inputThread = new Thread(new KeyboardListener());
            inputThread.start();

            // Main loop to accept client connections
            while (isRunning) {
                if (threadCounter < maxThreads) {
                    Socket clientSocket = serverSocket.accept();
                    WorkThread worker = new WorkThread(clientSocket);
                    new Thread(worker).start();
                    incrementThreadCounter();
                } else {                    
                    Thread.sleep(1000); // Wait if max threads reached
                }
            }
        } catch (IOException | InterruptedException e) {
         
        } finally {
            shutDownServer(); // Close resources when server stops
        }
    }

    public synchronized void incrementThreadCounter() {
        threadCounter++;
    }

    public synchronized void decrementThreadCounter() {
        threadCounter--;
    }

    // Method to shut down the server and release all resources
    public void shutDownServer() throws SQLException {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Close the ServerSocket
            }
            Pool.close(); // Close the connection pools       
        } catch (IOException e) {
           
        }
    }

    public static void stopRunning() {
        isRunning = false;
    }
}
