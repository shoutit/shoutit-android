package com.shoutit.app.android.view.createshout;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.media.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class ShoutMediaPresenter {

    public abstract class Item {

        public abstract void click();

    }

    public class BlankItem extends Item {

        @Override
        public void click() {

        }
    }

    public abstract class MediaItem extends Item {

        private final String mThumb;

        public MediaItem(@Nullable String media) {
            mThumb = media;
        }

        @Override
        public void click() {
            removeItem(this);
        }

        public String getThumb() {
            return mThumb;
        }
    }

    public class ImageItem extends MediaItem {

        public ImageItem(@NonNull String media) {
            super(media);
        }
    }

    public class RemoteImageItem extends ImageItem {

        public RemoteImageItem(@NonNull String media) {
            super(media);
        }
    }

    public class VideoItem extends MediaItem {

        private final String video;

        public VideoItem(@Nullable String media, @NonNull String video) {
            super(media);
            this.video = video;
        }

        public String getVideo() {
            return video;
        }
    }

    public class RemoteVideoItem extends VideoItem {

        public RemoteVideoItem(@Nullable String media, @NonNull String video) {
            super(media, video);
        }
    }

    public class AddImageItem extends Item {

        @Override
        public void click() {
            mMediaListener.openSelectMediaActivity();
        }
    }

    private final BiMap<Integer, Item> mediaItems = HashBiMap.create(ImmutableMap.<Integer, Item>of(
            0, new AddImageItem(),
            1, new BlankItem(),
            2, new BlankItem(),
            3, new BlankItem(),
            4, new BlankItem()
    ));

    private MediaListener mMediaListener;

    private final Context context;

    @Inject
    public ShoutMediaPresenter(@ForActivity Context context) {
        this.context = context;
    }

    private void removeItem(@NonNull Item imageItem) {
        final int firstAvailablePosition = getFirstAvailablePosition();
        final Integer position = mediaItems.inverse().get(imageItem);

        if (firstAvailablePosition - 1 == position) {
            mediaItems.forcePut(position, new AddImageItem());
        } else {
            for (int i = position + 1; i < firstAvailablePosition; i++) {
                final Item item = mediaItems.get(i);
                mediaItems.forcePut(i - 1, item);
            }
            mediaItems.forcePut(mediaItems.size() - 1, new AddImageItem());
        }

        if (position + 1 < mediaItems.values().size()) {
            mediaItems.put(position + 1, new BlankItem());
        }

        mMediaListener.setImages(mediaItems);
    }

    public void addRemoteMedia(List<String> images, List<Video> videos) {
        for (String image : images) {
            addImageItem(image, true);
        }

        for (Video video : videos) {
            addRemoteVideoItem(video.getUrl(), video.getThumbnailUrl());
        }

        mMediaListener.setImages(mediaItems);
    }

    public void addMediaItem(@NonNull String media, boolean isVideo) {
        if (isVideo) {
            addLocalVideoItem(media);
        } else {
            addImageItem(media, false);
        }
    }

    private void addLocalVideoItem(@NonNull String media) {
        if (!canAddVideo()) {
            mMediaListener.onlyOneVideoAllowedAlert();
            return;
        }

        final int position = getFirstAvailablePosition();
        File videoThumbnail = null;
        try {
            videoThumbnail = MediaUtils.createVideoThumbnail(context, Uri.parse(media));
        } catch (IOException e) {
            mMediaListener.thumbnailCreateError();
        }
        mediaItems.put(position, new VideoItem(
                videoThumbnail != null ? String.format("file://%1$s", videoThumbnail.getAbsolutePath()) : null,
                media));

        if (position + 1 < mediaItems.values().size()) {
            mediaItems.put(position + 1, new AddImageItem());
        }

        mMediaListener.setImages(mediaItems);
    }

    private void addRemoteVideoItem(@NonNull String media, @NonNull String thumbnail) {
        final int position = getFirstAvailablePosition();
        mediaItems.put(position, new RemoteVideoItem(thumbnail, media));

        if (position + 1 < mediaItems.values().size()) {
            mediaItems.put(position + 1, new AddImageItem());
        }
    }

    private void addImageItem(@NonNull String media, boolean isRemote) {
        final int position = getFirstAvailablePosition();

        if (position + 1 < mediaItems.values().size()) {
            mediaItems.put(position + 1, new AddImageItem());
        }

        mediaItems.put(position, isRemote ? new RemoteImageItem(media) : new ImageItem(String.format("file://%1$s", media)));

        if (!isRemote) {
            mMediaListener.setImages(mediaItems);
        }
    }

    private boolean canAddVideo() {
        final boolean hasVideo = Iterables.any(mediaItems.values(), new Predicate<Item>() {
            @Override
            public boolean apply(@Nullable Item input) {
                return input instanceof VideoItem;
            }
        });
        return !hasVideo;
    }

    private int getFirstAvailablePosition() {
        for (int i = 0; i < mediaItems.size(); i++) {
            if (mediaItems.get(i) instanceof AddImageItem) {
                return i;
            }
        }

        throw new IllegalStateException("cannot add image when list is full");
    }

    public void register(@NonNull MediaListener mediaListener) {
        mMediaListener = mediaListener;
        mediaListener.setImages(mediaItems);
    }

    public void unregister() {
        mMediaListener = null;
    }

    public interface MediaListener {

        void setImages(@NonNull Map<Integer, Item> mediaElements);

        void openSelectMediaActivity();

        void onlyOneVideoAllowedAlert();

        void thumbnailCreateError();
    }
}
