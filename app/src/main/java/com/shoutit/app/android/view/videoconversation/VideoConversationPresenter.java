package com.shoutit.app.android.view.videoconversation;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.model.TwilioResponse;
import com.shoutit.app.android.api.model.UserIdentity;
import com.shoutit.app.android.dao.UsersIdentityDao;
import com.shoutit.app.android.dao.VideoCallsDao;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class VideoConversationPresenter {

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private Observable<String> apiKeyUserObservable;
    @Nonnull
    private Observable<String> twilioRequirementObservable;

    @Inject
    public VideoConversationPresenter(@Nonnull final VideoCallsDao videoCallsDao,
                                      @Nonnull @UiScheduler final Scheduler uiScheduler
    ) {
        this.uiScheduler = uiScheduler;

        /** Requests **/
        final Observable<ResponseOrError<TwilioResponse>> twilioResponse = videoCallsDao.getVideoCallsObservable()
                .compose(ObservableExtensions.<ResponseOrError<TwilioResponse>>behaviorRefCount());

        final Observable<TwilioResponse> successTwilioResponse = twilioResponse
                .compose(ResponseOrError.<TwilioResponse>onlySuccess());

        final Observable<Throwable> failedTwilioResponse = twilioResponse
                .compose(ResponseOrError.<TwilioResponse>onlyError());

        apiKeyUserObservable = successTwilioResponse
                .map(new Func1<TwilioResponse, String>() {
                    @Override
                    public String call(TwilioResponse twilioResponse) {
                        return twilioResponse.getToken();
                    }
                }).observeOn(uiScheduler);

        twilioRequirementObservable = getApiKeyUserObservable()
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String apiKey) {
                        return apiKey != null;
                    }
                });
    }

    public Observable<String> getApiKeyUserObservable() {
        return apiKeyUserObservable;
    }

    public Observable<String> getTwilioRequirementObservable() {
        return twilioRequirementObservable;
    }
}
