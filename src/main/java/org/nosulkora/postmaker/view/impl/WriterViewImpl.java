package org.nosulkora.postmaker.view.impl;

import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.view.WriterView;

import java.io.BufferedReader;
import java.io.IOException;

public class WriterViewImpl implements WriterView {

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";

    @Override
    public String[] getNameByView(BufferedReader reader) throws IOException {
        System.out.println("Введите имя и фамилию через пробел. Например: Иван Иванов");
        String command = reader.readLine().trim();
        String[] names = null;
        if (command.matches("[a-zA-Zа-яА-Я]+ [a-zA-Zа-яА-Я]+")) {
            names = command.split(" ");
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
        return names;
    }

    @Override
    public String getCommandForWriter(BufferedReader reader) throws IOException {
        System.out.println("""
                Для получения данных о пользователе(ях) введи следующий набор данных на выбор:
                - Имя и фамилию Writer'а через пробел, чтобы получить данные об одном пользователе.
                - ID Writer'а, чтобы получить данные об одном пользователе.
                - Введи команду 'All', чтобы получить информацию о всех пользователях.
                """);
        return reader.readLine().trim();
    }

    @Override
    public void createWriter(Writer writer, boolean isExist) {
        if (writer != null) {
            System.out.println(
                    (isExist ? "Этот пользователь уже существует -> " : "Создался пользователь -> ") + writer);
        } else {
            System.out.println("Пользователь не создался.");
        }
    }

    @Override
    public void showWriter(Writer writer) {
        System.out.println(writer != null ? writer.toString() : "Writer не найден.");
    }

    @Override
    public String updateWriter(BufferedReader reader) throws IOException {
        System.out.println("""
                Для редактирования данных о пользователе введи id - writer'а.
                """);
        return reader.readLine().trim();
    }
}
