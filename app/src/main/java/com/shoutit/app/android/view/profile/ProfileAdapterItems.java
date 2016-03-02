package com.shoutit.app.android.view.profile;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.User;

import javax.annotation.Nonnull;

import rx.Observer;

public class ProfileAdapterItems {

    public static class NameAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        protected final User user;

        public NameAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof NameAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof NameAdapterItem && user.equals(((NameAdapterItem) item).getUser());
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
            return item instanceof NameAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof NameAdapterItem && user.equals(((NameAdapterItem) item).getUser());
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
        private final User user;
        @Nonnull
        private final T sectionItem;
        @Nonnull
        private final Observer<ProfilePresenter.UserWithItemToListen> listenItemObserver;
        private final boolean isMyProfile;

        public ProfileSectionAdapterItem(boolean isFirstItem,
                                         boolean isLastItem,
                                         @Nonnull User user,
                                         @Nonnull T sectionItem,
                                         @Nonnull Observer<ProfilePresenter.UserWithItemToListen> listenItemObserver,
                                         boolean isMyProfile) {
            this.isFirstItem = isFirstItem;
            this.isLastItem = isLastItem;
            this.user = user;
            this.sectionItem = sectionItem;
            this.listenItemObserver = listenItemObserver;
            this.isMyProfile = isMyProfile;
        }

        public void onItemListen() {
            listenItemObserver.onNext(new ProfilePresenter.UserWithItemToListen(user, sectionItem));
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
            // Done intentionally
            return false;
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
        protected final User user;

        public ThreeIconsAdapterItem(@Nonnull User user) {
            this.user = user;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ThreeIconsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            // Done intentionally, cuz the same User object is returned if listenProfile profile fail
            return false;
        }

        @Nonnull
        public User getUser() {
            return user;
        }

    }
}
