package com.shoutit.app.android.view.gallery;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.AmazonRequestTransfomer;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.Constants;
import com.veinhorn.scrollgalleryview.VideoPlayerActivity;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import javax.annotation.Nonnull;

public class MediaLoaders {

    public static class ImagesLoader implements MediaLoader {

        private final String imageUrl;
        private final Picasso picasso;
        private final Picasso picassoWithoutAmazonTransformer;
        private final Resources resources;

        public ImagesLoader(String imageUrl,
                            Picasso picasso,
                            Picasso picassoWithoutAmazonTransformer,
                            Resources resources) {
            this.imageUrl = imageUrl;
            this.picasso = picasso;
            this.picassoWithoutAmazonTransformer = picassoWithoutAmazonTransformer;
            this.resources = resources;
        }

        @Override
        public boolean isImage() {
            return true;
        }

        @Override
        public void loadMedia(Context context, ImageView imageView, final SuccessCallback callback) {
            picassoWithoutAmazonTransformer.load(AmazonRequestTransfomer.transformUrl(imageUrl, AmazonRequestTransfomer.LARGE))
                    .placeholder(new BitmapDrawable(resources, Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)))
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess();
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }

        @Override
        public void loadThumbnail(Context context, ImageView thumbnailView, final SuccessCallback callback) {
            picasso.load(imageUrl)
                    .placeholder(new BitmapDrawable(resources, Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)))
                    .resizeDimen(R.dimen.gallery_thumbnail_size, R.dimen.gallery_thumbnail_size)
                    .into(thumbnailView, new Callback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess();
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }

        @Override
        public String getItemId() {
            return imageUrl;
        }
    }

    public static class VideosLoader implements MediaLoader {

        @Nonnull
        private final Picasso picasso;
        @Nonnull
        private final String videoUrl;
        @Nonnull
        private final String thumbnailUrl;

        public VideosLoader(@Nonnull Picasso picasso, @Nonnull String videoUrl, @Nonnull String thumbnailUrl) {
            this.picasso = picasso;
            this.videoUrl = videoUrl;
            this.thumbnailUrl = thumbnailUrl;
        }

        @Override
        public boolean isImage() {
            return false;
        }

        @Override
        public void loadMedia(final Context context, ImageView imageView, SuccessCallback callback) {
            imageView.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayVideo(context, videoUrl);
                }
            });
        }

        @Override
        public void loadThumbnail(Context context, ImageView thumbnailView, final SuccessCallback callback) {
            picasso.load(thumbnailUrl)
                    .resizeDimen(R.dimen.gallery_thumbnail_size, R.dimen.gallery_thumbnail_size)
                    .into(thumbnailView, new Callback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess();
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }

        @Override
        public String getItemId() {
            return videoUrl;
        }

        private void displayVideo(Context context, String url) {
            final Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra(Constants.URL, url);
            context.startActivity(intent);
        }
    }

}