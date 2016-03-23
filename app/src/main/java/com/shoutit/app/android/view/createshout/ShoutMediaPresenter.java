package com.shoutit.app.android.view.createshout;

import android.support.annotation.NonNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ShoutMediaPresenter {

    public abstract class Item {

        public abstract void click();

    }

    public class ImageItem extends Item {

        protected static final int VIDEO = 0;
        protected static final int IMAGE = 1;

        private final String mMedia;

        protected final int type;

        public ImageItem(int type, String media) {
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
    }

    public class AddImageItem extends Item {

        @Override
        public void click() {
            mMediaListener.openSelectMediaActivity();
        }
    }

    private final BiMap<Integer, Item> images = HashBiMap.create(ImmutableMap.<Integer, Item>of(
            0, new AddImageItem(),
            1, new AddImageItem(),
            2, new AddImageItem(),
            3, new AddImageItem(),
            4, new AddImageItem()
    ));

    private MediaListener mMediaListener;

    private void removeItem(@NonNull Item imageItem) {
        final int firstAvailablePosition = getFirstAvailablePosition();
        final Integer position = images.inverse().get(imageItem);

        if (firstAvailablePosition - 1 == position) {
            images.forcePut(position, new AddImageItem());
        } else {
            for (int i = position + 1; i < firstAvailablePosition; i++) {
                final Item item = images.get(i);
                images.forcePut(i - 1, item);
            }
            images.forcePut(images.size() - 1, new AddImageItem());
        }
        mMediaListener.setImages(images);
    }

    public void addMediaItem(@NonNull String media) {
        int position = getFirstAvailablePosition();
        images.put(position, new ImageItem(media));
        mMediaListener.setImages(images);
    }

    private int getFirstAvailablePosition() {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i) instanceof AddImageItem) {
                return i;
            }
        }

        throw new IllegalStateException("cannot add image when list is full");
    }

    public void register(@NonNull MediaListener mediaListener) {
        mMediaListener = mediaListener;
        mediaListener.setImages(images);
    }

    public interface MediaListener {

        void setImages(@NonNull Map<Integer, Item> maps);

        void openSelectMediaActivity();
    }
}
