package com.shoutit.app.android.view.chats.chats_adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.view.chats.message_models.SentProfileMessage;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SentProfileMessageHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    @Nonnull
    private final View mItemView;
    private final Picasso mPicasso;
    private final Context context;

    @Bind(R.id.chats_sent_profile_image_imageview)
    ImageView profileAvatarIv;
    @Bind(R.id.chats_sent_profile_date_textview)
    TextView mDateTv;
    @Bind(R.id.chats_message_profile_name)
    TextView profileNameTv;
    @Bind(R.id.chats_profile_listeners)
    TextView listenersCountTv;

    public SentProfileMessageHolder(@Nonnull View itemView, Picasso picasso, Context context) {
        super(itemView);
        mItemView = itemView;
        mPicasso = picasso;
        this.context = context;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem item) {
        final SentProfileMessage message = (SentProfileMessage) item;

        mPicasso.load(message.getImage())
                .resizeDimen(R.dimen.chat_sent_shout_image_width, R.dimen.chat_sent_shout_image_height)
                .centerCrop()
                .into(profileAvatarIv);

        mDateTv.setText(message.getTime());
        profileNameTv.setText(message.getName());
        listenersCountTv.setText(context.getString(R.string.profile_listeners,
                TextHelper.formatListenersNumber(message.getListenersCount())));

        mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.click();
            }
        });
    }

    public static ViewHolderManager.BaseViewHolder create(@NonNull View view, Picasso picasso, Context context) {
        return new SentProfileMessageHolder(view, picasso, context);
    }

    @LayoutRes
    public static int getLayoutRes() {
        return R.layout.chat_send_profile_message;
    }
}
