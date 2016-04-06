package com.shoutit.app.android.api.model;

import java.util.List;

public class Message {

    private final String conversationId;
    private final ConversationProfile profile;
    private final String id;
    private final String text;
    private final List<MessageAttachment> attachments;
    private final long createdAt;


    public Message(String conversationId, ConversationProfile profile, String id, String text, List<MessageAttachment> attachments, long createdAt) {
        this.conversationId = conversationId;
        this.profile = profile;
        this.id = id;
        this.text = text;
        this.attachments = attachments;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public ConversationProfile getProfile() {
        return profile;
    }

    public String getConversationId() {
        return conversationId;
    }
}