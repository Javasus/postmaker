package org.nosulkora.postmaker.controller;

import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.LabelRepository;
import org.nosulkora.postmaker.repository.impl.JdbcLabelRepositoryImpl;

import java.util.List;
import java.util.Objects;

public class LabelController {

    private final LabelRepository labelRepository;

    public LabelController() {
        this.labelRepository = new JdbcLabelRepositoryImpl();
    }

    public LabelController(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    public Label createLabel(String name) throws RepositoryException {
        Label label = new Label();
        label.setName(name);
        label.setStatus(Status.ACTIVE);
        return labelRepository.save(label);
    }

    public Label getLabelById(Long id) throws RepositoryException {
        return labelRepository.getById(id);
    }

    public List<Label> getAllLabels() throws RepositoryException {
        return labelRepository.getAll();
    }

    public Label updateLabel(Long id, String name) throws RepositoryException {
        Label label = labelRepository.getById(id);
        if (Objects.nonNull(label)) {
            label.setName(name);
            return labelRepository.update(label);
        }
        return null;
    }

    public boolean deleteLabel(Long id) throws RepositoryException {
        return labelRepository.deleteById(id);
    }
}
