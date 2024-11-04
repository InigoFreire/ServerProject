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
        } catch (InactiveUserException e) {
            logger.log(Level.SEVERE, "User inactive", e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while closing the socket", e.getMessage());
            }
        }
    }

    private Message handleMessage(Message message) throws InactiveUserException {
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
            }
            //TODO ASK THIS -> Is this OK? (Logically, can the exceptions be launched here??) <- ASK THIS
        }catch (ServerException e){
            response = new Message(null, MessageType.SERVER_CONNECTION_ERROR);
            logger.log(Level.WARNING, "Server response ERROR -> Connection error", e.getMessage());
        }catch (UserCapException e){
            response = new Message(null, MessageType.SERVER_USER_CAP_REACHED);
            logger.log(Level.WARNING, "Server response ERROR -> User cap reached", e.getMessage());
        }catch (ExistingUserException e){
            response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
            logger.log(Level.WARNING, "Server response ERROR -> User already exists", e.getMessage());
        }catch (UserCredentialException e){
            response = new Message(null, MessageType.SERVER_USER_CREDENTIAL_ERROR);
            logger.log(Level.WARNING, "Server response ERROR -> User credential error", e.getMessage());
        }
        return response;
    }
}