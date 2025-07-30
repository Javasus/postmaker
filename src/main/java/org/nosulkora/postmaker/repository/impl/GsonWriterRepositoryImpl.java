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

    private static final String FILE_PATH = "src/main/resources/writers.json";
    private final Gson GSON = new Gson();

    @Override
    public Writer save(Writer writer) {
        List<Writer> existingWriter = getAllWritersInternal();
        Long id = generateId(existingWriter);
        writer.setId(id);
        existingWriter.add(writer);
        writeWriterToFile(existingWriter);
        return writer;
    }

    @Override
    public Writer getById(Long id) {
        return getAllWritersInternal().stream()
                .filter(writer -> writer.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Writer> getAll() {
        return getAllWritersInternal();
    }

    @Override
    public Writer update(Writer writer) {
        List<Writer> updatedWriters = getAllWritersInternal().stream()
                .map(currentWriter -> {
                    if (currentWriter.getId().equals(writer.getId())) {
                        return writer;
                    }
                    return currentWriter;
                })
                .toList();
        writeWriterToFile(updatedWriters);
        return writer;
    }

    @Override
    public void deleteById(Long id) {
        List<Writer> writers = getAllWritersInternal().stream()
                .map(currentWriter -> {
                    if (currentWriter.getId().equals(id)) {
                        currentWriter.setStatus(Status.DELETED);
                    }
                    return currentWriter;
                })
                .toList();
        writeWriterToFile(writers);
    }

    private void writeWriterToFile(List<Writer> writers) {
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            GSON.toJson(writers, fileWriter);
        } catch (IOException e) {
            System.out.println("Ошбка записи.");
        }
    }

    private List<Writer> getAllWritersInternal() {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> existing = GSON.fromJson(fileReader, type);
            return existing != null ? existing : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private Long generateId(List<Writer> writer) {
        return writer.stream()
                .mapToLong(Writer::getId)
                .max().orElse(0L) + 1;
    }
}
