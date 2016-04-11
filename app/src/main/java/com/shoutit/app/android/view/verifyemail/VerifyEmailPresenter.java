package com.shoutit.app.android.view.verifyemail;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.VerifyEmailRequest;
import com.shoutit.app.android.api.model.VerifyEmailResponse;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.Validators;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class VerifyEmailPresenter {

    @Nonnull
    private final Observable<String> emailObservable;
    @Nonnull
    private final Observable<String> resendResponseMessage;
    @Nonnull
    private final Observable<Boolean> isEmailVerifiedObservable;
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<Boolean> wrongEmailErrorObservable;

    @Nonnull
    private final BehaviorSubject<String> emailSubject = BehaviorSubject.create();
    @Nonnull
    private final PublishSubject<Object> verifyClickSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> resendClickSubject = PublishSubject.create();

    @Inject
    public VerifyEmailPresenter(@Nonnull final ProfilesDao profilesDao,
                                @Nonnull final ApiService apiService,
                                @Nonnull UserPreferences userPreferences,
                                @Nonnull @UiScheduler final Scheduler uiScheduler,
                                @Nonnull @NetworkScheduler final Scheduler networkScheduler) {

        final Observable<ResponseOrError<User>> profileObservable = profilesDao.getProfileDao(User.ME)
                .getProfileObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        final Observable<User> profilesSuccessObservable = profileObservable
                .compose(ResponseOrError.<User>onlySuccess());

        emailObservable = profilesSuccessObservable.first()
                .startWith(userPreferences.getUserObservable().first())
                .filter(Functions1.isNotNull())
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getEmail();
                    }
                })
                .compose(ObservableExtensions.<String>behaviorRefCount());

        final Observable<Boolean> isEmailCorrectObservable = emailSubject.startWith((String) null)
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String email) {
                        return Validators.isEmailValid(email);
                    }
                })
                .compose(ObservableExtensions.<Boolean>behaviorRefCount());

        /** Resend **/
        final PublishSubject<Boolean> progressSubject = PublishSubject.create();
        final Observable<ResponseOrError<VerifyEmailResponse>> resendObservable = resendClickSubject
                .withLatestFrom(isEmailCorrectObservable, new Func2<Object, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Object o, Boolean isCorrect) {
                        return isCorrect;
                    }
                })
                .filter(Functions1.isTrue())
                .lift(MoreOperators.callOnNext(progressSubject))
                .throttleFirst(1, TimeUnit.SECONDS)
                .switchMap(new Func1<Object, Observable<ResponseOrError<VerifyEmailResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<VerifyEmailResponse>> call(Object o) {
                        return apiService.verifyEmail(new VerifyEmailRequest(emailSubject.getValue()))
                                .compose(ResponseOrError.<VerifyEmailResponse>toResponseOrErrorObservable())
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler);
                    }
                });

        resendResponseMessage = resendObservable.compose(ResponseOrError.<VerifyEmailResponse>onlySuccess())
                .map(new Func1<VerifyEmailResponse, String>() {
                    @Override
                    public String call(VerifyEmailResponse verifyEmailResponse) {
                        return verifyEmailResponse.getSuccess();
                    }
                });

        /** Verify **/
        verifyClickSubject
                .subscribe(profilesDao.getProfileDao(User.ME).getRefreshSubject());

        isEmailVerifiedObservable = profilesSuccessObservable
                .skip(1)
                .map(new Func1<User, Boolean>() {
                    @Override
                    public Boolean call(User user) {
                        return user.isActivated();
                    }
                });

        /** Errors and Progress **/
        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(profileObservable),
                        ResponseOrError.transform(resendObservable)
                ))
                .observeOn(uiScheduler)
                .filter(Functions1.isNotNull());

        wrongEmailErrorObservable = resendClickSubject
                .flatMap(MoreFunctions1.returnObservableFirst(isEmailCorrectObservable.map(Functions1.neg())));

        progressObservable = Observable.merge(
                progressSubject,
                resendObservable.map(Functions1.returnFalse())
        ).observeOn(uiScheduler);

    }

    @Nonnull
    public Observable<Boolean> getWrongEmailErrorObservable() {
        return wrongEmailErrorObservable;
    }

    @Nonnull
    public Observable<String> getEmailObservable() {
        return emailObservable;
    }

    @Nonnull
    public Observable<String> getResendResponseMessage() {
        return resendResponseMessage;
    }

    @Nonnull
    public Observable<Boolean> getIsEmailVerifiedObservable() {
        return isEmailVerifiedObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observer<String> getEmailObserver() {
        return RxMoreObservers.ignoreCompleted(emailSubject);
    }

    @Nonnull
    public Observer<Object> getVerifyClickObserver() {
        return RxMoreObservers.ignoreCompleted(verifyClickSubject);
    }

    @Nonnull
    public Observer<Object> getResendClickObserver() {
        return RxMoreObservers.ignoreCompleted(resendClickSubject);
    }
}