package com.shoutit.app.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;

public class PicassoHelper {

    public static Target getRoundedBitmapTarget(@Nonnull final Context context,
                                                @Nonnull final ImageView imageView) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                final RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory
                        .create(context.getResources(), bitmap);
                roundedBitmap.setCircular(true);
                imageView.setImageDrawable(roundedBitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }
}
