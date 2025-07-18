package org.nosulkora.view.impl;

import org.nosulkora.model.Post;
import org.nosulkora.view.PostView;

import java.io.BufferedReader;
import java.io.IOException;

public class PostViewImpl implements PostView {

    @Override
    public String[] getPostByView (BufferedReader reader, String message) throws IOException {
        String[] post = new String[3];
        if (!message.isEmpty()) {
            System.out.println(message);
            String writerId = reader.readLine().trim();
            post[2] = writerId;
        }
        System.out.println("Введи заголовок.");
        String title = reader.readLine().trim();
        post[0] = title;
        System.out.println("Введи контент.");
        String content = reader.readLine().trim();
        post[1] = content;

        return post;
    }

    @Override
    public String getCommandForPost(BufferedReader reader) throws IOException {
        System.out.println("""
                Для получения данных о посте(ах) введи следующий набор данных на выбор:
                - ID Post'а, чтобы получить данные об одном посте.
                - Введи команду 'All', чтобы получить информацию о всех постах.
                """);
        return reader.readLine().trim();
    }
    @Override
    public void showPost(Post post) {
        System.out.println(post != null ? post.toString() : "Writer не найден.");
    }

    @Override
    public String updatePost(BufferedReader reader) throws IOException {
        System.out.println("""
                Для редактирования данных поста введи id - post'а.
                """);
        return reader.readLine().trim();
    }

}
