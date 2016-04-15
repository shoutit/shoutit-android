package com.shoutit.app.android.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

import com.amazonaws.services.s3.util.Mimetypes;

import javax.annotation.Nonnull;

public class IntentHelper {

    public static Intent websiteIntent(@Nonnull String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

    public static Intent getShareIntent(@Nonnull String shareUrl) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareUrl);

        return intent;
    }

    public static Intent videoIntent(@Nonnull String videoUrl) {
        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse(videoUrl), "video/*");
    }

    public static Intent getAppSettingsIntent(Context context) {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + context.getPackageName()));
    }
}
