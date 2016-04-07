package com.shoutit.app.android.utils;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import javax.annotation.Nonnull;

public class IntentHelper {

    public static Intent websiteIntent(@Nonnull String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

    public static Intent videoIntent(@Nonnull String videoUrl) {
        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse(videoUrl), "video/mp4");
    }
}
