package dataAccessTier;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author Ander
 */
public class Pool {

    private static BasicDataSource ds = null;
    private static String username;
    private static String password;
    private static String url;
    private static int userCap;

    // Asigna el usuario y contraseña desde el archivo de propiedades para entrar a la base de datos
    public static void getDatabaseCredentials() {
        ResourceBundle configFile;
        // Ruta de archivo de propiedades
        configFile = ResourceBundle.getBundle("resources.config");
        username = configFile.getString("DB_USER");
        password = configFile.getString("DB_PASSWORD");
        url = configFile.getString("URL");
        userCap = Integer.parseInt(configFile.getString("USER_CAP"));
    }

    //Este método crea y configura el pool de conexiones si aún no ha sido inicializado (ds == null)
    
    public static DataSource getDataSource() {
        if (ds == null) {
            ds = new BasicDataSource();

            // Define el controlador JDBC para PostgreSQL
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUsername(username);
            ds.setPassword(password);
            ds.setUrl(url);
            // Número de conexiones iniciales en el pool cuando se crea
            ds.setInitialSize(1);
            // Número máximo de conexiones que pueden quedar ociosas en el pool sin ser cerradas (ociosas: abiertas sin actividad)
            ds.setMaxIdle(10);
            // Número total máximo de conexiones abiertas que puede manejar el pool
            ds.setMaxTotal(userCap);
            // Tiempo máximo en milisegundos que un hilo puede esperar para obtener una conexión antes de lanzar una excepción (-1 espera de manera ilimitada)
            ds.setMaxWaitMillis(10000);
        }
        return ds;
    }

    public static Connection getConexion() throws SQLException {
        // Devuelve una conexión del pool llamando al método getConnection() de DataSource
        // La conexión que se devuelve es del tipo Connection, lista para ser usada en operaciones de base de datos
        // Si no hay conexiones disponibles en el pool y se alcanza el tiempo de espera (MaxWaitMillis), lanzará una excepción SQLException
        return getDataSource().getConnection();
    }

    public static void close() throws SQLException {
        if (ds != null) {
            // Cierra el pool y libera todas las conexiones
            ds.close();
        }
    }
}
