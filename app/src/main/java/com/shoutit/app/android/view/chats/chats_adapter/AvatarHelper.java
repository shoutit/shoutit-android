package com.shoutit.app.android.view.chats.chats_adapter;

import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;

import com.google.common.base.Strings;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.BaseProfile;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

public class AvatarHelper {

    public static void setAvatar(boolean isFirst, String avatarUrl, Picasso picasso, ImageView view) {
        if (isFirst && avatarUrl != null) {
            view.setVisibility(View.VISIBLE);
            picasso.load(Strings.emptyToNull(avatarUrl))
                    .fit()
                    .error(R.drawable.default_profile)
                    .placeholder(R.drawable.default_profile)
                    .centerCrop()
                    .into(view);
        } else if (isFirst) {
            view.setVisibility(View.VISIBLE);
            picasso.load(R.drawable.default_profile)
                    .fit()
                    .centerCrop()
                    .into(view);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @DrawableRes
    public static int getPlaceholderId(@Nonnull String profileType) {
        switch (Strings.nullToEmpty(profileType)) {
            case BaseProfile.USER:
                return R.drawable.default_profile;
            case BaseProfile.PAGE:
                return R.drawable.default_page;
            case BaseProfile.TAG:
                return R.drawable.default_tag;
            default:
                return R.drawable.default_profile;
        }
    }

    @DrawableRes
    public static int getCirclePlaceholderId(@Nonnull String type) {
        switch (Strings.nullToEmpty(type)) {
            case BaseProfile.USER:
                return R.drawable.default_profile_circle;
            case BaseProfile.PAGE:
                return R.drawable.default_page_circle;
            default:
                return R.drawable.default_profile_circle;
        }
    }
}
