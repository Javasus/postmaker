package org.nosulkora.postmaker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("WriteControllerTest")
public class WriteControllerTest {

    private static final Long WRITER_ID = 1L;
    private static final String WRITER_FIRSTNAME = "Roman";
    private static final String WRITER_LASTNAME = "Abramovich";

    private WriterController writerController;
    private WriterRepository writerRepository;

    @BeforeEach
    void setUp() {
        writerRepository = mock(WriterRepository.class);
        writerController = new WriterController(writerRepository);
    }

    private Writer creatTestWriter(Long id, String firstName, String lastName, Status status, List<Post> posts) {
        Writer writer = new Writer();
        writer.setId(id);
        writer.setFirstName(firstName);
        writer.setLastName(lastName);
        writer.setStatus(status);
        writer.setPosts(posts);
        return writer;
    }

    private Writer creatTestWriter(Long id, String firstName, String lastName, Status status) {
        Writer writer = new Writer();
        writer.setId(id);
        writer.setFirstName(firstName);
        writer.setLastName(lastName);
        writer.setStatus(status);
        return writer;
    }

    private Writer createDefaultTestWriter() {
        return creatTestWriter(WRITER_ID, WRITER_FIRSTNAME, WRITER_LASTNAME, Status.ACTIVE, createTestPosts());
    }

    private List<Post> createTestPosts() {
        Post post1 = new Post();
        post1.setId(46L);
        post1.setTitle("title1");
        post1.setContent("content1");
        post1.setWriterId(WRITER_ID);
        Post post2 = new Post();
        post2.setId(84L);
        post2.setTitle("title2");
        post2.setContent("content2");
        post2.setWriterId(WRITER_ID);
        return Arrays.asList(post1, post2);
    }

    @Test
    @DisplayName("Create writer")
    void createWriterTest() {
        Writer expectedWriter = createDefaultTestWriter();

        when(writerRepository.save(any(Writer.class))).thenReturn(expectedWriter);

        Writer actualWriter = writerController.createWriter(WRITER_FIRSTNAME, WRITER_LASTNAME);

        assertAll(
                () -> assertEquals(expectedWriter.getId(), actualWriter.getId(), "writerId"),
                () -> assertEquals(expectedWriter.getFirstName(), actualWriter.getFirstName(), "witerFirstName"),
                () -> assertEquals(expectedWriter.getLastName(), actualWriter.getLastName(), "witerLastName"),
                () -> assertEquals(expectedWriter.getStatus(), actualWriter.getStatus(), "writerStatus"),
                () -> assertEquals(expectedWriter.getPosts(), actualWriter.getPosts(), "writerPosts")
        );

        verify(writerRepository, times(1)).save(any(Writer.class));
    }

    @Test
    @DisplayName("Get writer by Id")
    void getWriterByIdTest() {
        Writer expectedWriter = createDefaultTestWriter();

        when(writerRepository.getById(WRITER_ID)).thenReturn(expectedWriter);
        Writer actualWriter = writerController.getWriterById(WRITER_ID);

        assertAll(
                () -> assertEquals(expectedWriter.getId(), actualWriter.getId(), "writerId"),
                () -> assertEquals(expectedWriter.getFirstName(), actualWriter.getFirstName(), "witerFirstName"),
                () -> assertEquals(expectedWriter.getLastName(), actualWriter.getLastName(), "witerLastName"),
                () -> assertEquals(expectedWriter.getStatus(), actualWriter.getStatus(), "writerStatus"),
                () -> assertEquals(expectedWriter.getPosts(), actualWriter.getPosts(), "writerPosts")
        );

        verify(writerRepository, times(1)).getById(WRITER_ID);
    }

    @Test
    @DisplayName("Get All Writers ")
    void getAllWritersTest() {
        Writer expectedWriter1 = createDefaultTestWriter();
        Writer expectedWriter2 = creatTestWriter(
                2L,
                "Ivan",
                "Ivanov",
                Status.ACTIVE,
                new ArrayList<>()
        );
        List<Writer> expectedWriters = List.of(expectedWriter1, expectedWriter2);

        when(writerRepository.getAll()).thenReturn(expectedWriters);

        List<Writer> actualWriters = writerController.getAllWriters();

        assertThat(expectedWriters)
                .describedAs("Check get all writers.")
                .containsExactlyInAnyOrderElementsOf(actualWriters);

        verify(writerRepository, times(1)).getAll();
    }

    @Test
    @DisplayName("Update writer.")
    void updateWriterTest() {
        Writer existingWriter = createDefaultTestWriter();
        Writer updatedWriter = creatTestWriter(WRITER_ID, "Ivan", "Ivanov", Status.ACTIVE);

        when(writerRepository.getById(WRITER_ID)).thenReturn(existingWriter);
        when(writerRepository.update(any(Writer.class))).thenReturn(updatedWriter);

        Writer actualWriter = writerController.updateWriter(WRITER_ID, "Ivan", "Ivanov");

        assertAll(
                () -> assertEquals(updatedWriter.getId(), actualWriter.getId(), "writerId"),
                () -> assertEquals(updatedWriter.getFirstName(), actualWriter.getFirstName(), "witerFirstName"),
                () -> assertEquals(updatedWriter.getLastName(), actualWriter.getLastName(), "witerLastName"),
                () -> assertEquals(updatedWriter.getStatus(), actualWriter.getStatus(), "writerStatus"),
                () -> assertEquals(updatedWriter.getPosts(), actualWriter.getPosts(), "writerPosts")
        );

        verify(writerRepository, times(1)).getById(WRITER_ID);
        verify(writerRepository, times(1)).update(any(Writer.class));
    }

    @Test
    @DisplayName("Delete writer.")
    void deleteWriterTest() {
        writerController.deleteWriter(WRITER_ID);
        verify(writerRepository, times(1)).deleteById(WRITER_ID);
    }

    @Test
    @DisplayName("When update writer and writer is not found return null.")
    void getUpdateWriterNotFoundTest() {
        when(writerRepository.getById(WRITER_ID)).thenReturn(null);

        Writer actualWriter = writerController.updateWriter(WRITER_ID, "Ivan", "Ivanov");

        assertNull(actualWriter);
        verify(writerRepository, times(1)).getById(WRITER_ID);
        verify(writerRepository, never()).update(any(Writer.class));
    }
}
