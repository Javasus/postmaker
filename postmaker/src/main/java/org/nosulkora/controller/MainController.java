package org.nosulkora.controller;

import org.nosulkora.model.Status;
import org.nosulkora.model.Writer;
import org.nosulkora.repository.WriterRepository;
import org.nosulkora.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController implements Controller {

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";

    private WriterRepository writerRepository;
    private View view;

    public MainController(WriterRepository writerRepository, View view) {
        this.writerRepository = writerRepository;
        this.view = view;
    }

    public void requestHandle(BufferedReader reader) throws IOException {

        String[] commands = view.runStartView(reader);

        if (commands != null) {
            String commandFirst = commands[0];
            String commandSecond = commands[1];
            if (commandFirst.equals("w")) {

                switch (commandSecond) {
                    case "c" -> createWriter(reader);
                    case "r" -> readWriter(reader);
                    case "u" -> updateWriter(reader);
//                case "d":
//                    deleteWriter(reader);
//                    break;
                    default -> System.out.println(YOU_ARE_WRONG);
                }
            }

        }
    }

    private void createWriter(BufferedReader reader) throws IOException {

        String[] nameByView = view.getNameByView(reader);
        if (nameByView != null) {
            String firstName = nameByView[0];
            String lastName = nameByView[1];
            Writer writerByName = writerRepository.getWriterByName(firstName, lastName);
            if (writerByName != null) {
                view.createWriter(writerByName, true);
            } else {
                Writer writer = new Writer(firstName, lastName, new ArrayList<>(), Status.ACTIVE);
                if (writerRepository.createWriter(writer)) {
                    view.createWriter(writer, false);
                } else {
                    view.createWriter(null, false);
                }
            }
        }
    }

    private void readWriter(BufferedReader reader) throws IOException {

        String command = view.getCommandForWriter(reader);
        if (command.equalsIgnoreCase("All")) {
            List<Writer> allWriters = writerRepository.getAllWriters();
            System.out.println("Список всех writer'ов:");
            allWriters.forEach(writer -> view.readWriter(writer));
        } else if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Writer writerById = writerRepository.getWriterById(id);
            view.readWriter(writerById);
        } else if (command.matches("[a-zA-Zа-яА-Я]+ [a-zA-Zа-яА-Я]+")) {
            String[] names = command.split(" ");
            String firstName = names[0];
            String lastName = names[1];
            Writer writerByName = writerRepository.getWriterByName(firstName, lastName);
            view.readWriter(writerByName);
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    private void updateWriter(BufferedReader reader) {


    }
}
