package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Contact {

    @Nullable
    private final String name;
    @Nonnull
    private final List<String> mobiles;
    @Nonnull
    private final List<String> emails;

    public Contact(@Nullable String name, @Nonnull List<String> mobiles, @Nonnull List<String> emails) {
        this.name = name;
        this.mobiles = mobiles;
        this.emails = emails;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nonnull
    public List<String> getMobiles() {
        return mobiles;
    }

    @Nonnull
    public List<String> getEmails() {
        return emails;
    }

    public Contact withNewMobile(@Nonnull String name, @Nonnull String phoneNumber) {
        final ImmutableList<String> mobiles = ImmutableList.<String>builder()
                .addAll(this.mobiles)
                .add(phoneNumber)
                .build();

        return new Contact(name, mobiles, emails);
    }

    public Contact withNewEmail(@Nonnull String name, @Nonnull String email) {
        final ImmutableList<String> emails = ImmutableList.<String>builder()
                .addAll(this.emails)
                .add(email)
                .build();

        return new Contact(name, mobiles, emails);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return Objects.equal(name, contact.name) &&
                Objects.equal(mobiles, contact.mobiles) &&
                Objects.equal(emails, contact.emails);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, mobiles, emails);
    }
}