package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.PostRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JdbcPostRepositoryImpl implements PostRepository {
    @Override
    public Post save(Post post) {

        if (post.getId() == null) {
            post.setId(generateNextId());
        }

        String sql = "INSERT INTO postmaker.posts (id, title, content, writer_id, status) VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, post.getId());
            pstmt.setString(2, post.getTitle());
            pstmt.setString(3, post.getContent());

            if (Objects.nonNull(post.getWriterId())) {
                pstmt.setLong(4, post.getWriterId());
            } else {
                throw new RuntimeException("Post не содержит Writer или Writer не имеет ID - " + post);
            }
            pstmt.setString(5, post.getStatus().name());
            pstmt.executeUpdate();

            // Сохраняем связи с лейблами
            savePostLabels(post);

            return post;

        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении поста: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Post update(Post post) {
        String sql = "UPDATE postmaker.posts SET title = ?, content = ?, writer_id = ?, status = ? WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setLong(3, post.getWriterId());
            pstmt.setString(4, post.getStatus().name());
            pstmt.setLong(5, post.getId());
            pstmt.executeUpdate();

            // Обновляем связи с лейблами
            updatePostLabels(post);

            return post;

        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении поста: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Post getById(Long id) {
        String sql = """
            SELECT p.*, w.id as writer_id, w.first_name, w.last_name, w.status as writer_status
            FROM postmaker.posts p
            LEFT JOIN postmaker.writers w ON p.writer_id = w.id
            WHERE p.id = ? AND p.status != 'DELETED';
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Post post = mapResultSetToPost(rs);

                    // Загружаем лейблы для поста
                    List<Label> labels = getLabelsForPost(conn, id);
                    post.setLabels(labels);

                    return post;
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при поиске поста: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Post> getAll() {
            List<Post> posts = new ArrayList<>();
        String sql = """
            SELECT p.*, w.id as writer_id, w.first_name, w.last_name, w.status as writer_status
            FROM postmaker.posts p
            LEFT JOIN postmaker.writers w ON p.writer_id = w.id
            WHERE p.status != 'DELETED'
            ORDER BY p.id;
        """;

            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);

                    // Загружаем лейблы для каждого поста
                    List<Label> labels = getLabelsForPost(conn, post.getId());
                    post.setLabels(labels);

                    posts.add(post);
                }
            } catch (SQLException e) {
                System.out.println("Ошибка при получении всех постов: " + e.getMessage());
            }

            return posts;
    }

    @Override
    public void deleteById(Long id) {
        // Soft delete
        String sql = "UPDATE postmaker.posts SET status = 'DELETED' WHERE id = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Ошибка при удалении поста: " + e.getMessage());
        }
    }

    @Override
    public List<Post> getPostsByWriterId(Long writerId) {
        List<Post> posts = new ArrayList<>();
        String sql = """
            SELECT p.*, w.id as writer_id, w.first_name, w.last_name, w.status as writer_status
            FROM postmaker.posts p
            LEFT JOIN postmaker.writers w ON p.writer_id = w.id
            WHERE p.writer_id = ? AND p.status != 'DELETED'
            ORDER BY p.id;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, writerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);

                    // Загружаем лейблы для поста
                    List<Label> labels = getLabelsForPost(conn, post.getId());
                    post.setLabels(labels);

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении постов автора: " + e.getMessage());
        }

        return posts;
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setStatus(Status.valueOf(rs.getString("status")));

        // Создаем Writer
        Long writerId = rs.getLong("writer_id");
        if (writerId != 0 && !rs.wasNull()) {
            Writer writer = new Writer();
            writer.setId(writerId);
            writer.setFirstName(rs.getString("first_name"));
            writer.setLastName(rs.getString("last_name"));
            writer.setStatus(Status.valueOf(rs.getString("writer_status")));
            post.setWriterId(writer.getId());
        }

        return post;
    }

    private List<Label> getLabelsForPost(Connection conn, Long postId) {
        List<Label> labels = new ArrayList<>();
        String sql = """
                SELECT l.* FROM postmaker.labels l
                JOIN postmaker.post_labels pl ON l.id = pl.label_id
                WHERE pl.post_id = ? AND l.status != 'DELETED'
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Label label = new Label();
                    label.setId(rs.getLong("id"));
                    label.setName(rs.getString("name"));
                    label.setStatus(Status.valueOf(rs.getString("status")));
                    labels.add(label);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при загрузке меток для поста: " + e.getMessage());
        }

        return labels;
    }

    private void savePostLabels(Post post) {
        // Сначала удаляем старые связи
        deletePostLabels(post.getId());

        // Добавляем новые связи
        if (post.getLabels() != null && !post.getLabels().isEmpty()) {
            String sql = "INSERT INTO postmaker.post_labels (post_id, label_id) VALUES (?, ?);";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (Label label : post.getLabels()) {
                    pstmt.setLong(1, post.getId());
                    pstmt.setLong(2, label.getId());
                    pstmt.addBatch();
                }

                pstmt.executeBatch();

            } catch (SQLException e) {
                System.out.println("Ошибка при сохранении связей поста с лейблами: " + e.getMessage());
            }
        }
    }

    private void updatePostLabels(Post post) {
        savePostLabels(post);
    }

    private void deletePostLabels(Long postId) {
        String sql = "DELETE FROM postmaker.post_labels WHERE post_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, postId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Ошибка при удалении связей поста с лейблами: " + e.getMessage());
        }
    }

//    TODO вспомогательный метод. Убрать если не где не используется.
    public List<Post> getPostsByLabel(Long labelId) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.* FROM postmaker.posts p " +
                "JOIN postmaker.post_labels pl ON p.id = pl.post_id " +
                "WHERE pl.label_id = ? AND p.status != 'DELETED' " +
                "ORDER BY p.id";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, labelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);

                    // Загружаем лейблы для поста
                    List<Label> labels = getLabelsForPost(conn, post.getId());
                    post.setLabels(labels);

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении постов по метке: " + e.getMessage());
        }

        return posts;
    }

    /**
     * Генерирует следующий ID на основе максимального существующего
     */
    private Long generateNextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) FROM postmaker.posts";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1) + 1;
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при генерации ID: " + e.getMessage());
        }

        return 1L;
    }
}
