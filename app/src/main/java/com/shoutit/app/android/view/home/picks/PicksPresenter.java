package com.shoutit.app.android.view.home.picks;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ConversationsResponse;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.utils.MoreFunctions1;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class PicksPresenter {

    private static final int MAX_VISIBLE_DISCOVER_ITEMS = 6;

    private final PublishSubject<String> onDiscoverSelectedSubject = PublishSubject.create();

    @Inject
    public PicksPresenter(ApiService apiService,
                          @NetworkScheduler Scheduler networkScheduler,
                          @UiScheduler Scheduler uiScheduler,
                          DiscoversDao discoversDao,
                          UserPreferences userPreferences) {

        final Observable<LocationPointer> locationObservable = userPreferences.getLocationObservable()
                .map(userLocation -> new LocationPointer(userLocation.getCountry(), userLocation.getCity(), userLocation.getState()))
                .compose(ObservableExtensions.<LocationPointer>behaviorRefCount());

        final Observable<ConversationsResponse> publicChatsRequest = apiService.publicChats(null, 2)
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.behaviorRefCount());

        /** Discovers **/
        final Observable<ResponseOrError<DiscoverResponse>> discoverRequestObservable = locationObservable
                .switchMap(discoversDao::getDiscoverObservable)
                .compose(ObservableExtensions.<ResponseOrError<DiscoverResponse>>behaviorRefCount());

        final Observable<Optional<String>> mainDiscoverIdObservable = discoverRequestObservable
                .compose(ResponseOrError.<DiscoverResponse>onlySuccess())
                .filter(discoverResponse -> discoverResponse != null && discoverResponse.getDiscovers() != null &&
                        !discoverResponse.getDiscovers().isEmpty())
                .map((Func1<DiscoverResponse, Optional<String>>) response -> {
                    if (response.getDiscovers() == null || response.getDiscovers().isEmpty()) {
                        return Optional.absent();
                    } else {
                        return Optional.of(response.getDiscovers().get(0).getId());
                    }
                });

        final Observable<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemDetailsObservable = mainDiscoverIdObservable
                .filter(MoreFunctions1.<String>isPresent())
                .switchMap(discoverId -> {
                    if (discoverId.isPresent()) {
                        return discoversDao.getDiscoverItemDao(discoverId.get()).getDiscoverItemObservable();
                    } else {
                        return Observable.just(ResponseOrError.<DiscoverItemDetailsResponse>fromError(new Throwable()));
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<DiscoverItemDetailsResponse>>behaviorRefCount());

        final Observable<List<DiscoverChild>> childDiscoversObservable =
                discoverItemDetailsObservable
                        .map((Func1<ResponseOrError<DiscoverItemDetailsResponse>, List<DiscoverChild>>) discoverItemDetailsResponse -> {
                            if (discoverItemDetailsResponse.isData()) {
                                return discoverItemDetailsResponse.data().getChildren();
                            } else {
                                return ImmutableList.of();
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

                        return new ImmutableList.Builder<BaseAdapterItem>()
                                .addAll(items)
                                .build();
                    }
                });
        /****/


    }

    public class DiscoverAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final DiscoverChild discover;

        public DiscoverAdapterItem(@Nonnull DiscoverChild discover) {
            this.discover = discover;
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
        private final Observer<Object> viewAllDiscovers;

        public DiscoverHeaderAdapterItem(String city,
                                         Observer<Object> viewAllDiscovers) {
            this.city = city;
            this.viewAllDiscovers = viewAllDiscovers;
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

        public void viewAllDiscovers() {
            viewAllDiscovers.onNext(null);
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

    public class DiscoverContainerAdapterItem extends BaseNoIDAdapterItem {

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
            return Objects.equal(adapterItems, that.adapterItems) &&
                    Objects.equal(locationPointer, that.locationPointer);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(locationPointer);
        }
    }
}
