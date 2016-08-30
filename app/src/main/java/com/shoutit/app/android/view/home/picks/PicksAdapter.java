package com.shoutit.app.android.view.home.picks;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
import com.shoutit.app.android.view.home.PicksDiscoversAdapter;
import com.shoutit.app.android.view.home.HomePresenter;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.viewholders.ShoutViewHolder;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;

public class PicksAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_DISCOVER_HEADER = 1;
    private static final int VIEW_TYPE_DISCOVERS_CONTAINER = 2;
    private static final int VIEW_TYPE_HEADER = 3;
    private static final int VIEW_TYPE_PUBLIC_CHAT = 4;
    private static final int VIEW_TYPE_POPULAR_PAGES = 5;
    public static final int VIEW_TYPE_TRENDING_SHOUT = 6;
    private static final int VIEW_TYPE_SEARCH_FAVOURITES = 7;

    @Nonnull
    private final PicksDiscoversAdapter homeDiscoversAdapter;
    private final Picasso picasso;

    @Inject
    public PicksAdapter(@ForActivity @Nonnull Context context,
                        Picasso picasso) {
        super(context);
        this.picasso = picasso;
        homeDiscoversAdapter = new PicksDiscoversAdapter(picasso, context);
    }

    class DiscoverHeaderViewHolder extends ViewHolderManager.BaseViewHolder<PicksAdapterItems.DiscoverHeaderAdapterItem> {
        @Bind(R.id.picks_discover_header_tv)
        TextView headerTv;

        private PicksAdapterItems.DiscoverHeaderAdapterItem item;

        public DiscoverHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull PicksAdapterItems.DiscoverHeaderAdapterItem item) {
            this.item = item;
            headerTv.setText(context.getString(R.string.home_discover_header, item.getCity()));
        }

        @OnClick(R.id.picks_discover_view_all)
        public void onViewAllClick() {
            item.viewAllDiscovers();
        }
    }

    class DiscoverContainerViewHolder extends ViewHolderManager.BaseViewHolder<HomePresenter.DiscoverContainerAdapterItem> {
        @Bind(R.id.picks_discover_recycler_view)
        RecyclerView recyclerView;

        private Subscription subscription;

        public DiscoverContainerViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            final MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
        }

        @Override
        public void bind(@Nonnull HomePresenter.DiscoverContainerAdapterItem item) {
            recycle();

            recyclerView.setAdapter(homeDiscoversAdapter);

            subscription = Observable.just(item.getAdapterItems())
                    .subscribe(homeDiscoversAdapter);
        }

        @Override
        public void onViewRecycled() {
            recycle();
            super.onViewRecycled();
        }

        private void recycle() {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    class HeaderViewHolder extends ViewHolderManager.BaseViewHolder<PicksAdapterItems.HeaderItem> {

        @Bind(R.id.picks_header_title_tv)
        TextView tileTv;
        @Bind(R.id.picks_header_view_all)
        TextView viewAllTv;

        private PicksAdapterItems.HeaderItem headerItem;

        public HeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull PicksAdapterItems.HeaderItem headerItem) {
            this.headerItem = headerItem;

            if (headerItem instanceof PicksAdapterItems.ViewAllChatsAdapterItem) {
                tileTv.setText(R.string.picks_view_all_chats);
                viewAllTv.setText(R.string.picks_public_chats);
            } else if (headerItem instanceof PicksAdapterItems.ViewAllShoutsAdapterItem) {
                tileTv.setText(R.string.picks_view_all_shouts);
                viewAllTv.setText(R.string.picks_trending_shouts);
            } else {
                throw new RuntimeException("Unknown item type: " + headerItem.getClass().getSimpleName());
            }
        }

        @OnClick(R.id.picks_header_view_all)
        public void onViewAllClicked() {
            headerItem.viewAllClicked();
        }
    }

    class ChatViewHolder extends ViewHolderManager.BaseViewHolder<PicksAdapterItems.ChatAdapterItem> {

        @Bind(R.id.picks_chat_name_tv)
        TextView chatNameTv;

        public ChatViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull PicksAdapterItems.ChatAdapterItem chatAdapterItem) {
            final Conversation conversation = chatAdapterItem.getConversation();
            chatNameTv.setText(conversation.getTitle());
        }
    }

    class PopularPagesViewHolder extends ViewHolderManager.BaseViewHolder<PicksAdapterItems.PopularPagesAdapterItem> {

        @Bind(R.id.popular_pages_first_page)
        ViewGroup firstPageContainer;
        @Bind(R.id.popular_pages_second_page)
        ViewGroup secondPageContainer;

        private final ImageView firstPageIv;
        private final TextView firstPageTitleTv;
        private final TextView firstPageSubtitleTv;

        private final ImageView secondPageIv;
        private final TextView secondPageTitleTv;
        private final TextView secondPageSubtitleTv;

        public PopularPagesViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            firstPageIv = ButterKnife.findById(firstPageContainer, R.id.dark_card_image);
            firstPageTitleTv = ButterKnife.findById(firstPageContainer, R.id.dark_card_title);
            firstPageSubtitleTv = ButterKnife.findById(firstPageContainer, R.id.dark_card_subtitle);

            secondPageIv = ButterKnife.findById(secondPageContainer, R.id.dark_card_image);
            secondPageTitleTv = ButterKnife.findById(secondPageContainer, R.id.dark_card_title);
            secondPageSubtitleTv = ButterKnife.findById(secondPageContainer, R.id.dark_card_subtitle);
        }

        @Override
        public void bind(@Nonnull PicksAdapterItems.PopularPagesAdapterItem popularPagesAdapterItem) {
            final List<Page> pages =  popularPagesAdapterItem.getPages();
            setData(pages.get(0), firstPageIv, firstPageTitleTv, firstPageSubtitleTv);
            setData(pages.get(1), secondPageIv, secondPageTitleTv, secondPageSubtitleTv);
        }

        public void setData(Page page, ImageView imageView, TextView titleTv, TextView subtitleTv) {
            picasso.load(page.getImage())
                    .fit()
                    .placeholder(R.drawable.pattern_placeholder)
                    .centerCrop()
                    .into(imageView);

            titleTv.setText(page.getName());
        }
    }


    class StartSearchViewHolder extends ViewHolderManager.BaseViewHolder<PicksAdapterItems.StartSearchingAdapterItem> implements View.OnClickListener {

        private PicksAdapterItems.StartSearchingAdapterItem item;

        public StartSearchViewHolder(@Nonnull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull PicksAdapterItems.StartSearchingAdapterItem item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            item.startSearching();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_DISCOVER_HEADER:
                return new DiscoverHeaderViewHolder(layoutInflater.inflate(R.layout.picks_discover_header, parent, false));
            case VIEW_TYPE_DISCOVERS_CONTAINER:
                return new DiscoverContainerViewHolder(layoutInflater.inflate(R.layout.picks_discover_container_item, parent, false));
            case VIEW_TYPE_PUBLIC_CHAT:
                return new ChatViewHolder(layoutInflater.inflate(R.layout.picks_chats_item, parent, false));
            case VIEW_TYPE_POPULAR_PAGES:
                return new PopularPagesViewHolder(layoutInflater.inflate(R.layout.picks_popular_pages_item, parent, false));
            case VIEW_TYPE_TRENDING_SHOUT:
                return new ShoutViewHolder(layoutInflater.inflate(R.layout.shout_item_grid, parent, false), picasso);
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(layoutInflater.inflate(R.layout.picks_header, parent, false));
            case VIEW_TYPE_SEARCH_FAVOURITES:
                return new StartSearchViewHolder(layoutInflater.inflate(R.layout.picks_start_search_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof PicksAdapterItems.DiscoverHeaderAdapterItem) {
            return VIEW_TYPE_DISCOVER_HEADER;
        } else if (item instanceof PicksAdapterItems.DiscoverContainerAdapterItem) {
            return VIEW_TYPE_DISCOVERS_CONTAINER;
        } else if (item instanceof PicksAdapterItems.HeaderItem) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof PicksAdapterItems.ChatAdapterItem) {
            return VIEW_TYPE_PUBLIC_CHAT;
        } else if (item instanceof PicksAdapterItems.PopularPagesAdapterItem) {
            return VIEW_TYPE_POPULAR_PAGES;
        } else if (item instanceof ShoutAdapterItem) {
            return VIEW_TYPE_TRENDING_SHOUT;
        } else if (item instanceof PicksAdapterItems.StartSearchingAdapterItem) {
            return VIEW_TYPE_SEARCH_FAVOURITES;
        } else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }
}
