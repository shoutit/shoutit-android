package com.shoutit.app.android.view.profile.page;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.view.chats.chats_adapter.AvatarHelper;
import com.shoutit.app.android.view.profile.BaseProfileAdapterItems;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileSectionViewHolder extends ViewHolderManager.BaseViewHolder<BaseProfileAdapterItems.BaseProfileSectionItem<ProfileType>> {
    private final Context context;
    private final Picasso picasso;
    @Bind(R.id.profile_section_iv)
    ImageView avatarImageView;
    @Bind(R.id.profile_section_name_tv)
    TextView nameTextView;
    @Bind(R.id.profile_section_listeners_tv)
    TextView listenerTextView;
    @Bind(R.id.profile_section_listening_iv)
    ImageView listeningImageView;
    @Bind(R.id.profile_section_container)
    View container;

    private BaseProfileAdapterItems.BaseProfileSectionItem<ProfileType> item;
    private final Target target;

    public ProfileSectionViewHolder(View itemView, Context context, Picasso picasso) {
        super(itemView);
        this.context = context;
        this.picasso = picasso;
        ButterKnife.bind(this, itemView);

        target = PicassoHelper.getRoundedBitmapTarget(context, avatarImageView,
                context.getResources().getDimensionPixelSize(R.dimen.profile_section_avatar_corners));
    }

    @Override
    public void bind(@Nonnull BaseProfileAdapterItems.BaseProfileSectionItem<ProfileType> item) {
        this.item = item;
        final ProfileType sectionItem = item.getSectionItem();

        if (item.isOnlyItemInSection()) {
            container.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_page_only_item));
        } else if (item.isFirstItem()) {
            container.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_page_top));
        } else if (item.isLastItem()) {
            container.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_page_bottom));
        } else {
            container.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_page_middle));
        }

        picasso.load(sectionItem.getImage())
                .placeholder(AvatarHelper.getPlaceholderId(sectionItem.getType()))
                .into(target);

        nameTextView.setText(sectionItem.getName());
        listenerTextView.setText(context.getString(R.string.profile_listeners,
                TextHelper.formatListenersNumber(sectionItem.getListenersCount())));
        setListeningIcon(sectionItem.isListening());
    }

    private void setListeningIcon(boolean isListening) {
        if (item.isSectionItemProfileMyProfile()) {
            listeningImageView.setVisibility(View.GONE);
        } else {
            listeningImageView.setVisibility(View.VISIBLE);
            listeningImageView.setImageDrawable(context.getResources().getDrawable(
                    isListening ? R.drawable.ic_listening_on : R.drawable.ic_listening_off));
        }
    }

    @OnClick(R.id.profile_section_listening_iv)
    public void onListenClicked() {
        if (item.isUserLoggedIn()) {
            setListeningIcon(!item.getSectionItem().isListening());
            item.onItemListen();
        } else {
            item.onActionOnlyForLoggedInUser();
        }
    }

    @OnClick(R.id.profile_section_container)
    public void onSectionItemSelected() {
        item.onSectionItemSelected();
    }
}
