package org.nosulkora.controller;

import org.nosulkora.model.Status;
import org.nosulkora.model.Writer;
import org.nosulkora.repository.GsonWriterRepositoryImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController implements Controller {


    private GsonWriterRepositoryImpl gsonWriterRepository = new GsonWriterRepositoryImpl();

    public void requestHandle(BufferedReader reader) throws IOException {

        System.out.println("""
                Привет, пользователь! Выбери, с какой сущностью будешь работать:
                Введи - 'w' , если хочешь работать с Writer.
                Введи - 'p' , если хочешь работать с Post.
                Введи - 'l' , если хочешь работать с label.
                """);

        if (reader.readLine().equals("w")) {
            System.out.println("""
                    Что хочешь сделать с сущностью?
                    Введи - 'c' , если хочешь добавить пользователя.
                    Введи - 'r' , если хочешь посмотреть пользователя.
                    Введи - 'u' , если хочешь изменить пользователя.
                    Введи - 'd' , если хочешь удалить пользователя.
                    """);

            switch (reader.readLine()) {
                case "c" -> createWriter(reader);
                case "r" -> readWriter(reader);

//                case "u":
//                    updateWriter(reader);
//                    break;
//                case "d":
//                    deleteWriter(reader);
//                    break;
                default -> System.out.println("Введена не верная команда");
            }

        }


    }

    private void createWriter(BufferedReader reader) throws IOException {

        System.out.println("Введите имя: ");
        String firstName = reader.readLine();
        System.out.println("Введите фамилию: ");
        String lastName = reader.readLine();
        Optional<Writer> writerByName = gsonWriterRepository.getWriterByName(firstName, lastName);
        writerByName.ifPresentOrElse(
                writer -> System.out.println("Этот пользователь уже существует: \n" + writer),
                () -> {
                    Writer writer = new Writer(firstName, lastName, new ArrayList<>(), Status.ACTIVE);
                    if (gsonWriterRepository.addWriter(writer)) {
                        System.out.println("Создался пользователь -> \n" + writer);
                    } else {
                        System.out.println("Пользователь не создался.");
                    }
                }
        );
//        TODO delete
//        if (existWriter.isEmpty()) {
//            Writer writer = new Writer(firstName, lastName, new ArrayList<>(), Status.ACTIVE);
//            if (gsonWriterRepository.addWriter(writer)) {
//                System.out.println("Создался пользователь -> " + writer);
//            } else {
//                System.out.println("Пользователь не создался");
//            }
//        } else {
//            System.out.println("Этот пользователь уже существует: " + existWriter.get());
//        }
    }

    private void readWriter(BufferedReader reader) throws IOException {
        System.out.println("""
                Для получения данных о пользователе(ях) введи следующий набор данных на выбор
                - Имя и фамилию Writer'а через пробел, что бы получить данные об одном пользователе.
                - ID Writer'а, что бы получить данные об одном пользователе.
                - Введи команду 'All', что бы получить информацию о всех пользователях.
                """);
        String command = reader.readLine().trim();

        if (command.equalsIgnoreCase("All")) {
            List<Writer> allWriters = gsonWriterRepository.getAllWriters();
            System.out.println("Список всех writer'ов:");
            allWriters.forEach(writer -> System.out.println(writer.toString()));
        } else if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Optional<Writer> writerById = gsonWriterRepository.getWriterById(id);
            writerById.ifPresentOrElse(
                    writer -> System.out.println("Writer по id = " + id + "найден: \n" + writer),
                    () -> System.out.println("Writer по id = " + id + " не найден.")
            );
        } else if (command.matches("[a-zA-Zа-яА-Я]+ [a-zA-Zа-яА-Я]+")) {
            String[] names = command.split(" ");
            String firstName = names[0];
            String lastName = names[1];
            Optional<Writer> writerByName = gsonWriterRepository.getWriterByName(firstName, lastName);
            writerByName.ifPresentOrElse(
                    writer -> System.out.println("Writer с ФИО = %s %s найден: "
                            .formatted(firstName, lastName) + writer),
                    () -> System.out.println("Writer с ФИО = %s %s не найден."
                            .formatted(firstName, lastName)));
        } else {
            System.out.println("Ты ввёл не верные данный. Пожалуйста следуй инструкциям.");
        }

    }
}
