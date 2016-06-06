package com.shoutit.app.android.view.createshout;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.appunite.rx.functions.BothParams;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.view.media.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.functions.FuncN;
import rx.subscriptions.CompositeSubscription;

public class ShoutMediaPresenter {

    private static final String TAG = ShoutMediaPresenter.class.getSimpleName();

    private boolean mIsOffer;

    public interface Item {

        void click();

    }

    public interface IVideoItem {

        String getVideo();

        int getDuration();

    }

    public interface IImageItem {

        String getThumb();

    }

    public interface LocalOrRemote {

        boolean isRemote();

    }

    public class BlankItem implements Item {

        @Override
        public void click() {

        }
    }

    public abstract class MediaItem implements Item, IImageItem, LocalOrRemote {

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

        @Override
        public boolean isRemote() {
            return false;
        }
    }

    public class RemoteImageItem extends MediaItem {

        public RemoteImageItem(@NonNull String media) {
            super(media);
        }

        @Override
        public boolean isRemote() {
            return true;
        }
    }

    public abstract class VideoItem extends MediaItem implements IVideoItem {

        private final String video;
        private final int duration;

        public VideoItem(@Nullable String media, @NonNull String video, int duration) {
            super(media);
            this.video = video;
            this.duration = duration;
        }

        public String getVideo() {
            return video;
        }

        public int getDuration() {
            return duration;
        }
    }

    public class LocalVideoItem extends VideoItem {

        public LocalVideoItem(@Nullable String media, @NonNull String video, int duration) {
            super(media, video, duration);
        }

        @Override
        public boolean isRemote() {
            return false;
        }
    }

    public class RemoteVideoItem extends VideoItem {

        public RemoteVideoItem(@Nullable String media, @NonNull String video, int duration) {
            super(media, video, duration);
        }

        @Override
        public boolean isRemote() {
            return true;
        }
    }

    public class AddImageItem implements Item {

        @Override
        public void click() {
            mMediaListener.openSelectMediaActivity(getFirstAvailablePositionAndCheck() == 0, mIsOffer);
        }
    }

    private final BiMap<Integer, Item> mediaItems = HashBiMap.create(ImmutableMap.of(
            0, new AddImageItem(),
            1, new BlankItem(),
            2, new BlankItem(),
            3, new BlankItem(),
            4, new BlankItem()
    ));

    private MediaListener mMediaListener;

    private final Context context;
    private final AmazonHelper mAmazonHelper;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Inject
    public ShoutMediaPresenter(@ForActivity Context context, AmazonHelper amazonHelper) {
        this.context = context;
        mAmazonHelper = amazonHelper;
    }

    private void removeItem(@NonNull Item imageItem) {
        final Integer firstAvailablePosition = MoreObjects.firstNonNull(getFirstAvailablePosition(), mediaItems.values().size());
        final Integer position = mediaItems.inverse().get(imageItem);

        for (int i = position + 1; i < firstAvailablePosition; i++) {
            final Item item = mediaItems.get(i);
            mediaItems.forcePut(i - 1, item);
        }
        mediaItems.forcePut(firstAvailablePosition - 1, new AddImageItem());

        for (int j = firstAvailablePosition; j < mediaItems.size(); j++) {
            mediaItems.put(j, new BlankItem());
        }
        mMediaListener.setImages(mediaItems);
    }

    public void setUp(List<String> images, List<Video> videos, boolean isOffer) {
        clear();

        mIsOffer = isOffer;
        for (String image : images) {
            addImageItem(image, true);
        }

        for (Video video : videos) {
            addRemoteVideoItem(video.getUrl(), video.getThumbnailUrl(), video.getDuration());
        }

        mMediaListener.setImages(mediaItems);
    }

    private void clear() {
        mediaItems.forcePut(0, new AddImageItem());
        mediaItems.forcePut(1, new BlankItem());
        mediaItems.forcePut(2, new BlankItem());
        mediaItems.forcePut(3, new BlankItem());
        mediaItems.forcePut(4, new BlankItem());
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

        final Integer position = getFirstAvailablePositionAndCheck();
        File videoThumbnail = null;
        try {
            videoThumbnail = MediaUtils.createVideoThumbnail(context, Uri.parse(media));
        } catch (IOException e) {
            mMediaListener.thumbnailCreateError();
        }

        final int videoLength = MediaUtils.getVideoLength(context, media);
        mediaItems.put(position, new LocalVideoItem(
                videoThumbnail != null ? String.format("file://%1$s", videoThumbnail.getAbsolutePath()) : null,
                media,
                videoLength));

        if (position + 1 < mediaItems.values().size()) {
            mediaItems.put(position + 1, new AddImageItem());
        }

        mMediaListener.setImages(mediaItems);
    }

    private void addRemoteVideoItem(@NonNull String media, @NonNull String thumbnail, int duration) {
        final int position = getFirstAvailablePositionAndCheck();
        mediaItems.put(position, new RemoteVideoItem(thumbnail, media, duration));

        if (position + 1 < mediaItems.values().size()) {
            mediaItems.put(position + 1, new AddImageItem());
        }
    }

    private void addImageItem(@NonNull String media, boolean isRemote) {
        final int position = getFirstAvailablePositionAndCheck();

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
                return input instanceof LocalVideoItem || input instanceof RemoteVideoItem;
            }
        });
        return !hasVideo;
    }

    private Integer getFirstAvailablePosition() {
        for (int i = 0; i < mediaItems.size(); i++) {
            if (mediaItems.get(i) instanceof AddImageItem) {
                return i;
            }
        }
        return null;
    }

    private int getFirstAvailablePositionAndCheck() {
        final Integer firstAvailablePosition = getFirstAvailablePosition();
        Preconditions.checkNotNull(firstAvailablePosition);
        return firstAvailablePosition;
    }

    public void send() {
        mMediaListener.showMediaProgress();

        final List<Observable<String>> imageObservables = Lists.newArrayList();
        Observable<Video> videoObservable = null;

        for (Item item : mediaItems.values()) {
            if (item instanceof LocalVideoItem) {
                final LocalVideoItem localVideoItem = (LocalVideoItem) item;
                final Observable<String> videoFileObservable = mAmazonHelper.uploadShoutMediaVideoObservable(AmazonHelper.getfileFromPath(localVideoItem.getVideo()));
                final Observable<String> thumbFileObservable = mAmazonHelper.uploadShoutMediaImageObservable(AmazonHelper.getfileFromPath(localVideoItem.getThumb()));
                videoObservable = Observable.zip(videoFileObservable, thumbFileObservable, new Func2<String, String, Video>() {
                    @Override
                    public Video call(String video, String thumb) {
                        return Video.createVideo(video, thumb, localVideoItem.getDuration());
                    }
                });
            } else if (item instanceof ImageItem) {
                final ImageItem imageItem = (ImageItem) item;
                imageObservables.add(mAmazonHelper.uploadShoutMediaImageObservable(AmazonHelper.getfileFromPath(imageItem.getThumb())));
            }
        }

        mergeVideoAndImagesObservable(imageObservables, videoObservable);
    }

    private void mergeVideoAndImagesObservable(List<Observable<String>> imageObservables, Observable<Video> videoObservable) {
        if (imageObservables.isEmpty()) {
            if (videoObservable != null) {
                mCompositeSubscription.add(videoObservable.subscribe(video -> {
                    getAllEditedImagesAndComplete(ImmutableList.<String>of(), ImmutableList.of(video));
                }, throwable -> {
                    mMediaListener.showUploadError(throwable);
                }));
            } else {
                getAllEditedImagesAndComplete(ImmutableList.<String>of(), ImmutableList.<Video>of());
            }
        } else {
            final Observable<List<String>> images = Observable.zip(imageObservables, new FuncN<List<String>>() {

                @Override
                public List<String> call(Object... args) {
                    final List<String> images = Lists.newArrayList();
                    for (Object url : args) {
                        images.add((String) url);
                    }
                    return images;
                }
            });

            if (videoObservable == null) {
                mCompositeSubscription.add(images.subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> images) {
                        getAllEditedImagesAndComplete(images, ImmutableList.<Video>of());
                    }
                }));
            } else {
                mCompositeSubscription.add(images.zipWith(
                        videoObservable, (images1, video) -> {
                            return BothParams.of(images1, video);
                        })
                        .subscribe(listBothParamsBothParams -> {
                            getAllEditedImagesAndComplete(listBothParamsBothParams.param1(), ImmutableList.of(listBothParamsBothParams.param2()));
                        }, throwable -> {
                            mMediaListener.showUploadError(throwable);
                            LogHelper.logThrowableAndCrashlytics(TAG, "Media upload failed", throwable);
                        }));
            }
        }
    }

    private void getAllEditedImagesAndComplete(List<String> images, List<Video> videos) {
        final List<String> allImages = getAllEditedImages(images);
        final List<Video> allVideos = getAllEditedVideos(videos);
        mMediaListener.mediaEditionCompleted(allImages, allVideos);
    }

    private List<String> getAllEditedImages(List<String> localImages) {
        final List<String> remoteImages = Lists.newArrayList();
        for (Item item : mediaItems.values()) {
            if (item instanceof RemoteImageItem) {
                remoteImages.add(((RemoteImageItem) item).getThumb());
            }
        }

        return ImmutableList.copyOf(Iterables.concat(localImages, remoteImages));
    }

    private List<Video> getAllEditedVideos(List<Video> localVideos) {
        final List<Video> remoteVideos = Lists.newArrayList();
        for (Item item : mediaItems.values()) {
            if (item instanceof RemoteVideoItem) {
                final RemoteVideoItem remoteVideoItem = (RemoteVideoItem) item;
                remoteVideos.add(Video.createVideo(remoteVideoItem.getVideo(), remoteVideoItem.getThumb(), remoteVideoItem.getDuration()));
            }
        }
        return ImmutableList.copyOf(Iterables.concat(localVideos, remoteVideos));
    }

    public void register(@NonNull MediaListener mediaListener) {
        mMediaListener = mediaListener;
        mediaListener.setImages(mediaItems);
    }

    public void unregister() {
        mMediaListener = null;
        mCompositeSubscription.unsubscribe();
    }

    public interface MediaListener {

        void setImages(@NonNull Map<Integer, Item> mediaElements);

        void openSelectMediaActivity(boolean isFirst, boolean isOffer);

        void onlyOneVideoAllowedAlert();

        void thumbnailCreateError();

        void mediaEditionCompleted(@NonNull List<String> images, @NonNull List<Video> videos);

        void showMediaProgress();

        void showUploadError(Throwable throwable);
    }
}
