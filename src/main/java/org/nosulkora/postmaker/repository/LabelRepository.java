package org.nosulkora.postmaker.repository;

import org.nosulkora.postmaker.model.Label;

import java.util.List;

public interface LabelRepository extends GenericRepository<Label, Long> {

    /**
     * Добавляет новый Label в файл labels.json.
     *
     * @param label  Обьект поста Post
     * @return boolean
     */
    Boolean createLabel(Label label);

    /**
     * Возвращает список всех лейблов из файла labels.json.
     *
     * @return Список лейблов
     */
    List<Label> getAllLabels();

    /**
     * Возвращает лейбл по идентификатору.
     *
     * @param id идентификатор лейбла
     * @return Label
     */
    Label getLabelById(Long id);

    /**
     * Обновляет name в лейбле.
     *
     * @param label обновлённый лейбл.
     * @return Label
     */
    Label updateLabel(Label label);
}
