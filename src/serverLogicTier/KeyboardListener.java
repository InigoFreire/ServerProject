package serverLogicTier;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * The KeyboardListener class monitors console input to allow for a server shutdown command.
 * It listens for the "close" command in a separate thread, and when received, initiates the shutdown process
 * by stopping the server loop and releasing all associated resources in ServerApplication.
 *
 * Usage:
 *     Thread keyboardThread = new Thread(new KeyboardListener());
 *     keyboardThread.start();
 * 
 * @see ServerApplication
 */
public class KeyboardListener implements Runnable {

    /**
     * Listens for console input continuously until the "close" command is entered.
     * When "close" is entered, this method:
     *  - Sets ServerApplication.isRunning to false to stop the server loop.
     *  - Calls ServerApplication.shutDownServer() to release server resources.
     * Once "close" is entered, the loop exits and the scanner is closed.
     */
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
    
        while (ServerApplication.isRunning) {
            String input = scanner.nextLine();
            if ("close".equalsIgnoreCase(input)) {
                ServerApplication.stopRunning(); // Stop server loop
                try {
                    ServerApplication.shutDownServer(); // Free resources
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        scanner.close();
    }
}
