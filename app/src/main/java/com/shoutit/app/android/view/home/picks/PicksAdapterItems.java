package com.shoutit.app.android.view.home.picks;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.api.model.Conversation;
import com.shoutit.app.android.api.model.DiscoverChild;
import com.shoutit.app.android.api.model.Page;
import com.shoutit.app.android.model.LocationPointer;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observer;
import rx.subjects.PublishSubject;

public class PicksAdapterItems {

    public static class DiscoverAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final DiscoverChild discover;
        @Nonnull
        private final Observer<String> discoverSelectedSubject;

        public DiscoverAdapterItem(@Nonnull DiscoverChild discover,
                                   @Nonnull Observer<String> discoverSelectedSubject) {
            this.discover = discover;
            this.discoverSelectedSubject = discoverSelectedSubject;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverAdapterItem &&
                    discover.getId().equals(((DiscoverAdapterItem) item).getDiscover().getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return this.equals(item);
        }

        @Nonnull
        public DiscoverChild getDiscover() {
            return discover;
        }

        public void onDiscoverSelected() {
            discoverSelectedSubject.onNext(discover.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscoverAdapterItem)) return false;
            final DiscoverAdapterItem that = (DiscoverAdapterItem) o;
            return Objects.equal(discover, that.discover);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(discover);
        }
    }

    public static class DiscoverHeaderAdapterItem implements BaseAdapterItem {

        private final String city;
        private final Observer<Object> viewAllDiscovers;

        public DiscoverHeaderAdapterItem(String city,
                                         Observer<Object> viewAllDiscovers) {
            this.city = city;
            this.viewAllDiscovers = viewAllDiscovers;
        }

        @Override
        public long adapterId() {
            return BaseAdapterItem.NO_ID;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverHeaderAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverHeaderAdapterItem && this.equals(item);
        }

        public String getCity() {
            return city;
        }

        public void viewAllDiscovers() {
            viewAllDiscovers.onNext(null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscoverHeaderAdapterItem)) return false;
            final DiscoverHeaderAdapterItem that = (DiscoverHeaderAdapterItem) o;
            return Objects.equal(city, that.city);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(city);
        }
    }

    public static class DiscoverContainerAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final List<BaseAdapterItem> adapterItems;
        @Nonnull
        private final LocationPointer locationPointer;

        public DiscoverContainerAdapterItem(@Nonnull List<BaseAdapterItem> adapterItems,
                                            @Nonnull LocationPointer locationPointer) {
            this.adapterItems = adapterItems;
            this.locationPointer = locationPointer;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverContainerAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return item instanceof DiscoverContainerAdapterItem && this.equals(item);
        }

        @Nonnull
        public List<BaseAdapterItem> getAdapterItems() {
            return adapterItems;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscoverContainerAdapterItem)) return false;
            final DiscoverContainerAdapterItem that = (DiscoverContainerAdapterItem) o;
            return Objects.equal(adapterItems, that.adapterItems) &&
                    Objects.equal(locationPointer, that.locationPointer);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(locationPointer);
        }
    }

    public static abstract class HeaderItem extends BaseNoIDAdapterItem {

        private final Observer<Object> viewAllShoutsObserver;

        public HeaderItem(Observer<Object> viewAllShoutsObserver) {
            this.viewAllShoutsObserver = viewAllShoutsObserver;
        }

        public void viewAllClicked() {
            viewAllShoutsObserver.onNext(null);
        }
    }

    public static class ViewAllChatsAdapterItem extends HeaderItem {

        public ViewAllChatsAdapterItem(Observer<Object> viewAllShoutsObserver) {
            super(viewAllShoutsObserver);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof ViewAllChatsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return true;
        }
    }

    public static class ViewAllShoutsAdapterItem extends HeaderItem {

        public ViewAllShoutsAdapterItem(Observer<Object> viewAllShoutsObserver) {
            super(viewAllShoutsObserver);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof ViewAllShoutsAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return true;
        }
    }

    public static class ChatAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final Conversation conversation;
        private final PublishSubject<String> publicChatSelectedSubject;

        public ChatAdapterItem(@Nonnull Conversation conversation,
                               PublishSubject<String> publicChatSelectedSubject) {
            this.conversation = conversation;
            this.publicChatSelectedSubject = publicChatSelectedSubject;
        }

        public void publicChatSelected() {
            publicChatSelectedSubject.onNext(null);
        }

        @Nonnull
        public Conversation getConversation() {
            return conversation;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof ChatAdapterItem &&
                    conversation.getId().equals(((ChatAdapterItem) baseAdapterItem).getConversation().getId());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return conversation.equals(((ChatAdapterItem) baseAdapterItem).conversation);
        }
    }

    public static class PopularPagesAdapterItem extends BaseNoIDAdapterItem {

        @Nonnull
        private final List<Page> pages;
        @Nonnull
        private final Observer<Object> viewAllPagesObserver;

        public PopularPagesAdapterItem(@Nonnull List<Page> pages,
                                       @Nonnull Observer<Object> viewAllPagesObserver) {
            this.pages = pages;
            this.viewAllPagesObserver = viewAllPagesObserver;
        }

        public void viewAllPages() {
            viewAllPagesObserver.onNext(null);
        }

        @Nonnull
        public List<Page> getPages() {
            return pages;
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof PopularPagesAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof PopularPagesAdapterItem &&
                    ((PopularPagesAdapterItem) baseAdapterItem).pages.equals(pages);
        }
    }

    public static class StartSearchingAdapterItem extends BaseNoIDAdapterItem {

        private final Observer<Object> startSearchingObserver;

        public StartSearchingAdapterItem(Observer<Object> startSearchingObserver) {
            this.startSearchingObserver = startSearchingObserver;
        }
        
        public void startSearching() {
            startSearchingObserver.onNext(null);
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof StartSearchingAdapterItem;
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return true;
        }
    }
}
