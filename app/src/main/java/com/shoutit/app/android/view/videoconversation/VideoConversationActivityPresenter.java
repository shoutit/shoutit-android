package com.shoutit.app.android.view.videoconversation;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.util.LogTransformer;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.VideoCallRequest;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class VideoConversationActivityPresenter {
    private static final int MAX_CALL_RETRIES = 3;

    private final BehaviorSubject<Set<String>> participantsSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> calledUserTwilioIdentitySubject = BehaviorSubject.create();
    private final PublishSubject<Object> makeOutgoingCallSubject = PublishSubject.create();
    private final PublishSubject<Object> finishCallRetriesSubject = PublishSubject.create();
    private final PublishSubject<Object> rejectCallObserver = PublishSubject.create();

    @Nonnull
    private final Observable<BothParams<Set<String>, Boolean>> makeCallObservable;

    @Inject
    public VideoConversationActivityPresenter(final ApiService apiService,
                                              @NetworkScheduler final Scheduler networkScheduler,
                                              @UiScheduler final Scheduler uiScheduler) {

        makeCallObservable = makeOutgoingCallSubject
                .scan(-1, new Func2<Integer, Object, Integer>() {
                    @Override
                    public Integer call(Integer integer, Object o) {
                        return ++integer;
                    }
                })
                .skip(1)
                .delay(new Func1<Integer, Observable<Object>>() {
                    @Override
                    public Observable<Object> call(Integer retryNumber) {
                        final int delay = retryNumber * 4;
                        return Observable.just(null)
                                .delay(delay, TimeUnit.SECONDS);
                    }
                })
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer retryNumber) {
                        return retryNumber < MAX_CALL_RETRIES;
                    }
                })
                .takeUntil(finishCallRetriesSubject)
                .compose(LogTransformer.<Integer>transformer("lol", "retry observable"))
                .withLatestFrom(calledUserTwilioIdentitySubject, new Func2<Integer, String, BothParams<String, Integer>>() {
                    @Override
                    public BothParams<String, Integer> call(Integer retryNumber, String twilioIdentity) {
                        return BothParams.of(twilioIdentity, retryNumber);
                    }
                })
                .switchMap(new Func1<BothParams<String, Integer>, Observable<BothParams<Set<String>, Boolean>>>() {
                    @Override
                    public Observable<BothParams<Set<String>, Boolean>> call(BothParams<String, Integer> identityWithRetryNumber) {
                        final String identity = identityWithRetryNumber.param1();
                        final boolean isLastRetry = identityWithRetryNumber.param2() == MAX_CALL_RETRIES - 1;

                        return apiService.videoCall(new VideoCallRequest(identity, false))
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable())
                                .compose(ResponseOrError.<ResponseBody>onlySuccess())
                                .withLatestFrom(participantsSubject, new Func2<ResponseBody, Set<String>, BothParams<Set<String>, Boolean>>() {
                                    @Override
                                    public BothParams<Set<String>, Boolean> call(ResponseBody responseBody, Set<String> participants) {
                                        return BothParams.of(participants, isLastRetry);
                                    }
                                });
                    }
                });
    }

    @Nonnull
    public Observable<BothParams<Set<String>, Boolean>> getMakeCallObservable() {
        return makeCallObservable;
    }

    public Observable<Object> getRejectCallObservable() {
        return rejectCallObserver;
    }

    public Observer<Object> getMakeOutgoingCallObserver() {
        return makeOutgoingCallSubject;
    }

    public Observer<String> getCalledUserTwilioIdentityObserver() {
        return calledUserTwilioIdentitySubject;
    }

    public Observer<Set<String>> getParticipantsObserver() {
        return participantsSubject;
    }

    public void retryCall() {
        makeOutgoingCallSubject.onNext(null);
    }

    public void finishRetries() {
        finishCallRetriesSubject.onNext(null);
    }
}
