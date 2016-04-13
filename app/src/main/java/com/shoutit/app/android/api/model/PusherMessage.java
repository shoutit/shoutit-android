package com.shoutit.app.android.api.model;

import java.util.List;

public class PusherMessage {

    private final ConversationProfile profile;
    private final String conversationId;
    private final String id;
    private final String text;
    private final List<MessageAttachment> attachments;
    private final long createdAt;


    public PusherMessage(ConversationProfile profile, String conversationId, String id, String text, List<MessageAttachment> attachments, long createdAt) {
        this.profile = profile;
        this.conversationId = conversationId;
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