package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.app.Activity;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.facebook.CallbackManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.view.profileslist.ProfileListAdapterItem;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class FacebookFriendsPresenter implements ProfilesListPresenter {

    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    private final PublishSubject<Object> openInviteClickedSubject = PublishSubject.create();
    private final PublishSubject<Object> progressSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> adapterItems;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> permissionsNotGrantedObservable;
    private final Observable<Boolean> progressObservable;

    private final ProfilesDao dao;
    private final ListeningHalfPresenter listeningHalfPresenter;

    public FacebookFriendsPresenter(final FacebookHelper facebookHelper,
                                    UserPreferences userPreferences,
                                    final Activity activity,
                                    final CallbackManager callbackManager,
                                    ProfilesDao dao,
                                    @UiScheduler final Scheduler uiScheduler,
                                    ListeningHalfPresenter listeningHalfPresenter) {
        this.dao = dao;
        this.listeningHalfPresenter = listeningHalfPresenter;

        //noinspection ConstantConditions
        final boolean hasRequiredPermissionInApi = facebookHelper.hasRequiredPermissionInApi(
                userPreferences.getUser(), FacebookHelper.PERMISSION_USER_FRIENDS);

        final Observable<ResponseOrError<ProfilesListResponse>> friendsRequest = dao.getFriendsDao(User.ME)
                .getProfilesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ProfilesListResponse>>behaviorRefCount());

        final Observable<ProfilesListResponse> successFriendsRequest = friendsRequest
                .compose(ResponseOrError.<ProfilesListResponse>onlySuccess());

        final Observable<ResponseOrError<Boolean>> arePermissionsGranted = Observable
                .just(hasRequiredPermissionInApi)
                .switchMap(hasPermissions -> {
                    if (hasPermissions) {
                        return Observable.just(ResponseOrError.fromData(true));
                    } else {
                        return facebookHelper.askForPermissionIfNeeded(
                                activity, FacebookHelper.PERMISSION_USER_FRIENDS, callbackManager, false);
                    }
                })
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<Boolean>>behaviorRefCount());


        adapterItems = arePermissionsGranted
                .compose(ResponseOrError.<Boolean>onlySuccess())
                .filter(Functions1.isTrue())
                .switchMap(aBoolean -> successFriendsRequest)
                .map(new Func1<ProfilesListResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ProfilesListResponse response) {
                        if (response.getResults().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataTextAdapterItem(activity.getString(R.string.facebook_friends_no_friends)));
                        } else {
                            return ImmutableList.<BaseAdapterItem>builder().addAll(
                                    Lists.transform(response.getResults(), new Function<BaseProfile, BaseAdapterItem>() {
                                        @Nullable
                                        @Override
                                        public BaseAdapterItem apply(BaseProfile profile) {
                                            return new ProfileListAdapterItem(profile, openProfileSubject, listeningHalfPresenter.getListenProfileSubject());
                                        }
                                    }))
                                    .add(new FacebookInviteFriendsAdapterItem(openInviteClickedSubject))
                                    .build();
                        }
                    }
                });

        listeningHalfPresenter
                .listeningObservable(successFriendsRequest)
                .subscribe(dao.getFriendsDao(User.ME).updatedProfileLocallyObserver());

        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(friendsRequest),
                        ResponseOrError.transform(arePermissionsGranted)
                ))
                .filter(Functions1.isNotNull())
                .mergeWith(listeningHalfPresenter.getErrorSubject());

        permissionsNotGrantedObservable = arePermissionsGranted
                .compose(ResponseOrError.<Boolean>onlySuccess())
                .filter(Functions1.isFalse());

        progressObservable = Observable.merge(
                progressSubject.map(Functions1.returnTrue()),
                adapterItems.map(Functions1.returnFalse()),
                errorObservable.map(Functions1.returnFalse()),
                permissionsNotGrantedObservable.map(Functions1.returnFalse()))
                .startWith(true);
    }

    @Nonnull
    public Observable<Boolean> getPermissionsNotGrantedObservable() {
        return permissionsNotGrantedObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getListenSuccessObservable() {
        return listeningHalfPresenter.getListenSuccess();
    }

    @Nonnull
    @Override
    public Observable<String> getUnListenSuccessObservable() {
        return listeningHalfPresenter.getUnListenSuccess();
    }

    @Override
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Override
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Override
    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItems;
    }

    @Override
    public Observable<String> getProfileToOpenObservable() {
        return openProfileSubject;
    }

    @Nonnull
    public Observable<Object> getOpenInviteClickedObservable() {
        return openInviteClickedSubject;
    }

    @Override
    public void refreshData() {
        progressSubject.onNext(null);
        dao.getFriendsDao(User.ME)
                .getRefreshSubject()
                .onNext(null);
    }

    @Override
    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(dao.getFriendsDao(User.ME)
                .getLoadMoreShoutsObserver());
    }

    public static class FacebookInviteFriendsAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Observer<Object> openInviteClicked;

        public FacebookInviteFriendsAdapterItem(@Nonnull Observer<Object> openInviteClicked) {
            this.openInviteClicked = openInviteClicked;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof FacebookInviteFriendsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return true;
        }

        public void onOpenInviteClicked() {
            openInviteClicked.onNext(null);
        }
    }
}
