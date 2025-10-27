package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.ConnectionManager;
import org.nosulkora.postmaker.repository.PostRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Post save(Post post) {
        try (Connection conn = ConnectionManager.transactionalConnection()) {
            try {
                Post savedPost = insertPost(conn, post);
                savePostLabels(conn, post);

                ConnectionManager.commit(conn);
                return savedPost;

            } catch (SQLException e) {
                ConnectionManager.rollback(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при сохранении поста: " + post, e);
        }
    }

    @Override
    public Post update(Post post) {
        try (Connection conn = ConnectionManager.transactionalConnection()) {
            try {
                updatePost(conn, post);
                updatePostLabels(conn, post);

                ConnectionManager.commit(conn);
                return post;

            } catch (SQLException e) {
                ConnectionManager.rollback(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при обновлении поста: " + post, e);
        }
    }

    @Override
    public Post getById(Long id) {
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_POST_BY_ID)) {
            ps.setLong(1, id);
            return executeQueryAndExtractPost(ps);

        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при поиске поста с id = " + id, e);
        }
    }

    @Override
    public List<Post> getAll() {
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_POSTS);
             ResultSet resultSet = ps.executeQuery()) {

            return extractPostsFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении всех постов.", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        // Soft delete
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE_POST)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при удалении поста с id =  " + id, e);
        }
    }

    @Override
    public List<Post> getPostsByWriterId(Long writerId) {
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_POSTS_BY_WRITER_ID)) {

            ps.setLong(1, writerId);
            try (ResultSet rs = ps.executeQuery()) {
                return extractPostsFromResultSet(rs);
            }

        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении постов автора с writerId =  " + writerId, e);
        }
    }

    /**
     * Сохраняет пост в таблицу posts.
     */
    private Post insertPost(Connection conn, Post post) throws SQLException {
        try (PreparedStatement ps = ConnectionManager.preparedStatementWithKeys(conn, SQL_CREATE_POST)) {

            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setLong(3, post.getWriterId());
            ps.setString(4, post.getStatus().name());

            int executeResult = ps.executeUpdate();
            if (executeResult == 0) {
                throw new SQLException("Ошибка создания поста, executeResult - " + executeResult);
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getLong(1));
                    return post;
                } else {
                    throw new SQLException("Ошибка создания поста, generatedKeys - " + generatedKeys);
                }
            }
        }
    }

    /**
     * Сохраняет связи постов и лейблов в таблицу post_labels.
     */
    private void savePostLabels(Connection conn, Post post) throws SQLException {
        if (post.getLabels() == null || post.getLabels().isEmpty()) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE_LABEL_POST)) {
            for (Label label : post.getLabels()) {
                ps.setLong(1, post.getId());
                ps.setLong(2, label.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Обновляет пост.
     */
    private void updatePost(Connection conn, Post post) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_POST)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setLong(3, post.getWriterId());
            ps.setString(4, post.getStatus().name());
            ps.setLong(5, post.getId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("Ошибка обновления поста с id = " + post.getId());
            }
        }
    }

    private void updatePostLabels(Connection conn, Post post) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_POST_LABELS)) {
            ps.setLong(1, post.getId());
            ps.executeUpdate();
        }

        savePostLabels(conn, post);
    }

    private Post executeQueryAndExtractPost(PreparedStatement ps) throws SQLException {
        try (ResultSet resultSet = ps.executeQuery()) {
            List<Post> posts = extractPostsFromResultSet(resultSet);
            return posts.isEmpty() ? null : posts.get(0);
        }
    }

    private List<Post> extractPostsFromResultSet(ResultSet resultSet) throws SQLException {
        Map<Long, Post> postsMap = new HashMap<>();

        while (resultSet.next()) {
            Long postId = resultSet.getLong("post_id");

            Post post = postsMap.computeIfAbsent(postId, id -> {
                try {
                    return createPostFromResultSet(resultSet);
                } catch (SQLException e) {
                    throw new RepositoryException("Ошибка при получении поста из result_set - " + resultSet, e);
                }
            });
            addLabelToPostIfPresent(resultSet, post);
        }
        return new ArrayList<>(postsMap.values());
    }

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
}
