package com.shoutit.app.android.view.discover;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
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
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.utils.MoreFunctions1;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.subjects.PublishSubject;

public class DiscoverPresenter {

    @Nonnull
    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    @Nonnull
    private final Observable<Throwable> errorsObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;

    @Nonnull
    private final PublishSubject<Object> showMoreObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> discoverSelectedObserver = PublishSubject.create();

    @Inject
    public DiscoverPresenter(@Nonnull UserPreferences userPreferences,
                             @Nonnull final DiscoversDao dao,
                             @Nonnull Optional<String> discoverParentId) {


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
                                return dao.getDiscoverObservable(locationPointer);
                            }
                        })
                        .compose(ObservableExtensions.<ResponseOrError<DiscoverResponse>>behaviorRefCount());

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


        /** DiscoverItemDetails **/
        final Observable<ResponseOrError<DiscoverItemDetailsResponse>> discoverItemObservable =
                Observable.merge(mainDiscoverIdObservable, parentDiscoverIdObservable)
                        .filter(Functions1.isNotNull())
                        .switchMap(new Func1<String, Observable<ResponseOrError<DiscoverItemDetailsResponse>>>() {
                            @Override
                            public Observable<ResponseOrError<DiscoverItemDetailsResponse>> call(String itemId) {
                                return dao.getDiscoverItemDao(itemId).getDiscoverItemObservable();
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
                        return dao.getDiscoverItemDao(response.getId()).getDiscoverItemShoutsObservable();
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
                                        return new DiscoverAdapterItem(input);
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
                        return shoutsResponse.getShouts() != null && !shoutsResponse.getShouts().isEmpty();
                    }
                })
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {

                        final List<BaseAdapterItem> items = Lists
                                .transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                                    @Nullable
                                    @Override
                                    public BaseAdapterItem apply(@Nullable Shout input) {
                                        return new ShoutAdapterItem(input);
                                    }
                                });

                        return ImmutableList.copyOf(items);
                    }
                });

        allAdapterItemsObservable = Observable.combineLatest(
                headerAdapterItemObservable,
                discoverAdapterItemsObservable.startWith(ImmutableList.<BaseAdapterItem>of()),
                shoutAdapterItemObservable.startWith(ImmutableList.<BaseAdapterItem>of()),
                new Func3<BaseAdapterItem, List<BaseAdapterItem>, List<BaseAdapterItem>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(BaseAdapterItem headerItem,
                                                      List<BaseAdapterItem> discovers,
                                                      List<BaseAdapterItem> shouts) {
                        final ImmutableList.Builder<BaseAdapterItem> builder = ImmutableList.builder();

                        builder.add(headerItem);

                        if (!discovers.isEmpty()) {
                            builder.addAll(discovers);
                        }

                        if (!shouts.isEmpty()) {
                            builder.add(new ShoutHeaderAdapterItem());
                            builder.addAll(shouts);
                            builder.add(new ShowMoreButtonAdapterItem(showMoreObserver));
                        }

                        return builder.build();
                    }
                }
        );


        /** Errors and Progress **/
        errorsObservable = ResponseOrError.combineErrorsObservable(
            ImmutableList.of(
                    ResponseOrError.transform(mainDiscoverObservable),
                    ResponseOrError.transform(discoverItemObservable),
                    ResponseOrError.transform(shoutsItemsObservable)
            )
        );

        progressObservable = Observable.merge(
                mainDiscoverObservable.map(Functions1.returnFalse()),
                discoverItemObservable.map(Functions1.returnFalse()),
                shoutsItemsObservable.map(Functions1.returnFalse())
        );
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

        public DiscoverAdapterItem(@Nonnull DiscoverChild discoverChild) {
            this.discoverChild = discoverChild;
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
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }
    }

    public class ShoutHeaderAdapterItem implements BaseAdapterItem {

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
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }
    }

    public class ShowMoreButtonAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Observer<Object> showMoreObserver;

        public ShowMoreButtonAdapterItem(@Nonnull Observer<Object> showMoreObserver) {
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
            showMoreObserver.onNext(null);
        }
    }
}
