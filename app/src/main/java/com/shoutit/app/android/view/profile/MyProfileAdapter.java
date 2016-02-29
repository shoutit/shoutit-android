package com.shoutit.app.android.view.profile;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.viewholders.ProfilePageSectionViewHolder;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MyProfileAdapter extends ProfileAdapter {

    @Nonnull
    private final Picasso picasso;

    @Inject
    public MyProfileAdapter(@Nonnull @ForActivity Context context, @Nonnull Picasso picasso) {
        super(context, picasso);
        this.picasso = picasso;
    }

    @Override
    protected ViewHolderManager.BaseViewHolder getThreeIconsViewHolder(ViewGroup parent) {
        return new MyProfileThreeIconsViewHolder(layoutInflater.inflate(R.layout.profile_three_icons_item, parent, false));
    }

    @Override
    protected ViewHolderManager.BaseViewHolder getSectionViewHolder(ViewGroup parent) {
        return new ProfilePageSectionViewHolder(layoutInflater.inflate(R.layout.profile_section_item, parent, false), context, picasso);
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

    class MyProfileThreeIconsViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.ThreeIconsAdapterItem> {
        @Bind(R.id.profile_first_icon_value_tv)
        TextView firstIconValue;
        @Bind(R.id.profile_second_icon_value_tv)
        TextView secondIconValue;
        @Bind(R.id.profile_second_icon_text_tv)
        TextView secondIconText;
        @Bind(R.id.profile_third_icon_value_tv)
        TextView thirdIconValue;
        @Bind(R.id.profile_third_icon_text_tv)
        TextView thirdIconText;

        public MyProfileThreeIconsViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull ProfileAdapterItems.ThreeIconsAdapterItem item) {
            final User user = item.getUser();

            firstIconValue.setText(TextHelper.formatListenersNumber(user.getListenersCount()));
            secondIconValue.setText(TextHelper.formatListenersNumber(user.getListeningCount().getProfileListening()));
            ImageHelper.setStartCompoundRelativeDrawable(secondIconValue, R.drawable.ic_listeners);
            secondIconText.setText(context.getString(R.string.profile_listening_label));
            thirdIconValue.setText(TextHelper.formatListenersNumber(user.getListeningCount().getTags()));
            ImageHelper.setStartCompoundRelativeDrawable(thirdIconValue, R.drawable.ic_tags);
            thirdIconText.setText(context.getString(R.string.profile_interests_label));
        }
    }


}
