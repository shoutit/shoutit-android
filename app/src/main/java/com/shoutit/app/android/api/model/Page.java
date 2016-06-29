package com.shoutit.app.android.api.model;


import com.google.common.base.Objects;
import com.shoutit.app.android.model.Stats;

public class Page extends BaseProfile {

    // DetailedProfile of the currently logged in admin
    private final User admin;

    public Page(String id, String type, String username, String name, String firstName,
                String lastName, boolean isActivated, String image, String cover, boolean isListening,
                int listenersCount, UserLocation location, Stats stats, boolean isOwner, String email, User admin) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount, location, isOwner, stats, email);
        this.admin = admin;
    }

    @Override
    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;

        return new Page(id, type, username, name, firstName, lastName, isActivated, image, cover,
                newIsListening, newListenersCount, location, getStats(), isOwner, getEmail(), admin);
    }

    public User getAdmin() {
        return admin;
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
                Objects.equal(admin, page.admin) &&
                Objects.equal(cover, page.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, username, name, firstName, lastName, isActivated,
                image, cover, isListening, listenersCount, getStats(), admin);
    }
}
