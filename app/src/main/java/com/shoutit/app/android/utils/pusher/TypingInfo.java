package com.shoutit.app.android.utils.pusher;

public class TypingInfo {

    private final boolean isTyping;
    private final String username;

    private TypingInfo(boolean isTyping, String username) {
        this.isTyping = isTyping;
        this.username = username;
    }

    public static TypingInfo notTyping() {
        return new TypingInfo(false, null);
    }

    public static TypingInfo typing(String username) {
        return new TypingInfo(true, username);
    }

    public boolean isTyping() {
        return isTyping;
    }

    public String getUsername() {
        return username;
    }
}