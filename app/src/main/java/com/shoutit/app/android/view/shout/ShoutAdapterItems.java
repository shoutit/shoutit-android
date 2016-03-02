package com.shoutit.app.android.view.shout;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.api.model.Shout;
import com.shoutit.app.android.api.model.User;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observer;

public class ShoutAdapterItems {

    public static class MainShoutAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Observer<String> addToCartObserver;
        @Nonnull
        private final Shout shout;

        public MainShoutAdapterItem(@Nonnull Observer<String> addToCartObserver, @Nonnull Shout shout) {
            this.addToCartObserver = addToCartObserver;
            this.shout = shout;
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }

        public void addToCartClicked() {
            addToCartObserver.onNext(null);
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof MainShoutAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SeeAllRelatesAdapterItem && this.equals(item);
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
    }

    public static class UserShoutAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Shout shout;

        @Nonnull
        private final Observer<String> shoutSelectedObserver;

        public UserShoutAdapterItem(@Nonnull Shout shout,
                                    @Nonnull Observer<String> shoutSelectedObserver) {
            this.shout = shout;
            this.shoutSelectedObserver = shoutSelectedObserver;
        }

        public void onShoutSelected() {
            shoutSelectedObserver.onNext(shout.getId());
        }

        @Nonnull
        public Shout getShout() {
            return shout;
        }


        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof UserShoutAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserShoutAdapterItem && this.equals(item);
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
        private final Observer<User> visitProfileObserver;
        @Nonnull
        private final User user;

        public VisitProfileAdapterItem(@Nonnull Observer<User> visitProfileObserver, @Nonnull User user) {
            this.visitProfileObserver = visitProfileObserver;
            this.user = user;
        }

        public void onViewUserProfileClicked() {
            visitProfileObserver.onNext(user);
        }

        @Nonnull
        public String getName() {
            return user.getName().toUpperCase();
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof VisitProfileAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof VisitProfileAdapterItem;
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
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof SeeAllRelatesAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SeeAllRelatesAdapterItem;
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
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }
    }
}
