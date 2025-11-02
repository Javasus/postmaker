package org.nosulkora.postmaker.repository;

import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.exceptions.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;

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
     * Выполняет UPDATE/DELETE операцию
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

//    /**
//     * Выполняет SELECT запрос и возвращает одну сущность
//     */
//    public static <T> T executeQuerySingle(String sql, BiFunction<ResultSet, T, T> mapper, Long param) {
//        return executeAutoCommit(sql, ps -> {
//            try {
//                ps.setLong(1, param);
//                try (ResultSet rs = ps.executeQuery()) {
//                    T cur = null;
//                    while (rs.next()) {
//                        T entity = mapper.apply(rs, cur);
//                        if (cur != null && !entity.equals(cur)){
//                            return cur;
//                        } else {
//                            cur = entity;
//                        }
//                    }
//                    return cur;
//                }
//            } catch (SQLException e) {
//                throw new RepositoryException("Ошибка выполнения запроса: " + sql, e);
//            }
//        });
//    }

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
     * Выполняет SELECT запрос с параметром и возвращает список сущностей
     */
    public static <T> List<T> executeQueryList(String sql, Function<ResultSet, T> mapper, Long id) {
        return executeAutoCommit(sql, ps -> {
            try {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> results = new ArrayList<>();
                    while (rs.next()) {
                        T entity = mapper.apply(rs);
                        if (entity != null) {
                            results.add(entity);
                        }
                    }
                    return results;
                } catch (SQLException e) {
                    throw new RepositoryException("Ошибка выполнения запроса.", e);
                }
            } catch (SQLException e) {
                throw new RepositoryException("Ошибка выполнения запроса: " + sql, e);
            }
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
     * Выполняет batch операцию в транзакции
     */
    public static void executeBatch(String sql, Consumer<PreparedStatement> batchSetter) {
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                batchSetter.accept(ps);
                ps.executeBatch();
            } catch (SQLException e) {
                throw new RepositoryException("Ошибка выполнения batch операции: " + sql, e);
            }
            return null;
        });
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


    /**
     * Возвращает PreparedStatement.
     *
     * @param sql   скрпит
     * @param param id объекта
     * @return PreparedStatement
     * @throws SQLException
     */
    public static PreparedStatement createAutoCommitStatement(String sql, Long param) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(true);
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, param);
        return ps;
    }

    /**
     * Возвращает PreparedStatement.
     *
     * @param sql скрпит
     * @return PreparedStatement
     * @throws SQLException
     */
    public static PreparedStatement createAutoCommitStatement(String sql) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(true);
        return conn.prepareStatement(sql);
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
