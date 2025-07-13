package org.nosulkora.controller;

import org.nosulkora.model.Status;
import org.nosulkora.model.Writer;
import org.nosulkora.repository.GsonWriterRepositoryImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class MainController implements Controller {

    GsonWriterRepositoryImpl gsonWriterRepository = new GsonWriterRepositoryImpl();
    String firstName;
    String lastName;
    String titlePost;
    String contentPost;

    public void requestHandle(BufferedReader reader) throws IOException {

        System.out.println("""
                привет, пользователь. Выбери, с какой сущностью будешь работать
                Введи - 'w' , если хочешь работать пользователя.
                Введи - 'p' , если хочешь работать запись.
                Введи - 'l' , если хочешь работать label.
                """);

        if (reader.readLine().equals("writer")) {
            System.out.println("""
                    Что хочешь сделать с пользователем?
                    Введи - 'c' , если хочешь добавить пользователя.
                    Введи - 'r' , если хочешь посмотреть пользователя.
                    Введи - 'u' , если хочешь изменить пользователя.
                    Введи - 'd' , если хочешь удалить пользователя.
                    """);

            if (reader.readLine().equals("create")) {
                createWriter(reader);
            }

        }


    }

    private void createWriter(BufferedReader reader) throws IOException {

        System.out.println("Введите имя: ");
        firstName = reader.readLine();
        System.out.println("Введите фамилию: ");
        lastName = reader.readLine();
        Optional<Writer> existWriter = gsonWriterRepository.getWriter(firstName, lastName);
        if (existWriter.isEmpty()) {
            UUID id = UUID.randomUUID();
            Writer writer = new Writer(id, firstName, lastName, new ArrayList<>(), Status.ACTIVE);
            if (gsonWriterRepository.addWriter(writer)) {
                System.out.println("Создался пользователь -> " + writer);
            } else {
                System.out.println("Пользователь не создался");
            }
        } else {
            System.out.println("Этот пользователь уже существует: " + existWriter.get());
        }
    }
}
