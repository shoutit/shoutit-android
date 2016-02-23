package com.shoutit.app.android.utils;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Optional;
import com.shoutit.app.android.api.model.Shout;

import javax.annotation.Nonnull;

public class ResourcesHelper {

    public static int getResourceIdForName(@Nonnull String resourceName, @Nonnull Context context) {
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }

    @IdRes
    @NonNull
    public static Optional<Integer> getCountryResId(@Nonnull Context context, @Nonnull Shout shout) {
        if (shout.getLocation() != null && !TextUtils.isEmpty(shout.getLocation().getCountry())) {
            final String countryCode = shout.getLocation().getCountry().toLowerCase();
            return Optional.of(ResourcesHelper.getResourceIdForName(countryCode, context));
        } else {
            return Optional.absent();
        }
    }
}
