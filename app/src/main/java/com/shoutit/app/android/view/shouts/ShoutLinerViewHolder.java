package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Strings;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.DateTimeUtils;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.PriceUtils;
import com.shoutit.app.android.view.chats.ChatActivity;
import com.shoutit.app.android.view.chats.chatsfirstconversation.ChatFirstConversationActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Named;

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
    private final Picasso picassoNoTransformer;
    private ShoutAdapterItem item;

    public ShoutLinerViewHolder(@Nonnull View itemView, Context context, Picasso picasso,
                                @Named("NoAmazonTransformer") Picasso picassoNoTransformer) {
        super(itemView);
        this.context = context;
        this.picasso = picasso;
        this.picassoNoTransformer = picassoNoTransformer;
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void bind(@Nonnull final ShoutAdapterItem item) {
        this.item = item;
        recycle();

        final Shout shout = item.getShout();

        titleTextView.setText(shout.getTitle());

        final String timeAgo = DateTimeUtils.timeAgoFromSecondsToWeek(context, shout.getDatePublishedInMillis());
        nameTextView.setText(context.getString(R.string.home_user_and_date, shout.getProfile().getName(), timeAgo));

        final String price = PriceUtils.formatPriceWithCurrency(shout.getPrice(),
                context.getResources(), shout.getCurrency());
        cardPriceTextView.setText(price);

        typeLabelTextView.setText(shout.getTypeResId());

        picasso.load(Strings.emptyToNull(shout.getThumbnail()))
                .placeholder(R.drawable.pattern_placeholder)
                .fit()
                .centerCrop()
                .into(cardImageView);

        picassoNoTransformer.load(item.getCategoryIconUrl())
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

        final List<Conversation> conversations = shout.getConversations();
        final boolean enableChatIcon = !item.isShoutOwner() && item.isNormalUser();
        chatIcon.setEnabled(enableChatIcon);
        chatIcon.setAlpha(enableChatIcon ? 1f : 0.5f);

        subscription = new CompositeSubscription(
                RxView.clicks(chatIcon)
                        .subscribe(new Action1<Void>() {
                            @Override
                            public void call(Void aVoid) {
                                if (!item.isNormalUser()) {
                                    return;
                                }

                                final boolean hasConversation = conversations != null && !conversations.isEmpty();
                                if (hasConversation) {
                                    context.startActivity(ChatActivity.newIntent(context, conversations.get(0).getId()));
                                } else {
                                    context.startActivity(ChatFirstConversationActivity.newIntent(context, true, item.getShout().getId()));
                                }
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