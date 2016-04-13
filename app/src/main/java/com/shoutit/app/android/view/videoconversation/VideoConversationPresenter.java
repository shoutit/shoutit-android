package com.shoutit.app.android.view.videoconversation;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.CallerProfile;
import com.shoutit.app.android.api.model.TwilioResponse;
import com.shoutit.app.android.dao.UsersIdentityDao;
import com.shoutit.app.android.dao.VideoCallsDao;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class VideoConversationPresenter {

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private Observable<String> apiKeyUserObservable;
    @Nonnull
    private Observable<String> twilioRequirementObservable;
    @Nonnull
    private Observable<String> callerNameObservable;
    @Nonnull
    private Observable<Throwable> errorObservable;
    @Nonnull
    private BehaviorSubject<String> callerIdentitySubject = BehaviorSubject.create();

    @Inject
    public VideoConversationPresenter(@Nonnull final VideoCallsDao videoCallsDao,
                                      @Nonnull final UsersIdentityDao usersIdentityDao,
                                      @Nonnull @UiScheduler final Scheduler uiScheduler
    ) {
        this.uiScheduler = uiScheduler;

        /** Requests **/
        final Observable<ResponseOrError<TwilioResponse>> twilioResponse = videoCallsDao.getVideoCallsObservable()
                .compose(ObservableExtensions.<ResponseOrError<TwilioResponse>>behaviorRefCount());

        final Observable<TwilioResponse> successTwilioResponse = twilioResponse
                .compose(ResponseOrError.<TwilioResponse>onlySuccess());

        apiKeyUserObservable = successTwilioResponse
                .map(new Func1<TwilioResponse, String>() {
                    @Override
                    public String call(TwilioResponse twilioResponse) {
                        return twilioResponse.getToken();
                    }
                }).observeOn(uiScheduler);

        twilioRequirementObservable = getApiKeyUserObservable()
                .filter(Functions1.isNotNull());


        Observable<ResponseOrError<CallerProfile>> callerProfileResponse = callerIdentitySubject
                .flatMap(new Func1<String, Observable<ResponseOrError<CallerProfile>>>() {
                    @Override
                    public Observable<ResponseOrError<CallerProfile>> call(String callerName) {
                        return usersIdentityDao.getUserByIdentityObservable(callerName);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<CallerProfile>>behaviorRefCount());

        Observable<CallerProfile> successCallerProfileResponse = callerProfileResponse
                .compose(ResponseOrError.<CallerProfile>onlySuccess());

        callerNameObservable = successCallerProfileResponse
                .filter(Functions1.isNotNull())
                .map(new Func1<CallerProfile, String>() {
                    @Override
                    public String call(CallerProfile callerProfile) {
                        return callerProfile.getName();
                    }
                }).observeOn(uiScheduler);

        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(twilioResponse),
                ResponseOrError.transform(callerProfileResponse)))
                .filter(Functions1.isNotNull())
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

    @Nonnull
    public Observable<String> getCallerNameObservable() {
        return callerNameObservable;
    }

    @Nonnull
    public void setCallerIdentity(@Nonnull String identity){
        callerIdentitySubject.onNext(identity);
    }
}
