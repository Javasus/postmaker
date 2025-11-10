package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.ConnectionManager;
import org.nosulkora.postmaker.repository.LabelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JdbcLabelRepositoryImpl implements LabelRepository {
    private static final Logger logger = LoggerFactory.getLogger(JdbcLabelRepositoryImpl.class);

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
        Long labelId = ConnectionManager.executeInsert(
                SQL_CREATE_LABEL,
                ps -> setLabelParameters(ps, label)
        );
        if (labelId == null) {
            logger.error("Не удалось сохранить лейбл в БД: {}", label.getName());
            return null;
        }
        label.setId(labelId);
        return label;
    }

    @Override
    public Label update(Label label) {
        int affectedRows = ConnectionManager.executeUpdate(
                SQL_UPDATE_LABEL,
                ps -> {
                    setLabelParameters(ps, label);
                    try {
                        ps.setLong(3, label.getId());
                    } catch (SQLException e) {
                        logger.error("Ошибка установки параметров для обновления лейбла.", e);
                    }
                }
        );
        if (affectedRows == 0) {
            logger.error("Лейбл с ID " + label.getId() + " не найден для обновления.");
            return null;
        }
        return label;
    }

    @Override
    public Label getById(Long id) {
        Label label = ConnectionManager.executeQuerySingle(
                SQL_GET_LABEL_BY_ID,
                this::mapSingleResultSetToLabel,
                id
        );
        if (Objects.isNull(label)) {
            logger.error("Не удалось вернуть лейбл по ID: {}", id);
            return null;
        }
        return label;
    }

    @Override
    public List<Label> getAll() {
        List<Label> labels = ConnectionManager.executeQueryList(
                SQL_GET_ALL_LABELS,
                this::mapResultSetToLabelList
        );
        if (Objects.isNull(labels)) {
            logger.error("Ошибка при возврате всех лейблов.");
            return null;
        }
        return labels;
    }

    @Override
    public boolean deleteById(Long id) {
        int affectedRow = ConnectionManager.executeUpdate(SQL_DELETE_LABEL, ps -> {
                    try {
                        ps.setLong(1, id);
                    } catch (SQLException e) {
                        logger.error("Ошибка установки параметра для удаления.", e);
                    }
                }
        );
        if (affectedRow == 0) {
            logger.error("лейбл с ID " + id + " не найден для удаления.");
            return false;
        }
        return true;
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
            logger.error("Ошибка установки параметров лейбла: {}", label, e);
        }
    }

    /**
     * Маппит ResultSet в один объект Label
     */
    private Label mapSingleResultSetToLabel(ResultSet rs) {
        try {
            if (!rs.next()) {
                return null;
            }
            return createLabelFromResultSet(rs);
        } catch (SQLException e) {
            logger.error("Ошибка при получении одного лейбла из resultSet: {}", rs, e);
            return null;
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
            logger.error("Ошибка при получении списка лейблов: {}", rs, e);
            return null;
        }
    }

    /**
     * Создает базовый объект Label из ResultSet
     */
    private Label createLabelFromResultSet(ResultSet rs) {
        Label label = new Label();
        try {
            label.setId(rs.getLong("id"));
            label.setName(rs.getString("name"));
            label.setStatus(Status.valueOf(rs.getString("status")));
        } catch (SQLException e) {
            logger.error("Ошибка при получении лейбла из resultSet: {}", rs, e);
            return null;
        }
        return label;
    }
}
