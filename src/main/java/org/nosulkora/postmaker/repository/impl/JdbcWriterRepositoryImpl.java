package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcWriterRepositoryImpl implements WriterRepository {

    @Override
    public Writer save(Writer writer) {

        if (writer.getId() == null) {
            writer.setId(generateNextId());
        }

        String sql = "INSERT INTO postmaker.writers (id, first_name, last_name, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, writer.getId());
            pstmt.setString(2, writer.getFirstName());
            pstmt.setString(3, writer.getLastName());
            pstmt.setString(4, writer.getStatus().name());
            pstmt.executeUpdate();

            return writer;

        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении писателя: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Writer update(Writer writer) {
        String sql = "UPDATE postmaker.writers SET first_name = ?, last_name = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, writer.getFirstName());
            pstmt.setString(2, writer.getLastName());
            pstmt.setString(3, writer.getStatus().name());
            pstmt.setLong(4, writer.getId());
            pstmt.executeUpdate();

            return writer;

        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении писателя: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Writer getById(Long id) {
        String sql = "SELECT * FROM postmaker.writers WHERE id = ? AND status != 'DELETED'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWriter(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при поиске писателя: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Writer> getAll() {
        List<Writer> writers = new ArrayList<>();
        String sql = "SELECT * FROM postmaker.writers WHERE status != 'DELETED' ORDER BY id";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                writers.add(mapResultSetToWriter(rs));
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при получении всех писателей: " + e.getMessage());
        }

        return writers;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "UPDATE postmaker.writers SET status = 'DELETED' WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Ошибка при удалении писателя: " + e.getMessage());
        }
    }

    private Writer mapResultSetToWriter(ResultSet rs) throws SQLException {
        Writer writer = new Writer();
        writer.setId(rs.getLong("id"));
        writer.setFirstName(rs.getString("first_name"));
        writer.setLastName(rs.getString("last_name"));
        writer.setStatus(Status.valueOf(rs.getString("status")));
        return writer;
    }

    /**
     * Генерирует следующий ID на основе максимального существующего
     */
    private Long generateNextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) FROM postmaker.writers";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1) + 1;
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при генерации ID: " + e.getMessage());
        }

        return 1L; // fallback
    }
}
