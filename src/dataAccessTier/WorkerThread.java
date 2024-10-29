package dataAccessTier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import userLogicTier.Message;
import userLogicTier.MessageType;
import userLogicTier.model.User;

public class WorkerThread implements Runnable {

    private static final Logger logger = Logger.getLogger(WorkerThread.class.getName());
    private final Socket clienteSocket;
    private final Server server;

    public Worker(Socket clienteSocket, Server server) {
        this.clienteSocket = clienteSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try (ObjectOutputStream salida = new ObjectOutputStream(clienteSocket.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(clienteSocket.getInputStream())) {

            // Lee el mensaje del cliente
            Message mensaje = (Message) entrada.readObject();
            logger.log(Level.INFO, "Mensaje recibido del cliente: {0}", mensaje.getMessageType());

            // Procesa el mensaje y obtiene la respuesta
            Message respuesta = procesarMensaje(mensaje);

            // Envía la respuesta al cliente
            salida.writeObject(respuesta);

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error en Worker: {0}", e.getMessage());
        } finally {
            try {
                clienteSocket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error al cerrar el socket del cliente: {0}", e.getMessage());
            }

            // Decrementa el contador de hilos al finalizar el hilo
            server.decrementarContadorHilos();
        }
    }

    private Message procesarMensaje(Message mensaje) {
        // Aquí se implementa la lógica para procesar el mensaje en función de su tipo
        Message respuesta = new Message(null, MessageType.SERVER_RESPONSE_DENIED);

        if (mensaje.getMessageType() == MessageType.SERVER_SIGN_UP_REQUEST) {
            User usuario = mensaje.getUser();
            if (registrarUsuarioEnBaseDeDatos(usuario)) {
                respuesta = new Message(usuario, MessageType.SERVER_RESPONSE_OK);
            } else {
                respuesta = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
            }
        } else if (mensaje.getMessageType() == MessageType.SERVER_SIGN_IN_REQUEST) {
            User usuario = mensaje.getUser();
            if (verificarCredencialesEnBaseDeDatos(usuario)) {
                respuesta = new Message(usuario, MessageType.SERVER_RESPONSE_OK);
            } else {
                respuesta = new Message(null, MessageType.SERVER_CREDENTIAL_ERROR);
            }
        }

        return respuesta;
    }

    private boolean registrarUsuarioEnBaseDeDatos(User user) {
        // Implementa aquí el código para registrar el usuario en la base de datos
        return true; // Suponiendo que el registro fue exitoso
    }

    private boolean verificarCredencialesEnBaseDeDatos(User user) {
        // Implementa aquí el código para verificar las credenciales del usuario
        return true; // Suponiendo que las credenciales son válidas
    }
}