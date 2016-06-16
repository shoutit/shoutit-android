package com.shoutit.app.android.api.model;


import javax.annotation.Nonnull;

public class PromoteResponse {

    @Nonnull
    private final Promotion promotion;
    private final String success;

    public PromoteResponse(@Nonnull Promotion promotion, String success) {
        this.promotion = promotion;
        this.success = success;
    }

    @Nonnull
    public Promotion getPromotion() {
        return promotion;
    }

    public String getSuccess() {
        return success;
    }
}
