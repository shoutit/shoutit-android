package com.shoutit.app.android.api.model;

import java.util.List;

public class PostMessage {

    private final String text;
    private final List<MessageAttachment> attachments;


    public PostMessage(String text, List<MessageAttachment> attachments) {
        this.text = text;
        this.attachments = attachments;
    }

    public String getText() {
        return text;
    }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

}