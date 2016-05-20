package com.shoutit.app.android.api.model;

public class ProfileRequest {

    private final Profile profile;

    public ProfileRequest(String id) {
        this.profile = new Profile(id);
    }

    private static class Profile {

        private final String id;

        public Profile(String id) {
            this.id = id;
        }
    }

}
