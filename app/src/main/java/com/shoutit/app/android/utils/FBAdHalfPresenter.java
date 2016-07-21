package com.shoutit.app.android.utils;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.adapteritems.FbAdAdapterItem;
import com.shoutit.app.android.facebook.FacebookHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;

public class FBAdHalfPresenter {

    private static final int AD_POSITION_CYCLE = 25;

    private final FacebookHelper facebookHelper;
    private final Scheduler uiScheduler;

    private final BehaviorSubject<Integer> shoutsCount = BehaviorSubject.create();

    @Nonnull
    private final Observable<List<NativeAd>> listAdsObservable;

    @Inject
    public FBAdHalfPresenter(FacebookHelper facebookHelper,
                             @UiScheduler Scheduler uiScheduler) {
        this.facebookHelper = facebookHelper;
        this.uiScheduler = uiScheduler;

        listAdsObservable = getAdsFetchObservable(facebookHelper.getListAdManager())
                .cache(1);
    }

    @Nonnull
    public Observable<List<NativeAd>> getAdsObservable() {
                return listAdsObservable;
    }

    @Nonnull
    private Observable<List<NativeAd>> getAdsFetchObservable(NativeAdsManager nativeAdsManager) {
        return shoutsCount
                .scan(BothParams.of(0, 0), (oldShoutsCountWithOldAdsToLoad, newShoutsCount) -> {
                    final Integer prevShoutsCount = oldShoutsCountWithOldAdsToLoad.param1();

                    final int adsToLoad = (newShoutsCount / AD_POSITION_CYCLE) - (prevShoutsCount / AD_POSITION_CYCLE);
                    return BothParams.of(newShoutsCount, adsToLoad);
                })
                .map(BothParams::param2)
                .filter(adsToLoad -> adsToLoad > 0)
                .switchMap(adsToLoad -> facebookHelper.getAdsObservable(nativeAdsManager, adsToLoad))
                .scan(new ArrayList<>(), new Func2<List<NativeAd>, List<NativeAd>, List<NativeAd>>() {
                    @Override
                    public List<NativeAd> call(List<NativeAd> adsList, List<NativeAd> newAds) {
                        return ImmutableList.<NativeAd>builder()
                                .addAll(adsList)
                                .addAll(newAds)
                                .build();
                    }
                })
                .subscribeOn(uiScheduler);
    }

    @Nonnull
    public static List<BaseAdapterItem> combineShoutsWithAds(List<BaseAdapterItem> shouts, List<NativeAd> nativeAds) {
        final List<BaseAdapterItem> shoutsWithAdsItems = new ArrayList<>(shouts);

        int adPosition;
        for (int i = 0; i < nativeAds.size(); i++) {
            adPosition = (i + 1) * AD_POSITION_CYCLE;
            if (adPosition < shouts.size()) {
                shoutsWithAdsItems.add(adPosition, new FbAdAdapterItem(nativeAds.get(i)));
            }
        }

        return ImmutableList.copyOf(shoutsWithAdsItems);
    }

    public void updatedShoutsCount(List<BaseAdapterItem> shoutItems) {
        shoutsCount.onNext(shoutItems.size());
    }
}
