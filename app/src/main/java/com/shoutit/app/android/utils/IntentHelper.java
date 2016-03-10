package com.shoutit.app.android.utils;

import android.content.Intent;
import android.net.Uri;

import javax.annotation.Nonnull;

public class IntentHelper {

    public static Intent websiteIntent(@Nonnull String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }
}
