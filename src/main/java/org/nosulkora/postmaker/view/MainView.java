package org.nosulkora.postmaker.view;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MainView {

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";

    private final LabelView labelView;
    private final PostView postView;
    private final WriterView writerView;
    private final Scanner scanner;

    private final String choiceCommand = """       
            Выбери действие:
                     Введи - 'c' , если хочешь добавить сущность.
                     Введи - 'r' , если хочешь посмотреть сущность.
                     Введи - 'ra' , если хочешь посмотреть все сущности.
                     Введи - 'u' , если хочешь изменить сущность.
                     Введи - 'd' , если хочешь удалить сущность.
            """;

    public MainView() {
        scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        labelView = new LabelView();
        postView = new PostView();
        writerView = new WriterView();
    }

    public MainView(LabelView labelView, PostView postView, WriterView writerView, Scanner scanner) {
        this.labelView = labelView;
        this.postView = postView;
        this.writerView = writerView;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("""  
                    Выбери, с какой сущностью будешь работать:
                        Введи - 'w' , если хочешь работать с Writer.
                        Введи - 'p' , если хочешь работать с Post.
                        Введи - 'l' , если хочешь работать с label.
                        Введи - 'exit' , чтобы выйти.
                    """);

            System.out.print("Ваш выбор: ");

            if (!scanner.hasNextLine()) {
                System.out.println("Нет ввода, завершение программы");
                break;
            }

            String command = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(command)) {
                System.out.println("Выход из программы");
                break;
            }

            switch (command) {
                case "w" -> writerMenu();
                case "p" -> postMenu();
                case "l" -> labelMenu();
                default -> System.out.println(YOU_ARE_WRONG);
            }
        }
        scanner.close();
    }

    private void labelMenu() {
        System.out.println(choiceCommand);
        System.out.print("Ваш выбор: ");

        if (!scanner.hasNextLine()) return;
        String command = scanner.nextLine().trim();

        switch (command) {
            case "c" -> labelView.createLabel();
            case "r" -> labelView.getLabelById();
            case "ra" -> labelView.getAllLabels();
            case "u" -> labelView.updateLabel();
            case "d" -> labelView.deleteLabel();
            default -> System.out.println(YOU_ARE_WRONG);
        }
    }

    private void postMenu() {
        System.out.println(choiceCommand);
        System.out.print("Ваш выбор: ");

        if (!scanner.hasNextLine()) return;
        String command = scanner.nextLine().trim();

        switch (command) {
            case "c" -> postView.createPost();
            case "r" -> postView.getPostById();
            case "ra" -> postView.getAllPosts();
            case "u" -> postView.updatePost();
            case "d" -> postView.deletePost();
            default -> System.out.println(YOU_ARE_WRONG);
        }
    }

    private void writerMenu() {
        System.out.println(choiceCommand);
        System.out.print("Ваш выбор: ");

        if (!scanner.hasNextLine()) return;
        String command = scanner.nextLine().trim();

        switch (command) {
            case "c" -> writerView.createWriter();
            case "r" -> writerView.getWriterById();
            case "ra" -> writerView.getAllWriters();
            case "u" -> writerView.updateWriter();
            case "d" -> writerView.deleteWriter();
            default -> System.out.println(YOU_ARE_WRONG);
        }
    }
}
