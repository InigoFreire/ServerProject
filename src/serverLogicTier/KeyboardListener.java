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

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
    
          while (ServerApplication.isRunning) {
            String input = scanner.nextLine();
            if ("close".equalsIgnoreCase(input)) {

                ServerApplication.stopRunning();
                try {
                    // Closes server´s resources
                    ServerApplication.shutDownServer();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        scanner.close();
    }
}
