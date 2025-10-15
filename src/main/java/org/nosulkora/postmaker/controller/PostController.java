package org.nosulkora.postmaker.controller;

import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.LabelRepository;
import org.nosulkora.postmaker.repository.PostRepository;
import org.nosulkora.postmaker.repository.WriterRepository;
import org.nosulkora.postmaker.repository.impl.JdbcLabelRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.JdbcPostRepositoryImpl;
import org.nosulkora.postmaker.repository.impl.JdbcWriterRepositoryImpl;

import java.util.List;
import java.util.Objects;

public class PostController {

    private final WriterRepository writerRepository;
    private final PostRepository postRepository;

    public PostController(
            WriterRepository writerRepository,
            PostRepository postRepository
    ) {
        this.writerRepository = writerRepository;
        this.postRepository = postRepository;
    }

    public PostController() {
        this.postRepository = new JdbcPostRepositoryImpl();
        this.writerRepository = new JdbcWriterRepositoryImpl();
    }

    public Post createPost(String title, String content, Long writerId, List<Label> labels) {

        Writer writer = writerRepository.getById(writerId);
        if (Objects.isNull(writer)) {
            throw new IllegalArgumentException("Писатель с ID " + writerId + " не найден");
        }
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setWriterId(writer.getId());
        post.setLabels(labels);
        post.setStatus(Status.ACTIVE);
        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.getById(id);
    }

    public List<Post> getAllPosts() {
        return postRepository.getAll();
    }

    public Post updatePost(Long id, String title, String content, List<Label> labels) {
        Post post = postRepository.getById(id);
        if (Objects.isNull(post)) {
            throw new IllegalArgumentException("Пост с ID " + id + " не найден");
        }
        post.setTitle(title);
        post.setContent(content);
        post.setLabels(labels);
        return postRepository.update(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
