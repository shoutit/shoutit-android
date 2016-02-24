package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.utils.DateTimeUtils;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class ShoutLinerViewHolder extends ViewHolderManager.BaseViewHolder<ShoutAdapterItem> implements View.OnClickListener {
    @Bind(R.id.shout_grid_image_view)
    ImageView cardImageView;
    @Bind(R.id.shout_grid_title_tv)
    TextView titleTextView;
    @Bind(R.id.home_feed_card_user_tv)
    TextView nameTextView;
    @Bind(R.id.shout_grid_price_tv)
    TextView cardPriceTextView;
    @Bind(R.id.home_feed_card_chat_iv)
    View chatIcon;
    @Bind(R.id.home_feed_card_item_icon_iv)
    ImageView itemCategoryImageView;
    @Bind(R.id.home_feed_card_type_label_tv)
    TextView typeLabelTextView;
    @Bind(R.id.home_feed_card_country_iv)
    ImageView countryImageView;

    private CompositeSubscription subscription;
    private final Context context;
    private final Picasso picasso;
    private ShoutAdapterItem item;

    public ShoutLinerViewHolder(@Nonnull View itemView, Context context, Picasso picasso) {
        super(itemView);
        this.context = context;
        this.picasso = picasso;
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void bind(@Nonnull ShoutAdapterItem item) {
        this.item = item;
        recycle();

        final Shout shout = item.getShout();

        titleTextView.setText(shout.getTitle());

        final String timeAgo = DateTimeUtils.timeAgoFromSecondsToWeek(context, shout.getDatePublishedInMillis());
        nameTextView.setText(context.getString(R.string.home_user_and_date, shout.getUser().getName(), timeAgo));

        final String price = PriceUtils.formatPrice(shout.getPrice());
        cardPriceTextView.setText(context.getString(
                        R.string.price_with_currency, price, shout.getCurrency())
        );

        typeLabelTextView.setText(shout.getTypeResId());

        picasso.load(Strings.emptyToNull(shout.getThumbnail()))
                .placeholder(R.drawable.pattern_placeholder)
                .fit()
                .centerCrop()
                .into(cardImageView);

        picasso.load(item.getCategoryIconUrl())
                .fit()
                .centerInside()
                .into(itemCategoryImageView);

        final Target target = PicassoHelper.getRoundedBitmapTarget(context, countryImageView);
        cardImageView.setTag(target);

        if (item.getCountryResId().isPresent()) {
            picasso.load(item.getCountryResId().get())
                    .resizeDimen(R.dimen.home_country_icon, R.dimen.home_country_icon)
                    .into(target);
        }

        subscription = new CompositeSubscription(
                RxView.clicks(chatIcon)
                        .subscribe(new Action1<Void>() {
                            @Override
                            public void call(Void aVoid) {
                                Toast.makeText(context, "Not implemented yet", Toast.LENGTH_LONG).show();
                            }
                        })
        );
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

    @Override
    public void onClick(View v) {
        item.onShoutSelected();
    }
}