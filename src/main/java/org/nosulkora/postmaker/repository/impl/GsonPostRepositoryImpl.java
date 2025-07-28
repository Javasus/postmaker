package org.nosulkora.postmaker.repository.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.repository.PostRepository;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GsonPostRepositoryImpl implements PostRepository {

    private static final String FILE_PATH = "posts.json";
    Gson gson = new Gson();

    @Override
    public Boolean createPost(Post post) {
        List<Post> postsFromJson = getAllPosts();
        postsFromJson.add(post);
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            gson.toJson(postsFromJson, fileWriter);
            return true;
        } catch (IOException e) {
            System.out.println("Ошибка записи.");
            return false;
        }
    }

    @Override
    public List<Post> getAllPosts() {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Post>>() {
            }.getType();
            List<Post> existing = gson.fromJson(fileReader, type);
            return existing != null ? existing : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Post getPostById(Long id) {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<Post>>() {
            }.getType();
            List<Post> posts = gson.fromJson(fileReader, userListType);
            Optional<Post> post = posts.stream().filter(pst -> pst.getId().equals(id)).findFirst();
            return post.orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Post updatePost(Post updatePost) {
        List<Post> allPosts = getAllPosts();
        Long updatePostId = updatePost.getId();
        allPosts.forEach(
                post -> {
                    if (post.getId().equals(updatePostId)) {
                        post.setTitle(updatePost.getTitle());
                        post.setContent(updatePost.getContent());
                        post.setLabels(updatePost.getLabels());
                        post.setStatus(updatePost.getStatus());
                    }
                });
        return addPosts(allPosts) ?
                allPosts.stream().filter(post -> post.getId().equals(updatePostId)).findFirst().orElse(null) :
                null;
    }

    @Override
    public Post updatePostWithNewLabel(Long postId, Label label) {
        List<Post> allPosts = getAllPosts();
        Post updatePost = null;
        for (Post post : allPosts) {
            if (post.getId().equals(postId)) {
                List<Label> labels = post.getLabels();
                labels.add(label);
                post.setLabels(labels);
                updatePost = post;
                break;
            }
        }
        return addPosts(allPosts) ? updatePost : null;
    }

    @Override
    public Post updateLabelInPost(Label label) {
        List<Post> allPosFromJson = getAllPosts();
        Post updatePost = null;
        for (Post post : allPosFromJson) {
            for (Label lbl : post.getLabels()) {
                if (lbl.getId().equals(label.getId())) {
                    lbl.setName(label.getName());
                    lbl.setStatus(label.getStatus());
                    updatePost = post;
                    break;
                }
            }
            if (Objects.nonNull(updatePost)) {
                break;
            }
        }
        return addPosts(allPosFromJson) ? updatePost : null;
    }

    /**
     * записывает в файл posts.json обновленную коллекцию постов.
     *
     * @param posts обновленная коллекция постов
     * @return boolean
     */
    private boolean addPosts(List<Post> posts) {
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            gson.toJson(posts, fileWriter);
            return true;
        } catch (IOException e) {
            System.out.println("Ошбка записи.");
            return false;
        }
    }
}
