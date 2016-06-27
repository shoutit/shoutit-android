package com.shoutit.app.android.api.model;


import com.google.common.base.Objects;

public class Page extends BaseProfile {

    private final PageStats stats;

    public Page(String id, String type, String username, String name, String firstName,
                String lastName, boolean isActivated, String image, String cover, boolean isListening,
                int listenersCount, UserLocation location, PageStats stats, boolean isOwner) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount, location, isOwner);
        this.stats = stats;
    }

    @Override
    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;

        return new Page(id, type, username, name, firstName, lastName, isActivated, image, cover,
                newIsListening, newListenersCount, location, stats, isOwner);
    }

    public PageStats getStats() {
        return stats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        final Page page = (Page) o;
        return isActivated == page.isActivated &&
                isListening == page.isListening &&
                listenersCount == page.listenersCount &&
                Objects.equal(id, page.id) &&
                Objects.equal(type, page.type) &&
                Objects.equal(username, page.username) &&
                Objects.equal(name, page.name) &&
                Objects.equal(firstName, page.firstName) &&
                Objects.equal(lastName, page.lastName) &&
                Objects.equal(image, page.image) &&
                Objects.equal(cover, page.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount, stats);
    }

    public static Page withIsListening(Page page, boolean isListening) {
        int listenersCount = isListening ? page.listenersCount + 1 : page.listenersCount - 1;
        return new Page(page.id, page.type, page.username, page.name, page.firstName,
                page.lastName, page.isActivated, page.image, page.cover, isListening,
                listenersCount, page.location, page.stats, page.isOwner);
    }
}
