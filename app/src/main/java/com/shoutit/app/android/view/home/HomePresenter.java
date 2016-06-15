package com.shoutit.app.android.view.home;

import android.content.Context;
import android.support.annotation.Nullable;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PromotionHelper;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

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
    private final PublishSubject<Object> showAllDiscoversSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> layoutManagerSwitchObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> onDiscoverSelectedSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> shoutSelectedObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> loadMoreShoutsSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> onShowInterestsClickedSubject = PublishSubject.create();

    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private final Observable<Boolean> linearLayoutManagerObservable;
    @Nonnull
    private final Observable<Object> refreshShoutsObservable;
    @Nonnull
    private final Observable<Object> loadMoreObservable;

    @Nonnull
    private final Scheduler uiScheduler;


    @Inject
    public HomePresenter(@Nonnull final ShoutsDao shoutsDao,
                         @Nonnull final DiscoversDao discoversDao,
                         @Nonnull final UserPreferences userPreferences,
                         @ForActivity final Context context,
                         @Nonnull @UiScheduler Scheduler uiScheduler,
                         @Nonnull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter) {
        this.uiScheduler = uiScheduler;

        final boolean isNormalUser = userPreferences.isNormalUser();
        final User currentUser = userPreferences.getUser();
        final String currentUserName = currentUser != null ? currentUser.getUsername() : null;

        final Observable<LocationPointer> locationObservable = userPreferences.getLocationObservable()
                .map(new Func1<UserLocation, LocationPointer>() {
                    @Override
                    public LocationPointer call(UserLocation userLocation) {
                        return new LocationPointer(userLocation.getCountry(), userLocation.getCity(), userLocation.getState());
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
                .map(new Func1<ResponseOrError<ShoutsResponse>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ResponseOrError<ShoutsResponse> shoutsResponse) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();
                        builder.add(new ShoutHeaderAdapterItem(isNormalUser,
                                userPreferences.getUserCity(), layoutManagerSwitchObserver));

                        if (shoutsResponse.isData()) {
                            final ShoutsResponse data = shoutsResponse.data();
                            if (!data.getShouts().isEmpty()) {
                                final Iterable<BaseAdapterItem> items = Iterables
                                        .transform(data.getShouts(), new Function<Shout, BaseAdapterItem>() {
                                            @javax.annotation.Nullable
                                            @Override
                                            public BaseAdapterItem apply(@Nullable Shout shout) {
                                                assert shout != null;
                                                final boolean isShoutOwner = shout.getProfile().getUsername().equals(currentUserName);
                                                return new ShoutAdapterItem(shout, isShoutOwner, isNormalUser, context, shoutSelectedObserver, PromotionHelper.promotionInfoOrNull(shout));
                                            }
                                        });

                                builder.addAll(items);
                            } else {
                                builder.add(new ShoutsEmptyAdapterItem(isNormalUser, onShowInterestsClickedSubject));
                            }

                            return builder.build();
                        } else {
                            return ImmutableList.of();
                        }
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

        final Observable<Optional<String>> mainDiscoverIdObservable = discoverRequestObservable
                .compose(ResponseOrError.<DiscoverResponse>onlySuccess())
                .filter(new Func1<DiscoverResponse, Boolean>() {
                    @Override
                    public Boolean call(DiscoverResponse discoverResponse) {
                        return discoverResponse != null && discoverResponse.getDiscovers() != null &&
                                !discoverResponse.getDiscovers().isEmpty();
                    }
                })
                .map(new Func1<DiscoverResponse, Optional<String>>() {
                    @Override
                    public Optional<String> call(DiscoverResponse response) {
                        if (response.getDiscovers() == null || response.getDiscovers().isEmpty()) {
                            return Optional.absent();
                        } else {
                            return Optional.of(response.getDiscovers().get(0).getId());
                        }
                    }
                });

        final Observable<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemDetailsObservable = mainDiscoverIdObservable
                .filter(MoreFunctions1.<String>isPresent())
                .switchMap(new Func1<Optional<String>, Observable<ResponseOrError<DiscoverItemDetailsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<DiscoverItemDetailsResponse>> call(Optional<String> discoverId) {
                        if (discoverId.isPresent()) {
                            return discoversDao.getDiscoverItemDao(discoverId.get()).getDiscoverItemObservable();
                        } else {
                            return Observable.just(ResponseOrError.<DiscoverItemDetailsResponse>fromError(new Throwable()));
                        }
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<DiscoverItemDetailsResponse>>behaviorRefCount());

        final Observable<List<DiscoverChild>> childDiscoversObservable =
                discoverItemDetailsObservable
                        .map(new Func1<ResponseOrError<DiscoverItemDetailsResponse>, List<DiscoverChild>>() {
                            @Override
                            public List<DiscoverChild> call(ResponseOrError<DiscoverItemDetailsResponse> discoverItemDetailsResponse) {
                                if (discoverItemDetailsResponse.isData()) {
                                    return discoverItemDetailsResponse.data().getChildren();
                                } else {
                                    return ImmutableList.of();
                                }
                            }
                        });

        final Observable<List<BaseAdapterItem>> allDiscoverAdapterItems =
                childDiscoversObservable.map(new Func1<List<DiscoverChild>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<DiscoverChild> discovers) {
                        final List<BaseAdapterItem> items = new ArrayList<>();
                        for (int i = 0; i < discovers.size() && i < MAX_VISIBLE_DISCOVER_ITEMS; i++) {
                            items.add(new DiscoverAdapterItem(discovers.get(i), onDiscoverSelectedSubject));
                        }

                        items.add(new DiscoverShowAllAdapterItem(showAllDiscoversSubject));

                        return new ImmutableList.Builder<BaseAdapterItem>()
                                .addAll(items)
                                .build();
                    }
                });


        /** Combines adapter items **/
        allAdapterItemsObservable = Observable.combineLatest(
                locationObservable,
                allDiscoverAdapterItems,
                allShoutAdapterItems,
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
                .filter(MoreFunctions1.<BaseAdapterItem>listNotEmpty())
                .observeOn(uiScheduler);

        /** Progress and Error **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(shoutsRequestObservable),
                ResponseOrError.transform(discoverRequestObservable),
                ResponseOrError.transform(discoverItemDetailsObservable)))
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        progressObservable = Observable.merge(
                errorObservable,
                allAdapterItemsObservable.filter(MoreFunctions1.<BaseAdapterItem>listNotEmpty()))
                .map(Functions1.returnFalse())
                .observeOn(uiScheduler);

        // Layout manager changes
        linearLayoutManagerObservable = layoutManagerSwitchObserver
                .scan(false, new Func2<Boolean, Object, Boolean>() {
                    @Override
                    public Boolean call(Boolean prev, Object o) {
                        return !prev;
                    }
                })
                .skip(1)
                .observeOn(uiScheduler);

        refreshShoutsObservable = shoutsGlobalRefreshPresenter
                .getShoutsGlobalRefreshObservable()
                .withLatestFrom(locationObservable, new Func2<Object, LocationPointer, Object>() {
                    @Override
                    public Object call(Object o, LocationPointer locationPointer) {
                        shoutsDao.getHomeShoutsRefreshObserver(locationPointer).onNext(null);
                        return null;
                    }
                });

        loadMoreObservable = loadMoreShoutsSubject
                .withLatestFrom(locationObservable, new Func2<Object, LocationPointer, Object>() {
                    @Override
                    public Object call(Object o, LocationPointer locationPointer) {
                        shoutsDao.getLoadMoreHomeShoutsObserver(locationPointer).onNext(null);
                        return null;
                    }
                });
    }

    @Nonnull
    public Observable<Object> getLoadMoreObservable() {
        return loadMoreObservable;
    }

    @Nonnull
    public Observable<Object> getRefreshShoutsObservable() {
        return refreshShoutsObservable;
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
        return RxMoreObservers.ignoreCompleted(loadMoreShoutsSubject);
    }

    @Nonnull
    public Observable<Boolean> getLinearLayoutManagerObservable() {
        return linearLayoutManagerObservable
                .filter(Functions1.isTrue())
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<Boolean> getGridLayoutManagerObservable() {
        return linearLayoutManagerObservable
                .filter(Functions1.isFalse())
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<Object> getShowAllDiscoversObservable() {
        return showAllDiscoversSubject.observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<String> getOnDiscoverSelectedObservable() {
        return onDiscoverSelectedSubject.observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedObserver;
    }

    @Nonnull
    public Observable<Object> openInterestsObservable() {
        return onShowInterestsClickedSubject;
    }

    /**
     * ADAPTER ITEMS
     **/
    public class DiscoverShowAllAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Observer<Object> showAllDiscoversObserver;

        public DiscoverShowAllAdapterItem(@Nonnull Observer<Object> showAllDiscoversObserver) {
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

        public void onShowAllClicked() {
            showAllDiscoversObserver.onNext(null);
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

        public DiscoverAdapterItem(@Nonnull DiscoverChild discover,
                                   @Nonnull Observer<String> onDiscoverSelectedSubject) {
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

        public void onDiscoverSelected() {
            onDiscoverSelectedSubject.onNext(discover.getId());
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

        private final boolean isLoggedInAsNormalUser;
        @Nonnull
        private final Observer<Object> onShowInterestsClickedObserver;

        public ShoutsEmptyAdapterItem(boolean isLoggedInAsNormalUser,
                                      @Nonnull Observer<Object> onShowInterestsClickedObserver) {
            this.isLoggedInAsNormalUser = isLoggedInAsNormalUser;
            this.onShowInterestsClickedObserver = onShowInterestsClickedObserver;
        }

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
            return item instanceof ShoutsEmptyAdapterItem &&
                    isLoggedInAsNormalUser == ((ShoutsEmptyAdapterItem) item).isLoggedInAsNormalUser;
        }

        public boolean isLoggedInAsNormalUser() {
            return isLoggedInAsNormalUser;
        }

        public void onShowInterestsClicked() {
            onShowInterestsClickedObserver.onNext(null);
        }
    }
    /** END OF ADAPTER ITEMS **/
}
