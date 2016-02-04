package com.shoutit.app.android.api.model.login;

import android.location.Location;
import android.support.annotation.Nullable;

public class LoginUser {

    private final UserLocation location;

    public LoginUser(double latitude, double longitude) {
        this.location = new UserLocation(latitude, longitude);
    }

    @Nullable
    public static LoginUser loginUser(@Nullable Location location) {
        if (location == null) {
            return null;
        } else {
            return new LoginUser(location.getLatitude(), location.getLongitude());
        }
    }

    public static class UserLocation {

        private final double latitude;
        private final double longitude;

        public UserLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public UserLocation getLocation() {
        return location;
    }
}
