package org.nosulkora.view;

import org.nosulkora.model.Writer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

public class View {

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";

    public String[] runStartView(BufferedReader reader) throws IOException {
        System.out.println("""
                 
                 Выбери, с какой сущностью будешь работать:
                     Введи - 'w' , если хочешь работать с Writer.
                     Введи - 'p' , если хочешь работать с Post.
                     Введи - 'l' , если хочешь работать с label.
                 
                 и выбери действие:
                     Введи - 'c' , если хочешь добавить пользователя.
                     Введи - 'r' , если хочешь посмотреть пользователя.
                     Введи - 'u' , если хочешь изменить пользователя.
                     Введи - 'd' , если хочешь удалить пользователя.
                     
                Введи эти команды через пробел. Например: w c
                 """);
        String command = reader.readLine().trim();
        String[] commands = null;

            if (command.matches("[wpl]+ [crud]+")) {
                commands = command.split(" ");
            } else {
                System.out.println(YOU_ARE_WRONG);
            }
        return commands;
    }

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

    public String getCommandForWriter(BufferedReader reader) throws IOException {
        System.out.println("""
                Для получения данных о пользователе(ях) введи следующий набор данных на выбор:
                - Имя и фамилию Writer'а через пробел, чтобы получить данные об одном пользователе.
                - ID Writer'а, чтобы получить данные об одном пользователе.
                - Введи команду 'All', чтобы получить информацию о всех пользователях.
                """);
        return reader.readLine().trim();
    }

    public void createWriter(Writer writer, boolean isExist) {
        if (writer != null) {
            System.out.println(
                    (isExist ? "Этот пользователь уже существует -> " : "Создался пользователь -> ") + writer);
        } else {
            System.out.println("Пользователь не создался.");
        }
    }

    public void readWriter(Writer writer) {
        System.out.println(writer != null ? writer.toString() : "Writer не найден.");
    }

    public void readWriter(Optional<Writer> writerOp) {
        writerOp.ifPresentOrElse(
                writer -> System.out.println("Writer найден -> " + writer),
                () -> System.out.println("Writer не найден.")
        );
    }

//    TODO удалить
    public void updatewriter(BufferedReader reader) {
        System.out.println("""
                Для редактирования данных о пользователе(ях) введи следующий набор данных на выбор:
                - Имя и фамилию Writer'а через пробел.
                - ID Writer'а.
                """);
    }
}
