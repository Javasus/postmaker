package org.nosulkora.postmaker.view;

import org.nosulkora.postmaker.model.Post;

import java.io.BufferedReader;
import java.io.IOException;

public interface PostView extends GenericView<Post> {

    String[] getPostByView(BufferedReader reader, String message) throws IOException;

    String getCommandForPost(BufferedReader reader) throws IOException;

    void showPost(Post post);

    String deletePost(BufferedReader reader) throws IOException;
}
