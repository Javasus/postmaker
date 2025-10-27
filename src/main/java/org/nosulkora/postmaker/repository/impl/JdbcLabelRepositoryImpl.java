package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.ConnectionManager;
import org.nosulkora.postmaker.repository.LabelRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcLabelRepositoryImpl implements LabelRepository {

    private static final String SQL_CREATE_LABEL =
            "INSERT INTO postmaker.labels (name, status) VALUES (?, ?)";

    private static final String SQL_UPDATE_LABEL =
            "UPDATE postmaker.labels SET name = ?, status = ? WHERE id = ?";

    private static final String SQL_GET_LABEL_BY_ID =
            "SELECT id, name, status FROM postmaker.labels WHERE id = ? AND status != 'DELETED'";

    private static final String SQL_GET_ALL_LABELS =
            "SELECT id, name, status FROM postmaker.labels WHERE status != 'DELETED' ORDER BY id";

    private static final String SQL_DELETE_LABEL =
            "UPDATE postmaker.labels SET status = 'DELETED' WHERE id = ?";

    private static final String SQL_GET_LABEL_BY_NAME =
            "SELECT id, name, status FROM postmaker.labels WHERE name = ? AND status != 'DELETED'";

    private static final String SQL_GET_LABELS_BY_POST_ID = """
        SELECT l.id, l.name, l.status
        FROM postmaker.labels l
        JOIN postmaker.post_labels pl ON l.id = pl.label_id
        WHERE pl.post_id = ? AND l.status != 'DELETED'
        ORDER BY l.name
        """;

    @Override
    public Label save(Label label) {
        try (Connection conn = ConnectionManager.autoCommitConnection()) {
            return insertLabel(conn, label);
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при сохранении лейбла: " + label, e);
        }
    }

    @Override
    public Label update(Label label) {
        try (Connection conn = DatabaseManager.getConnection()) {
            updateLabel(conn, label);
            return label;
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при обновлении лейбла с id: " + label.getId(), e);
        }
    }

    @Override
    public Label getById(Long id) {
        try (Connection conn = ConnectionManager.autoCommitConnection();
        PreparedStatement ps = conn.prepareStatement(SQL_GET_LABEL_BY_ID)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToLabel(rs) : null;
            }

        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при поиске лейбла по id: " + id, e);
        }
    }

    @Override
    public List<Label> getAll() {
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_LABELS);
             ResultSet rs = ps.executeQuery()) {

            List<Label> labels = new ArrayList<>();
            while (rs.next()) {
                labels.add(mapResultSetToLabel(rs));
            }
            return labels;

        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении всех лейблов: ", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = ConnectionManager.autoCommitConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE_LABEL)) {
            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при удалении лейбла с id : " + id, e);
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
     *  Создание лейбла.
     */
    private Label insertLabel(Connection conn, Label label) throws SQLException {
        try (PreparedStatement ps = ConnectionManager.preparedStatementWithKeys(conn, SQL_CREATE_LABEL)) {
            ps.setString(1, label.getName());
            ps.setString(2, label.getStatus().name());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("Не удалось создать лейбл, ни одна запись не была добавлена.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    label.setId(rs.getLong(1));
                    return label;
                } else {
                    throw new SQLException("Не удалось создать лейбл, id не получен.");
                }
            }
        }
    }

    /**
     *  Обновляем лейбл.
     */
   private void updateLabel(Connection conn, Label label) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_LABEL)) {
            ps.setString(1, label.getName());
            ps.setString(2, label.getStatus().name());
            ps.setLong(3, label.getId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("бновление лейбла не удалось, ни одна запись не была изменена.");
            }
        }
   }
}
