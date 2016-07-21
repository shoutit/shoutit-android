package com.shoutit.app.android.view.admins;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.AdminRequest;
import com.shoutit.app.android.api.model.ApiMessageResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.model.AdminsPointer;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class AdminsFragmentPresenter extends BaseProfileListPresenter {
    private static final int PAGE_SIZE = 20;

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;
    @Nonnull
    private final PublishSubject<String> removeAdminSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> addAdminSubject = PublishSubject.create();
    @Nonnull
    private final Observable<ApiMessageResponse> successRemoveAdminObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<ApiMessageResponse> successAddAdminObservable;

    public AdminsFragmentPresenter(@Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                   @Nonnull ProfilesDao profilesDao,
                                   @Nullable String placeholderText,
                                   @Nonnull @UiScheduler Scheduler uiScheduler,
                                   @Nonnull @NetworkScheduler Scheduler networkScheduler,
                                   @Nonnull UserPreferences userPreferences,
                                   @Nonnull ApiService apiService) {
        super(listeningHalfPresenter, uiScheduler, placeholderText, userPreferences);

        final String pageUserName = userPreferences.getPageUserName().get();

        daoObservable = Observable.just(profilesDao.getAdminsDao(new AdminsPointer(User.ME, PAGE_SIZE)))
                .compose(ObservableExtensions.behaviorRefCount());
        
        final Observable<ResponseOrError<ApiMessageResponse>> removeAdminObservable = removeAdminSubject
                .switchMap(userId -> apiService.deleteAdmin(pageUserName, new AdminRequest(userId))
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable()))
                .compose(ObservableExtensions.behaviorRefCount());

        successRemoveAdminObservable = removeAdminObservable.compose(ResponseOrError.onlySuccess())
                .doOnNext(responseBody -> refreshData());

        final Observable<ResponseOrError<ApiMessageResponse>> addAdminObservable = addAdminSubject
                .switchMap(userId -> apiService.addAdmin(pageUserName, new AdminRequest(userId))
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable())).compose(ObservableExtensions.behaviorRefCount());

        successAddAdminObservable = addAdminObservable
                .compose(ResponseOrError.onlySuccess())
                .doOnNext(responseBody -> refreshData());

        progressObservable = Observable.merge(
                removeAdminObservable.map(Functions1.returnFalse()),
                addAdminObservable.map(Functions1.returnFalse()),
                removeAdminSubject.map(Functions1.returnTrue()),
                addAdminSubject.map(Functions1.returnTrue()));

        errorObservable = Observable.merge(
                removeAdminObservable.compose(ResponseOrError.onlyError()),
                addAdminObservable.compose(ResponseOrError.onlyError()));

        init();
    }

    @Nonnull
    @Override
    public Observable<Boolean> getProgressObservable() {
        return super.getProgressObservable().mergeWith(progressObservable);
    }

    @Nonnull
    @Override
    public Observable<Throwable> getErrorObservable() {
        return super.getErrorObservable().mergeWith(errorObservable);
    }

    @NonNull
    @Override
    protected Observable<BaseProfileListDao> getDaoObservable() {
        return daoObservable;
    }

    @Nonnull
    public Observable<ApiMessageResponse> getSuccessRemoveAdminObservable() {
        return successRemoveAdminObservable;
    }

    @Nonnull
    public Observable<ApiMessageResponse> getSuccessAddAdminObservable() {
        return successAddAdminObservable;
    }

    public void removeAdmin(@Nonnull String selectedAdminId) {
        removeAdminSubject.onNext(selectedAdminId);
    }

    public void addAdmin(String selectedAdminId) {
        addAdminSubject.onNext(selectedAdminId);
    }
}
