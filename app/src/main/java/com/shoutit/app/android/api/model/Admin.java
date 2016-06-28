package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

public class Admin extends BaseProfile {

    public Admin(String id, String type, String username, String name, String firstName,
                 String lastName, boolean isActivated, String image, String cover, boolean isListening,
                 int listenersCount, UserLocation location, boolean isOwner, String email) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover,
                isListening, listenersCount, location, isOwner, null, email);
    }

    @Override
    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;

        return new Admin(id, type, username, name, firstName, lastName, isActivated, image, cover,
                newIsListening, newListenersCount, location, isOwner, getEmail());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Admin)) return false;
        final Admin admin = (Admin) o;
        return isActivated == admin.isActivated &&
                isListening == admin.isListening &&
                listenersCount == admin.listenersCount &&
                Objects.equal(id, admin.id) &&
                Objects.equal(type, admin.type) &&
                Objects.equal(username, admin.username) &&
                Objects.equal(name, admin.name) &&
                Objects.equal(firstName, admin.firstName) &&
                Objects.equal(lastName, admin.lastName) &&
                Objects.equal(image, admin.image) &&
                Objects.equal(cover, admin.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount);
    }

    public static Admin withIsListening(Admin admin, boolean isListening) {
        int listenersCount = isListening ? admin.listenersCount + 1 : admin.listenersCount - 1;
        return new Admin(admin.id, admin.type, admin.username, admin.name, admin.firstName,
                admin.lastName, admin.isActivated, admin.image, admin.cover, isListening,
                listenersCount, admin.location, admin.isOwner, admin.getEmail());
    }
}

