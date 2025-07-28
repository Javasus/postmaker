package org.nosulkora.postmaker.model;

import java.util.Random;

public class Label {

    private Long id;

    private String name;

    private Status status;

    public Label(String name) {
        this.id = new Random().nextLong(0, Long.MAX_VALUE);
        this.name = name;
        this.status = Status.ACTIVE;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Label{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
