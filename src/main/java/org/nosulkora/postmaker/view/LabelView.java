package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.controller.LabelController;
import org.nosulkora.postmaker.model.Label;

import java.util.List;
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

        Label label = labelController.createlabel(labelName);
        System.out.println("Label create: " + label);
    }

    public void getLabelById() {
        System.out.println("Enter labelId: ");
        Long id = Long.parseLong(scanner.nextLine());
        Label label = labelController.getLabelById(id);
        System.out.println("Lable by ID: " + label);
    }

    public void getAllLabels() {
        List<Label> labels = labelController.getAllLabels();
        labels.forEach(System.out::println);
    }

    public void updateLabel() {
        System.out.println("Enter labelId: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.println("Enter new name: ");
        String name = scanner.nextLine();
        Label label = labelController.updateLabel(id, name);
        System.out.println("Update label: " + label);
    }

    public void deleteLabel() {
        System.out.println("Enter labelId: ");
        Long id = Long.parseLong(scanner.nextLine());
        labelController.deleteLabel(id);
        System.out.println("Label is deleted.");
    }

}
