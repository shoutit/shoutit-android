package com.shoutit.app.android.view.chats.public_chat;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CreatePublicChatRequest;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.view.createshout.location.LocationResultHelper;

import okhttp3.ResponseBody;
import rx.Scheduler;
import rx.functions.Action1;

public class CreatePublicChatPresenter {

    public static final int RESULT_OK = -1;

    private State state = State.empty();
    private CreatePublicChatView listener;

    private final ImageCaptureHelper mImageCaptureHelper;
    private final Context mContext;
    @NonNull
    private final ApiService mApiService;
    private final Scheduler mNetworkScheduler;
    private final Scheduler mUiScheduler;

    public CreatePublicChatPresenter(@NonNull ImageCaptureHelper imageCaptureHelper,
                                     @NonNull @ForActivity Context context,
                                     @NonNull ApiService apiService,
                                     @NetworkScheduler Scheduler networkScheduler,
                                     @UiScheduler Scheduler uiScheduler) {
        mImageCaptureHelper = imageCaptureHelper;
        mContext = context;
        mApiService = apiService;
        mNetworkScheduler = networkScheduler;
        mUiScheduler = uiScheduler;
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
            mApiService.createPublicChat(new CreatePublicChatRequest(data.subject))
                    .subscribeOn(mNetworkScheduler)
                    .observeOn(mUiScheduler)
                    .subscribe(new Action1<ResponseBody>() {
                        @Override
                        public void call(ResponseBody responseBody) {
                            listener.finish();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            listener.createRequestError();
                            listener.showProgress(false);
                        }
                    });
        }
    }

    private boolean isDataCorrect(CreatePublicChatData data) {
        return !Strings.isNullOrEmpty(data.subject);
    }

    public void register(@NonNull CreatePublicChatView listener) {
        this.listener = listener;
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
            state = new State(state.url, location);
            listener.setLocation(ResourcesHelper.getResourceIdForName(location.getCountry(), mContext), location.getCity());
        }
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

        void setLocation(int flag, @Nullable String location);

        void setImage(@Nullable Uri imageUrl);

        void startSelectLocationActivity();

        void startSelectImageActivity();

        void subjectEmptyError();

        CreatePublicChatData getData();

        void finish();

        void createRequestError();
    }
}
