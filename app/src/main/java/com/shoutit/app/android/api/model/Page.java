package com.shoutit.app.android.api.model;


import android.support.annotation.NonNull;

import com.google.common.base.Objects;
import com.shoutit.app.android.model.Stats;

import javax.annotation.Nonnull;

public class Page extends BaseProfile {

    // DetailedProfile of the currently logged in admin
    private final User admin;
    private final String about;
    private final String description;
    private final String phone;
    private final String founded;
    private final String impressum;
    private final String overview;
    private final String mission;
    private final String generalInfo;
    private final boolean isVerified;
    private final boolean isPublished;

    public Page(String id, String type, String username, String name, String firstName,
                String lastName, boolean isActivated, String image, String cover, boolean isListening,
                int listenersCount, UserLocation location, Stats stats, boolean isOwner, String email, User admin, String about, String description, String phone, String founded, String impressum, String overview, String mission, String generalInfo, boolean isVerified, boolean isPublished, LinkedAccounts linkedAccounts) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount, location, isOwner, stats, email, linkedAccounts);
        this.admin = admin;
        this.about = about;
        this.description = description;
        this.phone = phone;
        this.founded = founded;
        this.impressum = impressum;
        this.overview = overview;
        this.mission = mission;
        this.generalInfo = generalInfo;
        this.isVerified = isVerified;
        this.isPublished = isPublished;
    }

    @NonNull
    @Override
    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;

        return new Page(id, type, username, name, firstName, lastName, isActivated, image, cover,
                newIsListening, newListenersCount, location, getStats(), isOwner, getEmail(), admin, about, description, phone, founded, impressum, overview, mission, generalInfo, isVerified, isPublished, linkedAccounts);
    }

    @Nonnull
    @Override
    public BaseProfile withUpdatedStats(@Nonnull Stats newStats) {
        return new Page(id, type, username, name, firstName, lastName, isActivated, image, cover,
                isListening, listenersCount, location, newStats, isOwner, getEmail(), admin, about, description, phone, founded, impressum, overview, mission, generalInfo, isVerified, isPublished, linkedAccounts);
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

    public String getAbout() {
        return about;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getFounded() {
        return founded;
    }

    public String getImpressum() {
        return impressum;
    }

    public String getOverview() {
        return overview;
    }

    public String getMission() {
        return mission;
    }

    public String getGeneralInfo() {
        return generalInfo;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean isPublished() {
        return isPublished;
    }
}
