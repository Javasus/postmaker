package org.nosulkora.postmaker.controller;

import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.LabelRepository;
import org.nosulkora.postmaker.repository.PostRepository;
import org.nosulkora.postmaker.repository.WriterRepository;

import java.util.List;

public class WriterController {

    private final WriterRepository writerRepository;
    private final PostRepository postRepository;
    private final LabelRepository labelRepository;

    public WriterController(
            WriterRepository writerRepository,
            PostRepository postRepository,
            LabelRepository labelRepository
    ) {
        this.writerRepository = writerRepository;
        this.postRepository = postRepository;
        this.labelRepository = labelRepository;
    }

    public Writer createWriter(String firstName, String lastName, List<Post> posts) {
        Writer writer = new Writer();
        writer.setFirstName(firstName);
        writer.setLastName(lastName);
//        writer.setPosts(posts);
        writer.setStatus(Status.ACTIVE);
        Writer savedWriter = writerRepository.save(writer);
//        for (Post post : posts) {
//            post.setWriter(savedWriter);
//            for (Label label : post.getLabels()) {
//                Label savedLabel = labelRepository.save(label);
//                label.setId(savedLabel.getId());
//            }
//            Post savedPost = postRepository.save(post);
//            post.setId(savedPost.getId());
//        }
//        savedWriter.setPosts(posts);
        return savedWriter;
    }

    public Writer getWriterById(Long id) {
        Writer writer = writerRepository.getById(id);
        if (writer != null) {
            List<Post> posts = postRepository.getPostsByWriterId(id);
            writer.setPosts(posts);
        }
        return writer;
    }

    public List<Writer> getAllWriter() {
        List<Writer> writers = writerRepository.getAll();
        for (Writer writer : writers) {
            List<Post> posts = postRepository.getPostsByWriterId(writer.getId());
            writer.setPosts(posts);
        }
        return writers;
    }

    public Writer updateWriter(Long id, String firstName, String lastName, List<Post> posts) {
        Writer writer = writerRepository.getById(id);
        writer.setFirstName(firstName);
        writer.setLastName(lastName);
        Writer updateWriter = writerRepository.update(writer);
        List<Post> postsByWriterId = postRepository.getPostsByWriterId(updateWriter.getId());
        updateWriter.setPosts(postsByWriterId);
//        List<Post> oldPosts = postRepository.getPostsByWriterId(id);
//        for (Post oldPost : oldPosts) {
//            postRepository.deleteById(oldPost.getId());
//        }
//        if (posts != null) {
//            for (Post post : posts) {
//                post.setWriter(updateWriter);
//
//                if (post.getLabels() != null) {
//                    for (Label label : post.getLabels()) {
//                        Label savedLabel = labelRepository.save(label);
//                        label.setId(savedLabel.getId());
//                    }
//                }
//                postRepository.save(post);
//            }
//            updateWriter.setPosts(posts);
//        }
        return updateWriter;

    }

    public void deleteWriter(Long id) {
        List<Post> posts = postRepository.getPostsByWriterId(id);
        for (Post post : posts) {
            postRepository.deleteById(post.getId());
        }
        writerRepository.deleteById(id);
    }
}
