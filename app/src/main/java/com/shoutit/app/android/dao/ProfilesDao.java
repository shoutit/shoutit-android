package com.shoutit.app.android.dao;

import com.appunite.appunitegcm.AppuniteGcm;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.RegisterDeviceRequest;
import com.shoutit.app.android.api.model.SearchProfileResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.LogHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class ProfilesDao {

    @Nonnull
    private final LoadingCache<String, ProfileDao> profilesCache;
    @Nonnull
    private final LoadingCache<String, SearchProfilesDao> searchProfilesCache;
    @Nonnull
    private final LoadingCache<String, FriendsDao> friendsCache;
    @Nonnull
    private final LoadingCache<String, ContactsDao> contactsCache;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private UserPreferences userPreferences;

    public ProfilesDao(@Nonnull ApiService apiService,
                       @Nonnull @NetworkScheduler Scheduler networkScheduler,
                       @Nonnull UserPreferences userPreferences) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;
        this.userPreferences = userPreferences;

        profilesCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ProfileDao>() {
                    @Override
                    public ProfileDao load(@Nonnull String userName) throws Exception {
                        return new ProfileDao(userName);
                    }
                });

        searchProfilesCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, SearchProfilesDao>() {
                    @Override
                    public SearchProfilesDao load(@Nonnull String query) throws Exception {
                        return new SearchProfilesDao(query);
                    }
                });

        friendsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, FriendsDao>() {
                    @Override
                    public FriendsDao load(@Nonnull String userName) throws Exception {
                        return new FriendsDao(userName);
                    }
                });

        contactsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ContactsDao>() {
                    @Override
                    public ContactsDao load(@Nonnull String userName) throws Exception {
                        return new ContactsDao(userName);
                    }
                });
    }

    @Nonnull
    public ContactsDao getContactsDao(@Nonnull String userName) {
        return contactsCache.getUnchecked(userName);
    }

    @Nonnull
    public FriendsDao getFriendsDao(@Nonnull String userName) {
        return friendsCache.getUnchecked(userName);
    }

    @Nonnull
    public SearchProfilesDao getSearchProfilesDao(@Nonnull String query) {
        return searchProfilesCache.getUnchecked(query);
    }

    @Nonnull
    public Observable<ResponseOrError<User>> getProfileObservable(@Nonnull String userName) {
        return profilesCache.getUnchecked(userName).getProfileObservable();
    }

    @Nonnull
    public Observer<Object> getRefreshProfileObserver(@Nonnull String userName) {
        return profilesCache.getUnchecked(userName).getRefreshSubject();
    }

    @Nonnull
    public ProfileDao getProfileDao(@Nonnull String userName) {
        return profilesCache.getUnchecked(userName);
    }

    public void registerToGcmAction(@Nullable final String token) {
        final Observable<User> user;
        if (Strings.isNullOrEmpty(token)) {
            user = apiService.unregisterGcmToken(RequestBody.create(MediaType.parse("application/json"), RegisterDeviceRequest.EMPTY_PUSHTOKEN));
        } else {
            user = apiService.registerGcmToken(new RegisterDeviceRequest(token));
        }

        user
                .subscribeOn(networkScheduler)
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        userPreferences.setGcmPushToken(token);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogHelper.logThrowableAndCrashlytics(AppuniteGcm.TAG, "Cannot register to gcm", throwable);
                    }
                });
    }

    public class ProfileDao {
        @Nonnull
        private Observable<ResponseOrError<User>> profileObservable;
        @Nonnull
        private PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private PublishSubject<ResponseOrError<User>> updatedProfileLocallySubject = PublishSubject.create();

        public ProfileDao(@Nonnull final String userName) {
            profileObservable = apiService.getProfile(userName)
                    .subscribeOn(networkScheduler)
                    .compose(MoreOperators.<User>refresh(refreshSubject))
                    .compose(ResponseOrError.<User>toResponseOrErrorObservable())
                    .mergeWith(updatedProfileLocallySubject)
                    .compose(MoreOperators.<ResponseOrError<User>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<User>> getProfileObservable() {
            return profileObservable;
        }

        @Nonnull
        public PublishSubject<Object> getRefreshSubject() {
            return refreshSubject;
        }

        @Nonnull
        public Observer<ResponseOrError<User>> updatedProfileLocallyObserver() {
            return updatedProfileLocallySubject;
        }
    }

    public class SearchProfilesDao {
        private final int SEARCH_PAGE_SIZE = 20;

        @Nonnull
        private final Observable<ResponseOrError<SearchProfileResponse>> profilesObservable;
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<ResponseOrError<SearchProfileResponse>> updatedProfilesLocallySubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<Object> loadMoreShoutsSubject = PublishSubject.create();

        public SearchProfilesDao(@Nonnull final String query) {
            final OperatorMergeNextToken<SearchProfileResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<SearchProfileResponse, Observable<SearchProfileResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<SearchProfileResponse> call(SearchProfileResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                if (previousResponse == null) {
                                    pageNumber = 0;
                                }
                                ++pageNumber;

                                final Observable<SearchProfileResponse> apiRequest = apiService
                                        .searchProfiles(query, pageNumber, SEARCH_PAGE_SIZE)
                                        .subscribeOn(networkScheduler);

                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergeSearchProfileResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });


            profilesObservable = loadMoreShoutsSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<SearchProfileResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<SearchProfileResponse>>refresh(refreshSubject))
                    .mergeWith(updatedProfilesLocallySubject)
                    .compose(MoreOperators.<ResponseOrError<SearchProfileResponse>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observer<Object> getLoadMoreShoutsObserver() {
            return loadMoreShoutsSubject;
        }

        @Nonnull
        public Observable<ResponseOrError<SearchProfileResponse>> getProfilesObservable() {
            return profilesObservable;
        }

        @Nonnull
        public PublishSubject<Object> getRefreshSubject() {
            return refreshSubject;
        }

        @Nonnull
        public Observer<ResponseOrError<SearchProfileResponse>> updatedProfileLocallyObserver() {
            return updatedProfilesLocallySubject;
        }
    }

    public abstract class BaseProfileListDao {
        protected final int PAGE_SIZE = 20;

        @Nonnull
        private final Observable<ResponseOrError<ProfilesListResponse>> profilesObservable;
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<ResponseOrError<ProfilesListResponse>> updatedProfilesLocallySubject = PublishSubject.create();
        @Nonnull
        private final PublishSubject<Object> loadMoreShoutsSubject = PublishSubject.create();
        @Nonnull
        protected final String userName;

        public BaseProfileListDao(@Nonnull final String userName) {
            this.userName = userName;
            final OperatorMergeNextToken<ProfilesListResponse, Object> loadMoreOperator =
                    OperatorMergeNextToken.create(new Func1<ProfilesListResponse, Observable<ProfilesListResponse>>() {
                        private int pageNumber = 0;

                        @Override
                        public Observable<ProfilesListResponse> call(ProfilesListResponse previousResponse) {
                            if (previousResponse == null || previousResponse.getNext() != null) {
                                if (previousResponse == null) {
                                    pageNumber = 0;
                                }
                                ++pageNumber;

                                final Observable<ProfilesListResponse> apiRequest = getRequest(pageNumber)
                                        .subscribeOn(networkScheduler);

                                if (previousResponse == null) {
                                    return apiRequest;
                                } else {
                                    return Observable.just(previousResponse).zipWith(apiRequest, new MergeProfilesListResponses());
                                }
                            } else {
                                return Observable.never();
                            }
                        }
                    });


            profilesObservable = loadMoreShoutsSubject.startWith((Object) null)
                    .lift(loadMoreOperator)
                    .compose(ResponseOrError.<ProfilesListResponse>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ResponseOrError<ProfilesListResponse>>refresh(refreshSubject))
                    .mergeWith(updatedProfilesLocallySubject)
                    .compose(MoreOperators.<ResponseOrError<ProfilesListResponse>>cacheWithTimeout(networkScheduler));
        }

        protected abstract Observable<ProfilesListResponse> getRequest(int pageNumber);

        @Nonnull
        public Observer<Object> getLoadMoreShoutsObserver() {
            return loadMoreShoutsSubject;
        }

        @Nonnull
        public Observable<ResponseOrError<ProfilesListResponse>> getProfilesObservable() {
            return profilesObservable;
        }

        @Nonnull
        public PublishSubject<Object> getRefreshSubject() {
            return refreshSubject;
        }

        @Nonnull
        public Observer<ResponseOrError<ProfilesListResponse>> updatedProfileLocallyObserver() {
            return updatedProfilesLocallySubject;
        }
    }

    public class FriendsDao extends BaseProfileListDao {

        public FriendsDao(@Nonnull String userName) {
            super(userName);
        }

        @Override
        protected Observable<ProfilesListResponse> getRequest(int pageNumber) {
            return apiService.facebookFriends(userName, pageNumber, PAGE_SIZE);
        }
    }

    public class ContactsDao extends BaseProfileListDao {

        public ContactsDao(@Nonnull String userName) {
            super(userName);
        }

        @Override
        protected Observable<ProfilesListResponse> getRequest(int pageNumber) {
            return apiService.mutualContacts(userName, pageNumber, PAGE_SIZE);
        }
    }

    @Nonnull
    public Observable<User> updateUser() {
        return apiService.getMyUser()
                .subscribeOn(networkScheduler)
                .compose(ResponseOrError.<User>toResponseOrErrorObservable())
                .compose(ResponseOrError.<User>onlySuccess());
    }

    public class MergeSearchProfileResponses implements Func2<SearchProfileResponse, SearchProfileResponse, SearchProfileResponse> {
        @Override
        public SearchProfileResponse call(SearchProfileResponse previousData, SearchProfileResponse newData) {
            final ImmutableList<User> allItems = ImmutableList.<User>builder()
                    .addAll(previousData.getResults())
                    .addAll(newData.getResults())
                    .build();

            final int count = previousData.getCount() + newData.getCount();
            return new SearchProfileResponse(count, newData.getNext(), newData.getPrevious(), allItems);
        }
    }

    public class MergeProfilesListResponses implements Func2<ProfilesListResponse, ProfilesListResponse, ProfilesListResponse> {
        @Override
        public ProfilesListResponse call(ProfilesListResponse previousData, ProfilesListResponse newData) {
            final ImmutableList<BaseProfile> allItems = ImmutableList.<BaseProfile>builder()
                    .addAll(previousData.getResults())
                    .addAll(newData.getResults())
                    .build();

            return new ProfilesListResponse(newData.getCount(), newData.getNext(), newData.getPrevious(), allItems);
        }
    }


}
