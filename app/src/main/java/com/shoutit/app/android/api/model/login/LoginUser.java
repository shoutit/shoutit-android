package com.shoutit.app.android.api.model.login;

public class LoginUser {

    private final Location location;

    public LoginUser(double latitude, double longitude) {
        this.location = new Location(latitude, longitude);
    }

    private class Location {

        private final double latitude;
        private final double longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
