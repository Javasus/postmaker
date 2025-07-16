package org.nosulkora.repository;

import org.nosulkora.model.Writer;

import java.util.List;
import java.util.Optional;

public interface WriterRepository extends GenericRepository<Writer, Long> {

    Boolean createWriter(Writer writer);

    List<Writer> getWriterByName(String firstname, String lastName);

    List<Writer> getAllWriters();

    Writer getWriterById(Long id);

    Writer updateWriter(Long id, String name, String LastName);

    Writer deleteWriterById(Long id);
}
