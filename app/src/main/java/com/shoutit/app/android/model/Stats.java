package com.shoutit.app.android.model;

public class Stats {

    private final Integer unreadConversationsCount;
    private final Integer unreadNotificationsCount;

    public Stats(Integer unreadConversationsCount, Integer unreadNotificationsCount) {
        this.unreadConversationsCount = unreadConversationsCount;
        this.unreadNotificationsCount = unreadNotificationsCount;
    }

    public Integer getUnreadConversationsCount() {
        return unreadConversationsCount;
    }

    public Integer getUnreadNotificationsCount() {
        return unreadNotificationsCount;
    }
}
