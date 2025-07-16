package org.nosulkora.controller;

import org.nosulkora.repository.LabelRepository;
import org.nosulkora.repository.PostRepository;
import org.nosulkora.repository.WriterRepository;
import org.nosulkora.view.*;

import java.io.BufferedReader;
import java.io.IOException;

public class MainController {

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";


    private final View view;
    private final WriteController writeController;
    private final PostController postController;
    private final LableController lableController;

    public MainController(
            WriterRepository writerRepository,
            PostRepository postRepository,
            LabelRepository labelRepository,
            View view
    ) {
        this.writeController = new WriteController(writerRepository, new WriterViewImpl());
        this.postController = new PostController(postRepository, new PostViewImpl());
        this.lableController = new LableController(labelRepository, new LabelViewImpl());
        this.view = view;
    }

    public void requestHandle(BufferedReader reader) throws IOException {

        String command = view.runStartView(reader);
        String[] commands;
        if (command.matches("[wpl]+ [crud]+")) {
            commands = command.split(" ");
        } else {
            System.out.println(YOU_ARE_WRONG);
            return;
        }

        String commandFirst = commands[0];
        String commandSecond = commands[1];

        switch (commandFirst) {
            case "w" -> {
                switch (commandSecond) {
                    case "c" -> writeController.create(reader);
                    case "r" -> writeController.read(reader);
                    case "u" -> writeController.update(reader);
                    case "d" -> writeController.delete(reader);
                    default -> System.out.println(YOU_ARE_WRONG);
                }
            }
            case "p" -> {
                switch (commandSecond) {
                    case "c" -> postController.create(reader);
                    case "r" -> postController.read(reader);
                    case "u" -> postController.update(reader);
                    case "d" -> postController.delete(reader);
                    default -> System.out.println(YOU_ARE_WRONG);
                }
            }
            case "l" -> {
                switch (commandSecond) {
                    case "c" -> lableController.create(reader);
                    case "r" -> lableController.read(reader);
                    case "u" -> lableController.update(reader);
                    case "d" -> lableController.delete(reader);
                    default -> System.out.println(YOU_ARE_WRONG);
                }
            }
        }
    }
}
