package org.nosulkora.postmaker.repository.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.LabelRepository;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GsonLabelRepositoryImpl implements LabelRepository {

    private final String FILE_PATH = "src/main/resources/labels.json";
    private final Gson GSON = new Gson();

    @Override
    public Label save(Label label) {
        List<Label> existingLabels = getAllLabelsInternal();
        long id = generateId(existingLabels);
        label.setId(id);
        existingLabels.add(label);
        writeLabelsToFile(existingLabels);
        return label;
    }

    @Override
    public Label getById(Long id) {
        return getAllLabelsInternal().stream()
                .filter(label -> label.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Label update(Label label) {
        List<Label> updatedLabels = getAllLabelsInternal().stream()
                .map(currentLabel -> {
                    if (currentLabel.getId().equals(label.getId())) {
                        return label;
                    }
                    return currentLabel;
                }).toList();
        writeLabelsToFile(updatedLabels);
        return label;
    }

    @Override
    public List<Label> getAll() {
        return getAllLabelsInternal();
    }

    @Override
    public void deleteById(Long id) {
        List<Label> updatedLabels = getAllLabelsInternal().stream()
                .peek(currentLabel -> {
                    if (currentLabel.getId().equals(id)) {
                        currentLabel.setStatus(Status.DELETED);
                    }
                }).toList();
        writeLabelsToFile(updatedLabels);
    }

    private void writeLabelsToFile(List<Label> labels) {
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            GSON.toJson(labels, fileWriter);
        } catch (IOException e) {
            System.out.println("Ошибка записи.");
        }
    }

    private List<Label> getAllLabelsInternal() {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Label>>() {
            }.getType();
            List<Label> existing = GSON.fromJson(fileReader, type);
            return existing != null ? existing : new ArrayList<>();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private Long generateId(List<Label> labels) {
        return labels.stream()
                .mapToLong(Label::getId)
                .max().orElse(0L) + 1;
    }
}
