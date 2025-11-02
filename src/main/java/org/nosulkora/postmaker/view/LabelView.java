package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.LabelController;
import org.nosulkora.postmaker.exceptions.RepositoryException;
import org.nosulkora.postmaker.model.Label;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class LabelView {

    private final Scanner scanner;
    private final LabelController labelController;

    public LabelView() {
        scanner = new Scanner(System.in);
        labelController = new LabelController();
    }

    public LabelView(Scanner scanner) {
        this.scanner = scanner;
        labelController = new LabelController();
    }

    public LabelView(Scanner scanner, LabelController labelController) {
        this.scanner = scanner;
        this.labelController = labelController;
    }

    public void createLabel() {
        System.out.println("Enter label name: ");
        String labelName = scanner.nextLine();

        try {
            Label label = labelController.createLabel(labelName);
            System.out.println("Label create: " + label);
        } catch (RepositoryException e) {
            System.out.println("При создании лейбла возникла ошибка - " + e);
        }
    }

    public void getLabelById() {
        System.out.println("Enter labelId: ");
        Long id = Long.parseLong(scanner.nextLine());
        try {
            Label label = labelController.getLabelById(id);
            if (Objects.isNull(label)) {
                System.out.println("Лейбл с ID " + id + " не найден.");
            }
            System.out.println("Lable by ID: " + label);
        } catch (RepositoryException e) {
            System.out.println("При возврате лейбла по ID возникла ошибка - " + e);
        }
    }

    public void getAllLabels() {
        try {
            List<Label> labels = labelController.getAllLabels();
            labels.forEach(System.out::println);
        } catch (RepositoryException e) {
            System.out.println("При возврате всех лейблов возникла ошибка - " + e);
        }
    }

    public void updateLabel() {
        System.out.println("Enter labelId: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.println("Enter new name: ");
        String name = scanner.nextLine();
        try {
            Label label = labelController.updateLabel(id, name);
            System.out.println("Update label: " + label);
        } catch (RepositoryException e) {
            System.out.println("При обновлении лейбла возникла ошибка - " + e);
        }
    }

    public void deleteLabel() {
        System.out.println("Enter labelId: ");
        Long id = Long.parseLong(scanner.nextLine());
        try {
            labelController.deleteLabel(id);
            System.out.println("Label is deleted.");
        } catch (RepositoryException e) {
            System.out.println("При удалении лейбла возникла ошибка - " + e);
        }
    }
}
