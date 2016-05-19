package com.shoutit.app.android.view.chats.chat_info.chats_blocked;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatBlockedUsersAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    private final Picasso mPicasso;

    @Inject
    public ChatBlockedUsersAdapter(@ForActivity @Nonnull Context context, Picasso picasso) {
        super(context);
        mPicasso = picasso;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return BlockedParticipantViewHolder.create(mLayoutInflater.inflate(R.layout.blocked_participant_item, parent, false), mPicasso);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    static class BlockedParticipantViewHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

        @Bind(R.id.chat_blocked_participant_item_image)
        ImageView mChatBlockedParticipantItemImage;
        @Bind(R.id.chat_blocked_participant_item_name)
        TextView mChatBlockedParticipantItemName;

        @Nonnull
        private final View mItemView;
        private final Picasso mPicasso;

        public BlockedParticipantViewHolder(@Nonnull View itemView, Picasso picasso) {
            super(itemView);
            mItemView = itemView;
            mPicasso = picasso;
            ButterKnife.bind(this, itemView);
        }

        public static BlockedParticipantViewHolder create(View layout, Picasso picasso) {
            return new BlockedParticipantViewHolder(layout, picasso);
        }

        @Override
        public void bind(@Nonnull BaseAdapterItem item) {
            final BlockedProfileItem profileItem = (BlockedProfileItem) item;
            mPicasso.load(profileItem.getImage())
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .error(R.drawable.ic_rect_avatar_placeholder)
                    .fit()
                    .centerCrop()
                    .into(mChatBlockedParticipantItemImage);

            mChatBlockedParticipantItemName.setText(profileItem.getName());
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profileItem.itemClicked();
                }
            });
        }
    }
}
