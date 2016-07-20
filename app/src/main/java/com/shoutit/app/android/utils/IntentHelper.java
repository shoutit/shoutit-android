package com.shoutit.app.android.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

import com.amazonaws.services.s3.util.Mimetypes;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.shoutit.app.android.view.gallery.GalleryActivity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class IntentHelper {

    private static final String TWITTER_PACKAGE = "com.twitter.android";

    public static Intent websiteIntent(@Nonnull String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

    public static Intent getShareIntent(@Nonnull String shareText) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);

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

    public static Intent getSmsIntent(@Nonnull String phoneNumber, @Nonnull String text) {
        final Uri uri = Uri.parse("smsto:" + phoneNumber);
        return new Intent(Intent.ACTION_SENDTO, uri)
                .putExtra("sms_body", text);
    }

    public static Intent getEmailIntent(@Nonnull String emailAddress, @Nonnull String text) {
        return new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null))
                .putExtra(Intent.EXTRA_TEXT, text);
    }

    @Nonnull
    public static List<Intent> getTwitterShareIntent(@Nonnull PackageManager packageManager,
                                                         @Nonnull String shareText) {
        final Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, shareText);

        final List<Intent> intentsList = new ArrayList<>();

        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(shareIntent, 0);
        for (ResolveInfo info : resolveInfoList) {
            final String packageNameToMatch = info.activityInfo.packageName;
            if (packageNameToMatch.contains(TWITTER_PACKAGE)) {
                final Intent intent = new Intent(shareIntent)
                        .setComponent(new ComponentName(packageNameToMatch, info.activityInfo.name))
                        .setPackage(packageNameToMatch);

                intentsList.add(intent);
            }
        }

        return intentsList;
    }

    public static Intent singleImageGalleryIntent(Context context, @Nonnull String imageUrl) {
        final String imageJson = new Gson().toJson(Lists.newArrayList(imageUrl));
        return GalleryActivity.singleImageIntent(context, imageJson);
    }
}
