package com.shoutit.app.android.view.conversations;

import android.content.Context;
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

    public static class ConversationChatItemHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

        @Nonnull
        private final View mItemView;
        private final Picasso mPicasso;

        @Bind(R.id.conversation_chat_item_image)
        ImageView mConversationChatItemImage;
        @Bind(R.id.conversation_chat_item_name)
        TextView mConversationChatItemName;
        @Bind(R.id.conversation_chat_item_message)
        TextView mConversationChatItemMessage;
        @Bind(R.id.conversation_chat_item_time)
        TextView mConversationShoutItemTime;

        public ConversationChatItemHolder(@Nonnull View itemView, Picasso picasso) {
            super(itemView);
            mItemView = itemView;
            mPicasso = picasso;
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull BaseAdapterItem item) {
            final ConversationsPresenter.ConversationChatItem conversationChatItem = (ConversationsPresenter.ConversationChatItem) item;
            mPicasso.load(conversationChatItem.getImage())
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .error(R.drawable.ic_rect_avatar_placeholder)
                    .into(mConversationChatItemImage);
            mConversationChatItemName.setText(conversationChatItem.getUser());
            mConversationChatItemMessage.setText(conversationChatItem.getMessage());
            mConversationShoutItemTime.setText(conversationChatItem.getTime());

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    conversationChatItem.click();
                }
            });
        }

        private static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
            return new ConversationChatItemHolder(view, picasso);
        }
    }

    public static class ConversationShoutItemHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

        @Nonnull
        private final View mItemView;
        private final Picasso mPicasso;

        @Bind(R.id.conversation_shout_item_image)
        ImageView mConversationShoutItemImage;
        @Bind(R.id.conversation_shout_item_shout_title)
        TextView mConversationShoutItemShoutTitle;
        @Bind(R.id.conversation_shout_item_name)
        TextView mConversationShoutItemName;
        @Bind(R.id.conversation_shout_item_message)
        TextView mConversationShoutItemMessage;
        @Bind(R.id.conversation_shout_item_time)
        TextView mConversationShoutItemTime;

        public ConversationShoutItemHolder(@Nonnull View itemView, Picasso picasso) {
            super(itemView);
            mItemView = itemView;
            mPicasso = picasso;
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull final BaseAdapterItem item) {
            final ConversationsPresenter.ConversationShoutItem conversationShoutItem = (ConversationsPresenter.ConversationShoutItem) item;

            mPicasso.load(conversationShoutItem.getImage())
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .error(R.drawable.ic_rect_avatar_placeholder)
                    .into(mConversationShoutItemImage);
            final String shoutDescription = conversationShoutItem.getShoutDescription();
            if(Strings.isNullOrEmpty(shoutDescription)){
                mConversationShoutItemShoutTitle.setVisibility(View.GONE);
            } else {
                mConversationShoutItemShoutTitle.setVisibility(View.VISIBLE);
                mConversationShoutItemShoutTitle.setText(shoutDescription);
            }
            mConversationShoutItemName.setText(conversationShoutItem.getUserNames());
            mConversationShoutItemMessage.setText(conversationShoutItem.getMessage());
            mConversationShoutItemTime.setText(conversationShoutItem.getTime());

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    conversationShoutItem.click();
                }
            });
        }

        private static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso) {
            return new ConversationShoutItemHolder(view, picasso);
        }
    }

    private static final int CONVERSATION_CHAT_TYPE = 0;
    private static final int CONVERSATION_SHOUT_TYPE = 1;

    @NonNull
    private final Picasso mPicasso;

    @Inject
    public ConversationsAdapter(@ForActivity @Nonnull Context context, @NonNull Picasso picasso) {
        super(context);
        mPicasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case CONVERSATION_CHAT_TYPE:
                return ConversationChatItemHolder.create(layoutInflater.inflate(R.layout.conversation_chat_item, parent, false), mPicasso);
            case CONVERSATION_SHOUT_TYPE:
                return ConversationShoutItemHolder.create(layoutInflater.inflate(R.layout.conversation_shout_item, parent, false), mPicasso);
            default:
                throw new RuntimeException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem baseAdapterItem = items.get(position);
        if (baseAdapterItem instanceof ConversationsPresenter.ConversationChatItem) {
            return CONVERSATION_CHAT_TYPE;
        } else if (baseAdapterItem instanceof ConversationsPresenter.ConversationShoutItem) {
            return CONVERSATION_SHOUT_TYPE;
        } else {
            throw new RuntimeException();
        }
    }
}
