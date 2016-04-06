package com.shoutit.app.android.view.videoconversation;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.TwilioResponse;
import com.shoutit.app.android.api.model.UserIdentity;
import com.shoutit.app.android.dao.UsersIdentityDao;
import com.shoutit.app.android.dao.VideoCallsDao;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;

public class VideoConversationPresenter {

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private Observable<String> apiKeyUserObservable;
    @Nonnull
    private Observable<String> twilioRequirementObservable;
    @Nonnull
    private Observable<Throwable> errorObservable;

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

        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(twilioResponse)))
                .filter(Functions1.isNotNull()).doOnNext(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                })
                .observeOn(uiScheduler);

    }

    @Nonnull
    public Observable<String> getApiKeyUserObservable() {
        return apiKeyUserObservable;
    }

    @Nonnull
    public Observable<String> getTwilioRequirementObservable() {
        return twilioRequirementObservable;
    }
    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }
}
