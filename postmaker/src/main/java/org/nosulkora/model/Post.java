package org.nosulkora.model;

import java.util.List;
import java.util.UUID;

public class Post {
    UUID id;

    String title;

    String content;

    List<Label> labels;

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "POST{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", labels=" + labels +
                '}';
    }
}
