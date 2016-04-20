package com.shoutit.app.android.dao;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ListeningResponse;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.Tag;
import com.shoutit.app.android.api.model.User;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class ListeningsDao {

    private final Observable<ResponseOrError<ListeningResponse>> listeningObservable;
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

    public ListeningsDao(final ApiService apiService, @NetworkScheduler final Scheduler networkScheduler) {

        final OperatorMergeNextToken<ListeningResponse, Object> loadMoreOperator =
                OperatorMergeNextToken.create(new Func1<ListeningResponse, Observable<ListeningResponse>>() {
                    private int pageNumber = 0;

                    @Override
                    public Observable<ListeningResponse> call(ListeningResponse previousResponse) {
                        if (previousResponse == null || previousResponse.getNext() != null) {
                            if (previousResponse == null) {
                                pageNumber = 0;
                            }
                            ++pageNumber;

                            final Observable<ListeningResponse> apiRequest = apiService
                                    .listenings()
                                    .subscribeOn(networkScheduler);

                            if (previousResponse == null) {
                                return apiRequest;
                            } else {
                                return Observable.just(previousResponse).zipWith(apiRequest, new MergeListeningResponses());
                            }
                        } else {
                            return Observable.never();
                        }
                    }
                });

        listeningObservable = loadMoreSubject.startWith((Object) null)
                .lift(loadMoreOperator)
                .compose(ResponseOrError.<ListeningResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<ResponseOrError<ListeningResponse>>cacheWithTimeout(networkScheduler));
    }

    @NonNull
    public Observable<ResponseOrError<ListeningResponse>> lgetLsteningObservable() {
        return listeningObservable;
    }

    @NonNull
    public Observer<Object> getLoadMoreObserver() {
        return loadMoreSubject;
    }

    private class MergeListeningResponses implements Func2<ListeningResponse, ListeningResponse, ListeningResponse> {
        @Override
        public ListeningResponse call(ListeningResponse previousResponses, ListeningResponse lastResponse) {
            final List<User> user = previousResponses.getUsers();
            final List<Page> pages = previousResponses.getPages();
            final List<Tag> tags = previousResponses.getTags();

            if (user != null && lastResponse.getUsers() != null) {
                user.addAll(lastResponse.getUsers());
            }

            if (pages != null && lastResponse.getPages() != null) {
                pages.addAll(lastResponse.getPages());
            }

            if (tags != null && lastResponse.getTags() != null) {
                tags.addAll(lastResponse.getTags());
            }

            return new ListeningResponse(lastResponse.getCount(), lastResponse.getNext(),
                    lastResponse.getPrevious(), user, pages, tags);
        }
    }
}
