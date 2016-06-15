package com.shoutit.app.android.view.promote;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.PromoteOption;
import com.shoutit.app.android.dagger.ForActivity;
import com.viewpagerindicator.CirclePageIndicator;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class PromoteAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_PROMOTE_LABELS = 1;
    private static final int VIEW_TYPE_PROMOTE_OPTION = 2;
    private static final int VIEW_TYPE_AVAILABLE_CREDITS = 3;

    @Nonnull
    private final PromoteLabelsPager promoteLabelsPager;

    @Inject
    public PromoteAdapter(@ForActivity @Nonnull Context context,
                          @Nonnull PromoteLabelsPager promoteLabelsPager) {
        super(context);
        this.promoteLabelsPager = promoteLabelsPager;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PROMOTE_LABELS:
                return new LabelsViewHolder(layoutInflater.inflate(R.layout.promote_labels_item, parent, false));
            case VIEW_TYPE_PROMOTE_OPTION:
                return new OptionsViewHolder(layoutInflater.inflate(R.layout.promote_options_item, parent, false));
            case VIEW_TYPE_AVAILABLE_CREDITS:
                return new CreditsViewHolder(layoutInflater.inflate(R.layout.promote_current_credits_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type : " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);

        if (item instanceof PromoteAdapterItems.LabelsAdapterItem) {
            return VIEW_TYPE_PROMOTE_LABELS;
        } else if (item instanceof PromoteAdapterItems.OptionAdapterItem) {
            return VIEW_TYPE_PROMOTE_OPTION;
        } else if (item instanceof PromoteAdapterItems.AvailableCreditsAdapterItem) {
            return VIEW_TYPE_AVAILABLE_CREDITS;
        } else {
            throw new RuntimeException("Unknown view type");
        }
    }

    private class LabelsViewHolder extends ViewHolderManager.BaseViewHolder<PromoteAdapterItems.LabelsAdapterItem> {

        @Bind(R.id.promote_shout_title)
        TextView shoutTitleTv;
        @Bind(R.id.promote_view_pager)
        ViewPager viewPager;
        @Bind(R.id.promote_circle_indicator)
        CirclePageIndicator pageIndicator;

        private Subscription subscription;

        public LabelsViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            viewPager.setAdapter(promoteLabelsPager);
            pageIndicator.setViewPager(viewPager);
        }

        @Override
        public void bind(@Nonnull PromoteAdapterItems.LabelsAdapterItem item) {
            recycle();

            shoutTitleTv.setText(item.getShoutTitle());

            promoteLabelsPager.bindData(item.getPromoteLabels());

            subscription = item.getSwitchLabelPageObservable()
                    .subscribe(o -> {
                        switchPagerPage();
                    });

        }

        private void switchPagerPage() {
            final int currentPosition = viewPager.getCurrentItem();
            final int count = viewPager.getAdapter().getCount();

            if (currentPosition + 1 < count) {
                viewPager.setCurrentItem(currentPosition + 1, true);
            } else {
                viewPager.setCurrentItem(0, true);
            }
        }

        @Override
        public void onViewRecycled() {
            recycle();
            super.onViewRecycled();
        }

        private void recycle() {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    private class OptionsViewHolder extends ViewHolderManager.BaseViewHolder<PromoteAdapterItems.OptionAdapterItem> {

        @Bind(R.id.promote_option_name)
        TextView optionNameTv;
        @Bind(R.id.promote_option_badge)
        TextView optionBadgeTv;
        @Bind(R.id.promote_option_buy_button)
        Button buyButton;
        @Bind(R.id.promote_option_days_tv)
        TextView daysTv;

        private PromoteAdapterItems.OptionAdapterItem item;

        public OptionsViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull PromoteAdapterItems.OptionAdapterItem item) {
            this.item = item;
            final PromoteOption option = item.getPromoteOption();
            final PromoteOption.Label label = option.getLabel();

            optionNameTv.setText(option.getName());

            optionBadgeTv.setText(label.getName());
            optionBadgeTv.setBackgroundColor(Color.parseColor(label.getColor()));

            daysTv.setText(option.getDays() == null ? null : String.valueOf(option.getDays()));

            buyButton.setText(context.getResources().getQuantityString(
                    R.plurals.plural_credits, option.getCredits(), option.getCredits()));

        }

        @OnClick(R.id.promote_option_buy_button)
        public void onBuyButtonClicked() {
            item.onBuyClicked();
        }
    }

    private class CreditsViewHolder extends ViewHolderManager.BaseViewHolder<PromoteAdapterItems.AvailableCreditsAdapterItem> {

        @Bind(R.id.promote_available_credits_tv)
        TextView availableCreditsTv;

        public CreditsViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull PromoteAdapterItems.AvailableCreditsAdapterItem item) {
            availableCreditsTv.setText(item.getCredits());
        }
    }


}
