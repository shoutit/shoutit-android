package com.shoutit.app.android.api.model;


import com.google.common.base.Objects;
import com.shoutit.app.android.model.Stats;

public class Page extends BaseProfile {

    public Page(String id, String type, String username, String name, String firstName,
                String lastName, boolean isActivated, String image, String cover, boolean isListening,
                int listenersCount, UserLocation location, Stats stats, boolean isOwner, String email) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount, location, isOwner, stats, email);
    }

    @Override
    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;

        return new Page(id, type, username, name, firstName, lastName, isActivated, image, cover,
                newIsListening, newListenersCount, location, getStats(), isOwner, getEmail());
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
        return Objects.hashCode(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount, getStats());
    }

    public static Page withIsListening(Page page, boolean isListening) {
        int listenersCount = isListening ? page.listenersCount + 1 : page.listenersCount - 1;
        return new Page(page.id, page.type, page.username, page.name, page.firstName,
                page.lastName, page.isActivated, page.image, page.cover, isListening,
                listenersCount, page.location, page.getStats(), page.isOwner, page.getEmail());
    }
}
