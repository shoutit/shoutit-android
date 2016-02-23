package com.shoutit.app.android.view.shout;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.api.model.Shout;

import javax.annotation.Nonnull;

import rx.Observer;

public class ShoutAdapterItems {

    public static class ShoutAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Observer<String> addToCartObserver;
        @Nonnull
        private final Shout shout;

        public ShoutAdapterItem(@Nonnull Observer<String> addToCartObserver, @Nonnull Shout shout) {
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
            return item instanceof ShoutAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SeeAllRelatesAdapterItem && this.equals(item);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShoutAdapterItem)) return false;
            final ShoutAdapterItem that = (ShoutAdapterItem) o;
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
            shoutSelectedObserver.onNext(shout.getUser().getName());
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

    public static class ViewProfileAdapterItem implements BaseAdapterItem {
        @Nonnull
        private final Observer<String> visitProfileObserver;
        @Nonnull
        private final String userName;

        public ViewProfileAdapterItem(@Nonnull Observer<String> visitProfileObserver, @Nonnull String userName) {
            this.visitProfileObserver = visitProfileObserver;
            this.userName = userName;
        }

        public void onViewUserProfileClicked() {
            visitProfileObserver.onNext(userName);
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ViewProfileAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ViewProfileAdapterItem;
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

    public static class HeaderAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final String title;

        public HeaderAdapterItem(@Nonnull String title) {
            this.title = title;
        }

        @Nonnull
        public String getTitle() {
            return title;
        }


        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof HeaderAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof HeaderAdapterItem && title.equals(((HeaderAdapterItem) item).getTitle());
        }
    }
}
