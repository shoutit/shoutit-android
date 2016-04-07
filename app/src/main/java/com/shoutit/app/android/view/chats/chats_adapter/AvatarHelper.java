package com.shoutit.app.android.view.chats.chats_adapter;

import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class AvatarHelper {

    public static void setAvatar(boolean isFirst, String avatarUrl, Picasso picasso, ImageView view) {
        if (isFirst) {
            view.setVisibility(View.VISIBLE);
            picasso.load(avatarUrl)
                    .fit()
                    .centerCrop()
                    .into(view);
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
