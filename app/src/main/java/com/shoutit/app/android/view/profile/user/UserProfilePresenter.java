package com.shoutit.app.android.view.profile.user;

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
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ListenResponse;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BaseProfileListDao;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.model.AdminsPointer;
import com.shoutit.app.android.model.PagesPointer;
import com.shoutit.app.android.model.ReportBody;
import com.shoutit.app.android.model.Stats;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.utils.BookmarkHelper;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.PreferencesHelper;
import com.shoutit.app.android.utils.PromotionHelper;
import com.shoutit.app.android.utils.pusher.PusherHelperHolder;
import com.shoutit.app.android.view.profile.BaseProfileAdapterItems;
import com.shoutit.app.android.view.profile.user.myprofile.MyProfileHalfPresenter;
import com.shoutit.app.android.view.profile.user.userprofile.UserProfileHalfPresenter;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.subsearch.SubSearchActivity;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func4;
import rx.subjects.PublishSubject;

public class UserProfilePresenter implements ProfilePresenter {
    private static final int SHOUTS_PAGE_SIZE = 4;
    private static final int ADMINS_AND_PAGES_PAGE_SIZE = 3;

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
    private final Observable<Object> refreshUserShoutsObservable;
    @Nonnull
    private final Observable<Object> userUpdatesObservable;
    @Nonnull
    private final Observable<Object> listeningObservable;
    @Nonnull
    private final Observable<Object> refreshPagesOrAdminsObservable;

    @Nonnull
    private final PublishSubject<String> showAllShoutsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> shareInitSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<ProfileType> profileToOpenSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> actionOnlyForLoggedInUserSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> webUrlClickedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> searchMenuItemClickSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> reportSubmitSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshPagesOrAdmins = PublishSubject.create();

    @Nonnull
    private final String userName;
    private ShoutsDao shoutsDao;
    @Nonnull
    private final Context context;
    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final ProfilesDao profilesDao;
    @Nonnull
    private final MyProfileHalfPresenter myProfilePresenter;
    @Nonnull
    private final UserProfileHalfPresenter userProfilePresenter;
    @Nonnull
    private final PreferencesHelper preferencesHelper;
    @Nonnull
    private final ListeningHalfPresenter listeningHalfPresenter;
    @NonNull
    private final BookmarkHelper mBookmarkHelper;
    @Nullable
    private String loggedInUserName;
    private boolean isNormalUser;
    private final boolean isMyProfile;

    public UserProfilePresenter(@Nonnull String username,
                                @Nonnull final ShoutsDao shoutsDao,
                                @Nonnull @ForActivity final Context context,
                                @Nonnull final UserPreferences userPreferences,
                                @Nonnull @UiScheduler final Scheduler uiScheduler,
                                @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                @Nonnull final ProfilesDao profilesDao,
                                @Nonnull MyProfileHalfPresenter myProfilePresenter,
                                @Nonnull UserProfileHalfPresenter userProfilePresenter,
                                @Nonnull PreferencesHelper preferencesHelper,
                                @Nonnull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter,
                                @Nonnull ListeningHalfPresenter listeningHalfPresenter,
                                @Nonnull final ApiService apiService,
                                @NonNull PusherHelperHolder pusherHelper,
                                @NonNull BookmarksDao bookmarksDao,
                                @NonNull BookmarkHelper bookmarkHelper) {
        this.shoutsDao = shoutsDao;
        this.context = context;
        this.userPreferences = userPreferences;
        this.profilesDao = profilesDao;
        this.myProfilePresenter = myProfilePresenter;
        this.userProfilePresenter = userProfilePresenter;
        this.preferencesHelper = preferencesHelper;
        this.listeningHalfPresenter = listeningHalfPresenter;
        mBookmarkHelper = bookmarkHelper;
        this.isNormalUser = userPreferences.isNormalUser();

        final BaseProfile loggedInUser = userPreferences.getUserOrPage();
        if (loggedInUser != null) {
            loggedInUserName = loggedInUser.getUsername();
        }

        isMyProfile = preferencesHelper.isMyProfile(username);
        if (isMyProfile) {
            username = BaseProfile.ME;
        }
        this.userName = username;

        /** User **/
        final Observable<ResponseOrError<User>> userRequestObservable = profilesDao.getProfileObservable(userName)
                .compose(ResponseOrError.map(new Func1<BaseProfile, User>() {
                    @Override
                    public User call(BaseProfile baseProfile) {
                        return (User) baseProfile;
                    }
                }))
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        userUpdatesObservable = userProfilePresenter.getUserUpdatesObservable()
                .map(userResponseOrError -> {
                    final ResponseOrError<BaseProfile> baseProfileResponseOrError;
                    if (userResponseOrError.isData()) {
                        baseProfileResponseOrError = ResponseOrError.fromData(userResponseOrError.data());
                    } else {
                        baseProfileResponseOrError = ResponseOrError.fromError(userResponseOrError.error());
                    }
                    profilesDao.getProfileDao(userName)
                            .updatedProfileLocallyObserver()
                            .onNext(baseProfileResponseOrError);
                    return null;
                });

        final Observable<User> userSuccessObservable = userRequestObservable.compose(ResponseOrError.<User>onlySuccess())
                .doOnNext(user -> {
                    if (User.ME.equals(userName)) {
                        userPreferences.setUserOrPage(user);
                    }
                })
                .compose(ObservableExtensions.<User>behaviorRefCount());

        final Observable<Boolean> isUserProfile = userSuccessObservable
                .map(User::isUser)
                .compose(ObservableExtensions.behaviorRefCount());

        /** User Pages **/
        final Observable<ProfilesDao.PagesDao> pagesDao = userSuccessObservable
                .filter(User::isUser)
                .map(user -> profilesDao.getPagesDao(new PagesPointer(user.getUsername(), ADMINS_AND_PAGES_PAGE_SIZE)));

        final Observable<ResponseOrError<ProfilesListResponse>> pagesRequest = pagesDao
                .switchMap(pagesDao1 -> pagesDao1.getProfilesObservable()
                        .observeOn(uiScheduler))
                .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ProfilesListResponse> successPagesRequest = pagesRequest
                .compose(ResponseOrError.onlySuccess());

        final Observable<List<BaseProfile>> pagesList = successPagesRequest
                .map(ProfilesListResponse::getResults);

        /** Pages Admins **/
        final Observable<ProfilesDao.AdminsDao> adminsDao = userSuccessObservable
                .filter(user -> !user.isUser())
                .map(user -> profilesDao.getAdminsDao(new AdminsPointer(user.getUsername(), ADMINS_AND_PAGES_PAGE_SIZE)));

        final Observable<ResponseOrError<ProfilesListResponse>> adminsRequest = adminsDao
                .switchMap(dao -> dao.getProfilesObservable()
                        .observeOn(uiScheduler))
                .compose(ObservableExtensions.behaviorRefCount());

        final Observable<ProfilesListResponse> successAdminsRequest = adminsRequest
                .compose(ResponseOrError.onlySuccess());

        final Observable<List<BaseProfile>> adminsList = successAdminsRequest
                .map(ProfilesListResponse::getResults);

        /** Pages or Admins Listening **/
        final Observable<ProfilesListResponse> pagesOrAdminsRequestObservable = isUserProfile
                .switchMap(isUserProfile1 -> isUserProfile1 ? successPagesRequest : successAdminsRequest);

        final Observable<BaseProfileListDao> pagesOrAdminsDaoObservable = isUserProfile
                .switchMap(isUserProfile1 -> isUserProfile1 ? pagesDao : adminsDao);

        listeningObservable = listeningHalfPresenter
                .listeningObservable(pagesOrAdminsRequestObservable)
                .switchMap(updatedProfile -> pagesOrAdminsDaoObservable
                        .map(dao -> {
                            dao.updatedProfileLocallyObserver().onNext(updatedProfile);
                            return null;
                        }));

        refreshPagesOrAdminsObservable = this.refreshPagesOrAdmins
                .flatMap(ignore -> pagesOrAdminsDaoObservable)
                .map(dao -> {
                    dao.getRefreshSubject().onNext(null);
                    return null;
                });


        /** Header Data **/
        avatarObservable = userSuccessObservable
                .map(User::getImage);

        coverUrlObservable = userSuccessObservable
                .map(User::getCover);

        toolbarTitleObservable = userSuccessObservable
                .map(User::getName);

        toolbarSubtitleObservable = userSuccessObservable
                .map(user -> context.getResources().getString(R.string.profile_subtitle, user.getListenersCount()));


        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> shoutsObservable =
                shoutsDao.getUserShoutObservable(new UserShoutsPointer(SHOUTS_PAGE_SIZE, userName))
                        .observeOn(uiScheduler)
                        .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> shoutsSuccessResponse = shoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(ShoutsResponse::getShouts)
                .filter(Functions1.isNotNull())
                .map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items = Lists.transform(shouts, new Function<Shout, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(Shout shout) {
                                final BookmarkHelper.ShoutItemBookmarkHelper shoutItemBookmarkHelper = bookmarkHelper.getShoutItemBookmarkHelper();
                                return new ShoutAdapterItem(shout, false, false, context, shoutSelectedSubject,
                                        PromotionHelper.promotionInfoOrNull(shout),
                                        bookmarksDao.getBookmarkForShout(shout.getId(), shout.isBookmarked()),
                                        shoutItemBookmarkHelper.getObserver(), shoutItemBookmarkHelper.getEnableObservable());
                            }
                        });

                        return ImmutableList.copyOf(items);
                    }
                });

        /** All adapter items **/
        allAdapterItemsObservable = Observable.combineLatest(
                userSuccessObservable,
                shoutsSuccessResponse.startWith(ImmutableList.<BaseAdapterItem>of()),
                pagesList.startWith(ImmutableList.<BaseProfile>of()),
                adminsList.startWith(ImmutableList.<BaseProfile>of()),
                combineAdapterItems());

        /** Report **/
        final Observable<ResponseOrError<Response<Object>>> reportRequestObservable = reportSubmitSubject
                .withLatestFrom(userSuccessObservable, (reportText, user) -> new BothParams<>(user.getId(), reportText))
                .flatMap(userIdWithReportText -> {
                    final String userId = userIdWithReportText.param1();
                    final String reportText = userIdWithReportText.param2();

                    return apiService.report(ReportBody.forProfile(userId, reportText))
                            .compose(ResponseOrError.<Response<Object>>toResponseOrErrorObservable())
                            .subscribeOn(networkScheduler)
                            .observeOn(uiScheduler);
                })
                .compose(ObservableExtensions.<ResponseOrError<Response<Object>>>behaviorRefCount());

        reportSuccessObservable = reportRequestObservable
                .compose(ResponseOrError.<Response<Object>>onlySuccess())
                .map(Functions1.toObject());


        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsObservable),
                ResponseOrError.transform(reportRequestObservable),
                ResponseOrError.transform(userRequestObservable),
                ResponseOrError.transform(pagesRequest),
                ResponseOrError.transform(adminsRequest)))
                .mergeWith(userProfilePresenter.getErrorObservable())
                .mergeWith(listeningHalfPresenter.getErrorSubject())
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        /** Progress **/
        progressObservable = Observable.merge(userRequestObservable, errorObservable)
                .map(Functions1.returnFalse())
                .startWith(true)
                .observeOn(uiScheduler);

        /** Menu actions **/
        shareObservable = shareInitSubject
                .withLatestFrom(userSuccessObservable, (o, user) -> user.getWebUrl());

        searchMenuItemClickObservable = searchMenuItemClickSubject
                .withLatestFrom(userSuccessObservable, (o, user) -> SubSearchActivity.newIntent(context,
                        SearchPresenter.SearchType.PROFILE, user.getUsername(),
                        user.getName()));

        refreshUserShoutsObservable = shoutsGlobalRefreshPresenter
                .getShoutsGlobalRefreshObservable()
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        shoutsDao.getUserShoutsDao(new UserShoutsPointer(SHOUTS_PAGE_SIZE, userName))
                                .getRefreshObserver()
                                .onNext(null);
                        return null;
                    }
                });
        notificationsUnreadObservable = getNotificationsUnreadObservable();
    }

    private Observable<Integer> getNotificationsUnreadObservable() {
        if (isMyProfile) {
            return userPreferences.getPageOrUserObservable()
                    .filter(Functions1.isNotNull())
                    .map(BaseProfile::getUnreadNotificationsCount);
        } else {
            return Observable.never();
        }
    }

    private Func4<User, List<BaseAdapterItem>, List<BaseProfile>, List<BaseProfile>, List<BaseAdapterItem>> combineAdapterItems() {
        return (user, shouts, pages, admins) -> {
            final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

            if (user.isOwner()) {
                builder.add(myProfilePresenter.getUserNameAdapterItem(user, notificationsUnreadObservable))
                        .add(myProfilePresenter.getThreeIconsAdapterItem(user));
            } else {
                builder.add(userProfilePresenter.getUserNameAdapterItem(user))
                        .add(userProfilePresenter.getThreeIconsAdapterItem(user, isNormalUser));
            }

            builder.add(new ProfileAdapterItems.UserInfoAdapterItem(user, webUrlClickedSubject));

            final List<BaseAdapterItem> pagesItems = createSectionAdapterItems(pages, user);
            final List<BaseAdapterItem> adminsItems = createSectionAdapterItems(admins, user);

            builder.addAll(pagesItems)
                    .addAll(adminsItems);

            if (!shouts.isEmpty()) {
                builder.add(new HeaderAdapterItem(user.isOwner() ?
                        myProfilePresenter.getShoutsHeaderTitle() : userProfilePresenter.getShoutsHeaderTitle(user)))
                        .addAll(shouts)
                        .add(new BaseProfileAdapterItems.SeeAllUserShoutsAdapterItem(
                                showAllShoutsSubject, user.getUsername()));
            }

            return builder.build();
        };
    }

    @Nonnull
    private List<BaseAdapterItem> createSectionAdapterItems(@Nonnull List<BaseProfile> pagesOrAdmins,
                                                            @Nonnull User currentProfile) {

        if (!pagesOrAdmins.isEmpty()) {
            final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.<BaseAdapterItem>builder();

            builder.add(new HeaderAdapterItem(getSectionHeaderTitle(currentProfile)));

            for (int i = 0; i < pagesOrAdmins.size(); i++) {
                builder.add(getSectionAdapterItemForPosition(i, pagesOrAdmins, loggedInUserName));
            }

            return builder.build();
        } else {
            return ImmutableList.<BaseAdapterItem>of();
        }
    }

    private BaseProfileAdapterItems.ProfileSectionAdapterItem getSectionAdapterItemForPosition(int position,
                                                                                               List<BaseProfile> items, @Nullable String loggedInUserName) {
        if (position == 0) {
            return new BaseProfileAdapterItems.ProfileSectionAdapterItem(true, false, items.get(position),
                    listeningHalfPresenter.getListenProfileSubject(), profileToOpenSubject,
                    actionOnlyForLoggedInUserSubject, loggedInUserName, isNormalUser, items.size() == 1);
        } else if (position == items.size() - 1) {
            return new BaseProfileAdapterItems.ProfileSectionAdapterItem(false, true, items.get(position),
                    listeningHalfPresenter.getListenProfileSubject(), profileToOpenSubject,
                    actionOnlyForLoggedInUserSubject, loggedInUserName, isNormalUser, false);
        } else {
            return new BaseProfileAdapterItems.ProfileSectionAdapterItem(false, false, items.get(position),
                    listeningHalfPresenter.getListenProfileSubject(), profileToOpenSubject,
                    actionOnlyForLoggedInUserSubject, loggedInUserName, isNormalUser, false);
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
    public String getSectionHeaderTitle(BaseProfile baseProfile) {
        switch (baseProfile.getType()) {
            case ProfileType.USER:
                return preferencesHelper.isMyProfile(baseProfile.getUsername()) ? context.getString(R.string.profile_my_pages) :
                        context.getString(R.string.profile_user_pages, baseProfile.getFirstName()).toUpperCase();
            case ProfileType.PAGE:
                return context.getString(R.string.profile_page_admins, baseProfile.getFirstName()).toUpperCase();
            default:
                throw new RuntimeException("Unknown profile type: " + baseProfile.getType());
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
    public Observable<ProfileType> getProfileToOpenObservable() {
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
    @NonNull
    public Observable<String> getBookmarkSuccesMessageObservable() {
        return mBookmarkHelper.getBookmarkSuccessMessage();
    }

    @Override
    public void refreshProfile() {
        profilesDao.getRefreshProfileObserver(userName).onNext(null);
        shoutsDao.getUserShoutsDao(new UserShoutsPointer(SHOUTS_PAGE_SIZE, userName))
                .getRefreshObserver().onNext(null);
        refreshPagesOrAdmins.onNext(null);
    }

    @Nonnull
    @Override
    public Observer<String> sendReportObserver() {
        return reportSubmitSubject;
    }

    @Nonnull
    @Override
    public Observable<ListenResponse> getListenSuccessObservable() {
        return userProfilePresenter.getListenSuccessObservable()
                .mergeWith(listeningHalfPresenter.getListenSuccess());
    }

    @Nonnull
    public Observable<Object> getRefreshUserShoutsObservable() {
        return refreshUserShoutsObservable;
    }

    @Nonnull
    @Override
    public Observable<ListenResponse> getUnListenSuccessObservable() {
        return userProfilePresenter.getUnListenSuccessObservable()
                .mergeWith(listeningHalfPresenter.getUnListenSuccess());
    }

    @Nonnull
    public Observable<Object> getListeningObservable() {
        return listeningObservable;
    }

    @Nonnull
    public Observable<Object> getUserUpdatesObservable() {
        return userUpdatesObservable;
    }

    public void onSearchMenuItemClicked() {
        searchMenuItemClickSubject.onNext(null);
    }

    @Nonnull
    public Observable<Object> getRefreshPagesOrAdminsObservable() {
        return refreshPagesOrAdminsObservable;
    }
}
