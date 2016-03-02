package com.shoutit.app.android.view.profile.userprofile;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.view.profile.ProfileAdapter;
import com.shoutit.app.android.viewholders.ProfileSectionViewHolder;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserProfileAdapter extends ProfileAdapter {

    public UserProfileAdapter(@Nonnull @ForActivity Context context, @Nonnull Picasso picasso) {
        super(context, picasso);
    }

    @Override
    protected ViewHolderManager.BaseViewHolder getThreeIconsViewHolder(ViewGroup parent) {
        return new UserProfileThreeIconsViewHolder(layoutInflater.inflate(R.layout.profile_user_three_icons_items, parent, false));
    }

    @Override
    protected ViewHolderManager.BaseViewHolder getSectionViewHolder(ViewGroup parent) {
        return new ProfileSectionViewHolder(layoutInflater.inflate(R.layout.profile_section_item, parent, false), context, picasso);
    }

    @Override
    protected ViewHolderManager.BaseViewHolder getUserNameViewHolder(ViewGroup parent) {
        return new UserProfileUserNameViewHolder(layoutInflater.inflate(R.layout.user_profile_name_item, parent, false));
    }

    class UserProfileUserNameViewHolder extends ViewHolderManager.BaseViewHolder<UserProfilePresenter.UserNameAdapterItem> {

        @Bind(R.id.profile_user_name)
        TextView userName;
        @Bind(R.id.profile_user_nick)
        TextView userNick;
        @Bind(R.id.profile_user_listening_to_you)
        TextView listeningToYouTextView;

        private UserProfilePresenter.UserNameAdapterItem item;

        public UserProfileUserNameViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull UserProfilePresenter.UserNameAdapterItem item) {
            this.item = item;
            final User user = item.getUser();
            userName.setText(user.getName());
            userNick.setText(user.getUsername());
            listeningToYouTextView.setVisibility(user.isListener() ? View.VISIBLE : View.GONE);
        }

        @OnClick(R.id.profile_menu_more_iv)
        public void onMenuMoreIconClick() {
            item.onMoreMenuOptionClicked();
        }
    }

    class UserProfileThreeIconsViewHolder extends ViewHolderManager.BaseViewHolder<UserProfilePresenter.OtherUserThreeIconsAdapterItem> {
        @Bind(R.id.profile_first_icon_value_tv)
        TextView firstIconValue;
        @Bind(R.id.profile_listen_icon_iv)
        ImageView listenImageView;
        @Bind(R.id.profile_listen_tv)
        TextView listenTextView;

        private UserProfilePresenter.OtherUserThreeIconsAdapterItem item;

        public UserProfileThreeIconsViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull UserProfilePresenter.OtherUserThreeIconsAdapterItem item) {
            this.item = item;
            final User user = item.getUser();

            firstIconValue.setText(TextHelper.formatListenersNumber(user.getListenersCount()));
            setListeningIcon(user.isListening());
        }

        private void setListeningIcon(boolean isListening) {
            listenTextView.setText(isListening ?
                    R.string.profile_stop_listening_label : R.string.profile_listen_label);
            listenImageView.setImageDrawable(context.getResources().getDrawable(isListening ?
                    R.drawable.ic_listening_on : R.drawable.ic_listening_off));
        }

        @OnClick(R.id.profile_listen_action_container)
        public void onListenActionClicked() {
            setListeningIcon(!item.getUser().isListening());
            item.onListenActionClicked();
        }

        @OnClick(R.id.profile_second_icon_text_tv)
        public void onChatActionClicked() {
            item.onChatActionClicked();
        }
    }
}
