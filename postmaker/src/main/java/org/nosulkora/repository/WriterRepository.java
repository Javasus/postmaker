package org.nosulkora.repository;

import org.nosulkora.model.Post;
import org.nosulkora.model.Writer;

import java.util.List;

public interface WriterRepository extends GenericRepository<Writer, Long> {

    /**
     * Добавляет нового writer в файл writers.json.
     *
     * @param writer Обьект пользователя Writer
     * @return boolean
     */
    Boolean createWriter(Writer writer);

    /**
     * Возвращает writer по имени и фамилии, если он уже существует в writers.json
     *
     * @param firstname Имя
     * @param lastName  Фамилия
     * @return writer
     */
    List<Writer> getWriterByName(String firstname, String lastName);

    /**
     * Возвращает список всех пользователей из файла writers.json.
     *
     * @return список пользователей
     */
    List<Writer> getAllWriters();

    /**
     * Возвращает writer по id из файла - writers.json.
     *
     * @param id Идентификатор writer'а
     * @return writer
     */
    Writer getWriterById(Long id);

    /**
     * Возвращает обновленного пользователья в файле - writers.json.
     *
     * @param id       id пользователя которого хотим обновить
     * @param name     имя которое хотим присовить пользователю
     * @param LastName фамилия которую хотим присвоить пользователю
     * @return writer
     */
    Writer updateWriter(Long id, String name, String LastName);

    /**
     * Возвращает удаленнного пользователя в файле writers.json.
     *
     * @param id id пользователя, которого хотим удалить
     * @return writer
     */
    Writer deleteWriterById(Long id);

    /**
     * Возвращает пользователя с добавленным постом.
     *
     * @param writerId id пользователя
     * @param post     пост
     * @return Writer
     */
    Writer updateWriterWithNewPost(Long writerId, Post post);

    /**
     * Возвращает писателя с обновленным постом.
     *
     * @param post пост на который нужно обновить существующий у писателя пост
     * @return Writer
     */
    Writer updatePostInWriter(Post post);
}
