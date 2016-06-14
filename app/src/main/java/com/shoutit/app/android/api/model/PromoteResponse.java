package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import javax.annotation.Nonnull;

public class PromoteResponse {

    @Nonnull
    private final Promotion promotion;
    private final String success;

    public PromoteResponse(@Nonnull Promotion promotion, String success) {
        this.promotion = promotion;
        this.success = success;
    }

    private class Promotion {
        @Nonnull
        private final String id;
        @Nonnull
        private final PromoteLabel label;
        @Nullable
        private final Integer days;
        private final long expiresAt;

        private Promotion(@Nonnull String id, @Nonnull PromoteLabel label,
                          @Nullable Integer days, long expiresAt) {
            this.id = id;
            this.label = label;
            this.days = days;
            this.expiresAt = expiresAt;
        }

        @Nonnull
        public String getId() {
            return id;
        }

        @Nonnull
        public PromoteLabel getLabel() {
            return label;
        }

        @Nullable
        public Integer getDays() {
            return days;
        }

        public long getExpiresAt() {
            return expiresAt;
        }
    }

    @Nonnull
    public Promotion getPromotion() {
        return promotion;
    }

    public String getSuccess() {
        return success;
    }
}
