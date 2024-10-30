package userLogicTier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import userLogicTier.model.User;

public class ServerApplication {

    private static final int PUERTO = 5000;
    private static final int MAX_HILOS = 10; // Máximo de hilos permitidos
    
    private volatile boolean esc = false; // Control de finalización del servidor
    private Integer contadorHilos = 0; // Contador de hilos
    private static final Logger logger = Logger.getLogger(ServerApplication.class.getName());

    public static void main(String[] args) {
        ServerApplication server = new ServerApplication();
        server.iniciar();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            logger.log(Level.INFO, "Servidor iniciado en el puerto {0}", PUERTO);

            // Bucle principal para aceptar conexiones
            while (!esc) {
                if (contadorHilos < MAX_HILOS) { // Limita la creación de nuevos hilos
                    // Acepta la conexión del cliente
                    Socket clienteSocket = serverSocket.accept();
                    logger.log(Level.INFO, "Cliente conectado: {0}", clienteSocket.getInetAddress());

                    // Crea y lanza un nuevo hilo worker
                    WorkThread worker = new WorkThread(clienteSocket);
                    new Thread(worker).start();

                    // Incrementa el contador de hilos
                    incrementarContadorHilos();
                } else {
                    logger.log(Level.INFO, "Número máximo de conexiones alcanzado. Rechazando nuevas conexiones.");
                    Thread.sleep(1000); // Espera antes de intentar aceptar nuevas conexiones
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Error en el servidor: {0}", e.getMessage());
        }
    }

    // Método synchronized para incrementar el contador de hilos
    private synchronized void incrementarContadorHilos() {
        contadorHilos++;
    }

    // Método synchronized para decrementar el contador de hilos
    private synchronized void decrementarContadorHilos() {
        contadorHilos--;
    }
}
