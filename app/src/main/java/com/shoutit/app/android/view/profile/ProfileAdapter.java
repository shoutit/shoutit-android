package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.viewholders.HeaderViewHolder;
import com.shoutit.app.android.viewholders.ProfileSectionViewHolder;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class ProfileAdapter extends BaseAdapter {

    public static final int VIEW_TYPE_USER_NAME = 1;
    public static final int VIEW_TYPE_MY_PROFILE_USER_NAME = 2;
    public static final int VIEW_TYPE_THREE_ICONS = 3;
    public static final int VIEW_TYPE_MY_PROFILE_THREE_ICONS = 4;
    public static final int VIEW_TYPE_USER_INFO = 5;
    public static final int VIEW_TYPE_USER_PAGES_OR_ADMINS = 6;
    public static final int VIEW_TYPE_SHOUT = 7;
    public static final int VIEW_TYPE_SEE_ALL_SHOUTS = 8;
    public static final int VIEW_TYPE_HEADER = 9;
    public static final int VIEW_TYPE_TAG_INFO = 10;

    @Nonnull
    protected final Picasso picasso;

    @Inject
    public ProfileAdapter(@Nonnull @ForActivity Context context, @Nonnull Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_MY_PROFILE_USER_NAME:
                return new ProfileViewHolders.MyProfileUserNameViewHolder(layoutInflater.inflate(R.layout.my_profile_name_item, parent, false));
            case VIEW_TYPE_USER_NAME:
                return new ProfileViewHolders.UserProfileUserNameViewHolder(layoutInflater.inflate(R.layout.user_profile_name_item, parent, false));
            case VIEW_TYPE_MY_PROFILE_THREE_ICONS:
                return new ProfileViewHolders.MyProfileThreeIconsViewHolder(layoutInflater.inflate(R.layout.profile_mine_three_icons_item, parent, false), context);
            case  VIEW_TYPE_THREE_ICONS:
                return new ProfileViewHolders.UserProfileThreeIconsViewHolder(layoutInflater.inflate(R.layout.profile_user_three_icons_items, parent, false), context);
            case VIEW_TYPE_USER_INFO:
                return new ProfileViewHolders.UserInfoViewHolder(layoutInflater.inflate(R.layout.profile_info_item, parent, false), context, picasso);
            case VIEW_TYPE_USER_PAGES_OR_ADMINS:
                return new ProfileSectionViewHolder(layoutInflater.inflate(R.layout.profile_section_item, parent, false), context, picasso);
            case VIEW_TYPE_SHOUT:
                return new ShoutGridViewHolder(layoutInflater.inflate(ShoutGridViewHolder.getLayoutRes(), parent, false), picasso);
            case VIEW_TYPE_SEE_ALL_SHOUTS:
                return new ProfileViewHolders.SeeAllButtonViewHolder(layoutInflater.inflate(R.layout.button_gray_with_stroke, parent, false), context);
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(layoutInflater.inflate(R.layout.base_header_item, parent, false));
            case VIEW_TYPE_TAG_INFO:
                return new ProfileViewHolders.TagViewHolder(layoutInflater.inflate(R.layout.profile_tag_info_item, parent, false), context);
            default:
                throw new RuntimeException("Unknown adapter view type");
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof ProfileAdapterItems.MyUserNameAdapterItem) {
            return VIEW_TYPE_MY_PROFILE_USER_NAME;
        } else if (item instanceof ProfileAdapterItems.UserNameAdapterItem) {
            return VIEW_TYPE_USER_NAME;
        } else if (item instanceof ProfileAdapterItems.UserInfoAdapterItem) {
            return VIEW_TYPE_USER_INFO;
        } else if (item instanceof ProfileAdapterItems.UserThreeIconsAdapterItem) {
            return VIEW_TYPE_THREE_ICONS;
        } else if (item instanceof ProfileAdapterItems.MyProfileThreeIconsAdapterItem) {
            return VIEW_TYPE_MY_PROFILE_THREE_ICONS;
        } else if (item instanceof ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (item instanceof ProfileAdapterItems.BaseProfileSectionItem) {
            return VIEW_TYPE_USER_PAGES_OR_ADMINS;
        } else if (item instanceof ProfileAdapterItems.SeeAllUserShoutsAdapterItem) {
            return VIEW_TYPE_SEE_ALL_SHOUTS;
        } else if (item instanceof HeaderAdapterItem) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof ProfileAdapterItems.TagInfoAdapterItem) {
            return VIEW_TYPE_TAG_INFO;
        } else {
            throw new RuntimeException("Unknown adapter view type: " + item.getClass().getSimpleName());
        }
    }
}
