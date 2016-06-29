package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.shoutit.app.android.api.model.ProfilesListResponse;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

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

    public BaseProfileListDao(@Nonnull final String userName,
                              @Nonnull @NetworkScheduler Scheduler networkScheduler) {
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
    public Observer<Object> getLoadMoreObserver() {
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
