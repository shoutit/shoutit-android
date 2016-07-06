package com.shoutit.app.android.view.pages;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.shoutit.app.android.viewholders.NoDataTextViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PagesAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_PAGE = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    private final Picasso picasso;

    @Inject
    public PagesAdapter(@ForActivity @Nonnull Context context,
                        Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PAGE:
                return new PageViewHolder(layoutInflater.inflate(R.layout.my_page_item, parent, false));
            case VIEW_TYPE_EMPTY:
                return new NoDataTextViewHolder(layoutInflater.inflate(R.layout.no_data_text_adapter_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);

        if (item instanceof PageAdapterItem) {
            return VIEW_TYPE_PAGE;
        } else if (item instanceof NoDataTextAdapterItem) {
            return VIEW_TYPE_EMPTY;
        } else {
            throw new RuntimeException("Unknown view type");
        }
    }

    public class PageViewHolder extends ViewHolderManager.BaseViewHolder<PageAdapterItem> {

        @Bind(R.id.pages_avatar_iv)
        ImageView avatarIv;
        @Bind(R.id.pages_listeners_tv)
        TextView listenersTv;
        @Bind(R.id.pages_name_tv)
        TextView pageNameTv;
        @Bind(R.id.pages_location_tv)
        TextView locationTv;
        @Bind(R.id.pages_badge_tv)
        TextView badgeTv;

        private PageAdapterItem item;
        private final Target target;

        public PageViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            target = PicassoHelper.getRoundedBitmapTarget(context, avatarIv,
                    context.getResources().getDimensionPixelSize(R.dimen.profile_section_avatar_corners));
            itemView.setOnClickListener(v -> item.onPagesSelected());
        }

        @Override
        public void bind(@Nonnull PageAdapterItem pageAdapterItem) {
            item = pageAdapterItem;

            final User page = pageAdapterItem.getPage();

            picasso.load(page.getImage())
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .into(target);

            pageNameTv.setText(page.getName());
            if (page.getLocation() != null) {
                locationTv.setText(page.getLocation().getCity());
            } else {
                locationTv.setText(null);
            }

            if (page.getStats() != null) {
                badgeTv.setText(String.valueOf(page.getStats().getUnreadNotifications()));
            }

            listenersTv.setText(context.getString(R.string.profile_listeners,
                    TextHelper.formatListenersNumber(page.getListenersCount())));
        }
    }
}
