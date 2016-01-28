package com.shoutit.app.android.view.home;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Discover;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.constants.RequestsConstants;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class HomePresenter {

    private final static int PAGE_SIZE = 20;

    @Nonnull
    private final Observable<List<BaseAdapterItem>> allDiscoversObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> allShoutsObservable;
    @Nonnull
    private final PublishSubject<Object> loadMoreDiscovers = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> loadMoreShouts = PublishSubject.create();
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final Observable<Boolean> progressObservable;


    @Inject
    public HomePresenter(@Nonnull final ApiService apiService,
                         @Nonnull @NetworkScheduler final Scheduler networkScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;

        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> myShoutsObservable =
                loadMoreShouts.startWith((Object) null)
                        .lift(getShoutsLoadMoreOperator(isUserLoggedIn()))
                        .compose(ResponseOrError.<ShoutsResponse>toResponseOrErrorObservable())
                        .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        allShoutsObservable = myShoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .filter(new Func1<ShoutsResponse, Boolean>() {
                    @Override
                    public Boolean call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse != null && !shoutsResponse.getShouts().isEmpty();
                    }
                })
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        final List<BaseAdapterItem> items = new ArrayList<>();
                        for (Shout shout : shoutsResponse.getShouts()) {
                            items.add(new ShoutAdapterItem(shout));
                        }

                        return new ImmutableList.Builder<BaseAdapterItem>()
                                .add(new ShoutHeaderAdapterItem(isUserLoggedIn()))
                                .addAll(items)
                                .build();
                    }
                });

        /** Discovers **/
        final Observable<ResponseOrError<DiscoverResponse>> discoverObservable =
                loadMoreDiscovers.startWith((Object) null)
                        .lift(getDiscoverLoadMoreOperator())
                        .compose(ResponseOrError.<DiscoverResponse>toResponseOrErrorObservable())
                        .compose(ObservableExtensions.<ResponseOrError<DiscoverResponse>>behaviorRefCount());

        allDiscoversObservable = discoverObservable
                .compose(ResponseOrError.<DiscoverResponse>onlySuccess())
                .filter(new Func1<DiscoverResponse, Boolean>() {
                    @Override
                    public Boolean call(DiscoverResponse discoverResponse) {
                        return discoverResponse != null && !discoverResponse.getDiscovers().isEmpty();
                    }
                })
                .map(new Func1<DiscoverResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(DiscoverResponse response) {
                        final List<BaseAdapterItem> items = new ArrayList<>();
                        for (Discover discover : response.getDiscovers()) {
                            items.add(new DiscoverAdapterItem(discover));
                        }

                        return new ImmutableList.Builder<BaseAdapterItem>()
                                .add(new DiscoverHeaderAdapterItem())
                                .addAll(items)
                                .build();
                    }
                });


        /** Progress and Error **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(myShoutsObservable),
                ResponseOrError.transform(discoverObservable)));

        progressObservable = Observable.merge(errorObservable, allDiscoversObservable, allShoutsObservable)
                .map(Functions1.returnFalse());

    }

    @Nonnull
    private OperatorMergeNextToken<ShoutsResponse, Object> getShoutsLoadMoreOperator(final boolean isUserLoggedIn) {
        return OperatorMergeNextToken.create(new Func1<ShoutsResponse, Observable<ShoutsResponse>>() {
            private int pageNumber = 0;

            @Override
            public Observable<ShoutsResponse> call(ShoutsResponse response) {
                if (response == null || response.getNext() != null) {
                    ++pageNumber;
                    final Observable<ShoutsResponse> apiRequest;
                    if (isUserLoggedIn) {
                        apiRequest = apiService
                                .home(RequestsConstants.USER_ME, pageNumber, PAGE_SIZE)
                                .subscribeOn(networkScheduler);
                    } else {
                        apiRequest = apiService
                                .shoutsForCountry("ADL", pageNumber, PAGE_SIZE)
                                .subscribeOn(networkScheduler);
                    }

                    return Observable.just(response).zipWith(apiRequest, new MergeShoutsResponses());
                } else {
                    return Observable.never();
                }
            }
        });
    }

    @Nonnull
    private OperatorMergeNextToken<DiscoverResponse, Object> getDiscoverLoadMoreOperator() {
        return OperatorMergeNextToken.create(new Func1<DiscoverResponse, Observable<DiscoverResponse>>() {
            private int pageNumber = 0;

            @Override
            public Observable<DiscoverResponse> call(DiscoverResponse response) {
                if (response == null || response.getNext() != null) {
                    ++pageNumber;
                    final Observable<DiscoverResponse> apiRequest = apiService
                            .discovers("ADL", pageNumber, PAGE_SIZE)
                            .subscribeOn(networkScheduler);

                    return Observable.just(response).zipWith(apiRequest, new MergeDiscoverResponses());
                } else {
                    return Observable.never();
                }
            }
        });
    }

    private class MergeShoutsResponses implements Func2<ShoutsResponse, ShoutsResponse, ShoutsResponse> {
        @Override
        public ShoutsResponse call(ShoutsResponse previousData, ShoutsResponse newData) {
            final ImmutableList<Shout> allItems = ImmutableList.<Shout>builder()
                    .addAll(previousData.getShouts())
                    .addAll(newData.getShouts())
                    .build();

            final int count = previousData.getCount() + newData.getCount();
            return new ShoutsResponse(count, newData.getNext(), newData.getPrevious(), allItems);
        }
    }

    private class MergeDiscoverResponses implements Func2<DiscoverResponse, DiscoverResponse, DiscoverResponse> {
        @Override
        public DiscoverResponse call(DiscoverResponse previousData, DiscoverResponse newData) {
            final ImmutableList<Discover> allItems = ImmutableList.<Discover>builder()
                    .addAll(previousData.getDiscovers())
                    .addAll(newData.getDiscovers())
                    .build();

            final int count = previousData.getCount() + newData.getCount();
            return new DiscoverResponse(count, newData.getNext(), newData.getPrevious(), allItems);
        }
    }

    private boolean isUserLoggedIn() {
        return true;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getDiscoversObservable() {
        return allDiscoversObservable;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllShoutsObservable() {
        return allShoutsObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public class ShoutAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Shout shout;

        public ShoutAdapterItem(@Nonnull Shout shout) {
            this.shout = shout;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ShoutAdapterItem &&
                    shout.getId().equals(((ShoutAdapterItem) item).shout.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return shout.equals(item);
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }
    }

    public class ShoutHeaderAdapterItem implements BaseAdapterItem {

        private final boolean isUserLoggedIn;

        public ShoutHeaderAdapterItem(boolean isUserLoggedIn) {
            this.isUserLoggedIn = isUserLoggedIn;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ShoutHeaderAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ShoutHeaderAdapterItem;
        }
    }

    private class DiscoverAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Discover discover;

        public DiscoverAdapterItem(@Nonnull Discover discover) {
            this.discover = discover;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverAdapterItem &&
                    discover.getId().equals(((DiscoverAdapterItem) item).getDiscover().getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return discover.equals(item);
        }

        @Nonnull
        public Discover getDiscover() {
            return discover;
        }
    }

    private class DiscoverHeaderAdapterItem implements BaseAdapterItem {

        public DiscoverHeaderAdapterItem() {
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverAdapterItem;
        }
    }
}
