package com.shoutit.app.android.view.shouts;

import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Strings;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class ShoutGridViewHolder extends ViewHolderManager.BaseViewHolder<ShoutAdapterItem> implements View.OnClickListener {

    @Bind(R.id.shout_grid_image_view)
    ImageView cardImageView;
    @Bind(R.id.shout_grid_title_tv)
    TextView titleTextView;
    @Bind(R.id.home_feed_card_name_tv)
    TextView nameTextView;
    @Bind(R.id.shout_grid_price_tv)
    TextView cardPriceTextView;
    @Bind(R.id.shout_promoted_label)
    TextView mShoutPromotedLabel;
    @Bind(R.id.shout_container)
    View mShoutContainer;
    @Bind(R.id.shout_grid_bookmark)
    CheckBox mBoomark;

    private final Picasso picasso;
    private ShoutAdapterItem item;
    private CompositeSubscription mSubscription;

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.shout_item_grid;
    }

    public ShoutGridViewHolder(@Nonnull View itemView, Picasso picasso) {
        super(itemView);
        this.picasso = picasso;
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void bind(@Nonnull ShoutAdapterItem item) {
        unsubscribe();

        this.item = item;
        final Shout shout = item.getShout();
        titleTextView.setText(shout.getTitle());
        titleTextView.setVisibility(TextUtils.isEmpty(shout.getTitle()) ? View.GONE : View.VISIBLE);
        nameTextView.setText(shout.getProfile().getName());
        cardPriceTextView.setText(item.getShoutPrice());

        picasso.load(Strings.emptyToNull(shout.getThumbnail()))
                .placeholder(R.drawable.pattern_placeholder)
                .fit()
                .centerCrop()
                .into(cardImageView);

        if (item.isPromoted()) {
            mShoutPromotedLabel.setVisibility(View.VISIBLE);
            mShoutPromotedLabel.setText(item.getLabel());
            mShoutPromotedLabel.setBackgroundColor(item.getColor());
            mShoutContainer.setBackgroundColor(item.getBgColor());
        } else {
            mShoutPromotedLabel.setVisibility(View.GONE);
            mShoutContainer.setBackgroundColor(Color.WHITE);
        }

        mSubscription = new CompositeSubscription(item.getBookmarkObservable()
                .subscribe(checked -> {
                    mBoomark.setChecked(checked);
                }), item.getEnableObservable().subscribe(enable -> {
            mBoomark.setEnabled(enable);
        }));

        mBoomark.setOnClickListener(v -> item.onBookmarkSelectionChanged(mBoomark.isChecked()));
    }

    @Override
    public void onClick(View v) {
        item.onShoutSelected();
    }

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
        unsubscribe();
    }

    private void unsubscribe() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }
}