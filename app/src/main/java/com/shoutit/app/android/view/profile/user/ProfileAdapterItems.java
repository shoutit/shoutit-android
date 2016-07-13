package com.shoutit.app.android.view.profile.user;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.BusinessVerificationResponse;
import com.shoutit.app.android.api.model.ConversationDetails;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.view.profile.ChatInfo;

import javax.annotation.Nonnull;

import rx.Observable;
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
            return item instanceof UserInfoAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof UserInfoAdapterItem && user.equals(((UserInfoAdapterItem) item).getUser());
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



    public static class MyUserNameAdapterItem extends ProfileAdapterItems.NameAdapterItem {

        @NonNull
        private final Observer<Object> editProfileClickObserver;
        @Nonnull
        private final Observer<Object> notificationsClickObserver;
        @Nonnull
        private final Observer<User> verifyAccountClickObserver;
        @Nonnull
        private final Observable<Integer> notificationsUnreadObservable;
        private final boolean shouldShowProfileBadge;
        @Nonnull
        private final Observable<BusinessVerificationResponse> pageVerificationObservable;

        public MyUserNameAdapterItem(@Nonnull User user, @NonNull Observer<Object> editProfileClickObserver,
                                     @Nonnull Observer<Object> notificationsClickObserver,
                                     @Nonnull Observer<User> verifyAccountClickObserver,
                                     @Nonnull Observable<Integer> notificationsUnreadObservable,
                                     boolean shouldShowProfileBadge,
                                     @Nonnull Observable<BusinessVerificationResponse> pageVerificationObservable) {
            super(user);
            this.editProfileClickObserver = editProfileClickObserver;
            this.notificationsClickObserver = notificationsClickObserver;
            this.verifyAccountClickObserver = verifyAccountClickObserver;
            this.notificationsUnreadObservable = notificationsUnreadObservable;
            this.shouldShowProfileBadge = shouldShowProfileBadge;
            this.pageVerificationObservable = pageVerificationObservable;
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
        public Observable<Integer> getNotificationsUnreadObservable() {
            return notificationsUnreadObservable;
        }

        @Nonnull
        public Observable<BusinessVerificationResponse> getPageVerificationObservable() {
            return pageVerificationObservable;
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

        public void onVerifyAccountClick() {
            verifyAccountClickObserver.onNext(user);
        }

        public boolean shouldShowProfileBadge() {
            return shouldShowProfileBadge;
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

    public static class MyProfileThreeIconsAdapterItem extends ProfileAdapterItems.ThreeIconsAdapterItem {

        @Nonnull
        private final Observer<Object> listeningsClickObserver;
        @Nonnull
        private final Observer<Object> interestsClickObserver;
        @Nonnull
        private final Observer<Object> listenersClickObserver;

        public MyProfileThreeIconsAdapterItem(@Nonnull User user,
                                              @Nonnull Observer<Object> listeningsClickObserver,
                                              @Nonnull Observer<Object> interestsClickObserver,
                                              @Nonnull Observer<Object> listenersClickObserver) {
            super(user);
            this.listeningsClickObserver = listeningsClickObserver;
            this.interestsClickObserver = interestsClickObserver;
            this.listenersClickObserver = listenersClickObserver;
        }

        public void onListengsClick() {
            listeningsClickObserver.onNext(null);
        }

        public void onInterestsClick() {
            interestsClickObserver.onNext(null);
        }

        public void onListenersClick() {
            listenersClickObserver.onNext(null);
        }
    }

    public static class UserThreeIconsAdapterItem extends ProfileAdapterItems.ThreeIconsAdapterItem {

        private final boolean isNormalUser;
        @Nonnull
        private final Observer<Object> actionOnlyForLoggedInUserObserver;
        @Nonnull
        private final Observer<ChatInfo> onChatIconClickedObserver;
        @Nonnull
        private final Observer<User> onListenActionClickedObserver;

        public UserThreeIconsAdapterItem(@Nonnull User user, boolean isNormalUser,
                                         @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                         @Nonnull Observer<ChatInfo> onChatIconClickedObserver,
                                         @Nonnull Observer<User> onListenActionClickedObserver) {
            super(user);
            this.isNormalUser = isNormalUser;
            this.actionOnlyForLoggedInUserObserver = actionOnlyForLoggedInUserObserver;
            this.onChatIconClickedObserver = onChatIconClickedObserver;
            this.onListenActionClickedObserver = onListenActionClickedObserver;
        }

        public boolean isNormalUser() {
            return isNormalUser;
        }

        public void onChatActionClicked() {
            final ConversationDetails conversation = user.getConversation();
            onChatIconClickedObserver.onNext(new ChatInfo(user.getUsername(), conversation != null ? conversation.getId() : null, user.isListener(), isNormalUser));
        }

        public void onListenActionClicked() {
            onListenActionClickedObserver.onNext(user);
        }

        public void onActionOnlyForLoggedInUser() {
            actionOnlyForLoggedInUserObserver.onNext(null);
        }
    }

}
