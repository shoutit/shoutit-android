package com.shoutit.app.android.view.listeningsandlisteners;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.viewholders.NoDataViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ListeningsAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_PROFILE = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    private final Picasso picasso;

    @Inject
    public ListeningsAdapter(@ForActivity @Nonnull Context context,
                             Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }


    public class ProfileViewHolder extends ViewHolderManager.BaseViewHolder<ListeningsAndListenersPresenter.ProfileAdapterItem> implements View.OnClickListener {

        @Bind(R.id.profile_avatar_iv)
        ImageView avatarImageView;
        @Bind(R.id.profile_name_tv)
        TextView nameTextView;
        @Bind(R.id.profile_listeners_tv)
        TextView listenerTextView;

        private final Target target;
        private ListeningsAndListenersPresenter.ProfileAdapterItem item;

        public ProfileViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            target = PicassoHelper.getRoundedBitmapTarget(context, avatarImageView,
                    context.getResources().getDimensionPixelSize(R.dimen.profile_section_avatar_corners));
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull ListeningsAndListenersPresenter.ProfileAdapterItem item) {
            this.item = item;
            final ProfileType profile = item.getProfile();

            picasso.load(profile.getImage())
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .into(target);

            nameTextView.setText(profile.getName());

            listenerTextView.setText(context.getString(R.string.profile_listeners,
                    TextHelper.formatListenersNumber(profile.getListenersCount())));
        }

        @Override
        public void onClick(View v) {
            item.openProfile();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PROFILE:
                return new ProfileViewHolder(layoutInflater.inflate(R.layout.profile_or_tag_item, parent, false));
            case VIEW_TYPE_EMPTY:
                return new NoDataViewHolder(layoutInflater.inflate(R.layout.listenings_empty, parent, false));
            default:
                throw new RuntimeException("Invalid view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof ListeningsAndListenersPresenter.ProfileAdapterItem) {
            return VIEW_TYPE_PROFILE;
        } else if (item instanceof NoDataAdapterItem) {
            return VIEW_TYPE_EMPTY;
        } else {
            throw new RuntimeException("Invalid view type: " + item.getClass().getSimpleName());
        }
    }
}
