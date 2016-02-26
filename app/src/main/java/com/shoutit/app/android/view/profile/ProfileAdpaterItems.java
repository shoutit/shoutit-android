package com.shoutit.app.android.view.profile;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.api.model.ProfileKind;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;

import rx.Observer;

public class ProfileAdpaterItems {

    public static class UserNameAdapterItem implements BaseAdapterItem {

        @Nonnull
        protected final User user;

        public UserNameAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof UserNameAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserNameAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }
    }

    public static class UserInfoAdapterItem implements BaseAdapterItem {

        @Nonnull
        protected final User user;

        public UserInfoAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof UserNameAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserNameAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }
    }

    public static class ProfileSectionAdapterItem<T extends ProfileKind> implements BaseAdapterItem {

        private final boolean isFirstItem;
        private final boolean isLastItem;
        @Nonnull
        private final T sectionItem;
        @Nonnull
        private final Observer<String> itemSelectedObserver;
        @Nonnull
        private final Observer<String> listenItemObserver;

        public ProfileSectionAdapterItem(boolean isFirstItem, boolean isLastItem,
                                         @Nonnull T sectionItem,
                                         @Nonnull Observer<String> itemSelectedObserver,
                                         @Nonnull Observer<String> listenItemObserver) {
            this.isFirstItem = isFirstItem;
            this.isLastItem = isLastItem;
            this.sectionItem = sectionItem;
            this.itemSelectedObserver = itemSelectedObserver;
            this.listenItemObserver = listenItemObserver;
        }

        public void onItemSelected() {
            itemSelectedObserver.onNext(sectionItem.getUserName());
        }

        public void onListenPage() {
            if (!sectionItem.isListening()) {
                listenItemObserver.onNext(sectionItem.getUserName());
            }
        }

        @Nonnull
        public T getSectionItem() {
            return sectionItem;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return false;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }
    }

    public static class SeeAllUserShoutsAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final Observer<String> showMoreShoutsObserver;
        @Nonnull
        private final String userName;

        public SeeAllUserShoutsAdapterItem(@Nonnull Observer<String> showMoreShoutsObserver,
                                           @Nonnull String userName) {
            this.showMoreShoutsObserver = showMoreShoutsObserver;
            this.userName = userName;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof SeeAllUserShoutsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof SeeAllUserShoutsAdapterItem;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        public void onSeeAllShouts() {
            showMoreShoutsObserver.onNext(userName);
        }
    }
}
