package com.shoutit.app.android.view.chats.chat_media_gallery;

import android.content.res.Resources;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ConversationMediaDaos;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class ChatMediaGalleryActivityModule {

    @Nonnull
    private final String conversationId;

    public ChatMediaGalleryActivityModule(@Nonnull String conversationId) {
        this.conversationId = conversationId;
    }

    @Provides
    ChatMediaGalleryPresenter provideChatMediaGalleryPresenter(ConversationMediaDaos dao,
                                                               @UiScheduler Scheduler uiScheduler,
                                                               @ForActivity Resources resources,
                                                               Picasso picasso,
                                                               @Named("NoAmazonTransformer") final Picasso picassoWithoutTransformer) {
        return new ChatMediaGalleryPresenter(dao, uiScheduler, conversationId, resources,
                picasso, picassoWithoutTransformer);
    }
}
