package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Optional;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.viewholders.HeaderViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class ProfileAdapter extends BaseAdapter {

    public static final int VIEW_TYPE_USER_NAME = 1;
    public static final int VIEW_TYPE_THREE_ICONS = 3;
    public static final int VIEW_TYPE_USER_INFO = 4;
    public static final int VIEW_TYPE_USER_PAGES_OR_ADMINS = 5;
    public static final int VIEW_TYPE_SHOUT = 6;
    public static final int VIEW_TYPE_SEE_ALL_SHOUTS = 7;
    public static final int VIEW_TYPE_HEADER = 8;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Nonnull
    protected final Picasso picasso;

    public ProfileAdapter(@Nonnull @ForActivity Context context, @Nonnull Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    class UserInfoViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.UserInfoAdapterItem> {
        @Bind(R.id.profile_bio_tv)
        TextView bioTextView;
        @Bind(R.id.profile_website_tv)
        TextView websiteTextView;
        @Bind(R.id.profile_date_joined_tv)
        TextView dateJoinedTextView;
        @Bind(R.id.profile_country_tv)
        TextView countryTextView;
        @Bind(R.id.profile_country_iv)
        ImageView countryFlagImageView;

        private final Target flagTarget;

        public UserInfoViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            flagTarget = PicassoHelper.getRoundedBitmapTarget(context, countryFlagImageView);
        }

        @Override
        public void bind(@Nonnull ProfileAdapterItems.UserInfoAdapterItem item) {
            final User user = item.getUser();
            bioTextView.setText(user.getBio());
            websiteTextView.setText(user.getWebUrl());

            if (user.getDateJoinedInMillis() > 0) {
                dateJoinedTextView.setText(context.getString(
                        R.string.profile_joined_date,
                        dateFormat.format(new Date(user.getDateJoinedInMillis()))));
            }

            if (user.getLocation() != null) {
                countryTextView.setText(user.getLocation().getCity());
                final Optional<Integer> countryResId = ResourcesHelper.getCountryResId(context, user.getLocation());
                if (countryResId.isPresent()) {
                    picasso.load(countryResId.get())
                            .into(flagTarget);
                }
            }
        }
    }

    class SeeAllButtonViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.SeeAllUserShoutsAdapterItem> implements View.OnClickListener {

        @Bind(R.id.button_gray_btn)
        Button seeAllButton;

        private ProfileAdapterItems.SeeAllUserShoutsAdapterItem item;

        public SeeAllButtonViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull ProfileAdapterItems.SeeAllUserShoutsAdapterItem item) {
            this.item = item;
            seeAllButton.setText(context.getString(R.string.profile_see_all_shouts).toUpperCase());
        }

        @Override
        public void onClick(View v) {
            item.onSeeAllShouts();
        }
    }

    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_USER_NAME:
                return getUserNameViewHolder(parent);
            case  VIEW_TYPE_THREE_ICONS:
                return getThreeIconsViewHolder(parent);
            case VIEW_TYPE_USER_INFO:
                return new UserInfoViewHolder(layoutInflater.inflate(R.layout.profile_info_item, parent, false));
            case VIEW_TYPE_USER_PAGES_OR_ADMINS:
                return getSectionViewHolder(parent);
            case VIEW_TYPE_SHOUT:
                return new ShoutGridViewHolder(layoutInflater.inflate(R.layout.shout_item_grid, parent, false), picasso, context);
            case VIEW_TYPE_SEE_ALL_SHOUTS:
                return new SeeAllButtonViewHolder(layoutInflater.inflate(R.layout.button_gray_with_stroke, parent, false));
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(layoutInflater.inflate(R.layout.base_header_item, parent, false));
            default:
                throw new RuntimeException("Unknown adapter view type");
        }
    }

    protected abstract ViewHolderManager.BaseViewHolder getThreeIconsViewHolder(ViewGroup parent);

    protected abstract ViewHolderManager.BaseViewHolder getSectionViewHolder(ViewGroup parent);

    protected abstract ViewHolderManager.BaseViewHolder getUserNameViewHolder(ViewGroup parent);

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof ProfileAdapterItems.NameAdapterItem) {
            return VIEW_TYPE_USER_NAME;
        } else if (item instanceof ProfileAdapterItems.UserInfoAdapterItem) {
            return VIEW_TYPE_USER_INFO;
        } else if (item instanceof ProfileAdapterItems.ThreeIconsAdapterItem) {
            return VIEW_TYPE_THREE_ICONS;
        } else if (item instanceof ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (item instanceof ProfileAdapterItems.ProfileSectionAdapterItem) {
            return VIEW_TYPE_USER_PAGES_OR_ADMINS;
        } else if (item instanceof ProfileAdapterItems.SeeAllUserShoutsAdapterItem) {
            return VIEW_TYPE_SEE_ALL_SHOUTS;
        } else if (item instanceof HeaderAdapterItem) {
            return VIEW_TYPE_HEADER;
        } else {
            throw new RuntimeException("Unknown adapter view type: " + item.getClass().getSimpleName());
        }
    }
}
