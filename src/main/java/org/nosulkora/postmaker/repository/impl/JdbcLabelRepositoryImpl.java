package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.ConnectionManager;
import org.nosulkora.postmaker.repository.LabelRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public Label save(Label label) throws RepositoryException {
        try {
            Long lableId = ConnectionManager.executeInsert(
                    SQL_CREATE_LABEL,
                    ps -> setLabelParameters(ps, label)
            );
            label.setId(lableId);
            return label;
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при сохранении лейбла: " + label, e);
        }
    }

    @Override
    public Label update(Label label) throws RepositoryException {
        try {
            int affectedRows = ConnectionManager.executeUpdate(
                    SQL_UPDATE_LABEL,
                    ps -> {
                        setLabelParameters(ps, label);
                        try {
                            ps.setLong(3, label.getId());
                        } catch (SQLException e) {
                            throw new RepositoryException("Ошибка установки параметров для обновления лейбла.", e);
                        }
                    }
            );
            if (affectedRows == 0) {
                throw new RepositoryException("Лейбл с ID " + label.getId() + " не найден для обновления.");
            }
            return label;
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при обновлении лейбла с id: " + label.getId(), e);
        }
    }

    @Override
    public Label getById(Long id) throws RepositoryException {
        try {
            return ConnectionManager.executeQuerySingle(
                    SQL_GET_LABEL_BY_ID,
                    this::mapSingleResultSetToLabel,
                    id
            );
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при поиске лейбла по id: " + id, e);
        }
    }

    @Override
    public List<Label> getAll() throws RepositoryException {
        try {
            return ConnectionManager.executeQueryList(
                    SQL_GET_ALL_LABELS,
                    this::mapResultSetToLabelList
            );
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при получении всех лейблов: ", e);
        }
    }

    @Override
    public void deleteById(Long id) throws RepositoryException {
        try {
            int affectedRow = ConnectionManager.executeUpdate(SQL_DELETE_LABEL, ps -> {
                        try {
                            ps.setLong(1, id);
                        } catch (SQLException e) {
                            throw new RepositoryException("Ошибка установки параметра для удаления." + e);
                        }
                    }
            );
            if (affectedRow == 0) {
                throw new RepositoryException("лейбл с ID " + id + " не найден для удаления.");
            }
        } catch (RepositoryException e) {
            throw new RepositoryException("Ошибка при удалении лейбла с id : " + id, e);
        }
    }

    //---------------------------private methods-----------------------------------------------------------

    /**
     * Устанавливает параметры для PreparedStatement из объекта Label
     */
    private void setLabelParameters(PreparedStatement ps, Label label) {
        try {
            ps.setString(1, label.getName());
            ps.setString(2, label.getStatus().name());
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка установки параметров лейбла." + e);
        }
    }

    /**
     * Маппит ResultSet в один объект Label
     */
    private Label mapSingleResultSetToLabel(ResultSet rs) throws RepositoryException {
        try {
            if (!rs.next()) {
                return null;
            }
            return createLabelFromResultSet(rs);
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка маппинга ResultSet в Label", e);
        }
    }

    /**
     * Маппит ВЕСЬ ResultSet в список Label (для методов, возвращающих List<Label>)
     */
    private List<Label> mapResultSetToLabelList(ResultSet rs) {
        try {
            Map<Long, Label> labelsMap = new LinkedHashMap<>();

            while (rs.next()) {
                Long labelId = rs.getLong("id");
                Label label = labelsMap.get(labelId);
                if (label == null) {
                    label = createLabelFromResultSet(rs);
                    labelsMap.put(labelId, label);
                }
            }
            return new ArrayList<>(labelsMap.values());
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка маппинга ResultSet в список Label", e);
        }
    }

    /**
     * Создает базовый объект Label из ResultSet
     */
    private Label createLabelFromResultSet(ResultSet rs) throws SQLException {
        Label label = new Label();
        label.setId(rs.getLong("id"));
        label.setName(rs.getString("name"));
        label.setStatus(Status.valueOf(rs.getString("status")));
        return label;
    }
}
