package com.shoutit.app.android.view.chats.chat_media_gallery;

import android.content.res.Resources;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.ConversationMediaResponse;
import com.shoutit.app.android.api.model.MessageAttachment;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dao.ConversationMediaDaos;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.gallery.MediaLoaders;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.MediaInfo;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Named;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;

public class ChatMediaGalleryPresenter {

    private final Observable<List<MediaInfo>> mediaInfoObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final ConversationMediaDaos daos;
    @Nonnull
    private final String conversationId;

    public ChatMediaGalleryPresenter(@Nonnull ConversationMediaDaos daos,
                                     @Nonnull @UiScheduler Scheduler uiScheduler,
                                     @Nonnull String conversationId,
                                     final Resources resources,
                                     final Picasso picasso,
                                     @Named("NoAmazonTransformer") final Picasso picassoWithoutTransformer) {
        this.daos = daos;
        this.conversationId = conversationId;

        final Observable<ResponseOrError<ConversationMediaResponse>> mediaObservable = daos.getDao(conversationId)
                .getMediaObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ConversationMediaResponse>>behaviorRefCount());

        final Observable<ConversationMediaResponse> successMediaObservable = mediaObservable
                .compose(ResponseOrError.<ConversationMediaResponse>onlySuccess());

        mediaInfoObservable = successMediaObservable
                .map(new Func1<ConversationMediaResponse, List<MediaInfo>>() {
                    @Override
                    public List<MediaInfo> call(ConversationMediaResponse response) {
                        final ImmutableList.Builder<MediaInfo> builder = ImmutableList.builder();

                        for (MessageAttachment attachment : response.getResults()) {
                            for (String imageUrl : attachment.getImages()) {
                                final MediaInfo mediaInfo = MediaInfo.mediaLoader(new MediaLoaders.ImagesLoader(
                                        imageUrl, picasso, picassoWithoutTransformer, resources));
                                builder.add(mediaInfo);
                            }

                            for (Video video : attachment.getVideos()) {
                                final MediaInfo mediaInfo = MediaInfo.mediaLoader(new MediaLoaders.VideosLoader(
                                        picasso, video.getUrl(), video.getThumbnailUrl()));
                                builder.add(mediaInfo);
                            }
                        }

                        return builder.build();
                    }
                })
                .filter(MoreFunctions1.<MediaInfo>listNotEmpty());

        errorObservable = mediaObservable
                .compose(ResponseOrError.<ConversationMediaResponse>onlyError());

        progressObservable = mediaObservable
                .map(Functions1.returnFalse())
                .startWith(true);

    }

    public Observable<List<MediaInfo>> getMediaInfoObservable() {
        return mediaInfoObservable;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(
                daos.getDao(conversationId).getLoadMoreObserver());
    }
}
