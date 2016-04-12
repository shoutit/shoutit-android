package com.shoutit.app.android.view.shout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.IntentHelper;
import com.shoutit.app.android.view.gallery.GalleryActivity;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.Constants;
import com.veinhorn.scrollgalleryview.VideoPlayerActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class ShoutImagesPagerAdapter extends PagerAdapter {

    private final Context context;
    @Nonnull
    private final Picasso picasso;
    @Nonnull
    private final Gson gson;
    @Nonnull
    private final LayoutInflater inflater;
    @Nonnull
    private List<String> images = ImmutableList.of();
    @Nonnull
    private List<Video> videos = ImmutableList.of();

    @Inject
    public ShoutImagesPagerAdapter(@ForActivity Context context, @Nonnull Picasso picasso,
                                   @Nonnull Gson gson) {
        this.context = context;
        this.picasso = picasso;
        this.gson = gson;
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(@Nonnull List<String> images, @Nonnull List<Video> videos) {
        this.images = images;
        this.videos = videos;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View view = inflater.inflate(R.layout.shout_pager_image, container, false);
        final ImageView imageView = (ImageView) view.findViewById(R.id.shout_image_iv);
        final ImageView videoIconView = (ImageView) view.findViewById(R.id.shout_video_icon_iv);

        String imageUrl = null;

        if (isImageItem(position)) {
            imageUrl = images.get(position);
            videoIconView.setVisibility(View.GONE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGallery(position);
                }
            });

        } else if (!videos.isEmpty()) {
            final int videoPosition = position - images.size();
            final Video video = videos.get(videoPosition);
            imageUrl = video.getThumbnailUrl();
            videoIconView.setVisibility(View.VISIBLE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo(video);
                }
            });
        }

        loadThumbnail(imageUrl, imageView);

        container.addView(view);

        return view;
    }

    private void playVideo(@Nonnull Video video) {
        final Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(Constants.URL, video.getUrl());
        context.startActivity(intent);
    }

    private void openGallery(int initPosition) {
        final String imagesJson = gson.toJson(images);
        final String videosJson = gson.toJson(videos);
        context.startActivity(GalleryActivity.newIntent(context, imagesJson, videosJson, initPosition));
    }

    private boolean isImageItem(int position) {
        return !images.isEmpty() && position < images.size();
    }

    public void loadThumbnail(@Nullable String thumbnailUrl, @Nonnull ImageView imageView) {
        picasso.load(thumbnailUrl)
                .placeholder(R.drawable.pattern_placeholder)
                .error(R.drawable.pattern_placeholder)
                .fit()
                .centerCrop()
                .into(imageView);
    }

    @Override
    public int getCount() {
        return images.size() + videos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}
