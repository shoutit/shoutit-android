package com.shoutit.app.android.view.createshout;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.appunite.rx.functions.BothParams;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
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
import rx.subscriptions.CompositeSubscription;

public class ShoutMediaPresenter {

    private static final String TAG = ShoutMediaPresenter.class.getSimpleName();

    private boolean mIsOffer;

    private void swapImage(int position, String newUrl) {
        mediaItems.remove(position);
        mediaItems.forcePut(position, new ImageItem(String.format("file://%1$s", newUrl)));
        mMediaListener.setImages(mediaItems);
    }

    private void swapVideo(int position, String newUrl) {
        mediaItems.remove(position);
        if (notifyIfCannotAddVideo()) return;

        final File videoThumbnail = getThumbnail(newUrl);

        final int videoLength = MediaUtils.getVideoLength(context, newUrl);
        putVideoItem(position, newUrl, videoThumbnail, videoLength);

        mMediaListener.setImages(mediaItems);
    }

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

        public String getThumb() {
            return mThumb;
        }
    }

    public abstract class AbstractImageItem extends MediaItem {

        public AbstractImageItem(@Nullable String media) {
            super(media);
        }

        @Override
        public void click() {
            showImageDialog(mediaItems.inverse().get(this), getThumb());
        }
    }

    public class ImageItem extends AbstractImageItem {

        public ImageItem(@NonNull String media) {
            super(media);
        }

        @Override
        public boolean isRemote() {
            return false;
        }

    }

    public class RemoteImageItem extends AbstractImageItem {

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

        @Override
        public void click() {
            showVideoDialog(mediaItems.inverse().get(this));
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

    private void showImageDialog(int position, String thumb) {
        mMediaListener.showImageDialog(position, thumb);
    }

    private void showVideoDialog(int position) {
        mMediaListener.showVideoDialog(position);
    }

    public void removeItem(int position) {
        removeItem(mediaItems.get(position));
    }

    private void removeItem(@NonNull Item mediaItem) {
        final Integer firstAvailablePosition = MoreObjects.firstNonNull(getFirstAvailablePosition(), mediaItems.values().size());
        final Integer position = mediaItems.inverse().get(mediaItem);

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

    public void swapMediaItem(int position, @NonNull String media, boolean isVideo) {
        if (isVideo) {
            swapVideo(position, media);
        } else {
            swapImage(position, media);
        }
    }

    private void addLocalVideoItem(@NonNull String media) {
        if (notifyIfCannotAddVideo()) return;

        final Integer position = getFirstAvailablePositionAndCheck();
        final File videoThumbnail = getThumbnail(media);

        final int videoLength = MediaUtils.getVideoLength(context, media);
        putVideoItem(position, media, videoThumbnail, videoLength);

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
        final boolean hasVideo = Iterables.any(mediaItems.values(), input -> input instanceof LocalVideoItem || input instanceof RemoteVideoItem);
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
                videoObservable = Observable.zip(videoFileObservable, thumbFileObservable, (video, thumb) -> Video.createVideo(video, thumb, localVideoItem.getDuration()));
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
            final Observable<List<String>> images = Observable.zip(imageObservables, args -> {
                final List<String> images1 = Lists.newArrayList();
                for (Object url : args) {
                    images1.add((String) url);
                }
                return images1;
            });

            if (videoObservable == null) {
                mCompositeSubscription.add(images.subscribe(images1 -> {
                    getAllEditedImagesAndComplete(images1, ImmutableList.<Video>of());
                }, throwable -> {
                    mMediaListener.showUploadError(throwable);
                    LogHelper.logThrowableAndCrashlytics(TAG, "Media upload failed", throwable);
                }));
            } else {
                mCompositeSubscription.add(images.zipWith(
                        videoObservable, BothParams::of)
                        .subscribe(listBothParamsBothParams -> {
                            getAllEditedImagesAndComplete(listBothParamsBothParams.param1(), ImmutableList.of(listBothParamsBothParams.param2()));
                        }, throwable -> {
                            mMediaListener.showUploadError(throwable);
                            LogHelper.logThrowableAndCrashlytics(TAG, "Media upload failed", throwable);
                        }));
            }
        }
    }

    private void putVideoItem(int position, String newUrl, File videoThumbnail, int videoLength) {
        mediaItems.put(position, new LocalVideoItem(
                videoThumbnail != null ? String.format("file://%1$s", videoThumbnail.getAbsolutePath()) : null,
                newUrl,
                videoLength));
    }

    private boolean notifyIfCannotAddVideo() {
        if (!canAddVideo()) {
            mMediaListener.onlyOneVideoAllowedAlert();
            return true;
        }
        return false;
    }

    @Nullable
    private File getThumbnail(String newUrl) {
        try {
            return MediaUtils.createVideoThumbnail(context, Uri.parse(newUrl));
        } catch (IOException e) {
            mMediaListener.thumbnailCreateError();
            return null;
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

        void showImageDialog(int position, String path);

        void showVideoDialog(int position);
    }
}
