package com.shoutit.app.android.view.promote;


import android.support.annotation.Nullable;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.PromoteLabel;
import com.shoutit.app.android.api.model.PromoteOption;
import com.shoutit.app.android.api.model.PromoteRequest;
import com.shoutit.app.android.api.model.PromoteResponse;
import com.shoutit.app.android.dao.PromoteLabelsDao;
import com.shoutit.app.android.dao.PromoteOptionsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.subjects.PublishSubject;

public class PromotePresenter {

    private static final int PAGE_SWITCH_INTERVAL = 5;

    private final PublishSubject<PromoteOption> buyButtonClicked = PublishSubject.create();
    private final PublishSubject<PromoteOption> buyOptionSubject = PublishSubject.create();
    private final PublishSubject<Object> startSwitchingPages = PublishSubject.create();

    @Nonnull
    private final Observable<List<BaseAdapterItem>> adapterItemsObservable;
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Observable<BothParams<String, PromoteResponse>> successfullyPromotedObservable;
    @Nonnull
    private final Observable<Object> notEnoughCreditsObservable;
    @Nonnull
    private final Observable<Object> switchLabelsObservable;
    @Nonnull
    private final Observable<PromoteOption> showConfirmDialogObservable;

    public PromotePresenter(@Nonnull PromoteLabelsDao promoteLabelsDao,
                            @Nonnull PromoteOptionsDao promoteOptionsDao,
                            @Nonnull @UiScheduler Scheduler uiScheduler,
                            @Nonnull @NetworkScheduler Scheduler networkScheduler,
                            @Nonnull ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter,
                            @Nonnull UserPreferences userPreferences,
                            @Nonnull ApiService apiService,
                            @Nullable String shoutTitle,
                            @Nonnull String shoutId) {

        switchLabelsObservable = startSwitchingPages
                .switchMap(new Func1<Object, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Object o) {
                        return Observable.interval(PAGE_SWITCH_INTERVAL, TimeUnit.SECONDS)
                                .observeOn(uiScheduler);
                    }
                })
                .map(Functions1.toObject());

        final Observable<ResponseOrError<List<PromoteLabel>>> labelsRequest = promoteLabelsDao
                .getLabelsObservable()
                .observeOn(uiScheduler);

        final Observable<ResponseOrError<List<PromoteOption>>> optionsRequest = promoteOptionsDao
                .getOptionsObservable()
                .observeOn(uiScheduler);

        final Observable<Integer> creditsObservable =
                userPreferences.getUserObservable()
                        .map(user -> user.getStats().getCredits())
                        .compose(ObservableExtensions.behaviorRefCount());

        final Observable<BaseAdapterItem> creditsAdapterItemObservable = creditsObservable
                .map(PromoteAdapterItems.AvailableCreditsAdapterItem::new);

        final Observable<BaseAdapterItem> labelsAdapterItemObservable = labelsRequest
                .compose(ResponseOrError.onlySuccess())
                .map((Func1<List<PromoteLabel>, PromoteAdapterItems.LabelsAdapterItem>) promoteLabels ->
                        new PromoteAdapterItems.LabelsAdapterItem(promoteLabels, switchLabelsObservable, startSwitchingPages, shoutTitle));

        final Observable<List<BaseAdapterItem>> optionsAdapterItemsObservable = optionsRequest
                .compose(ResponseOrError.onlySuccess())
                .map(new Func1<List<PromoteOption>, List<BaseAdapterItem>>() {
                    @Override
                    public List<BaseAdapterItem> call(List<PromoteOption> promoteOptions) {
                        return ImmutableList.copyOf(
                                Lists.transform(promoteOptions, input -> new PromoteAdapterItems.OptionAdapterItem(input, buyButtonClicked))
                        );
                    }
                });

        adapterItemsObservable = Observable.combineLatest(
                labelsAdapterItemObservable,
                optionsAdapterItemsObservable,
                creditsAdapterItemObservable,
                (Func3<BaseAdapterItem, List<BaseAdapterItem>, BaseAdapterItem, List<BaseAdapterItem>>)
                        (labelsAdapterItems, optionAdapterItems, availableCreditsAdapterItem) ->
                                ImmutableList.<BaseAdapterItem>builder()
                                        .add(labelsAdapterItems)
                                        .addAll(optionAdapterItems)
                                        .add(availableCreditsAdapterItem)
                                        .build());

        final Observable<Optional<PromoteOption>> optionToBuy = buyButtonClicked
                .withLatestFrom(creditsObservable, (Func2<PromoteOption, Integer, Optional<PromoteOption>>) (promoteOption, userCredits) -> {
                    if (promoteOption.getCredits() > userCredits) {
                        return Optional.absent();
                    } else {
                        return Optional.of(promoteOption);
                    }
                })
                .compose(ObservableExtensions.behaviorRefCount());

        showConfirmDialogObservable = optionToBuy
                .filter(Optional::isPresent)
                .map(Optional::get);

        final Observable<ResponseOrError<PromoteResponse>> promoteRequestObservable = buyOptionSubject
                .switchMap(option -> apiService.promote(shoutId, new PromoteRequest(option.getId()))
                        .subscribeOn(networkScheduler)
                        .observeOn(uiScheduler)
                        .compose(ResponseOrError.toResponseOrErrorObservable()))
                .compose(ObservableExtensions.behaviorRefCount());

        successfullyPromotedObservable = promoteRequestObservable
                .compose(ResponseOrError.onlySuccess())
                .map(promoteResponse -> new BothParams<>(shoutTitle, promoteResponse))
                .doOnNext(stringPromoteResponseBothParams -> shoutsGlobalRefreshPresenter.refreshShouts());

        notEnoughCreditsObservable = optionToBuy
                .filter(option -> !option.isPresent())
                .map(Functions1.toObject());

        errorObservable = ResponseOrError.combineErrorsObservable(
                ImmutableList.of(
                        ResponseOrError.transform(labelsRequest),
                        ResponseOrError.transform(optionsRequest),
                        ResponseOrError.transform(promoteRequestObservable)
                ))
                .filter(Functions1.isNotNull());

        progressObservable = Observable.merge(
                buyOptionSubject.map(Functions1.returnTrue()),
                adapterItemsObservable.map(Functions1.returnFalse()),
                successfullyPromotedObservable.map(Functions1.returnFalse()),
                notEnoughCreditsObservable.map(Functions1.returnFalse()),
                errorObservable.map(Functions1.returnFalse()))
                .startWith(true);
    }

    public void buyOption(@Nullable PromoteOption option) {
        buyOptionSubject.onNext(option);
    }

    @Nonnull
    public Observable<BothParams<String, PromoteResponse>> getSuccessfullyPromotedObservable() {
        return successfullyPromotedObservable;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> getAdapterItemsObservable() {
        return adapterItemsObservable;
    }

    @Nonnull
    public Observable<Throwable> getErrorObservable() {
        return errorObservable;
    }

    @Nonnull
    public Observable<Boolean> getProgressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observable<Object> getNotEnoughCreditsObservable() {
        return notEnoughCreditsObservable;
    }

    @Nonnull
    public Observable<PromoteOption> getShowConfirmDialogObservable() {
        return showConfirmDialogObservable;
    }
}