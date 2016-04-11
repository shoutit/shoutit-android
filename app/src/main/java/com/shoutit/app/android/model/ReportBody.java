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
        return new ReportBody(body, new AttachedObject(null, new Shout(id)));
    }

    public static ReportBody forProfile(String id, String body) {
        return new ReportBody(body, new AttachedObject(new Profile(id), null));
    }

    static class AttachedObject {
        @Nullable
        private final Profile profile;
        @Nullable
        private final Shout shout;

        AttachedObject(@Nullable Profile profile, @Nullable Shout shout) {
            this.profile = profile;
            this.shout = shout;
        }
    }

    static class Profile {
        @Nonnull
        private final String id;

        Profile(@Nonnull String id) {
            this.id = id;
        }
    }

    static class Shout {
        @Nonnull
        private final String id;

        Shout(@Nonnull String id) {
            this.id = id;
        }
    }
}
