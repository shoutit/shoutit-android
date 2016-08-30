package com.shoutit.app.android.view.home.picks;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.adapteritems.BaseShoutAdapterItem;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.ConversationsResponse;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.DiscoverItemDetailsResponse;
import com.shoutit.app.android.api.model.DiscoverResponse;
import com.shoutit.app.android.api.model.Label;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.Promotion;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoversDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.model.LocationPointer;
import com.shoutit.app.android.utils.MoreFunctions1;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func5;
import rx.subjects.PublishSubject;

public class PicksPresenter {

    private static final int MAX_VISIBLE_DISCOVER_ITEMS = 6;

    private final PublishSubject<String> discoverSelectedSubject = PublishSubject.create();
    private final PublishSubject<Object> viewAllPagesSubject = PublishSubject.create();
    private final PublishSubject<Object> viewAllDiscoversSubject = PublishSubject.create();
    private final PublishSubject<Object> viewAllChatsSubject = PublishSubject.create();
    private final PublishSubject<Object> viewAllShoutsSubject = PublishSubject.create();
    private final PublishSubject<Object> startSearchSubject = PublishSubject.create();
    private final PublishSubject<String> publicChatSelectedSubject = PublishSubject.create();
    private final PublishSubject<String> shoutSelectedSubject = PublishSubject.create();

    private final Observable<List<BaseAdapterItem>> allAdapterItemsObservable;
    private final Observable<Throwable> errorObservable;
    private final Observable<Boolean> progressObservable;

    @Inject
    public PicksPresenter(ApiService apiService,
                          @NetworkScheduler Scheduler networkScheduler,
                          @UiScheduler Scheduler uiScheduler,
                          DiscoversDao discoversDao,
                          UserPreferences userPreferences,
                          ShoutsDao shoutsDao,
                          @ForActivity Resources resources) {

        final Observable<LocationPointer> locationObservable = userPreferences.getLocationObservable()
                .map(userLocation -> new LocationPointer(userLocation.getCountry(), userLocation.getCity(), userLocation.getState()))
                .compose(ObservableExtensions.<LocationPointer>behaviorRefCount());


        /** Public chats **/
        final Observable<ResponseOrError<ConversationsResponse>> publicChatsRequest = apiService.publicChats(null, 2)
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ResponseOrError.toResponseOrErrorObservable())
                .compose(ObservableExtensions.<ResponseOrError<ConversationsResponse>>behaviorRefCount());

        final Observable<List<PicksAdapterItems.ChatAdapterItem>> publicChatItems = publicChatsRequest
                .compose(ResponseOrError.onlySuccess())
                .map(new Func1<ConversationsResponse, List<PicksAdapterItems.ChatAdapterItem>>() {
                    @Override
                    public List<PicksAdapterItems.ChatAdapterItem> call(ConversationsResponse conversationsResponse) {
                        final ImmutableList.Builder<PicksAdapterItems.ChatAdapterItem> builder = new ImmutableList.Builder<>();

                        for (Conversation conversation : conversationsResponse.getResults()) {
                            builder.add(new PicksAdapterItems.ChatAdapterItem(conversation, publicChatSelectedSubject));
                        }

                        return builder.build();
                    }
                });

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

        final Observable<List<PicksAdapterItems.DiscoverAdapterItem>> allDiscoverItems =
                childDiscoversObservable.map(new Func1<List<DiscoverChild>, List<PicksAdapterItems.DiscoverAdapterItem>>() {
                    @Override
                    public List<PicksAdapterItems.DiscoverAdapterItem> call(List<DiscoverChild> discovers) {
                        final List<PicksAdapterItems.DiscoverAdapterItem> items = new ArrayList<>();
                        for (int i = 0; i < discovers.size() && i < MAX_VISIBLE_DISCOVER_ITEMS; i++) {
                            items.add(new PicksAdapterItems.DiscoverAdapterItem(discovers.get(i), discoverSelectedSubject));
                        }

                        return new ImmutableList.Builder<PicksAdapterItems.DiscoverAdapterItem>()
                                .addAll(items)
                                .build();
                    }
                });
        /****/


        /**** Popular Pages TODO ****/
        final Observable<PicksAdapterItems.PopularPagesAdapterItem> popularPagesItems = Observable
                .just(Lists.newArrayList(getMockedPage(), getMockedPage()))
                .map(new Func1<List<Page>, PicksAdapterItems.PopularPagesAdapterItem>() {
                    @Override
                    public PicksAdapterItems.PopularPagesAdapterItem call(List<Page> pages) {
                        return new PicksAdapterItems.PopularPagesAdapterItem(pages, viewAllPagesSubject);
                    }
                });

        /**********/

        /**** Trending Shouts TODO ****/
        final Observable<ResponseOrError<ShoutsResponse>> trendingShoutsRequest = locationObservable
                .switchMap(locationPointer -> shoutsDao.getHomeShoutsObservable(locationPointer)
                        .observeOn(uiScheduler))
                .compose(ObservableExtensions.behaviorRefCount());

        final Observable<List<BaseShoutAdapterItem>> trendingShoutsItems = trendingShoutsRequest.compose(ResponseOrError.onlySuccess())
                .map(new Func1<ShoutsResponse, List<BaseShoutAdapterItem>>() {
                    @Override
                    public List<BaseShoutAdapterItem> call(ShoutsResponse shoutsResponse) {
                        final ImmutableList.Builder<BaseShoutAdapterItem> builder = new ImmutableList.Builder<>();

                        for (Shout shout : shoutsResponse.getShouts()) {
                            builder.add(new BaseShoutAdapterItem(shout, resources,
                                    shoutSelectedSubject, promotionInfoOrNull(shout)));
                        }

                        return builder.build();
                    }
                });

        /**********/


        allAdapterItemsObservable = Observable.combineLatest(
                allDiscoverItems,
                publicChatItems,
                popularPagesItems,
                trendingShoutsItems,
                locationObservable,
                (Func5<List<PicksAdapterItems.DiscoverAdapterItem>, List<PicksAdapterItems.ChatAdapterItem>,
                        PicksAdapterItems.PopularPagesAdapterItem, List<BaseShoutAdapterItem>, LocationPointer, List<BaseAdapterItem>>)
                        (discoverAdapterItems, chatAdapterItems, popularPagesAdapterItem, trendingShoutsAdapterItems, locationPointer) -> {
                            final ImmutableList.Builder<BaseAdapterItem> builder = new ImmutableList.Builder<>();

                            if (!discoverAdapterItems.isEmpty()) {
                                builder.add(new PicksAdapterItems.DiscoverHeaderAdapterItem(locationPointer.getCity(), viewAllDiscoversSubject));
                                builder.addAll(discoverAdapterItems);
                            }

                            if (!chatAdapterItems.isEmpty()) {
                                builder.add(new PicksAdapterItems.ViewAllChatsAdapterItem(viewAllChatsSubject));
                                builder.addAll(chatAdapterItems);
                            }

                            builder.add(popularPagesAdapterItem);

                            if (!trendingShoutsAdapterItems.isEmpty()) {
                                builder.add(new PicksAdapterItems.ViewAllShoutsAdapterItem(viewAllShoutsSubject));
                                builder.addAll(trendingShoutsAdapterItems);
                            }

                            builder.add(new PicksAdapterItems.StartSearchingAdapterItem(startSearchSubject));

                            return builder.build();
                        }
        );

        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(discoverRequestObservable),
                        ResponseOrError.transform(discoverItemDetailsObservable),
                        ResponseOrError.transform(publicChatsRequest),
                        ResponseOrError.transform(trendingShoutsRequest)
                )
        ).filter(Functions1.isNotNull());

        progressObservable = Observable.merge(
                allAdapterItemsObservable.map(Functions1.returnFalse()),
                errorObservable.map(Functions1.returnFalse())
        ).startWith(true);
    }

    private Page getMockedPage() {
        return new Page("id", null, null, null, "userrname", null, false, "http://placekitten.com/200/300",
                "", false, 2, null, null, false, null, null, null, null, null, null, null, null, null,
                null, false, false, null, null, false, null);
    }

    @Nullable
    public static BaseShoutAdapterItem.PromotionInfo promotionInfoOrNull(@NonNull Shout shout) {
        final boolean hasPromotion = hasPromotion(shout);
        if (hasPromotion) {
            return promotionInfo(shout.getPromotion());
        } else {
            return null;
        }
    }

    private static boolean hasPromotion(Shout shout) {
        return shout.getPromotion() != null && !shout.getPromotion().isExpired();
    }

    private static BaseShoutAdapterItem.PromotionInfo promotionInfo(Promotion promotion) {
        final Label label = promotion.getLabel();
        return new BaseShoutAdapterItem.PromotionInfo(Color.parseColor(label.getBgColor()), Color.parseColor(label.getColor()), label.getName());
    }

    public Observable<List<BaseAdapterItem>> getAllAdapterItemsObservable() {
        return allAdapterItemsObservable;
    }

    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    public Observable<String> getDiscoverSelectedObservable() {
        return discoverSelectedSubject;
    }

    public Observable<Object> getViewAllPagesObservable() {
        return viewAllPagesSubject;
    }

    public Observable<Object> getViewAllDiscoversObservable() {
        return viewAllDiscoversSubject;
    }

    public Observable<Object> getViewAllChatsObservable() {
        return viewAllChatsSubject;
    }

    public Observable<Object> getViewAllShoutsObservable() {
        return viewAllShoutsSubject;
    }

    public Observable<Object> getStartSearchObservable() {
        return startSearchSubject;
    }

    public Observable<String> getPublicChatSelectedObservable() {
        return publicChatSelectedSubject;
    }

    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedSubject;
    }
}
