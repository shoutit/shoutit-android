package com.shoutit.app.android.view.profile;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.RelatedTagsResponse;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.view.profile.tagprofile.TagProfilePresenter;

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

    public abstract static class BaseProfileSectionItem<T extends ProfileType> extends BaseNoIDAdapterItem {

        @Nonnull
        private final Observer<String> profileToOpenObserver;
        @Nonnull
        private final Observer<Object> actionOnlyForLoggedInUserObserver;
        private final boolean isFirstItem;
        private final boolean isLastItem;
        private final boolean isOnlyItemInSection;
        @Nonnull
        private final T sectionItem;
        private final boolean isUserLoggedIn;

        protected BaseProfileSectionItem(@Nonnull Observer<String> profileToOpenObserver,
                                         @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                         boolean isFirstItem, boolean isLastItem, boolean isOnlyItemInSection,
                                         @Nonnull T sectionItem, boolean isUserLoggedIn) {
            this.profileToOpenObserver = profileToOpenObserver;
            this.actionOnlyForLoggedInUserObserver = actionOnlyForLoggedInUserObserver;
            this.isFirstItem = isFirstItem;
            this.isLastItem = isLastItem;
            this.isOnlyItemInSection = isOnlyItemInSection;
            this.sectionItem = sectionItem;
            this.isUserLoggedIn = isUserLoggedIn;
        }

        public void onSectionItemSelected() {
            profileToOpenObserver.onNext(sectionItem.getUsername());
        }

        public void onActionOnlyForLoggedInUser() {
            actionOnlyForLoggedInUserObserver.onNext(null);
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

        public abstract boolean isSectionItemProfileMyProfile();

        public abstract void onItemListen();

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof BaseProfileSectionItem;
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
            final BaseProfileSectionItem<?> that = (BaseProfileSectionItem<?>) o;
            return isFirstItem == that.isFirstItem &&
                    isLastItem == that.isLastItem &&
                    Objects.equal(sectionItem, that.sectionItem);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(isFirstItem, isLastItem, sectionItem);
        }
    }

    public static class ProfileSectionAdapterItem<T extends ProfileType> extends BaseProfileSectionItem {
        @Nonnull
        private final User user;
        @Nonnull
        private final Observer<UserOrPageProfilePresenter.UserWithItemToListen> listenItemObserver;
        @Nullable
        private final String loggedInUserName;
        private final T sectionItem;

        public ProfileSectionAdapterItem(boolean isFirstItem,
                                         boolean isLastItem,
                                         @Nonnull User user,
                                         @Nonnull T sectionItem,
                                         @Nonnull Observer<UserOrPageProfilePresenter.UserWithItemToListen> listenItemObserver,
                                         @Nonnull Observer<String> profileToOpenObserver,
                                         @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                         @Nullable String loggedInUserName,
                                         boolean isUserLoggedIn,
                                         boolean isOnlyItemInSection) {
            super(profileToOpenObserver, actionOnlyForLoggedInUserObserver, isFirstItem, isLastItem, isOnlyItemInSection, sectionItem, isUserLoggedIn);
            this.user = user;
            this.sectionItem = sectionItem;
            this.listenItemObserver = listenItemObserver;
            this.loggedInUserName = loggedInUserName;
        }

        @Override
        public void onItemListen() {
            listenItemObserver.onNext(new UserOrPageProfilePresenter.UserWithItemToListen(user, sectionItem));
        }

        @Override
        public boolean isSectionItemProfileMyProfile() {
            return sectionItem.getUsername().equals(loggedInUserName);
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
        @Nonnull
        private final Observer<Object> verifyAccountClickObserver;
        @Nonnull
        private final Observable<Integer> notificationsUnreadObservable;

        public MyUserNameAdapterItem(@Nonnull User user, @NonNull Observer<Object> editProfileClickObserver,
                                     @Nonnull Observer<Object> notificationsClickObserver,
                                     @Nonnull Observer<Object> verifyAccountClickObserver,
                                     @Nonnull Observable<Integer> notificationsUnreadObservable) {
            super(user);
            this.editProfileClickObserver = editProfileClickObserver;
            this.notificationsClickObserver = notificationsClickObserver;
            this.verifyAccountClickObserver = verifyAccountClickObserver;
            this.notificationsUnreadObservable = notificationsUnreadObservable;
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
            verifyAccountClickObserver.onNext(null);
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
            final Conversation conversation = user.getConversation();
            onChatIconClickedObserver.onNext(new ChatInfo(user.getUsername(), conversation != null ? conversation.getId() : null, user.isListener(), isNormalUser));
        }

        public void onListenActionClicked() {
            onListenActionClickedObserver.onNext(user);
        }

        public void onActionOnlyForLoggedInUser() {
            actionOnlyForLoggedInUserObserver.onNext(null);
        }
    }

    public static class TagInfoAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final TagDetail tagDetail;
        private final boolean isUserLoggedIn;
        @Nonnull
        private final Observer<Object> actionOnlyForLoggedInUserObserver;
        @Nonnull
        private final Observer<TagDetail> onListenActionClickedObserver;
        @Nonnull
        private final Observer<Object> moreMenuOptionClickedObserver;

        public TagInfoAdapterItem(@NonNull TagDetail tagDetail, boolean isUserLoggedIn,
                                  @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                  @Nonnull Observer<TagDetail> onListenActionClickedObserver,
                                  @Nonnull Observer<Object> moreMenuOptionClickedObserver) {
            this.isUserLoggedIn = isUserLoggedIn;
            this.actionOnlyForLoggedInUserObserver = actionOnlyForLoggedInUserObserver;
            this.onListenActionClickedObserver = onListenActionClickedObserver;
            this.moreMenuOptionClickedObserver = moreMenuOptionClickedObserver;
            this.tagDetail = tagDetail;
        }

        public boolean isUserLoggedIn() {
            return isUserLoggedIn;
        }

        public void onListenActionClicked() {
            onListenActionClickedObserver.onNext(tagDetail);
        }

        public void onActionOnlyForLoggedInUser() {
            actionOnlyForLoggedInUserObserver.onNext(null);
        }

        public void onMoreMenuOptionClicked() {
            moreMenuOptionClickedObserver.onNext(null);
        }

        @Nonnull
        public TagDetail getTagDetail() {
            return tagDetail;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ThreeIconsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            // Done intentionally, cuz the same Tag object should be returned if listenProfile profile fail,
            // so it will clear listen selection
            return false;
        }
    }

    public static class RelatedTagAdapterItem extends BaseProfileSectionItem {

        @Nonnull
        private final TagDetail relatedTag;
        @Nonnull
        private final RelatedTagsResponse lastResponse;
        @Nonnull
        private final Observer<TagProfilePresenter.ListenedTagWithRelatedTags> listenItemObserver;

        public RelatedTagAdapterItem(boolean isFirstItem,
                                     boolean isLastItem,
                                     @Nonnull TagDetail relatedTag,
                                     @Nonnull RelatedTagsResponse lastResponse,
                                     @Nonnull Observer<TagProfilePresenter.ListenedTagWithRelatedTags> listenItemObserver,
                                     @Nonnull Observer<String> tagProfileToOpenObserver,
                                     @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                     boolean isUserLoggedIn,
                                     boolean isOnlyItemInSection) {
            super(tagProfileToOpenObserver, actionOnlyForLoggedInUserObserver, isFirstItem, isLastItem, isOnlyItemInSection, relatedTag, isUserLoggedIn);
            this.relatedTag = relatedTag;
            this.lastResponse = lastResponse;
            this.listenItemObserver = listenItemObserver;
        }

        public void onItemListen() {
            listenItemObserver.onNext(new TagProfilePresenter.ListenedTagWithRelatedTags(lastResponse, relatedTag));
        }

        @Nonnull
        public TagDetail getTag() {
            return relatedTag;
        }

        @Override
        public boolean isSectionItemProfileMyProfile() {
            return false;
        }
    }
}
