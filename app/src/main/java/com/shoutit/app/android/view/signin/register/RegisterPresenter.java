package com.shoutit.app.android.view.signin.register;

import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.EmailSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.login.LoginUser;
import com.shoutit.app.android.utils.MoreFunctions1;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
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

    @Inject
    public RegisterPresenter(@NonNull final ApiService apiService,
                             @NonNull final UserPreferences userPreferences,
                             @NonNull @NetworkScheduler final Scheduler networkScheduler,
                             @NonNull @UiScheduler final Scheduler uiScheduler) {

        mLocationObservable = userPreferences
                .getLocationObservable()
                .startWith((UserLocation) null)
                .compose(ObservableExtensions.<UserLocation>behaviorRefCount());

        final Observable<ResponseOrError<SignResponse>> responseOrErrorObservable = mProceedSubject
                .withLatestFrom(mLocationObservable, new Func2<Object, UserLocation, UserLocation>() {
                    @Override
                    public UserLocation call(Object o, UserLocation location) {
                        return location;
                    }
                })
                .switchMap(new Func1<UserLocation, Observable<EmailSignupRequest>>() {
                    @Override
                    public Observable<EmailSignupRequest> call(final UserLocation location) {
                        return Observable.zip(
                                mNameSubject.filter(getNotEmptyFunc1()),
                                mEmailSubject.filter(getNotEmptyFunc1()),
                                mPasswordSubject.filter(Functions1.neg(getLessThan6AndMoreThan20CharsFunc1())),
                                new Func3<String, String, String, EmailSignupRequest>() {
                                    @Override
                                    public EmailSignupRequest call(String name, String email, String password) {
                                        return new EmailSignupRequest(name, email, password, LoginUser.loginUser(location));
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
                        userPreferences.setLoggedIn(signResponse.getAccessToken(), signResponse.getRefreshToken());
                        userPreferences.saveUserAsJson(signResponse.getUser());
                    }
                });

        mErrorObservable = responseOrErrorObservable.compose(ResponseOrError.<SignResponse>onlyError());
    }

    @NonNull
    private Func1<? super CharSequence, Boolean> getNotEmptyFunc1() {
        return Functions1.neg(Functions1.isNullOrEmpty());
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

    @NonNull
    public Observer<String> getEmailObserver() {
        return mEmailSubject;
    }

    @NonNull
    public Observer<String> getPasswordObserver() {
        return mPasswordSubject;
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
        return mNameSubject;
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
