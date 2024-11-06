package serverLogicTier;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * The {@code KeyboardListener} class implements a listener to monitor console input.
 * It allows the server to be shut down by entering a specific command.
 * <p>
 * This class runs on a separate thread and listens for the "close" command.
 * When "close" is entered, it initiates the shutdown of {@link ServerApplication}
 * by calling the {@code stopRunning()} method and releasing all associated resources.
 * </p>
 * 
 * <p><b>Usage:</b></p>
 * <pre>
 *     Thread keyboardThread = new Thread(new KeyboardListener());
 *     keyboardThread.start();
 * </pre>
 * 
 * @author Pablo
 * @version 1.0
 * @see ServerApplication
 */
public class KeyboardListener implements Runnable {

    /**
     * Listens for console input in a loop. When the "close" command is entered, this method:
     * <ul>
     *     <li>Sets {@code ServerApplication.isRunning} to {@code false} to stop the server loop.</li>
     *     <li>Invokes {@code ServerApplication.shutDownServer()} to release server resources.</li>
     * </ul>
     * Once the command is received, the loop breaks and the {@code Scanner} is closed.
     */
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
    
        while (ServerApplication.isRunning) {
            String input = scanner.nextLine();
            if ("close".equalsIgnoreCase(input)) {
                ServerApplication.stopRunning(); // Sets isRunning to false
                try {
                    ServerApplication.shutDownServer(); // Releases server resources
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        scanner.close();
    }
}
