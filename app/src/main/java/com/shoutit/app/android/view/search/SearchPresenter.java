package com.shoutit.app.android.view.search;

import android.content.Context;
import android.content.Intent;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.MergeShoutsResponses;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsActivity;
import com.shoutit.app.android.view.search.subsearch.SubSearchActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
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
        DISCOVER,
        BROWSE
    }

    private final PublishSubject<String> loadMoreSubject = PublishSubject.create();
    private final PublishSubject<String> fillSearchWithSuggestionSubject = PublishSubject.create();
    private final PublishSubject<String> suggestionClickedSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> suggestionsAdapterItemsObservable;
    private final Observable<String> hintNameObservable;
    private final Observable<Intent> subSearchSubmittedObservable;

    private final ApiService apiService;
    private final SearchQueryPresenter searchQueryPresenter;

    @Inject
    public SearchPresenter(final ApiService apiService,
                           SearchQueryPresenter searchQueryPresenter,
                           @NetworkScheduler final Scheduler networkScheduler,
                           @UiScheduler Scheduler uiScheduler,
                           @Nonnull final SearchType searchType,
                           @Nullable final String contextItemId,
                           @Nullable final String contextualItemName,
                           @ForActivity final Context context) {
        this.apiService = apiService;
        this.searchQueryPresenter = searchQueryPresenter;

        /** Suggestions **/
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
                                    getSuggestionsRequest(pageNumber, query, searchType, contextItemId)
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

        final Observable<ResponseOrError<ShoutsResponse>> shoutsRequest = searchQueryPresenter.getQuerySubject()
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

        suggestionsAdapterItemsObservable = successShoutsRequest
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                        if (!shoutsResponse.getShouts().isEmpty()) {
                            builder.addAll(Iterables.transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                                @Nullable
                                @Override
                                public BaseAdapterItem apply(Shout input) {
                                    return new SearchSuggestionAdapterItem(input, fillSearchWithSuggestionSubject, suggestionClickedSubject);
                                }
                            }));
                            builder.add(new NoDataAdapterItem());
                        }

                        return builder.build();
                    }
                });

        /** Hint title **/
        final String hintName = context.getString(R.string.search_hint, contextualItemName);
        hintNameObservable = Observable.just(hintName);

        /** Search Submitted **/
        subSearchSubmittedObservable = searchQueryPresenter.getQuerySubmittedSubject()
                .map(new Func1<String, Intent>() {
                    @Override
                    public Intent call(String searchQuery) {
                        return SearchShoutsResultsActivity.newIntent(context, searchQuery, contextItemId, searchType);
                    }
                });

    }

    private Observable<ShoutsResponse> getSuggestionsRequest(int pageNumber,
                                                             @Nonnull String query,
                                                             @Nonnull SearchType searchType,
                                                             @Nullable String contextItemId) {
        // TODO There will be separate requests for suggestions in future
        switch (SearchType.values()[searchType.ordinal()]) {
            case PROFILE:
                return apiService.searchProfileShouts(query, pageNumber, PAGE_SIZE, contextItemId);
            case SHOUTS:
                return apiService.searchShouts(query, pageNumber, PAGE_SIZE, null, null, null);
            case TAG:
                return apiService.searchTagShouts(query, pageNumber, PAGE_SIZE, contextItemId, null, null, null);
            case DISCOVER:
                return apiService.searchDiscoverShouts(query, pageNumber, PAGE_SIZE, contextItemId);
            default:
                throw new RuntimeException("Unknwon profile type: " + SearchType.values()[searchType.ordinal()]);
        }
    }

    public Observable<Intent> getSubSearchSubmittedObservable() {
        return subSearchSubmittedObservable;
    }

    public Observable<String> getHintNameObservable() {
        return hintNameObservable;
    }

    public BehaviorSubject<String> getQuerySubject() {
        return searchQueryPresenter.getQuerySubject();
    }

    public void loadMoreShouts() {
        loadMoreSubject.onNext(searchQueryPresenter.getQuerySubject().getValue());
    }

    public Observable<String> getShoutSelectedObservable() {
        return suggestionClickedSubject;
    }

    public Observable<List<BaseAdapterItem>> getSuggestionsAdapterItemsObservable() {
        return suggestionsAdapterItemsObservable;
    }

    public class SearchSuggestionAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Shout shout;
        private final Observer<String> fillSearchWithSuggestionObserver;
        private final Observer<String> suggestionClickedObserver;

        public SearchSuggestionAdapterItem(@Nonnull Shout shout,
                                           Observer<String> fillSearchWithSuggestionObserver,
                                           Observer<String> suggestionClickedObserver) {
            this.shout = shout;
            this.fillSearchWithSuggestionObserver = fillSearchWithSuggestionObserver;
            this.suggestionClickedObserver = suggestionClickedObserver;
        }

        public void onFillSearchWithSuggestion() {
            fillSearchWithSuggestionObserver.onNext(shout.getTitle());
        }

        public void onSuggestionClick() {
            suggestionClickedObserver.onNext(shout.getId());
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof SearchSuggestionAdapterItem
                    && shout.getId().equals(((SearchSuggestionAdapterItem) item).shout.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SearchSuggestionAdapterItem &&
                    shout.equals(((SearchSuggestionAdapterItem) item).shout);
        }

        public String getSuggestionText() {
            return shout.getTitle();
        }
    }
}
