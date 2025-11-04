package org.nosulkora.postmaker.controller;

import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;

import java.util.List;
import java.util.Objects;

public class WriterController {

    private final WriterRepository writerRepository;

    public WriterController(
            WriterRepository writerRepository
    ) {
        this.writerRepository = writerRepository;
    }

    public Writer createWriter(String firstName, String lastName) throws RepositoryException {
        Writer writer = new Writer();
        writer.setFirstName(firstName);
        writer.setLastName(lastName);
        writer.setStatus(Status.ACTIVE);
        return writerRepository.save(writer);
    }

    public Writer getWriterById(Long id) throws RepositoryException {
        return writerRepository.getById(id);
    }

    public List<Writer> getAllWriters() throws RepositoryException {
        return writerRepository.getAll();
    }

    public Writer updateWriter(Long id, String firstName, String lastName) throws RepositoryException {
        Writer writer = getWriterById(id);
        if (Objects.nonNull(writer)) {
            writer.setFirstName(firstName);
            writer.setLastName(lastName);
            return writerRepository.update(writer);
        }
        return null;
    }

    public boolean deleteWriter(Long id) throws RepositoryException {
        return writerRepository.deleteById(id);
    }
}
