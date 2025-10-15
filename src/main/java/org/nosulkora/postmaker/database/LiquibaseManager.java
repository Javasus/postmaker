package org.nosulkora.postmaker.database;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

public class LiquibaseManager {
    /**
     * Запуск миграции без контекста - только схема.
     */
    public static void runMigrations() {
        runMigrations("");
    }

    /**
     * Запуск миграции - только для тестов. Тестовые данные добавляются в БД.
     */
    public static void runTestMigrations() {
        runMigrations("test");
    }

    public static void runMigrations(String context) {
        try (Connection connection = DatabaseManager.getConnection()) {
            // Настройка и запуск Liquibase
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            // Выполняем миграции
            liquibase.update(new Contexts(context), new LabelExpression());
            System.out.println("Миграции Liquibase выполнены c контекстом: " + context);
        } catch (Exception e) {
            System.out.println("Ошибка выполнения миграций: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void rollback(int changesToRollback) {
        try (Connection connection = DatabaseManager.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.rollback(changesToRollback, new Contexts(), new LabelExpression());
            System.out.println("Откат на " + changesToRollback + " изменений выполнен!");

        } catch (Exception e) {
            System.out.println("Ошибка отката миграций: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
