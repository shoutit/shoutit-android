package com.shoutit.app.android.model;


import com.google.common.base.Objects;

public class Stats {

    private final Integer credit;
    private final Integer unreadConversationsCount;
    private final Integer unreadNotificationsCount;
    private final Integer totalUnreadCount;

    public Stats(Integer credit, Integer unreadConversationsCount,
                 Integer unreadNotificationsCount,
                 Integer totalUnreadCount) {
        this.credit = credit;
        this.unreadConversationsCount = unreadConversationsCount;
        this.unreadNotificationsCount = unreadNotificationsCount;
        this.totalUnreadCount = totalUnreadCount;
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

    public int getTotalUnreadCount() {
        if (totalUnreadCount == null) {
            return 0;
        } else {
            return totalUnreadCount;
        }
    }

    public int getCredits() {
        if (credit == null) {
            return 0;
        } else {
            return credit;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stats)) return false;
        final Stats stats = (Stats) o;
        return Objects.equal(unreadConversationsCount, stats.unreadConversationsCount) &&
                Objects.equal(unreadNotificationsCount, stats.unreadNotificationsCount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unreadConversationsCount, unreadNotificationsCount);
    }

    @Override
    public String toString() {
        return "Stats{" +
                "credit=" + credit +
                ", unreadConversationsCount=" + unreadConversationsCount +
                ", unreadNotificationsCount=" + unreadNotificationsCount +
                '}';
    }
}
