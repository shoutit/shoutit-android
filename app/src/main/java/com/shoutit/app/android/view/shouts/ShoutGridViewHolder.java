package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Strings;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.utils.PriceUtils;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShoutGridViewHolder extends ViewHolderManager.BaseViewHolder<DiscoverShoutAdapterItem> {

    @Bind(R.id.home_feed_card_image_view)
    ImageView cardImageView;
    @Bind(R.id.home_feed_card_title_tv)
    TextView titleTextView;
    @Bind(R.id.home_feed_card_name_tv)
    TextView nameTextView;
    @Bind(R.id.home_feed_card_price_tv)
    TextView cardPriceTextView;

    private final Picasso picasso;
    private final Context context;

    public ShoutGridViewHolder(@Nonnull View itemView, Picasso picasso, Context context) {
        super(itemView);
        this.picasso = picasso;
        this.context = context;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull DiscoverShoutAdapterItem item) {
        final Shout shout = item.getShout();
        titleTextView.setText(shout.getTitle());
        nameTextView.setText(shout.getUser().getName());
        final String price = PriceUtils.formatPrice(shout.getPrice());
        cardPriceTextView.setText(context.getString(
                        R.string.price_with_currency, price, shout.getCurrency())
        );

        picasso.load(Strings.emptyToNull(shout.getThumbnail()))
                .placeholder(R.drawable.pattern_placeholder)
                .fit()
                .centerCrop()
                .into(cardImageView);
    }
}