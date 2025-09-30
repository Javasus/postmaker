package org.nosulkora.postmaker.repository.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.PostRepository;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class GsonPostRepositoryImpl implements PostRepository {

    private final String FILE_PATH = "src/main/resources/posts.json";
    private final Gson GSON = new Gson();

    @Override
    public Post save(Post post) {
        List<Post> existingPost = getAllPostsInternal();
        Long id = generateId(existingPost);
        post.setId(id);
        existingPost.add(post);
        writePostsToFile(existingPost);
        return post;
    }

    @Override
    public Post getById(Long id) {
        return getAllPostsInternal().stream()
                .filter(post -> post.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Post update(Post post) {
        List<Post> existingPost = getAllPostsInternal().stream()
                .map(currentPost -> {
                    if (currentPost.getId().equals(post.getId())) {
                        return post;
                    }
                    return currentPost;
                }).toList();
        writePostsToFile(existingPost);
        return post;
    }

    @Override
    public List<Post> getAll() {
        return getAllPostsInternal();
    }

    @Override
    public void deleteById(Long id) {
        List<Post> updatedPosts = getAllPostsInternal().stream()
                .peek(currentPost -> {
                    if (currentPost.getId().equals(id)) {
                        currentPost.setStatus(Status.DELETED);
                    }
                })
                .toList();
        writePostsToFile(updatedPosts);
    }

    private void writePostsToFile(List<Post> posts) {
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            GSON.toJson(posts, fileWriter);
        } catch (IOException e) {
            System.out.println("Ошбка записи.");
        }
    }

    private List<Post> getAllPostsInternal() {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Post>>() {
            }.getType();
            List<Post> existing = GSON.fromJson(fileReader, type);
            return existing != null ? existing : new ArrayList<>();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private Long generateId(List<Post> posts) {
        return posts.stream()
                .mapToLong(Post::getId)
                .max().orElse(0L) + 1;
    }
}
