package org.nosulkora.controller;

import org.nosulkora.model.Status;
import org.nosulkora.model.Writer;
import org.nosulkora.repository.WriterRepository;
import org.nosulkora.view.WriterView;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WriterController implements Controller{

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";

    private final WriterRepository writerRepository;
    private final WriterView writerView;

    public WriterController(WriterRepository writerRepository, WriterView writerView) {
        this.writerRepository = writerRepository;
        this.writerView = writerView;
    }

    @Override
    public void create(BufferedReader reader) throws IOException {

        String[] nameByView = writerView.getNameByView(reader);
        if (nameByView != null && nameByView.length == 2) {
            String firstName = nameByView[0];
            String lastName = nameByView[1];
            List<Writer> writersByName = writerRepository.getWriterByName(firstName, lastName);
            if (writersByName != null && !writersByName.isEmpty()) {
                writersByName.forEach(writer -> writerView.createWriter(writer, true));
            } else {
                Writer writer = new Writer(firstName, lastName, new ArrayList<>());
                if (writerRepository.createWriter(writer)) {
                    writerView.createWriter(writer, false);
                } else {
                    writerView.createWriter(null, false);
                }
            }
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    @Override
    public void read(BufferedReader reader) throws IOException {

        String command =writerView.getCommandForWriter(reader);
        if (command.equalsIgnoreCase("All")) {
            List<Writer> allWriters = writerRepository.getAllWriters();
            allWriters.forEach(writerView::showWriter);
        } else if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Writer writerById = writerRepository.getWriterById(id);
           writerView.showWriter(writerById);
        } else if (command.matches("[a-zA-Zа-яА-Я]+ [a-zA-Zа-яА-Я]+")) {
            String[] names = command.split(" ");
            String firstName = names[0];
            String lastName = names[1];
            List<Writer> writersByName = writerRepository.getWriterByName(firstName, lastName);
            writersByName.forEach(writerView::showWriter);
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    @Override
    public void update(BufferedReader reader) throws IOException {
        String command = writerView.updateWriter(reader);
        if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Writer writerById = writerRepository.getWriterById(id);
            if (writerById != null) {
               writerView.showWriter(writerById);
                String[] nameByView =writerView.getNameByView(reader);
                if (nameByView != null && nameByView.length == 2) {
                    String firstName = nameByView[0];
                    String lastName = nameByView[1];
                    Writer updatedWriter = writerRepository.updateWriter(id, firstName, lastName);
                    if (Objects.nonNull(updatedWriter)) {
                       writerView.showWriter(updatedWriter);
                    } else {
                        System.out.println(YOU_ARE_WRONG);
                    }
                }
            }
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    @Override
    public void delete(BufferedReader reader) throws IOException {
        String command =writerView.updateWriter(reader);
        if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Writer writerById = writerRepository.getWriterById(id);
            if (Objects.nonNull(writerById)) {
               writerView.showWriter(writerById);
                Writer deletedWriter = writerRepository.deleteWriterById(id);
                if (Objects.nonNull(deletedWriter)) {
                   writerView.showWriter(deletedWriter);
                } else {
                    System.out.println(YOU_ARE_WRONG);
                }
            }
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }
}
