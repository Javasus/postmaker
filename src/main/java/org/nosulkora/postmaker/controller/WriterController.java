package org.nosulkora.postmaker.controller;

import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;

import java.util.List;

public class WriterController {

    private final WriterRepository writerRepository;

    public WriterController(
            WriterRepository writerRepository
    ) {
        this.writerRepository = writerRepository;
    }

    public Writer createWriter(String firstName, String lastName) {
        Writer writer = new Writer();
        writer.setFirstName(firstName);
        writer.setLastName(lastName);
        writer.setStatus(Status.ACTIVE);
        return writerRepository.save(writer);
    }

    public Writer getWriterById(Long id) {
        Writer writer = writerRepository.getById(id);
        if (writer == null) {
           throw new IllegalArgumentException("Не найден писатель с id = " + id);
        }
        return writer;
    }

    public List<Writer> getAllWriters() {
        return writerRepository.getAll();
    }

    public Writer updateWriter(Long id, String firstName, String lastName) {
        Writer writer = getWriterById(id);
        writer.setFirstName(firstName);
        writer.setLastName(lastName);
        return writerRepository.update(writer);
    }

    public void deleteWriter(Long id) {
        writerRepository.deleteById(id);
    }
}
