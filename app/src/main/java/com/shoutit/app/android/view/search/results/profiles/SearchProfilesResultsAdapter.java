package com.shoutit.app.android.view.search.results.profiles;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchProfilesResultsAdapter extends BaseAdapter {

    private final Picasso picasso;

    @Inject
    public SearchProfilesResultsAdapter(@ForActivity @Nonnull Context context, Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    public class ProfileViewHolder extends ViewHolderManager.BaseViewHolder<SearchProfilesResultsPresenter.ProfileAdapterItem> {

        @Bind(R.id.profile_section_iv)
        ImageView avatarImageView;
        @Bind(R.id.profile_section_name_tv)
        TextView nameTextView;
        @Bind(R.id.profile_section_listeners_tv)
        TextView listenerTextView;
        @Bind(R.id.profile_section_listening_iv)
        ImageView listeningImageView;

        private final Target target;
        private SearchProfilesResultsPresenter.ProfileAdapterItem item;

        public ProfileViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            target = PicassoHelper.getRoundedBitmapTarget(context, avatarImageView,
                    context.getResources().getDimensionPixelSize(R.dimen.profile_section_avatar_corners));
        }

        @Override
        public void bind(@Nonnull SearchProfilesResultsPresenter.ProfileAdapterItem item) {
            this.item = item;
            final User profile = item.getProfile();

            picasso.load(profile.getImage())
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .into(target);

            nameTextView.setText(profile.getName());
            listenerTextView.setText(context.getString(R.string.profile_listeners,
                    TextHelper.formatListenersNumber(profile.getListenersCount())));
            setListeningIcon(profile.isListening());
        }

        private void setListeningIcon(boolean isListening) {
            if (item.isProfileMine()) {
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
                setListeningIcon(!item.getProfile().isListening());
                item.onProfileListened();
            } else {
                item.onActionOnlyForLoggedInUser();
            }
        }

        @OnClick(R.id.profile_section_selector)
        public void onSectionItemSelected() {
            item.onProfileItemSelected();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProfileViewHolder(layoutInflater.inflate(R.layout.search_results_profile_item, parent, false));
    }
}
