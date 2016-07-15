package com.shoutit.app.android.view.search.results.profiles;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.SearchProfileResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class SearchProfilesResultsPresenter {

    private final Observable<List<BaseAdapterItem>> adapterItemsObservable;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;

    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();
    private final PublishSubject<ProfileToListenWithLastResponse> profileListenedSubject = PublishSubject.create();
    private final PublishSubject<BaseProfile> profileToOpenSubject = PublishSubject.create();
    private final PublishSubject<Object> actionOnlyForLoggedInUserSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> listenSuccess = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> unListenSuccess = PublishSubject.create();

    private final ProfilesDao dao;
    @Nonnull
    private final String searchQuery;
    private final boolean isLoggedInAsNormalUser;

    public SearchProfilesResultsPresenter(ProfilesDao dao, @Nonnull String searchQuery,
                                          final ApiService apiService,
                                          @UiScheduler final Scheduler uiScheduler,
                                          @NetworkScheduler final Scheduler networkScheduler,
                                          UserPreferences userPreferences) {
        this.dao = dao;
        this.searchQuery = searchQuery;
        isLoggedInAsNormalUser = userPreferences.isNormalUser();

        final Observable<ResponseOrError<SearchProfileResponse>> profilesRequest = dao.getSearchProfilesDao(searchQuery)
                .getProfilesObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<SearchProfileResponse>>behaviorRefCount());

        final Observable<SearchProfileResponse> successProfilesRequest = profilesRequest
                .compose(ResponseOrError.<SearchProfileResponse>onlySuccess());

        adapterItemsObservable = successProfilesRequest
                .map(new Func1<SearchProfileResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(final SearchProfileResponse searchProfileResponse) {
                        if (searchProfileResponse.getResults().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
                        } else {
                            return ImmutableList.copyOf(
                                    Lists.transform(searchProfileResponse.getResults(),
                                            new Function<BaseProfile, BaseAdapterItem>() {
                                                @Nullable
                                                @Override
                                                public BaseAdapterItem apply(@Nullable BaseProfile input) {
                                                    return new ProfileAdapterItem(searchProfileResponse, input,
                                                            profileListenedSubject, profileToOpenSubject,
                                                            actionOnlyForLoggedInUserSubject);
                                                }
                                            }));
                        }
                    }
                });

        progressObservable = profilesRequest.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = Observable.merge(
                profilesRequest.compose(ResponseOrError.<SearchProfileResponse>onlyError()),
                errorSubject);

        profileListenedSubject
                .throttleFirst(400, TimeUnit.MILLISECONDS, uiScheduler)
                .switchMap(new Func1<ProfileToListenWithLastResponse, Observable<ResponseOrError<SearchProfileResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<SearchProfileResponse>> call(final ProfileToListenWithLastResponse profileToListenWithLastResponse) {
                        final String userName = profileToListenWithLastResponse.getProfile().getUsername();
                        final boolean isListeningToProfile = profileToListenWithLastResponse.getProfile().isListening();

                        Observable<ResponseOrError<ResponseBody>> listenRequestObservable;
                        if (isListeningToProfile) {
                            listenRequestObservable = apiService.unlistenProfile(userName)
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
                            listenRequestObservable = apiService.listenProfile(userName)
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
                                .map(new Func1<ResponseOrError<ResponseBody>, ResponseOrError<SearchProfileResponse>>() {
                                    @Override
                                    public ResponseOrError<SearchProfileResponse> call(ResponseOrError<ResponseBody> response) {
                                        if (response.isData()) {
                                            return ResponseOrError.fromData(updateLastResponse(profileToListenWithLastResponse));
                                        } else {
                                            errorSubject.onNext(new Throwable());
                                            // On error return current user in order to select/deselect already deselected/selected item to listenProfile
                                            return ResponseOrError.fromData(profileToListenWithLastResponse.getResponse());
                                        }
                                    }
                                });
                    }
                })
                .subscribe(dao.getSearchProfilesDao(searchQuery).updatedProfileLocallyObserver());
    }

    @Nonnull
    public Observable<String> getListenSuccessObservable() {
        return listenSuccess;
    }

    @Nonnull
    public Observable<String> getUnListenSuccessObservable() {
        return unListenSuccess;
    }

    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(dao.getSearchProfilesDao(searchQuery).getLoadMoreShoutsObserver());
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItemsObservable;
    }

    public Observable<BaseProfile> getProfileToOpenObservable() {
        return profileToOpenSubject;
    }

    public Observable<Object> getActionOnlyForLoggedInUserObserable() {
        return actionOnlyForLoggedInUserSubject;
    }

    private SearchProfileResponse updateLastResponse(ProfileToListenWithLastResponse profileToListenWithLastResponse) {
        final SearchProfileResponse response = profileToListenWithLastResponse.getResponse();
        final List<User> profiles = response.getResults();
        final String profileToUpdateUserName = profileToListenWithLastResponse.getProfile().getUsername();

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getUsername().equals(profileToUpdateUserName)) {
                final User profileToUpdate = profiles.get(i);
                final User updatedProfile = profileToUpdate.getListenedProfile();

                final List<User> updatedProfiles = new ArrayList<>(profiles);
                updatedProfiles.set(i, updatedProfile);

                return new SearchProfileResponse(response.getCount(), response.getNext(), response.getPrevious(), updatedProfiles);
            }
        }

        return response;
    }

    public void refreshData() {
        dao.getSearchProfilesDao(searchQuery).getRefreshSubject().onNext(null);
    }

    public class ProfileAdapterItem extends BaseNoIDAdapterItem {

        private final SearchProfileResponse lastResponse;
        private final BaseProfile profile;
        private final Observer<ProfileToListenWithLastResponse> profileListenedObserver;
        private Observer<BaseProfile> profileToOpenObserver;
        private Observer<Object> actionOnlyForLoggedInUserObserver;

        public ProfileAdapterItem(SearchProfileResponse lastResponse,
                                  BaseProfile profile,
                                  Observer<ProfileToListenWithLastResponse> profileListenedObserver,
                                  Observer<BaseProfile> profileToOpenObserver,
                                  Observer<Object> actionOnlyForLoggedInUserObserver) {
            this.lastResponse = lastResponse;
            this.profile = profile;
            this.profileListenedObserver = profileListenedObserver;
            this.profileToOpenObserver = profileToOpenObserver;
            this.actionOnlyForLoggedInUserObserver = actionOnlyForLoggedInUserObserver;
        }

        public BaseProfile getProfile() {
            return profile;
        }

        public void onProfileListened() {
            profileListenedObserver.onNext(new ProfileToListenWithLastResponse(profile, lastResponse));
        }

        public void onProfileItemSelected() {
            profileToOpenObserver.onNext(profile);
        }

        public void onActionOnlyForLoggedInUser() {
            actionOnlyForLoggedInUserObserver.onNext(null);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItem &&
                    ((ProfileAdapterItem) item).profile.getId().equals(profile.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItem &&
                    profile.equals(((ProfileAdapterItem) item).profile);
        }

        public boolean isProfileMine() {
            return profile.isOwner() && ProfileType.USER.equals(profile.getType());
        }

        public boolean isUserLoggedIn() {
            return isLoggedInAsNormalUser;
        }
    }

    public static class ProfileToListenWithLastResponse {

        @Nonnull
        private final BaseProfile profile;
        @Nonnull
        private final SearchProfileResponse response;

        public ProfileToListenWithLastResponse(@Nonnull BaseProfile profile, @Nonnull SearchProfileResponse response) {
            this.profile = profile;
            this.response = response;
        }

        @Nonnull
        public BaseProfile getProfile() {
            return profile;
        }

        @Nonnull
        public SearchProfileResponse getResponse() {
            return response;
        }
    }
}
