package com.shoutit.app.android.utils;

import javax.annotation.Nonnull;

public enum AmazonImage {

    ORIGINAL(""),
    SMALL("_small"),
    MEDIUM("_medium"),
    LARGE("_large"),;

    private static final String BASE_IMAGE_URL_INFIX = "-image.static.shoutit.com";
    public final String mQualityExtension;

    AmazonImage(String extension) {
        this.mQualityExtension = extension;
    }

    public String getShoutImageUri(@Nonnull String originalImageUrl) {
        if (!originalImageUrl.contains(BASE_IMAGE_URL_INFIX)) {
            return originalImageUrl;
        }

        int extensionIndex = originalImageUrl.lastIndexOf(AmazonHelper.JPEG);

        if (extensionIndex == -1) {
            return originalImageUrl;
        } else {
            return String.format("%1$s%2$s%3$s", originalImageUrl.substring(0, extensionIndex),
                    mQualityExtension, originalImageUrl.substring(extensionIndex));
        }
    }
}
