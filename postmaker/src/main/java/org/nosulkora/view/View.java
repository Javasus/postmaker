package org.nosulkora.view;

import java.io.BufferedReader;
import java.io.IOException;

public class View {

    public String runStartView(BufferedReader reader) throws IOException {
        System.out.println("""
                 
                 Выбери, с какой сущностью будешь работать:
                     Введи - 'w' , если хочешь работать с Writer.
                     Введи - 'p' , если хочешь работать с Post.
                     Введи - 'l' , если хочешь работать с label.
                 
                 выбери действие:
                     Введи - 'c' , если хочешь добавить сущность.
                     Введи - 'r' , если хочешь посмотреть сущность.
                     Введи - 'u' , если хочешь изменить сущность.
                     Введи - 'd' , если хочешь удалить сущность.
                                       
                Введи эти команды через пробел. Например: w c
                 """);
        return reader.readLine().trim();
    }
}
