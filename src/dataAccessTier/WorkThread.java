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

import userLogicTier.Message;
import userLogicTier.MessageType;
import userLogicTier.Signable;
import userLogicTier.exceptions.UserAlreadyExistsException;
import userLogicTier.exceptions.UserCapReachedException;
import userLogicTier.model.User;
/**
 *
 * @author inifr
 */
public class WorkThread implements Runnable { //implements Signable {

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket soc;
    private User user;
    private Message message;
    private Signable signable;  
    
    private static final Logger logger = Logger.getLogger(WorkThread.class.getName());
    
    public WorkThread() {
        
    }
    
    @Override
    public void run() {
        try {
            DAOFactory daoFactory = new DAOFactory();
            signable = daoFactory.getDAO();
            
            message = (Message) ois.readObject();
            
            switch (message.getMessageType()){
                case SERVER_SIGN_UP_REQUEST:
                    logger.info("Starting SignUp");
                    user = signable.signUp(message.getUser());
                    message.setUser(user);
                    if (user == null) {
                        message.setMessageType(MessageType.SERVER_RESPONSE_DENIED);
                    } else {
                        message.setMessageType(MessageType.SERVER_RESPONSE_OK);
                    }
                    break;
                case SERVER_SIGN_IN_REQUEST:
                    logger.info("Starting SignIn");
                    user = signable.signIn(message.getUser());
                    message.setUser(user);
                    if (user == null) {
                        message.setMessageType(MessageType.SERVER_RESPONSE_DENIED);
                    } else {
                        message.setMessageType(MessageType.SERVER_RESPONSE_OK);
                    }
                    
                }
        } catch (UserCapReachedException e){
            message.setMessageType(MessageType.SERVER_USER_CAP_REACHED);
            Logger.getLogger(WorkThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (UserAlreadyExistsException e){
            message.setMessageType(MessageType.SERVER_USER_ALREADY_EXISTS);
            Logger.getLogger(WorkThread.class.getName()).log(Level.SEVERE, null, e);            
        } catch (IOException | ClassNotFoundException e) {
            Logger.getLogger(WorkThread.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(WorkThread.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                logger.info("Cerrando conexiones");
                //Cerramos los distintos imputs y outputs más el propio socket
                oos = new ObjectOutputStream(soc.getOutputStream());
                oos.writeObject(message);
                //Llamamos a esta funcion del main para borrar el cliente una vez que cierre su conexión
                //SignerServer.borrarCliente(this);
                ois.close();
                oos.close();
                soc.close();
            } catch (IOException ex) {
                Logger.getLogger(WorkThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }  
}
