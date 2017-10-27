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
                int listenersCount, UserLocation location, Stats stats, boolean isOwner, String email,
                User admin, String about, String description, String phone, String founded,
                String impressum, String overview, String mission, String generalInfo, boolean isVerified,
                boolean isPublished, LinkedAccounts linkedAccounts, ConversationDetails conversation,
                boolean isListener, String website) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening,
                listenersCount, location, isOwner, stats, email, linkedAccounts, conversation,
                isListener, website);
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
    public BaseProfile getListenedProfile(int newListenersCount) {
        boolean newIsListening = !isListening;

        return new Page(id, type, username, name, firstName, lastName, isActivated, image, cover,
                newIsListening, newListenersCount, location, getStats(), isOwner, getEmail(),
                admin, about, description, phone, founded, impressum, overview, mission, generalInfo,
                isVerified, isPublished, linkedAccounts, conversation, isListener, website);
    }

    @Nonnull
    @Override
    public BaseProfile withUpdatedStats(@Nonnull Stats newStats) {
        return new Page(id, type, username, name, firstName, lastName, isActivated, image, cover,
                isListening, listenersCount, location, newStats, isOwner, getEmail(), admin, about,
                description, phone, founded, impressum, overview, mission, generalInfo, isVerified,
                isPublished, linkedAccounts, conversation, isListener, website);
    }

    public User getAdmin() {
        return admin;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        if (!super.equals(o)) return false;
        final Page page = (Page) o;
        return isVerified == page.isVerified &&
                isPublished == page.isPublished &&
                Objects.equal(admin, page.admin) &&
                Objects.equal(about, page.about) &&
                Objects.equal(description, page.description) &&
                Objects.equal(phone, page.phone) &&
                Objects.equal(founded, page.founded) &&
                Objects.equal(impressum, page.impressum) &&
                Objects.equal(overview, page.overview) &&
                Objects.equal(mission, page.mission) &&
                Objects.equal(generalInfo, page.generalInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), admin, about, description, phone,
                founded, impressum, overview, mission, generalInfo, isVerified, isPublished);
    }
}
