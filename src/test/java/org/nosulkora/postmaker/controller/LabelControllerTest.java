package org.nosulkora.postmaker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.LabelRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LabelControllerTest")
class LabelControllerTest {

    private static final String LABEL_NAME = "labelTest";
    private static final String UPDATED_LABEL_NAME = "newLabelTest";
    private static final Long LABEL_ID = 1L;
    private static final Long SECOND_LABEL_ID = 2L;
    private LabelController labelController;
    private LabelRepository labelRepository;

    private Label createTestLabel(Long id, String name, Status status) {
        Label label = new Label();
        label.setId(id);
        label.setName(name);
        label.setStatus(status);
        return label;
    }

    private Label createDefaultTestLabel() {
        return createTestLabel(LABEL_ID, LABEL_NAME, Status.ACTIVE);
    }

    @BeforeEach
    void setUp() {
        labelRepository = mock(LabelRepository.class);
        labelController = new LabelController(labelRepository);
    }

    @Test
    @DisplayName("Create label.")
    void createLabelTest() {
        Label expectedLabel = createDefaultTestLabel();
        when(labelRepository.save(any(Label.class))).thenReturn(expectedLabel);

        Label actualLabel = labelController.createLabel(LABEL_NAME);

        assertAll(
                "Check create label:",
                () -> assertEquals(expectedLabel.getName(), actualLabel.getName(), "labelName"),
                () -> assertEquals(expectedLabel.getStatus(), actualLabel.getStatus(), "labelStatus"),
                () -> assertEquals(expectedLabel.getId(), actualLabel.getId(), "labelId")
        );

        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    @DisplayName("Get label by id")
    void getLabelByIdTest() {
        Label expectedLabel = createDefaultTestLabel();
        when(labelRepository.getById(any(Long.class))).thenReturn(expectedLabel);

        Label actualLabel = labelController.getLabelById(1L);

        assertAll(
                "Check get label by id:",
                () -> assertEquals(expectedLabel.getName(), actualLabel.getName(), "labelName"),
                () -> assertEquals(expectedLabel.getStatus(), actualLabel.getStatus(), "labelStatus"),
                () -> assertEquals(expectedLabel.getId(), actualLabel.getId(), "labelId")
        );

        verify(labelRepository, times(1)).getById(LABEL_ID);
    }

    @Test
    @DisplayName("Get all labels")
    void getAllLabelsTest() {
        Label expectedLabel1 = createDefaultTestLabel();
        Label expectedLabel2 = createTestLabel(SECOND_LABEL_ID, UPDATED_LABEL_NAME, Status.ACTIVE);
        List<Label> expectedLabelList = Arrays.asList(expectedLabel1, expectedLabel2);

        when(labelRepository.getAll()).thenReturn(expectedLabelList);

        List<Label> actualLabelsList = labelController.getAllLabels();

        assertThat(expectedLabelList)
                .describedAs("Check get all labels.")
                .containsExactlyInAnyOrderElementsOf(actualLabelsList);

        verify(labelRepository, times(1)).getAll();
    }

    @Test
    @DisplayName("Update label")
    void updateLabelTest() {
        Label expectedLabel = createDefaultTestLabel();
        Label uploadLabel = createTestLabel(LABEL_ID, UPDATED_LABEL_NAME, Status.ACTIVE);

        when(labelRepository.getById(LABEL_ID)).thenReturn(expectedLabel);
        when(labelRepository.update(any(Label.class))).thenReturn(uploadLabel);

        Label actualLabel = labelController.updateLabel(1L, UPDATED_LABEL_NAME);

        assertAll(
                "Check update label:",
                () -> assertEquals(uploadLabel.getName(), actualLabel.getName(), "labelName"),
                () -> assertEquals(uploadLabel.getStatus(), actualLabel.getStatus(), "labelStatus"),
                () -> assertEquals(uploadLabel.getId(), actualLabel.getId(), "labelId")
        );

        verify(labelRepository, times(1)).getById(LABEL_ID);
        verify(labelRepository, times(1)).update(any(Label.class));
    }

    @Test
    @DisplayName("Delete label")
    void deleteLabelTest() {
        labelController.deleteLabel(LABEL_ID);

        verify(labelRepository, times(1)).deleteById(LABEL_ID);
    }

    @Test
    @DisplayName("Get not-existent label.")
    void getNotExistentLabelTest() {
        when(labelRepository.getById(LABEL_ID)).thenReturn(null);

        Label labelById = labelController.getLabelById(LABEL_ID);

        assertThat(labelById).isNull();
    }

    @Test
    @DisplayName("Delete label with repository exception")
    void deleteLabelWithExceptionsTest() {
        doThrow(new RuntimeException("DB error")).when(labelRepository).deleteById(LABEL_ID);

        assertThrows(RuntimeException.class, () -> labelController.deleteLabel(LABEL_ID));
    }
}
