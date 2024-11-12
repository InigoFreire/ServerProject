package dataAccessTier;

import exceptions.ExistingUserException;
import exceptions.InactiveUserException;
import exceptions.ServerException;
import exceptions.UserCapException;
import exceptions.UserCredentialException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import userLogicTier.Signable;
import userLogicTier.model.User;

/**
 * Data Access Object (DAO) implementation for handling user-related operations with the database. Implements the {@link Signable} interface to provide functionality for user sign-up and sign-in actions.
 *
 * The DAO interacts with a PostgreSQL database to execute SQL statements to create and verify user accounts.
 *
 * @see Signable
 * @see User
 * @see ServerException
 * @see ExistingUserException
 * @see InactiveUserException
 * @see UserCredentialException
 *
 * @author Ander
 * @author Aitziber
 */
public class DAO implements Signable {

    private static final Logger logger = Logger.getLogger(DAO.class.getName());

    /**
     * Registers a new user in the system. First, verifies if the user already exists, then creates new records in `res_partner` and `res_users` tables.
     *
     * @param user the User object containing registration details
     * @return the registered User object
     * @throws ServerException if a database-related error occurs
     * @throws ExistingUserException if the user already exists
     * @throws exceptions.UserCapException
     */
    @Override
    public User signUp(User user) throws ServerException, ExistingUserException, UserCapException {
        Connection connection = null;
        PreparedStatement stmtPartner = null;
        PreparedStatement stmtUser = null;
        ResultSet generatedKeys = null;

        String insertPartner = "INSERT INTO res_partner(company_id, name, street, city, zip, email, mobile) VALUES (1, ?, ?, ?, ?, ?, ?);";
        String insertUser = "INSERT INTO res_users(company_id, partner_id, login, password, active, notification_type) VALUES (1, ?, ?, ?, ?, 'email');";

        try {
            connection = getConnection();
            if (checkUserExistence(user.getEmail())) {
                throw new ExistingUserException("User already exists");
            }
            connection.setAutoCommit(false);

            stmtPartner = connection.prepareStatement(insertPartner, Statement.RETURN_GENERATED_KEYS);
            stmtPartner.setString(1, user.getName());
            stmtPartner.setString(2, user.getStreet());
            stmtPartner.setString(3, user.getCity());
            stmtPartner.setString(4, user.getZip());
            stmtPartner.setString(5, user.getEmail());
            stmtPartner.setInt(6, user.getMobile());
            stmtPartner.executeUpdate();

            generatedKeys = stmtPartner.getGeneratedKeys();
            int partnerId = 0;
            if (generatedKeys.next()) {
                partnerId = generatedKeys.getInt(1);
            }

            stmtUser = connection.prepareStatement(insertUser);
            stmtUser.setInt(1, partnerId);
            stmtUser.setString(2, user.getEmail());
            stmtUser.setString(3, user.getPassword());
            stmtUser.setBoolean(4, user.isActive());
            stmtUser.executeUpdate();

            connection.commit();

            return user;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL exception SignUp DAO", e);
            throw new ServerException("SERVER ERROR. Error searching user");

        } catch (UserCapException ex) {
            logger.log(Level.SEVERE, "User cap reached", ex);
            throw new UserCapException("SERVER ERROR. Error searching user");
            
        } finally {
            // Cerrar recursos y liberar conexión
            try {
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
                if (stmtPartner != null) {
                    stmtPartner.close();
                }
                if (stmtUser != null) {
                    stmtUser.close();
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "SQL exception closing resources", ex);
                throw new ServerException("SERVER ERROR. Error closing resources");
            } finally {
                if (connection != null) {
                    Pool.releaseConnection(connection); // Liberar la conexión
                }
            }
        }
    }

    /**
     * Checks if a user with the specified email already exists in the database.
     *
     * @param email the email of the user to check
     * @return true if the user exists, false otherwise
     * @throws ServerException if a database-related error occurs
     * @throws UserCapException if the user cap is reached
     */
    public boolean checkUserExistence(String email) throws ServerException, UserCapException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String searchUser = "SELECT login FROM res_users WHERE login = ?;";

        try {
            connection = getConnection();
            stmt = connection.prepareStatement(searchUser);
            stmt.setString(1, email);

            rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL exception checkUserExistence DAO", e);
            throw new ServerException("SERVER ERROR. Error searching user");

        } finally {
            // Cerrar recursos y liberar conexión
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "SQL exception closing resources", ex);
                throw new ServerException("SERVER ERROR. Error closing resources");
            } finally {
                if (connection != null) {
                    Pool.releaseConnection(connection); // Liberar la conexión
                }
            }
        }
    }

    /**
     * Authenticates a user by verifying their email and password, and checking if they are active in the database.
     *
     * @param user the User object containing login details
     * @return the authenticated User object with details from the database
     * @throws ServerException if a database-related error occurs
     * @throws UserCredentialException if the user credentials are invalid
     * @throws InactiveUserException if the user is inactive
     * @throws UserCapException if the user cap is reached
     */
    @Override
    public User signIn(User user) throws ServerException, UserCredentialException, InactiveUserException, UserCapException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String selectUserData = "SELECT u.login, u.password, u.active, p.name, p.street, p.city, p.zip "
                + "FROM res_users u "
                + "JOIN res_partner p ON u.partner_id = p.id "
                + "WHERE u.login = ? AND u.password = ?";

        try {
            connection = getConnection();
            stmt = connection.prepareStatement(selectUserData);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());

            rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getString("name"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("street"),
                        rs.getString("city"),
                        rs.getString("zip"),
                        rs.getBoolean("active")
                );
            } else {
                throw new UserCredentialException("Invalid user credentials");
            }

            if (!user.isActive()) {
                throw new InactiveUserException("User is inactive");
            }

            return user;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL exception SignIn DAO", e);
            throw new ServerException("SERVER ERROR. Error searching user");

        } finally {
            // Cerrar recursos y liberar conexión
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "SQL exception closing resources", ex);
                throw new ServerException("SERVER ERROR. Error closing resources");
            } finally {
                if (connection != null) {
                    Pool.releaseConnection(connection);
                }
            }
        }
    }

    /**
     * Provides a connection to the database via the connection pool.
     *
     * @return a database connection object
     * @throws SQLException if an error occurs when attempting to connect
     */
    private Connection getConnection() throws SQLException, UserCapException {
        return Pool.getConexion();
    }
}
