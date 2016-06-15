package com.shoutit.app.android.view.promote;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.PromoteLabel;
import com.shoutit.app.android.api.model.PromoteOption;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;

public class PromoteAdapterItems {

    public static class LabelsAdapterItem extends BaseNoIDAdapterItem {

        @NonNull
        private final List<PromoteLabel> promoteLabels;
        @Nonnull
        private final Observable<Object> switchLabelPageObservable;
        @Nonnull
        private final Observer<Object> startSwitchingPages;
        @Nullable
        private final String shoutTitle;

        public LabelsAdapterItem(@NonNull List<PromoteLabel> promoteLabels,
                                 @Nonnull Observable<Object> switchLabelPageObservable,
                                 @Nonnull Observer<Object> startSwitchingPages,
                                 @Nullable String shoutTitle) {
            this.promoteLabels = promoteLabels;
            this.switchLabelPageObservable = switchLabelPageObservable;
            this.startSwitchingPages = startSwitchingPages;
            this.shoutTitle = shoutTitle;
        }

        @NonNull
        public List<PromoteLabel> getPromoteLabels() {
            return promoteLabels;
        }

        @Nonnull
        public Observable<Object> getSwitchLabelPageObservable() {
            return switchLabelPageObservable;
        }

        public void startSwitchingPages() {
            startSwitchingPages.onNext(null);
        }

        @Nullable
        public String getShoutTitle() {
            return shoutTitle;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof LabelsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return false;
        }
    }

    public static class OptionAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final PromoteOption promoteOption;
        @Nonnull
        private final Observer<PromoteOption> promoteOptionBuyClickedObserver;

        public OptionAdapterItem(@Nonnull PromoteOption promoteOption,
                                 @Nonnull Observer<PromoteOption> promoteOptionBuyClickedObserver) {
            this.promoteOption = promoteOption;
            this.promoteOptionBuyClickedObserver = promoteOptionBuyClickedObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof OptionAdapterItem &&
                    promoteOption.getId().equals(((OptionAdapterItem) baseAdapterItem).promoteOption.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return false;
        }

        @Nonnull
        public PromoteOption getPromoteOption() {
            return promoteOption;
        }

        public void onBuyClicked() {
            promoteOptionBuyClickedObserver.onNext(promoteOption);
        }
    }

    public static class AvailableCreditsAdapterItem extends BaseNoIDAdapterItem {

        private final int credits;

        public AvailableCreditsAdapterItem(int credits) {
            this.credits = credits;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof AvailableCreditsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return false;
        }

        public int getCredits() {
            return credits;
        }
    }

}
