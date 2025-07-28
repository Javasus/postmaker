package org.nosulkora.postmaker;

import org.nosulkora.postmaker.controller.MainController;
import org.nosulkora.postmaker.repository.impl.GsonLabelRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.GsonPostRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.GsonWriterRepositoryImpl;
import org.nosulkora.postmaker.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {

        MainController mainController = new MainController(
                new GsonWriterRepositoryImpl(),
                new GsonPostRepositoryImpl(),
                new GsonLabelRepositoryImpl(),
                new View()
        );

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                mainController.requestHandle(reader);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage() + e.getCause());
        }
    }
}