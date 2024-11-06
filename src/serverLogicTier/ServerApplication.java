package serverLogicTier;

import com.sun.security.auth.login.ConfigFile;
import dataAccessTier.Pool;
import dataAccessTier.WorkThread;
import exceptions.ServerException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code ServerApplication} class represents the main server application responsible for
 * loading configuration, managing client connections, and maintaining a connection pool.
 * <p>
 * This class initializes a server that listens for incoming client connections, processes
 * them using worker threads, and manages the number of concurrent threads.
 * Configuration values such as the server port and maximum number of threads are loaded from a
 * properties file.
 * </p>
 * 
 * <p><b>Usage:</b></p>
 * <pre>
 *     ServerApplication server = new ServerApplication();
 *     server.loadConfig();
 *     server.startServer();
 * </pre>
 * 
 * @author Pablo
 * @version 1.0
 * @see Pool
 * @see WorkThread
 * @see ServerSocket
 */
public class ServerApplication {

    private static int port;
    private static int maxThreads;
    private static int threadCounter = 0; 
    private static ServerSocket serverSocket;
    public static volatile boolean isRunning = true;
    private static final Logger logger = Logger.getLogger(ServerApplication.class.getName());

    /**
     * Main entry point for the {@code ServerApplication}.
     * Loads the server configuration and starts the server if configuration is successful.
     *
     * @param args the command line arguments
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
     * Loads the server configuration from a {@code .properties} file.
     * Retrieves values for the server port and maximum number of threads.
     */
    public void loadConfig() {
        ResourceBundle configFile = ResourceBundle.getBundle("resources.config");
        port = Integer.parseInt(configFile.getString("PORT"));
        maxThreads = Integer.parseInt(configFile.getString("MAX_THREADS"));
    }

    /**
     * Stops the server by setting the {@code isRunning} flag to {@code false}.
     * This flag will stop the main loop in {@link #startServer()} and shut down the server.
     */
    public static void stopRunning() {
        ServerApplication.isRunning = false;
    }

    /**
     * Shuts down the server and releases all resources.
     * Closes the {@code ServerSocket} and connection pool.
     *
     * @throws SQLException if a database access error occurs during pool shutdown
     */
    public static void shutDownServer() throws SQLException {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Close the ServerSocket
            }
            Pool.close(); // Close the connection pools       
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server, initializes the {@code ServerSocket} and connection pool,
     * and enters the main loop to accept and handle client connections.
     * The server will continue running until {@code isRunning} is set to {@code false}.
     *
     * @throws ServerException if a database access error occurs
     * @throws SQLException if a database access error occurs during pool initialization
     */
    public void startServer() throws SQLException, ServerException, InterruptedException, IOException {

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
                WorkThread worker = new WorkThread(clientSocket,this);
                logger.log(Level.INFO, "Worker thread created");
                new Thread(worker).start();
                incrementThreadCounter();
                logger.log(Level.INFO, "Worker count (+1) =", threadCounter);
            } else {                    
                Thread.sleep(1000); // Wait if max threads reached

            }
            shutDownServer();
        }
    } catch (IOException | InterruptedException e) {
            // Handle exceptions
        } finally {
            shutDownServer(); // Close resources when server stops
        }
    }

    /**
     * Increments the thread counter, which tracks the number of active client threads.
     * Ensures that new threads can only be created if {@code threadCounter} is below {@code maxThreads}.
     */
    public synchronized void incrementThreadCounter() {
        if (getThreadCounter() < getMaxThreads()){
            threadCounter++;
        }
    }

    /**
     * Decrements the thread counter, used when a client thread completes its work.
     * Ensures that {@code threadCounter} does not go below zero.
     */
    public synchronized void decrementThreadCounter() {
        if (getThreadCounter() > 0){
            threadCounter--;
        }
    }

    /**
     * Gets the current thread counter value.
     * 
     * @return the current count of active threads
     */
    public int getThreadCounter(){
        return threadCounter;
    }

    /**
     * Sets the thread counter value.
     *
     * @param threadCounterInput the new count for active threads
     */
    public void setThreadCounter(int threadCounterInput){
        this.threadCounter = threadCounterInput;
    }

    /**
     * Gets the maximum number of allowed threads.
     * 
     * @return the maximum number of threads
     */
    public int getMaxThreads(){
        return maxThreads;
    }

    /**
     * Sets the maximum number of allowed threads.
     *
     * @param maxThreadsInput the maximum number of threads allowed
     */
    public void setMaxThreads(int maxThreadsInput){
        this.maxThreads = maxThreadsInput;
    }
}
