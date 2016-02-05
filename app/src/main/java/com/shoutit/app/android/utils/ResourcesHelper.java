package com.shoutit.app.android.utils;

import android.content.Context;

import javax.annotation.Nonnull;

public class ResourcesHelper {

    public static int getResourceIdForName(@Nonnull String resourceName, @Nonnull Context context) {
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }
}
