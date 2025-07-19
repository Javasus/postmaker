package org.nosulkora.view.impl;

import org.nosulkora.model.Label;
import org.nosulkora.view.LabelView;

import java.io.BufferedReader;
import java.io.IOException;

public class LabelViewImpl implements LabelView {

    @Override
    public String[] getLabelByView(BufferedReader reader, String message) throws IOException {
        String[] label = new String[2];
        if (!message.isEmpty()) {
            System.out.println(message);
            String postId = reader.readLine().trim();
            label[0] = postId;
        }
        System.out.println("Введи имя лейбла.");
        String name = reader.readLine().trim();
        label[1] = name;
        return label;
    }

    @Override
    public String getCommandForLabel(BufferedReader reader) throws IOException {
        System.out.println("""
                Для получения данных о лейбле(ах) введи следующий набор данных на выбор:
                - ID Lable'а, чтобы получить данные об одном лейбле.
                - Введи команду 'All', чтобы получить информацию о всех лейблах.
                """);
        return reader.readLine().trim();
    }

    @Override
    public void showLabel(Label label) {
        System.out.println(label != null ? label.toString() : "Label не найден.");
    }

    @Override
    public String deleteLabel(BufferedReader reader) throws IOException {
        System.out.println("""
                Для удаления лейбла введи его id.
                """);
        return reader.readLine().trim();
    }
}
