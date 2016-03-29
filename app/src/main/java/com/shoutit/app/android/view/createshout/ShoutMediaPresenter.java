package com.shoutit.app.android.view.createshout;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.appunite.rx.functions.BothParams;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.view.media.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func2;
import rx.functions.FuncN;

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

    public class RemoteImageItem extends MediaItem {

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

    public class RemoteVideoItem extends MediaItem {

        private final String video;

        public RemoteVideoItem(@Nullable String media, @NonNull String video) {
            super(media);
            this.video = video;
        }

        public String getVideo() {
            return video;
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
    private final ApiService mApiService;
    private final AmazonHelper mAmazonHelper;

    @Inject
    public ShoutMediaPresenter(@ForActivity Context context, ApiService apiService, AmazonHelper amazonHelper) {
        this.context = context;
        mApiService = apiService;
        mAmazonHelper = amazonHelper;
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

    public void send() {
        final List<Observable<String>> imageObservables = Lists.newArrayList();
        Observable<BothParams<String, String>> videoObservable = null;

        for (Item item : mediaItems.values()) {
            if (item instanceof VideoItem) {
                VideoItem videoItem = (VideoItem) item;
                final Observable<String> videoFileObservable = mAmazonHelper.uploadShoutMediaObservable(new File(videoItem.getVideo()));
                final Observable<String> thumbFileObservable = mAmazonHelper.uploadShoutMediaObservable(new File(videoItem.getThumb()));
                videoObservable = Observable.zip(videoFileObservable, thumbFileObservable, new Func2<String, String, BothParams<String, String>>() {
                    @Override
                    public BothParams<String, String> call(String video, String thumb) {
                        return BothParams.of(video, thumb);
                    }
                });
            } else if (item instanceof ImageItem) {
                final ImageItem imageItem = (ImageItem) item;
                imageObservables.add(mAmazonHelper.uploadUserImageObservable(new File(imageItem.getThumb())));
            }
        }

        final Observable<List<String>> images = Observable.zip(imageObservables, new FuncN<List<String>>() {
            @Override
            public List<String> call(Object... args) {
                return null;
            }
        });

        if (!imageObservables.isEmpty()) {
            if (videoObservable == null) {
                images.subscribe();
            } else {
                images.zipWith(videoObservable, new Func2<List<String>, BothParams<String, String>, Object>() {
                    @Override
                    public Object call(List<String> strings, BothParams<String, String> stringStringBothParams) {
                        return null;
                    }
                }).subscribe();
            }
        } else {
            if (videoObservable != null) {
                videoObservable.subscribe()
            }
        }

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
