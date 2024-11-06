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

    /**
     * Constructor of the class.
     * @param socketInput   Socket received from the client side, with an especific port and IP.
     * @param serverApp     Server application object used to decrement the threadCounter in the main class.
     */
    public WorkThread(Socket socketInput, ServerApplication serverApp) {
        this.socket = socketInput;
        this.main = serverApp;
    }

    /**
     * Default mehtod of a thread class.
     * Receives the message from the client side, translates it so it can be interpreted and then sent to the server side.
     * Then it receives the response from the server side and sends it back to the client.
     */
    @Override
    public void run() {
        try {
            // Initializes the reader and writer and translates the socket received from the client side.
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());            
            logger.log(Level.INFO, "Reader & writer instanced");

            // Reads client's message
            Message message = (Message) reader.readObject();
            logger.log(Level.INFO, "Client message received", message.getMessageType());

            // Interprets the message so it can be sent to the server and receives the response from it
            Message response = handleMessage(message);

            // Sends the response from the server back to the client
            writer.writeObject(response);
            logger.log(Level.INFO, "Response sent to client", response.getMessageType());

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Client handling error", e);
        } finally {
            try {
                // Closes the socket
                socket.close();
                logger.log(Level.INFO, "Socket closed");
                // Decrements the thread counter in the main class by one unit.
                main.decrementThreadCounter();
                logger.log(Level.INFO, "Thread counter decremented");
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
        // Instantiates the response
        Message response = new Message(null, MessageType.SERVER_RESPONSE_DENIED);
        try{
            // Checks if the message is a sign up or sign in request
            if (message.getMessageType() == MessageType.SERVER_SIGN_UP_REQUEST) {
                // Gets the user implied in the message
                User user = message.getUser();
                logger.log(Level.WARNING, "Sign up request received", message.getMessageType());
                // Checks if the user is already registered
                if (DAOFactory.getDAO().signUp(user) != null) {
                    // If the user is not registered, it gets an OK message
                    response = new Message(user, MessageType.SERVER_RESPONSE_OK);
                    logger.log(Level.WARNING, "Server response OK", message.getMessageType());
                // If the user is already registered, it gets an ERROR message
                } else {
                    response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
                    logger.log(Level.WARNING, "Server response ERROR -> User already exists", message.getMessageType());
                }
            } else if (message.getMessageType() == MessageType.SERVER_SIGN_IN_REQUEST) {
                // Gets the user implied in the message
                User user = message.getUser();
                logger.log(Level.WARNING, "Sign in request received", message.getMessageType());
                // Checks if the user is registered
                if (DAOFactory.getDAO().signIn(user) != null) {
                    // If the user is registered, it gets an OK message
                    response = new Message(user, MessageType.SERVER_RESPONSE_OK);
                    logger.log(Level.WARNING, "Server response OK", message.getMessageType());
                // If the user is not registered, it gets an ERROR message
                } else {
                    response = new Message(null, MessageType.SERVER_USER_CREDENTIAL_ERROR);
                    logger.log(Level.WARNING, "Server response ERROR -> User credential error", message.getMessageType());
                }
            // If the message is not a sign up or sign in request, it gets an ERROR message, as it is a non expected message request
            } else {
                // Sends an ERROR message
                response = new Message(null, MessageType.SERVER_CONNECTION_ERROR);
                logger.log(Level.WARNING, "Server error", message.getMessageType());
            }
        // Catches exceptions and sends them to the client side, so they can be handled properly there
        } catch (ExistingUserException e) {
            // Catches and sends the exception when the user exists in the DB, at the time of registering the same user
            response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
            logger.log(Level.WARNING, "SERVER ERROR. User already exists", e);
        } catch (UserCredentialException e) {
            // Catches and sends the exception when the user credentials are wrong (either username, password or both)
            response = new Message(null, MessageType.SERVER_USER_CREDENTIAL_ERROR);
            logger.log(Level.WARNING, "SERVER ERROR. User credential error", e);
        } catch (InactiveUserException e) {
            // Catches and sends the exception when the user is inactive (marked as so when signing up)
            response = new Message(null, MessageType.SERVER_USER_INACTIVE);
            logger.log(Level.WARNING, "SERVER ERROR. User inactive", e);
        } catch (UserCapException e) {
            // Catches and sends the exception when the user cap (in the connection pool) is reached
            response = new Message(null, MessageType.SERVER_USER_CAP_REACHED);
            logger.log(Level.WARNING, "SERVER ERROR. User cap reached", e);
        } catch (ServerException e) {
            // Catches and sends the exception when there is a server error
            response = new Message(null, MessageType.SERVER_CONNECTION_ERROR);
            logger.log(Level.WARNING, "SERVER ERROR. Server connection error", e);
        }
        return response;
    }
}