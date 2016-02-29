package com.shoutit.app.android.view.profile;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;

import rx.Observer;

public class ProfileAdapterItems {

    public static class UserNameAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        protected final User user;

        public UserNameAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof UserNameAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserNameAdapterItem && user.equals(((UserNameAdapterItem) item).getUser());
        }

        @Nonnull
        public User getUser() {
            return user;
        }
    }

    public static class UserInfoAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        protected final User user;

        public UserInfoAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof UserNameAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserNameAdapterItem && user.equals(((UserNameAdapterItem) item).getUser());
        }

        @Nonnull
        public User getUser() {
            return user;
        }
    }

    public static class ProfileSectionAdapterItem<T extends ProfileType> extends BaseNoIDAdapterItem {

        private final boolean isFirstItem;
        private final boolean isLastItem;
        @Nonnull
        private final T sectionItem;
        @Nonnull
        private final Observer<String> itemSelectedObserver;
        @Nonnull
        private final Observer<String> listenItemObserver;
        private final boolean isMyProfile;

        public ProfileSectionAdapterItem(boolean isFirstItem, boolean isLastItem,
                                         @Nonnull T sectionItem,
                                         @Nonnull Observer<String> itemSelectedObserver,
                                         @Nonnull Observer<String> listenItemObserver, boolean isMyProfile) {
            this.isFirstItem = isFirstItem;
            this.isLastItem = isLastItem;
            this.sectionItem = sectionItem;
            this.itemSelectedObserver = itemSelectedObserver;
            this.listenItemObserver = listenItemObserver;
            this.isMyProfile = isMyProfile;
        }

        public void onItemSelected() {
            itemSelectedObserver.onNext(sectionItem.getUserName());
        }

        public void onItemListen() {
            if (!sectionItem.isListening()) {
                listenItemObserver.onNext(sectionItem.getUserName());
            }
        }

        public boolean isMyProfile() {
            return isMyProfile;
        }

        public boolean isFirstItem() {
            return isFirstItem;
        }

        public boolean isLastItem() {
            return isLastItem;
        }

        @Nonnull
        public T getSectionItem() {
            return sectionItem;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileSectionAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileSectionAdapterItem && this.equals(item);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProfileSectionAdapterItem)) return false;
            final ProfileSectionAdapterItem<?> that = (ProfileSectionAdapterItem<?>) o;
            return isFirstItem == that.isFirstItem &&
                    isLastItem == that.isLastItem &&
                    isMyProfile == that.isMyProfile &&
                    Objects.equal(sectionItem, that.sectionItem);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(isFirstItem, isLastItem, sectionItem, isMyProfile);
        }
    }

    public static class SeeAllUserShoutsAdapterItem extends BaseNoIDAdapterItem {

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
            return item instanceof SeeAllUserShoutsAdapterItem && this.equals(item);
        }

        public void onSeeAllShouts() {
            showMoreShoutsObserver.onNext(userName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SeeAllUserShoutsAdapterItem)) return false;
            final SeeAllUserShoutsAdapterItem that = (SeeAllUserShoutsAdapterItem) o;
            return Objects.equal(userName, that.userName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(userName);
        }
    }

    public static abstract class ThreeIconsAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final User user;

        public ThreeIconsAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ThreeIconsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ThreeIconsAdapterItem && this.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ThreeIconsAdapterItem)) return false;
            final ThreeIconsAdapterItem that = (ThreeIconsAdapterItem) o;
            return Objects.equal(user, that.user);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(user);
        }
    }
}
