/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAccessTier;

import exceptions.ExistingUserException;
import exceptions.InactiveUserException;
import exceptions.ServerException;
import exceptions.UserCredentialException;
import java.awt.BorderLayout;
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
 *
 * @authors Ander
 * @authors Aitziber
 */
public class DAO implements Signable {

    private static final Logger logger = Logger.getLogger(DAO.class.getName());

    /**
     *
     * @param user
     * @return
     * @throws ServerException
     * @throws ExistingUserException
     */
    @Override
    public User signUp(User user) throws ServerException, ExistingUserException {
        PreparedStatement stmtPartner = null;
        PreparedStatement stmtUser = null;
        ResultSet generatedKeys = null;

        String insertPartner = "INSERT INTO res_partner(company_id, name, street, city, zip, email) VALUES (1, ?, ?, ?, ?, ?);";
        String insertUser = "INSERT INTO res_users(company_id, partner_id, login, password, active, notification_type) VALUES (1, ?, ?, ?, ?, 'email');";

        // Usamos una conexión local
        try (Connection connection = getConnection()) {
            // Verificar si el usuario ya existe
            if (checkUserExistence(user.getEmail())) {
                throw new ExistingUserException("User already exists");
            }

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
            generatedKeys = stmtPartner.getGeneratedKeys();
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

            return user;

        } catch (SQLException e) {
            throw new ServerException("SERVER ERROR. Error searching user");

        } finally {
            // Cerrar recursos
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
                throw new ServerException("SERVER ERROR. Error searching user");
            }
        }
    }

    /**
     *
     * @param email
     * @return
     * @throws ServerException
     */
    public boolean checkUserExistence(String email) throws ServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String searchUser = "SELECT login FROM res_users WHERE login = ?;";

        // Usamos una conexión local
        try (Connection connection = getConnection()) {
            if (connection == null) {
                throw new ServerException("SERVER ERROR. La conexión con la base de datos es nula.");
            }
            // Preparar la consulta con parámetros para evitar inyecciones SQL
            stmt = connection.prepareStatement(searchUser);
            stmt.setString(1, email);

            // Ejecutar la consulta y obtener los resultados
            rs = stmt.executeQuery();

            // Retornar true si el usuario existe
            return rs.next();

        } catch (SQLException e) {
            throw new ServerException("SERVER ERROR. Error searching user");

        } finally {
            // Cerrar ResultSet y PreparedStatement en el bloque finally
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                throw new ServerException("SERVER ERROR. Error searching user");
            }
        }
    }

    /**
     *
     * @param user
     * @return
     * @throws ServerException
     * @throws UserCredentialException
     * @throws InactiveUserException
     */
    @Override
    public User signIn(User user) throws ServerException, UserCredentialException, InactiveUserException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String selectUserData = "SELECT u.login, u.password, u.active, p.name, p.street, p.city, p.zip "
                + "FROM res_users u "
                + "JOIN res_partner p ON u.partner_id = p.id "
                + "WHERE u.login = ? AND u.password = ?";

        // Usamos una conexión local
        try (Connection connection = getConnection()) {
            // Preparar la consulta con parámetros para evitar inyecciones SQL
            stmt = connection.prepareStatement(selectUserData);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());

            // Ejecutar la consulta y obtener los resultados
            rs = stmt.executeQuery();

            // Si el usuario existe y está activo, asignar los valores a un nuevo objeto User
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
            throw new ServerException("SERVER ERROR. Error searching user");

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                throw new ServerException("SERVER ERROR. Error searching user");
            }
        }
    }

    // Accede a la conexión de la clase Pool
    private Connection getConnection() throws SQLException {
        return Pool.getConexion();
    }
}
