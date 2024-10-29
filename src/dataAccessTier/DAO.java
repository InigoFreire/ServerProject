package dataAccessTier;

/**
 *
 * @author Ander
 */
public class DAO {

    private Connection connection = null;

    public User signUp(User user) throws SQLException {
        boolean estado = false;
        PreparedStatement stmtPartner = null;
        PreparedStatement stmtUser = null;

        connection = getConnection();

        String insertPartner = "INSERT INTO res_partner(company_id, name, street, city, zip, email) VALUES "
                + "(1, ?, ?, ?, ?, ?);";
        String insertUser = "INSERT INTO res_users(company_id, parner_id, login, password, active, notification_type) VALUES "
                + "(1, ?, ?, ?, ?, email);";

        try {
            // Le indicamos el inicio de la transacción
            connection.setAutoCommit(false);

            // Preparo el statement de partner para prevenir inyecciones maliciosas
            stmtPartner = connection.prepareStatement(insertPartner, Statement.RETURN_GENERATED_KEYS);

            // Le paso los datos
            stmtPartner.setString(1, user.getName());
            stmtPartner.setString(2, user.getStreet());
            stmtPartner.setString(3, user.getCity());
            stmtPartner.setString(4, user.getZip());
            stmtPartner.setString(5, user.getEmail());

            // Ejecuto la actualización de la base de datos
            stmtPartner.executeUpdate();

            // Obtener el ID generado
            ResultSet generatedKeys = stmtPartner.getGeneratedKeys();
            int partnerId = 0;
            if (generatedKeys.next()) {
                // Obtener el primer campo de la clave generada
                partnerId = generatedKeys.getInt(1);
            }

            // Preparo el statement de users
            stmtUser = connection.prepareStatement(insertUser);

            // Le paso los datos
            // El primero siempre 1
            stmtUser.setInt(1, partnerId);
            stmtUser.setString(2, user.getEmail());
            stmtUser.setString(3, user.getPassword());
            stmtUser.setBoolean(4, user.isActive());
            // El último siempre email

            // Ejecuto la actualización de la base de datos
            stmtUser.executeUpdate();

            // Si todo va bien hacemos commit y guardamos los datos
            connection.commit();
            estado = true;
        } catch (SQLException e) {
            // Si ocurre un error, realizar rollback
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            estado = false;
        } finally {
            // Cerrar recursos
            if (stmtPartner != null) {
                stmtPartner.close();
            }
            if (stmtUser != null) {
                stmtUser.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return user;
    }

    public User signIn(User user) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        String selectUserData = "SELECT login, password, active FROM res_users ";
        String selectPartnerData = "SELECT name, street, city, zip FROM res_partner";

        try {
            stmt = connection.prepareStatement(selectUserData);
            rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(enunciadoId);
                user.setDescripcion(rs.getString("descripcion"));
                user.setNivel(Dificultad.valueOf(nivelStr));
                user.setDisponible(rs.getBoolean("disponible"));
                user.setRuta(rs.getString("ruta"));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return user;
    }

    // accede a la conexión de la clase Pool
    private Connection getConnection() throws SQLException {
        return Pool.getConexion();
    }
}
