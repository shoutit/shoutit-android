package com.shoutit.app.android.view.shout;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.FbAdAdapterItem;
import com.shoutit.app.android.adapteritems.HeaderAdapterItem;
import com.shoutit.app.android.api.model.Filter;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.Video;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.DateTimeUtils;
import com.shoutit.app.android.utils.PicassoHelper;
import com.shoutit.app.android.utils.ResourcesHelper;
import com.shoutit.app.android.viewholders.FbAdLinearViewHolder;
import com.shoutit.app.android.viewholders.HeaderViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class ShoutAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_SHOUT = 1;
    public static final int VIEW_TYPE_USER_SHOUTS = 2;
    private static final int VIEW_TYPE_VISIT_PROFILE = 3;
    public static final int VIEW_TYPE_RELATED_SHOUTS_CONTAINER = 4;
    private static final int VIEW_TYPE_HEADER = 5;
    private static final int VIEW_TYPE_FB_AD = 6;

    @Nonnull
    private final Picasso picasso;
    @Nonnull
    private final ShoutImagesPagerAdapter imagesPagerAdapter;
    @Nonnull
    private final RelatedShoutsAdapter relatedShoutsAdapter;

    @Inject
    public ShoutAdapter(@ForActivity @Nonnull Context context, @Nonnull Picasso picasso,
                        @Nonnull ShoutImagesPagerAdapter imagesPagerAdapter,
                        @Nonnull RelatedShoutsAdapter relatedShoutsAdapter) {
        super(context);
        this.picasso = picasso;
        this.imagesPagerAdapter = imagesPagerAdapter;
        this.relatedShoutsAdapter = relatedShoutsAdapter;
    }

    class ShoutViewHolder extends ViewHolderManager.BaseViewHolder<ShoutAdapterItems.MainShoutAdapterItem> {
        private final int initDetailsContainerChildsCount;
        @Bind(R.id.shout_avatar_iv)
        ImageView avatarImageView;
        @Bind(R.id.shout_name_tv)
        TextView nameTextView;
        @Bind(R.id.shout_user_location_tv)
        TextView userLocationTextView;
        @Bind(R.id.shout_type_label)
        TextView labelTextView;
        @Bind(R.id.shout_view_pager)
        ViewPager shoutViewPager;
        @Bind(R.id.activity_intro_page_indicators)
        CirclePageIndicator pageIndicator;
        @Bind(R.id.shouts_title_tv)
        TextView titleTextView;
        @Bind(R.id.shouts_price_tv)
        TextView priceTextView;
        @Bind(R.id.shout_available_tv)
        TextView availableTextView;
        @Bind(R.id.shout_date_tv)
        TextView dateTextView;
        @Bind(R.id.shout_description_tv)
        TextView descriptionTextView;
        @Bind(R.id.shout_category_tv)
        TextView categoryTextView;
        @Bind(R.id.shout_location_tv)
        TextView locationTextView;
        @Bind(R.id.shout_location_flag_iv)
        ImageView flagImageView;
        @Bind(R.id.shout_details_container)
        LinearLayout detailsContainer;
        @Bind(R.id.shout_detail_location_row)
        View locationContainer;
        @Bind(R.id.shout_item_description_container)
        View descriptionContainer;
        @Bind(R.id.shout_item_description_header)
        View descriptionHeader;
        @Bind(R.id.shout_pager_container)
        View viewPagerContainer;
        @Bind(R.id.shout_like)
        ImageView shoutLikeImageView;
        @Bind(R.id.shout_bookmark)
        CheckBox bookmarkCheckbox;

        private final Target flagTarget;
        private ShoutAdapterItems.MainShoutAdapterItem item;
        private CompositeSubscription subscription;

        public ShoutViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            flagTarget = PicassoHelper.getRoundedBitmapTarget(context, flagImageView);
            initDetailsContainerChildsCount = detailsContainer.getChildCount();
        }

        @OnClick(R.id.shout_add_to_cart_btn)
        public void onAddToCartClicked() {
            item.addToCartClicked();
        }

        @OnClick(R.id.shout_like)
        public void onShoutLikeClicked() {
            item.onLikeClicked();
        }

        @Override
        public void bind(@Nonnull ShoutAdapterItems.MainShoutAdapterItem item) {
            unsubscribe();
            this.item = item;
            final Shout shout = item.getShout();
            final User user = shout.getProfile();
            final UserLocation location = shout.getLocation();

            shoutViewPager.setAdapter(imagesPagerAdapter);
            pageIndicator.setViewPager(shoutViewPager);

            picasso.load(user.getImage())
                    .resizeDimen(R.dimen.shout_avatar_size, R.dimen.shout_avatar_size)
                    .centerCrop()
                    .placeholder(R.drawable.ic_rect_avatar_placeholder)
                    .error(R.drawable.ic_rect_avatar_placeholder)
                    .into(avatarImageView);

            shoutLikeImageView.setImageResource(shout.isLiked() ? R.drawable.likeon : R.drawable.likeoff);

            nameTextView.setText(user.getName());

            setUserLocation(location);

            labelTextView.setText(shout.getTypeResId());

            titleTextView.setText(shout.getTitle());

            priceTextView.setText(item.getShoutPrice());

            setNumber(shout.getNumber(), shout.getAvailableCount());

            setIndicatorVisibility(shout.getImages(), shout.getVideos());
            setMedia(shout.getImages(), shout.getVideos());

            setDescription(shout.getText());

            dateTextView.setText(DateTimeUtils.getShoutDetailDate(shout.getDatePublishedInMillis()));
            categoryTextView.setText(shout.getCategory().getName());

            setFlag(location);

            setUpFilters(shout);

            subscription = new CompositeSubscription(
                    item.getEnableBookmarkObservable().subscribe(RxView.enabled(bookmarkCheckbox)),
                    item.getBookmarObservable().subscribe(RxCompoundButton.checked(bookmarkCheckbox)),
                    RxView.clicks(bookmarkCheckbox).subscribe(aVoid -> {
                        item.onBookmarkSelectionChanged(bookmarkCheckbox.isChecked());
                    }));
        }

        private void setFlag(UserLocation location) {
            final Optional<Integer> flagResId = ResourcesHelper.getCountryResId(context, location);
            if (flagResId.isPresent()) {
                picasso.load(flagResId.get())
                        .into(flagTarget);
            }
        }

        private void setDescription(String text) {
            descriptionTextView.setText(text);
            final boolean isDescription = !TextUtils.isEmpty(text);
            descriptionHeader.setVisibility(isDescription ? View.VISIBLE : View.GONE);
            descriptionContainer.setVisibility(isDescription ? View.VISIBLE : View.GONE);
        }

        private void setIndicatorVisibility(List<String> images, List<Video> videos) {
            boolean hasMoreThanOneItem = images.size() + videos.size() > 1;
            pageIndicator.setVisibility(hasMoreThanOneItem ? View.VISIBLE : View.GONE);
        }

        private void setNumber(float number, int availableCount) {
            if (number == 0) {
                availableTextView.setText(context.getString(R.string.shout_available, availableCount));
            } else {
                availableTextView.setText(context.getString(R.string.shout_only_available, availableCount));
            }
        }

        private void setUserLocation(UserLocation location) {
            if (location != null) {
                userLocationTextView.setText(context.getString(R.string.shout_user_location,
                        location.getCity(), location.getCountry()));
                locationTextView.setText(location.getCity());
            }
        }

        private void setMedia(List<String> images, List<Video> videos) {
            imagesPagerAdapter.setData(images, videos);
        }

        @Override
        public void onViewRecycled() {
            super.onViewRecycled();
            unsubscribe();
        }

        private void unsubscribe() {
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }

        private void setUpFilters(Shout shout) {
            if (detailsContainer.getChildCount() > initDetailsContainerChildsCount) {
                return;
            }

            for (int i = 0; i < shout.getFilters().size(); i++) {
                final Filter filter = shout.getFilters().get(i);
                final LinearLayout view;
                if (i % 2 == 0) {
                    view = (LinearLayout) layoutInflater.inflate(R.layout.shout_detail_row_light, detailsContainer, false);
                } else {
                    view = (LinearLayout) layoutInflater.inflate(R.layout.shout_detail_row_dark, detailsContainer, false);
                }

                ((TextView) view.getChildAt(0)).setText(filter.getName());
                ((TextView) view.getChildAt(1)).setText(filter.getValue().getName());

                Preconditions.checkArgument(detailsContainer.getChildCount() >= 2);
                detailsContainer.addView(view, detailsContainer.getChildCount() - 1);
                view.setOnClickListener(v -> item.onCategoryClick(filter.getValue().getSlug()));
            }

            final boolean isLastElementLight = detailsContainer.getChildCount() % 2 != 0;
            locationContainer.setBackgroundColor(ContextCompat.getColor(context, isLastElementLight ? android.R.color.white : R.color.black_12));
        }

        @OnClick(R.id.shout_detail_category_row)
        public void onCategoryClick() {
            item.onCategoryClick(item.getShout().getCategory().getSlug());
        }

        @OnClick(R.id.shout_user_header)
        public void onHeaderClick() {
            item.onHeaderClick();
        }
    }

    public class UserShoutViewHolder extends ViewHolderManager.BaseViewHolder<ShoutAdapterItems.UserShoutAdapterItem> implements View.OnClickListener {
        @Bind(R.id.shout_grid_image_view)
        ImageView cardImageView;
        @Bind(R.id.shout_grid_title_tv)
        TextView titleTextView;
        @Bind(R.id.home_feed_card_name_tv)
        TextView nameTextView;
        @Bind(R.id.shout_grid_price_tv)
        TextView cardPriceTextView;
        private ShoutAdapterItems.UserShoutAdapterItem item;


        public UserShoutViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull ShoutAdapterItems.UserShoutAdapterItem item) {
            this.item = item;
            final Shout shout = item.getShout();
            titleTextView.setText(shout.getTitle());
            titleTextView.setVisibility(TextUtils.isEmpty(shout.getTitle()) ?
                    View.GONE : View.VISIBLE);
            nameTextView.setText(shout.getProfile().getName());

            final String shoutPrice = item.getShoutPrice();
            cardPriceTextView.setText(shoutPrice);

            picasso.load(shout.getThumbnail())
                    .placeholder(R.drawable.pattern_placeholder)
                    .fit()
                    .centerCrop()
                    .into(cardImageView);
        }

        @Override
        public void onClick(View v) {
            item.onShoutSelected();
        }
    }

    public class VisitProfileViewHolder extends ViewHolderManager.BaseViewHolder<ShoutAdapterItems.VisitProfileAdapterItem> implements View.OnClickListener {
        @Bind(R.id.button_gray_btn)
        Button button;

        private ShoutAdapterItems.VisitProfileAdapterItem item;

        public VisitProfileViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull ShoutAdapterItems.VisitProfileAdapterItem item) {
            this.item = item;
            button.setText(context.getString(R.string.shout_visit_profile, item.getName()));
        }

        @Override
        public void onClick(View v) {
            item.onViewUserProfileClicked();
        }
    }

    public class RelatedShoutsContainerViewHolder extends ViewHolderManager.BaseViewHolder<ShoutAdapterItems.RelatedContainerAdapterItem> {

        @Bind(R.id.shout_related_container_recycler_view)
        RecyclerView recyclerView;

        private Subscription subscription;

        public RelatedShoutsContainerViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            final int itemSpacing = context.getResources().getDimensionPixelOffset(R.dimen.shout_related_item_spacing);
            final RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.left = itemSpacing;
                    outRect.right = itemSpacing;
                }
            };
            final LinearLayoutManager layoutManager = new LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.addItemDecoration(itemDecoration);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(relatedShoutsAdapter);
        }

        @Override
        public void bind(@Nonnull ShoutAdapterItems.RelatedContainerAdapterItem item) {
            recycle();

            subscription = Observable.just(item.getItems())
                    .subscribe(relatedShoutsAdapter);
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

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SHOUT:
                return new ShoutViewHolder(layoutInflater.inflate(R.layout.shout_item, parent, false));
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(layoutInflater.inflate(R.layout.base_header_item, parent, false));
            case VIEW_TYPE_USER_SHOUTS:
                return new UserShoutViewHolder(layoutInflater.inflate(R.layout.shout_item_grid, parent, false));
            case VIEW_TYPE_VISIT_PROFILE:
                return new VisitProfileViewHolder(layoutInflater.inflate(R.layout.button_gray_with_stroke, parent, false));
            case VIEW_TYPE_RELATED_SHOUTS_CONTAINER:
                return new RelatedShoutsContainerViewHolder(layoutInflater.inflate(R.layout.shout_related_shout_container_item, parent, false));
            case VIEW_TYPE_FB_AD:
                return new FbAdLinearViewHolder(layoutInflater.inflate(R.layout.ad_list_layout, parent, false), context);
            default:
                throw new RuntimeException("Unknown view type");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof ShoutAdapterItems.MainShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (item instanceof HeaderAdapterItem) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof ShoutAdapterItems.RelatedContainerAdapterItem) {
            return VIEW_TYPE_RELATED_SHOUTS_CONTAINER;
        } else if (item instanceof ShoutAdapterItems.VisitProfileAdapterItem) {
            return VIEW_TYPE_VISIT_PROFILE;
        } else if (item instanceof ShoutAdapterItems.UserShoutAdapterItem) {
            return VIEW_TYPE_USER_SHOUTS;
        } else if (item instanceof FbAdAdapterItem) {
            return VIEW_TYPE_FB_AD;
        } else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }
}
