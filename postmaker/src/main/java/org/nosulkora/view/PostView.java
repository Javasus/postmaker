package org.nosulkora.view;

import org.nosulkora.model.Post;

import java.io.BufferedReader;
import java.io.IOException;

public interface PostView extends GenericView<Post>{

    String[] getPostByView (BufferedReader reader, String message) throws IOException;

    String getCommandForPost(BufferedReader reader) throws IOException;

    void showPost(Post post);

    String updatePost(BufferedReader reader) throws IOException;

}
