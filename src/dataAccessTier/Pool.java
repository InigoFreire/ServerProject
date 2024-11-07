package dataAccessTier;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * The Pool class provides a connection pool for managing database connections efficiently.
 * It initializes a BasicDataSource for PostgreSQL, using configuration settings from a properties file.
 * This setup allows connections to be reused and managed automatically, enhancing performance for applications
 * requiring frequent database access.
 * 
 * @author Ander
 */
public class Pool {

    private static BasicDataSource ds = null;
    private static String username;
    private static String password;
    private static String url;
    private static int userCap;

    /**
     * Loads database credentials from a configuration file.
     * These credentials include database user, password, URL, and connection limit.
     */
    public static void getDatabaseCredentials() {
        ResourceBundle configFile;
        configFile = ResourceBundle.getBundle("resources.config");
        username = configFile.getString("DB_USER");
        password = configFile.getString("DB_PASSWORD");
        url = configFile.getString("URL");
        userCap = Integer.parseInt(configFile.getString("USER_CAP"));
    }

    /**
     * Creates and configures the connection pool if it has not been initialized.
     * Sets up parameters such as initial size, maximum idle connections, total connections, 
     * and wait time.
     * 
     * @return a DataSource object representing the connection pool
     */
    public static DataSource getDataSource() {
        if (ds == null) {
            ds = new BasicDataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUsername(username);
            ds.setPassword(password);
            ds.setUrl(url);
            ds.setInitialSize(1);
            ds.setMaxIdle(10);
            ds.setMaxTotal(userCap);
            ds.setMaxWaitMillis(10000);
        }
        return ds;
    }

    /**
     * Obtains a connection from the pool, which can be used for database operations.
     * Throws a SQLException if no connections are available within the specified wait time.
     * 
     * @return a Connection object for database interaction
     * @throws SQLException if a connection cannot be obtained
     */
    public static Connection getConexion() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Closes the connection pool, releasing all database connections.
     * 
     * @throws SQLException if an error occurs while closing connections
     */
    public static void close() throws SQLException {
        if (ds != null) {
            ds.close();
        }
    }
}
