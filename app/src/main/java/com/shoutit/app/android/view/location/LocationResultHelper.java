package com.shoutit.app.android.view.location;

import android.content.Intent;

import com.shoutit.app.android.api.model.UserLocation;

public class LocationResultHelper {

    public static UserLocation getLocationFromIntent(Intent data) {
        return (UserLocation) data.getSerializableExtra(LocationActivityForResult.EXTRAS_USER_LOCATION);
    }
}
