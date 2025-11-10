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
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private ConnectionManager() {
    }

    /**
     * Выполняет INSERT и возвращает сгенерированный ID
     */
    public static Long executeInsert(String sql, Consumer<PreparedStatement> parameterSetter) {
        logger.debug("Выполнение INSERT: {}", sql);
        try {
            return executeTransaction(conn -> {
                        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                            parameterSetter.accept(ps);
                            if (ps.executeUpdate() == 0) {
                                logger.warn("Вставка не удалась, ни одна строка не была затронута: {}", sql);
                                return null;
                            }
                            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    return generatedKeys.getLong(1);
                                } else {
                                    logger.warn("Вставка не удалась, ID не был получен: {}", sql);
                                    return null;
                                }
                            }
                        } catch (SQLException e) {
                            logger.error("Ошибка выполнения вставки: {}", sql, e);
                            return null;
                        }
                    }
            );
        } catch (Exception e) {
            logger.error("Ошибка выполнения вставки: {}", sql, e);
            return null;
        }
    }

    /**
     * Выполняет UPDATE/DELETE операции
     */
    public static int executeUpdate(String sql, Consumer<PreparedStatement> parameterSetter) {
        logger.debug("Выполнение UPDATE/DELETE: {}", sql);
        try {
            return executeAutoCommit(sql, ps -> {
                try {
                    parameterSetter.accept(ps);
                    return ps.executeUpdate();
                } catch (SQLException e) {
                    logger.error("Ошибка выполнения UPDATE/DELETE: " + sql, e);
                    return -1;
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка выполнения UPDATE/DELETE: " + sql, e);
            return -1;
        }
    }

    /**
     * Выполняет SELECT запрос и возвращает одну сущность
     */
    public static <T> T executeQuerySingle(String sql, Function<ResultSet, T> mapper, Long param) {
        logger.debug("Выполнение SELECT single: {} с параметром: {}", sql, param);
        try {
            return executeAutoCommit(sql, ps -> {
                try {
                    ps.setLong(1, param);
                    try (ResultSet rs = ps.executeQuery()) {
                        return mapper.apply(rs);
                    }
                } catch (SQLException e) {
                    logger.error("Ошибка выполнения запроса: {}", sql, e);
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка выполнения запроса: {}", sql, e);
            return null;
        }
    }

    /**
     * Выполняет SELECT запрос и возвращает список сущностей
     */
    public static <T> List<T> executeQueryList(String sql, Function<ResultSet, List<T>> mapper) {
        logger.debug("Выполнение SELECT list: {}", sql);
        try {
            return executeAutoCommit(sql, ps -> {
                try (ResultSet rs = ps.executeQuery()) {
                    return mapper.apply(rs);
                } catch (SQLException e) {
                    logger.error("Ошибка при выполнении SELECT операции: {}", sql, e);
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка при выполнении SELECT операции: {}", sql, e);
            return null;
        }
    }

    /**
     * Выполняет batch операцию в транзакции
     */
    public static boolean executeBatch(String sql, Consumer<PreparedStatement> batchSetter) {
        logger.debug("Выполнение BATCH: {}", sql);
        try {
            executeTransaction(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    batchSetter.accept(ps);
                    ps.executeBatch();
                } catch (SQLException e) {
                    logger.error("SQL ошибка при выполнении BATCH: {}", sql, e);
                    throw new RuntimeException(e);
                }
                return null;
            });
            return true;
        } catch (Exception e) {
            logger.error("SQL ошибка при выполнении BATCH: {}", sql, e);
            return  false;
        }
    }

    /**
     * Выполняет операцию в транзакции с кастомной логикой
     */
    public static <T> T executeTransaction(Function<Connection, T> operation) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            logger.debug("Транзакция начата");

            T result = operation.apply(conn);
            conn.commit();
            logger.debug("Транзакция завершена успешно");
            return result;
        } catch (SQLException e) {
            safeRollback(conn);
            logger.error("Ошибка выполнения транзакции", e);
            throw new RuntimeException("Ошибка транзакции", e);
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
            logger.error("Ошибка подключения к БД при выполнении: {}", sql, e);
            throw new RuntimeException("Ошибка подключения к БД", e);
        }
    }

    /**
     * Безопасно выполняет откат транзакции
     */
    private static void safeRollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                logger.debug("Откат транзакции выполнен");
            } catch (SQLException e) {
                logger.error("Ошибка при откате транзакции", e);
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
                logger.debug("Соединение с БД закрыто");
            } catch (SQLException e) {
                logger.error("Ошибка при закрытии соединения", e);
            }
        }
    }
}
