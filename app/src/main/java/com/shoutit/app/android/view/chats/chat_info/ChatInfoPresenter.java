package com.shoutit.app.android.view.chats.chat_info;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ConversationDetails;
import com.shoutit.app.android.api.model.EditPublicChatRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.model.ReportBody;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.DateTimeUtils;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.view.chats.ChatsMediaHelper;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class ChatInfoPresenter {

    public static final int RESULT_OK = -1;

    private boolean isAdmin;
    private Uri url;
    private ChatInfoView listener;

    private final ImageCaptureHelper mImageCaptureHelper;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final AmazonHelper mAmazonHelper;
    private final String mConversationId;
    @NonNull
    private final Resources mResources;
    private final Context mContext;
    private final String mId;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Inject
    public ChatInfoPresenter(@NonNull ImageCaptureHelper imageCaptureHelper,
                             @NonNull ApiService apiService,
                             @NetworkScheduler Scheduler networkScheduler,
                             @UiScheduler Scheduler uiScheduler,
                             @NonNull AmazonHelper amazonHelper,
                             @NonNull String conversationId,
                             @NonNull UserPreferences userPreferences,
                             @NonNull @ForActivity Resources resources,
                             @ForActivity Context context) {
        mImageCaptureHelper = imageCaptureHelper;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mAmazonHelper = amazonHelper;
        mConversationId = conversationId;
        mResources = resources;
        mContext = context;

        final User user = userPreferences.getUser();
        assert user != null;
        mId = user.getId();
    }

    public void selectImageClicked() {
        if (isAdmin) {
            if (url == null) {
                listener.startSelectImageActivity();
            } else {
                url = null;
                listener.setImage(null);
            }
        }
    }

    public void saveClicked() {
        final String data = listener.getSubject();
        if (!isDataCorrect(data)) {
            listener.subjectEmptyError();
        } else {
            listener.showProgress(true);
            mCompositeSubscription.add(Observable
                    .defer(new Func0<Observable<String>>() {
                        @Override
                        public Observable<String> call() {
                            return ChatsMediaHelper.uploadChatImage(mAmazonHelper, url, mContext, mNetworkScheduler, mUiScheduler);
                        }
                    })
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .flatMap(new Func1<String, Observable<ResponseBody>>() {
                        @Override
                        public Observable<ResponseBody> call(String url) {
                            return mApiService.updateConversation(mConversationId, new EditPublicChatRequest(data, url))
                                    .subscribeOn(mNetworkScheduler)
                                    .observeOn(mUiScheduler);
                        }
                    })
                    .subscribe(new Action1<ResponseBody>() {
                        @Override
                        public void call(ResponseBody responseBody) {
                            listener.finishScreen(false);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            listener.editRequestError();
                            listener.showProgress(false);
                        }
                    }));
        }
    }

    private boolean isDataCorrect(String data) {
        return !Strings.isNullOrEmpty(data);
    }

    public void register(@NonNull ChatInfoView listener) {
        this.listener = listener;
        listener.showProgress(true);
        loadConversation();
    }

    private void loadConversation() {
        mCompositeSubscription.add(getConversationObservable()
                .subscribe(
                        new Action1<ConversationDetails>() {
                            @Override
                            public void call(ConversationDetails conversation) {
                                isAdmin = isAdmin(conversation.getAdmins());

                                listener.isAdmin(isAdmin);
                                final ConversationDetails.AttatchmentCount attachmentsCount = conversation.getAttachmentsCount();
                                listener.setParticipantsCount(conversation.getProfiles().size());
                                listener.setBlockedCount(conversation.getBlocked().size());
                                listener.setMediaCount(attachmentsCount.getMedia());
                                listener.setShoutsCount(attachmentsCount.getShout());
                                final ConversationDetails.Display display = conversation.getDisplay();
                                final String image = display.getImage();
                                if (!Strings.isNullOrEmpty(image)) {
                                    listener.setImage(Uri.parse(image));
                                }
                                listener.setSubject(display.getTitle());
                                listener.showSubject(conversation.isPublicChat(), isAdmin);
                                listener.showReport(conversation.isPublicChat());
                                listener.setChatCreatedBy(getCreatedByString(conversation.getCreator().getName()));
                                listener.setChatCreatedAt(getCreatedAtString(conversation.getCreatedAt()));
                                listener.showProgress(false);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                listener.showProgress(false);
                                listener.loadConversationError();
                            }
                        }));
    }


    public void refreshCounts() {
        mCompositeSubscription.add(getConversationObservable()
                .subscribe(new Action1<ConversationDetails>() {
                    @Override
                    public void call(ConversationDetails conversation) {
                        listener.setParticipantsCount(conversation.getProfiles().size());
                        listener.setBlockedCount(conversation.getBlocked().size());

                        final ConversationDetails.AttatchmentCount attachmentsCount = conversation.getAttachmentsCount();
                        listener.setMediaCount(attachmentsCount.getMedia());
                        listener.setShoutsCount(attachmentsCount.getShout());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        listener.loadConversationError();
                    }
                }));
    }

    private Observable<ConversationDetails> getConversationObservable() {
        return mApiService.getConversation(mConversationId)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler);
    }

    private String getCreatedByString(@NonNull String name) {
        return String.format(mResources.getString(R.string.chat_info_created_by), name);
    }

    private String getCreatedAtString(long createdAt) {
        return String.format(mResources.getString(R.string.chat_info_created_at), DateTimeUtils.getChatCreatedAtDate(createdAt * 1000));
    }

    private boolean isAdmin(List<String> admins) {
        return admins.contains(mId);
    }

    public void unregister() {
        listener = null;
        mCompositeSubscription.unsubscribe();
    }

    public void onImageActivityFinished(int resultCode, Intent data) {
        final Optional<Uri> uriOptional = mImageCaptureHelper.onResult(resultCode, data);
        if (uriOptional.isPresent()) {
            url = uriOptional.get();
            listener.setImage(url);
        }
    }

    public void exitChatClicked() {
        listener.showProgress(true);
        mCompositeSubscription.add(mApiService.deleteConversation(mConversationId)
                .observeOn(mUiScheduler)
                .subscribeOn(mNetworkScheduler)
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        listener.finishScreen(true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        listener.exitChatError();
                        listener.showProgress(false);
                    }
                }));
    }

    public void sendReport(String reportBody) {
        listener.showProgress(true);
        mCompositeSubscription.add(mApiService.report(ReportBody.forConversation(mConversationId, reportBody))
                .subscribeOn(mNetworkScheduler)
                .observeOn(mUiScheduler)
                .subscribe(new Action1<Response<Object>>() {
                    @Override
                    public void call(Response<Object> objectResponse) {
                        listener.reportSent();
                        listener.showProgress(false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        listener.reportError();
                        listener.showProgress(false);
                    }
                }));
    }

    public interface ChatInfoView {

        void showProgress(boolean show);

        void setImage(@Nullable Uri imageUrl);

        void setSubject(@Nonnull String subject);

        void startSelectImageActivity();

        void subjectEmptyError();

        String getSubject();

        void finishScreen(boolean closeChat);

        void editRequestError();

        void setShoutsCount(int count);

        void setMediaCount(int count);

        void setParticipantsCount(int count);

        void setBlockedCount(int count);

        void loadConversationError();

        void isAdmin(boolean isAdmin);

        void showSubject(boolean show, boolean isAdmin);

        void setChatCreatedBy(@NonNull String createdBy);

        void setChatCreatedAt(@NonNull String chatCreatedAt);

        void exitChatError();

        void showReport(boolean show);

        void reportSent();

        void reportError();
    }
}
