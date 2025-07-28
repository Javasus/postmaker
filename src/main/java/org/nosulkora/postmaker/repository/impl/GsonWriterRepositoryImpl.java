package org.nosulkora.postmaker.repository.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GsonWriterRepositoryImpl implements WriterRepository {

    private static final String FILE_PATH = "writers.json";
    Gson gson = new Gson();

    @Override
    public Boolean createWriter(Writer writer) {
        List<Writer> writers = getAllWriters();
        writers.add(writer);
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            gson.toJson(writers, fileWriter);
            return true;
        } catch (IOException e) {
            System.out.println("Ошибка записи.");
            return false;
        }
    }

    @Override
    public List<Writer> getWriterByName(String firstname, String lastName) {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> writers = gson.fromJson(fileReader, userListType);
            return writers.stream()
                    .filter(writer -> writer.getFirstName().equals(firstname))
                    .filter(writer -> writer.getLastname().equals(lastName)).toList();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Writer> getAllWriters() {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> existing = gson.fromJson(fileReader, type);
            return existing != null ? existing : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Writer getWriterById(Long id) {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> writers = gson.fromJson(fileReader, userListType);
            Optional<Writer> writer = writers.stream().filter(wr -> wr.getId().equals(id)).findFirst();
            return writer.orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Writer updateWriter(Long id, String name, String LastName) {
        List<Writer> allWriters = getAllWriters();
        allWriters.forEach(writer -> {
            if (writer.getId().equals(id)) {
                writer.setFirstName(name);
                writer.setLastname(LastName);
            }
        });
        return addWriters(allWriters) ?
                allWriters.stream().filter(writer -> writer.getId().equals(id)).findFirst().orElse(null) :
                null;
    }

    @Override
    public Writer deleteWriterById(Long id) {
        List<Writer> allWriters = getAllWriters();
        List<Writer> updatedWriters = allWriters.stream().peek(writer -> {
            if (writer.getId().equals(id)) {
                writer.setStatus(Status.DELETED);
            }
        }).collect(Collectors.toList());
        return addWriters(updatedWriters) ?
                updatedWriters.stream().filter(writer -> writer.getId().equals(id)).findFirst().orElse(null) :
                null;
    }

    @Override
    public Writer updateWriterWithNewPost(Long writerId, Post post) {
        List<Writer> allWriters = getAllWriters();
        allWriters.forEach(writer -> {
                    if (writer.getId().equals(writerId)) {
                        List<Post> posts = writer.getPosts();
                        posts.add(post);
                        writer.setPosts(posts);
                    }
                }
        );
        return addWriters(allWriters) ?
                allWriters.stream().filter(writer -> writer.getId().equals(writerId)).findFirst().orElse(null) :
                null;
    }

    @Override
    public Writer updatePostInWriter(Post updatePost) {
        List<Writer> allWriters = getAllWriters();
        Writer updateWriter = null;
        for (Writer writer : allWriters) {
            for (Post post : writer.getPosts()) {
                if (post.getId().equals(updatePost.getId())) {
                    post.setTitle(updatePost.getTitle());
                    post.setContent(updatePost.getContent());
                    post.setLabels(updatePost.getLabels());
                    if (updatePost.getStatus().equals(Status.DELETED)) {
                        post.setStatus(updatePost.getStatus());
                    }
                    updateWriter = writer;
                    break;
                }
            }
            if (Objects.nonNull(updateWriter)) {
                break;
            }
        }
        return addWriters(allWriters) ? updateWriter : null;
    }

    /**
     * записывает в файл writers.json обновленную коллекцию пользователей.
     *
     * @param writers обновленная коллекция пользователей
     * @return boolean
     */
    private boolean addWriters(List<Writer> writers) {
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            gson.toJson(writers, fileWriter);
            return true;
        } catch (IOException e) {
            System.out.println("Ошбка записи.");
            return false;
        }
    }
}
