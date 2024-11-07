package dataAccessTier;

import userLogicTier.Signable;

/**
 * The DAOFactory class provides a method to obtain an instance of a Signable object.
 * This factory class centralizes the creation of DAO objects, which are used to manage 
 * data access operations.
 * 
 * Using this factory pattern allows for easier maintenance and testing by abstracting 
 * the instantiation of the DAO class.
 * 
 * Usage:
 * Signable dataAccessObject = DAOFactory.getDAO();
 * 
 * @see Signable
 * @see DAO
 * 
 * @author Pablo
 */
public class DAOFactory {

    /**
     * Returns an instance of Signable, implemented by DAO.
     * 
     * This method allows clients to access the data access object without directly 
     * instantiating the DAO class, promoting loose coupling and dependency inversion.
     * 
     * @return a new instance of Signable implemented by DAO
     */
    public static Signable getDAO() {
        return new DAO();
    }
}
