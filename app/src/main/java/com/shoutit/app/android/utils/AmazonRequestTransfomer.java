package com.shoutit.app.android.utils;

import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import javax.annotation.Nonnull;

public class AmazonRequestTransfomer implements Picasso.RequestTransformer {

    private static final String IMAGE_URL = "image.static.shoutit.com";

    private static final String SMALL = "small";
    private static final String MEDIUM = "medium";
    private static final String LARGE = "large";

    private static final int SMALL_SIZE = 240;
    private static final int MEDIUM_SIZE = 480;

    @Override
    public Request transformRequest(Request request) {
        final Uri uri = request.uri;
        if (uri != null && uri.getHost().contains(IMAGE_URL)) {
            final int maxDimen = Math.max(request.targetWidth, request.targetHeight);
            final String variation;
            if (maxDimen < SMALL_SIZE) {
                variation = SMALL;
            } else if (maxDimen < MEDIUM_SIZE) {
                variation = MEDIUM;
            } else {
                variation = LARGE;
            }

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

    @Nonnull
    String transformUrl(@Nonnull String path, @Nonnull String lastPathSegment, @Nonnull String variation) {
        final String[] split = lastPathSegment.split("\\.");
        final String newLastPath = String.format("%1$s_%2$s.%3$s", split[0], variation, split[1]);
        return path.replace(lastPathSegment, newLastPath);
    }
}
