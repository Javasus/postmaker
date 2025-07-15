package org.nosulkora.repository;

import org.nosulkora.model.Writer;

import java.util.List;
import java.util.Optional;

public interface WriterRepository extends GenericRepository<Writer, Long> {

    Boolean createWriter(Writer writer);

   Writer getWriterByName(String firstname, String lastName);

    List<Writer> getAllWriters();

    Writer getWriterById(Long id);
}
