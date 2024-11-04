/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccessTier;

import exceptions.ExistingUserException;
import exceptions.ServerException;
import exceptions.UserCredentialException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import userLogicTier.Signable;
import userLogicTier.model.User;

/**
 *
 * @author Ander
 */
public class DAO implements Signable {

    private Connection connection = null;

    @Override
    public User signUp(User user) throws SQLException, ServerException {
        PreparedStatement stmtPartner = null;
        PreparedStatement stmtUser = null;

        connection = getConnection();

        String insertPartner = "INSERT INTO res_partner(company_id, name, street, city, zip, email) VALUES (1, ?, ?, ?, ?, ?);";
        String insertUser = "INSERT INTO res_users(company_id, partner_id, login, password, active, notification_type) VALUES (1, ?, ?, ?, ?, 'email');";

        try {
            // Iniciar la transacción
            connection.setAutoCommit(false);

            // Preparar y ejecutar el statement para res_partner
            stmtPartner = connection.prepareStatement(insertPartner, Statement.RETURN_GENERATED_KEYS);
            stmtPartner.setString(1, user.getName());
            stmtPartner.setString(2, user.getStreet());
            stmtPartner.setString(3, user.getCity());
            stmtPartner.setString(4, user.getZip());
            stmtPartner.setString(5, user.getEmail());
            stmtPartner.executeUpdate();

            // Obtener el ID generado
            ResultSet generatedKeys = stmtPartner.getGeneratedKeys();
            int partnerId = 0;
            if (generatedKeys.next()) {
                partnerId = generatedKeys.getInt(1);
            }

            // Preparar y ejecutar el statement para res_users
            stmtUser = connection.prepareStatement(insertUser);
            stmtUser.setInt(1, partnerId);
            stmtUser.setString(2, user.getEmail());
            stmtUser.setString(3, user.getPassword());
            stmtUser.setBoolean(4, user.isActive());
            stmtUser.executeUpdate();

            // Confirmar la transacción
            connection.commit();

        } catch (SQLException e) {
            // Realizar rollback en caso de error
            if (connection != null) {
                connection.rollback();
            }
            throw new ServerException("SERVER ERROR. Error en la inserción de datos");
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

    @Override
    public User signIn(User user) throws SQLException, ServerException, UserCredentialException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String userEmail = user.getEmail();

        String selectUserData = "SELECT u.login, u.password, u.active, p.name, p.street, p.city, p.zip "
                + "FROM res_users u "
                + "JOIN res_partner p ON u.partner_id = p.id "
                + "WHERE u.login = ? AND u.password = ? AND u.active = true;";

        try {
            // Iniciar la transacción
            connection.setAutoCommit(false);
            // Preparar la consulta con parámetros para evitar inyecciones SQL
            stmt = connection.prepareStatement(selectUserData);
            stmt.setString(1, userEmail);
            stmt.setString(2, user.getPassword());

            // Ejecutar la consulta y obtener los resultados
            rs = stmt.executeQuery();

            // Si el usuario existe y está activo, asignar los valores a un nuevo objeto User
            if (rs.next()) {
                // Lo pongo así por que hay un constructor con todos esos valores
                user = new User(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("street"),
                        rs.getString("city"),
                        rs.getString("zip"),
                        rs.getBoolean("active")
                );
            } else {
                // Si el usuario no existe, devolvemos null y lanzamos una excepción personalizada
                user = null;
                throw new UserCredentialException();
            }
            
            // Confirmar la transacción
            connection.commit();

        } catch (SQLException e) {
            // Realizar rollback en caso de error
            if (connection != null) {
                connection.rollback();
            }
            throw new ServerException("SERVER ERROR. Error al buscar el usuario");
        } finally {
            // Cerrar ResultSet y PreparedStatement en el bloque finally
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return user;
    }

    // Accede a la conexión de la clase Pool
    private Connection getConnection() throws SQLException {
        return Pool.getConexion();
    }
}
