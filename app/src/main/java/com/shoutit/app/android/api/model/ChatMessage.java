package com.shoutit.app.android.api.model;

import java.util.List;

public interface ChatMessage {
    String getId();

    String getText();

    List<MessageAttachment> getAttachments();

    long getCreatedAt();

    ConversationProfile getProfile();

    String getConversationId();
}
