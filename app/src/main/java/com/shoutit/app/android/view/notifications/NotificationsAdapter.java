package com.shoutit.app.android.view.notifications;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Strings;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.NotificationsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.DateTimeUtils;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.viewholders.NoDataTextViewHolder;
import com.shoutit.app.android.viewholders.NoDataViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotificationsAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_NOTIFICATION = 1;
    private static final int VIEW_TYPE_NO_DATA = 2;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public NotificationsAdapter(@ForActivity @Nonnull Context context,
                                @Nonnull Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    public class NotificationViewHolder extends ViewHolderManager.BaseViewHolder<NotificationsPresenter.NotificationAdapterItem> {

        @Bind(R.id.notifications_root_view)
        View rootView;
        @Bind(R.id.notifications_text_tv)
        TextView textTv;
        @Bind(R.id.notifications_time_ago_tv)
        TextView timeAgoTextView;
        @Bind(R.id.notifications_avatar_iv)
        ImageView avatarImageView;

        private NotificationsPresenter.NotificationAdapterItem item;
        private final Target target;

        public NotificationViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            target = PicassoHelper.getRoundedBitmapTarget(context, avatarImageView,
                    context.getResources().getDimensionPixelSize(R.dimen.notifications_avatar_corners));
        }

        @Override
        public void bind(@Nonnull NotificationsPresenter.NotificationAdapterItem item) {
            this.item = item;
            final NotificationsResponse.Notification notification = item.getNotification();

            rootView.setBackground(context.getResources().getDrawable(
                    notification.isRead() ? R.drawable.white_selector : R.drawable.selector_notification));

            final String timeAgo = DateTimeUtils.timeAgoFromDate(notification.getCreatedAtInMillis());
            timeAgoTextView.setText(timeAgo);

            final NotificationsResponse.DisplayInfo displayInfo = item.getDisplayInfo();
            final SpannableString spannedText = getSpannedText(displayInfo.getText(), displayInfo.getRanges());
            textTv.setText(spannedText);

            picasso.load(Strings.emptyToNull(displayInfo.getImage()))
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .into(target);
        }

        @Nonnull
        private SpannableString getSpannedText(String text, List<NotificationsResponse.Range> rangeList) {
            final SpannableString spannableString = new SpannableString(text);


            for (NotificationsResponse.Range range : rangeList) {
                final int start = range.getOffset();
                final int end = start + range.getLength();

                spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannableString.setSpan(new TypefaceSpan("sans-serif-medium"), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.black_87))
                        , start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            return spannableString;
        }

        @OnClick(R.id.notifications_root_view)
        public void onItemClicked() {
            item.onNotificationClicked();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NOTIFICATION:
                return new NotificationViewHolder(layoutInflater.inflate(R.layout.notifications_item, parent, false));
            case VIEW_TYPE_NO_DATA:
                return new NoDataTextViewHolder(layoutInflater.inflate(R.layout.no_data_text_adapter_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type:" + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof NotificationsPresenter.NotificationAdapterItem) {
            return VIEW_TYPE_NOTIFICATION;
        } else if (item instanceof NoDataTextAdapterItem) {
            return VIEW_TYPE_NO_DATA;
        }else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }
}
