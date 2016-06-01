package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.app.Activity;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.facebook.CallbackManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.MutualFriendsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.view.profileslist.ProfileListAdapterItem;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class FacebookFriendsPresenter implements ProfilesListPresenter {

    private final PublishSubject<String> openProfileSubject = PublishSubject.create();
    private final PublishSubject<BaseProfile> listenProfileSubject = PublishSubject.create();
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();
    
    private final Observable<List<BaseAdapterItem>> adapterItems;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> permissionsNotGrantedObservable;
    private final Observable<Boolean> progressObservable;
    private final ProfilesDao dao;

    public FacebookFriendsPresenter(final FacebookHelper facebookHelper,
                                    final ApiService apiService,
                                    UserPreferences userPreferences,
                                    final Activity activity,
                                    final CallbackManager callbackManager,
                                    ProfilesDao dao,
                                    @UiScheduler final Scheduler uiScheduler,
                                    @NetworkScheduler final Scheduler networkScheduler) {
        this.dao = dao;

        //noinspection ConstantConditions
        final boolean hasRequiredPermissionInApi = facebookHelper.hasRequiredPermissionInApi(
                userPreferences.getUser(), FacebookHelper.PERMISSION_USER_FRIENDS);

        final Observable<ResponseOrError<MutualFriendsResponse>> friendsRequest = dao.getFriendsDao(User.ME)
                .getProfilesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<MutualFriendsResponse>>behaviorRefCount());

        final Observable<MutualFriendsResponse> successFriendsRequest = friendsRequest
                .compose(ResponseOrError.<MutualFriendsResponse>onlySuccess());

        final Observable<ResponseOrError<Boolean>> arePermissionsGranted = Observable
                .just(hasRequiredPermissionInApi)
                .switchMap(new Func1<Boolean, Observable<ResponseOrError<Boolean>>>() {
                    @Override
                    public Observable<ResponseOrError<Boolean>> call(Boolean hasPermissions) {
                        if (hasPermissions) {
                            return Observable.just(ResponseOrError.fromData(true));
                        } else {
                            return facebookHelper.askForPermissionIfNeeded(
                                    activity, FacebookHelper.PERMISSION_USER_FRIENDS, callbackManager, false);
                        }
                    }
                })
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<Boolean>>behaviorRefCount());


        adapterItems = arePermissionsGranted
                .compose(ResponseOrError.<Boolean>onlySuccess())
                .filter(Functions1.isTrue())
                .switchMap(new Func1<Boolean, Observable<MutualFriendsResponse>>() {
                    @Override
                    public Observable<MutualFriendsResponse> call(Boolean aBoolean) {
                        return successFriendsRequest;
                    }
                })
                .map(new Func1<MutualFriendsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(MutualFriendsResponse response) {
                        if (response.getResults().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataTextAdapterItem(activity.getString(R.string.facebook_friends_no_friends)));
                        } else {
                            return ImmutableList.copyOf(
                                    Lists.transform(response.getResults(), new Function<BaseProfile, BaseAdapterItem>() {
                                        @Nullable
                                        @Override
                                        public BaseAdapterItem apply(BaseProfile profile) {
                                            return new ProfileListAdapterItem(profile, openProfileSubject, listenProfileSubject);
                                        }
                                    }));
                        }
                    }
                });

        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(friendsRequest),
                        ResponseOrError.transform(arePermissionsGranted)
                )
        ).filter(Functions1.isNotNull());

        permissionsNotGrantedObservable = arePermissionsGranted
                .compose(ResponseOrError.<Boolean>onlySuccess())
                .filter(Functions1.isFalse());

        progressObservable = Observable.merge(
                adapterItems.map(Functions1.returnFalse()),
                errorObservable.map(Functions1.returnFalse()),
                permissionsNotGrantedObservable.map(Functions1.returnFalse()))
                .startWith(true);

        listenProfileSubject
                .withLatestFrom(successFriendsRequest, new Func2<BaseProfile, MutualFriendsResponse, ProfileToListenWithLastResponse>() {
                    @Override
                    public ProfileToListenWithLastResponse call(BaseProfile profileToListen, MutualFriendsResponse listeningResponse) {
                        return new ProfileToListenWithLastResponse(profileToListen, listeningResponse);
                    }
                })
                .switchMap(new Func1<ProfileToListenWithLastResponse, Observable<ResponseOrError<MutualFriendsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<MutualFriendsResponse>> call(final ProfileToListenWithLastResponse profileToListenWithLastResponse) {

                        final String profileId = profileToListenWithLastResponse.getProfile().getUsername();
                        final boolean isListeningToProfile = profileToListenWithLastResponse.getProfile().isListening();

                        Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                        if (isListeningToProfile) {
                            listenRequestObservable = apiService.unlistenProfile(profileId)
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .doOnNext(new Action1<ResponseBody>() {
                                        @Override
                                        public void call(ResponseBody responseBody) {
                                            unListenSuccess.onNext(profileToListenWithLastResponse.getProfile().getName());
                                        }
                                    })
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        } else {
                            listenRequestObservable = apiService.listenProfile(profileId)
                                    .subscribeOn(networkScheduler)
                                    .observeOn(uiScheduler)
                                    .doOnNext(new Action1<ResponseBody>() {
                                        @Override
                                        public void call(ResponseBody responseBody) {
                                            listenSuccess.onNext(profileToListenWithLastResponse.getProfile().getName());
                                        }
                                    })
                                    .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable());
                        }

                        return listenRequestObservable
                                .map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<MutualFriendsResponse>>() {
                                    @Override
                                    public ResponseOrError<MutualFriendsResponse> call(ResponseOrError<ResponseBody> response) {
                                        if (response.isData()) {
                                            return ResponseOrError.fromData(updateLastResponse(profileToListenWithLastResponse));
                                        } else {
                                            errorSubject.onNext(new Throwable());
                                            // On error return current user in order to select/deselect already deselected/selected item
                                            return ResponseOrError.fromData(profileToListenWithLastResponse.getResponse());
                                        }
                                    }
                                });
                    }
                })
                .subscribe(dao.getFriendsDao(User.ME).updatedProfileLocallyObserver());
    }

    private MutualFriendsResponse updateLastResponse(ProfileToListenWithLastResponse profileToListenWithLastResponse) {
        final MutualFriendsResponse response = profileToListenWithLastResponse.getResponse();

        final List<BaseProfile> profiles = response.getResults();
        final String profileToUpdateId = profileToListenWithLastResponse.getProfile().getUsername();

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getUsername().equals(profileToUpdateId)) {
                final BaseProfile profileToUpdate = profiles.get(i);
                final BaseProfile updatedProfile = profileToUpdate.getListenedProfile();

                final List<BaseProfile> updatedProfiles = new ArrayList<>(profiles);
                updatedProfiles.set(i, updatedProfile);

                return new MutualFriendsResponse(response.getCount(), response.getNext(),
                        response.getPrevious(), updatedProfiles);
            }
        }

        return response;
    }

    @Nonnull
    public Observable<Boolean> getPermissionsNotGrantedObservable() {
        return permissionsNotGrantedObservable;
    }

    @Nonnull
    @Override
    public Observable<String> getListenSuccessObservable() {
        return listenSuccess;
    }

    @Nonnull
    @Override
    public Observable<String> getUnListenSuccessObservable() {
        return unListenSuccess;
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

    @Override
    public void refreshData() {
        dao.getFriendsDao(User.ME)
                .getRefreshSubject()
                .onNext(null);
    }

    @Override
    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(dao.getFriendsDao(User.ME)
                .getLoadMoreShoutsObserver());
    }

    public static class ProfileToListenWithLastResponse {

        @Nonnull
        private final BaseProfile profile;
        @Nonnull
        private final MutualFriendsResponse response;

        public ProfileToListenWithLastResponse(@Nonnull BaseProfile profile,
                                               @Nonnull MutualFriendsResponse response) {
            this.profile = profile;
            this.response = response;
        }

        @Nonnull
        public BaseProfile getProfile() {
            return profile;
        }

        @Nonnull
        public MutualFriendsResponse getResponse() {
            return response;
        }
    }
}
