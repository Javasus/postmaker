package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.model.Writer;

import java.io.BufferedReader;
import java.io.IOException;

public interface WriterView extends GenericView<Writer> {

    String[] getNameByView(BufferedReader reader) throws IOException;

    String getCommandForWriter(BufferedReader reader) throws IOException;

    void createWriter(Writer writer, boolean isExist);

    void showWriter(Writer writer);

    String updateWriter(BufferedReader reader) throws IOException;
}
