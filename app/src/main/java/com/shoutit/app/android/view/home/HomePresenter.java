package com.shoutit.app.android.view.home;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.utils.rx.RxMoreObservers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
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
    @Nonnull
    private final Scheduler uiScheduler;
    private final Context context;


    @Inject
    public HomePresenter(@Nonnull final ShoutsDao shoutsDao,
                         @Nonnull final DiscoversDao discoversDao,
                         @Nonnull final UserPreferences userPreferences,
                         @Nonnull @UiScheduler final Scheduler uiScheduler,
                         @ForActivity Context context) {
        this.shoutsDao = shoutsDao;
        this.uiScheduler = uiScheduler;
        this.context = context;

        final boolean isUserLoggedIn = userPreferences.isUserLoggedIn();

        final Observable<LocationPointer> locationObservable = userPreferences.getLocationObservable()
                .map(new Func1<UserLocation, LocationPointer>() {
                    @Override
                    public LocationPointer call(UserLocation userLocation) {
                        return new LocationPointer(userLocation.getCountry(), userLocation.getCity());
                    }
                })
                .compose(ObservableExtensions.<LocationPointer>behaviorRefCount());

        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> shoutsRequestObservable = locationObservable
                .switchMap(new Func1<LocationPointer, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(LocationPointer locationPointer) {
                        return shoutsDao.getHomeShoutsObservable(locationPointer);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());


        final Observable<List<BaseAdapterItem>> allShoutAdapterItems = shoutsRequestObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .filter(Functions1.isNotNull())
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        final List<BaseAdapterItem> items = new ArrayList<>();

                        if (shoutsResponse.getShouts() != null && !shoutsResponse.getShouts().isEmpty()) {
                            for (Shout shout : shoutsResponse.getShouts()) {
                                items.add(new ShoutAdapterItem(shout));
                            }
                        } else {
                            items.add(new ShoutsEmptyAdapterItem());
                        }

                        return new ImmutableList.Builder<BaseAdapterItem>()
                                .add(new ShoutHeaderAdapterItem(isUserLoggedIn,
                                        userPreferences.getUserCity(), layoutManagerSwitchObserver))
                                .addAll(items)
                                .build();
                    }
                });

        /** Discovers **/
        final Observable<ResponseOrError<DiscoverResponse>> discoverRequestObservable = locationObservable
                .switchMap(new Func1<LocationPointer, Observable<ResponseOrError<DiscoverResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<DiscoverResponse>> call(LocationPointer locationPointer) {
                        return discoversDao.getDiscoverObservable(locationPointer);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<DiscoverResponse>>behaviorRefCount());

        final Observable<String> mainDiscoverIdObservable = discoverRequestObservable
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
                        return discoversDao.getDiscoverItemDao(discoverId).getDiscoverItemObservable();
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
                        for (int i = 0; i < discovers.size(); i++) {
                            if (i >= MAX_VISIBLE_DISCOVER_ITEMS) {
                                break;
                            }
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
                locationObservable,
                allDiscoverAdapterItems.startWith(new ArrayList<BaseAdapterItem>()),
                allShoutAdapterItems.startWith(new ArrayList<BaseAdapterItem>()),
                new Func3<LocationPointer, List<BaseAdapterItem>, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(LocationPointer locationPointer,
                                                      List<BaseAdapterItem> discovers,
                                                      List<BaseAdapterItem> shouts) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                        if (!discovers.isEmpty()) {
                            builder.add(new DiscoverHeaderAdapterItem(locationPointer.getCity()))
                                    .add(new DiscoverContainerAdapterItem(discovers, locationPointer));
                        }

                        return builder
                                .addAll(shouts)
                                .build();
                    }
                })
                .filter(MoreFunctions1.<BaseAdapterItem>listNotEmpty());

        /** Progress and Error **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsRequestObservable),
                ResponseOrError.transform(discoverRequestObservable),
                ResponseOrError.transform(discoverItemDetailsObservable)))
                .filter(Functions1.isNotNull());

        progressObservable = Observable.merge(
                errorObservable,
                allAdapterItemsObservable.filter(MoreFunctions1.<BaseAdapterItem>listNotEmpty()))
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
        return RxMoreObservers.ignoreCompleted(shoutsDao.getLoadMoreHomeShoutsObserver());
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
            return shout.equals(item);
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }

        @Nullable
        public String getCategoryIconUrl() {
            if (shout.getCategory() != null && shout.getCategory().getMainTag() != null) {
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
            return item instanceof ShoutHeaderAdapterItem && this.equals(item);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShoutHeaderAdapterItem)) return false;
            final ShoutHeaderAdapterItem that = (ShoutHeaderAdapterItem) o;
            return isUserLoggedIn == that.isUserLoggedIn &&
                    Objects.equal(userCity, that.userCity);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(isUserLoggedIn, userCity);
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
            if (!(o instanceof DiscoverAdapterItem)) return false;
            final DiscoverAdapterItem that = (DiscoverAdapterItem) o;
            return Objects.equal(discover, that.discover);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(discover);
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
            return item instanceof DiscoverHeaderAdapterItem && this.equals(item);
        }

        public String getCity() {
            return city;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscoverHeaderAdapterItem)) return false;
            final DiscoverHeaderAdapterItem that = (DiscoverHeaderAdapterItem) o;
            return Objects.equal(city, that.city);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(city);
        }
    }

    public class DiscoverContainerAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final List<BaseAdapterItem> adapterItems;
        @Nonnull
        private final LocationPointer locationPointer;

        public DiscoverContainerAdapterItem(@Nonnull List<BaseAdapterItem> adapterItems,
                                            @Nonnull LocationPointer locationPointer) {
            this.adapterItems = adapterItems;
            this.locationPointer = locationPointer;
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
            return item instanceof DiscoverContainerAdapterItem && this.equals(item);
        }

        @Nonnull
        public List<BaseAdapterItem> getAdapterItems() {
            return adapterItems;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscoverContainerAdapterItem)) return false;
            final DiscoverContainerAdapterItem that = (DiscoverContainerAdapterItem) o;
            return Objects.equal(locationPointer, that.locationPointer);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(locationPointer);
        }
    }

    public class ShoutsEmptyAdapterItem implements BaseAdapterItem {

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ShoutsEmptyAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ShoutsEmptyAdapterItem;
        }
    }
    /** END OF ADAPTER ITEMS **/
}
