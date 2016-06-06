package com.shoutit.app.android.view.chats;

import com.shoutit.app.android.api.model.ChatMessage;
import com.shoutit.app.android.api.model.ConversationProfile;
import com.shoutit.app.android.api.model.MessageAttachment;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public class LocalMessageBus {

    private final PublishSubject<LocalMessage> localMessage = PublishSubject.create();

    public Observable<LocalMessage> getLocalMessageObservable() {
        return localMessage;
    }

    public void post(LocalMessage message) {
        localMessage.onNext(message);
    }

    public static class LocalMessage implements ChatMessage {

        private final ConversationProfile profile;
        private final String conversationId;
        private final String id;
        private final String text;
        private final List<MessageAttachment> attachments;
        private final long createdAt;

        public LocalMessage(ConversationProfile profile, String conversationId, String id, String text, List<MessageAttachment> attachments, long createdAt) {
            this.profile = profile;
            this.conversationId = conversationId;
            this.id = id;
            this.text = text;
            this.attachments = attachments;
            this.createdAt = createdAt;
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
    }
}
