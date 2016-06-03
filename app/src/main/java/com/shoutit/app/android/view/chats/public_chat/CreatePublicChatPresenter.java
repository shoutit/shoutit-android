package com.shoutit.app.android.view.chats.public_chat;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreatePublicChatRequest;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserLocationSimple;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.AmazonHelper;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.view.chats.ChatsMediaHelper;
import com.shoutit.app.android.view.conversations.RefreshConversationBus;
import com.shoutit.app.android.view.createshout.location.LocationResultHelper;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class CreatePublicChatPresenter {

    public static final int RESULT_OK = -1;

    private State state = State.empty();
    private CreatePublicChatView listener;

    private final ImageCaptureHelper mImageCaptureHelper;
    private final Context mContext;
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;
    private final AmazonHelper mAmazonHelper;
    @NonNull
    private final UserPreferences mUserPreferences;
    private final RefreshConversationBus mRefreshConversationBus;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Inject
    public CreatePublicChatPresenter(@NonNull ImageCaptureHelper imageCaptureHelper,
                                     @NonNull @ForActivity Context context,
                                     @NonNull ApiService apiService,
                                     @NetworkScheduler Scheduler networkScheduler,
                                     @UiScheduler Scheduler uiScheduler,
                                     @NonNull AmazonHelper amazonHelper,
                                     @NonNull UserPreferences userPreferences,
                                     RefreshConversationBus refreshConversationBus) {
        mImageCaptureHelper = imageCaptureHelper;
        mContext = context;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
        mAmazonHelper = amazonHelper;
        mUserPreferences = userPreferences;
        mRefreshConversationBus = refreshConversationBus;
    }

    public void selectImageClicked() {
        if (state.url == null) {
            listener.startSelectImageActivity();
        } else {
            state = new State(null, state.location);
            listener.setImage(null);
        }
    }

    public void selectLocationClicked() {
        listener.startSelectLocationActivity();
    }

    public void createClicked() {
        final CreatePublicChatData data = listener.getData();
        if (!isDataCorrect(data)) {
            listener.subjectEmptyError();
        } else {
            listener.showProgress(true);
            mCompositeSubscription.add(
                    ChatsMediaHelper.uploadChatImage(mAmazonHelper, state.url, mContext, mNetworkScheduler, mUiScheduler)
                            .subscribeOn(mNetworkScheduler)
                            .observeOn(mUiScheduler)
                            .flatMap(new Func1<String, Observable<ResponseBody>>() {
                                @Override
                                public Observable<ResponseBody> call(String url) {
                                    final UserLocation location = state.location;
                                    return mApiService.createPublicChat(new CreatePublicChatRequest(
                                            data.subject,
                                            url,
                                            location == null ? null : new UserLocationSimple(location.getLatitude(), location.getLongitude())))
                                            .subscribeOn(mNetworkScheduler)
                                            .observeOn(mUiScheduler);
                                }
                            })
                            .subscribe(new Action1<ResponseBody>() {
                                @Override
                                public void call(ResponseBody responseBody) {
                                    mRefreshConversationBus.post();
                                    listener.finish();
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    listener.createRequestError();
                                    listener.showProgress(false);
                                }
                            }));
        }
    }

    private boolean isDataCorrect(CreatePublicChatData data) {
        return !Strings.isNullOrEmpty(data.subject);
    }

    public void register(@NonNull CreatePublicChatView listener) {
        this.listener = listener;
        final UserLocation location = mUserPreferences.getLocation();
        if (location != null) {
            setLocation(location);
        }
    }

    public void unregister() {
        listener = null;
        mCompositeSubscription.unsubscribe();
    }

    public void onImageActivityFinished(int resultCode, Intent data) {
        final Optional<Uri> uriOptional = mImageCaptureHelper.onResult(resultCode, data);
        if (uriOptional.isPresent()) {
            final Uri imageUrl = uriOptional.get();
            state = new State(imageUrl, state.location);
            listener.setImage(imageUrl);
        }
    }

    public void onLocationActivityFinished(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final UserLocation location = LocationResultHelper.getLocationFromIntent(data);
            setLocation(location);
        }
    }

    private void setLocation(UserLocation location) {
        state = new State(state.url, location);
        listener.setLocation(ResourcesHelper.getResourceIdForName(location.getCountry(), mContext), location.getCity());
    }

    private static class State {
        private final Uri url;
        private final UserLocation location;

        private State(Uri url, UserLocation location) {
            this.url = url;
            this.location = location;
        }

        public static State empty() {
            return new State(null, null);
        }
    }

    public static class CreatePublicChatData {

        private final String subject;
        private final boolean facebook;
        private final boolean twitter;

        public CreatePublicChatData(String subject, boolean facebook, boolean twitter) {
            this.subject = subject;
            this.facebook = facebook;
            this.twitter = twitter;
        }
    }

    public interface CreatePublicChatView {

        void showProgress(boolean show);

        void setLocation(@DrawableRes int flag, @NonNull String location);

        void setImage(@Nullable Uri imageUrl);

        void startSelectLocationActivity();

        void startSelectImageActivity();

        void subjectEmptyError();

        CreatePublicChatData getData();

        void finish();

        void createRequestError();
    }
}
