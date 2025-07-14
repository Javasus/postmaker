package org.nosulkora.model;

import java.util.List;

public class Post {

    private static Long ID = 0L;

    private Long id;

    private String title;

    private String content;

    private List<Label> labels;

    public Post(String title, String content, List<Label> labels) {
        this.id = ID++;
        this.title = title;
        this.content = content;
        this.labels = labels;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
