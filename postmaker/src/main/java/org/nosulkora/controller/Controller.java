package org.nosulkora.controller;

import java.io.BufferedReader;
import java.io.IOException;

public interface Controller {
    void create(BufferedReader reader) throws IOException;

    void read(BufferedReader reader) throws IOException;

    void update(BufferedReader reader) throws IOException;

    void delete(BufferedReader reader) throws IOException;
}
