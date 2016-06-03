package com.shoutit.app.android.api.model;

import java.util.List;

public class PusherMessage implements ChatMessage {

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public ConversationProfile getProfile() {
        return profile;
    }

    @Override
    public String getConversationId() {
        return conversationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PusherMessage that = (PusherMessage) o;

        if (createdAt != that.createdAt) return false;
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) return false;
        if (conversationId != null ? !conversationId.equals(that.conversationId) : that.conversationId != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        return attachments != null ? attachments.equals(that.attachments) : that.attachments == null;

    }

    @Override
    public int hashCode() {
        int result = profile != null ? profile.hashCode() : 0;
        result = 31 * result + (conversationId != null ? conversationId.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "PusherMessage{" +
                "profile=" + profile +
                ", conversationId='" + conversationId + '\'' +
                ", id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", attachments=" + attachments +
                ", createdAt=" + createdAt +
                '}';
    }
}