package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.LabelRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcLabelRepositoryImpl implements LabelRepository {
    @Override
    public Label save(Label label) {

        if (label.getId() == null) {
            label.setId(generateNextId());
        }

        String sql = "INSERT INTO postmaker.labels (id, name, status) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, label.getId());
            pstmt.setString(2, label.getName());
            pstmt.setString(3, label.getStatus().name());
            pstmt.executeUpdate();

            return label;

        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении лейбла: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Label update(Label label) {
        String sql = "UPDATE postmaker.labels SET name = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, label.getName());
            pstmt.setString(2, label.getStatus().name());
            pstmt.setLong(3, label.getId());
            pstmt.executeUpdate();

            return label;

        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении лейбла: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Label getById(Long id) {
        String sql = "SELECT * FROM postmaker.labels WHERE id = ? AND status != 'DELETED'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLabel(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при поиске лейбла: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Label> getAll() {
        List<Label> labels = new ArrayList<>();
        String sql = "SELECT * FROM postmaker.labels WHERE status != 'DELETED' ORDER BY id";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                labels.add(mapResultSetToLabel(rs));
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при получении всех лейблов: " + e.getMessage());
        }

        return labels;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "UPDATE postmaker.labels SET status = 'DELETED' WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Ошибка при удалении лейбла: " + e.getMessage());
        }
    }

    private Label mapResultSetToLabel(ResultSet rs) throws SQLException {
        Label label = new Label();
        label.setId(rs.getLong("id"));
        label.setName(rs.getString("name"));
        label.setStatus(Status.valueOf(rs.getString("status")));
        return label;
    }

    /**
     * Генерирует следующий ID на основе максимального существующего
     */
    private Long generateNextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) FROM postmaker.labels";

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
