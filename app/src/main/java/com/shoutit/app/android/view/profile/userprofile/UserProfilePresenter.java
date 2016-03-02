package com.shoutit.app.android.view.profile.userprofile;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.view.profile.ProfileAdapterItems;
import com.shoutit.app.android.view.profile.ProfilePresenter;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class UserProfilePresenter extends ProfilePresenter {

    @Nonnull
    private final PublishSubject<Object> moreMenuOptionClickedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> onChatIconClickedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<User> onListenActionClickedSubject = PublishSubject.create();

    @Nonnull
    private final Observable<ResponseOrError<User>> userObservable;

    public UserProfilePresenter(@Nonnull String userName, @Nonnull ShoutsDao shoutsDao,
                                @Nonnull @ForActivity Context context, @Nonnull UserPreferences userPreferences,
                                @Nonnull @UiScheduler final Scheduler uiScheduler,
                                @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                @Nonnull final ApiService apiService) {
        super(userName, shoutsDao, context, userPreferences, uiScheduler, networkScheduler, apiService);

        final Observable<ResponseOrError<User>> userRequestObservable = apiService.getProfile(userName)
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<User>toResponseOrErrorObservable())
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        final Observable<ResponseOrError<User>> updatedUserWithListening = onListenActionClickedSubject
                .throttleFirst(2, TimeUnit.SECONDS)
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
                                    return ResponseOrError.fromData(User.listenedUser(user, !user.isListening()));
                                } else {
                                    errorsSubject.onNext(new Throwable());
                                    // On error return current user in order to select/deselect already deselected/selected 'listenProfile' icon
                                    return ResponseOrError.fromData(user);
                                }
                            }
                        });
                    }
                });

        userObservable = Observable.merge(
                userRequestObservable,
                updatedUserWithListening);

        initPresenter();
    }

    @Override
    protected ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull User user) {
        return new UserNameAdapterItem(user, moreMenuOptionClickedSubject);
    }

    @Override
    protected BaseAdapterItem getThreeIconsAdapterItem(@Nonnull User user) {
        return new OtherUserThreeIconsAdapterItem(user);
    }

    @Nonnull
    @Override
    protected Observable<ResponseOrError<User>> getUserObservable() {
        return userObservable;
    }

    @Override
    protected String getShoutsHeaderTitle(User user) {
        return context.getString(R.string.shout_user_shouts_header, user.getName()).toUpperCase();
    }

    @Nonnull
    public PublishSubject<Object> getMoreMenuOptionClickedSubject() {
        return moreMenuOptionClickedSubject;
    }

    @Nonnull
    public PublishSubject<Object> getOnChatIconClickedSubject() {
        return onChatIconClickedSubject;
    }

    public class UserNameAdapterItem extends ProfileAdapterItems.NameAdapterItem {

        @Nonnull
        private final Observer<Object> moreMenuOptionClickedObserver;

        public UserNameAdapterItem(@Nonnull User user, @NonNull Observer<Object> moreMenuOptionClickedObserver) {
            super(user);
            this.moreMenuOptionClickedObserver = moreMenuOptionClickedObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.NameAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.NameAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }

        public void onMoreMenuOptionClicked() {
            moreMenuOptionClickedObserver.onNext(null);
        }
    }

    public class OtherUserThreeIconsAdapterItem extends ProfileAdapterItems.ThreeIconsAdapterItem {

        public OtherUserThreeIconsAdapterItem(@Nonnull User user) {
            super(user);
        }

        public void onChatActionClicked() {
            onChatIconClickedSubject.onNext(user.getUsername());
        }

        public void onListenActionClicked() {
            onListenActionClickedSubject.onNext(user);
        }
    }
}
