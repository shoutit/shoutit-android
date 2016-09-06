package com.shoutit.app.android.view.postsignup.users;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.view.chats.chats_adapter.AvatarHelper;
import com.shoutit.app.android.viewholders.HeaderViewHolder;
import com.shoutit.app.android.viewholders.NoDataTextViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostSignUpUsersAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_SUGGESTIONS = 1;
    private static final int VIEW_TYPE_NO_DATA = 2;
    private static final int VIEW_TYPE_HEADER = 3;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public PostSignUpUsersAdapter(@ForActivity @Nonnull Context context, @Nonnull Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SUGGESTIONS:
                return new SuggestionViewHolder(layoutInflater.inflate(R.layout.search_results_profile_item, parent, false), context, picasso);
            case VIEW_TYPE_NO_DATA:
                return new NoDataTextViewHolder(layoutInflater.inflate(R.layout.no_data_text_adapter_item, parent, false));
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(layoutInflater.inflate(R.layout.post_signup_header, parent, false));
            default:
                throw new RuntimeException("Unknown view type:" + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof PostSignupSecondPresenter.SuggestionAdapterItem) {
            return VIEW_TYPE_SUGGESTIONS;
        } else if (item instanceof NoDataTextAdapterItem) {
            return VIEW_TYPE_NO_DATA;
        } else if (item instanceof HeaderAdapterItem) {
            return VIEW_TYPE_HEADER;
        } else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }

    public class SuggestionViewHolder extends ViewHolderManager.BaseViewHolder<PostSignupSecondPresenter.SuggestionAdapterItem> {
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

        private PostSignupSecondPresenter.SuggestionAdapterItem item;
        private final Target target;

        public SuggestionViewHolder(View itemView, Context context, Picasso picasso) {
            super(itemView);
            this.context = context;
            this.picasso = picasso;
            ButterKnife.bind(this, itemView);

            target = PicassoHelper.getRoundedBitmapTarget(context, avatarImageView,
                    context.getResources().getDimensionPixelSize(R.dimen.profile_section_avatar_corners));
        }

        @Override
        public void bind(@Nonnull PostSignupSecondPresenter.SuggestionAdapterItem item) {
            this.item = item;
            final BaseProfile baseProfile = item.getBaseprofile();

            picasso.load(baseProfile.getImage())
                    .placeholder(AvatarHelper.getPlaceholderId(baseProfile.getType()))
                    .into(target);

            nameTextView.setText(baseProfile.getName());
            listenerTextView.setText(context.getString(R.string.profile_listeners,
                    TextHelper.formatListenersNumber(baseProfile.getListenersCount())));
            setListeningIcon(baseProfile.isListening());
        }

        private void setListeningIcon(boolean isListening) {
            listeningImageView.setImageDrawable(context.getResources().getDrawable(
                    isListening ? R.drawable.ic_listening_on : R.drawable.ic_listening_off));
        }

        @OnClick(R.id.profile_section_listening_iv)
        public void onListenClicked() {
            setListeningIcon(!item.getBaseprofile().isListening());
            item.onItemClicked();
        }
    }
}
