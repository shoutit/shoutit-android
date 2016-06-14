package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class PromoteRequest {

    @Nonnull
    private final Option option;

    public PromoteRequest(@Nonnull String optionId) {
        this.option = new Option(optionId);
    }

    private class Option {
        private final String id;

        private Option(String id) {
            this.id = id;
        }
    }
}
