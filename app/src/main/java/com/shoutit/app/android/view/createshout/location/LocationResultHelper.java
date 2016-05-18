package com.shoutit.app.android.view.createshout.location;

import android.content.Intent;

import com.shoutit.app.android.api.model.UserLocation;

public class LocationResultHelper {

    public static UserLocation getLocationFromIntent(Intent data) {
        return (UserLocation) data.getSerializableExtra(LocationActivity.EXTRAS_USER_LOCATION);
    }

}
