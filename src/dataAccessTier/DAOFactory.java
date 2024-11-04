package dataAccessTier;

import userLogicTier.Signable;

/**
 * The {@code DAOFactory} class provides a method to obtain an instance of a {@link Signable} object.
 * This factory class centralizes the creation of {@code DAO} objects, which are used to manage 
 * data access operations.
 * <p>
 * Using this factory pattern allows for easier maintenance and testing by abstracting 
 * the instantiation of the {@link DAO} class.
 * </p>
 * 
 * <p><b>Usage:</b></p>
 * <pre>
 *     Signable dataAccessObject = DAOFactory.getDAO();
 * </pre>
 * 
 * @author Pebble
 * @see Signable
 * @see DAO
 */
public class DAOFactory {

    /**
     * Returns an instance of {@link Signable}, implemented by {@link DAO}.
     * <p>
     * This method allows clients to access the data access object without directly 
     * instantiating the {@link DAO} class, promoting loose coupling and dependency inversion.
     * </p>
     * 
     * @return a new instance of {@link Signable} implemented by {@link DAO}
     */
    public static Signable getDAO() {
        return new DAO();
    }
}
