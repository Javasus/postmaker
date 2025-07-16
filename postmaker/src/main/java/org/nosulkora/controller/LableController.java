package org.nosulkora.controller;

import org.nosulkora.repository.LabelRepository;
import org.nosulkora.view.LabelView;
import org.nosulkora.view.View;

import java.io.BufferedReader;
import java.io.IOException;

public class LableController implements Controller{
    private final LabelRepository labelRepository;
    private final LabelView labelView;

    public LableController(LabelRepository labelRepository, LabelView labelView) {
        this.labelRepository = labelRepository;
        this.labelView = labelView;
    }

    @Override
    public void create(BufferedReader reader) throws IOException {

    }

    @Override
    public void read(BufferedReader reader) throws IOException {

    }

    @Override
    public void update(BufferedReader reader) throws IOException {

    }

    @Override
    public void delete(BufferedReader reader) throws IOException {

    }
}
