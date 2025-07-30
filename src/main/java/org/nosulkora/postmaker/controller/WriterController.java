package org.nosulkora.postmaker.controller;

import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;
import org.nosulkora.postmaker.repository.impl.GsonWriterRepositoryImpl;

import java.util.List;

public class WriterController {

    private final WriterRepository writerRepository;

    public WriterController() {
        writerRepository = new GsonWriterRepositoryImpl();
    }

    public WriterController(WriterRepository writerRepository) {
        this.writerRepository = writerRepository;
    }

    public Writer createWriter(String firstName, String lastName, List<Post> posts) {
        Writer writer = new Writer();
        writer.setFirstName(firstName);
        writer.setLastname(lastName);
        writer.setPosts(posts);
        writer.setStatus(Status.ACTIVE);
        return writerRepository.save(writer);
    }

    public Writer getWriterById(Long id) {
        return writerRepository.getById(id);
    }

    public List<Writer> getAllWriter() {
        return writerRepository.getAll();
    }

    public Writer updateWriter(Long id, String firstName, String lastName, List<Post> posts) {
        Writer writer = writerRepository.getById(id);
        writer.setFirstName(firstName);
        writer.setLastname(lastName);
        writer.setPosts(posts);
        return writerRepository.update(writer);
    }

    public void deleteWriter(Long id) {
        writerRepository.deleteById(id);
    }
}
