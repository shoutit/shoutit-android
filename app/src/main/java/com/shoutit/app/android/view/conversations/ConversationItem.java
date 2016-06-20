package com.shoutit.app.android.view.conversations;

import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.PusherConversationUpdate;

public class ConversationItem {

    private final String id;
    private final String image;
    private final String subtitle;
    private final String title;
    private final String lastMessageSummary;
    private final long modifiedAt;
    private final String type;
    private final int unreadMessagesCount;

    private ConversationItem(String id, String image, String subtitle, String title, String lastMessageSummary, long modifiedAt, String type, int unreadMessagesCount) {
        this.id = id;
        this.image = image;
        this.subtitle = subtitle;
        this.title = title;
        this.lastMessageSummary = lastMessageSummary;
        this.modifiedAt = modifiedAt;
        this.type = type;
        this.unreadMessagesCount = unreadMessagesCount;
    }

    public static ConversationItem fromConversation(Conversation conversation) {
        return new ConversationItem(conversation.getId(), conversation.getImage(), conversation.getSubTitle(), conversation.getTitle(), conversation.getLastMessageSummary(), conversation.getModifiedAt(), conversation.getType(), conversation.getUnreadMessagesCount());
    }

    public static ConversationItem fromPusherUpdateConversation(PusherConversationUpdate conversation) {
        return new ConversationItem(conversation.getId(), conversation.getImage(), conversation.getSubTitle(), conversation.getTitle(), conversation.getLastMessageSummary(), conversation.getModifiedAt(), conversation.getType(), conversation.getUnreadMessagesCount());
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getSubTitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }

    public String getLastMessageSummary() {
        return lastMessageSummary;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public String getType() {
        return type;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public ConversationItem withUpdatedLastMessage(String text, long createdAt, boolean isOwnMessage) {
        return new ConversationItem(id, image, subtitle, title, text, createdAt, type, unreadMessagesCount + (isOwnMessage ? 0 : 1));
    }

    public ConversationItem withIsReadTrue() {
        return new ConversationItem(id, image, subtitle, title, lastMessageSummary, modifiedAt, type, 0);
    }
}
