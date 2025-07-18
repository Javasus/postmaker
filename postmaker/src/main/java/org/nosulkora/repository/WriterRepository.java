package org.nosulkora.repository;

import org.nosulkora.model.Post;
import org.nosulkora.model.Writer;

import java.util.List;

public interface WriterRepository extends GenericRepository<Writer, Long> {

    Boolean createWriter(Writer writer);

    List<Writer> getWriterByName(String firstname, String lastName);

    List<Writer> getAllWriters();

    Writer getWriterById(Long id);

    Writer updateWriter(Long id, String name, String LastName);

    Writer deleteWriterById(Long id);

    Writer updateWriterWithNewPost(Long writerId, Post post);

    Writer updatePostInWriter(Post post);

//    /**
//     * Ищет пост по идентификатору у писателя и меняет егго статус на DELETE.
//     *
//     * @param post пост оторого нужно поменять на DELETE
//     * @return Writer
//     */
//    Writer deletePostInWriter(Post post);
}
