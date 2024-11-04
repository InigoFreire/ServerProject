package serverLogicTier;

import java.util.Scanner;

/**
 * The {@code KeyboardListener} class listens for keyboard input from the user.
 * It is used to monitor the console for a specific command ("close") to stop the server.
 * <p>
 * When the "close" command is detected, {@link ServerApplication#stopRunning()} is called to 
 * set the server's {@code isRunning} flag to {@code false}, prompting the server to shut down.
 * </p>
 * <p>
 * This class implements the {@link Runnable} interface, so it can be run in a separate thread.
 * </p>
 * 
 * <p><b>Usage:</b></p>
 * <pre>
 *     Thread inputThread = new Thread(new KeyboardListener());
 *     inputThread.start();
 * </pre>
 * 
 * @see ServerApplication
 */
public class KeyboardListener implements Runnable {

    /**
     * Listens for console input and stops the server when the "close" command is entered.
     * <p>
     * This method continuously reads user input from the console. If the user types "close" 
     * (case-insensitive), it invokes {@link ServerApplication#stopRunning()} to notify the server
     * to stop running, and then exits the loop.
     * </p>
     */
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
     
        while (ServerApplication.isRunning) {
            String input = scanner.nextLine();
            if ("close".equalsIgnoreCase(input)) {
                ServerApplication.stopRunning(); // Notify server to stop
                break;
            }
        }
        scanner.close();
    }
}
