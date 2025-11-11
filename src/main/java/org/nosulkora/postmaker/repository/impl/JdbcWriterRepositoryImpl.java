package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.utils.DatabaseManager;
import org.nosulkora.postmaker.repository.WriterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JdbcWriterRepositoryImpl implements WriterRepository {
    private static final Logger logger = LoggerFactory.getLogger(JdbcWriterRepositoryImpl.class);

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
        Long writerId = DatabaseManager.executeInsert(
                SQL_CREATE_WRITER,
                ps -> setWriterParameters(ps, writer)
        );
        if (writerId == null) {
            logger.error("Не удалось сохранить писателя в БД: {} {}", writer.getFirstName(), writer.getLastName());
            return null;
        }
        writer.setId(writerId);
        return writer;
    }

    @Override
    public Writer update(Writer writer) {
        int affectedRaws = DatabaseManager.executeUpdate(
                SQL_UPDATE_WRITER,
                ps -> {
                    try {
                        setWriterParameters(ps, writer);
                        ps.setLong(4, writer.getId());
                    } catch (SQLException e) {
                        logger.error("Ошибка установки параметров для обновления writer", e);
                    }
                }
        );
        if (affectedRaws == 0) {
            logger.error("Writer с ID " + writer.getId() + " не найден для обновления.");
            return null;
        }
        return writer;
    }

    @Override
    public Writer getById(Long id) {
        Writer writer = DatabaseManager.executeQuerySingle(
                SQL_GET_WRITER_BY_ID,
                this::mapSingleResultSetToWriterWithPostsAndLabels,
                id
        );
        if (Objects.isNull(writer)) {
            logger.error("Не удалось вернуть писателя по ID: {}", id);
            return null;
        }
        return writer;
    }

    @Override
    public List<Writer> getAll() {
        List<Writer> writers = DatabaseManager.executeQueryList(
                SQL_GET_ALL_WRITERS,
                this::extractWritersFromResultSet);
        if (Objects.isNull(writers)) {
            logger.error("Ошибка при возврате всех писателей.");
            return null;
        }
        return writers;
    }

    @Override
    public boolean deleteById(Long id) {
        int affectedRows = DatabaseManager.executeUpdate(
                SQL_DELETE_WRITER,
                ps -> {
                    try {
                        ps.setLong(1, id);
                    } catch (SQLException e) {
                        logger.error("Ошибка установки параметра для удаления.", e);
                    }
                });

        if (affectedRows == 0) {
            logger.error("Писатель с ID " + id + " не найден для удаления.");
            return false;
        }
        return true;
    }

    //------------------------------------private methods-------------------------------------------------------

    /**
     * Маппит ResultSet в один объект Writer с постами и лейблами (из JOIN запроса)
     */
    private Writer mapSingleResultSetToWriterWithPostsAndLabels(ResultSet rs) {
        List<Writer> writers = extractWritersFromResultSet(rs);
        return writers == null || writers.isEmpty() ? null : writers.get(0);
    }

    /**
     * Маппит ВЕСЬ ResultSet в список писателей с постами и лейблами
     */
    private List<Writer> extractWritersFromResultSet(ResultSet rs) {
        try {

            Map<Long, Writer> writersMap = new HashMap<>();
            Map<Long, Post> postsMap = new HashMap<>();

            while (rs.next()) {
                Long writerId = rs.getLong("writer_id");

                // Получаем писателя
                Writer writer = writersMap.computeIfAbsent(writerId, id -> {
                    try {
                        return createBasicWriterFromResultSet(rs);
                    } catch (SQLException e) {
                        logger.error("Ошибка при получении писателя из resultSet: {}", rs, e);
                        return null;
                    }
                });

                Long postId = rs.getLong("post_id");
                if (!rs.wasNull() && postId > 0) {
                    // Получаем пост
                    Post post = postsMap.computeIfAbsent(postId, id -> {
                        try {
                            return createPostFromResultSet(rs);
                        } catch (SQLException e) {
                            logger.error("Ошибка при получении поста из resultSet: {}", rs, e);
                            return null;
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
                        Label label = null;
                        try {
                            label = createLabelFromResultSet(rs);
                        } catch (SQLException e) {
                            logger.error("Ошибка при получении лейбла из resultSet: {}", rs, e);
                        }

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
        } catch (SQLException e) {
            logger.error("Ошибка маппинга ResultSet в список писателей.", e);
            return null;
        }
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
    private void setWriterParameters(PreparedStatement ps, Writer writer) {
        try {
            ps.setString(1, writer.getFirstName());
            ps.setString(2, writer.getLastName());
            ps.setString(3, writer.getStatus().name());
        } catch (SQLException e) {
            logger.error("Ошибка установки параметров писателя.", e);
        }
    }
}
