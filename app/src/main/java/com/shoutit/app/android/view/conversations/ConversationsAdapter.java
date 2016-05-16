package com.shoutit.app.android.view.conversations;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Strings;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConversationsAdapter extends BaseAdapter {

    public static class ConversationItemHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

        @Nonnull
        private final View mItemView;
        private final Picasso mPicasso;
        private final Resources mResources;

        @Bind(R.id.conversation_shout_item_image)
        ImageView mImageIv;
        @Bind(R.id.conversation_shout_item_shout_title)
        TextView mTitleTv;
        @Bind(R.id.conversation_shout_item_name)
        TextView mSubtitleTv;
        @Bind(R.id.conversation_shout_item_message)
        TextView mLastMessageTv;
        @Bind(R.id.conversation_shout_item_time)
        TextView mTimeTv;

        public ConversationItemHolder(@Nonnull View itemView, Picasso picasso, Resources resources) {
            super(itemView);
            mItemView = itemView;
            mPicasso = picasso;
            mResources = resources;
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull final BaseAdapterItem item) {
            final ConversationsPresenter.ConversationAdapterItem adapterItem =
                    (ConversationsPresenter.ConversationAdapterItem) item;

            mPicasso.load(adapterItem.getImage())
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .error(R.drawable.ic_rect_avatar_placeholder)
                    .into(mImageIv);

            final String title = adapterItem.getTitle();
            setText(mTitleTv, title);
            mTitleTv.setTextColor(mResources.getColor(
                    adapterItem.isShoutChat() ? R.color.accent_blue : R.color.black_87));

            final String subTitle = adapterItem.getSubTitle();
            setText(mSubtitleTv, subTitle);

            mLastMessageTv.setText(adapterItem.getMessage());
            mTimeTv.setText(adapterItem.getTime());

            mItemView.setBackgroundColor(adapterItem.isUnread() ?
                    mResources.getColor(R.color.conversation_not_read) : Color.WHITE);

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapterItem.click();
                }
            });
        }

        private void setText(TextView textView, String text) {
            if (Strings.isNullOrEmpty(text)) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(text);
            }
        }

        private static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso, Resources resources) {
            return new ConversationItemHolder(view, picasso, resources);
        }
    }

    @NonNull
    private final Picasso mPicasso;

    @Inject
    public ConversationsAdapter(@ForActivity @Nonnull Context context, @NonNull Picasso picasso) {
        super(context);
        mPicasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ConversationItemHolder.create(
                layoutInflater.inflate(R.layout.conversation_item, parent, false), mPicasso, context.getResources());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }
}
