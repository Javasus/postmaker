package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.LabelController;
import org.nosulkora.postmaker.controller.PostController;
import org.nosulkora.postmaker.controller.WriterController;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.impl.GsonLabelRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.JdbcLabelRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.JdbcPostRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.JdbcWriterRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class WriterView {

    private final Scanner scanner;
    private final WriterController writerController;
    private final PostController postController;
    private final LabelController labelController;

    public WriterView() {
        scanner = new Scanner(System.in);
        writerController = new WriterController(
                new JdbcWriterRepositoryImpl(),
                new JdbcPostRepositoryImpl(),
                new JdbcLabelRepositoryImpl()
        );
        postController = new PostController();
        labelController = new LabelController();
    }

    public WriterView(Scanner scanner) {
        this.scanner = scanner;
        writerController = new WriterController(
                new JdbcWriterRepositoryImpl(),
                new JdbcPostRepositoryImpl(),
                new GsonLabelRepositoryImpl()
        );
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
        Writer writer = writerController.createWriter(firstName, lastName, null);
//        List<Post> posts = addPosts(writer.getId());
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
        Writer writer = writerController.updateWriter(id, firstName, lastName, null);
//        List<Post> posts = addPosts(writer.getId());
//        writer.setPosts(posts);
        System.out.println("writer update : " + writer);
    }

    public void deleteWriter() {
        System.out.println("Enter writerID: ");
        Long id = Long.parseLong(scanner.nextLine());
        writerController.deleteWriter(id);
        System.out.println("Writer is delete");
    }

//    private List<Post> addPosts(Long writerId) {
//        System.out.println("Введи один или несколько post. В конце введи пустую строку.");
//        List<Post> posts = new ArrayList<>();
//        System.out.println("Enter post title: ");
//        while (scanner.hasNextLine()) {
//            String title = scanner.nextLine();
//            if (title.trim().isEmpty()) break;
//            System.out.println("Enter post content: ");
//            String content = scanner.nextLine();
//            List<Label> labels = addLabels();
//            Post post = postController.createPost(title, content, writerId, labels);
//            posts.add(post);
//            System.out.println("Enter post title: ");
//        }
//        return posts;
//    }
//
//    private List<Label> addLabels() {
//        System.out.println(
//                "Введи один или несколько label или введи id существующего из списка. " +
//                        "В конце введи пустую строку.");
//        List<Label> result = new ArrayList<>();
//        while (scanner.hasNextLine()) {
//            String text = scanner.nextLine();
//            if (text.trim().isEmpty()) break;
//
//            Label label = null;
//            if (text.trim().matches("[0-9.]*")) {
//                Long id = Long.parseLong(text);
//                label = labelController.getLabelById(id);
//            }
//
//            if (Objects.isNull(label)) {
//                label = labelController.createlabel(text);
//            }
//
//            result.add(label);
//        }
//        return result;
//    }
}
