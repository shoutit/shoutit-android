package com.shoutit.app.android.view.promote;

import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.PromoteLabel;
import com.shoutit.app.android.api.model.PromoteOption;

import javax.annotation.Nonnull;

import rx.Observer;

public class PromoteAdapterItems {

    public static class LabelsAdapterItem extends BaseNoIDAdapterItem {

        @NonNull
        private final PromoteLabel promoteLabel;

        public LabelsAdapterItem(@NonNull PromoteLabel promoteLabel) {
            this.promoteLabel = promoteLabel;
        }

        @NonNull
        public PromoteLabel getPromoteLabel() {
            return promoteLabel;
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
    }

}
