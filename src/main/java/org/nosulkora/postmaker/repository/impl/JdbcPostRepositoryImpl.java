package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.utils.DatabaseManager;
import org.nosulkora.postmaker.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JdbcPostRepositoryImpl implements PostRepository {
    private static final Logger logger = LoggerFactory.getLogger(JdbcPostRepositoryImpl.class);

    private static final String SQL_GET_POST_BY_ID = """
            SELECT
                p.id as post_id, p.title, p.content, p.status as post_status, p.writer_id,
                w.first_name, w.last_name, w.status as writer_status,
                l.id as label_id, l.name as label_name, l.status as label_status
            FROM postmaker.posts p
            LEFT JOIN postmaker.writers w ON p.writer_id = w.id
            LEFT JOIN postmaker.post_labels pl ON p.id = pl.post_id
            LEFT JOIN postmaker.labels l ON pl.label_id = l.id AND l.status != 'DELETED'
            WHERE p.id = ? AND p.status != 'DELETED'
            ORDER BY p.id, l.id
            """;

    private static final String SQL_GET_ALL_POSTS = """
            SELECT
                p.id as post_id, p.title, p.content, p.status as post_status, p.writer_id,
                w.first_name, w.last_name, w.status as writer_status,
                l.id as label_id, l.name as label_name, l.status as label_status
            FROM postmaker.posts p
            LEFT JOIN postmaker.writers w ON p.writer_id = w.id
            LEFT JOIN postmaker.post_labels pl ON p.id = pl.post_id
            LEFT JOIN postmaker.labels l ON pl.label_id = l.id AND l.status != 'DELETED'
            WHERE p.status != 'DELETED'
            ORDER BY p.id, l.id
            """;

    private static final String SQL_GET_POSTS_BY_WRITER_ID = """
            SELECT
                p.id as post_id, p.title, p.content, p.status as post_status, p.writer_id,
                w.first_name, w.last_name, w.status as writer_status,
                l.id as label_id, l.name as label_name, l.status as label_status
            FROM postmaker.posts p
            LEFT JOIN postmaker.writers w ON p.writer_id = w.id
            LEFT JOIN postmaker.post_labels pl ON p.id = pl.post_id
            LEFT JOIN postmaker.labels l ON pl.label_id = l.id AND l.status != 'DELETED'
            WHERE p.writer_id = ? AND p.status != 'DELETED'
            ORDER BY p.id, l.id
            """;

    private static final String SQL_CREATE_POST =
            "INSERT INTO postmaker.posts (title, content, writer_id, status) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE_POST =
            "UPDATE postmaker.posts SET title = ?, content = ?, writer_id = ?, status = ? WHERE id = ?";

    private static final String SQL_DELETE_POST =
            "UPDATE postmaker.posts SET status = 'DELETED' WHERE id = ?";

    private static final String SQL_SAVE_LABEL_POST =
            "INSERT INTO postmaker.post_labels (post_id, label_id) VALUES (?, ?)";

    private static final String SQL_DELETE_POST_LABELS =
            "DELETE FROM postmaker.post_labels WHERE post_id = ?";

    @Override
    public Post save(Post post) {
        Long postId = DatabaseManager.executeInsert(
                SQL_CREATE_POST,
                ps -> setPostParameters(ps, post)
        );
        if (postId == null) {
            logger.error("Не удалось сохранить пост в БД: {} {}", post.getTitle(), post.getContent());
            return null;
        }
        post.setId(postId);

        if (post.getLabels() != null && !post.getLabels().isEmpty()) {
            if (savePostLabels(post.getId(), post.getLabels())) {
                return post;
            }
        }
        logger.error("Не удалось сохранить лейблы и пост в результирующую таблицу{} {}", post.getId(), post.getLabels());
        return null;
    }

    @Override
    public Post update(Post post) {
        int affectedRows = DatabaseManager.executeUpdate(
                SQL_UPDATE_POST,
                ps -> {
                    try {
                        setPostParameters(ps, post);
                        ps.setLong(5, post.getId());
                    } catch (SQLException e) {
                        logger.error("Ошибка установки параметров для обновления поста", e);
                    }
                }
        );
        if (affectedRows == 0) {
            logger.error("Пост с ID " + post.getId() + " не найден для обновления.");
            return null;
        }
        if (post.getLabels() != null && !post.getLabels().isEmpty()) {
            if (updatePostLabels(post)){
                return post;
            }
        }
        logger.error("Не удалось сохранить лейблы и пост в результирующую таблицу{} {}", post.getId(), post.getLabels());
        return null;
    }

    @Override
    public Post getById(Long id) {
        Post post = DatabaseManager.executeQuerySingle(
                SQL_GET_POST_BY_ID,
                this::mapSingleResultSetToPostWithLabels,
                id
        );
        if (Objects.isNull(post)) {
            logger.error("Не удалось вернуть пост по ID: {}", id);
            return null;
        }
        return post;
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = DatabaseManager.executeQueryList(SQL_GET_ALL_POSTS, this::mapResultSetToPostList);
        if (Objects.isNull(posts)) {
            logger.error("Ошибка при возврате всех постов.");
            return null;
        }
        return posts;
    }

    @Override
    public boolean deleteById(Long id) {
        // Soft delete
        int affectedRows = DatabaseManager.executeUpdate(SQL_DELETE_POST, ps -> {
            try {
                ps.setLong(1, id);
            } catch (SQLException e) {
                logger.error("Ошибка установки параметра для удаления.", e);
            }
        });

        if (affectedRows == 0) {
            logger.error("Пост с ID " + id + " не найден для удаления.");
            return false;
        }
        return true;
    }

    //------------------------------------private methods-------------------------------------------------------

    /**
     * Сохраняет связи постов и лейблов в таблицу post_labels.
     */
    private boolean savePostLabels(Long postId, List<Label> labels) {
        return DatabaseManager.executeBatch(
                SQL_SAVE_LABEL_POST,
                ps -> labels.forEach(label -> {
                    try {
                        ps.setLong(1, postId);
                        ps.setLong(2, label.getId());
                        ps.addBatch();
                    } catch (SQLException e) {
                        logger.error("Ошибка добавления batch для лейбла.", e);
                    }
                })
        );
    }

    /**
     * Обновляет лейблы поста (удаляет старые, сохраняет новые)
     */
    private boolean updatePostLabels(Post post) {
        // Удоляем старые лейблы
        DatabaseManager.executeUpdate(
                SQL_DELETE_POST_LABELS,
                ps -> {
                    try {
                        ps.setLong(1, post.getId());
                    } catch (SQLException e) {
                        logger.error("Ошибка при установке параметра для обновления лейбла" + e);
                    }
                }
        );
        // добавляем новые лейблы
        return savePostLabels(post.getId(), post.getLabels());
    }

    /**
     * Заполняет PreparedStatement данными из поста.
     */
    private void setPostParameters(PreparedStatement ps, Post post) {
        try {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setLong(3, post.getWriterId());
            ps.setString(4, post.getStatus().name());
        } catch (SQLException e) {
            logger.error("Ошибка установки параметров поста.", e);
        }
    }

    /**
     * Маппит ResultSet в один объект Post с лейблами (из JOIN запроса)
     */
    private Post mapSingleResultSetToPostWithLabels(ResultSet rs) {
        List<Post> posts = mapResultSetToPostList(rs);
        return posts == null || posts.isEmpty() ? null : posts.get(0);
    }

    /**
     * Маппит ВЕСЬ ResultSet в список постов (для методов, возвращающих List<Post>)
     */
    private List<Post> mapResultSetToPostList(ResultSet rs) {
        try {
            Map<Long, Post> postsMap = new HashMap<>();
            while (rs.next()) {
                Long postId = rs.getLong("post_id");

                Post post = postsMap.get(postId);
                if (post == null) {
                    try {
                        post = createPostFromResultSet(rs);
                    } catch (SQLException e) {
                        logger.error("Ошибка при получении поста из ResultSet: {}", rs, e);
                    }
                    postsMap.put(postId, post);
                }
                try {
                    addLabelToPostIfPresent(rs, post);
                } catch (SQLException e) {
                    logger.error("Ошибка при получении лейбла из ResultSet и добавлении в пост: {}", rs, e);
                }
            }
            return new ArrayList<>(postsMap.values());
        } catch (SQLException e) {
            logger.error("Ошибка маппинга ResultSet в список постов с лейблами", e);
            return null;
        }
    }

    /**
     * Создает базовый объект Post из ResultSet
     */
    private Post createPostFromResultSet(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getLong("post_id"));
        post.setTitle(resultSet.getString("title"));
        post.setContent(resultSet.getString("content"));
        post.setWriterId(resultSet.getLong("writer_id"));
        post.setStatus(Status.valueOf(resultSet.getString("post_status")));
        post.setLabels(new ArrayList<>());
        return post;
    }

    /**
     * Добавляет лейбл в пост.
     */
    private void addLabelToPostIfPresent(ResultSet resultSet, Post post) throws SQLException {
        Long labelId = resultSet.getLong("label_id");
        if (!resultSet.wasNull() && labelId > 0) {
            Label label = new Label();
            label.setId(labelId);
            label.setName(resultSet.getString("label_name"));
            label.setStatus(Status.valueOf(resultSet.getString("label_status")));
            post.getLabels().add(label);
        }
    }
}
