package com.shoutit.app.android.view.settings.account.password;

import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Strings;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ChangePasswordRequest;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.functions.Func4;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class ChangePasswordPresenter {

    @Nonnull
    private final Observable<Boolean> hadUserPasswordSetObservable;
    @Nonnull
    private final BehaviorSubject<String> oldPasswordSubject = BehaviorSubject.create();
    @Nonnull
    private final BehaviorSubject<String> newPasswordSubject = BehaviorSubject.create();
    @Nonnull
    private final BehaviorSubject<String> newPasswordVerifySubject = BehaviorSubject.create();
    @Nonnull
    private final PublishSubject<Object> confirmClickSubject = PublishSubject.create();

    @Nonnull
    private final Observable<Boolean> oldPasswordEmptyError;
    @Nonnull
    private final Observable<Boolean> passwordError;
    @Nonnull
    private final Observable<Boolean> passwordVerifyError;
    @Nonnull
    private final Observable<Boolean> passwordsDoNotMatchError;
    @Nonnull
    private final Observable<Throwable> requestErrorObservable;
    @Nonnull
    private final Observable<Object> requestSuccessObservable;
    @Nonnull
    private final Scheduler uiScheduler;


    @Inject
    public ChangePasswordPresenter(@Nonnull final ApiService apiService,
                                   @Nonnull UserPreferences userPreferences,
                                   @Nonnull @UiScheduler final Scheduler uiScheduler,
                                   @Nonnull @NetworkScheduler final Scheduler networkScheduler) {
        this.uiScheduler = uiScheduler;

        oldPasswordSubject.onNext(null);
        newPasswordSubject.onNext(null);
        newPasswordVerifySubject.onNext(null);

        hadUserPasswordSetObservable = Observable.just(userPreferences.getUser())
                .map(User::isPasswordSet)
                .compose(ObservableExtensions.<Boolean>behaviorRefCount());

        /** Errors **/
        oldPasswordEmptyError = Observable.combineLatest(
                oldPasswordSubject,
                hadUserPasswordSetObservable,
                new Func2<String, Boolean, Boolean>() {
                    @Override
                    public Boolean call(String oldPass, Boolean hadPasswordSet) {
                        return hadPasswordSet && Strings.isNullOrEmpty(oldPass);
                    }
                })
                .startWith(false)
                .compose(ObservableExtensions.<Boolean>behaviorRefCount());


        passwordError = newPasswordSubject
                .map(isLessThan6OrMoreThan20CharsFunc1())
                .compose(ObservableExtensions.<Boolean>behaviorRefCount());

        passwordVerifyError = newPasswordVerifySubject
                .map(isLessThan6OrMoreThan20CharsFunc1())
                .compose(ObservableExtensions.<Boolean>behaviorRefCount());

        passwordsDoNotMatchError = Observable.combineLatest(
                newPasswordSubject,
                newPasswordVerifySubject,
                new Func2<String, String, Boolean>() {
                    @Override
                    public Boolean call(String password, String passwordVerify) {
                        return password == null || !password.equals(passwordVerify);
                    }
                })
                .compose(ObservableExtensions.<Boolean>behaviorRefCount());

        final Observable<Boolean> isAnyErrorObservable = Observable.combineLatest(oldPasswordEmptyError, passwordError, passwordVerifyError,
                passwordsDoNotMatchError, new Func4<Boolean, Boolean, Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean error1, Boolean error2, Boolean error3, Boolean error4) {
                        return error1 || error2 || error3 || error4;
                    }
                });


        /** Combines data **/
        final Observable<ChangePasswordRequest> lastCredentialsObservable = Observable.combineLatest(
                oldPasswordSubject.startWith((String) null),
                newPasswordSubject,
                newPasswordVerifySubject,
                new Func3<String, String, String, ChangePasswordRequest>() {
                    @Override
                    public ChangePasswordRequest call(String oldPassword, String newPassword, String newPasswordVerification) {
                        return new ChangePasswordRequest(oldPassword, newPassword, newPasswordVerification);
                    }
                });


        /** Request **/
        final Observable<ResponseOrError<ResponseBody>> requestObservable = confirmClickSubject
                .withLatestFrom(isAnyErrorObservable, new Func2<Object, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Object o, Boolean isError) {
                        return isError;
                    }
                })
                .filter(Functions1.isFalse())
                .switchMap(new Func1<Boolean, Observable<ChangePasswordRequest>>() {
                    @Override
                    public Observable<ChangePasswordRequest> call(Boolean ignore) {
                        return lastCredentialsObservable.take(1);
                    }
                })
                .switchMap(new Func1<ChangePasswordRequest, Observable<ResponseOrError<ResponseBody>>>() {
                    @Override
                    public Observable<ResponseOrError<ResponseBody>> call(ChangePasswordRequest changePasswordRequest) {
                        return apiService.changePassword(changePasswordRequest)
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ResponseBody>>behaviorRefCount());

        requestErrorObservable = requestObservable
                .compose(ResponseOrError.<ResponseBody>onlyError())
                .observeOn(uiScheduler);

        requestSuccessObservable = requestObservable
                .compose(ResponseOrError.<ResponseBody>onlySuccess())
                .map(Functions1.toObject())
                .observeOn(uiScheduler);
    }

    @NonNull
    private Func1<? super CharSequence, Boolean> isLessThan6OrMoreThan20CharsFunc1() {
        return new Func1<CharSequence, Boolean>() {
            @Override
            public Boolean call(CharSequence charSequence) {
                if (charSequence == null) {
                    return true;
                }
                final int length = charSequence.length();
                return length < 6 || length > 20;
            }
        };
    }

    @Nonnull
    public Observer<String> getOldPasswordObserver() {
        return RxMoreObservers.ignoreCompleted(oldPasswordSubject);
    }

    @Nonnull
    public Observer<String> getPasswordObserver() {
        return RxMoreObservers.ignoreCompleted(newPasswordSubject);
    }

    @Nonnull
    public Observer<String> getPasswordConfirmObserver() {
        return RxMoreObservers.ignoreCompleted(newPasswordVerifySubject);
    }

    @Nonnull
    public Observable<Boolean> getHadUserPasswordSetObservable() {
        return hadUserPasswordSetObservable.observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<Object> getRequestSuccessObservable() {
        return requestSuccessObservable;
    }

    @Nonnull
    public Observable<Boolean> getOldPasswordEmptyError() {
        return confirmClickSubject
                .flatMap(MoreFunctions1.returnObservableFirst(oldPasswordEmptyError))
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<Boolean> getPasswordError() {
        return confirmClickSubject
                .flatMap(MoreFunctions1.returnObservableFirst(passwordError))
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<Boolean> getPasswordConfirmError() {
        return confirmClickSubject
                .flatMap(MoreFunctions1.returnObservableFirst(passwordVerifyError))
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<Boolean> getPasswordsDoNotMatchError() {
        return confirmClickSubject
                .flatMap(MoreFunctions1.returnObservableFirst(passwordsDoNotMatchError))
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observer<Object> getConfirmClickObserver() {
        return RxMoreObservers.ignoreCompleted(confirmClickSubject);
    }

    @Nonnull
    public Observable<Throwable> getRequestErrorObservable() {
        return requestErrorObservable;
    }
}
