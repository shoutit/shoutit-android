package com.shoutit.app.android.view.interests;

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
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.view.chats.chats_adapter.AvatarHelper;
import com.shoutit.app.android.viewholders.NoDataTextViewHolder;
import com.shoutit.app.android.viewholders.NoDataViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class InterestsAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_TAG = 1;
    private static final int VIEW_TYPE_EMPTY = 2;
    private static final int VIEW_TYPE_EMPTY_WITH_TEXT = 3;

    private final Picasso picasso;

    @Inject
    public InterestsAdapter(@ForActivity Context context,
                            Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    public class TagViewHolder extends ViewHolderManager.BaseViewHolder<InterestsPresenter.TagAdapterItem> {

        @Bind(R.id.profile_section_iv)
        ImageView avatarImageView;
        @Bind(R.id.profile_section_name_tv)
        TextView nameTextView;
        @Bind(R.id.profile_section_listeners_tv)
        TextView listenerTextView;
        @Bind(R.id.profile_section_listening_iv)
        ImageView listenImageView;

        private final Target target;
        private InterestsPresenter.TagAdapterItem item;

        public TagViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            target = PicassoHelper.getRoundedBitmapTarget(context, avatarImageView,
                    context.getResources().getDimensionPixelSize(R.dimen.profile_section_avatar_corners));
        }

        @Override
        public void bind(@Nonnull InterestsPresenter.TagAdapterItem item) {
            this.item = item;
            final TagDetail tag = item.getTag();

            picasso.load(tag.getImage())
                    .placeholder(AvatarHelper.getPlaceholderId(tag.getType()))
                    .into(target);

            nameTextView.setText(tag.getName());

            listenerTextView.setText(context.getString(R.string.profile_listeners,
                    TextHelper.formatListenersNumber(tag.getListenersCount())));
            setListeningIcon(tag.isListening());
        }

        private void setListeningIcon(boolean isListening) {
            listenImageView.setVisibility(View.VISIBLE);
            listenImageView.setImageDrawable(context.getResources().getDrawable(
                    isListening ? R.drawable.ic_listening_on : R.drawable.ic_listening_off));
        }

        @OnClick(R.id.profile_section_listening_iv)
        public void onListenClicked() {
            setListeningIcon(!item.getTag().isListening());
            item.onTagListened();
        }

        @OnClick(R.id.profile_section_container)
        public void onSectionItemSelected() {
            item.openTagProfile();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TAG:
                return new TagViewHolder(layoutInflater.inflate(R.layout.search_results_profile_item, parent, false));
            case VIEW_TYPE_EMPTY:
                return new NoDataViewHolder(layoutInflater.inflate(R.layout.listenings_empty, parent, false));
            case VIEW_TYPE_EMPTY_WITH_TEXT:
                return new NoDataTextViewHolder(layoutInflater.inflate(R.layout.no_data_text_adapter_item, parent, false));
            default:
                throw new RuntimeException("Invalid view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof InterestsPresenter.TagAdapterItem) {
            return VIEW_TYPE_TAG;
        } else if (item instanceof NoDataAdapterItem) {
            return VIEW_TYPE_EMPTY;
        } else if (item instanceof NoDataTextAdapterItem) {
            return VIEW_TYPE_EMPTY_WITH_TEXT;
        } else {
            throw new RuntimeException("Invalid view type: " + item.getClass().getSimpleName());
        }
    }
}

