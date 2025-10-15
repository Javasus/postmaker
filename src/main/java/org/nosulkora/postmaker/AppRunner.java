package org.nosulkora.postmaker;

import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.database.LiquibaseManager;
import org.nosulkora.postmaker.view.MainView;

public class AppRunner {
    public static void main(String[] args) {
        try {
            // Запускаем миграции Liquibase
            LiquibaseManager.runMigrations();

            // Запускаем приложение
            MainView mainView = new MainView();
            mainView.start();

        } catch (Exception e) {
            System.out.println("Ошибка запуска приложения: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeConnection();
        }
    }
}