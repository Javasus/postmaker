package org.nosulkora.postmaker.controller;

import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.PostRepository;
import org.nosulkora.postmaker.repository.WriterRepository;
import org.nosulkora.postmaker.view.PostView;
import org.nosulkora.postmaker.view.WriterView;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostController implements Controller {

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";

    private final PostRepository postRepository;
    private final PostView postView;
    private final WriterRepository writerRepository;
    private final WriterView writerView;

    public PostController(
            PostRepository postRepository,
            PostView postView,
            WriterRepository writerRepository,
            WriterView writerView
    ) {
        this.postRepository = postRepository;
        this.postView = postView;
        this.writerRepository = writerRepository;
        this.writerView = writerView;
    }

    @Override
    public void create(BufferedReader reader) throws IOException {

        String[] postByView = postView.getPostByView(reader, "Введи id writer'а от которого хочешь запостить.");
        if (postByView != null
                && postByView.length == 3
                && postByView[2].matches("\\d+")
                && !postByView[0].isEmpty()
                && !postByView[1].isEmpty()
        ) {
            String title = postByView[0];
            String content = postByView[1];
            Long writerId = Long.parseLong(postByView[2]);
            Post post = new Post(title, content, new ArrayList<>());
            if (postRepository.createPost(post)) {
                postView.showPost(post);
                Writer writer = writerRepository.updateWriterWithNewPost(writerId, post);
                if (Objects.nonNull(writer)) {
                    writerView.showWriter(writer);
                } else {
                    System.out.println(YOU_ARE_WRONG);
                }
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
//                Если мы нашли пост с таким id, меняем его данные и обновляем его.
                post.setTitle(title);
                post.setContent(content);
                Post updatePost = postRepository.updatePost(post);
                if (Objects.nonNull(updatePost)) {
//                    Если пост обновился отправляем его, что бы он обновился у писателя.
                    postView.showPost(updatePost);
                    Writer writer = writerRepository.updatePostInWriter(updatePost);
                    if (Objects.nonNull(writer)) {
                        writerView.showWriter(writer);
                    } else {
                        System.out.println(YOU_ARE_WRONG);
                    }
                }
            } else {
                System.out.println(YOU_ARE_WRONG);
            }
        }
    }

    @Override
    public void delete(BufferedReader reader) throws IOException {

        String command = postView.deletePost(reader);
        if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Post post = postRepository.getPostById(id);
            if (Objects.nonNull(post)) {
//                Если мы нашли пост с таким id, меняем его статус Status.DELETED и обновляем его.
                post.setStatus(Status.DELETED);
                Post deletedPost = postRepository.updatePost(post);
                if (Objects.nonNull(deletedPost)) {
//                    Если пост обновился отправлем его, что бы он обновился у писателя.
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
