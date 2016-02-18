package com.shoutit.app.android.view.discover;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.view.home.HomePresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.functions.Func4;
import rx.subjects.PublishSubject;

public class DiscoverPresenter {

    private static final int MAX_DISPLAYED_SHOUTS = 4;

    @Nonnull
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private final Observable<Throwable> errorsObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;

    @Nonnull
    private final PublishSubject<String> showMoreObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> discoverSelectedObserver = PublishSubject.create();

    public DiscoverPresenter(@Nonnull UserPreferences userPreferences,
                             @Nonnull final DiscoversDao discoversDao,
                             @Nonnull final DiscoverShoutsDao discoverShoutsDao,
                             @Nonnull Optional<String> discoverParentId,
                             @Nonnull @UiScheduler final Scheduler uiScheduler,
                             @Nonnull @NetworkScheduler final Scheduler networkScheduler) {


        final Observable<LocationPointer> locationObservable = userPreferences.getLocationObservable()
                .map(new Func1<UserLocation, LocationPointer>() {
                    @Override
                    public LocationPointer call(UserLocation userLocation) {
                        return new LocationPointer(userLocation.getCountry(), userLocation.getCity());
                    }
                })
                .compose(ObservableExtensions.<LocationPointer>behaviorRefCount());


        /** Main Discover Item **/
        final Observable<Optional<String>> discoverParentIdObservable = Observable.just(discoverParentId)
                .compose(ObservableExtensions.<Optional<String>>behaviorRefCount());

        final Observable<ResponseOrError<DiscoverResponse>> mainDiscoverObservable =
                discoverParentIdObservable
                        .filter(MoreFunctions1.<String>isAbsent())
                        .flatMap(new Func1<Optional<String>, Observable<LocationPointer>>() {
                            @Override
                            public Observable<LocationPointer> call(Optional<String> optional) {
                                return locationObservable;
                            }
                        })
                        .switchMap(new Func1<LocationPointer, Observable<ResponseOrError<DiscoverResponse>>>() {
                            @Override
                            public Observable<ResponseOrError<DiscoverResponse>> call(LocationPointer locationPointer) {
                                return discoversDao.getDiscoverObservable(locationPointer);
                            }
                        });

        final Observable<String> mainDiscoverIdObservable = mainDiscoverObservable
                .compose(ResponseOrError.<DiscoverResponse>onlySuccess())
                .map(new Func1<DiscoverResponse, String>() {
                    @Override
                    public String call(DiscoverResponse response) {
                        assert response.getDiscovers() != null;

                        return response.getDiscovers().get(0).getId();
                    }
                });

        final Observable<String> parentDiscoverIdObservable = discoverParentIdObservable
                .filter(MoreFunctions1.<String>isPresent())
                .map(new Func1<Optional<String>, String>() {
                    @Override
                    public String call(Optional<String> optional) {
                        return optional.get();
                    }
                });

        final Observable<String> idObservable = Observable.merge(
                mainDiscoverIdObservable, parentDiscoverIdObservable)
                .compose(ObservableExtensions.<String>behaviorRefCount());

        /** DiscoverItemDetails **/
        final Observable<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemObservable =
                idObservable
                        .filter(Functions1.isNotNull())
                        .switchMap(new Func1<String, Observable<ResponseOrError<DiscoverItemDetailsResponse>>>() {
                            @Override
                            public Observable<ResponseOrError<DiscoverItemDetailsResponse>> call(String itemId) {
                                return discoversDao.getDiscoverItemDao(itemId)
                                        .getDiscoverItemObservable();
                            }
                        })
                        .compose(ObservableExtensions.<ResponseOrError<DiscoverItemDetailsResponse>>behaviorRefCount());

        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> shoutsItemsObservable = discoverItemObservable
                .compose(ResponseOrError.<DiscoverItemDetailsResponse>onlySuccess())
                .filter(new Func1<DiscoverItemDetailsResponse, Boolean>() {
                    @Override
                    public Boolean call(DiscoverItemDetailsResponse response) {
                        return response.isShowShouts();
                    }
                })
                .switchMap(new Func1<DiscoverItemDetailsResponse, Observable<ResponseOrError<ShoutsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<ShoutsResponse>> call(DiscoverItemDetailsResponse response) {
                        return discoverShoutsDao.getShoutsObservable(response.getId())
                                .subscribeOn(networkScheduler)
                                .observeOn(uiScheduler);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        /** Adapter Items **/
        final Observable<BaseAdapterItem> headerAdapterItemObservable = discoverItemObservable
                .compose(ResponseOrError.<DiscoverItemDetailsResponse>onlySuccess())
                .map(new Func1<DiscoverItemDetailsResponse, BaseAdapterItem>() {
                    @Override
                    public BaseAdapterItem call(DiscoverItemDetailsResponse response) {
                        return new HeaderAdapterItem(response.getTitle(), response.getImage());
                    }
                });

        final Observable<List<BaseAdapterItem>> discoverAdapterItemsObservable = discoverItemObservable
                .compose(ResponseOrError.<DiscoverItemDetailsResponse>onlySuccess())
                .filter(new Func1<DiscoverItemDetailsResponse, Boolean>() {
                    @Override
                    public Boolean call(DiscoverItemDetailsResponse response) {
                        return response.isShowChildren() && response.getChildren() != null;
                    }
                })
                .map(new Func1<DiscoverItemDetailsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(DiscoverItemDetailsResponse response) {
                        assert response.getChildren() != null;

                        final List<BaseAdapterItem> items = Lists
                                .transform(response.getChildren(), new Function<DiscoverChild, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public BaseAdapterItem apply(@Nullable DiscoverChild input) {
                                        assert input != null;
                                        return new DiscoverAdapterItem(input, discoverSelectedObserver);
                                    }
                                });


                        return ImmutableList.copyOf(items);
                    }
                });

        final Observable<List<BaseAdapterItem>> shoutAdapterItemObservable = shoutsItemsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .filter(new Func1<ShoutsResponse, Boolean>() {
                    @Override
                    public Boolean call(ShoutsResponse shoutsResponse) {
                        return !shoutsResponse.getShouts().isEmpty();
                    }
                })
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        final List<BaseAdapterItem> items = new ArrayList<>();

                        final List<Shout> shouts = shoutsResponse.getShouts();
                        for (int i = 0; i < Math.min(shouts.size(), MAX_DISPLAYED_SHOUTS); i++) {
                            items.add(new ShoutAdapterItem(shouts.get(i)));
                        }

                        return ImmutableList.copyOf(items);
                    }
                });

        allAdapterItemsObservable = Observable.combineLatest(
                idObservable,
                headerAdapterItemObservable.startWith((BaseAdapterItem) null),
                discoverAdapterItemsObservable.startWith(ImmutableList.<BaseAdapterItem>of()),
                shoutAdapterItemObservable.startWith(ImmutableList.<BaseAdapterItem>of()),
                new Func4<String, BaseAdapterItem, List<BaseAdapterItem>, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(String discoverId,
                                                      BaseAdapterItem headerItem,
                                                      List<BaseAdapterItem> discovers,
                                                      List<BaseAdapterItem> shouts) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        if (headerItem != null) {
                            builder.add(headerItem);
                        }

                        if (!discovers.isEmpty()) {
                            builder.addAll(discovers);
                        }

                        if (!shouts.isEmpty()) {
                            builder.add(new ShoutHeaderAdapterItem());
                            builder.addAll(shouts);
                            builder.add(new ShowMoreButtonAdapterItem(discoverId, showMoreObserver));
                        }

                        return builder.build();
                    }
                })
                .observeOn(uiScheduler);


        /** Errors and Progress **/
        errorsObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(mainDiscoverObservable),
                        ResponseOrError.transform(discoverItemObservable),
                        ResponseOrError.transform(shoutsItemsObservable)
                ))
                .filter(Functions1.isNotNull())
                .throttleFirst(5, TimeUnit.SECONDS)
                .observeOn(uiScheduler);

        progressObservable = Observable.merge(
                discoverItemObservable.map(Functions1.returnFalse()),
                shoutsItemsObservable.map(Functions1.returnFalse()))
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorsObservable() {
        return errorsObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<String> getShowMoreObservable() {
        return showMoreObserver;
    }

    @Nonnull
    public Observable<String> getDiscoverSelectedObservable() {
        return discoverSelectedObserver;
    }

    /**
     * Adapter Items
     **/

    public class HeaderAdapterItem implements BaseAdapterItem {

        private final String title;
        private final String image;

        public HeaderAdapterItem(String title, String image) {

            this.title = title;
            this.image = image;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }

        public String getTitle() {
            return title;
        }

        public String getImage() {
            return image;
        }
    }

    public class DiscoverAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final DiscoverChild discoverChild;
        @Nonnull
        private final Observer<String> discoverSelectedObserver;

        public DiscoverAdapterItem(@Nonnull DiscoverChild discoverChild,
                                   @Nonnull Observer<String> discoverSelectedObserver) {
            this.discoverChild = discoverChild;
            this.discoverSelectedObserver = discoverSelectedObserver;
        }

        @Nonnull
        public DiscoverChild getDiscoverChild() {
            return discoverChild;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof HomePresenter.DiscoverAdapterItem &&
                    discoverChild.getId().equals(((HomePresenter.DiscoverAdapterItem) item).getDiscover().getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof HomePresenter.DiscoverAdapterItem && this.equals(item);
        }

        public void onDiscoverSelected() {
            discoverSelectedObserver.onNext(discoverChild.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscoverAdapterItem)) return false;
            final DiscoverAdapterItem that = (DiscoverAdapterItem) o;
            return Objects.equal(discoverChild, that.discoverChild);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(discoverChild);
        }
    }

    public class ShoutHeaderAdapterItem implements BaseAdapterItem {

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
                    shout.getId().equals(((ShoutAdapterItem) item).getShout().getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ShoutAdapterItem &&
                    this.equals(item);
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShoutAdapterItem)) return false;
            final ShoutAdapterItem that = (ShoutAdapterItem) o;
            return Objects.equal(shout, that.shout);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(shout);
        }
    }

    public class ShowMoreButtonAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final String discoverId;
        @Nonnull
        private final Observer<String> showMoreObserver;

        public ShowMoreButtonAdapterItem(@Nonnull String discoverId,
                                         @Nonnull Observer<String> showMoreObserver) {
            this.discoverId = discoverId;
            this.showMoreObserver = showMoreObserver;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ShowMoreButtonAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ShowMoreButtonAdapterItem;
        }

        public void showMoreClicked() {
            showMoreObserver.onNext(discoverId);
        }
    }
}
