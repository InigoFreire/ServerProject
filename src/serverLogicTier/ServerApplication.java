package serverLogicTier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import userLogicTier.model.User;

public class ServerApplication extends Thread{

    private static final int PUERTO = 5000;
    private static final int MAX_HILOS = 10; 
    public volatile Integer contadorHilos = 0; 
    

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

                 
                    incrementarContadorHilos();
                } else {                    
                    Thread.sleep(1000); 
                }
            }
        } catch (IOException | InterruptedException e) {
            
        }
    }

    public synchronized void incrementarContadorHilos() {
        contadorHilos++;
    }

    
    public synchronized void decrementarContadorHilos() {
        contadorHilos--;
    }
    
    public boolean lectorTeclado(){
    HiloLector hilolector= new HiloLector();
            
    new Thread(hilolector).start();
    
    return hilolector.isClosed();
           
            }
}
