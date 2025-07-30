package org.nosulkora.postmaker.controller;

import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.PostRepository;
import org.nosulkora.postmaker.repository.impl.GsonPostRepositoryImpl;

import java.util.List;

public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostController() {
        postRepository = new GsonPostRepositoryImpl();
    }

    public Post createPost(String title, String content, List<Label> labels) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
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
        post.setTitle(title);
        post.setContent(content);
        post.setLabels(labels);
        return postRepository.update(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }


}
