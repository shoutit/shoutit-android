package com.shoutit.app.android.view.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Optional;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.ImageHelper;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileViewHolders {

    public static class UserInfoViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.UserInfoAdapterItem> {
        @SuppressLint("SimpleDateFormat")
        private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        @Bind(R.id.profile_bio_tv)
        TextView bioTextView;
        @Bind(R.id.profile_bio_iv)
        ImageView bioImageView;
        @Bind(R.id.profile_website_tv)
        TextView websiteTextView;
        @Bind(R.id.profile_date_joined_tv)
        TextView dateJoinedTextView;
        @Bind(R.id.profile_country_container)
        View locationContainer;
        @Bind(R.id.profile_country_tv)
        TextView countryTextView;
        @Bind(R.id.profile_country_iv)
        ImageView countryFlagImageView;
        @Bind(R.id.profile_bio_container)
        View bioContainer;
        @Bind(R.id.profile_joined_container)
        View joinedContainer;
        @Bind(R.id.profile_website_container)
        View webContainer;

        private final Target flagTarget;
        private final Context context;
        private final Picasso picasso;
        private ProfileAdapterItems.UserInfoAdapterItem item;

        public UserInfoViewHolder(@Nonnull View itemView, Context context, Picasso picasso) {
            super(itemView);
            this.context = context;
            this.picasso = picasso;
            ButterKnife.bind(this, itemView);
            flagTarget = PicassoHelper.getRoundedBitmapTarget(context, countryFlagImageView);
        }

        @Override
        public void bind(@Nonnull ProfileAdapterItems.UserInfoAdapterItem item) {
            this.item = item;
            final User user = item.getUser();

            bioTextView.setText(item.getBioText());
            bioImageView.setImageDrawable(context.getResources().getDrawable(item.getBioResId()));
            bioContainer.setVisibility(TextUtils.isEmpty(item.getBioText()) ? View.GONE : View.VISIBLE);

            final String website = user.getWebsite();
            websiteTextView.setText(website);
            webContainer.setVisibility(TextUtils.isEmpty(website) ? View.GONE : View.VISIBLE);

            if (user.getDateJoinedInMillis() > 0) {
                joinedContainer.setVisibility(View.VISIBLE);
                dateJoinedTextView.setText(context.getString(
                        R.string.profile_joined_date,
                        dateFormat.format(new Date(user.getDateJoinedInMillis()))));
            } else {
                joinedContainer.setVisibility(View.GONE);
            }

            if (user.getLocation() != null && !TextUtils.isEmpty(user.getLocation().getCountry())) {
                locationContainer.setVisibility(View.VISIBLE);
                countryTextView.setText(user.getLocation().getCity());
                final Optional<Integer> countryResId = ResourcesHelper.getCountryResId(context, user.getLocation());
                if (countryResId.isPresent()) {
                    picasso.load(countryResId.get())
                            .into(flagTarget);
                }
            } else {
                locationContainer.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.profile_website_container)
        public void onWebClicked() {
            item.onWebsiteClicked();
        }
    }

    public static class SeeAllButtonViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.SeeAllUserShoutsAdapterItem> implements View.OnClickListener {

        private final Context context;
        @Bind(R.id.button_gray_btn)
        Button seeAllButton;

        private ProfileAdapterItems.SeeAllUserShoutsAdapterItem item;

        public SeeAllButtonViewHolder(@Nonnull View itemView, Context context) {
            super(itemView);
            this.context = context;
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

    public static class MyProfileUserNameViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.MyUserNameAdapterItem> {

        @Bind(R.id.profile_user_name)
        TextView userName;
        @Bind(R.id.profile_user_nick)
        TextView userNick;

        private ProfileAdapterItems.MyUserNameAdapterItem item;

        public MyProfileUserNameViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull ProfileAdapterItems.MyUserNameAdapterItem item) {
            this.item = item;
            final User user = item.getUser();
            userName.setText(user.getName());
            userNick.setText(user.getUsername());
        }

        @OnClick(R.id.profile_notification_iv)
        public void onNotificationClick() {
            item.onShowNotificationClicked();
        }

        @OnClick(R.id.profile_edit_profile_iv)
        public void onEditProfileClick() {
            item.onEditProfileClicked();
        }
    }

    public static class MyProfileThreeIconsViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.ThreeIconsAdapterItem> {
        private final Context context;
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

        public MyProfileThreeIconsViewHolder(@Nonnull View itemView, Context context) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull ProfileAdapterItems.ThreeIconsAdapterItem item) {
            final User user = item.getUser();

            firstIconValue.setText(TextHelper.formatListenersNumber(user.getListenersCount()));
            secondIconValue.setText(TextHelper.formatListenersNumber(user.getListeningCount().getProfileListening()));
            ImageHelper.setStartCompoundRelativeDrawable(secondIconValue, R.drawable.ic_listening);
            secondIconText.setText(context.getString(R.string.profile_listening_label));
            thirdIconValue.setText(TextHelper.formatListenersNumber(user.getListeningCount().getTags()));
            ImageHelper.setStartCompoundRelativeDrawable(thirdIconValue, R.drawable.ic_tags);
            thirdIconText.setText(context.getString(R.string.profile_interests_label));
        }
    }

    public static class UserProfileUserNameViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.UserNameAdapterItem> {

        @Bind(R.id.profile_user_name)
        TextView userName;
        @Bind(R.id.profile_user_nick)
        TextView userNick;
        @Bind(R.id.profile_user_listening_to_you)
        TextView listeningToYouTextView;

        private ProfileAdapterItems.UserNameAdapterItem item;

        public UserProfileUserNameViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull ProfileAdapterItems.UserNameAdapterItem item) {
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

    public static class UserProfileThreeIconsViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.UserThreeIconsAdapterItem> {
        @Bind(R.id.profile_first_icon_value_tv)
        TextView firstIconValue;
        @Bind(R.id.profile_listen_icon_iv)
        ImageView listenImageView;
        @Bind(R.id.profile_listen_tv)
        TextView listenTextView;

        private ProfileAdapterItems.UserThreeIconsAdapterItem item;
        private final Context context;

        public UserProfileThreeIconsViewHolder(@Nonnull View itemView, Context context) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull ProfileAdapterItems.UserThreeIconsAdapterItem item) {
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
            if (item.isUserLoggedIn()) {
                setListeningIcon(!item.getUser().isListening());
                item.onListenActionClicked();
            } else {
                item.onActionOnlyForLoggedInUser();
            }
        }

        @OnClick(R.id.profile_second_icon_text_tv)
        public void onChatActionClicked() {
            item.onChatActionClicked();
        }
    }
}
