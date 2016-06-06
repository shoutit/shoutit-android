package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.listenings.ProfilesListAdapter;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

public class FacebookFriendsAdapter extends ProfilesListAdapter {

    private static final int VIEW_TYPE_FACEBOOK_INVITE = 10;

    public FacebookFriendsAdapter(@Nonnull @ForActivity Context context,
                                  @Nonnull Picasso picasso) {
        super(context, picasso);
    }

    public class FacebookFriendsViewHolder extends ViewHolderManager.BaseViewHolder<FacebookFriendsPresenter.FacebookInviteFriendsAdapterItem> implements View.OnClickListener {

        private FacebookFriendsPresenter.FacebookInviteFriendsAdapterItem item;

        public FacebookFriendsViewHolder(@Nonnull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull FacebookFriendsPresenter.FacebookInviteFriendsAdapterItem adapterItem) {
            item = adapterItem;
        }

        @Override
        public void onClick(View v) {
            item.onOpenInviteClicked();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (VIEW_TYPE_FACEBOOK_INVITE == viewType) {
            return new FacebookFriendsViewHolder(layoutInflater.inflate(R.layout.facebook_invite_item, parent, false));
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem baseAdapterItem = items.get(position);

        if (baseAdapterItem instanceof FacebookFriendsPresenter.FacebookInviteFriendsAdapterItem) {
            return VIEW_TYPE_FACEBOOK_INVITE;
        } else {
            return super.getItemViewType(position);
        }
    }
}
