package com.shoutit.app.android.view.chats.public_chat;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ImageCaptureHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.view.createshout.location.LocationResultHelper;

public class CreatePublicChatPresenter {

    public static final int RESULT_OK = -1;

    private State state = State.empty();
    private CreatePublicChatView listener;

    private final ImageCaptureHelper mImageCaptureHelper;
    private final Context mContext;

    public CreatePublicChatPresenter(@NonNull ImageCaptureHelper imageCaptureHelper, @ForActivity Context context) {
        mImageCaptureHelper = imageCaptureHelper;
        mContext = context;
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
        // TODO
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

    private static class CreatePublicChatData {

        private final String subject;
        private final String photoUrl;
        private final boolean facebook;
        private final boolean twitter;
        private final UserLocation location;

        public CreatePublicChatData(String subject, String photoUrl, boolean facebook, boolean twitter, UserLocation location) {
            this.subject = subject;
            this.photoUrl = photoUrl;
            this.facebook = facebook;
            this.twitter = twitter;
            this.location = location;
        }
    }

    public interface CreatePublicChatView {

        void showProgress(boolean show);

        void setLocation(int flag, @Nullable String location);

        void setImage(@Nullable Uri imageUrl);

        void startSelectLocationActivity();

        void startSelectImageActivity();

    }
}
