package com.shoutit.app.android.view.search;

import android.content.Context;
import android.content.Intent;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Suggestion;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class SearchPresenter {

    public static final int MINIMUM_LENGTH = 3;

    public enum SearchType {
        SHOUTS,
        PROFILE,
        TAG,
        DISCOVER,
        BROWSE
    }

    private final PublishSubject<String> suggestionClickedSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> suggestionsAdapterItemsObservable;
    private final Observable<String> hintNameObservable;
    private final Observable<Intent> subSearchSubmittedObservable;
    private final Observable<Intent> suggestionClickedObservable;

    private final SearchQueryPresenter searchQueryPresenter;

    @Inject
    public SearchPresenter(final ApiService apiService,
                           final SearchQueryPresenter searchQueryPresenter,
                           @NetworkScheduler final Scheduler networkScheduler,
                           @UiScheduler final Scheduler uiScheduler,
                           @Nonnull final SearchType searchType,
                           @Nullable final String contextItemId,
                           @Nullable final String contextualItemName,
                           @Nonnull UserPreferences userPreferences,
                           @ForActivity final Context context) {
        this.searchQueryPresenter = searchQueryPresenter;

        final UserLocation userLocation = userPreferences.getLocation();

        final Observable<ResponseOrError<List<Suggestion>>> suggestionsObservable = searchQueryPresenter
                .getQuerySubject()
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String searchQuery) {
                        return searchQuery != null && searchQuery.length() >= MINIMUM_LENGTH;
                    }
                })
                .throttleLast(400, TimeUnit.MILLISECONDS)
                .switchMap(new Func1<String, Observable<ResponseOrError<List<Suggestion>>>>() {
                    @Override
                    public Observable<ResponseOrError<List<Suggestion>>> call(String query) {
                        final String categorySlug = searchType == SearchType.TAG ? contextItemId : null;
                        final String country = userLocation == null ? null : userLocation.getCountry();
                        return apiService.searchSuggestions(query, categorySlug, country)
                                .subscribeOn(networkScheduler)
                                .compose(ResponseOrError.<List<Suggestion>>toResponseOrErrorObservable())
                                .observeOn(uiScheduler);

                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<List<Suggestion>>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> clearSuggestionsIfQueryTooShort =
                searchQueryPresenter.getQuerySubject()
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

        suggestionsAdapterItemsObservable = suggestionsObservable
                .compose(ResponseOrError.<List<Suggestion>>onlySuccess())
                .map(new Func1<List<Suggestion>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Suggestion> suggestions) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        if (!suggestions.isEmpty()) {
                            builder.addAll(Iterables.transform(suggestions, new Function<Suggestion, BaseAdapterItem>() {
                                @Nullable
                                @Override
                                public BaseAdapterItem apply(Suggestion input) {
                                    return new SearchSuggestionAdapterItem(
                                            input,
                                            searchQueryPresenter.getFillSearchWithSuggestionObserver(),
                                            suggestionClickedSubject);
                                }
                            }));
                            builder.add(new NoDataAdapterItem());
                        }

                        return builder.build();
                    }
                }).mergeWith(clearSuggestionsIfQueryTooShort);

        final String hintName = context.getString(R.string.search_hint, contextualItemName);
        hintNameObservable = Observable.just(hintName);

        subSearchSubmittedObservable = searchQueryPresenter.getQuerySubmittedSubject()
                .map(new Func1<String, Intent>() {
                    @Override
                    public Intent call(String searchQuery) {
                        return SearchShoutsResultsActivity.newIntent(context, searchQuery, contextItemId, searchType);
                    }
                });

        suggestionClickedObservable = suggestionClickedSubject
                .map(new Func1<String, Intent>() {
                    @Override
                    public Intent call(String searchQuery) {
                        return SearchShoutsResultsActivity.newIntent(context, searchQuery, contextItemId, searchType);
                    }
                });
    }

    public Observable<Intent> getSuggestionClickedObservable() {
        return suggestionClickedObservable;
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

    public Observable<List<BaseAdapterItem>> getSuggestionsAdapterItemsObservable() {
        return suggestionsAdapterItemsObservable;
    }

    public class SearchSuggestionAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Suggestion suggestion;
        private final Observer<String> fillSearchWithSuggestionObserver;
        private final Observer<String> suggestionClickedObserver;

        public SearchSuggestionAdapterItem(@Nonnull Suggestion suggestion,
                                           Observer<String> fillSearchWithSuggestionObserver,
                                           Observer<String> suggestionClickedObserver) {
            this.suggestion = suggestion;
            this.fillSearchWithSuggestionObserver = fillSearchWithSuggestionObserver;
            this.suggestionClickedObserver = suggestionClickedObserver;
        }

        public void onFillSearchWithSuggestion() {
            fillSearchWithSuggestionObserver.onNext(suggestion.getTerm());
        }

        public void onSuggestionClick() {
            suggestionClickedObserver.onNext(suggestion.getTerm());
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof SearchSuggestionAdapterItem
                    && getSuggestionText().equals(((SearchSuggestionAdapterItem) item).getSuggestionText());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SearchSuggestionAdapterItem &&
                    getSuggestionText().equals(((SearchSuggestionAdapterItem) item).getSuggestionText());
        }

        public String getSuggestionText() {
            return suggestion.getTerm().trim();
        }
    }
}
