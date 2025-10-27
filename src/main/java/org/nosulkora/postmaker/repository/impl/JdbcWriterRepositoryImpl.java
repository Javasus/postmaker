package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.ConnectionManager;
import org.nosulkora.postmaker.repository.WriterRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcWriterRepositoryImpl implements WriterRepository {

    private static final String SQL_CREATE_WRITER =
            "INSERT INTO postmaker.writers (first_name, last_name, status) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE_WRITER =
            "UPDATE postmaker.writers SET first_name = ?, last_name = ?, status = ? WHERE id = ?";

    private static final String SQL_GET_WRITER_BY_ID = """
            SELECT
                w.id as writer_id, w.first_name, w.last_name, w.status as writer_status,
                p.id as post_id, p.title, p.content, p.status as post_status, p.writer_id as post_writer_id,
                l.id as label_id, l.name as label_name, l.status as label_status
            FROM postmaker.writers w
            LEFT JOIN postmaker.posts p ON w.id = p.writer_id AND p.status != 'DELETED'
            LEFT JOIN postmaker.post_labels pl ON p.id = pl.post_id
            LEFT JOIN postmaker.labels l ON pl.label_id = l.id AND l.status != 'DELETED'
            WHERE w.id = ? AND w.status != 'DELETED'
            ORDER BY w.id, p.id, l.id
            """;
    private static final String SQL_GET_ALL_WRITERS = """
            SELECT
                w.id as writer_id, w.first_name, w.last_name, w.status as writer_status,
                p.id as post_id, p.title, p.content, p.status as post_status, p.writer_id as post_writer_id,
                l.id as label_id, l.name as label_name, l.status as label_status
            FROM postmaker.writers w
            LEFT JOIN postmaker.posts p ON w.id = p.writer_id AND p.status != 'DELETED'
            LEFT JOIN postmaker.post_labels pl ON p.id = pl.post_id
            LEFT JOIN postmaker.labels l ON pl.label_id = l.id AND l.status != 'DELETED'
            WHERE w.status != 'DELETED'
            ORDER BY w.id, p.id, l.id
            """;
    private static final String SQL_DELETE_WRITER =
            "UPDATE postmaker.writers SET status = 'DELETED' WHERE id = ?";

    @Override
    public Writer save(Writer writer) {
        try (Connection conn = ConnectionManager.autoCommitConnection()) {
            return insertWriter(conn, writer);
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при сохранении писателя: " + writer, e);
        }
    }

    @Override
    public Writer update(Writer writer) {
        try (Connection conn = ConnectionManager.autoCommitConnection()) {
            updateWriter(conn, writer);
            return writer;
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при обновлении писателя: " + writer, e);
        }
    }

    @Override
    public Writer getById(Long id) {
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_WRITER_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Writer> writers = extractWritersFromResultSet(rs);
                return writers.isEmpty() ? null : writers.get(0);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при поиске писателя с id: " + id, e);
        }
    }

    @Override
    public List<Writer> getAll() {
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_WRITERS);
             ResultSet rs = ps.executeQuery()) {
            return extractWritersFromResultSet(rs);
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении всех писателей.", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE_WRITER)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при удалении писателя с id: " + id, e);
        }
    }

    /**
     * Извлекает писателей с постами и лейблами из resultSet.
     */
    private List<Writer> extractWritersFromResultSet(ResultSet rs) throws SQLException {
        Map<Long, Writer> writersMap = new HashMap<>();
        Map<Long, Post> postsMap = new HashMap<>();

        while (rs.next()) {
            Long writerId = rs.getLong("writer_id");

            // Получаем писателя
            Writer writer = writersMap.computeIfAbsent(writerId, id -> {
                try {
                    return createBasicWriterFromResultSet(rs);
                } catch (SQLException e) {
                    throw new RepositoryException("Ошибка при получении писателя из resultSet: " + rs, e);
                }
            });

            Long postId = rs.getLong("post_id");
            if (!rs.wasNull() && postId > 0) {
                // Получаем пост
                Post post = postsMap.computeIfAbsent(postId, id -> {
                   try {
                       return createPostFromResultSet(rs);
                   } catch (SQLException e) {
                       throw new RepositoryException("Ошибка при получении поста из resultSet: " + rs, e);
                   }
                });

                // Добавляем пост к писателю
                if (writer.getPosts() == null) {
                    writer.setPosts(new ArrayList<>());
                }
                if (writer.getPosts().stream().noneMatch(p -> p.getId().equals(postId))) {
                    writer.getPosts().add(post);
                }

                Long labelId = rs.getLong("label_id");
                if (!rs.wasNull() && labelId > 0) {
                    Label label = createLabelFromResultSet(rs);

                    if (post.getLabels() == null) {
                        post.setLabels(new ArrayList<>());
                    }
                    if (post.getLabels().stream().noneMatch(l -> l.getId().equals(labelId))) {
                        post.getLabels().add(label);
                    }
                }
            }
        }
        return new ArrayList<>(writersMap.values());
    }

    /**
     * Возвращает пистаеля из resultSet.
     */
    private Writer createBasicWriterFromResultSet(ResultSet rs) throws SQLException {
        Writer writer = new Writer();
        writer.setId(rs.getLong("writer_id"));
        writer.setFirstName(rs.getString("first_name"));
        writer.setLastName(rs.getString("last_name"));
        writer.setStatus(Status.valueOf(rs.getString("writer_status")));
        return writer;
    }

    /**
     * Возвращает пост из resultSet.
     */
    private Post createPostFromResultSet(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("post_id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setStatus(Status.valueOf(rs.getString("post_status")));
        post.setWriterId(rs.getLong("post_writer_id"));
        return post;
    }

    /**
     * Возвращает лейбл из resultSet.
     */
    private Label createLabelFromResultSet(ResultSet rs) throws SQLException {
        Label label = new Label();
        label.setId(rs.getLong("label_id"));
        label.setName(rs.getString("label_name"));
        label.setStatus(Status.valueOf(rs.getString("label_status")));
        return label;
    }

    /**
     * Добавляет писателя.
     */
    private Writer insertWriter(Connection conn, Writer writer) throws SQLException {
        try (PreparedStatement ps = ConnectionManager.preparedStatementWithKeys(conn, SQL_CREATE_WRITER)) {
            ps.setString(1, writer.getFirstName());
            ps.setString(2, writer.getLastName());
            ps.setString(3, writer.getStatus().name());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("Не удалось создать писателя, ни одна из записей не была добавлена.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    writer.setId(rs.getLong(1));
                    return writer;
                } else {
                    throw new SQLException("Не удалось создать писателя, id не получен.");
                }
            }
        }
    }

    /**
     * Обновляет писателя.
     */
    private void updateWriter(Connection conn, Writer writer) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_WRITER)) {
            ps.setString(1, writer.getFirstName());
            ps.setString(2, writer.getLastName());
            ps.setString(3, writer.getStatus().name());
            ps.setLong(4, writer.getId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("Не удалось обновить писателя, ни одна запись не была изменена.");
            }
        }
    }
}
