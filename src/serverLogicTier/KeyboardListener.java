package serverLogicTier;

import java.sql.SQLException;
import java.util.Scanner;


public class KeyboardListener implements Runnable {

  
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
      ServerApplication ServerApplication = new ServerApplication();
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
