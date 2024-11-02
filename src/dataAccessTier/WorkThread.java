/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccessTier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import message.MessageType;
import serverLogicTier.ServerApplication;
import userLogicTier.model.User;
/**
 *
 * @author InigoFreire
 */
public class WorkThread implements Runnable { 
    private ObjectInputStream reader;
    private  ObjectOutputStream writer;
    private final Socket socket;
    private Logger logger = Logger.getLogger(WorkThread.class.getName());

    public WorkThread(Socket socketInput) {
        this.socket = socketInput;
    }

    @Override
    public void run() {
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());            

            // Reads client's message
            Message message = (Message) reader.readObject();
            logger.log(Level.INFO, "Client message received", message.getMessageType());

            // Filters the message so it gets a response
            Message response = handleMessage(message);

            // Sends the response back to the client
            writer.writeObject(response);
            logger.log(Level.INFO, "Response sent to client", response.getMessageType());

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Client handling error", e.getMessage());
        } finally {
            try {
                socket.close();
                socket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error while closing the socket", e.getMessage());
            }
        }
    }

    private Message handleMessage(Message message) {
        Message response = new Message(null, MessageType.SERVER_RESPONSE_DENIED);

        try{
            if (message.getMessageType() == MessageType.SERVER_SIGN_UP_REQUEST) {
                User user = message.getUser();
                // TODO Llamar al la factoria del dao que llama al metodo que registra y devuelve un usuario
                if (RegisterUserIntoDatabase(user)) {
                    response = new Message(user, MessageType.SERVER_RESPONSE_OK);
                } else {
                    response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
                }
            } else if (message.getMessageType() == MessageType.SERVER_SIGN_IN_REQUEST) {
                User usuario = message.getUser();
                // TODO Lo mismo que arriba
                if (VerifyUserCredentials(usuario)) {
                    response = new Message(usuario, MessageType.SERVER_RESPONSE_OK);
                } else {
                    response = new Message(null, MessageType.SERVER_CREDENTIAL_ERROR);
                }
            }
            
        //TODO cambiar los nombres de las excepciones e importarlas
        }catch (ConnectionErrorException e){
            response = new Message(null, MessageType.SERVER_CONNECTION_ERROR);
        }catch (UserCapReachedException e){
            response = new Message(null, MessageType.SERVER_USER_CAP_REACHED);
        }catch (UserAlreadyExistsException e){
            response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
        }catch (CredentialErrorException e){
            response = new Message(null, MessageType.SERVER_CREDENTIAL_ERROR);
        }
        return response;
    }
}