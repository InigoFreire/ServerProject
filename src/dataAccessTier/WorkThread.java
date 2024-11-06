/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccessTier;

import exceptions.ExistingUserException;
import exceptions.InactiveUserException;
import exceptions.ServerException;
import exceptions.UserCapException;
import exceptions.UserCredentialException;
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
    private ServerApplication main;
    private Logger logger = Logger.getLogger(WorkThread.class.getName());

    public WorkThread(Socket socketInput, ServerApplication serverApp) {
        this.socket = socketInput;
        this.main = serverApp;
    }

    /**
     * 
     */
    @Override
    public void run() {
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());            
            logger.log(Level.INFO, "Reader & writer instanced");

            // Reads client's message
            Message message = (Message) reader.readObject();
            logger.log(Level.INFO, "Client message received", message.getMessageType());

            // Filters the message so it gets a response
            Message response = handleMessage(message);

            // Sends the response back to the client
            writer.writeObject(response);
            logger.log(Level.INFO, "Response sent to client", response.getMessageType());

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Client handling error", e);
        } finally {
            try {
                socket.close();
                main.decrementThreadCounter();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while closing the socket", e);
            }
        }
    }
    /**
    * Handles the message delivered by both the client (@link userLogicTier.Client) and the server (@link dataAcessTier.DAO).
    * 
    * <ul>
    * <li>
    * <p>1. Firstly, it receives the message from the client side and extracts the user inside it so it can send to the server side
    * a message and a user, in order to be used in many operations in the DB.</p> 
    * </li>
    * <li>
    * <p>2. Then, it sends the message to the server and receives the response.</p>
    * </li>
    * <li>
    * <p>3. Finally, Depending on the response received from the server side, sends back a message and a user inside a response object to the client.</p>
    * </ul> 
    *
    * @param message a message with a request from the client side
    * @return a message containing the ser side response
    */
    private Message handleMessage(Message message){
        Message response = new Message(null, MessageType.SERVER_RESPONSE_DENIED);
        try{
            if (message.getMessageType() == MessageType.SERVER_SIGN_UP_REQUEST) {
                User user = message.getUser();
                logger.log(Level.WARNING, "Sign up request received", message.getMessageType());
                if (DAOFactory.getDAO().signUp(user) != null) {
                    response = new Message(user, MessageType.SERVER_RESPONSE_OK);
                    logger.log(Level.WARNING, "Server response OK", message.getMessageType());
                } else {
                    response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
                    logger.log(Level.WARNING, "Server response ERROR -> User already exists", message.getMessageType());
                }
            } else if (message.getMessageType() == MessageType.SERVER_SIGN_IN_REQUEST) {
                User user = message.getUser();
                logger.log(Level.WARNING, "Sign in request received", message.getMessageType());
                if (DAOFactory.getDAO().signIn(user) != null) {
                    response = new Message(user, MessageType.SERVER_RESPONSE_OK);
                    logger.log(Level.WARNING, "Server response OK", message.getMessageType());
                } else {
                    response = new Message(null, MessageType.SERVER_USER_CREDENTIAL_ERROR);
                    logger.log(Level.WARNING, "Server response ERROR -> User credential error", message.getMessageType());
                }
            } else {
                User user = message.getUser();
                response = new Message(null, MessageType.SERVER_CONNECTION_ERROR);
                logger.log(Level.WARNING, "Server error", message.getMessageType());
            }
        } catch (ExistingUserException e) {
            response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
            logger.log(Level.WARNING, "SERVER ERROR. User already exists", e);
        } catch (UserCredentialException e) {
            response = new Message(null, MessageType.SERVER_USER_CREDENTIAL_ERROR);
            logger.log(Level.WARNING, "SERVER ERROR. User credential error", e);
        } catch (InactiveUserException e) {
            response = new Message(null, MessageType.SERVER_USER_INACTIVE);
            logger.log(Level.WARNING, "SERVER ERROR. User inactive", e);
        } catch (UserCapException e) {
            response = new Message(null, MessageType.SERVER_USER_CAP_REACHED);
            logger.log(Level.WARNING, "SERVER ERROR. User cap reached", e);
        } catch (ServerException e) {
            response = new Message(null, MessageType.SERVER_CONNECTION_ERROR);
            logger.log(Level.WARNING, "SERVER ERROR. Server connection error", e);
        }
        return response;
    }
}