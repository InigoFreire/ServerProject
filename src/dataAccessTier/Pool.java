package dataAccessTier;

import exceptions.UserCapException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;

public class Pool {

    private static Stack<Connection> connectionStack = new Stack<>();
    private static String username;
    private static String password;
    private static String url;
    private static int userCap;

    /**
     * Carga las credenciales de la base de datos desde un archivo de configuración. Estas credenciales incluyen el usuario de la base de datos, la contraseña, la URL y el límite de conexiones.
     */
    public static void getDatabaseCredentials() {
        ResourceBundle configFile = ResourceBundle.getBundle("resources.config");
        username = configFile.getString("DB_USER");
        password = configFile.getString("DB_PASSWORD");
        url = configFile.getString("URL");
        userCap = Integer.parseInt(configFile.getString("USER_CAP"));
    }

    /**
     * Inicializa el stack de conexiones y configura el pool si no está ya configurado. Crea las conexiones necesarias y las agrega al stack.
     */
    public static void initializePool() throws SQLException {
        getDatabaseCredentials();

        // Inicializa el stack con el número máximo de conexiones permitido
        for (int i = 0; i < userCap; i++) {
            Connection connection = DriverManager.getConnection(url, username, password);
            connectionStack.push(connection);
        }
    }

    /**
     * Obtiene una conexión del stack. Si el stack está vacío, se espera que se libere una conexión.
     *
     * @return una conexión a la base de datos.
     * @throws UserCapException si no se puede obtener una conexión.
     */
    public static synchronized Connection getConexion() throws UserCapException {
        if (connectionStack.isEmpty()) {
            throw new UserCapException("User cap reached, wait for a connection.");
        }
        return connectionStack.pop();
    }

    /**
     * Libera una conexión y la devuelve al stack.
     *
     * @param connection la conexión que se devuelve al stack.
     */
    public static synchronized void releaseConnection(Connection connection) {
        if (connection != null && connectionStack.size() < userCap) {
            connectionStack.push(connection);
        }
    }

    /**
     * Cierra todas las conexiones del pool.
     *
     * @throws SQLException si ocurre un error al cerrar las conexiones.
     */
    public static void closeAllConnections() throws SQLException {
        while (!connectionStack.isEmpty()) {
            Connection connection = connectionStack.pop();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
}
