package org.nosulkora.controller;

import org.nosulkora.model.Label;
import org.nosulkora.model.Post;
import org.nosulkora.model.Status;
import org.nosulkora.model.Writer;
import org.nosulkora.repository.LabelRepository;
import org.nosulkora.repository.PostRepository;
import org.nosulkora.repository.WriterRepository;
import org.nosulkora.view.LabelView;
import org.nosulkora.view.PostView;
import org.nosulkora.view.WriterView;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class LableController implements Controller {

    private static final String YOU_ARE_WRONG = "Ты ввёл не верные данные. Пожалуйста следуй инструкциям.";

    private final WriterRepository writerRepository;
    private final WriterView writerView;
    private final PostRepository postRepository;
    private final PostView postView;
    private final LabelRepository labelRepository;
    private final LabelView labelView;

    public LableController(
            WriterRepository writerRepository,
            WriterView writerView,
            PostRepository postRepository,
            PostView postView,
            LabelRepository labelRepository,
            LabelView labelView
    ) {
        this.writerRepository = writerRepository;
        this.writerView = writerView;
        this.postRepository = postRepository;
        this.postView = postView;
        this.labelRepository = labelRepository;
        this.labelView = labelView;
    }

    @Override
    public void create(BufferedReader reader) throws IOException {
        String[] dataLabel = labelView.getLabelByView(
                reader,
                "Введи id post'а, к которому хочешь назначить лейбл."
        );
        if (dataLabel != null
                && dataLabel.length == 2
                && dataLabel[0].matches("\\d+")
                && !dataLabel[1].isEmpty()
        ) {
            Label label = new Label(dataLabel[1]);
            Long postId = Long.parseLong(dataLabel[0]);
            if (labelRepository.createLabel(label)) {
//                если лейбл создался тогда обновим пост.
                labelView.showLabel(label);
                Post post = postRepository.updatePostWithNewLabel(postId, label);
                if (Objects.nonNull(post)) {
//                    Если пост обновился, тогда обновим писателя.
                    postView.showPost(post);
                    Writer writer = writerRepository.updatePostInWriter(post);
                    if (Objects.nonNull(writer)) {
                        writerView.showWriter(writer);
                    } else {
                        System.out.println(YOU_ARE_WRONG);
                    }
                } else {
                    System.out.println(YOU_ARE_WRONG);
                }
            }
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        String command = labelView.getCommandForLabel(reader);
        if (command.equalsIgnoreCase("All")) {
            List<Label> allLabel = labelRepository.getAllLabels();
            allLabel.forEach(labelView::showLabel);
        } else if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
            Label labelById = labelRepository.getLabelById(id);
            labelView.showLabel(labelById);
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    @Override
    public void update(BufferedReader reader) throws IOException {

        String[] updateLabelData = labelView.getLabelByView(
                reader,
                "Введи id Label'а который хочешь изменить."
        );

        if (updateLabelData != null
                && updateLabelData.length == 2
                && updateLabelData[0].matches("\\d+")
                && !updateLabelData[1].isEmpty()
        ) {
            Long labelId = Long.parseLong(updateLabelData[0]);
            String labelName = updateLabelData[1];
//            Ищем лейбл по переданному id.
            Label label = labelRepository.getLabelById(labelId);
            if (Objects.nonNull(label)) {
                label.setName(labelName);
//                если такой лейбл существует обновляем его.
                Label updateLabel = labelRepository.updateLabel(label);
                if (Objects.nonNull(updateLabel)) {
                    labelView.showLabel(updateLabel);
//                    если лейбл обновился отправляем его, что бы он обновился в посте.
                    Post updatePost = postRepository.updateLabelInPost(label);
                    if (Objects.nonNull(updatePost)) {
//                        если пост обновился отправляем его, что бы он обновился в писателе.
                        postView.showPost(updatePost);
                        Writer writer = writerRepository.updatePostInWriter(updatePost);
                        if (Objects.nonNull(writer)) {
                            writerView.showWriter(writer);
                        } else {
                            System.out.println(YOU_ARE_WRONG);
                        }
                    } else {
                        System.out.println(YOU_ARE_WRONG);
                    }
                }
            }
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }

    @Override
    public void delete(BufferedReader reader) throws IOException {
        String command = labelView.deleteLabel(reader);
        if (command.matches("\\d+")) {
            Long id = Long.parseLong(command);
//            Ищем лейбл по переданному id.
            Label label = labelRepository.getLabelById(id);
            if (Objects.nonNull(label)) {
//                если такой лейбл существует status -> DELETED и обновляем его.
                label.setStatus(Status.DELETED);
                Label updateLabel = labelRepository.updateLabel(label);
                if (Objects.nonNull(updateLabel)) {
//                    если лейбл обновился отправляем его, что бы он обновился в посте.
                    labelView.showLabel(updateLabel);
                    Post updatePost = postRepository.updateLabelInPost(updateLabel);
                    if (Objects.nonNull(updatePost)) {
//                        если пост обновился отправляем его, что бы он обновился в писателе.
                        postView.showPost(updatePost);
                        Writer writer = writerRepository.updatePostInWriter(updatePost);
                        if (Objects.nonNull(writer)) {
                            writerView.showWriter(writer);
                        } else {
                            System.out.println(YOU_ARE_WRONG);
                        }
                    } else {
                        System.out.println(YOU_ARE_WRONG);
                    }
                }
            }
        } else {
            System.out.println(YOU_ARE_WRONG);
        }
    }
}
