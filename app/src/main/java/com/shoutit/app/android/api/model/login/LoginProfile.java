package com.shoutit.app.android.api.model.login;

import android.support.annotation.Nullable;

import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserLocationSimple;

public class LoginProfile {

    private final UserLocationSimple location;

    public LoginProfile(double latitude, double longitude) {
        this.location = new UserLocationSimple(latitude, longitude);
    }

    @Nullable
    public static LoginProfile loginUser(@Nullable UserLocation location) {
        if (location == null) {
            return null;
        } else {
            return new LoginProfile(location.getLatitude(), location.getLongitude());
        }
    }

    public UserLocationSimple getLocation() {
        return location;
    }
}
