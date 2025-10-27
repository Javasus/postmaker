package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.WriterController;
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
        Writer writer = writerController.createWriter(firstName, lastName);
        System.out.println("writer create: " + writer);
    }

    public void getWriterById() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        Writer writer = writerController.getWriterById(id);
        System.out.println("writer by ID: " + writer);
    }

    public void getAllWriters() {
        List<Writer> writers = writerController.getAllWriters();
        writers.forEach(System.out::println);
    }

    public void updateWriter() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.println("Enter writer firstName: ");
        String firstName = scanner.nextLine();
        System.out.println("Enter writer lastName: ");
        String lastName = scanner.nextLine();
        Writer writer = writerController.updateWriter(id, firstName, lastName);
        System.out.println("writer update : " + writer);
    }

    public void deleteWriter() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        writerController.deleteWriter(id);
        System.out.println("Writer is delete");
    }
}
