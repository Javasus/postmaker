package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.ConnectionManager;
import org.nosulkora.postmaker.repository.PostRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JdbcPostRepositoryImpl implements PostRepository {

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
    public Post save(Post post) throws RepositoryException {
        try {
            Long postId = ConnectionManager.executeInsert(
                    SQL_CREATE_POST,
                    ps -> setPostParameters(ps, post)
            );

            post.setId(postId);

            if (post.getLabels() != null && !post.getLabels().isEmpty()) {
                savePostLabels(post.getId(), post.getLabels());
            }
            return post;
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при сохранении поста: " + post, e);
        }
    }

    @Override
    public Post update(Post post) throws RepositoryException {
        try {
            int affectedRows = ConnectionManager.executeUpdate(
                    SQL_UPDATE_POST,
                    ps -> {
                        try {
                            setPostParameters(ps, post);
                            ps.setLong(5, post.getId());
                        } catch (SQLException e) {
                            throw new RepositoryException("Ошибка установки параметров для обновления поста", e);
                        }
                    }
            );
            if (affectedRows == 0) {
                throw new RepositoryException("Пост с ID " + post.getId() + " не найден для обновления.");
            }
            updatePostLabels(post);
            return post;
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при обновлении поста: " + post, e);
        }
    }

    @Override
    public Post getById(Long id) throws RepositoryException {
        try {
            return ConnectionManager.executeQuerySingle(
                    SQL_GET_POST_BY_ID,
                    this::mapSingleResultSetToPostWithLabels,
                    id
            );
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при поиске поста с id = " + id, e);
        }
    }

    @Override
    public List<Post> getAll() throws RepositoryException {
        return ConnectionManager.executeQueryList(SQL_GET_ALL_POSTS, this::mapResultSetToPostList);
    }

    @Override
    public void deleteById(Long id) throws RepositoryException {
        // Soft delete
        try {
            int affectedRows = ConnectionManager.executeUpdate(SQL_DELETE_POST, ps -> {
                try {
                    ps.setLong(1, id);
                } catch (SQLException e) {
                    throw new RepositoryException("Ошибка установки параметра для удаления." + e);
                }
            });

            if (affectedRows == 0) {
                throw new RepositoryException("Пост с ID " + id + " не найден для удаления.");
            }
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при удалении поста с id =  " + id, e);
        }
    }

//    @Override
//    public List<Post> getPostsByWriterId(Long writerId) throws RepositoryException {
//        try {
//            return ConnectionManager.executeQueryList(
//                    SQL_GET_POSTS_BY_WRITER_ID,
//                    this::mapSingleResultSetToPostWithLabels,
//                    writerId
//            );
//        } catch (RepositoryException e) {
//            throw new RepositoryException("Ошибка при получении постов автора с writerId =  " + writerId, e);
//        }
//    }

    /**
     * Сохраняет связи постов и лейблов в таблицу post_labels.
     */
    private void savePostLabels(Long postId, List<Label> labels) throws RepositoryException {
        try {
            ConnectionManager.executeBatch(
                    SQL_SAVE_LABEL_POST,
                    ps -> labels.forEach(label -> {
                        try {
                            ps.setLong(1, postId);
                            ps.setLong(2, label.getId());
                            ps.addBatch();
                        } catch (SQLException e) {
                            throw new RepositoryException("Ошибка добавления batch для лейбла.", e);
                        }
                    })
            );
        } catch (RepositoryException e) {
            throw new RepositoryException("ошибка при сохранении лейблов для поста с ID: " + postId, e);
        }
    }

    /**
     * Обновляет лейблы поста (удаляет старые, сохраняет новые)
     */
    private void updatePostLabels(Post post) throws RepositoryException {
        try {
            // Удоляем старые лейблы
            ConnectionManager.executeUpdate(
                    SQL_DELETE_POST_LABELS,
                    ps -> {
                        try {
                            ps.setLong(1, post.getId());
                        } catch (SQLException e) {
                            throw new RepositoryException("Ошибка при установке параметра для обновления лейбла" + e);
                        }
                    }
            );
            // добавляем новые лейблы
            Optional.ofNullable(post.getLabels())
                    .filter(labels -> !labels.isEmpty())
                    .ifPresent(labels -> savePostLabels(post.getId(), labels));
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при обновлении лейблов для постов с ID: " + post.getId(), e);
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

    private void setPostParameters(PreparedStatement ps, Post post) {
        try {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setLong(3, post.getWriterId());
            ps.setString(4, post.getStatus().name());
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка установки параметров поста." + e);
        }
    }

    /**
     * Маппит ResultSet в объект Post с лейблами (из JOIN запроса)
     */
    private Post mapSingleResultSetToPostWithLabels(ResultSet rs) {
        try {
            if (!rs.next()) {
                return null;
            }
            Post post = createPostFromResultSet(rs);
            addLabelToPostIfPresent(rs, post);

            // Обрабатываем все строки для этого поста (если есть несколько лейблов)
            Long firstPostId = post.getId();
            while (rs.next()) {
                Long currentPostId = rs.getLong("post_id");

                if (!firstPostId.equals(currentPostId)) {
                    break;
                }
                addLabelToPostIfPresent(rs, post);
            }
            return post;
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка маппинга ResultSet в Post & Label.", e);
        }
    }

//    /**
//     * Маппит ResultSet в объект Post с лейблами (из JOIN запроса)
//     */
//    private Post mapSingleResultSetToPostWithLabels(ResultSet rs, Post cur) {
//        try {
//            if (rs.wasNull()) {
//                return null;
//            }
//            Long postId = rs.getLong("post_id");
//            if (cur != null && postId.equals(cur.getId())) {
//                addLabelToPostIfPresent(rs, cur);
//                return cur;
//            }
//            Post post = createPostFromResultSet(rs);
//            addLabelToPostIfPresent(rs, post);
//
//            return post;
//        } catch (SQLException e) {
//            throw new RepositoryException("Ошибка маппинга ResultSet в Post & Label.", e);
//        }
//    }

    /**
     * Маппит ВЕСЬ ResultSet в список постов (для методов, возвращающих List<Post>)
     */
    private List<Post> mapResultSetToPostList(ResultSet rs) {
        try {
            Map<Long, Post> postsMap = new LinkedHashMap<>();

            while (rs.next()) {
                Long postId = rs.getLong("post_id");

                Post post = postsMap.get(postId);
                if (post == null) {
                    post = createPostFromResultSet(rs);
                    postsMap.put(postId, post);
                }
                addLabelToPostIfPresent(rs, post);
            }
            return new ArrayList<>(postsMap.values());
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка маппинга ResultSet в список постов", e);
        }
    }
}
