package com.shoutit.app.android.view.signin.login;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ResetPasswordRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;
import com.shoutit.app.android.api.model.login.LoginUser;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.view.signin.CoarseLocationObservableProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class LoginPresenter {

    private final Observable<SignResponse> mSuccessObservable;
    private final Observable<Throwable> mErrorObservable;
    private final BehaviorSubject<String> mEmailSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> mPasswordSubject = BehaviorSubject.create();
    private final PublishSubject<Object> mProceedSubject = PublishSubject.create();
    private final PublishSubject<Object> resetPasswordClickObserver = PublishSubject.create();
    private final PublishSubject<Boolean> progressSubject = PublishSubject.create();

    private final Observable<String> mPasswordEmpty;
    private final Observable<String> mEmailEmpty;
    private final Observable<Location> mLocationObservable;
    private final Observable<ResponseBody> resetPasswordSuccess;
    private final Observable<Object> resetPasswordEmptyEmail;
    private final Observable<Boolean> progressObservable;
    private final Observable<String> mEmailNotEmpty;
    private final Observable<String> mPasswordNotEmpty;

    @Inject
    public LoginPresenter(@NonNull final ApiService apiService,
                          @NonNull @ForActivity Context context,
                          @NonNull CoarseLocationObservableProvider coarseLocationObservableProvider,
                          @NonNull final UserPreferences userPreferences,
                          @NonNull @NetworkScheduler final Scheduler networkScheduler,
                          @NonNull @UiScheduler final Scheduler uiScheduler) {
        mLocationObservable = coarseLocationObservableProvider
                .get(context)
                .startWith((Location) null)
                .compose(ObservableExtensions.<Location>behaviorRefCount());

        // Login
        final Observable<ResponseOrError<SignResponse>> loginRequestObservable = mProceedSubject
                .withLatestFrom(mLocationObservable, new Func2<Object, Location, Location>() {
                    @Override
                    public Location call(Object o, Location location) {
                        return location;
                    }
                })
                .switchMap(new Func1<Location, Observable<EmailLoginRequest>>() {
                    @Override
                    public Observable<EmailLoginRequest> call(final Location location) {
                        return Observable
                                .zip(mEmailSubject.filter(getNotEmptyFunc1()), mPasswordSubject.filter(getNotEmptyFunc1()), new Func2<String, String, EmailLoginRequest>() {
                                    @Override
                                    public EmailLoginRequest call(String email, String password) {
                                        return new EmailLoginRequest(email, password, LoginUser.loginUser(location));
                                    }
                                })
                                .first();
                    }
                })
                .doOnNext(showProgressAction())
                .switchMap(new Func1<EmailLoginRequest, Observable<ResponseOrError<SignResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<SignResponse>> call(EmailLoginRequest loginRequest) {
                        return apiService.login(loginRequest)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<SignResponse>toResponseOrErrorObservable());
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<SignResponse>>behaviorRefCount());

        mPasswordEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mPasswordSubject)).filter(Functions1.isNullOrEmpty());
        mEmailEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mEmailSubject)).filter(Functions1.isNullOrEmpty());

        mPasswordNotEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mPasswordSubject)).filter(Functions1.neg(Functions1.isNullOrEmpty()));
        mEmailNotEmpty = mProceedSubject.flatMap(MoreFunctions1.returnObservableFirst(mEmailSubject)).filter(Functions1.neg(Functions1.isNullOrEmpty()));

        mSuccessObservable = loginRequestObservable
                .compose(ResponseOrError.<SignResponse>onlySuccess())
                .doOnNext(new Action1<SignResponse>() {
                    @Override
                    public void call(SignResponse signResponse) {
                        userPreferences.setLoggedIn(signResponse.getAccessToken(), signResponse.getRefreshToken());
                        userPreferences.saveUserAsJson(signResponse.getUser());
                    }
                });

        // Reset password
        final Observable<String> resetPasswordClickObservable = resetPasswordClickObserver
                .withLatestFrom(mEmailSubject, new Func2<Object, String, String>() {
                    @Override
                    public String call(Object ignore, String email) {
                        return email;
                    }
                });

        final Observable<ResponseOrError<ResponseBody>> resetPasswordRequestObservable = resetPasswordClickObservable
                .filter(MoreFunctions1.textNotEmpty())
                .doOnNext(showProgressAction())
                .switchMap(new Func1<String, Observable<ResponseOrError<ResponseBody>>>() {
                    @Override
                    public Observable<ResponseOrError<ResponseBody>> call(String email) {
                        return apiService.resetPassword(new ResetPasswordRequest(email))
                                .subscribeOn(networkScheduler)
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());

                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ResponseBody>>behaviorRefCount());

        resetPasswordEmptyEmail = resetPasswordClickObservable
                .filter(Functions1.neg(MoreFunctions1.textNotEmpty()))
                .map(Functions1.toObject());

        resetPasswordSuccess = resetPasswordRequestObservable
                .compose(ResponseOrError.<ResponseBody>onlySuccess())
                .observeOn(uiScheduler);


        // Errors
        mErrorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(loginRequestObservable),
                ResponseOrError.transform(resetPasswordRequestObservable)))
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        // Progress
        progressObservable = Observable.merge(
                progressSubject,
                mErrorObservable.map(Functions1.returnFalse()),
                loginRequestObservable.map(Functions1.returnFalse()),
                resetPasswordRequestObservable.map(Functions1.returnFalse()))
                .observeOn(uiScheduler);
    }

    @NonNull
    private Action1<Object> showProgressAction() {
        return new Action1<Object>() {
            @Override
            public void call(Object ignore) {
                progressSubject.onNext(true);
            }
        };
    }

    @NonNull
    private Func1<? super CharSequence, Boolean> getNotEmptyFunc1() {
        return Functions1.neg(Functions1.isNullOrEmpty());
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
    public Observable<String> getEmailEmpty() {
        return mEmailEmpty;
    }

    @NonNull
    public Observable<Location> getLocationObservable() {
        return mLocationObservable;
    }

    @Nonnull
    public Observer<Object> getResetPasswordClickObserver() {
        return resetPasswordClickObserver;
    }

    @Nonnull
    public Observable<ResponseBody> successResetPassword() {
        return resetPasswordSuccess;
    }

    @Nonnull
    public Observable<Object> resetPasswordEmptyEmail() {
        return resetPasswordEmptyEmail;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<String> getPasswordNotEmpty() {
        return mPasswordNotEmpty;
    }

    @Nonnull
    public Observable<String> getEmailNotEmpty() {
        return mEmailNotEmpty;
    }
}
