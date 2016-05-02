package com.shoutit.app.android.view.signin.register;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Strings;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.login.LoginUser;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.Validators;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class RegisterPresenter {

    private final Observable<SignResponse> mSuccessObservable;
    private final Observable<Throwable> mErrorObservable;
    private final BehaviorSubject<String> mEmailSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> mPasswordSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> mNameSubject = BehaviorSubject.create();
    private final PublishSubject<Object> mProceedSubject = PublishSubject.create();
    private final Observable<String> mPasswordEmpty;
    private final Observable<UserLocation> mLocationObservable;
    private final Observable<String> mEmailEmpty;
    private final Observable<String> mNameEmpty;
    private final Observable<String> mPasswordNotEmpty;
    private final Observable<String> mEmailNotEmpty;
    private final Observable<String> mNameNotEmpty;
    private final Observable<Boolean> wrongEmailErrorObservable;

    @Inject
    public RegisterPresenter(@NonNull final ApiService apiService,
                             @NonNull final UserPreferences userPreferences,
                             @NonNull @NetworkScheduler final Scheduler networkScheduler,
                             @NonNull @UiScheduler final Scheduler uiScheduler,
                             @Nonnull final MixPanel mixPanel) {

        mLocationObservable = userPreferences
                .getLocationObservable()
                .startWith((UserLocation) null)
                .compose(ObservableExtensions.<UserLocation>behaviorRefCount());

        final Observable<ResponseOrError<SignResponse>> responseOrErrorObservable = mProceedSubject
                .map(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return areValuesCorrect();
                    }
                })
                .filter(Functions1.isTrue())
                .switchMap(new Func1<Boolean, Observable<UserLocation>>() {
                    @Override
                    public Observable<UserLocation> call(Boolean aBoolean) {
                        return mLocationObservable.filter(Functions1.isNotNull()).first();
                    }
                })
                .switchMap(new Func1<UserLocation, Observable<EmailSignupRequest>>() {
                    @Override
                    public Observable<EmailSignupRequest> call(final UserLocation location) {
                        return Observable.zip(mNameSubject, mEmailSubject, mPasswordSubject,
                                new Func3<String, String, String, EmailSignupRequest>() {
                                    @Override
                                    public EmailSignupRequest call(String name, String email, String password) {
                                        return new EmailSignupRequest(name, email, password, LoginUser.loginUser(location), mixPanel.getDistinctId());
                                    }
                                })
                                .first();
                    }
                })
                .switchMap(new Func1<EmailSignupRequest, Observable<ResponseOrError<SignResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<SignResponse>> call(EmailSignupRequest signupRequest) {
                        return apiService.signup(signupRequest)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<SignResponse>toResponseOrErrorObservable());
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<SignResponse>>behaviorRefCount());


        mPasswordEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mPasswordSubject)).filter(getLessThan6AndMoreThan20CharsFunc1());
        mEmailEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mEmailSubject)).filter(Functions1.isNullOrEmpty());
        mNameEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mNameSubject)).filter(Functions1.isNullOrEmpty());

        mPasswordNotEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mPasswordSubject)).filter(Functions1.neg(getLessThan6AndMoreThan20CharsFunc1()));
        mEmailNotEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mEmailSubject)).filter(Functions1.neg(Functions1.isNullOrEmpty()));
        mNameNotEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mNameSubject)).filter(Functions1.neg(Functions1.isNullOrEmpty()));

        mSuccessObservable = responseOrErrorObservable
                .compose(ResponseOrError.<SignResponse>onlySuccess())
                .doOnNext(new Action1<SignResponse>() {
                    @Override
                    public void call(SignResponse signResponse) {
                        userPreferences.setLoggedIn(signResponse.getAccessToken(),
                                signResponse.getRefreshToken(), signResponse.getUser());
                        userPreferences.setShouldAskForInterestTrue();
                    }
                });

        mErrorObservable = responseOrErrorObservable.compose(ResponseOrError.<SignResponse>onlyError());

        wrongEmailErrorObservable = mProceedSubject
                .map(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return !isEmailCorrect();
                    }
                });
    }

    private boolean areValuesCorrect() {
        return isEmailCorrect() &&
                !Strings.isNullOrEmpty(mNameSubject.getValue()) &&
                isPasswordCorrect(mPasswordSubject.getValue());
    }

    private boolean isEmailCorrect() {
        return Validators.isEmailValid(mEmailSubject.getValue());
    }

    @Nonnull
    public Observable<Boolean> getWrongEmailErrorObservable() {
        return wrongEmailErrorObservable;
    }

    @NonNull
    private Func1<? super CharSequence, Boolean> getLessThan6AndMoreThan20CharsFunc1() {
        return new Func1<CharSequence, Boolean>() {
            @Override
            public Boolean call(CharSequence charSequence) {
                final int length = charSequence.length();
                return length < 6 || length > 20;
            }
        };
    }

    private boolean isPasswordCorrect(@Nullable String password) {
        return password != null && password.length() >= 6 && password.length() <= 20;
    }

    @NonNull
    public Observer<String> getEmailObserver() {
        return RxMoreObservers.ignoreCompleted(mEmailSubject);
    }

    @NonNull
    public Observer<String> getPasswordObserver() {
        return RxMoreObservers.ignoreCompleted(mPasswordSubject);
    }

    @NonNull
    public Observer<Object> getProceedObserver() {
        return mProceedSubject;
    }

    @NonNull
    public Observable<SignResponse> successObservable() {
        return mSuccessObservable;
    }

    @NonNull
    public Observable<Throwable> failObservable() {
        return mErrorObservable;
    }

    @NonNull
    public Observable<String> getPasswordEmpty() {
        return mPasswordEmpty;
    }

    @NonNull
    public Observer<String> getNameObserver() {
        return RxMoreObservers.ignoreCompleted(mNameSubject);
    }

    @NonNull
    public Observable<String> getEmailEmpty() {
        return mEmailEmpty;
    }

    @NonNull
    public Observable<UserLocation> getLocationObservable() {
        return mLocationObservable;
    }

    @NonNull
    public Observable<String> getNameEmpty() {
        return mNameEmpty;
    }

    @NonNull
    public Observable<String> getPasswordNotEmpty() {
        return mPasswordNotEmpty;
    }

    @NonNull
    public Observable<String> getEmailNotEmpty() {
        return mEmailNotEmpty;
    }

    @NonNull
    public Observable<String> getNameNotEmpty() {
        return mNameNotEmpty;
    }
}
