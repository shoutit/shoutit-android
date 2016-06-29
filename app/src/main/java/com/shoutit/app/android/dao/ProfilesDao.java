package com.shoutit.app.android.dao;

import com.appunite.appunitegcm.AppuniteGcm;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.PagesSuggestionResponse;
import com.shoutit.app.android.api.model.ProfilesListResponse;
import com.shoutit.app.android.api.model.RegisterDeviceRequest;
import com.shoutit.app.android.api.model.SearchProfileResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.UserSuggestionResponse;
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
    private final LoadingCache<FriendsSuggestionPointer, UsersSuggestionDao> usersSuggestionCache;
    @Nonnull
    private final LoadingCache<FriendsSuggestionPointer, PagesSuggestionDao> pagesSuggestionCache;
    @Nonnull
    private final LoadingCache<String, AdminsDao> adminsCache;

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

        usersSuggestionCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<FriendsSuggestionPointer, UsersSuggestionDao>() {
                    @Override
                    public UsersSuggestionDao load(final FriendsSuggestionPointer pointer) throws Exception {
                        return new UsersSuggestionDao(pointer);
                    }
                });

        pagesSuggestionCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<FriendsSuggestionPointer, PagesSuggestionDao>() {
                    @Override
                    public PagesSuggestionDao load(final FriendsSuggestionPointer pointer) throws Exception {
                        return new PagesSuggestionDao(pointer);
                    }
                });

        adminsCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, AdminsDao>() {
                    @Override
                    public AdminsDao load(@Nonnull String userName) throws Exception {
                        return new AdminsDao(userName);
                    }
                });
    }

    @Nonnull
    public UsersSuggestionDao getUsersSuggestionDao(@Nonnull FriendsSuggestionPointer pointer) {
        return usersSuggestionCache.getUnchecked(pointer);
    }

    @Nonnull
    public PagesSuggestionDao getPagesSuggestionDao(@Nonnull FriendsSuggestionPointer pointer) {
        return pagesSuggestionCache.getUnchecked(pointer);
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

    @Nonnull
    public AdminsDao getAdminsDao(@Nonnull String userName) {
        return adminsCache.getUnchecked(userName);
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

    public class FriendsDao extends BaseProfileListDao {

        public FriendsDao(@Nonnull String userName) {
            super(userName, networkScheduler);
        }

        @Override
        protected Observable<ProfilesListResponse> getRequest(int pageNumber) {
            return apiService.facebookFriends(userName, pageNumber, PAGE_SIZE);
        }
    }

    public class ContactsDao extends BaseProfileListDao {

        public ContactsDao(@Nonnull String userName) {
            super(userName, networkScheduler);
        }

        @Override
        protected Observable<ProfilesListResponse> getRequest(int pageNumber) {
            return apiService.mutualContacts(userName, pageNumber, PAGE_SIZE);
        }
    }

    public class UsersSuggestionDao extends BaseProfileListDao {

        private final UserLocation userLocation;
        private FriendsSuggestionPointer pointer;

        public UsersSuggestionDao(@Nonnull FriendsSuggestionPointer pointer) {
            super(pointer.getUserName(), networkScheduler);
            this.pointer = pointer;
            userLocation = pointer.getUserLocation();
        }

        @Override
        protected Observable<ProfilesListResponse> getRequest(int pageNumber) {
            if (userLocation != null) {
                return apiService.usersSuggestion(userLocation.getCountry(), userLocation.getState(), userLocation.getCity(), pageNumber, PAGE_SIZE)
                        .map((Func1<UserSuggestionResponse, ProfilesListResponse>) userSuggestionResponse -> userSuggestionResponse);
            } else {
                return apiService.usersSuggestion(null, null, null, pageNumber, PAGE_SIZE)
                        .map((Func1<UserSuggestionResponse, ProfilesListResponse>) userSuggestionResponse -> userSuggestionResponse);
            }
        }
    }

    public class PagesSuggestionDao extends BaseProfileListDao {

        private final UserLocation userLocation;
        private FriendsSuggestionPointer pointer;

        public PagesSuggestionDao(@Nonnull FriendsSuggestionPointer pointer) {
            super(pointer.getUserName(), networkScheduler);
            this.pointer = pointer;
            userLocation = pointer.getUserLocation();
        }

        @Override
        protected Observable<ProfilesListResponse> getRequest(final int pageNumber) {
            if (userLocation != null) {
                return apiService.pagesSuggestion(userLocation.getCountry(), userLocation.getState(), userLocation.getCity(), pageNumber, PAGE_SIZE)
                        .map((Func1<PagesSuggestionResponse, ProfilesListResponse>) pagesSuggestionResponse -> pagesSuggestionResponse);
            } else {
                return apiService.pagesSuggestion(null, null, null, pageNumber, PAGE_SIZE)
                        .map((Func1<PagesSuggestionResponse, ProfilesListResponse>) pagesSuggestionResponse -> pagesSuggestionResponse);
            }
        }
    }

    public class AdminsDao extends BaseProfileListDao {

        public AdminsDao(@Nonnull String userName) {
            super(userName, networkScheduler);
        }

        @Override
        protected Observable<ProfilesListResponse> getRequest(int pageNumber) {
            return apiService.getAdmins(userName, pageNumber, PAGE_SIZE);
        }
    }

    public static class FriendsSuggestionPointer {
        @android.support.annotation.Nullable
        private final UserLocation userLocation;
        private final String userName;

        public FriendsSuggestionPointer(@android.support.annotation.Nullable UserLocation userLocation, String userName) {
            this.userLocation = userLocation;
            this.userName = userName;
        }

        @android.support.annotation.Nullable
        public UserLocation getUserLocation() {
            return userLocation;
        }

        public String getUserName() {
            return userName;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final FriendsSuggestionPointer that = (FriendsSuggestionPointer) o;
            return Objects.equal(userLocation, that.userLocation) &&
                    Objects.equal(userName, that.userName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(userLocation, userName);
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

}
