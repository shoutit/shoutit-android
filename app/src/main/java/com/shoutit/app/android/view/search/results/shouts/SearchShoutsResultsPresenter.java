package com.shoutit.app.android.view.search.results.shouts;

import android.content.Context;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.SearchShoutPointer;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class SearchShoutsResultsPresenter {

    private final PublishSubject<Object> layoutManagerSwitchSubject = PublishSubject.create();
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> adapterItems;
    private final Observable<Boolean> progressObservable;
    private final Observable<Throwable> errorObservable;

    public SearchShoutsResultsPresenter(final ShoutsDao dao, @Nonnull final String searchQuery,
                                        final SearchPresenter.SearchType searchType, UserPreferences userPreferences,
                                        @Nullable final String contextItemId, @ForActivity final Context context) {

        final Observable<ResponseOrError<ShoutsResponse>> shoutsRequest = userPreferences.getLocationObservable()
                .filter(Functions1.isNotNull())
                .first()
                .switchMap(new Func1<UserLocation, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(UserLocation userLocation) {
                        return dao.getSearchShoutsDao(new SearchShoutPointer(
                                searchQuery, searchType, userLocation, contextItemId))
                                .getShoutsObservable();
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        adapterItems = shoutsRequest
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        if (shoutsResponse.getShouts().isEmpty()) {
                            return ImmutableList.<BaseAdapterItem>of(new NoDataAdapterItem());
                        } else {
                            final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                            builder.add(new ShoutHeaderAdapterItem(searchQuery, shoutsResponse.getCount(), layoutManagerSwitchSubject));
                            builder.addAll(Lists.transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                                @Nullable
                                @Override
                                public BaseAdapterItem apply(@Nullable Shout input) {
                                    return new ShoutAdapterItem(input, context, shoutSelectedSubject);
                                }
                            }));
                            return builder.build();
                        }
                    }
                });

        progressObservable = shoutsRequest.map(Functions1.returnFalse())
                .startWith(true);

        errorObservable = shoutsRequest.compose(ResponseOrError.<ShoutsResponse>onlyError());

    }

    public Observable<List<BaseAdapterItem>> getAdapterItems() {
        return adapterItems;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public class ShoutHeaderAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final String searchQuery;
        private final int totalItemsCount;
        private final Observer<Object> layoutManagerSwitchObserver;

        public ShoutHeaderAdapterItem(@Nonnull String searchQuery, int totalItemsCount,
                                      Observer<Object> layoutManagerSwitchObserver) {
            this.searchQuery = searchQuery;
            this.totalItemsCount = totalItemsCount;
            this.layoutManagerSwitchObserver = layoutManagerSwitchObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ShoutHeaderAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }

        public Observer<Object> getLayoutManagerSwitchObserver() {
            return layoutManagerSwitchObserver;
        }

        public int getTotalItemsCount() {
            return totalItemsCount;
        }

        @Nonnull
        public String getSearchQuery() {
            return searchQuery;
        }
    }
}
