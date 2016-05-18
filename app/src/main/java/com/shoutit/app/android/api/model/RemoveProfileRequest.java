package com.shoutit.app.android.api.model;

public class RemoveProfileRequest {

    private final Profile profile;

    public RemoveProfileRequest(String id) {
        this.profile = new Profile(id);
    }

    private static class Profile {

        private final String id;

        public Profile(String id) {
            this.id = id;
        }
    }

}
