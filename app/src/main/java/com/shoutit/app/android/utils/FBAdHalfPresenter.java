package com.shoutit.app.android.utils;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.facebook.ads.NativeAd;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.FbAdAdapterItem;
import com.shoutit.app.android.view.loginintro.FacebookHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class FBAdHalfPresenter {

    private static final int AD_POSITION_CYCLE = 25;

    private final FacebookHelper facebookHelper;
    private final Scheduler uiScheduler;

    @Inject
    public FBAdHalfPresenter(FacebookHelper facebookHelper,
                             @UiScheduler Scheduler uiScheduler) {
        this.facebookHelper = facebookHelper;
        this.uiScheduler = uiScheduler;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getShoutsWithAdsObservable(Observable<List<BaseAdapterItem>> shoutsItemsObservable,
                                                                        Observable<Boolean> isListLayoutObservable) {

        return Observable.combineLatest(
                shoutsItemsObservable,
                isListLayoutObservable.startWith(true),
                BothParams::of)
                .switchMap(shoutsAndIsList -> {
                    final List<BaseAdapterItem> shouts = shoutsAndIsList.param1();
                    final Boolean isListLayout = shoutsAndIsList.param2();
                    final int adsToLoad = shouts.size() / AD_POSITION_CYCLE;

                    if (adsToLoad < 1) {
                        return Observable.just(shouts);
                    }

                    return facebookHelper.getAdsObservable(isListLayout, adsToLoad)
                            .map((Func1<List<NativeAd>, List<BaseAdapterItem>>) nativeAds -> {
                                final List<BaseAdapterItem> items = new ArrayList<>(shouts);

                                int adPosition;
                                for (int i = 0; i < nativeAds.size(); i++) {
                                    adPosition = (i + 1) * AD_POSITION_CYCLE;
                                    items.add(adPosition, new FbAdAdapterItem(nativeAds.get(i)));
                                }

                                return ImmutableList.copyOf(items);
                            });
                })
                .subscribeOn(uiScheduler);
    }
}
