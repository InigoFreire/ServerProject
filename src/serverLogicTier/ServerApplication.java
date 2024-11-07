package serverLogicTier;

import com.sun.security.auth.login.ConfigFile;
import dataAccessTier.Pool;
import dataAccessTier.WorkThread;
import exceptions.ServerException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ServerApplication class serves as the main server program that loads configuration settings, 
 * manages client connections, and maintains a connection pool. 
 * It sets up a server that listens for incoming client connections, processes them with worker threads, 
 * and controls the maximum number of concurrent threads based on configuration.
 * 
 * Usage:
 * ServerApplication server = new ServerApplication();
 * server.loadConfig();
 * server.startServer();
 * 
 * @see Pool
 * @see WorkThread
 * @see ServerSocket
 */
public class ServerApplication {

    /** The port on which the server listens for incoming client connections. */
    private static int port;

    /** The maximum number of threads allowed to process client connections concurrently. */
    private static int maxThreads;

    /** Counter to track the number of active client threads. */
    private static int threadCounter = 0;

    /** The server socket that listens for incoming client connections. */
    private static ServerSocket serverSocket;

    /** Flag indicating whether the server is running. */
    public static volatile boolean isRunning = true;

    /** Logger for logging messages in the server application. */
    private static final Logger logger = Logger.getLogger(ServerApplication.class.getName());

    /**
     * Main entry point for ServerApplication.
     * Loads configuration and starts the server if configuration is successful.
     *
     * @param args command line arguments
     * @throws SQLException if a database access error occurs
     */
    public static void main(String[] args) throws SQLException {
        logger.log(Level.INFO, "Server Application started");
        ServerApplication server = new ServerApplication();
        server.loadConfig();
        logger.log(Level.INFO, "Server Application loaded correctly");
        try {
            server.startServer();
        } catch (ServerException e) {
            // Handle ServerException
        }
    }

    /**
     * Loads server configuration from a .properties file, 
     * including server port and maximum number of threads.
     */
    public void loadConfig() {
        ResourceBundle configFile = ResourceBundle.getBundle("resources.config");
        port = Integer.parseInt(configFile.getString("PORT"));
        maxThreads = Integer.parseInt(configFile.getString("MAX_THREADS"));
    }

    /**
     * Stops the server by setting isRunning to false, 
     * stopping the main loop in startServer() and shutting down the server.
     */
    public static void stopRunning() {
        ServerApplication.isRunning = false;  // Establece isRunning en false directamente en ServerApplication
    }

    /**
     * Shuts down the server and releases all resources. Closes the ServerSocket 
     * and connection pool, ensuring proper cleanup.
     * 
     * @throws SQLException if a database access error occurs during pool shutdown
     */
    public static void shutDownServer() throws SQLException {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            Pool.close();   
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server, initializes the ServerSocket and connection pool, 
     * and continuously accepts and processes client connections until isRunning is set to false.
     *
     * @throws ServerException if a database access error occurs
     * @throws SQLException if a database access error occurs during pool initialization
     */

    public void startServer() throws SQLException, ServerException {
        try {
            // Initialize ServerSocket and Connection Pool
            serverSocket = new ServerSocket(port);
            logger.log(Level.INFO, "Port acquired");
            Pool.getDatabaseCredentials(); // Configure connection pool
            logger.log(Level.INFO, "Pool credentials acquired");

            // Start thread to monitor keyboard input
            Thread inputThread = new Thread(new KeyboardListener());
            inputThread.start();
            logger.log(Level.INFO, "InputListener thread started");

            // Main loop to accept client connections
            while (isRunning) {
                if (threadCounter < maxThreads) {
                    Socket clientSocket = serverSocket.accept();
                    logger.log(Level.INFO, "Incoming socket connection accepted");
                    WorkThread worker = new WorkThread(clientSocket, this);
                    logger.log(Level.INFO, "Worker thread created");
                    new Thread(worker).start();
                    incrementThreadCounter();
                    logger.log(Level.INFO, "Worker count (+1) =", threadCounter);
                } else {
                    Thread.sleep(1000); // Wait if max threads reached
                }
            }
            shutDownServer();
        } catch (IOException | InterruptedException e) {
            //
        } finally {
            // Close resources when server stops
            shutDownServer();
        }
    }

    /**
     * Increments the thread counter, which tracks the number of active client threads.
     * New threads can only be created if threadCounter is below maxThreads.
     */
    public synchronized void incrementThreadCounter() {
        if (getThreadCounter() < getMaxThreads()) {
            threadCounter++;
        }
    }

    /**
     * Decrements the thread counter, used when a client thread completes its work.
     * Ensures threadCounter does not go below zero.
     */
    public synchronized void decrementThreadCounter() {
        if (getThreadCounter() > 0) {
            threadCounter--;
        }
    }

    /**
     * Returns the current thread counter value.
     * 
     * @return the current count of active threads
     */
    public int getThreadCounter() {
        return threadCounter;
    }

    /**
     * Sets the thread counter value.
     *
     * @param threadCounterInput the new count for active threads
     */
    public void setThreadCounter(int threadCounterInput) {
        this.threadCounter = threadCounterInput;
    }

    /**
     * Returns the maximum number of allowed threads.
     * 
     * @return the maximum number of threads
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * Sets the maximum number of allowed threads.
     *
     * @param maxThreadsInput the maximum number of threads allowed
     */
    public void setMaxThreads(int maxThreadsInput) {
        this.maxThreads = maxThreadsInput;
    }
}
