package dataAccessTier;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

public class Pool {

    private static BasicDataSource ds = null;

    public static DataSource getDataSource() {
        if (ds == null) {
            ds = new BasicDataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUsername("postgres"); // Cambia el usuario según tu configuración
            ds.setPassword("your_password");
            ds.setUrl("jdbc:postgresql://localhost:5432/test"); // Cambiar a la URL de PostgreSQL
            // Definimos el tamaño del pool de conexiones
            ds.setInitialSize(10);
            ds.setMaxIdle(10);
            ds.setMaxTotal(20);
            ds.setMaxWaitMillis(5000);
        }
        return ds;
    }

    public static Connection getConexion() throws SQLException {
        return getDataSource().getConnection();
    }
}
