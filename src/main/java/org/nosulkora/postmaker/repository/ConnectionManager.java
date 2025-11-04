package org.nosulkora.postmaker.repository;

import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConnectionManager {

    private ConnectionManager() {
    }

    /**
     * Выполняет INSERT и возвращает сгенерированный ID
     */
    public static Long executeInsert(String sql, Consumer<PreparedStatement> parameterSetter) {
        return executeTransaction(conn -> {
                    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        parameterSetter.accept(ps);
                        if (ps.executeUpdate() == 0) {
                            throw new RepositoryException(
                                    "Вставка не удалась, ни одна строка не была затронута: " + sql);
                        }
                        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                return generatedKeys.getLong(1);
                            } else {
                                throw new RepositoryException("Вставка не удалась, ID не был получен: " + sql);
                            }
                        }
                    } catch (SQLException e) {
                        throw new RepositoryException("Ошибка выполнения вставки: " + sql, e);
                    }
                }
        );
    }

    /**
     * Выполняет UPDATE/DELETE операции
     */
    public static int executeUpdate(String sql, Consumer<PreparedStatement> parameterSetter) throws RepositoryException {
        return executeAutoCommit(sql, ps -> {
            try {
                parameterSetter.accept(ps);
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new RepositoryException("Ошибка выполнения UPDATE/DELETE: " + sql, e);
            }
        });
    }

    /**
     * Выполняет SELECT запрос и возвращает одну сущность
     */
    public static <T> T executeQuerySingle(String sql, Function<ResultSet, T> mapper, Long param) {
        return executeAutoCommit(sql, ps -> {
            try {
                ps.setLong(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapper.apply(rs);
                }
            } catch (SQLException e) {
                throw new RepositoryException("Ошибка выполнения запроса: " + sql, e);
            }
        });
    }

    /**
     * Выполняет SELECT запрос и возвращает список сущностей
     */
    public static <T> List<T> executeQueryList(String sql, Function<ResultSet, List<T>> mapper) {
        return executeAutoCommit(sql, ps -> {
            try (ResultSet rs = ps.executeQuery()) {
                return mapper.apply(rs);
            } catch (SQLException e) {
                throw new RepositoryException("Ошибка выполнения запроса.", e);
            }
        });
    }

    /**
     * Выполняет batch операцию в транзакции
     */
    public static void executeBatch(String sql, Consumer<PreparedStatement> batchSetter) {
            executeTransaction(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    batchSetter.accept(ps);
                    ps.executeBatch();
                } catch (SQLException e) {
                    throw new RepositoryException("Ошибка выполнения запроса.", e);
                }
                return null;
            });
    }

    /**
     * Выполняет операцию в транзакции с кастомной логикой
     */
    public static <T> T executeTransaction(Function<Connection, T> operation) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            T result = operation.apply(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            safeRollback(conn);
            throw new RepositoryException("ошибка выполнения транзакции." + e);
        } finally {
            safeClose(conn);
        }
    }

    /**
     * Выполняет операцию с автокоммитом
     */
    private static <T> T executeAutoCommit(String sql, Function<PreparedStatement, T> operation) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(true);
            return operation.apply(ps);
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка выполнения запроса: " + sql, e);
        }
    }

    /**
     * Безопасно выполняет откат транзакции
     */
    private static void safeRollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                throw new RepositoryException("Ошибка отката транзакции." + e);
            }
        }
    }

    /**
     * Безопасно закрывает соединение
     */
    private static void safeClose(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
                conn.close();
            } catch (SQLException e) {
                throw new RepositoryException("Ошибка закрытия соединения." + e);
            }
        }
    }
}
