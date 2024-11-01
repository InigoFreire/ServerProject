package serverLogicTier;

import dataAccessTier.Pool;
import dataAccessTier.WorkThread;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import userLogicTier.model.User;
/**
 *
 * @author Pebble
 */


public class ServerApplication extends Thread {

    private static final int PUERTO = 5000;
    private static final int MAX_HILOS = 10; 
    public static Integer contadorHilos = 0; 
    
    public static void main(String[] args) {
        ServerApplication server = new ServerApplication();
        server.iniciar();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (!lectorTeclado()) {
                if (contadorHilos < MAX_HILOS) {

                    Socket clienteSocket = serverSocket.accept();
                    WorkThread worker = new WorkThread(clienteSocket);
                    new Thread(worker).start();

                    threadCounterUpwards();
                } else {                    
                    Thread.sleep(1000); 
                }
            }
        } catch (IOException | InterruptedException e) {
            
        }
    }

    public synchronized void threadCounterUpwards() {
        contadorHilos++;
    }

    
    public synchronized void threadCounterDownwards() {
        contadorHilos--;
    }

    public boolean lectorTeclado() {
        HiloLector hilolector = new HiloLector();

        new Thread((Runnable) hilolector).start();

        return hilolector.isClosed();

    }
    

}
