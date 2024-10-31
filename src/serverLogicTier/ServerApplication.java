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



public class ServerApplication extends Thread {

    private static final int PUERTO = 5000;
    private static final int MAX_HILOS = 10;

    private volatile boolean esc = false;
    private Integer contadorHilos = 0;
    private static final Logger logger = Logger.getLogger(ServerApplication.class.getName());

    public static void main(String[] args) {
        ServerApplication server = new ServerApplication();
        server.iniciar();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            logger.log(Level.INFO, "Servidor iniciado en el puerto {0}", PUERTO);

            while (!lectorTeclado()) {
                if (contadorHilos < MAX_HILOS) {

                    Socket clienteSocket = serverSocket.accept();
                    logger.log(Level.INFO, "Cliente conectado: {0}", clienteSocket.getInetAddress());

                    WorkThread worker = new WorkThread(clienteSocket);
                    new Thread(worker).start();

                    incrementarContadorHilos();
                } else {
                    logger.log(Level.INFO, "Número máximo de conexiones alcanzado. Rechazando nuevas conexiones.");
                    Thread.sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Error en el servidor: {0}", e.getMessage());
        }
    }

    private synchronized void incrementarContadorHilos() {
        contadorHilos++;
    }

    private synchronized void decrementarContadorHilos() {
        contadorHilos--;
    }

    public boolean lectorTeclado() {
        HiloLector hilolector = new HiloLector();

        new Thread(hilolector).start();

        return hilolector.isClosed();

    }
}
