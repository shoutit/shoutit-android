package com.shoutit.app.android.view.search.main.shouts;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.view.search.SearchPresenter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class SearchWithRecentsPresenter {

    private final PublishSubject<Object> clearAllRecentsSubject = PublishSubject.create();
    private final PublishSubject<String> removeRecentSubject = PublishSubject.create();

    private final Observable<Object> clearAllRecentsObservable;
    private final Observable<Object> removeRecentObservable;
    private final Observable<List<BaseAdapterItem>> recentsSearchesObservable;

    private final SearchPresenter searchPresenter;

    @Inject
    public SearchWithRecentsPresenter(final RecentSearchesTable recentSearchesTable,
                                      @NetworkScheduler final Scheduler networkScheduler,
                                      @UiScheduler Scheduler uiScheduler,
                                      final SearchPresenter searchPresenter) {
        this.searchPresenter = searchPresenter;

        /** Recent Searches Items **/
        final Observable<List<BaseAdapterItem>> recentsObservable = recentSearchesTable
                .getAllRecentSearchesObservable()
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .filter(Functions1.isNotNull())
                .map(new Func1<List<String>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<String> recents) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        if (!recents.isEmpty()) {
                            builder.add(new RecentsHeaderAdapterItem(clearAllRecentsSubject));
                        }
                        builder.addAll(Iterables.transform(recents, new Function<String, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(@Nullable String input) {
                                return new RecentSearchAdapterItem(input, removeRecentSubject);
                            }
                        }));

                        if (!recents.isEmpty()) {
                            builder.add(new NoDataAdapterItem());
                        }

                        return builder.build();
                    }
                });

        final Observable<List<BaseAdapterItem>> showRecentsOnEmptyQueryObservable = searchPresenter.getQuerySubject()
                .distinctUntilChanged()
                .filter(Functions1.isNullOrEmpty())
                .switchMap(new Func1<String, Observable<List<BaseAdapterItem>>>() {
                    @Override
                    public Observable<List<BaseAdapterItem>> call(String s) {
                        return recentsObservable;
                    }
                });

        recentsSearchesObservable = Observable.merge(recentsObservable, showRecentsOnEmptyQueryObservable)
                .filter(new Func1<List<BaseAdapterItem>, Boolean>() {
                    @Override
                    public Boolean call(List<BaseAdapterItem> baseAdapterItems) {
                        return Strings.isNullOrEmpty(searchPresenter.getQuerySubject().getValue());
                    }
                });


        /** Suggestion db action **/
        clearAllRecentsObservable = clearAllRecentsSubject
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        recentSearchesTable.clearRecentSearch();
                        return null;
                    }
                });

        removeRecentObservable = removeRecentSubject
                .map(new Func1<String, Object>() {
                    @Override
                    public Object call(String query) {
                        recentSearchesTable.removeRecentSearch(query);
                        return null;
                    }
                });
    }

    public Observable<Object> getClearAllRecentsObservable() {
        return clearAllRecentsObservable;
    }

    public Observable<Object> getRemoveRecentObservable() {
        return removeRecentObservable;
    }

    public Observable<List<BaseAdapterItem>> getRecentsSearchesObservable() {
        return recentsSearchesObservable;
    }

    public SearchPresenter getSearchPresenter() {
        return searchPresenter;
    }

    public class RecentSearchAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final String suggestion;
        private final Observer<String> suggestionRemoveObserver;

        public RecentSearchAdapterItem(@Nonnull String suggestion, Observer<String> suggestionRemoveObserver) {
            this.suggestion = suggestion;
            this.suggestionRemoveObserver = suggestionRemoveObserver;
        }

        public void onSuggestionRemove() {
            suggestionRemoveObserver.onNext(suggestion);
        }

        @Nonnull
        public String getSuggestion() {
            return suggestion;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof RecentSearchAdapterItem &&
                    suggestion.equals(((RecentSearchAdapterItem) item).suggestion);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof RecentSearchAdapterItem &&
                    suggestion.equals(((RecentSearchAdapterItem) item).suggestion);
        }
    }

    public class RecentsHeaderAdapterItem extends BaseNoIDAdapterItem {

        private final Observer<Object> clearAllRecentsObserver;

        public RecentsHeaderAdapterItem(Observer<Object> clearAllRecentsObserver) {
            this.clearAllRecentsObserver = clearAllRecentsObserver;
        }

        public void onClearAllRecentsClicked() {
            clearAllRecentsObserver.onNext(null);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof RecentsHeaderAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof RecentsHeaderAdapterItem;
        }
    }
}
