package org.nosulkora.postmaker.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nosulkora.postmaker.database.LiquibaseManager;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;
import org.nosulkora.postmaker.repository.impl.JdbcWriterRepositoryImpl;

import static org.junit.jupiter.api.Assertions.*;

class WriterRepositoryTest {

    private WriterRepository writerRepository;

    @BeforeEach
    void setUp() {
        // Запускаем миграции только с тестовыми данными
        LiquibaseManager.runTestMigrations();
        writerRepository = new JdbcWriterRepositoryImpl();
    }

    @Test
    void shouldFindTestWriter() {
        Writer writer = writerRepository.getById(1L);
        assertNotNull(writer);
        assertEquals("Тестовый", writer.getFirstName());
    }
}