package com.shoutit.app.android.view.chats.chat_info;


import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.EditPublicChatRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.ImageCaptureHelper;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

public class ChatInfoPresenter {

    public static final int RESULT_OK = -1;

    private Uri url;
    private ChatInfoView listener;

    private final ImageCaptureHelper mImageCaptureHelper;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final AmazonHelper mAmazonHelper;
    private final String mConversationId;
    private final UserPreferences mUserPreferences;

    @Inject
    public ChatInfoPresenter(@NonNull ImageCaptureHelper imageCaptureHelper,
                             @NonNull ApiService apiService,
                             @NetworkScheduler Scheduler networkScheduler,
                             @UiScheduler Scheduler uiScheduler,
                             @NonNull AmazonHelper amazonHelper,
                             @NonNull String conversationId,
                             @NonNull UserPreferences userPreferences) {
        mImageCaptureHelper = imageCaptureHelper;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mAmazonHelper = amazonHelper;
        mConversationId = conversationId;
        mUserPreferences = userPreferences;
    }

    public void selectImageClicked() {
        if (url == null) {
            listener.startSelectImageActivity();
        } else {
            url = null;
            listener.setImage(null);
        }
    }

    public void createClicked() {
        final String data = listener.getSubject();
        if (!isDataCorrect(data)) {
            listener.subjectEmptyError();
        } else {
            listener.showProgress(true);
            Observable
                    .defer(new Func0<Observable<String>>() {
                        @Override
                        public Observable<String> call() {
                            if (url != null) {
                                return mAmazonHelper.uploadGroupChatObservable(new File(url.toString()))
                                        .subscribeOn(mNetworkScheduler)
                                        .observeOn(mUiScheduler);
                            } else {
                                return Observable.just(null);
                            }
                        }
                    })
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .flatMap(new Func1<Object, Observable<ResponseBody>>() {
                        @Override
                        public Observable<ResponseBody> call(Object user) {
                            return mApiService.updateConversation(mConversationId, new EditPublicChatRequest(data))
                                    .subscribeOn(mNetworkScheduler)
                                    .observeOn(mUiScheduler);
                        }
                    })
                    .subscribe(new Action1<ResponseBody>() {
                        @Override
                        public void call(ResponseBody responseBody) {
                            listener.finish();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            listener.editRequestError();
                            listener.showProgress(false);
                        }
                    });
        }
    }

    private boolean isDataCorrect(String data) {
        return !Strings.isNullOrEmpty(data);
    }

    public void register(@NonNull ChatInfoView listener) {
        this.listener = listener;
        loadConversation();
    }

    private void loadConversation() {
        mApiService.getConversation(mConversationId)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(
                        new Action1<Conversation>() {
                            @Override
                            public void call(Conversation conversation) {
                                final int participantsCount = conversation.getProfiles().size();
                                final int blockedSize = conversation.getBlocked().size();

                                final boolean isAdmin = isAdmin(conversation.getAdmins());

                                listener.isAdmin(isAdmin);
                                listener.setParticipantsCount(participantsCount);
                                listener.setBlockedCount(blockedSize);
                                if (!Strings.isNullOrEmpty(conversation.getIcon())) {
                                    listener.setImage(Uri.parse(conversation.getIcon()));
                                }
                                listener.setSubject(conversation.getSubject());
                                listener.showSubject(conversation.isPublicChat());
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                listener.loadConversationError();
                            }
                        });
    }

    private boolean isAdmin(List<String> admins) {
        final User user = mUserPreferences.getUser();
        assert user != null;
        return admins.contains(user.getId());
    }

    public void unregister() {
        listener = null;
        // TODO unsub
    }

    public void onImageActivityFinished(int resultCode, Intent data) {
        final Optional<Uri> uriOptional = mImageCaptureHelper.onResult(resultCode, data);
        if (uriOptional.isPresent()) {
            url = uriOptional.get();
            listener.setImage(url);
        }
    }

    public interface ChatInfoView {

        void showProgress(boolean show);

        void setImage(@Nullable Uri imageUrl);

        void setSubject(@Nonnull String subject);

        void startSelectImageActivity();

        void subjectEmptyError();

        String getSubject();

        void finish();

        void editRequestError();

        void setShoutsCount(int count);

        void setMediaCount(int count);

        void setParticipantsCount(int count);

        void setBlockedCount(int count);

        void loadConversationError();

        void isAdmin(boolean isAdmin);

        void showSubject(boolean show);
    }
}
