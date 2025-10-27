package org.nosulkora.postmaker.repository;

import org.nosulkora.postmaker.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionManager {

    private ConnectionManager() {
    }

    /**
     * Возвращает транзакционное соединение для простых операций.
     */
    public static Connection autoCommitConnection() throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * Возвращает транзакционное соединение для сложных операций.
     * Операции - save, update.
     */
    public static Connection transactionalConnection() throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    /**
     * Создаёт PreparedStatement с возвратом сгенерированных ключей.
     */
    public static PreparedStatement preparedStatementWithKeys(
            Connection connection,
            String sql
    ) throws SQLException {
        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    /**
     * Выполняет коммит транзакции с обработкой ошибок.
     */
    public static void commit(Connection connection) {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка коммита транзакции: " + e);
        }
    }

    /**
     * Выполняет откат транзакции с обработкой ошибок.
     */
    public static void rollback(Connection connection) {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка отката транзакции: " + e);
        }
    }
}
