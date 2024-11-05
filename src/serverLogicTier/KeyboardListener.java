package serverLogicTier;

import java.sql.SQLException;
import java.util.Scanner;


public class KeyboardListener implements Runnable {

  
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
    
          while (ServerApplication.isRunning) {
            String input = scanner.nextLine();
            if ("close".equalsIgnoreCase(input)) {
                ServerApplication.stopRunning(); // Cambia isRunning a false
                try {
                    ServerApplication.shutDownServer(); // Cierra los recursos del servidor
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        scanner.close();
    }
    



}
