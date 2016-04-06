package com.shoutit.app.android.view.shout;

import android.content.Context;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserIdentity;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.dao.UsersIdentityDao;
import com.shoutit.app.android.model.MobilePhoneResponse;
import com.shoutit.app.android.model.RelatedShoutsPointer;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.subjects.PublishSubject;

public class ShoutPresenter {

    private static final int USER_SHOUTS_PAGE_SIZE = 4;
    private static final int RELATED_SHOUTS_PAGE_SIZE = 6;
    private static final int MAX_RELATED_ITEMS = 6;

    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<String> titleObservable;
    private final Observable<String> usernameObservable;
    private final Observable<Boolean> isUserShoutOwnerObservable;
    private final Observable<ResponseOrError<UserIdentity>> userIdentityResponse;
    private final Observable<ResponseOrError<MobilePhoneResponse>> callErrorObservable;
    private final Observable<Boolean> hasMobilePhoneObservable;
    private final Observable<Response<Object>> deleteShoutResponseObservable;
    private final Observable<Boolean> showDeleteDialogObservable;
    private final PublishSubject<Object> deleteShoutSubject = PublishSubject.create();
    private final Observable<Response<Object>> reportShoutObservable;

    private Observable<String> identityUserObservable;

    private Observable<UserIdentity> successUserIdentity;
    private Observable<Throwable> failedUserIdentity;

    private PublishSubject<String> addToCartSubject = PublishSubject.create();
    private PublishSubject<String> onCategoryClickedSubject = PublishSubject.create();
    private PublishSubject<String> userShoutSelectedSubject = PublishSubject.create();
    private PublishSubject<String> relatedShoutSelectedSubject = PublishSubject.create();
    private PublishSubject<String> seeAllRelatedShoutSubject = PublishSubject.create();
    private PublishSubject<User> visitProfileSubject = PublishSubject.create();

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final PublishSubject<Object> callOrDeleteSubject = PublishSubject.create();
    private final PublishSubject<String> sendReportObserver = PublishSubject.create();

    @Inject
    public ShoutPresenter(@Nonnull final ShoutsDao shoutsDao,
                          @Nonnull final String shoutId,
                          @Nonnull @ForActivity final Context context,
                          @Nonnull @UiScheduler final Scheduler uiScheduler,
                          @Nonnull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter,
                          @Nonnull UserPreferences userPreferences,
                          @Nonnull final UsersIdentityDao usersIdentityDao
    ) {
        this.uiScheduler = uiScheduler;

        /** Requests **/
        final Observable<ResponseOrError<Shout>> shoutResponse = shoutsDao.getShoutObservable(shoutId)
                .compose(ObservableExtensions.<ResponseOrError<Shout>>behaviorRefCount());

        final Observable<Shout> successShoutResponse = shoutResponse
                .compose(ResponseOrError.<Shout>onlySuccess());

        final Observable<String> userNameObservable = successShoutResponse
                .map(new Func1<Shout, String>() {
                    @Override
                    public String call(Shout shout) {
                        return shout.getProfile().getUsername();
                    }
                })
                .compose(ObservableExtensions.<String>behaviorRefCount());

        titleObservable = successShoutResponse
                .map(new Func1<Shout, String>() {
                    @Override
                    public String call(Shout shout) {
                        return shout.getTitle();
                    }
                });

        hasMobilePhoneObservable = successShoutResponse
                .map(new Func1<Shout, Boolean>() {
                    @Override
                    public Boolean call(Shout shout) {
                        return shout.isMobileSet();
                    }
                });

        usernameObservable = successShoutResponse
                .map(new Func1<Shout, String>() {
                    @Override
                    public String call(Shout shout) {
                        return shout.getProfile().getUsername();
                    }
                });

        final Observable<ResponseOrError<ShoutsResponse>> userShoutsObservable = userNameObservable
                .switchMap(new Func1<String, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(String userName) {
                        return shoutsDao.getUserShoutObservable(new UserShoutsPointer(USER_SHOUTS_PAGE_SIZE, userName));
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<Shout>> successUserShoutsObservable = userShoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<Shout>>() {
                    @Override
                    public List<Shout> call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getShouts();
                    }
                })
                .filter(Functions1.isNotNull());

        final Observable<ResponseOrError<ShoutsResponse>> relatedShoutsObservable = shoutsDao
                .getRelatedShoutsObservable(new RelatedShoutsPointer(shoutId, RELATED_SHOUTS_PAGE_SIZE))
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<Shout>> successRelatedShoutsObservable = shoutsDao
                .getRelatedShoutsObservable(new RelatedShoutsPointer(shoutId, RELATED_SHOUTS_PAGE_SIZE))
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<Shout>>() {
                    @Override
                    public List<Shout> call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getShouts();
                    }
                })
                .filter(Functions1.isNotNull());


        /** Adapter Items **/
        final Observable<ShoutAdapterItems.MainShoutAdapterItem> shoutItemObservable =
                successShoutResponse.map(new Func1<Shout, ShoutAdapterItems.MainShoutAdapterItem>() {
                    @Override
                    public ShoutAdapterItems.MainShoutAdapterItem call(Shout shout) {
                        return new ShoutAdapterItems.MainShoutAdapterItem(addToCartSubject, onCategoryClickedSubject, shout, context.getResources());
                    }
                });

        final Observable<List<BaseAdapterItem>> userShoutItemsObservable =
                successUserShoutsObservable.map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items =
                                Lists.transform(shouts, new Function<Shout, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public ShoutAdapterItems.UserShoutAdapterItem apply(@Nullable Shout input) {
                                        return new ShoutAdapterItems.UserShoutAdapterItem(input, userShoutSelectedSubject, context.getResources());
                                    }
                                });

                        return ImmutableList.copyOf(items);
                    }
                });

        final Observable<List<BaseAdapterItem>> relatedShoutsItems = successRelatedShoutsObservable
                .map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items =
                                Lists.transform(shouts, new Function<Shout, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public ShoutAdapterItem apply(@Nullable Shout input) {
                                        return new ShoutAdapterItem(input, context, relatedShoutSelectedSubject);
                                    }
                                });

                        final ImmutableList.Builder<BaseAdapterItem> builder = new ImmutableList.Builder<>();
                        builder.addAll(items);
                        if (items.size() >= MAX_RELATED_ITEMS) {
                            builder.add(new ShoutAdapterItems.SeeAllRelatesAdapterItem(shoutId, seeAllRelatedShoutSubject));
                        }

                        return builder.build();
                    }
                });

        allAdapterItemsObservable = Observable.combineLatest(
                shoutItemObservable,
                userShoutItemsObservable.startWith(ImmutableList.<BaseAdapterItem>of()),
                relatedShoutsItems.startWith(ImmutableList.<BaseAdapterItem>of()),
                new Func3<ShoutAdapterItems.MainShoutAdapterItem, List<BaseAdapterItem>, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutAdapterItems.MainShoutAdapterItem shout,
                                                      List<BaseAdapterItem> userShouts,
                                                      List<BaseAdapterItem> relatedShouts) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        builder.add(shout);

                        final User user = shout.getShout().getProfile();
                        if (!userShouts.isEmpty()) {
                            builder.add(new HeaderAdapterItem(context.getString(R.string.shout_user_shouts_header, user.getFirstName()).toUpperCase()))
                                    .addAll(userShouts);
                        }

                        builder.add(new ShoutAdapterItems.VisitProfileAdapterItem(visitProfileSubject, user));

                        if (!relatedShouts.isEmpty()) {
                            builder.add(new HeaderAdapterItem(context.getString(R.string.shout_related_shouts_header)))
                                    .add(new ShoutAdapterItems.RelatedContainerAdapterItem(relatedShouts));
                        }

                        return builder.build();
                    }
                })
                .observeOn(uiScheduler);

        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(userShoutsObservable),
                ResponseOrError.transform(shoutResponse),
                ResponseOrError.transform(relatedShoutsObservable)))
                .filter(Functions1.isNotNull()).doOnNext(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                })
                .observeOn(uiScheduler);

        /** Progress **/
        progressObservable = Observable.merge(
                errorObservable.map(Functions1.returnFalse()),
                allAdapterItemsObservable.map(Functions1.returnFalse()))
                .startWith(true)
                .observeOn(uiScheduler);

        /** Others **/
        isUserShoutOwnerObservable = Observable.zip(
                userNameObservable, userPreferences.getUserObservable(), Observable.just(userPreferences.isNormalUser()),
                new Func3<String, User, Boolean, Boolean>() {
                    @Override
                    public Boolean call(String shoutUser, @Nullable User user, Boolean isNormalUser) {
                        return isNormalUser && user != null && user.getUsername().equals(shoutUser);
                    }
                })
                .compose(ObservableExtensions.<Boolean>behaviorRefCount())
                .first();

        shoutsGlobalRefreshPresenter.getShoutsGlobalRefreshObservable()
                .subscribe(shoutsDao.getShoutDao(shoutId).getRefreshObserver());

        /** Shout Owner Identity**/

        userIdentityResponse = getUsernameObservable()
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s != null;
                    }
                })
                .flatMap(new Func1<String, Observable<ResponseOrError<UserIdentity>>>() {
                    @Override
                    public Observable<ResponseOrError<UserIdentity>> call(String username) {
                        return usersIdentityDao.getUserIdentityObservable(username);
                    }
                });


        successUserIdentity = userIdentityResponse
                .compose(ResponseOrError.<UserIdentity>onlySuccess());

        failedUserIdentity = userIdentityResponse
                .compose(ResponseOrError.<UserIdentity>onlyError());

        identityUserObservable = successUserIdentity
                .map(new Func1<UserIdentity, String>() {
                    @Override
                    public String call(UserIdentity userIdentity) {
                        return userIdentity.getIdentity();
                    }
                }).observeOn(uiScheduler);

        final Observable<Boolean> callOrEditObservable = Observable
                .combineLatest(callOrDeleteSubject, isUserShoutOwnerObservable, new Func2<Object, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Object o, Boolean isOwner) {
                        return isOwner;
                    }
                });

        final Observable<ResponseOrError<MobilePhoneResponse>> shoutMobilePhoneErrorObservable = shoutsDao
                .getShoutMobilePhoneObservable(shoutId)
                .compose(ObservableExtensions.<ResponseOrError<MobilePhoneResponse>>behaviorRefCount());

        callErrorObservable = callOrEditObservable
                .filter(Functions1.isFalse())
                .flatMap(new Func1<Boolean, Observable<ResponseOrError<MobilePhoneResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<MobilePhoneResponse>> call(Boolean aBoolean) {
                        return shoutMobilePhoneErrorObservable;
                    }
                })
                .observeOn(uiScheduler);

        deleteShoutSubject.subscribe(shoutsDao.getDeleteShoutObserver(shoutId));

        sendReportObserver.subscribe(shoutsDao.getReportShoutObserver(shoutId));

        reportShoutObservable = shoutsDao.getReportShoutObservable(shoutId)
                .observeOn(uiScheduler);

        showDeleteDialogObservable = callOrEditObservable
                .filter(Functions1.isTrue())
                .observeOn(uiScheduler);

        deleteShoutResponseObservable = shoutsDao.getDeleteShoutObservable(shoutId)
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<String> getIdentityUserObservable() {
        return identityUserObservable;
    }

    @Nonnull
    public Observable<String> getOnCategoryClickedObservable() {
        return onCategoryClickedSubject;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<String> getTitleObservable() {
        return titleObservable
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<String> getUserShoutSelectedObservable() {
        return userShoutSelectedSubject;
    }

    @Nonnull
    public Observable<String> getRelatedShoutSelectedObservable() {
        return relatedShoutSelectedSubject;
    }

    @Nonnull
    public Observable<String> getSeeAllRelatedShoutObservable() {
        return seeAllRelatedShoutSubject;
    }

    @Nonnull
    public Observable<User> getVisitProfileObservable() {
        return visitProfileSubject;
    }

    @Nonnull
    public Observable<String> getAddToCartSubject() {
        return addToCartSubject;
    }

    @Nonnull
    public Observable<Boolean> getIsUserShoutOwnerObservable() {
        return allAdapterItemsObservable
                .first()
                .flatMap(new Func1<List<BaseAdapterItem>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(List<BaseAdapterItem> baseAdapterItems) {
                        return isUserShoutOwnerObservable;
                    }
                })
                .observeOn(uiScheduler);
    }

    public Observable<String> getUsernameObservable() {
        return usernameObservable;
    }

    @Nonnull
    public Observer<Object> callOrDeleteObserver() {
        return callOrDeleteSubject;
    }

    @Nonnull
    public Observable<ResponseOrError<MobilePhoneResponse>> getCallErrorObservable() {
        return callErrorObservable;
    }

    @Nonnull
    public Observable<Boolean> getHasMobilePhoneObservable() {
        return hasMobilePhoneObservable;
    }

    @Nonnull
    public Observable<Response<Object>> getDeleteShoutResponseObservable() {
        return deleteShoutResponseObservable;
    }

    @Nonnull
    public Observer<String> sendReportObserver() {
        return sendReportObserver;
    }

    @Nonnull
    public Observable<Boolean> getShowDeleteDialogObservable() {
        return showDeleteDialogObservable;
    }

    @Nonnull
    public Observer<Object> getDeleteShoutObserver() {
        return deleteShoutSubject;
    }

    @Nonnull
    public Observable<Response<Object>> getReportShoutObservable() {
        return reportShoutObservable;
    }
}


