package com.shoutit.app.android.view.createshout;

import android.support.annotation.NonNull;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.Map;

import javax.annotation.Nullable;

public class ShoutMediaPresenter {

    public abstract class Item {

        public abstract void click();

    }

    public class MediaItem extends Item {

        protected static final int VIDEO = 0;
        protected static final int IMAGE = 1;

        private final String mMedia;

        protected final int type;

        public MediaItem(int type, String media) {
            mMedia = media;
            this.type = type;
        }

        @Override
        public void click() {
            removeItem(this);
        }

        public String getMedia() {
            return mMedia;
        }

        public int getType() {
            return type;
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
            1, new AddImageItem(),
            2, new AddImageItem(),
            3, new AddImageItem(),
            4, new AddImageItem()
    ));

    private MediaListener mMediaListener;

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
        mMediaListener.setImages(mediaItems);
    }

    public void addMediaItem(@NonNull String media, boolean isVideo) {
        if (isVideo && !canAddVideo()) {
            mMediaListener.onlyOneVideoAllowedAlert();
            return;
        }

        int position = getFirstAvailablePosition();
        mediaItems.put(position, new MediaItem(isVideo ? MediaItem.VIDEO : MediaItem.IMAGE, media));
        mMediaListener.setImages(mediaItems);
    }

    private boolean canAddVideo() {
        final boolean hasVideo = Iterables.any(mediaItems.values(), new Predicate<Item>() {
            @Override
            public boolean apply(@Nullable Item input) {
                return input instanceof MediaItem &&
                        ((MediaItem) input).getType() == MediaItem.VIDEO;
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

    public interface MediaListener {

        void setImages(@NonNull Map<Integer, Item> mediaElements);

        void openSelectMediaActivity();

        void onlyOneVideoAllowedAlert();
    }
}
