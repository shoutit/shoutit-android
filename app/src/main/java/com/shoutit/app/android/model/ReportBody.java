package com.shoutit.app.android.model;

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
        private final Profile profile;
        private final Shout shout;

        AttachedObject(Profile profile, Shout shout) {
            this.profile = profile;
            this.shout = shout;
        }
    }

    static class Profile {
        private final String id;

        Profile(String id) {
            this.id = id;
        }
    }

    static class Shout {
        private final String id;

        Shout(String id) {
            this.id = id;
        }
    }
}
