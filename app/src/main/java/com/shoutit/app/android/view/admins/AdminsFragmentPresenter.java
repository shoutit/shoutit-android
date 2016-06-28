package com.shoutit.app.android.view.admins;

import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PreferencesHelper;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.subjects.PublishSubject;

public class AdminsFragmentPresenter extends BaseProfileListPresenter {

    @Nonnull
    private final Observable<BaseProfileListDao> daoObservable;
    @Nonnull
    private final PublishSubject<String> removeAdminSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> addAdminSubject = PublishSubject.create();
    @Nonnull
    private final Observable<ResponseBody> successRemoveAdminObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final PreferencesHelper preferencesHelper;
    @Nonnull
    private final Observable<ResponseBody> successAddAdminObservable;

    public AdminsFragmentPresenter(@Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                   @Nonnull ProfilesDao profilesDao,
                                   @Nonnull String placeholderText,
                                   @Nonnull @UiScheduler Scheduler uiScheduler,
                                   @Nonnull @NetworkScheduler Scheduler networkScheduler,
                                   @Nonnull UserPreferences userPreferences,
                                   @Nonnull PreferencesHelper preferencesHelper,
                                   @Nonnull ApiService apiService) {
        super(listeningHalfPresenter, uiScheduler, placeholderText, userPreferences);
        this.preferencesHelper = preferencesHelper;

        daoObservable = Observable.just(userPreferences.getPageUserName())
                .filter(MoreFunctions1.isPresent())
                .map(pageUserName -> profilesDao.getAdminsDao(pageUserName.get()))
                .compose(ObservableExtensions.behaviorRefCount());
        
        final Observable<ResponseOrError<ResponseBody>> removeAdminObservable = removeAdminSubject
                .switchMap(userName -> apiService.deleteAdmin(userName)
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable()))
                .compose(ObservableExtensions.behaviorRefCount());

        successRemoveAdminObservable = removeAdminObservable.compose(ResponseOrError.onlySuccess())
                .doOnNext(responseBody -> refreshData());

        final Observable<ResponseOrError<ResponseBody>> addAdminObservable = addAdminSubject
                .switchMap(userName -> apiService.addAdmin(userName)
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable())).compose(ObservableExtensions.behaviorRefCount());

        successAddAdminObservable = addAdminObservable
                .compose(ResponseOrError.onlySuccess())
                .doOnNext(responseBody -> refreshData());

        progressObservable = Observable.merge(
                removeAdminObservable.map(Functions1.returnFalse()),
                addAdminObservable.map(Functions1.returnTrue()),
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
    public Observable<ResponseBody> getSuccessRemoveAdminObservable() {
        return successRemoveAdminObservable;
    }

    @Nonnull
    public Observable<ResponseBody> getSuccessAddAdminObservable() {
        return successAddAdminObservable;
    }

    public void removeAdmin(@Nonnull String userName) {
        if (preferencesHelper.isMyProfile(userName)) {
            profileSelectedSubject.onNext(userName);
        } else {
            removeAdminSubject.onNext(userName);
        }
    }

    public void addAdmin(String selectedAdminUserName) {
        addAdminSubject.onNext(selectedAdminUserName);
    }
}
