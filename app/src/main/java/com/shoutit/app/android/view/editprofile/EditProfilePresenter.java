package com.shoutit.app.android.view.editprofile;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

public class EditProfilePresenter {

    private final PublishSubject<String> nameObserver = PublishSubject.create();
    private final PublishSubject<String> usernameObserver = PublishSubject.create();
    private final PublishSubject<String> bioObserver = PublishSubject.create();
    private final PublishSubject<String> websiteObserver = PublishSubject.create();

    @Nonnull
    private final Observable<User> userObservable;

    @Inject
    public EditProfilePresenter(@Nonnull UserPreferences userPreferences,
                                @Nonnull ApiService apiService) {

        userObservable = userPreferences
                .getUserObservable()
                .filter(Functions1.isNotNull())
                .compose(ObservableExtensions.<User>behaviorRefCount());

    }

    @Nonnull
    public Observable<User> getUserObservable() {
        return userObservable;
    }

    @Nonnull
    public Observer<String> getNameObserver() {
        return RxMoreObservers.ignoreCompleted(nameObserver);
    }

    @Nonnull
    public Observer<String> getUserNameObserver() {
        return RxMoreObservers.ignoreCompleted(usernameObserver);
    }

    @Nonnull
    public Observer<String> getBioObserver() {
        return RxMoreObservers.ignoreCompleted(bioObserver);
    }

    @Nonnull
    public Observer<String> getWebsiteObserver() {
        return RxMoreObservers.ignoreCompleted(websiteObserver);
    }
}
