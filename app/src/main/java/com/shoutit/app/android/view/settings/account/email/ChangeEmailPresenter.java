package com.shoutit.app.android.view.settings.account.email;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.UpdateUserRequest;
import com.shoutit.app.android.api.model.User;
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
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class ChangeEmailPresenter {

    @Nonnull
    private final Observable<Boolean> wrongEmailErrorObservable;
    @Nonnull
    private final Observable<Throwable> apiErrorObservable;
    @Nonnull
    private final Observable<Object> successObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;

    @Nonnull
    private final PublishSubject<Object> confirmClickSubject = PublishSubject.create();
    @Nonnull
    private final BehaviorSubject<String> emailSubject = BehaviorSubject.create();

    @Inject
    public ChangeEmailPresenter(@Nonnull final ApiService apiService,
                                @Nonnull @UiScheduler final Scheduler uiScheduler,
                                @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                @Nonnull final UserPreferences userPreferences) {

        final PublishSubject<Boolean> progressSubject = PublishSubject.create();

        final Observable<Boolean> isEmailCorrectObservable = emailSubject.startWith((String) null)
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String email) {
                        return Validators.isEmailValid(email);
                    }
                })
                .compose(ObservableExtensions.<Boolean>behaviorRefCount());


        final Observable<ResponseOrError<User>> requestObservable = confirmClickSubject
                .withLatestFrom(isEmailCorrectObservable, new Func2<Object, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Object o, Boolean isEmailValid) {
                        return isEmailValid;
                    }
                })
                .filter(Functions1.isTrue())
                .lift(MoreOperators.callOnNext(progressSubject))
                .switchMap(new Func1<Boolean, Observable<ResponseOrError<User>>>() {
                    @Override
                    public Observable<ResponseOrError<User>> call(Boolean ignore) {
                        return apiService.updateUser(new UpdateUserRequest(emailSubject.getValue()))
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler)
                                .compose(ResponseOrError.<User>toResponseOrErrorObservable());
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        successObservable = requestObservable.compose(ResponseOrError.<User>onlySuccess())
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        userPreferences.saveUserAsJson(user);
                    }
                })
                .map(Functions1.toObject());


        /** Errors **/
        apiErrorObservable = requestObservable.compose(ResponseOrError.<User>onlyError());

        wrongEmailErrorObservable = confirmClickSubject
                .flatMap(MoreFunctions1.returnObservableFirst(isEmailCorrectObservable.map(Functions1.neg())));

        /** Progress **/
        progressObservable = Observable.merge(progressSubject,
                successObservable.map(Functions1.returnFalse()),
                apiErrorObservable.map(Functions1.returnFalse()))
                .startWith(false);
    }

    @Nonnull
    public Observable<Boolean> getWrongEmailErrorObservable() {
        return wrongEmailErrorObservable;
    }

    @Nonnull
    public Observable<Throwable> getApiErrorObservable() {
        return apiErrorObservable;
    }

    @Nonnull
    public Observable<Object> getSuccessObservable() {
        return successObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observer<Object> getConfirmClickSubject() {
        return RxMoreObservers.ignoreCompleted(confirmClickSubject);
    }

    @Nonnull
    public Observer<String> getEmailObserver() {
        return RxMoreObservers.ignoreCompleted(emailSubject);
    }
}
