package com.shoutit.app.android.utils;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmazonRequestTransfomer implements Picasso.RequestTransformer {

    private static final String IMAGE_URL = "image.static.shoutit.com";

    private static final String SMALL = "small";
    private static final String MEDIUM = "medium";
    public static final String LARGE = "large";

    private static final int SMALL_SIZE = 240;
    private static final int MEDIUM_SIZE = 480;

    @Override
    public Request transformRequest(Request request) {
        final Uri uri = request.uri;
        if (uri != null && uri.getHost() != null && uri.getHost().contains(IMAGE_URL)) {
            final String variation = getVariation(request.targetWidth, request.targetHeight);

            final String newPath = transformUrl(uri.getPath(), uri.getLastPathSegment(), variation);

            return request.buildUpon()
                    .setUri(request.uri
                            .buildUpon()
                            .path(newPath)
                            .build())
                    .build();
        } else {
            return request;
        }
    }

    @NonNull
    private String getVariation(int targetWidth, int targetHeight) {
        final int maxDimen = Math.max(targetWidth, targetHeight);
        final String variation;
        if (maxDimen < SMALL_SIZE) {
            variation = SMALL;
        } else if (maxDimen < MEDIUM_SIZE) {
            variation = MEDIUM;
        } else {
            variation = LARGE;
        }
        return variation;
    }

    @Nonnull
    static String transformUrl(@Nonnull String path, @Nonnull String lastPathSegment, @Nonnull String variation) {
        final String[] split = lastPathSegment.split("\\.");
        if (split.length > 1) {
            final String newLastPath = String.format("%1$s_%2$s.%3$s", split[0], variation, split[1]);
            return path.replace(lastPathSegment, newLastPath);
        } else {
            return String.format("%1$s_%2$s", lastPathSegment, variation);
        }
    }

    @Nullable
    public static String transformUrl(@Nullable String url, @Nonnull String variation) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        final Uri uri = Uri.parse(url);
        final String newPath = transformUrl(uri.getPath(), uri.getLastPathSegment(), variation);
        return uri.buildUpon()
                .path(newPath)
                .build()
                .toString();
    }
}
