package com.shoutit.app.android.view.shouts.discover;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.ShoutsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.utils.FBAdHalfPresenter;
import com.shoutit.app.android.utils.PromotionHelper;
import com.shoutit.app.android.utils.rx.RxMoreObservers;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class DiscoverShoutsPresenter {

    private final Observable<List<BaseAdapterItem>> mListObservable;
    private final Observable<Throwable> mThrowableObservable;
    private final Observable<Boolean> mProgressObservable;
    private final Observable<BothParams<String, String>> searchClickedObservable;
    private final Observable<String> shareClickedObservable;
    private final Observable<Integer> countObservable;
    private final DiscoverShoutsDao mDiscoverShoutsDao;
    private final String discoverId;

    @Nonnull
    private final PublishSubject<String> shoutSelectedObserver = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> searchMenuItemClicked = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> shareMenuItemClicked = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Boolean> isLinearLayoutSubject = PublishSubject.create();

    public DiscoverShoutsPresenter(@NetworkScheduler Scheduler networkScheduler,
                                   @UiScheduler Scheduler uiScheduler,
                                   DiscoverShoutsDao discoverShoutsDao,
                                   final String discoverId,
                                   final String discoverName,
                                   UserPreferences userPreferences,
                                   FBAdHalfPresenter fbAdHalfPresenter,
                                   @ForActivity final Context context) {
        mDiscoverShoutsDao = discoverShoutsDao;
        this.discoverId = discoverId;

        final boolean isNormalUser = userPreferences.isNormalUser();
        final BaseProfile currentUser = userPreferences.getUserOrPage();
        final String currentUserName = currentUser != null ? currentUser.getUsername() : null;

        final Observable<ResponseOrError<ShoutsResponse>> shoutsObservable = discoverShoutsDao
                .getShoutsObservable(discoverId)
                .compose(ObservableExtensions.<ResponseOrError<ShoutsResponse>>behaviorRefCount())
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler);

        final Observable<ShoutsResponse> successShoutsObservable = shoutsObservable
                .compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .compose(ObservableExtensions.<ShoutsResponse>behaviorRefCount());

        final Observable<List<BaseAdapterItem>> shoutItems = successShoutsObservable
                .map(new Func1<ShoutsResponse, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(ShoutsResponse shoutsResponse) {
                        return ImmutableList.copyOf(Iterables.transform(shoutsResponse.getShouts(), new Function<Shout, BaseAdapterItem>() {
                            @Nullable
                            @Override
                            public BaseAdapterItem apply(Shout shout) {
                                final boolean isShoutOwner = shout.getProfile().getUsername().equals(currentUserName);
                                return new ShoutAdapterItem(shout, isShoutOwner, isNormalUser, context, shoutSelectedObserver, PromotionHelper.promotionInfoOrNull(shout));
                            }
                        }));
                    }
                });

        mListObservable = fbAdHalfPresenter.getShoutsWithAdsObservable(shoutItems, isLinearLayoutSubject);

        countObservable = shoutsObservable.compose(ResponseOrError.<ShoutsResponse>onlySuccess())
                .map(new Func1<ShoutsResponse, Integer>() {
                    @Override
                    public Integer call(ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getCount();
                    }
                });


        mThrowableObservable = shoutsObservable.compose(ResponseOrError.<ShoutsResponse>onlyError());
        mProgressObservable = shoutsObservable.map(Functions1.returnFalse()).startWith(true);

        searchClickedObservable = searchMenuItemClicked
                .map(new Func1<Object, BothParams<String, String>>() {
                    @Override
                    public BothParams<String, String> call(Object o) {
                        return new BothParams<>(discoverId, discoverName);
                    }
                });

        shareClickedObservable = shareMenuItemClicked.withLatestFrom(successShoutsObservable,
                new Func2<Object, ShoutsResponse, String>() {
                    @Override
                    public String call(Object o, ShoutsResponse shoutsResponse) {
                        return shoutsResponse.getWebUrl();
                    }
                });
    }

    @Nonnull
    public Observable<String> getShareClickedObservable() {
        return shareClickedObservable;
    }

    @Nonnull
    public Observable<Integer> getCountObservable() {
        return countObservable;
    }

    @Nonnull
    public Observable<BothParams<String, String>> getSearchClickedObservable() {
        return searchClickedObservable;
    }

    @NonNull
    public Observable<List<BaseAdapterItem>> getSuccessObservable() {
        return mListObservable;
    }

    @NonNull
    public Observable<Throwable> getFailObservable() {
        return mThrowableObservable;
    }

    @NonNull
    public Observable<Boolean> getProgressVisible() {
        return mProgressObservable;
    }

    @NonNull
    public Observer<Object> getLoadMoreObserver() {
        return RxMoreObservers.ignoreCompleted(mDiscoverShoutsDao.getShoutsDao(discoverId).getLoadMoreObserver());
    }

    @Nonnull
    public Observable<String> getShoutSelectedObservable() {
        return shoutSelectedObserver;
    }

    public void onSearchClicked() {
        searchMenuItemClicked.onNext(null);
    }

    public void onShareClicked() {
        shareMenuItemClicked.onNext(null);
    }

    public void setLinearLayoutManager(boolean isLinearLayout) {
        isLinearLayoutSubject.onNext(isLinearLayout);
    }
}