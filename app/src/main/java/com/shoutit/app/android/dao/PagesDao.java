package com.shoutit.app.android.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.PagesResponse;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

@Singleton
public class PagesDao {

    private static final int PAGE_SIZE = 20;

    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    @Nonnull
    private final Observable<ResponseOrError<PagesResponse>> pagesObservable;

    @Inject
    public PagesDao(@Nonnull ApiService apiService,
                    @Nonnull @NetworkScheduler Scheduler networkScheduler) {

        final OperatorMergeNextToken<PagesResponse, Object> loadMoreOperator =
                OperatorMergeNextToken.create(new Func1<PagesResponse, Observable<PagesResponse>>() {
                    private int pageNumber = 0;

                    @Override
                    public Observable<PagesResponse> call(PagesResponse previousResponse) {
                        if (previousResponse == null || previousResponse.getNext() != null) {
                            if (previousResponse == null) {
                                pageNumber = 0;
                            }
                            ++pageNumber;

                            final Observable<PagesResponse> apiRequest = apiService.myPages(User.ME, pageNumber, PAGE_SIZE)
                                    .subscribeOn(networkScheduler);

                            if (previousResponse == null) {
                                return apiRequest;
                            } else {
                                return Observable.just(previousResponse).zipWith(apiRequest, new MergePagesResponses());
                            }
                        } else {
                            return Observable.never();
                        }
                    }
                });

        pagesObservable = loadMoreSubject.startWith((Object) null)
                .lift(loadMoreOperator)
                .compose(ResponseOrError.<PagesResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<PagesResponse>>cacheWithTimeout(networkScheduler))
                .mergeWith(Observable.never());
    }

    @Nonnull
    public Observable<ResponseOrError<PagesResponse>> getPagesObservable() {
        return pagesObservable;
    }

    public PublishSubject<Object> getLoadMoreSubject() {
        return loadMoreSubject;
    }
}
