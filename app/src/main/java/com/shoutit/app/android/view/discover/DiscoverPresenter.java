package com.shoutit.app.android.view.discover;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.adapteritems.BaseShoutAdapterItem;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.subsearch.SubSearchActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
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
    private final Observable<DiscoveryInfo> mDiscoveryInfoObservable;
    @Nonnull
    private final Observable<Intent> searchMenuItemClickObservable;
    @Nonnull
    private final Observable<Object> shoutsRefreshObservable;

    @Nonnull
    private final PublishSubject<String> showMoreObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> discoverSelectedObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> shoutSelectedObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> searchMenuItemClickSubject = PublishSubject.create();
    @NonNull
    private final Resources mResources;

    public DiscoverPresenter(@Nonnull UserPreferences userPreferences,
                             @Nonnull final DiscoversDao discoversDao,
                             @Nonnull final DiscoverShoutsDao discoverShoutsDao,
                             @Nonnull final Optional<String> discoverParentId,
                             @Nonnull @UiScheduler final Scheduler uiScheduler,
                             @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                             @NonNull @ForActivity Resources resources,
                             @Nonnull @ForActivity final Context context,
                             @Nonnull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter) {
        mResources = resources;


        final Observable<LocationPointer> locationObservable = userPreferences.getLocationObservable()
                .map(new Func1<UserLocation, LocationPointer>() {
                    @Override
                    public LocationPointer call(UserLocation userLocation) {
                        return new LocationPointer(userLocation.getCountry(), userLocation.getCity(), userLocation.getState());
                    }
                })
                .compose(ObservableExtensions.<LocationPointer>behaviorRefCount());


        /** Main Discover Item **/
        final Observable<Optional<String>> optionalDiscoverParentIdObservable = Observable.just(discoverParentId)
                .compose(ObservableExtensions.<Optional<String>>behaviorRefCount());

        final Observable<ResponseOrError<DiscoverResponse>> mainDiscoverObservable =
                optionalDiscoverParentIdObservable
                        .filter(MoreFunctions1.<String>isAbsent())
                        .flatMap(optional -> locationObservable)
                        .switchMap(discoversDao::getDiscoverObservable);

        final Observable<String> mainDiscoverIdObservable = mainDiscoverObservable
                .compose(ResponseOrError.<DiscoverResponse>onlySuccess())
                .map(response -> {
                    assert response.getDiscovers() != null;

                    return response.getDiscovers().get(0).getId();
                });

        final Observable<String> discoverParentIdObservable = optionalDiscoverParentIdObservable
                .filter(MoreFunctions1.<String>isPresent())
                .map(Optional::get);

        final Observable<String> idObservable = Observable.merge(
                mainDiscoverIdObservable, discoverParentIdObservable)
                .compose(ObservableExtensions.<String>behaviorRefCount());

        /** DiscoverItemDetails **/
        final Observable<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemObservable =
                idObservable
                        .filter(Functions1.isNotNull())
                        .switchMap(itemId -> discoversDao.getDiscoverItemDao(itemId)
                                .getDiscoverItemObservable())
                        .compose(ObservableExtensions.<ResponseOrError<DiscoverItemDetailsResponse>>behaviorRefCount());

        final Observable<DiscoverItemDetailsResponse> itemDetailsResponseObservable = discoverItemObservable
                .compose(ResponseOrError.<DiscoverItemDetailsResponse>onlySuccess());

        final Observable<String> discoveryTitle = itemDetailsResponseObservable.map(DiscoverItemDetailsResponse::getTitle);

        /** Shouts **/
        final Observable<ResponseOrError<ShoutsResponse>> shoutsItemsObservable = itemDetailsResponseObservable
                .filter(DiscoverItemDetailsResponse::isShowShouts)
                .switchMap(response -> discoverShoutsDao.getShoutsObservable(response.getId())
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler))
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount());

        /** Adapter Items **/
        final Observable<BaseAdapterItem> headerAdapterItemObservable = itemDetailsResponseObservable
                .map((Func1<DiscoverItemDetailsResponse, BaseAdapterItem>) response -> new HeaderAdapterItem(response.getTitle(), response.getCover()));

        final Observable<List<BaseAdapterItem>> discoverAdapterItemsObservable = itemDetailsResponseObservable
                .filter(response -> response.isShowChildren() && response.getChildren() != null)
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
                .filter(shoutsResponse -> !shoutsResponse.getShouts().isEmpty())
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        final List<BaseAdapterItem> items = new ArrayList<>();

                        final List<Shout> shouts = shoutsResponse.getShouts();
                        for (int i = 0; i < Math.min(shouts.size(), MAX_DISPLAYED_SHOUTS); i++) {
                            items.add(new BaseShoutAdapterItem(shouts.get(i), context.getResources(), shoutSelectedObserver, null));
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

        mDiscoveryInfoObservable = showMoreObserver.withLatestFrom(discoveryTitle, DiscoveryInfo::new);

        /** Search Click **/
        searchMenuItemClickObservable = searchMenuItemClickSubject
                .withLatestFrom(itemDetailsResponseObservable, (o, response) -> SubSearchActivity.newIntent(
                        context, SearchPresenter.SearchType.DISCOVER,
                        response.getId(), response.getTitle()));

        shoutsRefreshObservable = shoutsGlobalRefreshPresenter
                .getShoutsGlobalRefreshObservable()
                .withLatestFrom(itemDetailsResponseObservable, (o, response) -> {
                    discoverShoutsDao.getRefreshObserver(response.getId()).onNext(null);
                    return null;
                });

    }

    @Nonnull
    public Observable<Object> getShoutsRefreshObservable() {
        return shoutsRefreshObservable;
    }

    @Nonnull
    public Observable<Intent> getSearchMenuItemClickObservable() {
        return searchMenuItemClickObservable;
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
    public Observable<DiscoveryInfo> getShowMoreObservable() {
        return mDiscoveryInfoObservable;
    }

    @Nonnull
    public Observable<String> getDiscoverSelectedObservable() {
        return discoverSelectedObserver;
    }

    @Nonnull
    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedObserver;
    }

    public void onSearchMenuItemClicked() {
        searchMenuItemClickSubject.onNext(null);
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
            return item instanceof HeaderAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof HeaderAdapterItem && this.equals(item);
        }

        public String getTitle() {
            return title;
        }

        public String getImage() {
            return image;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HeaderAdapterItem)) return false;
            final HeaderAdapterItem that = (HeaderAdapterItem) o;
            return Objects.equal(title, that.title) &&
                    Objects.equal(image, that.image);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(title, image);
        }
    }

    public class DiscoverAdapterItem extends BaseNoIDAdapterItem {

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
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverAdapterItem &&
                    discoverChild.getId().equals(((DiscoverAdapterItem) item).getDiscoverChild().getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverAdapterItem && this.equals(item);
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

    public class DiscoveryInfo {
        private final String mId;
        private final String mTitle;

        public DiscoveryInfo(String id, String title) {
            mId = id;
            mTitle = title;
        }

        public String getId() {
            return mId;
        }

        public String getTitle() {
            return mTitle;
        }
    }
}
