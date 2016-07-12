package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class Promotion {

    @Nonnull
    private final String id;
    private final Label label;
    @Nullable
    private final Integer days;
    private final boolean isExpired;
    private final int expiresAt;

    public Promotion(@Nonnull String id, Label label, @Nullable Integer days, boolean isExpired, int expiresAt) {
        this.id = id;
        this.label = label;
        this.days = days;
        this.isExpired = isExpired;
        this.expiresAt = expiresAt;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    public Label getLabel() {
        return label;
    }

    @Nullable
    public Integer getDays() {
        return days;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public int getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Promotion)) return false;
        final Promotion promotion = (Promotion) o;
        return isExpired == promotion.isExpired &&
                Objects.equal(id, promotion.id) &&
                Objects.equal(label, promotion.label) &&
                Objects.equal(expiresAt, promotion.expiresAt) &&
                Objects.equal(days, promotion.days);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, label, days, isExpired, expiresAt);
    }
}
