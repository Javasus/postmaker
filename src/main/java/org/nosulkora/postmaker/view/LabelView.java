package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.model.Label;

import java.io.BufferedReader;
import java.io.IOException;

public interface LabelView extends GenericView<Label> {

    String[] getLabelByView(BufferedReader reader, String message) throws IOException;

    String getCommandForLabel(BufferedReader reader) throws IOException;

    void showLabel(Label label);

    String deleteLabel(BufferedReader reader) throws IOException;
}
