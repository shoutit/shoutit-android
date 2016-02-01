package com.shoutit.app.android.view.home;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
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
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class HomePresenter {

    private final static int PAGE_SIZE = 20;
    private final static int VISIBLE_DISCOVER_ITEMS_ON_START = 6;

    @Nonnull
    private final PublishSubject<Object> loadMoreDiscovers = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> loadMoreShouts = PublishSubject.create();
    @Nonnull
    private final BehaviorSubject<Boolean> showAllDiscovers = BehaviorSubject.create();
    @Nonnull
    private final PublishSubject<Object> layoutManagerSwitchObserver = PublishSubject.create();
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final ApiService apiService;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private final Observable<Boolean> linearLayoutManagerObservable;


    @Inject
    public HomePresenter(@Nonnull final ApiService apiService,
                         @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                         @Nonnull @UiScheduler final Scheduler uiScheduler) {
        this.apiService = apiService;
        this.networkScheduler = networkScheduler;
        this.uiScheduler = uiScheduler;

        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> myShoutsObservable =
                loadMoreShouts.startWith((Object) null)
                        .lift(getShoutsLoadMoreOperator(isUserLoggedIn()))
                        .compose(ResponseOrError.<ShoutsResponse>toResponseOrErrorObservable())
                        .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> allShoutAdapterItems = myShoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .filter(new Func1<ShoutsResponse, Boolean>() {
                    @Override
                    public Boolean call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse != null && shoutsResponse.getShouts() != null &&
                                !shoutsResponse.getShouts().isEmpty();
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
                                .add(new ShoutHeaderAdapterItem(isUserLoggedIn(), "Dubaj", layoutManagerSwitchObserver)) // TODO provide city here
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

        final Observable<List<Discover>> successDiscoverObservable = discoverObservable
                .compose(ResponseOrError.<DiscoverResponse>onlySuccess())
                .filter(new Func1<DiscoverResponse, Boolean>() {
                    @Override
                    public Boolean call(DiscoverResponse discoverResponse) {
                        return discoverResponse != null && discoverResponse.getDiscovers() != null &&
                                !discoverResponse.getDiscovers().isEmpty();
                    }
                })
                .map(new Func1<DiscoverResponse, List<Discover>>() {
                    @Override
                    public List<Discover> call(DiscoverResponse response) {
                        return response.getDiscovers();
                    }
                });

        final Observable<List<BaseAdapterItem>> allDiscoverAdapterItems = Observable.combineLatest(
                successDiscoverObservable,
                showAllDiscovers.startWith(false),
                new Func2<List<Discover>, Boolean, List<Discover>>() {
                    @Override
                    public List<Discover> call(List<Discover> discovers, Boolean showAllItems) {
                            if (showAllItems || discovers.size() < VISIBLE_DISCOVER_ITEMS_ON_START) {
                                return ImmutableList.copyOf(discovers);
                            } else {
                                return ImmutableList.copyOf(discovers.subList(0, VISIBLE_DISCOVER_ITEMS_ON_START));
                            }
                    }
                })
                .map(new Func1<List<Discover>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<Discover> discovers) {
                        final List<BaseAdapterItem> items = new ArrayList<>();
                        for (Discover discover : discovers) {
                            items.add(new DiscoverAdapterItem(discover));
                        }

                        if (items.size() == VISIBLE_DISCOVER_ITEMS_ON_START) {
                            items.add(new DiscoverShowAllAdapterItem(showAllDiscovers));
                        }

                        return new ImmutableList.Builder<BaseAdapterItem>()
                                .addAll(items)
                                .build();
                    }
                });

        /** Results **/
        allAdapterItemsObservable = Observable.combineLatest(
                allDiscoverAdapterItems.startWith(ImmutableList.<List<BaseAdapterItem>>of()),
                allShoutAdapterItems.startWith(ImmutableList.<List<BaseAdapterItem>>of()),
                new Func2<List<BaseAdapterItem>, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<BaseAdapterItem> discovers,
                                                      List<BaseAdapterItem> shouts) {
                        return ImmutableList.<BaseAdapterItem>builder()
                                .add(new DiscoverHeaderAdapterItem("Dubaj")) // TODO provide city
                                .add(new DiscoverContainerAdapterItem(discovers, loadMoreDiscovers))
                                .addAll(shouts)
                                .build();
                    }
                });

        /** Progress and Error **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(myShoutsObservable),
                ResponseOrError.transform(discoverObservable)))
                .filter(Functions1.isNotNull());

        progressObservable = Observable.merge(errorObservable, allDiscoverAdapterItems, allShoutAdapterItems)
                .map(Functions1.returnFalse());

        // Layout manager changes
        linearLayoutManagerObservable = layoutManagerSwitchObserver
                .scan(0, new Func2<Integer, Object, Integer>() {
                    @Override
                    public Integer call(Integer counter, Object o) {
                        return ++counter;
                    }
                })
                .skip(1)
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer count) {
                        return count % 2 == 1;
                    }
                });
    }

    @Nonnull
    private OperatorMergeNextToken<ShoutsResponse, Object> getShoutsLoadMoreOperator(final boolean isUserLoggedIn) {
        return OperatorMergeNextToken.create(new Func1<ShoutsResponse, Observable<ShoutsResponse>>() {
            private int pageNumber = 0;

            @Override
            public Observable<ShoutsResponse> call(ShoutsResponse previousResponse) {
                if (previousResponse == null || previousResponse.getNext() != null) {
                    ++pageNumber;
                    final Observable<ShoutsResponse> apiRequest;
                    if (isUserLoggedIn) {
                        apiRequest = apiService
                                .home(RequestsConstants.USER_ME, pageNumber, PAGE_SIZE)
                                .subscribeOn(networkScheduler);
                    } else {
                        apiRequest = apiService
                                .shoutsForCountry("GE", pageNumber, PAGE_SIZE)
                                .subscribeOn(networkScheduler);
                    }

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
    }

    @Nonnull
    private OperatorMergeNextToken<DiscoverResponse, Object> getDiscoverLoadMoreOperator() {
        return OperatorMergeNextToken.create(new Func1<DiscoverResponse, Observable<DiscoverResponse>>() {
            private int pageNumber = 0;

            @Override
            public Observable<DiscoverResponse> call(DiscoverResponse previousResponse) {
                if (previousResponse == null || previousResponse.getNext() != null) {
                    ++pageNumber;
                    final Observable<DiscoverResponse> apiRequest = apiService
                            .discovers("GE", pageNumber, PAGE_SIZE)
                            .subscribeOn(networkScheduler);

                    if (previousResponse == null) {
                        return apiRequest;
                    } else {
                        return Observable.just(previousResponse).zipWith(apiRequest, new MergeDiscoverResponses());
                    }
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
        return false;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public PublishSubject<Object> getLoadMoreShouts() {
        return loadMoreShouts;
    }

    @Nonnull
    public Observable<Boolean> getLinearLayoutManagerObservable() {
        return linearLayoutManagerObservable
                .observeOn(uiScheduler)
                .filter(Functions1.isTrue());
    }

    @Nonnull
    public Observable<Boolean> getGridLayoutManagerObservable() {
        return linearLayoutManagerObservable
                .observeOn(uiScheduler)
                .filter(Functions1.isFalse());
    }

    /** ADAPTER ITEMS **/
    public class DiscoverShowAllAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Observer<Boolean> showAllDiscoversObserver;

        public DiscoverShowAllAdapterItem(@Nonnull Observer<Boolean> showAllDiscoversObserver) {
            this.showAllDiscoversObserver = showAllDiscoversObserver;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverShowAllAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverShowAllAdapterItem;
        }

        @Nonnull
        public Observer<Boolean> getShowAllDiscoversObserver() {
            return showAllDiscoversObserver;
        }
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
        // TODO need provide this city
        private final String userCity;
        private final Observer<Object> layoutManagerSwitchObserver;

        public ShoutHeaderAdapterItem(boolean isUserLoggedIn, String userCity,
                                      Observer<Object> layoutManagerSwitchObserver) {
            this.isUserLoggedIn = isUserLoggedIn;
            this.userCity = userCity;
            this.layoutManagerSwitchObserver = layoutManagerSwitchObserver;
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

        public boolean isUserLoggedIn() {
            return isUserLoggedIn;
        }

        public String getUserCity() {
            return userCity;
        }

        public Observer<Object> getLayoutManagerSwitchObserver() {
            return layoutManagerSwitchObserver;
        }
    }

    public class DiscoverAdapterItem implements BaseAdapterItem {

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

    public class DiscoverHeaderAdapterItem implements BaseAdapterItem {

        private final String city;

        public DiscoverHeaderAdapterItem(String city) {
            this.city = city;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverHeaderAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverHeaderAdapterItem;
        }

        public String getCity() {
            return city;
        }
    }

    public class DiscoverContainerAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final List<BaseAdapterItem> adapterItems;
        @Nonnull
        private final Observer<Object> loadMoreDiscoversObserver;

        public DiscoverContainerAdapterItem(@Nonnull List<BaseAdapterItem> adapterItems,
                                            @Nonnull Observer<Object> loadMoreDiscoversObserver) {
            this.adapterItems = adapterItems;
            this.loadMoreDiscoversObserver = loadMoreDiscoversObserver;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverContainerAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverContainerAdapterItem;
        }

        @Nonnull
        public List<BaseAdapterItem> getAdapterItems() {
            return adapterItems;
        }

        @Nonnull
        public Observer<Object> getLoadMoreDiscoversObserver() {
            return loadMoreDiscoversObserver;
        }
    }
    /** END OF ADAPTER ITEMS **/
}
