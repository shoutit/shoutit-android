package com.shoutit.app.android.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.view.profile.ProfileAdapterItems;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfilePageSectionViewHolder extends ViewHolderManager.BaseViewHolder<ProfileAdapterItems.ProfileSectionAdapterItem<Page>> {
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

    private ProfileAdapterItems.ProfileSectionAdapterItem<Page> item;
    private final Target target;

    public ProfilePageSectionViewHolder(View itemView, Context context, Picasso picasso) {
        super(itemView);
        this.context = context;
        this.picasso = picasso;
        ButterKnife.bind(this, itemView);

        target = PicassoHelper.getRoundedBitmapTarget(context, avatarImageView,
                context.getResources().getDimensionPixelSize(R.dimen.profile_section_avatar_corners));
    }

    @Override
    public void bind(@Nonnull ProfileAdapterItems.ProfileSectionAdapterItem<Page> item) {
        this.item = item;
        final Page page = item.getSectionItem();

        if (item.isFirstItem()) {
            container.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_page_top));
        } else if (item.isLastItem()) {
            container.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_page_bottom));
        } else {
            container.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        }

        picasso.load(page.getImage())
                .placeholder(R.drawable.ic_avatar_placeholder)
                .into(target);

        nameTextView.setText(page.getName());
        listenerTextView.setText(context.getString(R.string.profile_listeners,
                TextHelper.formatListenersNumber(page.getListenersCount())));
        if (item.isMyProfile()) {
            listeningImageView.setVisibility(View.GONE);
        } else {
            listeningImageView.setImageDrawable(context.getResources().getDrawable(
                    page.isListening() ? R.drawable.ic_listening_on : R.drawable.ic_listening_off));
        }

    }

    @OnClick(R.id.profile_section_iv)
    public void onItemSelected() {
        item.onItemSelected();
    }

    @OnClick(R.id.profile_section_listening_iv)
    public void onListenClicked() {
        item.onItemListen();
    }
}
