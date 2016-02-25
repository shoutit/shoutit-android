package com.shoutit.app.android.view.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.detector.ChangesDetector;
import com.appunite.detector.SimpleDetector;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.functions.Action1;

public class ProfileAdapter extends RecyclerView.Adapter<ViewHolderManager.BaseViewHolder>
        implements Action1<List<BaseAdapterItem>>, ChangesDetector.ChangesAdapter {

    public static final int VIEW_TYPE_USER = 1;
    public static final int VIEW_TYPE_SHOUT = 2;
    public static final int VIEW_TYPE_SHOW_MORE = 3;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Nonnull
    private final ChangesDetector<BaseAdapterItem, BaseAdapterItem> changesDetector;
    @Nonnull
    private final LayoutInflater layoutInflater;
    @Nonnull
    private final Activity activity;
    @Nonnull
    private List<BaseAdapterItem> items = ImmutableList.of();

    public ProfileAdapter(@Nonnull Activity activity) {
        this.activity = activity;
        changesDetector = new ChangesDetector<>(new SimpleDetector<BaseAdapterItem>());
        layoutInflater = LayoutInflater.from(this.activity);
    }

    @Override
    public void call(List<BaseAdapterItem> items) {
        this.items = items;
        changesDetector.newData(this, items, false);
    }

    class UserViewHolder extends ViewHolderManager.BaseViewHolder<ProfilePresenter.UserAdapterItem> {
        @Bind(R.id.profile_user_name)
        TextView userNameTextView;
        @Bind(R.id.profile_user_nick)
        TextView userNickTextView;
        @Bind(R.id.profile_listeners_tv)
        TextView listenersTextView;
        @Bind(R.id.profile_listening_tv)
        TextView listeningTextView;
        @Bind(R.id.profile_interests_tv)
        TextView interestsTextView;
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

        public UserViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull ProfilePresenter.UserAdapterItem item) {
            final User user = item.getUser();
            userNameTextView.setText(user.getFirstName() + " " + user.getLastName());
            userNickTextView.setText(user.getUsername());
            listenersTextView.setText(String.valueOf(user.getListenersCount()));
            listeningTextView.setText(String.valueOf(user.getUsersListeningCount()));
            interestsTextView.setText(String.valueOf(user.getTagsListeningCount()));
            bioTextView.setText(user.getBio());
            websiteTextView.setText(user.getWebUrl());
            if (user.getDateJoined() > 0) {
                dateJoinedTextView.setText(activity.getString(
                        R.string.profile_joined_date,
                        dateFormat.format(new Date(user.getDateJoined() * 1000)))
                );
            }
            countryTextView.setText(user.getLocation().getCity());
            final String countryCode = user.getLocation().getCountry().toLowerCase();
            if (!TextUtils.isEmpty(countryCode)) {
                final int flagResId = activity.getResources().getIdentifier(countryCode,
                        "drawable", activity.getPackageName());
                if (flagResId != 0) {
                    Glide.with(activity)
                            .load(flagResId)
                            .asBitmap()
                            .centerCrop()
                            .into(new BitmapImageViewTarget(countryFlagImageView) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    final RoundedBitmapDrawable roundedBitmap =
                                            RoundedBitmapDrawableFactory.create(activity.getResources(), resource);
                                    roundedBitmap.setCircular(true);
                                    countryFlagImageView.setImageDrawable(roundedBitmap);
                                }
                            });
                }
            }
        }

        @OnClick(R.id.profile_fragment_edit_profile)
        public void onEditProfileClick() {
            activity.startActivityForResult(
                    new Intent(activity, EditProfileActivity.class), ProfileActivity.RC_EDIT_PROFILE
            );
        }

        @OnClick(R.id.profile_fragment_notification)
        public void onNotificationClick() {
            Toast.makeText(activity, "Not implemented yet", Toast.LENGTH_LONG).show();
        }
    }

    class ShoutViewHolder extends ViewHolderManager.BaseViewHolder<ProfilePresenter.ShoutAdapterItem>
            implements View.OnClickListener {
        @Bind(R.id.profile_fragment_card_title_tv)
        TextView titleTextView;
        @Bind(R.id.profile_fragment_card_name_tv)
        TextView nameTextView;
        @Bind(R.id.profile_fragment_card_price_tv)
        TextView priceTextView;
        @Bind(R.id.profile_fragment_card_image_view)
        ImageView shoutImageView;

        public ShoutViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull ProfilePresenter.ShoutAdapterItem item) {
            final Shout shout = item.getShout();
            titleTextView.setText(shout.getTitle());
            nameTextView.setText(shout.getUser().getFirstName() + " " + shout.getUser().getLastName());
            priceTextView.setText(shout.getPrice() + " " + shout.getCurrency());

            // TODO add Glide to module
            Glide.with(activity)
                    .load(Utils.getThumbnailUri(shout, ShoutitImageType.LARGE))
                    .placeholder(R.drawable.image_pending)
                    .error(R.drawable.no_pic)
                    .centerCrop()
                    .into(shoutImageView);
        }

        @Override
        public void onClick(View v) {
            final Shout shout = ((ProfilePresenter.ShoutAdapterItem) items.get(getAdapterPosition())).getShout();
            Intent intent = new Intent(activity, ShoutDetailActivity.class);
            intent.putExtra(ShoutDetailActivity.EXTRA_SHOUT, shout);
            activity.startActivity(intent);
        }
    }

    class ShowMoreButtonViewHolder extends ViewHolderManager.BaseViewHolder<ProfilePresenter.ShowMoreShoutsAdapterItem>
            implements View.OnClickListener {

        private Observer<Boolean> showMoreShoutsObserver;

        public ShowMoreButtonViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull ProfilePresenter.ShowMoreShoutsAdapterItem item) {
            showMoreShoutsObserver = item.getShowMoreShoutsObserver();
        }

        @Override
        public void onClick(View v) {
            showMoreShoutsObserver.onNext(true);
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_USER:
                return new UserViewHolder(layoutInflater.inflate(R.layout.profile_header, parent, false));
            case VIEW_TYPE_SHOUT:
                return new ShoutViewHolder(layoutInflater.inflate(R.layout.profile_shout_card, parent, false));
            case VIEW_TYPE_SHOW_MORE:
                return new ShowMoreButtonViewHolder(layoutInflater.inflate(R.layout.profile_shouts_button_item, parent, false));
            default:
                throw new RuntimeException("Unknown adapter view type");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof ProfilePresenter.UserAdapterItem) {
            return VIEW_TYPE_USER;
        } else if (item instanceof ProfilePresenter.ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (item instanceof ProfilePresenter.ShowMoreShoutsAdapterItem) {
            return VIEW_TYPE_SHOW_MORE;
        } else {
            throw new RuntimeException("Unknown adapter view type");
        }
    }
}
