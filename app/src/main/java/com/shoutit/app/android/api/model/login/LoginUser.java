package com.shoutit.app.android.api.model.login;

import android.support.annotation.Nullable;

import com.shoutit.app.android.api.model.UserLocationSimple;

public class LoginUser {

    private final UserLocationSimple location;

    public LoginUser(double latitude, double longitude) {
        this.location = new UserLocationSimple(latitude, longitude);
    }

    @Nullable
    public static LoginUser loginUser(@Nullable com.shoutit.app.android.api.model.UserLocation location) {
        if (location == null) {
            return null;
        } else {
            return new LoginUser(location.getLatitude(), location.getLongitude());
        }
    }

    public UserLocationSimple getLocation() {
        return location;
    }
}
