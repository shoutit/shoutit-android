package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.util.LogTransformer;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public abstract class ProfilePresenter {
    private static final int SHOUTS_PAGE_SIZE = 4;

    @Nonnull
    private final Observable<String> avatarObservable;
    @Nonnull
    private final Observable<String> coverUrlObservable;
    @Nonnull
    private final Observable<String> toolbarTitleObservable;
    @Nonnull
    private final Observable<String> toolbarSubtitleObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private final Observable<Throwable> shoutsErrorsResponse;
    @Nonnull
    private final Observable<String> shareObservable;

    @Nonnull
    private final PublishSubject<String> showAllShoutsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshProfileSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> shareInitSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<String> sectionItemSelectedSubject = PublishSubject.create();
    @Nonnull
    protected final PublishSubject<String> sectionItemListenSubject = PublishSubject.create();

    @Nonnull
    protected final Context context;
    @Nonnull
    protected final UserPreferences userPreferences;
    protected boolean isMyProfile;

    public ProfilePresenter(@Nonnull final String userName,
                            @Nonnull final ShoutsDao shoutsDao,
                            @Nonnull @ForActivity final Context context,
                            @Nonnull UserPreferences userPreferences,
                            @Nonnull @UiScheduler Scheduler uiScheduler) {
        this.context = context;
        this.userPreferences = userPreferences;
        isMyProfile = userName.equals(userPreferences.getUser().getUsername());

        /** User **/
        final Observable<ResponseOrError<User>> userObservable = getUserObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<User>>behaviorRefCount());

        final Observable<User> successUserObservable = userObservable
                .compose(ResponseOrError.<User>onlySuccess());

        /** Header Data **/
        avatarObservable = successUserObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getImage();
                    }
                });

        coverUrlObservable = successUserObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getCover();
                    }
                });

        toolbarTitleObservable = successUserObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return String.format("%1$s %2$s", user.getFirstName(), user.getLastName());
                    }
                });

        toolbarSubtitleObservable = successUserObservable
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
                successUserObservable,
                shoutsSuccessResponse.startWith(ImmutableList.<List<BaseAdapterItem>>of()),
                combineAdapterItems())
                .compose(LogTransformer.<List<BaseAdapterItem>>transformer("lol", "allAdapterItemsObservable"));


        /** Errors **/
        shoutsErrorsResponse = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsObservable),
                ResponseOrError.transform(userObservable)))
                .filter(Functions1.isNotNull())
                .compose(LogTransformer.<Throwable>transformer("lol", "shoutsErrorsResponse"));

        /** Progress **/
        progressObservable = Observable.merge(userObservable, shoutsErrorsResponse)
                .map(Functions1.returnFalse())
                .startWith(true);

        /** Share **/
        shareObservable = shareInitSubject
                .withLatestFrom(successUserObservable, new Func2<Object, User, String>() {
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

                builder.add(getUserNameAdapterItem(user))
                        .add(getThreeIconsAdapterItem(user))
                        .add(new ProfileAdapterItems.UserInfoAdapterItem(user));

                final List<BaseAdapterItem> items = new ArrayList<>();
                if (!user.getPages().isEmpty()) {
                    builder.add(new HeaderAdapterItem(getSectionHeaderTitle(user)));

                    for (int i = 0; i < user.getPages().size(); i++) {
                        items.add(getSectionAdapterItemForPosition(i, user.getPages(), isMyProfile));
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

    private <T extends ProfileType> ProfileAdapterItems.ProfileSectionAdapterItem getSectionAdapterItemForPosition(int position, List<T> items, boolean isMyProfile) {
        if (position == 0) {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(true, false, items.get(position), sectionItemSelectedSubject, sectionItemListenSubject, isMyProfile);
        } else if (position == items.size() - 1) {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(false, true, items.get(position), sectionItemSelectedSubject, sectionItemListenSubject, isMyProfile);
        } else {
            return new ProfileAdapterItems.ProfileSectionAdapterItem<>(false, false, items.get(position), sectionItemSelectedSubject, sectionItemListenSubject, isMyProfile);
        }
    }

    protected abstract ProfileAdapterItems.UserNameAdapterItem getUserNameAdapterItem(@Nonnull User user);

    protected abstract BaseAdapterItem getThreeIconsAdapterItem(@Nonnull User user);

    @Nonnull
    protected abstract Observable<ResponseOrError<User>> getUserObservable();

    public String getSectionHeaderTitle(User user) {
        switch (user.getType()) {
            case ProfileType.PROFILE:
                return isMyProfile ? context.getString(R.string.profile_my_pages) :
                        context.getString(R.string.profile_user_pages, user.getName()).toUpperCase();
            case ProfileType.PAGE:
                return context.getString(R.string.profile_page_admins, user.getName()).toUpperCase();
            default:
                throw new RuntimeException("Unknown profile type");
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
    public Observable<Throwable> getShoutsErrorsResponse() {
        return shoutsErrorsResponse;
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
    public PublishSubject<String> getSectionItemSelectedSubject() {
        return sectionItemSelectedSubject;
    }

    @Nonnull
    public PublishSubject<String> getSectionItemListenSubject() {
        return sectionItemListenSubject;
    }
}
