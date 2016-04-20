package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
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
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.model.Stats;
import com.shoutit.app.android.model.ReportBody;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.utils.PreferencesHelper;
import com.shoutit.app.android.utils.PusherHelper;
import com.shoutit.app.android.view.profile.myprofile.MyProfileHalfPresenter;
import com.shoutit.app.android.view.profile.userprofile.UserProfileHalfPresenter;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.subsearch.SubSearchActivity;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class UserOrPageProfilePresenter implements ProfilePresenter {
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
    private Observable<Intent> searchMenuItemClickObservable;
    @Nonnull
    private final Observable<Object> reportSuccessObservable;
    @Nonnull
    private final Observable<Integer> notificationsUnreadObservable;

    @Nonnull
    private final PublishSubject<String> showAllShoutsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> shareInitSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> profileToOpenSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Throwable> errorsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> actionOnlyForLoggedInUserSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> webUrlClickedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> searchMenuItemClickSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> reportSubmitSubject = PublishSubject.create();

    @Nonnull
    private final String userName;
    private ShoutsDao shoutsDao;
    @Nonnull
    private final Context context;
    @Nonnull
    private final ProfilesDao profilesDao;
    @Nonnull
    private final MyProfileHalfPresenter myProfilePresenter;
    @Nonnull
    private final UserProfileHalfPresenter userProfilePresenter;
    @Nonnull
    private final PreferencesHelper preferencesHelper;
    @Nullable
    private String loggedInUserName;
    private boolean isNormalUser;

    public UserOrPageProfilePresenter(@Nonnull final String userName,
                                      @Nonnull final ShoutsDao shoutsDao,
                                      @Nonnull @ForActivity final Context context,
                                      @Nonnull final UserPreferences userPreferences,
                                      @Nonnull @UiScheduler final Scheduler uiScheduler,
                                      @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                      @Nonnull ProfilesDao profilesDao,
                                      @Nonnull MyProfileHalfPresenter myProfilePresenter,
                                      @Nonnull UserProfileHalfPresenter userProfilePresenter,
                                      @Nonnull PreferencesHelper preferencesHelper,
                                      @Nonnull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter,
                                      @Nonnull final ApiService apiService,
                                      @NonNull PusherHelper pusherHelper) {
        this.userName = userName;
        this.shoutsDao = shoutsDao;
        this.context = context;
        this.profilesDao = profilesDao;
        this.myProfilePresenter = myProfilePresenter;
        this.userProfilePresenter = userProfilePresenter;
        this.preferencesHelper = preferencesHelper;
        this.isNormalUser = userPreferences.isNormalUser();

        final User loggedInUser = userPreferences.getUser();
        if (loggedInUser != null) {
            loggedInUserName = loggedInUser.getUsername();
        }

        /** User **/
        final Observable<ResponseOrError<User>> userRequestObservable = profilesDao.getProfileObservable(userName)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        userProfilePresenter.getUserUpdatesObservable()
                .subscribe(profilesDao.getProfileDao(userName).updatedProfileLocallyObserver());

        final Observable<User> userSuccessObservable = userRequestObservable.compose(ResponseOrError.<User>onlySuccess())
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (User.ME.equals(userName)) {
                            userPreferences.saveUserAsJson(user);
                        }
                    }
                });

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
                        return user.getName();
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

        /** Report **/
        final Observable<ResponseOrError<Response<Object>>> reportRequestObservable = reportSubmitSubject
                .withLatestFrom(userSuccessObservable, new Func2<String, User, BothParams<String, String>>() {
                    @Override
                    public BothParams<String, String> call(String reportText, User user) {
                        return new BothParams<>(user.getId(), reportText);
                    }
                })
                .flatMap(new Func1<BothParams<String, String>, Observable<ResponseOrError<Response<Object>>>>() {
                    @Override
                    public Observable<ResponseOrError<Response<Object>>> call(BothParams<String, String> userIdWithReportText) {
                        final String userId = userIdWithReportText.param1();
                        final String reportText = userIdWithReportText.param2();

                        return apiService.report(ReportBody.forProfile(userId, reportText))
                                .compose(ResponseOrError.<Response<Object>>toResponseOrErrorObservable())
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<Response<Object>>>behaviorRefCount());

        reportSuccessObservable = reportRequestObservable
                .compose(ResponseOrError.<Response<Object>>onlySuccess())
                .map(Functions1.toObject());


        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsObservable),
                ResponseOrError.transform(reportRequestObservable),
                ResponseOrError.transform(userRequestObservable)))
                .mergeWith(errorsSubject)
                .mergeWith(userProfilePresenter.getErrorObservable())
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        /** Progress **/
        progressObservable = Observable.merge(userRequestObservable, errorObservable)
                .map(Functions1.returnFalse())
                .startWith(true)
                .observeOn(uiScheduler);

        /** Menu actions **/
        shareObservable = shareInitSubject
                .withLatestFrom(userSuccessObservable, new Func2<Object, User, String>() {
                    @Override
                    public String call(Object o, User user) {
                        return user.getWebUrl();
                    }
                });

        searchMenuItemClickObservable = searchMenuItemClickSubject
                .withLatestFrom(userSuccessObservable, new Func2<Object, User, Intent>() {
                    @Override
                    public Intent call(Object o, User user) {
                        return SubSearchActivity.newIntent(context,
                                SearchPresenter.SearchType.PROFILE, user.getUsername(),
                                user.getName());
                    }
                });

        shoutsGlobalRefreshPresenter
                .getShoutsGlobalRefreshObservable()
                .subscribe(shoutsDao.getUserShoutsDao(new UserShoutsPointer(SHOUTS_PAGE_SIZE, userName)).getRefreshObserver());

        notificationsUnreadObservable = userPreferences.getUserObservable()
                .filter(Functions1.isNotNull())
                .map(new Func1<User, Integer>() {
                    @Override
                    public Integer call(User user) {
                        return user.getUnreadNotificationsCount();
                    }
                })
                .mergeWith(pusherHelper.getStatsObservable()
                        .map(new Func1<Stats, Integer>() {
                            @Override
                            public Integer call(Stats pusherStats) {
                                return pusherStats.getUnreadNotifications();
                            }
                        }));
    }

    @NonNull
    protected Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>> combineAdapterItems() {
        return new Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
            @Override
            public List<BaseAdapterItem> call(User user, List<BaseAdapterItem> shouts) {
                final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                if (user.isOwner()) {
                    builder.add(myProfilePresenter.getUserNameAdapterItem(user, notificationsUnreadObservable))
                            .add(myProfilePresenter.getThreeIconsAdapterItem(user));
                } else {
                    builder.add(userProfilePresenter.getUserNameAdapterItem(user))
                            .add(userProfilePresenter.getThreeIconsAdapterItem(user, isNormalUser));
                }

                builder.add(new ProfileAdapterItems.UserInfoAdapterItem(user, webUrlClickedSubject));

                final List<BaseAdapterItem> items = new ArrayList<>();

                if (user.getPages() != null && !user.getPages().isEmpty()) {
                    builder.add(new HeaderAdapterItem(getSectionHeaderTitle(user)));

                    for (int i = 0; i < user.getPages().size(); i++) {
                        items.add(getSectionAdapterItemForPosition(i, user, user.getPages(), loggedInUserName));
                    }
                    builder.addAll(items);
                }

                if (user.getAdmins() != null && !user.getAdmins().isEmpty()) {
                    builder.add(new HeaderAdapterItem(getSectionHeaderTitle(user)));

                    for (int i = 0; i < user.getAdmins().size(); i++) {
                        items.add(getSectionAdapterItemForPosition(i, user, user.getAdmins(), loggedInUserName));
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

    private <T extends ProfileType> ProfileAdapterItems.ProfileSectionAdapterItem getSectionAdapterItemForPosition(int position, User user, List<T> items, @Nullable String loggedInUserName) {
        if (position == 0) {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(true, false, user, items.get(position),
                    userProfilePresenter.getSectionItemListenObserver(), profileToOpenSubject, actionOnlyForLoggedInUserSubject, loggedInUserName, isNormalUser, items.size() == 1);
        } else if (position == items.size() - 1) {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(false, true, user, items.get(position),
                    userProfilePresenter.getSectionItemListenObserver(), profileToOpenSubject, actionOnlyForLoggedInUserSubject, loggedInUserName, isNormalUser, false);
        } else {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(false, false, user, items.get(position),
                    userProfilePresenter.getSectionItemListenObserver(), profileToOpenSubject, actionOnlyForLoggedInUserSubject, loggedInUserName, isNormalUser, false);
        }
    }

    @Nonnull
    public Observable<Object> getReportSuccessObservable() {
        return reportSuccessObservable;
    }

    @Nonnull
    public Observable<Intent> getSearchMenuItemClickObservable() {
        return searchMenuItemClickObservable;
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
                        context.getString(R.string.profile_user_pages, user.getFirstName()).toUpperCase();
            case ProfileType.PAGE:
                return context.getString(R.string.profile_page_admins, user.getFirstName()).toUpperCase();
            default:
                throw new RuntimeException("Unknown profile type: " + user.getType());
        }
    }

    @Nonnull
    @Override
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    @Override
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @Nonnull
    @Override
    public Observable<Object> getActionOnlyForLoggedInUserObservable() {
        return actionOnlyForLoggedInUserSubject;
    }

    @NonNull
    @Override
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @NonNull
    @Override
    public Observable<String> getAvatarObservable() {
        return avatarObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getCoverUrlObservable() {
        return coverUrlObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getToolbarTitleObservable() {
        return toolbarTitleObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getToolbarSubtitleObservable() {
        return toolbarSubtitleObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getShareObservable() {
        return shareObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedSubject;
    }

    @Nonnull
    @Override
    public Observable<String> getProfileToOpenObservable() {
        return profileToOpenSubject;
    }

    @Nonnull
    @Override
    public Observer<Object> getShareInitObserver() {
        return shareInitSubject;
    }

    @Nonnull
    public Observable<String> getWebUrlClickedObservable() {
        return webUrlClickedSubject.filter(Functions1.isNotNull());
    }

    @Nonnull
    @Override
    public Observable<Object> getMoreMenuOptionClickedSubject() {
        return userProfilePresenter.getMoreMenuOptionClickedSubject();
    }

    @Nonnull
    @Override
    public Observable<String> getSeeAllShoutsObservable() {
        return showAllShoutsSubject;
    }

    @Override
    public void refreshProfile() {
        profilesDao.getRefreshProfileObserver(userName).onNext(null);
        shoutsDao.getUserShoutsDao(new UserShoutsPointer(SHOUTS_PAGE_SIZE, userName))
                .getRefreshObserver().onNext(null);
    }

    @Nonnull
    @Override
    public Observer<String> sendReportObserver() {
        return reportSubmitSubject;
    }

    public void onSearchMenuItemClicked() {
        searchMenuItemClickSubject.onNext(null);
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
