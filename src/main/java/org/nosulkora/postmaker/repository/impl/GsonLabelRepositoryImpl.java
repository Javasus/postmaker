package org.nosulkora.postmaker.repository.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.repository.LabelRepository;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GsonLabelRepositoryImpl implements LabelRepository {

    private static final String FILE_PATH = "labels.json";
    private final Gson GSON = new Gson();

    @Override
    public Boolean createLabel(Label label) {
        List<Label> allLabelsFromJson = getAllLabels();
        allLabelsFromJson.add(label);
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            GSON.toJson(allLabelsFromJson, fileWriter);
            return true;
        } catch (IOException e) {
            System.out.println("Ошибка записи.");
            return false;
        }
    }

    @Override
    public List<Label> getAllLabels() {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Label>>() {
            }.getType();
            List<Label> existing = GSON.fromJson(fileReader, type);
            return existing != null ? existing : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Label getLabelById(Long id) {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<Label>>() {
            }.getType();
            List<Label> labels = GSON.fromJson(fileReader, userListType);
            Optional<Label> label = labels.stream().filter(lbl -> lbl.getId().equals(id)).findFirst();
            return label.orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Label updateLabel(Label label) {
        List<Label> allLabelsFromJson = getAllLabels();
        Label updateLabel = null;
        for (Label lbl : allLabelsFromJson) {
            if (lbl.getId().equals(label.getId())) {
                lbl.setName(label.getName());
                lbl.setStatus(label.getStatus());
                updateLabel = lbl;
                break;
            }
        }
        return addLabels(allLabelsFromJson) ? updateLabel : null;
    }

    /**
     * записывает в файл labels.json обновленную коллекцию постов.
     *
     * @param labels обновленная коллекция постов
     * @return boolean
     */
    private boolean addLabels(List<Label> labels) {
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            GSON.toJson(labels, fileWriter);
            return true;
        } catch (IOException e) {
            System.out.println("Ошбка записи.");
            return false;
        }
    }
}
