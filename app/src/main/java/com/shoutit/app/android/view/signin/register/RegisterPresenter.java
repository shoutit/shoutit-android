package com.shoutit.app.android.view.signin.register;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.login.EmailLoginRequest;
import com.shoutit.app.android.api.model.login.LoginUser;
import com.shoutit.app.android.rx.CoarseLocationObservable;
import com.shoutit.app.android.utils.MoreFunctions1;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class RegisterPresenter {

    interface CoarseLocationObservableProvider {

        CoarseLocationObservableProvider DEFAULT = new CoarseLocationObservableProvider() {
            @Override
            public Observable<Location> get(@NonNull Context context) {
                return CoarseLocationObservable.get(context);
            }
        };

        Observable<Location> get(@NonNull Context context);
    }

    private final Observable<SignResponse> mSuccessObservable;
    private final Observable<Throwable> mErrorObservable;
    private final BehaviorSubject<String> mEmailSubject = BehaviorSubject.create();
    private final BehaviorSubject<String> mPasswordSubject = BehaviorSubject.create();
    private final PublishSubject<Object> mProceedSubject = PublishSubject.create();
    private final Observable<String> mPasswordEmpty;
    private final Observable<Location> mLocationObservable;

    @Inject
    public RegisterPresenter(@NonNull final ApiService apiService,
                             @NonNull Context context,
                             @NonNull CoarseLocationObservableProvider coarseLocationObservableProvider,
                             @NonNull final UserPreferences userPreferences,
                             @NonNull @NetworkScheduler final Scheduler networkScheduler,
                             @NonNull @UiScheduler final Scheduler uiScheduler) {
        mLocationObservable = coarseLocationObservableProvider
                .get(context)
                .startWith((Location) null)
                .compose(ObservableExtensions.<Location>behaviorRefCount());

        final Observable<ResponseOrError<SignResponse>> responseOrErrorObservable = mProceedSubject
                .switchMap(MoreFunctions1.returnObservable(mLocationObservable.first()))
                .flatMap(new Func1<Location, Observable<EmailLoginRequest>>() {
                    @Override
                    public Observable<EmailLoginRequest> call(final Location location) {
                        return Observable.zip(mEmailSubject, mPasswordSubject.filter(getNotEmptyFunc1()), new Func2<String, String, EmailLoginRequest>() {
                            @Override
                            public EmailLoginRequest call(String email, String password) {
                                return new EmailLoginRequest(email, password, location != null ? new LoginUser(location.getLatitude(), location.getLongitude()) : null);
                            }
                        });
                    }
                })
                .flatMap(new Func1<EmailLoginRequest, Observable<SignResponse>>() {
                    @Override
                    public Observable<SignResponse> call(EmailLoginRequest loginRequest) {
                        return apiService.login(loginRequest)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler);
                    }
                })
                .compose(ResponseOrError.<SignResponse>toResponseOrErrorObservable())
                .compose(ObservableExtensions.<ResponseOrError<SignResponse>>behaviorRefCount());

        mPasswordEmpty = mPasswordSubject.filter(Functions1.isNullOrEmpty());

        mSuccessObservable = responseOrErrorObservable
                .compose(ResponseOrError.<SignResponse>onlySuccess())
                .doOnNext(new Action1<SignResponse>() {
                    @Override
                    public void call(SignResponse signResponse) {
                        userPreferences.setLoggedIn(signResponse.getAccessToken(), signResponse.getRefreshToken());
                    }
                });

        mErrorObservable = responseOrErrorObservable.compose(ResponseOrError.<SignResponse>onlyError());
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
    public Observable<Location> getLocationObservable() {
        return mLocationObservable;
    }
}
