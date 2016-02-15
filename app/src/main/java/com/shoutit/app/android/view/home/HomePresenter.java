package com.shoutit.app.android.view.home;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class HomePresenter {

    private final static int MAX_VISIBLE_DISCOVER_ITEMS = 6;

    @Nonnull
    private final PublishSubject<Boolean> showAllDiscovers = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> layoutManagerSwitchObserver = PublishSubject.create();

    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private final Observable<Boolean> linearLayoutManagerObservable;

    @Nonnull
    private final ShoutsDao shoutsDao;
    private final Context context;
    private final String userCity;

    @Inject
    public HomePresenter(@Nonnull ShoutsDao shoutsDao,
                         @Nonnull final DiscoversDao discoversDao,
                         @Nonnull final UserPreferences userPreferences,
                         @ForActivity Context context) {
        this.shoutsDao = shoutsDao;
        this.context = context;

        final boolean isUserLoggedIn = userPreferences.isUserLoggedIn();
        userCity = userPreferences.getUserCity();

        /** Shouts **/
        final Observable<List<BaseAdapterItem>> allShoutAdapterItems = shoutsDao.getHomeShoutsObservable()
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
                        assert shoutsResponse.getShouts() != null;
                        final ImmutableList<ShoutAdapterItem> items = ImmutableList.copyOf(Iterables.transform(shoutsResponse.getShouts(), new Function<Shout, ShoutAdapterItem>() {
                            @javax.annotation.Nullable
                            @Override
                            public ShoutAdapterItem apply(@Nullable Shout input) {
                                assert input != null;
                                return new ShoutAdapterItem(input);
                            }
                        }));

                        return new ImmutableList.Builder<BaseAdapterItem>()
                                .add(new ShoutHeaderAdapterItem(isUserLoggedIn, userCity, layoutManagerSwitchObserver))
                                .addAll(items)
                                .build();
                    }
                });

        /** Discovers **/
        final Observable<String> mainDiscoverIdObservable = discoversDao.getHomeDiscoverObservable()
                .compose(ResponseOrError.<DiscoverResponse>onlySuccess())
                .filter(new Func1<DiscoverResponse, Boolean>() {
                    @Override
                    public Boolean call(DiscoverResponse discoverResponse) {
                        return discoverResponse != null && discoverResponse.getDiscovers() != null &&
                                !discoverResponse.getDiscovers().isEmpty();
                    }
                })
                .map(new Func1<DiscoverResponse, String>() {
                    @Override
                    public String call(DiscoverResponse response) {
                        assert response.getDiscovers() != null;
                        return response.getDiscovers().get(0).getId();
                    }
                });

        final Observable<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemDetailsObservable = mainDiscoverIdObservable
                .switchMap(new Func1<String, Observable<ResponseOrError<DiscoverItemDetailsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<DiscoverItemDetailsResponse>> call(String discoverId) {
                        return discoversDao.discoverItemDao(discoverId)
                                .getDiscoverItemObservable();
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<DiscoverItemDetailsResponse>>behaviorRefCount());

        final Observable<List<DiscoverChild>> childDiscoversObservable =
                discoverItemDetailsObservable
                        .compose(ResponseOrError.<DiscoverItemDetailsResponse>onlySuccess())
                        .filter(Functions1.isNotNull())
                        .map(new Func1<DiscoverItemDetailsResponse, List<DiscoverChild>>() {
                            @Override
                            public List<DiscoverChild> call(DiscoverItemDetailsResponse discoverItemDetailsResponse) {
                                return discoverItemDetailsResponse.getChildren();
                            }
                        });

        final Observable<List<BaseAdapterItem>> allDiscoverAdapterItems =
                childDiscoversObservable.map(new Func1<List<DiscoverChild>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<DiscoverChild> discovers) {
                        final List<BaseAdapterItem> items = new ArrayList<>();
                        for (int i = 0; i < discovers.size() && i < MAX_VISIBLE_DISCOVER_ITEMS; i++) {
                            items.add(new DiscoverAdapterItem(discovers.get(i)));
                        }

                        items.add(new DiscoverShowAllAdapterItem(showAllDiscovers));

                        return new ImmutableList.Builder<BaseAdapterItem>()
                                .addAll(items)
                                .build();
                    }
                });

        /** Combines adapter items **/
        allAdapterItemsObservable = Observable.combineLatest(
                allDiscoverAdapterItems.startWith(ImmutableList.<BaseAdapterItem>of()),
                allShoutAdapterItems.startWith(ImmutableList.<BaseAdapterItem>of()),
                new Func2<List<BaseAdapterItem>, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<BaseAdapterItem> discovers,
                                                      List<BaseAdapterItem> shouts) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                        if (!discovers.isEmpty()) {
                            builder.add(new DiscoverHeaderAdapterItem(userCity))
                                    .add(new DiscoverContainerAdapterItem(discovers));
                        }

                        return builder
                                .addAll(shouts)
                                .build();
                    }
                })
                .filter(MoreFunctions1.<BaseAdapterItem>listNotEmpty());

        /** Progress and Error **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsDao.getHomeShoutsObservable()),
                ResponseOrError.transform(discoversDao.getHomeDiscoverObservable()),
                ResponseOrError.transform(discoverItemDetailsObservable)))
                .filter(Functions1.isNotNull());

        progressObservable = Observable.merge(
                errorObservable,
                allAdapterItemsObservable.filter(MoreFunctions1.<BaseAdapterItem>listNotEmpty()))
                .map(Functions1.returnFalse());

        // Layout manager changes
        linearLayoutManagerObservable = layoutManagerSwitchObserver
                .scan(true, new Func2<Boolean, Object, Boolean>() {
                    @Override
                    public Boolean call(Boolean prev, Object o) {
                        return !prev;
                    }
                })
                .skip(1)
                .startWith(true);
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
    public Observer<Object> getLoadMoreShouts() {
        return RxMoreObservers.ignoreCompleted(shoutsDao.getLoadMoreShoutsObserver());
    }

    @Nonnull
    public Observable<Boolean> getLinearLayoutManagerObservable() {
        return linearLayoutManagerObservable
                .filter(Functions1.isTrue());
    }

    @Nonnull
    public Observable<Boolean> getGridLayoutManagerObservable() {
        return linearLayoutManagerObservable
                .filter(Functions1.isFalse());
    }

    @Nonnull
    public Observable<Boolean> getShowAllDiscoversObservable() {
        return showAllDiscovers;
    }

    /**
     * ADAPTER ITEMS
     **/
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
            return this.equals(item);
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }

        @Nullable
        public String getCategoryIconUrl() {
            if (shout.getCategory() != null) {
                return Strings.emptyToNull(shout.getCategory().getMainTag().getImage());
            } else {
                return null;
            }
        }

        @IdRes
        @Nullable
        public Integer getCountryResId() {
            if (shout.getLocation() != null && !TextUtils.isEmpty(shout.getLocation().getCountry())) {
                final String countryCode = shout.getLocation().getCountry().toLowerCase();
                return ResourcesHelper.getResourceIdForName(countryCode, context);
            } else {
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ShoutAdapterItem that = (ShoutAdapterItem) o;

            return shout.equals(that.shout);

        }

        @Override
        public int hashCode() {
            return shout.hashCode();
        }
    }

    public class ShoutHeaderAdapterItem implements BaseAdapterItem {

        private final boolean isUserLoggedIn;
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
        private final DiscoverChild discover;

        public DiscoverAdapterItem(@Nonnull DiscoverChild discover) {
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
            return this.equals(item);
        }

        @Nonnull
        public DiscoverChild getDiscover() {
            return discover;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DiscoverAdapterItem that = (DiscoverAdapterItem) o;

            return discover.equals(that.discover);

        }

        @Override
        public int hashCode() {
            return discover.hashCode();
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

        public DiscoverContainerAdapterItem(@Nonnull List<BaseAdapterItem> adapterItems) {
            this.adapterItems = adapterItems;
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
    }
    /** END OF ADAPTER ITEMS **/
}
