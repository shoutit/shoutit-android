package com.shoutit.app.android.view.chats.chats_adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.chats.message_models.DateItem;
import com.shoutit.app.android.view.chats.message_models.ReceivedImageMessage;
import com.shoutit.app.android.view.chats.message_models.ReceivedLocationMessage;
import com.shoutit.app.android.view.chats.message_models.ReceivedShoutMessage;
import com.shoutit.app.android.view.chats.message_models.ReceivedTextMessage;
import com.shoutit.app.android.view.chats.message_models.ReceivedVideoMessage;
import com.shoutit.app.android.view.chats.message_models.SentImageMessage;
import com.shoutit.app.android.view.chats.message_models.SentLocationMessage;
import com.shoutit.app.android.view.chats.message_models.SentShoutMessage;
import com.shoutit.app.android.view.chats.message_models.SentTextMessage;
import com.shoutit.app.android.view.chats.message_models.SentVideoMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class ChatsAdapter extends BaseAdapter {

    private static final int DATE_ITEM = 0;
    private static final int RECEIVER_IMAGE = 1;
    private static final int RECEIVER_LOCATION = 2;
    private static final int RECEIVER_VIDEO = 3;
    private static final int RECEIVED_SHOUT = 4;
    private static final int RECEIVER_TEXT = 5;
    private static final int SENT_IMAGE = 6;
    private static final int SENT_TEXT = 7;
    private static final int SENT_LOCATION = 8;
    private static final int SENT_VIDEO = 9;
    private static final int SENT_SHOUT = 10;

    @NonNull
    private final Picasso mPicasso;

    @Inject
    public ChatsAdapter(@ForActivity @Nonnull Context context, @NonNull Picasso picasso) {
        super(context);
        mPicasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case DATE_ITEM:
                return DateItemHolder.create(layoutInflater.inflate(DateItemHolder.getLayoutRes(), parent, false));
            case RECEIVER_IMAGE:
                return ReceivedImageMessageHolder.create(layoutInflater.inflate(ReceivedImageMessageHolder.getLayoutRes(), parent, false), mPicasso);
            case RECEIVER_LOCATION:
                return ReceivedLocationMessageHolder.create(layoutInflater.inflate(ReceivedLocationMessageHolder.getLayoutRes(), parent, false), mPicasso);
            case RECEIVER_TEXT:
                return ReceivedTextMessageHolder.create(layoutInflater.inflate(ReceivedTextMessageHolder.getLayoutRes(), parent, false), mPicasso);
            case RECEIVED_SHOUT:
                return ReceivedShoutMessageHolder.create(layoutInflater.inflate(ReceivedShoutMessageHolder.getLayoutRes(), parent, false), mPicasso);
            case RECEIVER_VIDEO:
                return ReceivedVideoMessageHolder.create(layoutInflater.inflate(ReceivedVideoMessageHolder.getLayoutRes(), parent, false), mPicasso);
            case SENT_IMAGE:
                return SentImageMessageHolder.create(layoutInflater.inflate(SentImageMessageHolder.getLayoutRes(), parent, false), mPicasso);
            case SENT_LOCATION:
                return SentLocationMessageHolder.create(layoutInflater.inflate(SentLocationMessageHolder.getLayoutRes(), parent, false));
            case SENT_SHOUT:
                return SentShoutMessageHolder.create(layoutInflater.inflate(SentShoutMessageHolder.getLayoutRes(), parent, false), mPicasso);
            case SENT_TEXT:
                return SentTextMessageHolder.create(layoutInflater.inflate(SentTextMessageHolder.getLayoutRes(), parent, false));
            case SENT_VIDEO:
                return SentVideoMessageHolder.create(layoutInflater.inflate(SentVideoMessageHolder.getLayoutRes(), parent, false), mPicasso);
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
        if (baseAdapterItem instanceof ReceivedLocationMessage) {
            return RECEIVER_LOCATION;
        } else if (baseAdapterItem instanceof DateItem) {
            return DATE_ITEM;
        } else if (baseAdapterItem instanceof ReceivedImageMessage) {
            return RECEIVER_IMAGE;
        } else if (baseAdapterItem instanceof ReceivedVideoMessage) {
            return RECEIVER_VIDEO;
        } else if (baseAdapterItem instanceof ReceivedShoutMessage) {
            return RECEIVED_SHOUT;
        } else if (baseAdapterItem instanceof ReceivedTextMessage) {
            return RECEIVER_TEXT;
        } else if (baseAdapterItem instanceof SentImageMessage) {
            return SENT_IMAGE;
        } else if (baseAdapterItem instanceof SentTextMessage) {
            return SENT_TEXT;
        } else if (baseAdapterItem instanceof SentLocationMessage) {
            return SENT_LOCATION;
        } else if (baseAdapterItem instanceof SentVideoMessage) {
            return SENT_VIDEO;
        } else if (baseAdapterItem instanceof SentShoutMessage) {
            return SENT_SHOUT;
        } else {
            throw new RuntimeException();
        }
    }
}
