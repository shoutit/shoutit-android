package com.shoutit.app.android.view.profile.userprofile;

import android.content.Context;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Admin;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.profile.ChatInfo;
import com.shoutit.app.android.view.profile.ProfileAdapterItems;
import com.shoutit.app.android.view.profile.UserOrPageProfilePresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class UserProfileHalfPresenter {

    @Nonnull
    private final PublishSubject<Object> moreMenuOptionClickedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> actionOnlyForLoggedInUserSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<ChatInfo> onChatIconClickedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<User> onListenActionClickedSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<UserOrPageProfilePresenter.UserWithItemToListen> sectionItemListenSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<Throwable> errorSubject = PublishSubject.create();

    @Nonnull
    private final Observable<ResponseOrError<User>> userUpdatesObservable;
    @Nonnull
    private final Context context;

    @Inject
    public UserProfileHalfPresenter(@Nonnull @UiScheduler final Scheduler uiScheduler,
                                    @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                    @Nonnull final ApiService apiService,
                                    @Nonnull @ForActivity Context context) {
        this.context = context;

        final Observable<ResponseOrError<User>> userWithUpdatedSectionItems = sectionItemListenSubject
                .throttleFirst(1, TimeUnit.SECONDS)
                .switchMap(new Func1<UserOrPageProfilePresenter.UserWithItemToListen, Observable<ResponseOrError<User>>>() {
                    @Override
                    public Observable<ResponseOrError<User>> call(final UserOrPageProfilePresenter.UserWithItemToListen userWithItemToListen) {
                        final String userName = userWithItemToListen.getProfileToListen().getUsername();
                        final boolean isListeningToProfile = userWithItemToListen.getProfileToListen().isListening();

                        Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                        if (isListeningToProfile) {
                            listenRequestObservable = apiService.unlistenProfile(userName)
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        } else {
                            listenRequestObservable = apiService.listenProfile(userName)
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        }

                        return listenRequestObservable
                                .map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<User>>() {
                                    @Override
                                    public ResponseOrError<User> call(ResponseOrError<ResponseBody> response) {
                                        if (response.isData()) {
                                            return ResponseOrError.fromData(updateUserWithChangedSectionItem(userWithItemToListen));
                                        } else {
                                            errorSubject.onNext(new Throwable());
                                            // On error return current user in order to select/deselect already deselected/selected item to listenProfile
                                            return ResponseOrError.fromData(userWithItemToListen.getCurrentProfileUser());
                                        }
                                    }
                                });
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        final Observable<ResponseOrError<User>> updatedUserWithListeningToProfile = onListenActionClickedSubject
                .throttleFirst(1, TimeUnit.SECONDS)
                .switchMap(new Func1<User, Observable<ResponseOrError<User>>>() {
                    @Override
                    public Observable<ResponseOrError<User>> call(final User user) {
                        final Observable<ResponseOrError<ResponseBody>> request;
                        if (user.isListening()) {
                             request = apiService.unlistenProfile(user.getUsername())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        } else {
                            request = apiService.listenProfile(user.getUsername())
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        }

                        return request.map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<User>>() {
                            @Override
                            public ResponseOrError<User> call(ResponseOrError<ResponseBody> response) {
                                if (response.isData()) {
                                    return ResponseOrError.fromData(user.getListenedProfile());
                                } else {
                                    errorSubject.onNext(new Throwable());
                                    // On error return current user in order to select/deselect already deselected/selected 'listenProfile' icon
                                    return ResponseOrError.fromData(user);
                                }
                            }
                        });
                    }
                });

        userUpdatesObservable = Observable.merge(
                userWithUpdatedSectionItems,
                updatedUserWithListeningToProfile);
    }

    @Nonnull
    private User updateUserWithChangedSectionItem(@Nonnull UserOrPageProfilePresenter.UserWithItemToListen userWithItemToListen) {
        final List<Page> pages = userWithItemToListen.getCurrentProfileUser().getPages();
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getUsername().equals(userWithItemToListen.getProfileToListen().getUsername())) {
                final Page pageToUpdate = pages.get(i);
                final Page updatedPage = Page.withIsListening(pageToUpdate, !pageToUpdate.isListening());
                final List<Page> updatedPages = new ArrayList<>(pages);
                updatedPages.set(i, updatedPage);

                return User.userWithUpdatedPages(userWithItemToListen.getCurrentProfileUser(), updatedPages);
            }
        }

        final List<Admin> admins = userWithItemToListen.getCurrentProfileUser().getAdmins();
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getUsername().equals(userWithItemToListen.getProfileToListen().getUsername())) {
                final Admin adminToUpdate = admins.get(i);
                final Admin updatedAdmin = Admin.withIsListening(adminToUpdate, !adminToUpdate.isListening());
                final List<Admin> updatedAdmins = new ArrayList<>(admins);
                updatedAdmins.set(i, updatedAdmin);

                return User.userWithUpdatedAdmins(userWithItemToListen.getCurrentProfileUser(), updatedAdmins);
            }
        }

        return userWithItemToListen.getCurrentProfileUser();
    }

    public ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull User user) {
        return new ProfileAdapterItems.UserNameAdapterItem(user, moreMenuOptionClickedSubject);
    }

    public ProfileAdapterItems.ThreeIconsAdapterItem getThreeIconsAdapterItem(@Nonnull User user, boolean isNormalUser) {
        return new ProfileAdapterItems.UserThreeIconsAdapterItem(user, isNormalUser,
                actionOnlyForLoggedInUserSubject, onChatIconClickedSubject, onListenActionClickedSubject);
    }

    @Nonnull
    public Observable<ResponseOrError<User>> getUserUpdatesObservable() {
        return userUpdatesObservable;
    }

    public String getShoutsHeaderTitle(User user) {
        return context.getString(R.string.profile_user_shouts, user.getFirstName()).toUpperCase();
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorSubject;
    }

    @Nonnull
    public PublishSubject<Object> getMoreMenuOptionClickedSubject() {
        return moreMenuOptionClickedSubject;
    }

    @Nonnull
    public Observable<Object> getActionOnlyForLoggedInUserObservable() {
        return actionOnlyForLoggedInUserSubject;
    }

    @Nonnull
    public PublishSubject<ChatInfo> getOnChatIconClickedSubject() {
        return onChatIconClickedSubject;
    }

    @Nonnull
    public Observer<UserOrPageProfilePresenter.UserWithItemToListen> getSectionItemListenObserver() {
        return sectionItemListenSubject;
    }
}
