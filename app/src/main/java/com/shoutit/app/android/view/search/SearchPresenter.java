package com.shoutit.app.android.view.search;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.MergeShoutsResponses;
import com.shoutit.app.android.db.SuggestionsTable;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class SearchPresenter {

    private static final int PAGE_SIZE = 20;
    private static final int MINIMUM_LENGTH = 3;

    public enum SearchType {
        SHOUTS,
        PROFILE,
        TAG,
        DISCOVER
    }

    private final BehaviorSubject<String> querySubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showSuggestionsSubject = BehaviorSubject.create();
    private final PublishSubject<String> loadMoreSubject = PublishSubject.create();
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> suggestionsItemsObservable;
    private final Observable<List<BaseAdapterItem>> shoutsAdapterItemsObservable;

    private final ApiService apiService;

    @Inject
    public SearchPresenter(final ApiService apiService,
                           SuggestionsTable suggestionsTable,
                           @ForActivity final Context context,
                           @NetworkScheduler final Scheduler networkScheduler,
                           @UiScheduler Scheduler uiScheduler,
                           @Nonnull final SearchType searchType,
                           @Nullable final String contextItemId,
                           boolean showSuggestions) {
        this.apiService = apiService;

        /** Requests **/
        final OperatorMergeNextToken<ShoutsResponse, String> loadMoreOperator =
                OperatorMergeNextToken.create(new Func2<ShoutsResponse, String, Observable<ShoutsResponse>>() {

                    private int pageNumber = 0;

                    @Override
                    public Observable<ShoutsResponse> call(ShoutsResponse previousResponse, String query) {

                        if (previousResponse == null || previousResponse.getNext() != null) {
                            if (previousResponse == null) {
                                pageNumber = 0;
                            }
                            ++pageNumber;

                            final Observable<ShoutsResponse> apiRequest =
                                    getRequest(pageNumber, query, searchType, contextItemId)
                                            .subscribeOn(networkScheduler);

                            if (previousResponse == null) {
                                return apiRequest;
                            } else {
                                return Observable.just(previousResponse).zipWith(apiRequest, new MergeShoutsResponses());
                            }
                        } else {
                            return Observable.never();
                        }
                    }
                });

        final Observable<ResponseOrError<ShoutsResponse>> shoutsRequest = querySubject
                .distinctUntilChanged()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String query) {
                        return query != null && query.length() >= MINIMUM_LENGTH;
                    }
                })
                .switchMap(new Func1<String, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(String query) {
                        return loadMoreSubject.startWith(query)
                                .lift(loadMoreOperator)
                                .compose(ResponseOrError.<ShoutsResponse>toResponseOrErrorObservable())
                                .compose(MoreOperators.<ResponseOrError<ShoutsResponse>>cacheWithTimeout(networkScheduler));
                    }
                })
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<ShoutsResponse> successShoutsRequest = shoutsRequest
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess());

        /** Shout Items **/
        final Observable<List<BaseAdapterItem>> shoutsItems = successShoutsRequest
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        return ImmutableList.copyOf(Iterables.transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(Shout input) {
                                return new ShoutAdapterItem(input, context, shoutSelectedSubject);
                            }
                        }));
                    }
                });

        final Observable<List<BaseAdapterItem>> clearResultsObservable = querySubject
                .distinctUntilChanged()
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String query) {
                        return query == null || query.length() < MINIMUM_LENGTH;
                    }
                })
                .map(new Func1<String, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(String s) {
                        return ImmutableList.of();
                    }
                });

        shoutsAdapterItemsObservable = Observable.merge(shoutsItems, clearResultsObservable);


        /** Suggestion Items **/
        final Observable<List<String>> suggestionsObservable = suggestionsTable
                .getAllSuggestionsObservable()
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler);

        suggestionsItemsObservable = Observable.combineLatest(
                suggestionsObservable,
                showSuggestionsSubject.startWith(showSuggestions),
                new Func2<List<String>, Boolean, List<String>>() {
                    @Override
                    public List<String> call(List<String> suggestions, Boolean show) {
                        if (show) {
                            return suggestions;
                        } else {
                            return null;
                        }
                    }
                })
                .filter(Functions1.isNotNull())
                .map(new Func1<List<String>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<String> suggestions) {
                        return ImmutableList.copyOf(Iterables.transform(suggestions, new Function<String, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(@Nullable String input) {
                                return new SuggestionAdapterItem(input);
                            }
                        }));
                    }
                });
    }

    private Observable<ShoutsResponse> getRequest(int pageNumber,
                                                  @Nonnull String query,
                                                  @Nonnull SearchType searchType,
                                                  @Nullable String contextItemId) {
        switch (SearchType.values()[searchType.ordinal()]) {
            case PROFILE:
                return apiService.searchProfileShouts(query, pageNumber, PAGE_SIZE, contextItemId);
            case SHOUTS:
                return apiService.searchShouts(query, pageNumber, PAGE_SIZE);
            case TAG:
                return apiService.searchTagShouts(query, pageNumber, PAGE_SIZE, contextItemId);
            case DISCOVER:
                return apiService.searchDiscoverShouts(query, pageNumber, PAGE_SIZE, contextItemId);
            default:
                throw new RuntimeException("Unknwon profile type: " + SearchType.values()[searchType.ordinal()]);
        }
    }

    public void loadMoreShouts() {
        loadMoreSubject.onNext(querySubject.getValue());
    }

    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedSubject;
    }

    public Observable<List<BaseAdapterItem>> getShoutsAdapterItemsObservable() {
        return shoutsAdapterItemsObservable;
    }

    public Observable<List<BaseAdapterItem>> getSuggestionsItemsObservable() {
        return suggestionsItemsObservable;
    }

    public void showOrHideSuggestion(boolean show) {
        showSuggestionsSubject.onNext(show);
    }

    public class SuggestionAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final String suggestion;

        public SuggestionAdapterItem(@Nonnull String suggestion) {
            this.suggestion = suggestion;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof SuggestionAdapterItem &&
                    suggestion.equals(((SuggestionAdapterItem) item).suggestion);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SuggestionAdapterItem &&
                    suggestion.equals(((SuggestionAdapterItem) item).suggestion);
        }
    }
}
