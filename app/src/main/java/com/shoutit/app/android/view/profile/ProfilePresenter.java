package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.utils.PreferencesHelper;
import com.shoutit.app.android.view.profile.myprofile.MyProfileHalfPresenter;
import com.shoutit.app.android.view.profile.userprofile.UserProfileHalfPresenter;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class ProfilePresenter {
    private static final int SHOUTS_PAGE_SIZE = 4;

    @Nonnull
    private Observable<String> avatarObservable;
    @Nonnull
    private Observable<String> coverUrlObservable;
    @Nonnull
    private Observable<String> toolbarTitleObservable;
    @Nonnull
    private Observable<String> toolbarSubtitleObservable;
    @Nonnull
    private Observable<Boolean> progressObservable;
    @Nonnull
    private Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private Observable<Throwable> errorObservable;
    @Nonnull
    private Observable<String> shareObservable;

    @Nonnull
    private final PublishSubject<String> showAllShoutsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> shareInitSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<String> profileToOpenSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<Throwable> errorsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> actionOnlyForLoggedInUserSubject = PublishSubject.create();

    @Nonnull
    protected final String userName;
    @Nonnull
    protected final Context context;
    @Nonnull
    protected final UserPreferences userPreferences;
    @Nonnull
    protected final ApiService apiService;
    @Nonnull
    private final ProfilesDao profilesDao;
    @Nonnull
    private final MyProfileHalfPresenter myProfilePresenter;
    @Nonnull
    private final UserProfileHalfPresenter userProfilePresenter;
    @Nonnull
    private final PreferencesHelper preferencesHelper;
    protected boolean isUserLoggedIn;

    public ProfilePresenter(@Nonnull final String userName,
                            @Nonnull final ShoutsDao shoutsDao,
                            @Nonnull @ForActivity final Context context,
                            @Nonnull UserPreferences userPreferences,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull ApiService apiService,
                            @Nonnull ProfilesDao profilesDao,
                            @Nonnull MyProfileHalfPresenter myProfilePresenter,
                            @Nonnull UserProfileHalfPresenter userProfilePresenter,
                            @Nonnull PreferencesHelper preferencesHelper) {
        this.userName = userName;
        this.context = context;
        this.userPreferences = userPreferences;
        this.apiService = apiService;
        this.profilesDao = profilesDao;
        this.myProfilePresenter = myProfilePresenter;
        this.userProfilePresenter = userProfilePresenter;
        this.preferencesHelper = preferencesHelper;
        this.isUserLoggedIn = userPreferences.isNormalUser();

        /** User **/
        final Observable<ResponseOrError<User>> userRequestObservable = profilesDao.getProfileObservable(userName)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        final Observable<ResponseOrError<User>> userObservable = Observable.merge(
                userRequestObservable,
                userProfilePresenter.getUserUpdatesObservable())
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        final Observable<User> userSuccessObservable = userObservable.compose(ResponseOrError.<User>onlySuccess());

        /** Header Data **/
        avatarObservable = userSuccessObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getImage();
                    }
                });

        coverUrlObservable = userSuccessObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getCover();
                    }
                });

        toolbarTitleObservable = userSuccessObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return String.format("%1$s %2$s", user.getFirstName(), user.getLastName());
                    }
                });

        toolbarSubtitleObservable = userSuccessObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return context.getResources().getString(R.string.profile_subtitle, user.getListenersCount());
                    }
                });


        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> shoutsObservable =
                shoutsDao.getUserShoutObservable(new UserShoutsPointer(SHOUTS_PAGE_SIZE, userName))
                        .observeOn(uiScheduler)
                        .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> shoutsSuccessResponse = shoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<Shout>>() {
                    @Override
                    public List<Shout> call(ShoutsResponse response) {
                        return response.getShouts();
                    }
                })
                .filter(Functions1.isNotNull())
                .map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items = Lists.transform(shouts, new Function<Shout, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(@Nullable Shout shout) {
                                return new ShoutAdapterItem(shout, context, shoutSelectedSubject);
                            }
                        });

                        return ImmutableList.copyOf(items);
                    }
                });

        /** All adapter items **/
        allAdapterItemsObservable = Observable.combineLatest(
                userSuccessObservable,
                shoutsSuccessResponse.startWith(ImmutableList.<List<BaseAdapterItem>>of()),
                combineAdapterItems());


        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsObservable),
                ResponseOrError.transform(userRequestObservable),
                ResponseOrError.transform(userObservable)))
                .mergeWith(errorsSubject)
                .mergeWith(userProfilePresenter.getErrorObservable())
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        /** Progress **/
        progressObservable = Observable.merge(userRequestObservable, errorObservable)
                .map(Functions1.returnFalse())
                .startWith(true)
                .observeOn(uiScheduler);

        /** Share **/
        shareObservable = shareInitSubject
                .withLatestFrom(userSuccessObservable, new Func2<Object, User, String>() {
                    @Override
                    public String call(Object o, User user) {
                        return user.getWebUrl();
                    }
                });
    }

    @NonNull
    protected Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>> combineAdapterItems() {
        return new Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
            @Override
            public List<BaseAdapterItem> call(User user, List<BaseAdapterItem> shouts) {
                final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                if (user.isOwner()) {
                    builder.add(myProfilePresenter.getUserNameAdapterItem(user))
                            .add(myProfilePresenter.getThreeIconsAdapterItem(user));
                } else {
                    builder.add(userProfilePresenter.getUserNameAdapterItem(user))
                            .add(userProfilePresenter.getThreeIconsAdapterItem(user, isUserLoggedIn));
                }

                builder.add(new ProfileAdapterItems.UserInfoAdapterItem(user));

                final List<BaseAdapterItem> items = new ArrayList<>();

                if (user.getPages() != null && !user.getPages().isEmpty()) {
                    builder.add(new HeaderAdapterItem(getSectionHeaderTitle(user)));

                    for (int i = 0; i < user.getPages().size(); i++) {
                        items.add(getSectionAdapterItemForPosition(i, user, user.getPages()));
                    }
                    builder.addAll(items);
                }

                if (user.getAdmins() != null && !user.getAdmins().isEmpty()) {
                    builder.add(new HeaderAdapterItem(getSectionHeaderTitle(user)));

                    for (int i = 0; i < user.getAdmins().size(); i++) {
                        items.add(getSectionAdapterItemForPosition(i, user, user.getAdmins()));
                    }
                    builder.addAll(items);
                }

                if (!shouts.isEmpty()) {
                    builder.add(new HeaderAdapterItem(user.isOwner() ?
                            myProfilePresenter.getShoutsHeaderTitle() : userProfilePresenter.getShoutsHeaderTitle(user)))
                            .addAll(shouts)
                            .add(new ProfileAdapterItems.SeeAllUserShoutsAdapterItem(
                                    showAllShoutsSubject, user.getUsername()));
                }

                return builder.build();
            }
        };
    }

    private <T extends ProfileType> ProfileAdapterItems.ProfileSectionAdapterItem getSectionAdapterItemForPosition(int position, User user, List<T> items) {
        if (position == 0) {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(true, false, user, items.get(position),
                    userProfilePresenter.getSectionItemListenObserver(), profileToOpenSubject, actionOnlyForLoggedInUserSubject, isUserLoggedIn, items.size() == 1);
        } else if (position == items.size() - 1) {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(false, true, user, items.get(position),
                    userProfilePresenter.getSectionItemListenObserver(), profileToOpenSubject, actionOnlyForLoggedInUserSubject, isUserLoggedIn, false);
        } else {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(false, false, user, items.get(position),
                    userProfilePresenter.getSectionItemListenObserver(), profileToOpenSubject, actionOnlyForLoggedInUserSubject, isUserLoggedIn, false);
        }
    }

    @Nonnull
    public MyProfileHalfPresenter getMyProfilePresenter() {
        return myProfilePresenter;
    }

    @Nonnull
    public UserProfileHalfPresenter getUserProfilePresenter() {
        return userProfilePresenter;
    }

    @Nonnull
    public String getSectionHeaderTitle(User user) {
        switch (user.getType()) {
            case ProfileType.USER:
                return preferencesHelper.isMyProfile(user.getUsername()) ? context.getString(R.string.profile_my_pages) :
                        context.getString(R.string.profile_user_pages, user.getName()).toUpperCase();
            case ProfileType.PAGE:
                return context.getString(R.string.profile_page_admins, user.getName()).toUpperCase();
            default:
                throw new RuntimeException("Unknown profile type: " + user.getType());
        }
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @Nonnull
    public Observable<Object> getActionOnlyForLoggedInUserObservable() {
        return actionOnlyForLoggedInUserSubject;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public Observable<String> getAvatarObservable() {
        return avatarObservable;
    }

    @Nonnull
    public Observable<String> getCoverUrlObservable() {
        return coverUrlObservable;
    }

    @Nonnull
    public Observable<String> getToolbarTitleObservable() {
        return toolbarTitleObservable;
    }

    @Nonnull
    public Observable<String> getToolbarSubtitleObservable() {
        return toolbarSubtitleObservable;
    }

    @Nonnull
    public Observable<String> getShareObservable() {
        return shareObservable;
    }

    @Nonnull
    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedSubject;
    }

    @Nonnull
    public Observable<String> getProfileToOpenObservable() {
        return profileToOpenSubject;
    }

    @Nonnull
    public Observer<Object> getShareInitObserver() {
        return shareInitSubject;
    }

    public void refreshProfile() {
        profilesDao.getRefreshProfileObserver(userName).onNext(null);
    }

    public static class UserWithItemToListen {
        @Nonnull
        private final User currentProfileUser;
        @Nonnull
        private final ProfileType profileToListen;

        public UserWithItemToListen(@Nonnull User user, @Nonnull ProfileType profileToListen) {
            this.currentProfileUser = user;
            this.profileToListen = profileToListen;
        }

        @Nonnull
        public User getCurrentProfileUser() {
            return currentProfileUser;
        }

        @Nonnull
        public ProfileType getProfileToListen() {
            return profileToListen;
        }
    }
}
