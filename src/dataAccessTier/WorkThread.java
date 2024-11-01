/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccessTier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import message.Message;
import message.MessageType;
import serverLogicTier.ServerApplication;

import userLogicTier.model.User;
/**
 *
 * @author inifr
 */
public class WorkThread implements Runnable { //implements Signable {

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final Socket socket;
    private ServerApplication serverApplication;
    private static final Logger logger = Logger.getLogger(WorkThread.class.getName());

    public WorkThread(Socket socketInput) {
        this.socket = socketInput;
    }

    @Override
    public void run() {
        try {
            // Reads client's message
            Message message = (Message) ois.readObject();
            logger.log(Level.INFO, "Client side message received", message.getMessageType());

            // Filters the message so it gets a response
            Message response = filterMessage(message);

            // Sends the response back to the client
            oos.writeObject(response);

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error while closing the socket", e.getMessage());
            }
            serverApplication.threadCounterDownwards();
        }
    }

    private Message filterMessage(Message message) {
        Message response = new Message(null, MessageType.SERVER_RESPONSE_DENIED);

        if (message.getMessageType() == MessageType.SERVER_SIGN_UP_REQUEST) {
            User usuario = message.getUser();
            if (registrarUsuarioEnBaseDeDatos(usuario)) {
                response = new Message(usuario, MessageType.SERVER_RESPONSE_OK);
            } else {
                response = new Message(null, MessageType.SERVER_USER_ALREADY_EXISTS);
            }
        } else if (message.getMessageType() == MessageType.SERVER_SIGN_IN_REQUEST) {
            User usuario = message.getUser();
            if (verificarCredencialesEnBaseDeDatos(usuario)) {
                response = new Message(usuario, MessageType.SERVER_RESPONSE_OK);
            } else {
                response = new Message(null, MessageType.SERVER_CREDENTIAL_ERROR);
            }
        }
        return response;
    }
}