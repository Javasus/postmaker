package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.LabelController;
import org.nosulkora.postmaker.controller.PostController;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PostView {

    private final Scanner scanner;
    private final PostController postController;
    private final LabelController labelController;

    public PostView(Scanner scanner, PostController postController, LabelController labelController) {
        this.scanner = scanner;
        this.postController = postController;
        this.labelController = labelController;
    }

    public PostView() {
        scanner = new Scanner(System.in);
        postController = new PostController();
        labelController = new LabelController();
    }

    public void createPost() {
        System.out.println("Enter post title: ");
        String title = scanner.nextLine();
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
        System.out.println("post create : " + post);
    }

    public void getPostById() {
        System.out.println("Enter postID: ");
        Long id = Long.parseLong(scanner.nextLine());
        Post post = postController.getPostById(id);
        System.out.println("Post by ID: " + post);
    }

    public void getAllPosts() {
        List<Post> posts = postController.getAllPosts();
        posts.forEach(System.out::println);
    }

    public void updatePost() {
        System.out.println("Enter postId: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.println("Enter new post title: ");
        String title = scanner.nextLine();
        System.out.println("Enter new post content: ");
        String content = scanner.nextLine();
        System.out.println("Введи один или несколько label. В конце введи пустую строку.");
        List<Label> labels = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String name = scanner.nextLine();
            if (name.trim().isEmpty()) break;
            Label label = labelController.createlabel(name);
            labels.add(label);
        }
        Post post = postController.updatePost(id, title, content, labels);
        System.out.println("post update : " + post);
    }

    public void deletePost() {
        System.out.println("Enter postId: ");
        Long id = Long.parseLong(scanner.nextLine());
        postController.deletePost(id);
        System.out.println("Post is deleted.");
    }
}
