package org.nosulkora.controller;

import org.nosulkora.model.Post;
import org.nosulkora.model.Status;
import org.nosulkora.model.Writer;
import org.nosulkora.repository.PostRepository;
import org.nosulkora.repository.WriterRepository;
import org.nosulkora.view.PostView;
import org.nosulkora.view.WriterView;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostController implements Controller {

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";

    private final PostRepository postRepository;
    private final WriterRepository writerRepository;
    private final PostView postView;
    private final WriterView writerView;

    public PostController(
            PostRepository postRepository,
            WriterRepository writerRepository,
            PostView postView,
            WriterView writerView
    ) {
        this.postRepository = postRepository;
        this.writerRepository = writerRepository;
        this.postView = postView;
        this.writerView = writerView;
    }

    @Override
    public void create(BufferedReader reader) throws IOException {

        String[] postByView = postView.getPostByView(reader, "Введи id writer'а от которого хочешь запостить.");
        if (postByView != null && postByView.length == 3 && postByView[2].matches("\\d+")) {
            String title = postByView[0];
            String content = postByView[1];
            Long writerId = Long.parseLong(postByView[2]);
            Post post = new Post(title, content, new ArrayList<>());
            if (postRepository.createPost(writerId, post)) {
                postView.showPost(post);
                Writer writer = writerRepository.updateWriterWithNewPost(writerId, post);
                writerView.showWriter(writer);
            }
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        String command = postView.getCommandForPost(reader);
        if (command.equalsIgnoreCase("All")) {
            List<Post> allPosts = postRepository.getAllPosts();
            allPosts.forEach(postView::showPost);
        } else if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Post postById = postRepository.getPostById(id);
            postView.showPost(postById);
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    @Override
    public void update(BufferedReader reader) throws IOException {
        String[] updatePostData = postView.getPostByView(
                reader,
                "Введи id post'а который хочешь изменить."
        );
        if (updatePostData != null && updatePostData.length == 3 && updatePostData[2].matches("\\d+")) {
            String title = updatePostData[0];
            String content = updatePostData[1];
            Long postId = Long.parseLong(updatePostData[2]);
            Post post = postRepository.getPostById(postId);
            if (Objects.nonNull(post)) {
                post.setTitle(title);
                post.setContent(content);
                Post updatePost = postRepository.updatePost(post);
                if (Objects.nonNull(updatePost)) {
                    postView.showPost(updatePost);
                    Writer writer = writerRepository.updatePostInWriter(updatePost);
                    writerView.showWriter(writer);
                }
            } else {
                System.out.println(YOU_ARE_WRONG);
            }
        }
    }

    @Override
    public void delete(BufferedReader reader) throws IOException {
        String command = postView.updatePost(reader);
        if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Post post = postRepository.getPostById(id);
            if (Objects.nonNull(post)) {
                Post deletedPost = postRepository.deletePostById(id);
                if (Objects.nonNull(deletedPost)) {
                    postView.showPost(deletedPost);
                    Writer writer = writerRepository.updatePostInWriter(deletedPost);
                    if (Objects.nonNull(writer)) {
                        writerView.showWriter(writer);
                    }
                } else {
                    System.out.println(YOU_ARE_WRONG);
                }
            }
        } else {
            System.out.println(YOU_ARE_WRONG);
        }

    }
}
