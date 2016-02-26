package com.shoutit.app.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
                if (errorDrawable != null) {
                    imageView.setImageDrawable(errorDrawable);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                    imageView.setImageDrawable(placeHolderDrawable);
                }
            }
        };
    }

    public static Target getRoundedBitmapTarget(@Nonnull final Context context,
                                                @Nonnull final ImageView imageView,
                                                final float cornerRadius) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                final RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory
                        .create(context.getResources(), bitmap);
                roundedBitmap.setCornerRadius(cornerRadius);
                imageView.setImageDrawable(roundedBitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (errorDrawable != null) {
                    imageView.setImageDrawable(errorDrawable);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                    imageView.setImageDrawable(placeHolderDrawable);
                }
            }
        };
    }

    public static Target getRoundedBitmapWithStrokeTarget(@Nonnull final ImageView imageView,
                                                          final int strokeSize) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImageBitmap(getRoundedBitmapWithStroke(bitmap, strokeSize));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (errorDrawable != null) {
                    imageView.setImageDrawable(errorDrawable);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                    imageView.setImageDrawable(placeHolderDrawable);
                }
            }
        };
    }

    public static Bitmap getRoundedBitmapWithStroke(Bitmap bitmap, int strokeSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int radius = Math.min(height / 2, width / 2);
        final Bitmap output = Bitmap.createBitmap(width + 8, height + 8, Bitmap.Config.ARGB_8888);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        final Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle((width / 2) + strokeSize, (height / 2) + strokeSize, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, strokeSize, strokeSize, paint);
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        canvas.drawCircle((width / 2) + strokeSize, (height / 2) + strokeSize, radius, paint);

        return output;
    }
}
