package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PicassoHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyProfileAdapter extends ProfileAdapter {

    @Nonnull
    private final Picasso picasso;

    @Inject
    public MyProfileAdapter(@Nonnull @ForActivity Context context, @Nonnull Picasso picasso) {
        super(context, picasso);
        this.picasso = picasso;
    }

    @Override
    protected ViewHolderManager.BaseViewHolder getProfileSectionViewHolder(ViewGroup parent) {
        return new PageProfileSectionViewHolder(layoutInflater.inflate(R.layout.profile_section_item, parent, false));
    }

    @Override
    protected ViewHolderManager.BaseViewHolder getUserNameViewHolder(ViewGroup parent) {
        return new MyProfileUserNameViewHolder(layoutInflater.inflate(R.layout.my_profile_name_item, parent, false));
    }

    class MyProfileUserNameViewHolder extends ViewHolderManager.BaseViewHolder<MyProfilePresenter.MyUserNameAdapterItem> {

        @Bind(R.id.profile_user_name)
        TextView userName;
        @Bind(R.id.profile_user_nick)
        TextView userNick;
        @Bind(R.id.profile_fragment_notification)
        ImageView notificationIcon;
        @Bind(R.id.profile_fragment_edit_profile)
        ImageView editProfileIcon;

        public MyProfileUserNameViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull MyProfilePresenter.MyUserNameAdapterItem item) {
            final User user = item.getUser();
            userName.setText(user.getName());
            userNick.setText(user.getUsername());
        }
    }

    class PageProfileSectionViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdpaterItems.ProfileSectionAdapterItem<Page>> {
        @Bind(R.id.profile_section_iv)
        ImageView avatarImageView;
        @Bind(R.id.profile_section_name_tv)
        TextView nameTextView;
        @Bind(R.id.profile_section_listeners_tv)
        TextView listenerTextView;
        @Bind(R.id.profile_section_listening_iv)
        ImageView listeningImageView;

        private ProfileAdpaterItems.ProfileSectionAdapterItem<Page> item;
        private final Target target;

        public PageProfileSectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            target = PicassoHelper.getRoundedBitmapTarget(context, avatarImageView,
                    context.getResources().getDimensionPixelSize(R.dimen.profile_section_avatar_corners));
        }

        @Override
        public void bind(@Nonnull ProfileAdpaterItems.ProfileSectionAdapterItem<Page> item) {
            this.item = item;
            final Page page = item.getSectionItem();

            picasso.load(page.getImage())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(target);

            nameTextView.setText(page.getName());
            listenerTextView.setText(String.valueOf(page.getListenersCount()));
            listeningImageView.setImageDrawable(context.getResources().getDrawable(
                    page.isListening() ? R.drawable.ic_listening_on : R.drawable.ic_listening_off));
        }

        @OnClick(R.id.profile_section_iv)
        public void onProfileSelected() {
            item.onItemSelected();
        }

        @OnClick(R.id.profile_section_listening_iv)
        public void onListenClicked() {
            item.onListenPage();
        }
    }
}
