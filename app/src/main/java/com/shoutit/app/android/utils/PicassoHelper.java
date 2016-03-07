package com.shoutit.app.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

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

    public static Target getCircularBitmapWithStrokeTarget(@Nonnull final ImageView imageView, final int strokeSize) {
        return getRoundedBitmapWithStrokeTarget(imageView, strokeSize, true, 0);
    }

    public static Target getRoundedBitmapWithStrokeTarget(@Nonnull final ImageView imageView,
                                                          final int strokeSize, final boolean isCircular, final int radius) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImageBitmap(getRoundedBitmapWithStroke(bitmap, strokeSize, isCircular, radius));
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

    public static Transformation getCircularBitmapWithStrokeTarget(final int strokeSize,
                                                                   final String transformationKey) {
        return roundedWithStrokeTransformation(strokeSize, true, 0, transformationKey);
    }

    public static Transformation roundedWithStrokeTransformation(final int strokeSize,
                                                                 final boolean isCircular,
                                                                 final int radius,
                                                                 final String transformationKey) {
        return new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                final Bitmap output = getRoundedBitmapWithStroke(source, strokeSize, isCircular, radius);
                source.recycle();
                return output;
            }

            @Override
            public String key() {
                return transformationKey;
            }
        };
    }

    public static Bitmap getRoundedBitmapWithStroke(Bitmap bitmap, int strokeSize, boolean isCircular, int radius) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (isCircular) {
            radius = Math.min(height / 2, width / 2);
        }

        final Bitmap output = Bitmap.createBitmap(width + 2 * strokeSize, height + 2 * strokeSize, Bitmap.Config.ARGB_8888);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        final Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setStyle(Paint.Style.FILL);

        final RectF imageBounds = new RectF(strokeSize, strokeSize, width + strokeSize, height + strokeSize);
        canvas.drawRoundRect(imageBounds, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, strokeSize, strokeSize, paint);
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(strokeSize);
        final RectF strokeBounds = new RectF(0, 0, width + 2 * strokeSize, height + 2 * strokeSize);
        canvas.drawRoundRect(strokeBounds, radius, radius, paint);

        return output;
    }
}
