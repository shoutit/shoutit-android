package com.shoutit.app.android.model;


public class Stats {

    private final Integer unreadConversationsCount;
    private final Integer unreadNotificationsCount;

    public Stats(Integer unreadConversationsCount, Integer unreadNotificationsCount) {
        this.unreadConversationsCount = unreadConversationsCount;
        this.unreadNotificationsCount = unreadNotificationsCount;
    }

    public int getUnreadConversationsCount() {
        if (unreadConversationsCount == null) {
            return 0;
        } else {
            return unreadConversationsCount;
        }
    }

    public int getUnreadNotifications() {
        if (unreadNotificationsCount == null) {
            return 0;
        } else {
            return unreadNotificationsCount;
        }
    }
}
