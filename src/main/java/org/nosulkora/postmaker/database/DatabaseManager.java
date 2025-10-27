package org.nosulkora.postmaker.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final HikariDataSource dataSource;

    static {
        // настройки подключения
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3307/postmaker");
        config.setUsername("appuser");
        config.setPassword("apppassword");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // настройки пула
        config.setMaximumPoolSize(20); // макс соединений
        config.setMinimumIdle(5); // мин простаивающих соединений
        config.setConnectionTimeout(30000); // 30сек таймаут на получ. соед.
        config.setIdleTimeout(600000); // 10мин время жизни простаивающего соед.
        config.setMaxLifetime(1800000); // 30мин макисмальное время жизни соед.
        config.setAutoCommit(true); // автокомит по умолчанию

        // оптимизация для MySQL
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("serverTimezone", "UTC");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Для миграции
    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariCP pool closed");
        }
    }

    /**
     * Статистика пула для мониторинга
     */
    public static void printPoolStats() {
        if (dataSource != null) {
            System.out.println("HikariCP Pool Stats:");
            System.out.println("Active connections: " + dataSource.getHikariPoolMXBean().getActiveConnections());
            System.out.println("Idle connections: " + dataSource.getHikariPoolMXBean().getIdleConnections());
            System.out.println("Total connections: " + dataSource.getHikariPoolMXBean().getTotalConnections());
            System.out.println("Threads awaiting connection: " + dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
    }

}
