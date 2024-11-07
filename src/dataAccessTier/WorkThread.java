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
 * The WorkThread class handles communication between a client and server.
 * It processes client requests, interacts with the server to fetch results, and sends responses back to the client.
 * Each instance of WorkThread is associated with a client connection and operates as a separate thread.
 * 
 * This class includes logic for interpreting different client messages, handling sign-in and sign-up requests, and managing exceptions.
 * @see ServerApplication
 * @see MessageType
 * @see DAOFactory
 */
public class WorkThread implements Runnable {

    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private final Socket socket;
    private ServerApplication main;
    private Logger logger = Logger.getLogger(WorkThread.class.getName());

    /**
     * Initializes a new WorkThread instance with a client socket and server application reference.
     * 
     * @param socketInput Socket received from the client side
     * @param serverApp Reference to the main server application
     */
    public WorkThread(Socket socketInput, ServerApplication serverApp) {
        this.socket = socketInput;
        this.main = serverApp;
    }

    /**
     * Starts the thread to handle client requests.
     * Reads the incoming message, processes it, and sends back a response.
     * Closes the socket and decrements the server's thread counter after completing the interaction.
     */
    @Override
    public void run() {
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
            logger.log(Level.INFO, "Reader & writer initialized");

            Message message = (Message) reader.readObject();
            logger.log(Level.INFO, "Client message received", message.getMessageType());

            Message response = handleMessage(message);

            writer.writeObject(response);
            logger.log(Level.INFO, "Response sent to client", response.getMessageType());

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Client handling error", e);
        } finally {
            try {
                socket.close();
                logger.log(Level.INFO, "Socket closed");
                main.decrementThreadCounter();
                logger.log(Level.INFO, "Thread counter decremented");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while closing the socket", e);
            }
        }
    }

    /**
     * Processes incoming client messages, interprets requests, and interacts with the database through DAO.
     * Supports handling of sign-in and sign-up requests, with responses based on database operation results.
     * 
     * @param message A message containing a client request
     * @return A message with the server response, indicating success or error
     */
    private Message handleMessage(Message message) {
        Message response = new Message(null, MessageType.SERVER_RESPONSE_DENIED);
        try {
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
                    user = DAOFactory.getDAO().signIn(user);
                    response = new Message(user, MessageType.SERVER_RESPONSE_OK);
                    logger.log(Level.WARNING, "Server response OK", message.getMessageType());
                } else {
                    response = new Message(null, MessageType.SERVER_USER_CREDENTIAL_ERROR);
                    logger.log(Level.WARNING, "Server response ERROR -> User credential error", message.getMessageType());
                }
            } else {
                response = new Message(null, MessageType.SERVER_CONNECTION_ERROR);
                logger.log(Level.WARNING, "Unexpected message type", message.getMessageType());
            }
        } catch (ExistingUserException e) {
            response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
            logger.log(Level.WARNING, "User already exists", e);
        } catch (UserCredentialException e) {
            response = new Message(null, MessageType.SERVER_USER_CREDENTIAL_ERROR);
            logger.log(Level.WARNING, "User credential error", e);
        } catch (InactiveUserException e) {
            response = new Message(null, MessageType.SERVER_USER_INACTIVE);
            logger.log(Level.WARNING, "User inactive", e);
        } catch (UserCapException e) {
            response = new Message(null, MessageType.SERVER_USER_CAP_REACHED);
            logger.log(Level.WARNING, "User cap reached", e);
        } catch (ServerException e) {
            response = new Message(null, MessageType.SERVER_CONNECTION_ERROR);
            logger.log(Level.WARNING, "Server connection error", e);
        }
        return response;
    }
}
