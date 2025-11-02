package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.WriterController;
import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.impl.JdbcPostRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.JdbcWriterRepositoryImpl;

import java.util.List;
import java.util.Scanner;

public class WriterView {

    private final Scanner scanner;
    private final WriterController writerController;

    public WriterView() {
        scanner = new Scanner(System.in);
        writerController = new WriterController(new JdbcWriterRepositoryImpl());
    }

    public WriterView(Scanner scanner) {
        this.scanner = scanner;
        writerController = new WriterController(new JdbcWriterRepositoryImpl());
    }

    public WriterView(Scanner scanner, WriterController writerController) {
        this.scanner = scanner;
        this.writerController = writerController;
    }

    public void createWriter() {
        System.out.println("Enter writer firstName: ");
        String firstName = scanner.nextLine();
        System.out.println("Enter writer lastName: ");
        String lastName = scanner.nextLine();
        try {
            Writer writer = writerController.createWriter(firstName, lastName);
            System.out.println("writer create: " + writer);
        } catch (RepositoryException e) {
            System.out.println("При создании писателя возникла ошибка - " + e);
        }
    }

    public void getWriterById() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        try {
            Writer writer = writerController.getWriterById(id);
            System.out.println("writer by ID: " + writer);
        } catch (RepositoryException e) {
            System.out.println("При возврате писателя возникла ошибка - " + e);
        }
    }

    public void getAllWriters() {
        try {
            List<Writer> writers = writerController.getAllWriters();
            writers.forEach(System.out::println);
        } catch (RepositoryException e) {
            System.out.println("При возврате всех писателей возникла ошибка - " + e);
        }
    }

    public void updateWriter() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.println("Enter writer firstName: ");
        String firstName = scanner.nextLine();
        System.out.println("Enter writer lastName: ");
        String lastName = scanner.nextLine();
        try {
            Writer writer = writerController.updateWriter(id, firstName, lastName);
            System.out.println("writer update : " + writer);
        } catch (RepositoryException e) {
            System.out.println("При обновлении писателя возникла ошибка - " + e);
        }
    }

    public void deleteWriter() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        try {
            writerController.deleteWriter(id);
            System.out.println("Writer is delete");
        } catch (RepositoryException e) {
            System.out.println("При удалении писателя возникла ошибка - " + e);
        }
    }
}
