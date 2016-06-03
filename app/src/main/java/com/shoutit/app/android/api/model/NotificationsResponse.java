package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class NotificationsResponse extends PaginatedResponse {

    private static final String NOTIFICATION_MESSAGE = "new_message";
    private static final String NOTIFICATION_LISTEN = "new_listen";

    @Nonnull
    private final List<Notification> results;

    public NotificationsResponse(int count, @Nullable String next, @Nullable String previous,
                                 @Nonnull List<Notification> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nonnull
    public List<Notification> getResults() {
        return results;
    }

    public class Notification {
        @Nonnull
        private final String id;
        @Nonnull
        private final String type;
        private final int createdAt;
        private final boolean isRead;
        private final DisplayInfo display;
        private final AttachedObject attachedObject;
        @Nullable
        private final String appUrl;

        public Notification(@Nonnull String id, @Nonnull String type, int createdAt,
                            boolean isRead, DisplayInfo display, AttachedObject attachedObject, @Nullable String appUrl) {
            this.id = id;
            this.type = type;
            this.createdAt = createdAt;
            this.isRead = isRead;
            this.display = display;
            this.attachedObject = attachedObject;
            this.appUrl = appUrl;
        }

        public Notification markAsRead() {
            return new Notification(id, type, createdAt, true, display, attachedObject, appUrl);
        }

        public boolean isListenNotification() {
            return NOTIFICATION_LISTEN.equals(type);
        }

        public boolean isMessageNotification() {
            return NOTIFICATION_MESSAGE.equals(type);
        }

        @Nonnull
        public String getId() {
            return id;
        }

        public long getCreatedAtInMillis() {
            return createdAt * 1000L;
        }

        public int getCreatedAt() {
            return createdAt;
        }

        public boolean isRead() {
            return isRead;
        }

        public AttachedObject getAttachedObject() {
            return attachedObject;
        }

        public DisplayInfo getDisplay() {
            return display;
        }

        @Nullable
        public String getAppUrl() {
            return appUrl;
        }

        @Override
        public String toString() {
            return "Notification{" +
                    "id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", createdAt=" + createdAt +
                    ", isRead=" + isRead +
                    ", display=" + display +
                    ", attachedObject=" + attachedObject +
                    ", appUrl='" + appUrl + '\'' +
                    '}';
        }
    }

    public class AttachedObject {
        @Nullable
        private final Message message;
        @Nullable
        private final BaseProfile profile;
        @Nullable
        private final Shout shout;

        private AttachedObject(@Nullable Message message, @Nullable BaseProfile profile, @Nullable Shout shout) {
            this.message = message;
            this.profile = profile;
            this.shout = shout;
        }

        @Nullable
        public Message getMessage() {
            return message;
        }

        @Nullable
        public BaseProfile getProfile() {
            return profile;
        }
    }

    public class Message {
        @Nonnull
        private final String id;
        private final int createdAt;
        @Nonnull
        private final String conversationId;
        @Nonnull
        private final BaseProfile profile;
        @Nullable
        private final String text;

        private Message(@Nonnull String id, int createdAt, @Nonnull String conversationId,
                        @Nonnull BaseProfile profile, @Nullable String text) {
            this.id = id;
            this.createdAt = createdAt;
            this.conversationId = conversationId;
            this.profile = profile;
            this.text = text;
        }

        @Nonnull
        public BaseProfile getProfile() {
            return profile;
        }

        @Nullable
        public String getText() {
            return text;
        }
    }

    public class DisplayInfo {
        private final String title;
        private final String text;
        private final List<Range> ranges;
        private final String image;

        private DisplayInfo(String title, String text, List<Range> ranges, String image) {
            this.title = title;
            this.text = text;
            this.ranges = ranges;
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

        public List<Range> getRanges() {
            return ranges;
        }

        public String getImage() {
            return image;
        }
    }

    public class Range {
        private final int length;
        private final int offset;

        private Range(int length, int offset) {
            this.length = length;
            this.offset = offset;
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return offset;
        }
    }
}
