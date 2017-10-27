package com.shoutit.app.android.view.shout;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.BookmarkHelper;
import com.shoutit.app.android.utils.PriceUtils;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;

public class ShoutAdapterItems {

    public static class MainShoutAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Observer<String> addToCartObserver;
        @Nonnull
        private final Observer<String> onCategoryClickedObserver;
        @Nonnull
        private final Observer<BaseProfile> visitProfileObserver;
        @Nonnull
        private final Observer<Boolean> likeClickedObserver;
        @Nonnull
        private final Shout shout;
        @Nonnull
        private final Resources mResources;
        @NonNull
        private final Observable<Boolean> mBookmarObservable;
        @NonNull
        private final Observer<Pair<String, Boolean>> mBookmarkObserver;
        @NonNull
        private final Observer<Shout> markAsObserver;
        private final boolean isShoutOwner;
        private final boolean isNormalUser;
        @NonNull
        private final Observable<Boolean> mEnableBookmarkObservable;

        public MainShoutAdapterItem(@Nonnull Observer<String> addToCartObserver,
                                    @Nonnull Observer<String> onCategoryClickedObserver,
                                    @Nonnull Observer<BaseProfile> visitProfileObserver,
                                    @Nonnull Observer<Boolean> likeClickedObserver,
                                    @Nonnull Shout shout,
                                    @Nonnull Resources resources,
                                    @NonNull Observable<Boolean> bookmarObservable,
                                    @NonNull Observer<Pair<String, Boolean>> bookmarkObserver,
                                    @NonNull Observer<Shout> markAsObserver,
                                    boolean isShoutOwner, boolean isNormalUser,
                                    @NonNull Observable<Boolean> enableBookmarkObservable) {
            this.addToCartObserver = addToCartObserver;
            this.onCategoryClickedObserver = onCategoryClickedObserver;
            this.visitProfileObserver = visitProfileObserver;
            this.likeClickedObserver = likeClickedObserver;
            this.shout = shout;
            mResources = resources;
            mBookmarObservable = bookmarObservable;
            mBookmarkObserver = bookmarkObserver;
            this.markAsObserver = markAsObserver;
            this.isShoutOwner = isShoutOwner;
            this.isNormalUser = isNormalUser;
            mEnableBookmarkObservable = enableBookmarkObservable;
        }

        @NonNull
        public Observable<Boolean> getEnableBookmarkObservable() {
            return mEnableBookmarkObservable;
        }

        public void onBookmarkSelectionChanged(boolean checked) {
            mBookmarkObserver.onNext(Pair.create(shout.getId(), checked));
        }

        @NonNull
        public Observable<Boolean> getBookmarObservable() {
            return mBookmarObservable;
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }

        public void addToCartClicked() {
            addToCartObserver.onNext(null);
        }

        @Nullable
        public String getShoutPrice() {
            final Long price = shout.getPrice();
            if (price == null) {
                return null;
            } else {
                return PriceUtils.formatPriceWithCurrency(shout.getPrice(), mResources, shout.getCurrency());
            }
        }

        public boolean isShoutOwner() {
            return isShoutOwner;
        }

        public boolean isNormalUser() {
            return isNormalUser;
        }

        public void onLikeClicked() {
            likeClickedObserver.onNext(shout.isLiked());
        }

        @Override
        public long adapterId() {
            return shout.getId().hashCode();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof MainShoutAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof MainShoutAdapterItem && equals(item);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MainShoutAdapterItem)) return false;
            final MainShoutAdapterItem that = (MainShoutAdapterItem) o;
            return Objects.equal(shout, that.shout);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(shout);
        }

        public void onCategoryClick(String slug) {
            onCategoryClickedObserver.onNext(slug);
        }

        public void onHeaderClick() {
            visitProfileObserver.onNext(shout.getProfile());
        }

        public void onMarkAsClick() {
            markAsObserver.onNext(shout);
        }
    }

    public static class UserShoutAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Shout shout;

        @Nonnull
        private final Observer<String> shoutSelectedObserver;
        @Nonnull
        private final Resources mResources;

        public UserShoutAdapterItem(@Nonnull Shout shout,
                                    @Nonnull Observer<String> shoutSelectedObserver,
                                    @Nonnull Resources resources) {
            this.shout = shout;
            this.shoutSelectedObserver = shoutSelectedObserver;
            mResources = resources;
        }

        public void onShoutSelected() {
            shoutSelectedObserver.onNext(shout.getId());
        }

        @Nullable
        public String getShoutPrice() {
            final Long price = shout.getPrice();
            if (price == null) {
                return null;
            } else {
                return PriceUtils.formatPriceWithCurrency(shout.getPrice(), mResources, shout.getCurrency());
            }
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }

        @Override
        public long adapterId() {
            return shout.hashCode();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof UserShoutAdapterItem && ((UserShoutAdapterItem) item).shout.getId().equals(shout.getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserShoutAdapterItem && equals(item);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserShoutAdapterItem)) return false;
            final UserShoutAdapterItem that = (UserShoutAdapterItem) o;
            return Objects.equal(shout, that.shout);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(shout);
        }
    }

    public static class VisitProfileAdapterItem implements BaseAdapterItem {
        @Nonnull
        private final Observer<BaseProfile> visitProfileObserver;
        @Nonnull
        private final BaseProfile user;

        public VisitProfileAdapterItem(@Nonnull Observer<BaseProfile> visitProfileObserver, @Nonnull BaseProfile user) {
            this.visitProfileObserver = visitProfileObserver;
            this.user = user;
        }

        public void onViewUserProfileClicked() {
            visitProfileObserver.onNext(user);
        }

        @Nonnull
        public String getName() {
            if (user.isUser()) {
                return user.getFirstName().toUpperCase();
            } else {
                return user.getName().toUpperCase();
            }
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final VisitProfileAdapterItem that = (VisitProfileAdapterItem) o;
            return Objects.equal(user, that.user);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(user);
        }

        @Override
        public long adapterId() {
            return user.hashCode();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof VisitProfileAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof VisitProfileAdapterItem && equals(item);
        }
    }

    public static class SeeAllRelatesAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final String shoutId;
        @Nonnull
        private final Observer<String> seeAllObserver;

        public SeeAllRelatesAdapterItem(@Nonnull String shoutId,
                                        @Nonnull Observer<String> seeAllObserver) {
            this.shoutId = shoutId;
            this.seeAllObserver = seeAllObserver;
        }

        public void onSeeAllClicked() {
            seeAllObserver.onNext(shoutId);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final SeeAllRelatesAdapterItem that = (SeeAllRelatesAdapterItem) o;
            return Objects.equal(shoutId, that.shoutId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(shoutId);
        }

        @Override
        public long adapterId() {
            return shoutId.hashCode();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof SeeAllRelatesAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SeeAllRelatesAdapterItem && item.equals(this);
        }
    }

    public static class RelatedContainerAdapterItem implements BaseAdapterItem {

        private final List<BaseAdapterItem> items;

        public RelatedContainerAdapterItem(List<BaseAdapterItem> items) {
            this.items = items;
        }

        public List<BaseAdapterItem> getItems() {
            return items;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final RelatedContainerAdapterItem that = (RelatedContainerAdapterItem) o;
            return Objects.equal(items, that.items);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(items);
        }

        @Override
        public long adapterId() {
            return items.hashCode();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof RelatedContainerAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof RelatedContainerAdapterItem && item.equals(this);
        }
    }
}
