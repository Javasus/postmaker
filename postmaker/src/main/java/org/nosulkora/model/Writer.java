package org.nosulkora.model;

import java.util.List;
import java.util.UUID;

public class Writer {
    UUID id;

    String firstName;

    String lastname;

    List<Post> posts;

    Status status;

    public Writer(UUID id, String firstName, String lastname, List<Post> posts, Status status) {
        this.id = id;
        this.firstName = firstName;
        this.lastname = lastname;
        this.posts = posts;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Writer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastname='" + lastname + '\'' +
                ", posts=" + posts +
                ", status=" + status +
                '}';
    }
}
