package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.LabelController;
import org.nosulkora.postmaker.controller.PostController;
import org.nosulkora.postmaker.controller.WriterController;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WriterView {

    private final Scanner scanner;
    private final WriterController writerController;
    private final PostController postController;
    private final LabelController labelController;

    public WriterView() {
        scanner = new Scanner(System.in);
        writerController = new WriterController();
        postController = new PostController();
        labelController = new LabelController();
    }

    public WriterView(
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

    public void createWriter() {
        System.out.println("Enter writer firstName: ");
        String firstName = scanner.nextLine();
        System.out.println("Enter writer lastName: ");
        String lastName = scanner.nextLine();
        System.out.println("Введи один или несколько post. В конце введи пустую строку.");
        List<Post> posts = new ArrayList<>();
        System.out.println("Enter post title: ");
        while (scanner.hasNextLine()) {
            String title = scanner.nextLine();
            if (title.trim().isEmpty()) break;
            System.out.println("Enter post content: ");
            String content = scanner.nextLine();
            System.out.println("Введи один или несколько label. В конце введи пустую строку.");
            List<Label> labels = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine();
                if (name.trim().isEmpty()) break;
                Label label = labelController.createlabel(name);
                labels.add(label);
            }
            Post post = postController.createPost(title, content, labels);
            posts.add(post);
            System.out.println("Enter post title: ");
        }
        Writer writer = writerController.createWriter(firstName, lastName, posts);
        System.out.println("writer create: " + writer);
    }

    public void getWriterById() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        Writer writer = writerController.getWriterById(id);
        System.out.println("writer by ID: " + writer);
    }

    public void getAllWriters() {
        List<Writer> writers = writerController.getAllWriter();
        writers.forEach(System.out::println);
    }

    public void updateWriter() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.println("Enter writer firstName: ");
        String firstName = scanner.nextLine();
        System.out.println("Enter writer lastName: ");
        String lastName = scanner.nextLine();
        System.out.println("Введи один или несколько post. В конце введи пустую строку.");
        List<Post> posts = new ArrayList<>();
        System.out.println("Enter post title: ");
        while (scanner.hasNextLine()) {
            String title = scanner.nextLine();
            if (title.trim().isEmpty()) break;
            System.out.println("Enter post content: ");
            String content = scanner.nextLine();
            System.out.println("Введи один или несколько label. В конце введи пустую строку.");
            List<Label> labels = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine();
                if (name.trim().isEmpty()) break;
                Label label = labelController.createlabel(name);
                labels.add(label);
            }
            Post post = postController.createPost(title, content, labels);
            posts.add(post);
            System.out.println("Enter post title: ");
        }
        Writer writer = writerController.updateWriter(id, firstName, lastName, posts);
        System.out.println("writer update : " + writer);
    }

    public void deleteWriter() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        writerController.deleteWriter(id);
        System.out.println("Writer is delete");
    }
}
