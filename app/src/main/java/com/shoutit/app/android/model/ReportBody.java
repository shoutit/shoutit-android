package com.shoutit.app.android.model;

import android.support.annotation.Nullable;

import javax.annotation.Nonnull;

public class ReportBody {
    private final String text;
    private final AttachedObject attachedObject;

    public ReportBody(String text, AttachedObject attachedObject) {
        this.text = text;
        this.attachedObject = attachedObject;
    }

    public static ReportBody forShout(String id, String body) {
        return new ReportBody(body, new AttachedObject(null, new NestedObject(id), null));
    }

    public static ReportBody forProfile(String id, String body) {
        return new ReportBody(body, new AttachedObject(new NestedObject(id), null, null));
    }

    public static ReportBody forConversation(String id, String body) {
        return new ReportBody(body, new AttachedObject(null, null, new NestedObject(id)));
    }

    static class AttachedObject {
        @Nullable
        private final NestedObject profile;
        @Nullable
        private final NestedObject shout;
        @Nullable
        private final NestedObject conversation;

        AttachedObject(@Nullable NestedObject profile, @Nullable NestedObject shout, @Nullable NestedObject conversation) {
            this.profile = profile;
            this.shout = shout;
            this.conversation = conversation;
        }
    }

    static class NestedObject {
        @Nonnull
        private final String id;

        NestedObject(@Nonnull String id) {
            this.id = id;
        }
    }
}
