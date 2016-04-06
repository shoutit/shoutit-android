package com.shoutit.app.android.view.notifications;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.NotificationsResponse;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.DateTimeUtils;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.viewholders.NoDataViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotificationsAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_NOTIFICATION = 1;
    private static final int VIEW_TYPE_SHADOW = 2;

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

            final NotificationsResponse.AttachedObject attachedObject = notification.getAttachedObject();
            if (attachedObject != null) {
                String imageUrl = null;
                if (notification.isMessageNotification() && attachedObject.getMessage() != null) {
                    final BaseProfile profile = attachedObject.getMessage().getProfile();
                    imageUrl = profile.getImage();

                    final String spannedText = getSpannedText(
                            profile.getFirstName(),
                            attachedObject.getMessage().getText());
                    textTv.setText(spannedText);
                } else if (notification.isListenNotification() && attachedObject.getProfile() != null) {
                    final BaseProfile profile = attachedObject.getProfile();
                    imageUrl = profile.getImage();

                    final String spannedText = getSpannedText(
                            profile.getFirstName(),
                            context.getString(R.string.notifications_listening_to_you));
                    textTv.setText(spannedText);
                }

                picasso.load(imageUrl)
                        .placeholder(R.drawable.ic_rect_avatar_placeholder)
                        .into(target);
            }
        }

        @Nonnull
        private String getSpannedText(String name, String message) {
            final String text = name + "  " + message;
            final SpannableStringBuilder builder = new SpannableStringBuilder(text);
            final StyleSpan boldedName = new StyleSpan(Typeface.BOLD);
            final TypefaceSpan typefaceSpan = new TypefaceSpan("sans-serif-medium");

            builder.setSpan(boldedName, 0, name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            builder.setSpan(typefaceSpan, 0, name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            return builder.toString();
        }

        @OnClick(R.id.notifications_root_view)
        public void onItemClicked() {
            if (item.getNotification().isListenNotification()) {
                item.openProfile();
            } else {
                Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NOTIFICATION:
                return new NotificationViewHolder(layoutInflater.inflate(R.layout.notifications_item, parent, false));
            case VIEW_TYPE_SHADOW:
                return new NoDataViewHolder(layoutInflater.inflate(R.layout.shadow_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type:" + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof NotificationsPresenter.NotificationAdapterItem) {
            return VIEW_TYPE_NOTIFICATION;
        } else if (item instanceof NoDataAdapterItem) {
            return VIEW_TYPE_SHADOW;
        } else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }
}
