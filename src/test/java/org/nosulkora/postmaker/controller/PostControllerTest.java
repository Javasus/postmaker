package org.nosulkora.postmaker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.PostRepository;
import org.nosulkora.postmaker.repository.WriterRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PostControllerTest")
public class PostControllerTest {

    private static final long POST_ID = 1L;
    private static final String POST_TITLE = "postTitle";
    private static final String POST_CONTENT = "postContent";
    private static final Long POST_WRITER_ID = 11L;

    private PostController postController;
    private PostRepository postRepository;
    private WriterRepository writerRepository;

    @BeforeEach
    void setUp() {
        postRepository = mock(PostRepository.class);
        writerRepository = mock(WriterRepository.class);
        postController = new PostController(writerRepository, postRepository);
    }

    private Label createTestLabel(Long id, String name, Status status) {
        Label label = new Label();
        label.setId(id);
        label.setName(name);
        label.setStatus(status);
        return label;
    }

    private Post createTestPost(Long id, String title, String content, Long writerId, List<Label> labels) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setContent(content);
        post.setWriterId(writerId);
        post.setLabels(labels);
        return post;
    }

    private List<Label> createTestLabels() {
        Label label1 = createTestLabel(111L, "labelTestName1", Status.ACTIVE);
        Label label2 = createTestLabel(112L, "labelTestName2", Status.ACTIVE);
        return Arrays.asList(label1, label2);
    }

    private Post createDefaultTestPost() {
        List<Label> labels = createTestLabels();
        return createTestPost(POST_ID, POST_TITLE, POST_CONTENT, POST_WRITER_ID, labels);
    }

    private Writer creatTestWriter(Long id, String firstName, String lastName, Status status) {
        Writer writer = new Writer();
        writer.setId(id);
        writer.setFirstName(firstName);
        writer.setLastName(lastName);
        writer.setStatus(status);
        return writer;
    }

    private Writer createDefaultTestWriter() {
        return creatTestWriter(POST_WRITER_ID, "Roman", "Abramovich", Status.ACTIVE);
    }

    @Test
    @DisplayName("Create post")
    void createPostTest() {
        Post expectedPost = createDefaultTestPost();
        Writer writer = createDefaultTestWriter();

        when(writerRepository.getById(POST_WRITER_ID)).thenReturn(writer);
        when(postRepository.save(any(Post.class))).thenReturn(expectedPost);

        Post actualPost = postController.createPost(POST_TITLE, POST_CONTENT, POST_WRITER_ID, createTestLabels());

        assertAll(
                "Check create post",
                () -> assertEquals(expectedPost.getId(), actualPost.getId(), "postId"),
                () -> assertEquals(expectedPost.getTitle(), actualPost.getTitle(), "postTitle"),
                () -> assertEquals(expectedPost.getContent(), actualPost.getContent(), "postContent"),
                () -> assertThat(expectedPost.getLabels())
                        .describedAs("postLabels")
                        .containsExactlyInAnyOrderElementsOf(actualPost.getLabels()),
                () -> assertEquals(expectedPost.getWriterId(), actualPost.getWriterId(), "postWriterId"),
                () -> assertEquals(expectedPost.getStatus(), actualPost.getStatus(), "postStatus")
        );

        verify(writerRepository, times(1)).getById(POST_WRITER_ID);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Create post with not-exist writer")
    void createPostWithNotExistWriterTest() {

        when(writerRepository.getById(POST_WRITER_ID)).thenReturn(null);

        Post post = postController.createPost(POST_TITLE, POST_CONTENT, POST_WRITER_ID, createTestLabels());

        assertNull(post);

        verify(writerRepository, times(1)).getById(POST_WRITER_ID);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Get post by id")
    void getPostByIdTest() {
        Post expectedPost = createDefaultTestPost();
        when(postRepository.getById(POST_ID)).thenReturn(expectedPost);

        Post actualPost = postController.getPostById(POST_ID);

        assertAll(
                "Check create post",
                () -> assertEquals(expectedPost.getId(), actualPost.getId(), "postId"),
                () -> assertEquals(expectedPost.getTitle(), actualPost.getTitle(), "postTitle"),
                () -> assertEquals(expectedPost.getContent(), actualPost.getContent(), "postContent"),
                () -> assertThat(expectedPost.getLabels())
                        .describedAs("postLabels")
                        .containsExactlyInAnyOrderElementsOf(actualPost.getLabels()),
                () -> assertEquals(expectedPost.getWriterId(), actualPost.getWriterId(), "postWriterId"),
                () -> assertEquals(expectedPost.getStatus(), actualPost.getStatus(), "postStatus")
        );

        verify(postRepository, times(1)).getById(POST_ID);

    }

    @Test
    @DisplayName("get all posts")
    void getAllPostsTest() {
        Post expectedPost1 = createDefaultTestPost();
        Post expectedPost2 = createTestPost(
                2L,
                "Title2",
                "Content2",
                POST_WRITER_ID,
                Collections.emptyList()
        );

        List<Post> expectedPosts = Arrays.asList(expectedPost1, expectedPost2);

        when(postRepository.getAll()).thenReturn(expectedPosts);

        List<Post> actualPosts = postController.getAllPosts();

        assertThat(expectedPosts)
                .describedAs("Check get all posts")
                .containsExactlyInAnyOrderElementsOf(actualPosts);

        verify(postRepository, times(1)).getAll();
    }

    @Test
    @DisplayName("Update post")
    void updatePostTest() {
        Post oldPost = createDefaultTestPost();
        String newTitle = "Updated Title";
        String newContent = "Updated Content";
        List<Label> newLabels = Collections.singletonList(createTestLabel(333L, "New Label", Status.ACTIVE));

        when(postRepository.getById(POST_ID)).thenReturn(oldPost);
        when(postRepository.update(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post actualPost = postController.updatePost(POST_ID, newTitle, newContent, newLabels);

        assertAll(
                "Check update fields post",
                () -> assertEquals(newTitle, actualPost.getTitle(), "postTitle"),
                () -> assertEquals(newContent, actualPost.getContent(), "postContent"),
                () -> assertThat(newLabels)
                        .describedAs("postLabels")
                        .containsExactlyInAnyOrderElementsOf(actualPost.getLabels())
        );

        verify(postRepository, times(1)).getById(POST_ID);
        verify(postRepository, times(1)).update(any(Post.class));
    }

    @Test
    @DisplayName("Update with not-existent post")
    void updatePostWithNotExistentPostTest() {
        when(postRepository.getById(POST_ID)).thenReturn(null);

        Post post = postController.updatePost(
                POST_ID,
                "TitleEx",
                "ContentEx",
                Collections.emptyList());

        assertNull(post);

        verify(postRepository, times(1)).getById(POST_ID);
        verify(postRepository, never()).update(any(Post.class));
    }

    @Test
    @DisplayName("Delete post")
    void deletePostTest() {
        postController.deletePost(POST_ID);
        verify(postRepository, times(1)).deleteById(POST_ID);
    }
}
