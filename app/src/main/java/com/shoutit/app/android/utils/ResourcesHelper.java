package com.shoutit.app.android.utils;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Optional;
import com.shoutit.app.android.api.model.UserLocation;

import javax.annotation.Nonnull;

public class ResourcesHelper {

    public static int getResourceIdForName(@Nonnull String resourceName, @Nonnull Context context) {
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }

    @IdRes
    @NonNull
    public static Optional<Integer> getCountryResId(@Nonnull Context context, @Nonnull UserLocation location) {
        if (location != null && !TextUtils.isEmpty(location.getCountry())) {
            final String countryCode = location.getCountry().toLowerCase();
            return Optional.of(ResourcesHelper.getResourceIdForName(countryCode, context));
        } else {
            return Optional.absent();
        }
    }
}
