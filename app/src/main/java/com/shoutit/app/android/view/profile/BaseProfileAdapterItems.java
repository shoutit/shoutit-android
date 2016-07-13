package com.shoutit.app.android.view.profile;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.api.model.RelatedTagsResponse;
import com.shoutit.app.android.api.model.TagDetail;
import com.shoutit.app.android.view.profile.tagprofile.TagProfilePresenter;

import javax.annotation.Nonnull;

import rx.Observer;

public class BaseProfileAdapterItems {

    public abstract static class BaseProfileSectionItem<T extends ProfileType> extends BaseNoIDAdapterItem {

        @Nonnull
        private final Observer<ProfileType> profileToOpenObserver;
        @Nonnull
        private final Observer<Object> actionOnlyForLoggedInUserObserver;
        private final boolean isFirstItem;
        private final boolean isLastItem;
        private final boolean isOnlyItemInSection;
        @Nonnull
        private final T sectionItem;
        private final boolean isUserLoggedIn;
        protected int listenersCount;

        protected BaseProfileSectionItem(@Nonnull Observer<ProfileType> profileToOpenObserver,
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
            listenersCount = sectionItem.getListenersCount();
        }

        public void onSectionItemSelected() {
            profileToOpenObserver.onNext(sectionItem);
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
            return item instanceof BaseProfileSectionItem &&
                    item.equals(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProfileSectionAdapterItem)) return false;
            final BaseProfileSectionItem<?> that = (BaseProfileSectionItem<?>) o;
            return isFirstItem == that.isFirstItem &&
                    isLastItem == that.isLastItem &&
                    Objects.equal(sectionItem, that.sectionItem) &&
                    listenersCount == that.listenersCount;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(isFirstItem, isLastItem, sectionItem, listenersCount);
        }
    }

    public static class ProfileSectionAdapterItem extends BaseProfileSectionItem<BaseProfile> {
        @Nonnull
        private final Observer<BaseProfile> listenItemObserver;
        @Nullable
        private final String loggedInUserName;
        @Nonnull
        private final BaseProfile pageOrAdmin;

        public ProfileSectionAdapterItem(boolean isFirstItem,
                                         boolean isLastItem,
                                         @Nonnull BaseProfile pageOrAdmin,
                                         @Nonnull Observer<BaseProfile> listenItemObserver,
                                         @Nonnull Observer<ProfileType> profileToOpenObserver,
                                         @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                         @Nullable String loggedInUserName,
                                         boolean isUserLoggedIn,
                                         boolean isOnlyItemInSection) {
            super(profileToOpenObserver, actionOnlyForLoggedInUserObserver, isFirstItem, isLastItem, isOnlyItemInSection, pageOrAdmin, isUserLoggedIn);
            this.pageOrAdmin = pageOrAdmin;
            this.listenItemObserver = listenItemObserver;
            this.loggedInUserName = loggedInUserName;
        }

        @Override
        public void onItemListen() {
            if (pageOrAdmin.isListening()) {
                --listenersCount;
            } else {
                ++listenersCount;
            }

            listenItemObserver.onNext(pageOrAdmin);
        }

        @Override
        public boolean isSectionItemProfileMyProfile() {
            return pageOrAdmin.getUsername().equals(loggedInUserName);
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
                                     @Nonnull Observer<ProfileType> tagProfileToOpenObserver,
                                     @Nonnull Observer<Object> actionOnlyForLoggedInUserObserver,
                                     boolean isUserLoggedIn,
                                     boolean isOnlyItemInSection) {
            super(tagProfileToOpenObserver, actionOnlyForLoggedInUserObserver, isFirstItem, isLastItem, isOnlyItemInSection, relatedTag, isUserLoggedIn);
            this.relatedTag = relatedTag;
            this.lastResponse = lastResponse;
            this.listenItemObserver = listenItemObserver;
        }

        public void onItemListen() {
            if (relatedTag.isListening()) {
                --listenersCount;
            } else {
                ++listenersCount;
            }

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
            return item instanceof TagInfoAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            // Done intentionally, cuz the same Tag object should be returned if listenProfile profile fail,
            // so it will clear listen selection
            return false;
        }
    }

}
