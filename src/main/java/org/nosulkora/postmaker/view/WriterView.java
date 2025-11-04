package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.WriterController;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.impl.JdbcWriterRepositoryImpl;

import java.util.List;
import java.util.Objects;
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
        System.out.println("Введите имя писателя: ");
        String firstName = scanner.nextLine();
        System.out.println("Введите фамилию писателя: ");
        String lastName = scanner.nextLine();
        Writer writer = writerController.createWriter(firstName, lastName);
        if (Objects.isNull(writer)) {
            System.out.println("При создании писателя возникла ошибка");
        } else {
            System.out.println("Писатель создан: " + writer);
        }
    }

    public void getWriterById() {
        System.out.println("Введите writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        Writer writer = writerController.getWriterById(id);
        if (Objects.isNull(writer)) {
            System.out.println("При возврате писателя по ID = " + id + " возникла ошибка");
        } else {
            System.out.println("Писатель по ID: " + writer);
        }
    }

    public void getAllWriters() {
        List<Writer> writers = writerController.getAllWriters();
        if (Objects.isNull(writers)) {
            System.out.println("При возврате всех писателей возникла ошибка");
        } else {
            writers.forEach(System.out::println);
        }
    }

    public void updateWriter() {
        System.out.println("Введите writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.println("Ведите имя писателя: ");
        String firstName = scanner.nextLine();
        System.out.println("Введите фамилию писателя: ");
        String lastName = scanner.nextLine();
        Writer writer = writerController.updateWriter(id, firstName, lastName);
        if (Objects.isNull(writer)) {
            System.out.println("При обновлении писателя с ID = " + id + " возникла ошибка");
        } else {
            System.out.println("Писатель обновлен: " + writer);
        }
    }

    public void deleteWriter() {
        System.out.println("Введите writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        if (writerController.deleteWriter(id)) {
            System.out.println("Писатель удалён");
        } else {
            System.out.println("При удалении писателя с ID = " + id + " возникла ошибка - ");
        }
    }
}
