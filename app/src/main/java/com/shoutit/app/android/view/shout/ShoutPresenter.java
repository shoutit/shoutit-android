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
import com.shoutit.app.android.api.model.ConversationDetails;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BaseShoutsDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.model.MobilePhoneResponse;
import com.shoutit.app.android.model.RelatedShoutsPointer;
import com.shoutit.app.android.model.UserShoutsPointer;
import com.shoutit.app.android.utils.PromotionHelper;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
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
    private final Observable<Boolean> isUserShoutOwnerObservable;
    private final Observable<ResponseOrError<MobilePhoneResponse>> callErrorObservable;
    private final Observable<Boolean> hasMobilePhoneObservable;
    private final Observable<Response<Object>> deleteShoutResponseObservable;
    private final Observable<Boolean> showDeleteDialogObservable;
    private final PublishSubject<Object> deleteShoutSubject = PublishSubject.create();
    private final Observable<Response<Object>> reportShoutObservable;
    private final Observable<List<ConversationDetails>> conversationObservable;
    private final Observable<Object> refreshShoutsObservable;
    private final Observable<User> videoCallClickedObservable;
    private final Observable<Boolean> editShoutClickedObservable;
    private final Observable<Object> onlyForLoggedInUserObservable;
    private final Observable<String> shareObservable;
    private final Observable<Shout> showPromoteObservable;
    private final Observable<Boolean> showPromotedObservable;

    private PublishSubject<String> addToCartSubject = PublishSubject.create();
    private PublishSubject<String> onCategoryClickedSubject = PublishSubject.create();
    private PublishSubject<String> userShoutSelectedSubject = PublishSubject.create();
    private PublishSubject<String> relatedShoutSelectedSubject = PublishSubject.create();
    private PublishSubject<String> seeAllRelatedShoutSubject = PublishSubject.create();
    private PublishSubject<User> visitProfileSubject = PublishSubject.create();
    private PublishSubject<Object> onVideoOrEditClickSubject = PublishSubject.create();
    private PublishSubject<Object> shareSubject = PublishSubject.create();
    private PublishSubject<Object> showDeleteDialogSubject = PublishSubject.create();

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final UserPreferences mUserPreferences;
    @Nonnull
    private final PublishSubject<Object> callOrPromoteSubject = PublishSubject.create();
    private final PublishSubject<String> sendReportObserver = PublishSubject.create();
    private final PublishSubject<Object> refreshShoutsSubject = PublishSubject.create();
    private final Observable<User> shoutOwnerProfile;
    private final Observable<Shout> successShoutResponse;

    @Inject
    public ShoutPresenter(@Nonnull final ShoutsDao shoutsDao,
                          @Nonnull final String shoutId,
                          @Nonnull @ForActivity final Context context,
                          @Nonnull @UiScheduler final Scheduler uiScheduler,
                          @Nonnull final UserPreferences userPreferences,
                          @Nonnull final ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter) {
        this.uiScheduler = uiScheduler;
        mUserPreferences = userPreferences;

        final boolean isNormalUser = userPreferences.isNormalUser();
        final User currentUser = userPreferences.getUser();
        final String currentUserName = currentUser != null ? currentUser.getUsername() : null;

        /** Requests **/
        final Observable<ResponseOrError<Shout>> shoutResponse = shoutsDao.getShoutObservable(shoutId)
                .compose(ObservableExtensions.<ResponseOrError<Shout>>behaviorRefCount());

        successShoutResponse = shoutResponse
                .compose(ResponseOrError.<Shout>onlySuccess());

        final Observable<String> userNameObservable = successShoutResponse
                .map(shout -> shout.getProfile().getUsername())
                .compose(ObservableExtensions.<String>behaviorRefCount());

        shoutOwnerProfile = successShoutResponse
                .map(Shout::getProfile);

        titleObservable = successShoutResponse
                .map(Shout::getTitle);

        conversationObservable = successShoutResponse
                .map(Shout::getConversations);

        final Observable<ShoutsDao.UserShoutsDao> userShoutDaoObservable = userNameObservable
                .map(new Func1<String, ShoutsDao.UserShoutsDao>() {
                    @Override
                    public ShoutsDao.UserShoutsDao call(String userName) {
                        return shoutsDao.getUserShoutsDao(new UserShoutsPointer(USER_SHOUTS_PAGE_SIZE, userName));
                    }
                })
                .compose(ObservableExtensions.<ShoutsDao.UserShoutsDao>behaviorRefCount());

        final Observable<ResponseOrError<ShoutsResponse>> userShoutsObservable = userShoutDaoObservable
                .flatMap(BaseShoutsDao::getShoutsObservable)
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<Shout>> successUserShoutsObservable = userShoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(ShoutsResponse::getShouts)
                .filter(Functions1.isNotNull());

        final Observable<ResponseOrError<ShoutsResponse>> relatedShoutsObservable = shoutsDao
                .getRelatedShoutsObservable(new RelatedShoutsPointer(shoutId, RELATED_SHOUTS_PAGE_SIZE))
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<Shout>> successRelatedShoutsObservable = shoutsDao
                .getRelatedShoutsObservable(new RelatedShoutsPointer(shoutId, RELATED_SHOUTS_PAGE_SIZE))
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(ShoutsResponse::getShouts)
                .filter(Functions1.isNotNull());


        /** Adapter Items **/
        final Observable<ShoutAdapterItems.MainShoutAdapterItem> shoutItemObservable =
                successShoutResponse.map(shout -> new ShoutAdapterItems.MainShoutAdapterItem(addToCartSubject, onCategoryClickedSubject,
                        visitProfileSubject, shout, context.getResources()));

        final Observable<List<BaseAdapterItem>> userShoutItemsObservable =
                successUserShoutsObservable.map((Func1<List<Shout>, List<BaseAdapterItem>>) shouts -> {
                    final List<BaseAdapterItem> items =
                            Lists.transform(shouts, (Function<Shout, BaseAdapterItem>) input -> new ShoutAdapterItems.UserShoutAdapterItem(input, userShoutSelectedSubject, context.getResources()));

                    return ImmutableList.copyOf(items);
                });

        final Observable<List<BaseAdapterItem>> relatedShoutsItems = successRelatedShoutsObservable
                .map(new Func1<List<Shout>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Shout> shouts) {
                        final List<BaseAdapterItem> items =
                                Lists.transform(shouts, (Function<Shout, BaseAdapterItem>) shout -> {
                                    final boolean isShoutOwner = shout.getProfile().getUsername().equals(currentUserName);
                                    return new ShoutAdapterItem(shout, isShoutOwner, isNormalUser, context, relatedShoutSelectedSubject, PromotionHelper.promotionInfoOrNull(shout));
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
                .filter(Functions1.isNotNull())
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
                .take(1)
                .compose(ObservableExtensions.<Boolean>behaviorRefCount());

        hasMobilePhoneObservable = successShoutResponse
                .map(Shout::isMobileSet)
                .withLatestFrom(isUserShoutOwnerObservable, (isMobileSet, isShoutOwner) -> {
                    if (!isShoutOwner) {
                        return isMobileSet;
                    } else {
                        return true;
                    }
                });

        /** Refresh shouts **/
        final Observable<Object> refreshShout = Observable
                .merge(shoutsGlobalRefreshPresenter.getShoutsGlobalRefreshObservable(), refreshShoutsSubject)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        shoutsDao.getShoutDao(shoutId)
                                .getRefreshObserver().onNext(null);
                        return null;
                    }
                });

        final Observable<Object> refreshUserShouts = shoutsGlobalRefreshPresenter.getShoutsGlobalRefreshObservable()
                .withLatestFrom(userShoutDaoObservable, new Func2<Object, ShoutsDao.UserShoutsDao, Object>() {
                    @Override
                    public Observer<Object> call(Object o, ShoutsDao.UserShoutsDao userShoutsDao) {
                        userShoutsDao.getRefreshObserver().onNext(null);
                        return null;
                    }
                });

        final Observable<Object> refreshRelatedShouts = shoutsGlobalRefreshPresenter.getShoutsGlobalRefreshObservable()
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        shoutsDao.getRelatedShoutsDao(new RelatedShoutsPointer(shoutId, RELATED_SHOUTS_PAGE_SIZE))
                                .getRefreshObserver().onNext(null);
                        return null;
                    }
                });

        refreshShoutsObservable = Observable.merge(refreshUserShouts, refreshRelatedShouts, refreshShout);
        /** **/

        final Observable<Boolean> callOrPromoteObservable = Observable
                .combineLatest(callOrPromoteSubject, isUserShoutOwnerObservable, (o, isOwner) -> isOwner);

        final Observable<ResponseOrError<MobilePhoneResponse>> shoutMobilePhoneErrorObservable = shoutsDao
                .getShoutMobilePhoneObservable(shoutId)
                .compose(ObservableExtensions.<ResponseOrError<MobilePhoneResponse>>behaviorRefCount());

        callErrorObservable = callOrPromoteObservable
                .filter(Functions1.isFalse())
                .flatMap(isNotShoutOwner -> shoutMobilePhoneErrorObservable)
                .observeOn(uiScheduler);

        deleteShoutSubject.subscribe(shoutsDao.getDeleteShoutObserver(shoutId));

        sendReportObserver.subscribe(shoutsDao.getReportShoutObserver(shoutId));

        reportShoutObservable = shoutsDao.getReportShoutObservable(shoutId)
                .observeOn(uiScheduler);

        showDeleteDialogObservable = showDeleteDialogSubject
                .withLatestFrom(isUserShoutOwnerObservable, (o, isShoutOwner) -> isShoutOwner)
                .filter(Functions1.isTrue())
                .observeOn(uiScheduler);

        showPromoteObservable = callOrPromoteObservable
                .filter(Functions1.isTrue())
                .withLatestFrom(successShoutResponse, (ignore, shout) -> shout)
                .filter(shout -> !shout.isPromoted())
                .observeOn(uiScheduler);

        showPromotedObservable = callOrPromoteObservable
                .filter(Functions1.isTrue())
                .withLatestFrom(successShoutResponse, (ignore, shout) -> shout.isPromoted())
                .filter(Functions1.isFalse())
                .observeOn(uiScheduler);

        deleteShoutResponseObservable = shoutsDao.getDeleteShoutObservable(shoutId)
                .observeOn(uiScheduler)
                .doOnNext(objectResponse -> shoutsGlobalRefreshPresenter.refreshShouts());

        videoCallClickedObservable = onVideoOrEditClickSubject
                .withLatestFrom(isUserShoutOwnerObservable, (o, isShoutOwner) -> isShoutOwner)
                .filter(Functions1.isFalse())
                .filter(aBoolean -> !userPreferences.isGuest())
                .withLatestFrom(shoutOwnerProfile, (aBoolean, profile) -> profile);

        editShoutClickedObservable = onVideoOrEditClickSubject
                .withLatestFrom(isUserShoutOwnerObservable, (o, isOwner) -> isOwner)
                .filter(isOwner -> isOwner)
                .filter(aBoolean -> !userPreferences.isGuest());

        onlyForLoggedInUserObservable = onVideoOrEditClickSubject
                .filter(o -> userPreferences.isGuest());

        shareObservable = shareSubject
                .withLatestFrom(successShoutResponse, (o, shout) -> shout.getWebUrl());
    }

    @Nonnull
    public Observable<String> getShareObservable() {
        return shareObservable;
    }

    @Nonnull
    public Observable<Boolean> getEditShoutClickedObservable() {
        return editShoutClickedObservable;
    }

    @Nonnull
    public Observable<Object> getOnlyForLoggedInUserObservable() {
        return onlyForLoggedInUserObservable;
    }

    @Nonnull
    public Observable<User> getVideoCallClickedObservable() {
        return videoCallClickedObservable;
    }

    @Nonnull
    public Observer<Object> getVideoOrEditClickSubject() {
        return RxMoreObservers.ignoreCompleted(onVideoOrEditClickSubject);
    }

    @Nonnull
    public Observable<Object> getRefreshShoutsObservable() {
        return refreshShoutsObservable;
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
    public Observer<Object> getShowDeleteDialogObserver() {
        return showDeleteDialogSubject;
    }

    @Nonnull
    public Observable<BottomBarData> getBottomBarDataObservable() {
        return allAdapterItemsObservable
                .take(1)
                .flatMap(baseAdapterItems -> Observable.combineLatest(
                        isUserShoutOwnerObservable,
                        conversationObservable,
                        successShoutResponse,
                        (isUserShoutOwner, conversations, shout) -> {
                            final boolean hasConversation = conversations != null && !conversations.isEmpty();
                            return new BottomBarData(isUserShoutOwner, hasConversation,
                                    hasConversation ? conversations.get(0).getId() : null,
                                    mUserPreferences.isNormalUser(), shout.isPromoted());
                        }
                ))
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observer<Object> callOrPromoteObserver() {
        return callOrPromoteSubject;
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
    public Observable<Shout> getShowPromoteObservable() {
        return showPromoteObservable;
    }

    public Observable<Boolean> getShowPromotedObservable() {
        return showPromotedObservable;
    }

    @Nonnull
    public Observer<Object> getDeleteShoutObserver() {
        return deleteShoutSubject;
    }

    @Nonnull
    public Observable<Response<Object>> getReportShoutObservable() {
        return reportShoutObservable;
    }

    @Nonnull
    public Observer<Object> refreshShoutsObserver() {
        return refreshShoutsSubject;
    }

    public void onShareClicked() {
        shareSubject.onNext(null);
    }

    public static class BottomBarData {

        private final boolean isUserShoutOwner;
        private final boolean hasConversation;
        private final String conversationId;
        private final boolean isNormalUser;
        private final boolean isPromoted;

        public BottomBarData(boolean isUserShoutOwner, boolean hasConversation, String conversationId,
                             boolean isNormalUser, boolean isPromoted) {
            this.isUserShoutOwner = isUserShoutOwner;
            this.hasConversation = hasConversation;
            this.conversationId = conversationId;
            this.isNormalUser = isNormalUser;
            this.isPromoted = isPromoted;
        }

        public boolean isUserShoutOwner() {
            return isUserShoutOwner;
        }

        public boolean isHasConversation() {
            return hasConversation;
        }

        public String getConversationId() {
            return conversationId;
        }

        public boolean isNormalUser() {
            return isNormalUser;
        }

        public boolean isPromoted() {
            return isPromoted;
        }
    }
}


