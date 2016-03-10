package com.shoutit.app.android.view.profile;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.R;
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
        @Nonnull
        private final Observer<String> webUrlClickedObserver;

        public UserInfoAdapterItem(@Nonnull User user, @Nonnull Observer<String> webUrlClickedObserver) {
            this.user = user;
            this.webUrlClickedObserver = webUrlClickedObserver;
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

        public String getBioText() {
            if (ProfileType.USER.equals(user.getType())) {
                return user.getBio();
            } else {
                return user.getAbout();
            }
        }

        @DrawableRes
        public int getBioResId() {
            if (ProfileType.USER.equals(user.getType())) {
                return R.drawable.ic_bio;
            } else {
                return R.drawable.ic_about;
            }
        }

        public void onWebsiteClicked() {
            webUrlClickedObserver.onNext(user.getWebsite());
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
        @Nonnull
        private final Observer<String> profileToOpenObserver;
        @Nonnull
        private final Observer<Object> actionOnlyForLoggedInUserObserver;
        @Nullable
        private final String loggedInUserName;
        private final boolean isUserLoggedIn;
        private final boolean isOnlyItemInSection;

        public ProfileSectionAdapterItem(boolean isFirstItem,
                                         boolean isLastItem,
                                         @Nonnull User user,
                                         @Nonnull T sectionItem,
                                         @Nonnull Observer<ProfilePresenter.UserWithItemToListen> listenItemObserver,
                                         @Nonnull Observer<String> profileToOpenObserver,
                                         @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                         @Nullable String loggedInUserName,
                                         boolean isUserLoggedIn,
                                         boolean isOnlyItemInSection) {
            this.isFirstItem = isFirstItem;
            this.isLastItem = isLastItem;
            this.user = user;
            this.sectionItem = sectionItem;
            this.listenItemObserver = listenItemObserver;
            this.profileToOpenObserver = profileToOpenObserver;
            this.actionOnlyForLoggedInUserObserver = actionOnlyForLoggedInUserObserver;
            this.loggedInUserName = loggedInUserName;
            this.isUserLoggedIn = isUserLoggedIn;
            this.isOnlyItemInSection = isOnlyItemInSection;
        }

        public void onItemListen() {
            listenItemObserver.onNext(new ProfilePresenter.UserWithItemToListen(user, sectionItem));
        }

        public void onSectionItemSelected() {
            profileToOpenObserver.onNext(sectionItem.getUsername());
        }

        public void onActionOnlyForLoggedInUser() {
            actionOnlyForLoggedInUserObserver.onNext(null);
        }

        public boolean isSectionItemProfileMyProfile() {
            return sectionItem.getUsername().equals(loggedInUserName);
        }

        public boolean isFirstItem() {
            return isFirstItem;
        }

        public boolean isLastItem() {
            return isLastItem;
        }

        public boolean isOnlyItemInSection() {
            return isOnlyItemInSection;
        }

        @Nonnull
        public T getSectionItem() {
            return sectionItem;
        }

        public boolean isUserLoggedIn() {
            return isUserLoggedIn;
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
                    Objects.equal(sectionItem, that.sectionItem);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(isFirstItem, isLastItem, sectionItem);
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

    public static class MyUserNameAdapterItem extends ProfileAdapterItems.NameAdapterItem {

        @NonNull
        private final Observer<Object> editProfileClickObserver;
        @Nonnull
        private final Observer<Object> notificationsClickObserver;

        public MyUserNameAdapterItem(@Nonnull User user, @NonNull Observer<Object> editProfileClickObserver,
                                     @Nonnull Observer<Object> notificationsClickObserver) {
            super(user);
            this.editProfileClickObserver = editProfileClickObserver;
            this.notificationsClickObserver = notificationsClickObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.NameAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.NameAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }

        public void onEditProfileClicked() {
            editProfileClickObserver.onNext(null);
        }

        public void onShowNotificationClicked() {
            notificationsClickObserver.onNext(null);
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


    public static class UserNameAdapterItem extends ProfileAdapterItems.NameAdapterItem {

        @Nonnull
        private final Observer<Object> moreMenuOptionClickedObserver;

        public UserNameAdapterItem(@Nonnull User user, @NonNull Observer<Object> moreMenuOptionClickedObserver) {
            super(user);
            this.moreMenuOptionClickedObserver = moreMenuOptionClickedObserver;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.NameAdapterItem && !user.equals(item);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof ProfileAdapterItems.NameAdapterItem && user.equals(item);
        }

        @Nonnull
        public User getUser() {
            return user;
        }

        public void onMoreMenuOptionClicked() {
            moreMenuOptionClickedObserver.onNext(null);
        }
    }

    public static class MyUserThreeIconsAdapterItem extends ProfileAdapterItems.ThreeIconsAdapterItem {

        public MyUserThreeIconsAdapterItem(@Nonnull User user) {
            super(user);
        }
    }

    public static class UserThreeIconsAdapterItem extends ProfileAdapterItems.ThreeIconsAdapterItem {

        private final boolean isUserLoggedIn;
        @Nonnull
        private final Observer<Object> actionOnlyForLoggedInUserObserver;
        @Nonnull
        private final Observer<String> onChatIconClickedObserver;
        @Nonnull
        private final Observer<User> onListenActionClickedObserver;

        public UserThreeIconsAdapterItem(@Nonnull User user, boolean isUserLoggedIn,
                                         @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                         @Nonnull Observer<String> onChatIconClickedObserver,
                                         @Nonnull Observer<User> onListenActionClickedObserver) {
            super(user);
            this.isUserLoggedIn = isUserLoggedIn;
            this.actionOnlyForLoggedInUserObserver = actionOnlyForLoggedInUserObserver;
            this.onChatIconClickedObserver = onChatIconClickedObserver;
            this.onListenActionClickedObserver = onListenActionClickedObserver;
        }

        public boolean isUserLoggedIn() {
            return isUserLoggedIn;
        }

        public void onChatActionClicked() {
            onChatIconClickedObserver.onNext(user.getUsername());
        }

        public void onListenActionClicked() {
            onListenActionClickedObserver.onNext(user);
        }

        public void onActionOnlyForLoggedInUser() {
            actionOnlyForLoggedInUserObserver.onNext(null);
        }
    }
}
