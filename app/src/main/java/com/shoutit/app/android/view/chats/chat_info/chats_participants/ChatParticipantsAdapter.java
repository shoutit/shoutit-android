package com.shoutit.app.android.view.chats.chat_info.chats_participants;

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

public class ChatParticipantsAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    @Nonnull
    private final Context mContext;
    private final Picasso mPicasso;

    @Inject
    public ChatParticipantsAdapter(@ForActivity @Nonnull Context context, Picasso picasso) {
        super(context);
        mContext = context;
        mPicasso = picasso;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ParticipantViewHolder.create(mLayoutInflater.inflate(R.layout.participant_item, parent, false), mPicasso, mContext);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    static class ParticipantViewHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

        @Nonnull
        private final View mItemView;
        private final Picasso mPicasso;
        private final Context mContext;
        @Bind(R.id.chat_participant_item_image)
        ImageView mChatParticipantItemImage;
        @Bind(R.id.chat_participant_item_name)
        TextView mChatParticipantItemName;
        @Bind(R.id.chat_participant_item_type)
        TextView mChatParticipantItemType;

        public ParticipantViewHolder(@Nonnull View itemView, Picasso picasso, Context context) {
            super(itemView);
            mItemView = itemView;
            mPicasso = picasso;
            mContext = context;
            ButterKnife.bind(this, itemView);
        }

        public static ParticipantViewHolder create(View layout, Picasso picasso, Context context) {
            return new ParticipantViewHolder(layout, picasso, context);
        }

        @Override
        public void bind(@Nonnull BaseAdapterItem item) {
            final ProfileItem profileItem = (ProfileItem) item;

            mPicasso.load(profileItem.getImage())
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .fit()
                    .centerCrop()
                    .into(mChatParticipantItemImage);

            mChatParticipantItemName.setText(profileItem.getName());
            mChatParticipantItemType.setText(getStatus(profileItem));
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profileItem.itemClicked();
                }
            });
        }

        private String getStatus(ProfileItem profileItem) {
            if (profileItem.isBlocked()) {
                return mContext.getString(R.string.chat_paricipants_blocked);
            } else if (profileItem.isAdmin()) {
                return mContext.getString(R.string.chat_participants_admin);
            } else {
                return null;
            }
        }
    }
}
