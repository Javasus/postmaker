package org.nosulkora.repository;

import org.nosulkora.model.Post;

import java.util.List;

public interface PostRepository extends GenericRepository<Post, Long>{

    /**
     * Добавляет новый post в файл posts.json.
     *
     * @param post     Обьект поста Post
     * @param writerId id пользователя которому пренадлежит пост
     * @return boolean
     */
    Boolean createPost(Long writerId, Post post);

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
     * Меняет статус пост на - DELETE по идентификатору.
     *
     * @param id идентификатор
     * @return Post
     */
    Post deletePostById(Long id);
}
