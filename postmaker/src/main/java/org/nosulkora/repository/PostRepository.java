package org.nosulkora.repository;

import org.nosulkora.model.Label;
import org.nosulkora.model.Post;

import java.util.List;

public interface PostRepository extends GenericRepository<Post, Long> {

    /**
     * Добавляет новый post в файл posts.json.
     *
     * @param post Обьект поста Post
     * @return boolean
     */
    Boolean createPost(Post post);

    /**
     * Возвращает список всех постов из файла posts.json.
     *
     * @return Список постов
     */
    List<Post> getAllPosts();

    /**
     * Возвращает пост по идентификатору.
     *
     * @param id идентификатор
     * @return Post
     */
    Post getPostById(Long id);

    /**
     * Обновляет title и content в посте.
     *
     * @param post обновлённый пост.
     * @return Post
     */
    Post updatePost(Post post);

    /**
     * Возвращает пост с обновленным лейблом.
     *
     * @param label лейбл на который нужно обновить уже существующий лейбл поста
     * @return Post
     */
    Post updatePostWithNewLabel(Long postId, Label label);

    /**
     * Возвращает пост с обновленным лейблом.
     *
     * @param label пост на который нужно обновить существующий у писателя пост
     * @return Writer
     */
    Post updateLabelInPost(Label label);
}
