package com.shoutit.app.android.view.chats.chats_adapter;

import android.view.View;
import android.widget.ImageView;

import com.google.common.base.Strings;
import com.shoutit.app.android.R;
import com.squareup.picasso.Picasso;

public class AvatarHelper {

    public static void setAvatar(boolean isFirst, String avatarUrl, Picasso picasso, ImageView view) {
        if (isFirst && avatarUrl != null) {
            view.setVisibility(View.VISIBLE);
            picasso.load(Strings.emptyToNull(avatarUrl))
                    .fit()
                    .error(R.drawable.ic_rect_avatar_placeholder)
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .centerCrop()
                    .into(view);
        } else if (isFirst) {
            view.setVisibility(View.VISIBLE);
            picasso.load(R.drawable.ic_rect_avatar_placeholder)
                    .fit()
                    .centerCrop()
                    .into(view);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }
}
