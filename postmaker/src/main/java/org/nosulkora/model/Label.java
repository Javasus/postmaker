package org.nosulkora.model;

import java.util.Random;

public class Label {

    private Long id;

    private String name;

    public Label(String name) {
        this.id = new Random().nextLong(0, Long.MAX_VALUE);
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Label{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
