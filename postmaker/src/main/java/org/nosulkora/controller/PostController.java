package org.nosulkora.controller;

import org.nosulkora.repository.PostRepository;
import org.nosulkora.view.PostView;
import org.nosulkora.view.PostViewImpl;
import org.nosulkora.view.View;

import java.io.BufferedReader;
import java.io.IOException;

public class PostController implements  Controller{

    private final PostRepository postRepository;
    private final PostView postView;

    public PostController(PostRepository postRepository, PostView postView) {
        this.postRepository  = postRepository;
        this.postView = postView;
    }

    @Override
    public void create(BufferedReader reader) throws IOException {

//        String[] dataByView = view.

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
