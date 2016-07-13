package com.shoutit.app.android.view.location;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class LocationHelper {

    public static UserLocation getLocationFromIntent(Intent data) {
        return (UserLocation) data.getSerializableExtra(LocationActivityForResult.EXTRAS_USER_LOCATION);
    }

    public static void setupLocation(UserLocation userLocation, Context context, TextView locationTv, ImageView flagIv, Picasso picasso) {
        locationTv.setText(context.getString(R.string.edit_profile_country,
                Strings.nullToEmpty(userLocation.getCity()),
                Strings.nullToEmpty(userLocation.getCountry())));

        final Optional<Integer> countryResId = ResourcesHelper
                .getCountryResId(context, userLocation);
        final Target flagTarget = PicassoHelper
                .getRoundedBitmapTarget(context, flagIv);
        if (countryResId.isPresent()) {
            picasso.load(countryResId.get())
                    .into(flagTarget);
        }
    }
}
