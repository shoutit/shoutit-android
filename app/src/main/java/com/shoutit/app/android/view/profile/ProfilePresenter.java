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
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public abstract class ProfilePresenter {
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
    protected final PublishSubject<UserWithItemToListen> sectionItemListenSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<Throwable> errorsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> actionOnlyForLoggedInUserSubject = PublishSubject.create();

    @Nonnull
    protected final String userName;
    @Nonnull
    private final ShoutsDao shoutsDao;
    @Nonnull
    protected final Context context;
    @Nonnull
    protected final UserPreferences userPreferences;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    protected final ApiService apiService;
    protected boolean isMyProfile;
    protected boolean isUserLoggedIn;

    public ProfilePresenter(@Nonnull final String userName,
                            @Nonnull final ShoutsDao shoutsDao,
                            @Nonnull @ForActivity final Context context,
                            @Nonnull UserPreferences userPreferences,
                            boolean isMyProfile,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull @NetworkScheduler Scheduler networkScheduler,
                            @Nonnull ApiService apiService) {
        this.userName = userName;
        this.shoutsDao = shoutsDao;
        this.context = context;
        this.userPreferences = userPreferences;
        this.uiScheduler = uiScheduler;
        this.networkScheduler = networkScheduler;
        this.apiService = apiService;
        this.isMyProfile = isMyProfile;
        this.isUserLoggedIn = userPreferences.isNormalUser();
    }

    /** Must be called after child constructor is finished **/
    protected void initPresenter() {

        /** User **/
        final Observable<ResponseOrError<User>> userRequestObservable = getUserObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        final Observable<User> successUserRequestObservable = userRequestObservable
                .compose(ResponseOrError.<User>onlySuccess());

        final Observable<User> userWithUpdatedSectionItems = sectionItemListenSubject
                .throttleFirst(1, TimeUnit.SECONDS)
                .switchMap(new Func1<UserWithItemToListen, Observable<User>>() {
                    @Override
                    public Observable<User> call(final UserWithItemToListen userWithItemToListen) {
                        final String userName = userWithItemToListen.getProfileToListen().getUsername();
                        final boolean isListeningToProfile = userWithItemToListen.profileToListen.isListening();

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
                                .map(new Func1<ResponseOrError<ResponseBody>, User>() {
                                    @Override
                                    public User call(ResponseOrError<ResponseBody> response) {
                                        if (response.isData()) {
                                            return updateUserWithChangedPageItems(userWithItemToListen);
                                        } else {
                                            errorsSubject.onNext(new Throwable());
                                            // On error return current user in order to select/deselect already deselected/selected item to listenProfile
                                            return userWithItemToListen.getCurrentProfileUser();
                                        }
                                    }
                                });
                    }
                })
                .compose(ObservableExtensions.<User>behaviorRefCount());

        final Observable<User> userObservable = Observable.merge(
                successUserRequestObservable,
                userWithUpdatedSectionItems)
                .compose(ObservableExtensions.<User>behaviorRefCount());

        /** Header Data **/
        avatarObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getImage();
                    }
                });

        coverUrlObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getCover();
                    }
                });

        toolbarTitleObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return String.format("%1$s %2$s", user.getFirstName(), user.getLastName());
                    }
                });

        toolbarSubtitleObservable = userObservable
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
                userObservable,
                shoutsSuccessResponse.startWith(ImmutableList.<List<BaseAdapterItem>>of()),
                combineAdapterItems());


        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsObservable),
                ResponseOrError.transform(userRequestObservable)))
                .mergeWith(errorsSubject)
                .filter(Functions1.isNotNull());

        /** Progress **/
        progressObservable = Observable.merge(userRequestObservable, errorObservable)
                .map(Functions1.returnFalse())
                .startWith(true);

        /** Share **/
        shareObservable = shareInitSubject
                .withLatestFrom(userObservable, new Func2<Object, User, String>() {
                    @Override
                    public String call(Object o, User user) {
                        return user.getWebUrl();
                    }
                });
    }

    @Nonnull
    private User updateUserWithChangedPageItems(@Nonnull UserWithItemToListen userWithItemToListen) {
        final List<Page> pages = userWithItemToListen.getCurrentProfileUser().getPages();
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getUsername().equals(userWithItemToListen.profileToListen.getUsername())) {
                final Page pageToUpdate = pages.get(i);
                final Page updatedPage = Page.withIsListening(pageToUpdate, !pageToUpdate.isListening());
                final List<Page> updatedPages = new ArrayList<>(pages);
                updatedPages.set(i, updatedPage);

                return User.userWithUpdatedPages(userWithItemToListen.currentProfileUser, updatedPages);
            }
        }

        return userWithItemToListen.getCurrentProfileUser();
    }

    @NonNull
    protected Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>> combineAdapterItems() {
        return new Func2<User, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
            @Override
            public List<BaseAdapterItem> call(User user, List<BaseAdapterItem> shouts) {
                final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                builder.add(getUserNameAdapterItem(user))
                        .add(getThreeIconsAdapterItem(user))
                        .add(new ProfileAdapterItems.UserInfoAdapterItem(user));

                final List<BaseAdapterItem> items = new ArrayList<>();
                if (!user.getPages().isEmpty()) {
                    builder.add(new HeaderAdapterItem(getSectionHeaderTitle(user)));

                    for (int i = 0; i < user.getPages().size(); i++) {
                        items.add(getSectionAdapterItemForPosition(i, user, user.getPages(), isMyProfile));
                    }
                    builder.addAll(items);
                }

                if (!shouts.isEmpty()) {
                    builder.add(new HeaderAdapterItem(getShoutsHeaderTitle(user)))
                            .addAll(shouts)
                            .add(new ProfileAdapterItems.SeeAllUserShoutsAdapterItem(
                                    showAllShoutsSubject, user.getUsername()));
                }

                return builder.build();
            }
        };
    }

    private <T extends ProfileType> ProfileAdapterItems.ProfileSectionAdapterItem getSectionAdapterItemForPosition(int position, User user, List<T> items, boolean isMyProfile) {
        if (position == 0) {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(true, false, user, items.get(position),
                    sectionItemListenSubject, actionOnlyForLoggedInUserSubject, isMyProfile, isUserLoggedIn, items.size() == 1);
        } else if (position == items.size() - 1) {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(false, true, user, items.get(position),
                    sectionItemListenSubject, actionOnlyForLoggedInUserSubject, isMyProfile, isUserLoggedIn, false);
        } else {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(false, false, user, items.get(position),
                    sectionItemListenSubject, actionOnlyForLoggedInUserSubject, isMyProfile, isUserLoggedIn, false);
        }
    }

    protected abstract ProfileAdapterItems.NameAdapterItem getUserNameAdapterItem(@Nonnull User user);

    protected abstract BaseAdapterItem getThreeIconsAdapterItem(@Nonnull User user);

    @Nonnull
    protected abstract Observable<ResponseOrError<User>> getUserObservable();

    @Nonnull
    public String getSectionHeaderTitle(User user) {
        switch (user.getType()) {
            case ProfileType.USER:
                return isMyProfile ? context.getString(R.string.profile_my_pages) :
                        context.getString(R.string.profile_user_pages, user.getFirstName()).toUpperCase();
            case ProfileType.PAGE:
                return context.getString(R.string.profile_page_admins, user.getFirstName()).toUpperCase();
            default:
                throw new RuntimeException("Unknown profile type: " + user.getType());
        }
    }

    protected abstract String getShoutsHeaderTitle(User user);

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
