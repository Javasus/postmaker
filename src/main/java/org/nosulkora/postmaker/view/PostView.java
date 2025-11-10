package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.LabelController;
import org.nosulkora.postmaker.controller.PostController;
import org.nosulkora.postmaker.controller.WriterController;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.impl.JdbcPostRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.JdbcWriterRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class PostView {

    private final Scanner scanner;
    private final PostController postController;
    private final LabelController labelController;
    private final WriterController writerController;

    public PostView() {
        scanner = new Scanner(System.in);
        postController = new PostController(
                new JdbcWriterRepositoryImpl(),
                new JdbcPostRepositoryImpl()
        );
        labelController = new LabelController();
        writerController = new WriterController(new JdbcWriterRepositoryImpl());
    }

    public PostView(Scanner scanner) {
        this.scanner = scanner;
        postController = new PostController(
                new JdbcWriterRepositoryImpl(),
                new JdbcPostRepositoryImpl()
        );
        labelController = new LabelController();
        writerController = new WriterController(new JdbcWriterRepositoryImpl());
    }

    public PostView(
            Scanner scanner,
            WriterController writerController,
            PostController postController,
            LabelController labelController
    ) {
        this.scanner = scanner;
        this.writerController = writerController;
        this.postController = postController;
        this.labelController = labelController;
    }

    public void createPost() {
        System.out.println("Введи id писателя из существующих или напиши 'new', что бы создать нового: ");
        List<Writer> writers = writerController.getAllWriters();
        writers.forEach(System.out::println);
        Writer writer = null;
        while (Objects.isNull(writer)) {
            String text = scanner.nextLine();
            if (text.trim().matches("[0-9.]*")) {
                Long writerId = Long.parseLong(text);
                writer = writerController.getWriterById(writerId);
                if (Objects.isNull(writer)) {
                    System.out.println("При возврате писателя возникла ошибка.");
                }
            } else if (text.trim().matches("new")) {
                System.out.println("Enter writer firstName: ");
                String firstName = scanner.nextLine();
                System.out.println("Enter writer lastName: ");
                String lastName = scanner.nextLine();
                writer = writerController.createWriter(firstName, lastName);
                if (Objects.isNull(writer)) {
                    System.out.println("При создании писателя возникла ошибка");
                } else {
                    System.out.println("writer create: " + writer);
                }
            } else {
                System.out.println("Не верный ввод, попробуйте ещё раз.");
            }
        }
        System.out.println("Enter post title: ");
        String title = scanner.nextLine();
        System.out.println("Enter post content: ");
        String content = scanner.nextLine();
        List<Label> labels = addLabels();
        Post post = postController.createPost(title, content, writer.getId(), labels);
        if (Objects.isNull(post)) {
            System.out.println("При создании поста произощла ошибка");
        } else {
            System.out.println("post create : " + post);
        }
    }

    public void getPostById() {
        System.out.println("Enter postID: ");
        Long id = Long.parseLong(scanner.nextLine());
        Post post = postController.getPostById(id);
        if (Objects.isNull(post)) {
            System.out.println("При возврате поста по ID = " + id + " возникла ошибка");
        } else {
            System.out.println("Post by ID: " + post);
        }
    }

    public void getAllPosts() {
        List<Post> posts = postController.getAllPosts();
        if (Objects.isNull(posts)) {
            System.out.println("При возврате всех постов возникла ошибка");
        } else {
            posts.forEach(System.out::println);
        }
    }

    public void updatePost() {
        System.out.println("Enter postId: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.println("Enter new post title: ");
        String title = scanner.nextLine();
        System.out.println("Enter new post content: ");
        String content = scanner.nextLine();
        List<Label> labels = addLabels();
        Post post = postController.updatePost(id, title, content, labels);
        if (Objects.isNull(post)) {
            System.out.println("При обновлении поста с ID = " + id + " возникла ошибка");
        } else {
            System.out.println("post update : " + post);
        }
    }

    public void deletePost() {
        System.out.println("Введите postId: ");
        Long id = Long.parseLong(scanner.nextLine());
        if (postController.deletePost(id)) {
            System.out.println("Пост удалён");
        } else {
            System.out.println("При удалении поста возникла ошибка");
        }
    }

    private List<Label> addLabels() {
        System.out.println(
                "Введи один или несколько label или введи id существующего из списка. В конце введи пустую строку.");
        List<Label> allLabels = labelController.getAllLabels();
        if (Objects.isNull(allLabels)) {
            System.out.println("Возникла ошибка при возврате всех лейблов");
        } else {
            allLabels.forEach(System.out::println);
        }
        List<Label> result = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String text = scanner.nextLine();
            if (text.trim().isEmpty()) break;

            Label label = null;
            if (text.trim().matches("[0-9.]*")) {
                Long id = Long.parseLong(text);
                label = labelController.getLabelById(id);
                if (Objects.isNull(label)) {
                    System.out.println("При поиске лейбла по ID = " + id + " возникла ошибка");
                }
            }
            if (Objects.isNull(label)) {
                label = labelController.createLabel(text);
                if (Objects.isNull(label)) {
                    System.out.println("При создании лейбла c name = " + text + " возникла ошибка");
                }
            }
            result.add(label);
        }
        return result;
    }
}
